/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCompositeFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsMetadataMaintainedAttribute;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * AttributeStorage implementation driven by XStream. This implementation uses it's own FS storage to store attributes
 * in separate place then LocalStorage. This is the "old" default storage.
 * 
 * @author cstamas
 */
@Named
@Typed( value = AttributeStorage.class )
@Singleton
public class DefaultFSAttributeStorage extends AbstractFSAttributeStorage
{

    /** The xstream. */
    private final XStream xstream;

    /**
     * Instantiates a new FSX stream attribute storage.
     *
     * @param applicationEventMulticaster
     * @param applicationConfiguration
     */
    @Inject
    public DefaultFSAttributeStorage( final ApplicationEventMulticaster applicationEventMulticaster, final ApplicationConfiguration applicationConfiguration)
    {
        super( applicationEventMulticaster, applicationConfiguration);

        this.xstream = new XStream();
        this.xstream.alias( "file", DefaultStorageFileItem.class );
        this.xstream.alias( "compositeFile", DefaultStorageCompositeFileItem.class );
        this.xstream.alias( "collection", DefaultStorageCollectionItem.class );
        this.xstream.alias( "link", DefaultStorageLinkItem.class );
    }



    protected void doPutAttribute( StorageItem item, OutputStream outputStream ) throws IOException
    {
        xstream.toXML( item, outputStream );
        outputStream.flush();
    }

    // ==

    /**
     * Gets the attributes.
     * 
     * @param uid the uid
     * @return the attributes
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected AbstractStorageItem doGetAttributes( final RepositoryItemUid uid )
        throws IOException
    {
        final File target = getFileFromBase( uid );

        AbstractStorageItem result = null;

        boolean corrupt = false;

        if ( target.exists() && target.isFile() )
        {
            FileInputStream fis = null;

            try
            {
                fis = new FileInputStream( target );

                result = (AbstractStorageItem) xstream.fromXML( fis );

                result.setRepositoryItemUid( uid );

                // fixing remoteChecked
                if ( result.getRemoteChecked() == 0 || result.getRemoteChecked() == 1 )
                {
                    result.setRemoteChecked( System.currentTimeMillis() );

                    result.setExpired( true );
                }

                // fixing lastRequested
                if ( result.getLastRequested() == 0 )
                {
                    result.setLastRequested( System.currentTimeMillis() );
                }
            }
            catch ( IOException e )
            {
                getLogger().info( "While reading attributes of " + uid + " we got IOException:", e );

                throw e;
            }
            catch ( NullPointerException e )
            {
                // NEXUS-3911: seems that on malformed XML the XMLpull parser throws NPE?
                // org.xmlpull.mxp1.MXParser.fillBuf(MXParser.java:3020) : NPE
                // it is corrupt
                if ( getLogger().isDebugEnabled() )
                {
                    // we log the stacktrace
                    getLogger().info( "Attributes of " + uid + " are corrupt, deleting it.", e );
                }
                else
                {
                    // just remark about this
                    getLogger().info( "Attributes of " + uid + " are corrupt, deleting it." );
                }

                corrupt = true;
            }
            catch ( XStreamException e )
            {
                // it is corrupt -- so says XStream, but see above and NEXUS-3911
                if ( getLogger().isDebugEnabled() )
                {
                    // we log the stacktrace
                    getLogger().info( "Attributes of " + uid + " are corrupt, deleting it.", e );
                }
                else
                {
                    // just remark about this
                    getLogger().info( "Attributes of " + uid + " are corrupt, deleting it." );
                }

                corrupt = true;
            }
            finally
            {
                IOUtil.close( fis );
            }
        }

        if ( corrupt )
        {
            deleteAttributes( uid );
        }

        return result;
    }


}
