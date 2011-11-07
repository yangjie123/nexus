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

package org.sonatype.nexus.proxy.attributes.perf.internal;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.RepositoryItemUidAttributeManager;
import org.sonatype.nexus.proxy.mirror.PublishedMirrors;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.RequestProcessor;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.nexus.scheduling.RepositoryTaskFilter;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * A Mock Repository, using Mockito answers + JUnitBenchmark causes some problems
 */
public class MockRepository implements Repository
{
    private final String id;

    private final RepositoryItemUidFactory repositoryItemUidFactory;

    public MockRepository( String id, RepositoryItemUidFactory repositoryItemUidFactory )
    {
        this.id = id;
        this.repositoryItemUidFactory = repositoryItemUidFactory;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public RepositoryItemUid createUid( String path )
    {
        return new TestRepositoryItemUid(repositoryItemUidFactory, this, path);
    }
    

    @Override
    public String getProviderRole()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getProviderHint()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setId( String id )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CoreConfiguration getCurrentCoreConfiguration()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void configure( Object config )
        throws ConfigurationException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDirty()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean commitChanges()
        throws ConfigurationException
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean rollbackChanges()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setName( String name )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getPathPrefix()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setPathPrefix( String prefix )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RepositoryKind getRepositoryKind()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ContentClass getRepositoryContentClass()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RepositoryTaskFilter getRepositoryTaskFilter()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StorageItem retrieveItem( ResourceStoreRequest request )
        throws ItemNotFoundException, IllegalOperationException, StorageException, AccessDeniedException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void copyItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException, StorageException,
        AccessDeniedException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException, StorageException,
        AccessDeniedException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteItem( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException, StorageException,
        AccessDeniedException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException, StorageException,
        AccessDeniedException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException, StorageException,
        AccessDeniedException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<StorageItem> list( ResourceStoreRequest request )
        throws ItemNotFoundException, IllegalOperationException, StorageException, AccessDeniedException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TargetSet getTargetsForRequest( ResourceStoreRequest request )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasAnyTargetsForRequest( ResourceStoreRequest request )
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RepositoryItemUidAttributeManager getRepositoryItemUidAttributeManager()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Action getResultingActionOnWrite( ResourceStoreRequest rsr )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isCompatible( Repository repository )
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <F> F adaptToFacet( Class<F> t )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNotFoundCacheTimeToLive()
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setNotFoundCacheTimeToLive( int notFoundCacheTimeToLive )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PathCache getNotFoundCache()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setNotFoundCache( PathCache notFoundcache )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void maintainNotFoundCache( ResourceStoreRequest request )
        throws ItemNotFoundException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addToNotFoundCache( String path )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeFromNotFoundCache( String path )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addToNotFoundCache( ResourceStoreRequest request )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeFromNotFoundCache( ResourceStoreRequest request )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isNotFoundCacheActive()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setNotFoundCacheActive( boolean notFoundCacheActive )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AttributesHandler getAttributesHandler()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setAttributesHandler( AttributesHandler attributesHandler )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getLocalUrl()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLocalUrl( String url )
        throws StorageException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LocalStatus getLocalStatus()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLocalStatus( LocalStatus val )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LocalStorageContext getLocalStorageContext()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LocalRepositoryStorage getLocalStorage()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setLocalStorage( LocalRepositoryStorage storage )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PublishedMirrors getPublishedMirrors()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, RequestProcessor> getRequestProcessors()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isUserManaged()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setUserManaged( boolean val )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isExposed()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setExposed( boolean val )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isBrowseable()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setBrowseable( boolean val )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RepositoryWritePolicy getWritePolicy()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setWritePolicy( RepositoryWritePolicy writePolicy )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isIndexable()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setIndexable( boolean val )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSearchable()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setSearchable( boolean val )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void expireCaches( ResourceStoreRequest request )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void expireNotFoundCaches( ResourceStoreRequest request )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<String> evictUnusedItems( ResourceStoreRequest request, long timestamp )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean recreateAttributes( ResourceStoreRequest request, Map<String, String> initialData )
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AccessManager getAccessManager()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setAccessManager( AccessManager accessManager )
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StorageItem retrieveItem( boolean fromTask, ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void copyItem( boolean fromTask, ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void moveItem( boolean fromTask, ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteItem( boolean fromTask, ResourceStoreRequest request )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<StorageItem> list( boolean fromTask, ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void storeItem( boolean fromTask, StorageItem item )
        throws UnsupportedStorageOperationException, IllegalOperationException, StorageException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<StorageItem> list( boolean fromTask, StorageCollectionItem item )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
