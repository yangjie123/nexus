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

import com.fasterxml.jackson.xml.XmlMapper;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Jackson based Attribute Storage.
 */
public class JacksonXMLFSAttributeStorage
    extends AbstractFSAttributeStorage
{
    private final ObjectMapper objectMapper = new XmlMapper();

    public JacksonXMLFSAttributeStorage( ApplicationEventMulticaster applicationEventMulticaster,
                                         ApplicationConfiguration applicationConfiguration )
    {
        super( applicationEventMulticaster, applicationConfiguration );


//        objectMapper.enableDefaultTyping(); // defaults for defaults (see below); include as wrapper-array, non-concrete types
//        objectMapper.enableDefaultTyping( ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT ); // all non-final types
//        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
//        // make deserializer use JAXB annotations (only)
//        objectMapper.getDeserializationConfig().withAnnotationIntrospector(introspector);
//        // make serializer use JAXB annotations (only)
//        objectMapper.getSerializationConfig().withAnnotationIntrospector(introspector);

        objectMapper.setVisibility( JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY) // auto-detect all member fields
        .setVisibility(JsonMethod.GETTER, JsonAutoDetect.Visibility.NONE) // but only public getters
        .setVisibility(JsonMethod.IS_GETTER, JsonAutoDetect.Visibility.NONE); // and none of "is-setters"
        
        objectMapper.disable( DeserializationConfig.Feature.AUTO_DETECT_SETTERS );
        objectMapper.disable( SerializationConfig.Feature.AUTO_DETECT_GETTERS );
        objectMapper.disable( SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS );

//        objectMapper.writerWithDefaultPrettyPrinter();
    }

    @Override
    protected void doPutAttribute( StorageItem item, OutputStream outputStream )
        throws IOException
    {
        objectMapper.writeValue( outputStream, item );
    }

    @Override
    protected AbstractStorageItem doGetAttributes( RepositoryItemUid uid )
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

                // FIXME: this is fine for performance tests, but would need to resolve the correct type
                result = objectMapper.readValue( fis, DefaultStorageFileItem.class );

                result.setRepositoryItemUid( uid );

                //FIXME: are these legacy bits we do NOT need to take forward ?
//                // fixing remoteChecked
//                if ( result.getRemoteChecked() == 0 || result.getRemoteChecked() == 1 )
//                {
//                    result.setRemoteChecked( System.currentTimeMillis() );
//
//                    result.setExpired( true );
//                }
//
//                // fixing lastRequested
//                if ( result.getLastRequested() == 0 )
//                {
//                    result.setLastRequested( System.currentTimeMillis() );
//                }
            }
            catch ( IOException e )
            {
                getLogger().info( "While reading attributes of " + uid + " we got IOException:", e );

                throw e;
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
