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
package org.sonatype.nexus.proxy.attributes.perf;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.AttributeStorage;
import org.sonatype.nexus.proxy.attributes.perf.internal.MockRepository;
import org.sonatype.nexus.proxy.attributes.perf.internal.OrderedRunner;
import org.sonatype.nexus.proxy.attributes.perf.internal.TestRepositoryItemUid;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DummyRepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * The performance tests for specific implementations of AttributesStorage.
 */
@BenchmarkHistoryChart()
@BenchmarkMethodChart()
@AxisRange(min = 0)
@RunWith( OrderedRunner.class )
public abstract class AttributeStoragePerformanceTestSupport
{
//    @Rule
//    public MethodRule benchmarkRun = new BenchmarkRule();

    private Repository repository;

    private AttributeStorage attributeStorage;

    final private DummyRepositoryItemUidFactory repositoryItemUidFactory = new DummyRepositoryItemUidFactory();

    final private File CONTENT_TEST_FILE = new File( "target/" + getClass().getSimpleName() + "/testContent.txt" );

    final private String SHA1_ATTRIBUTE_KEY = "digest.sha1";

    final private String SHA1_ATTRIBUTE_VALUE = "100f4ae295695335ef5d3346b42ed81ecd063fc9";

    final private static int ITERATION_COUNT = 100;

    @Before
    public void setupRepositoryMock()
    {
        // Do NOT us a mockito mock, using answers does not play well with junitbenchmark
        repository = new MockRepository( "mock-repo", repositoryItemUidFactory );
    }

    @Before
    public void setupAttributeStorage()
    {
        this.attributeStorage = getAttributeStorage();
    }

    @Before
    public void setupContentFile() throws IOException
    {
        FileUtils.writeStringToFile( CONTENT_TEST_FILE, "CONTENT" );
    }

    public abstract AttributeStorage getAttributeStorage();

    //////////////
    // Test writes
    //////////////
    @Test
    public void test0PrimePutAndGettAttribute()
    {
        writeEntryToAttributeStorage( "/prime.txt" );
        getStorageItemFromAttributeStore( "/prime.txt" );
    }

    @Test
    public void test1PutAttribute()
    {
        writeEntryToAttributeStorage( "/a.txt" );
    }

    @Test
    public void test2PutAttributeX100()
    {
        for( int ii=0; ii< ITERATION_COUNT; ii++)
        {
            writeEntryToAttributeStorage( "/"+ii+".txt" );
        }
    }

    @Test
    public void test3GetAttribute()
    {
        AbstractStorageItem storageItem = getStorageItemFromAttributeStore( "/a.txt" );
        assertThat( storageItem.getAttributes().get( SHA1_ATTRIBUTE_KEY ), equalTo( SHA1_ATTRIBUTE_VALUE) );
    }

    @Test
    public void test4GetAttributeX100()
    {
        for( int ii=0; ii< ITERATION_COUNT; ii++)
        {
            getStorageItemFromAttributeStore( "/"+ii+".txt" );
        }
    }

    @Test
    public void test5DeleteAttributes()
    {
        deleteStorageItemFromAttributeStore( "/a.txt" );
    }

    @Test
    public void test6DeleteAttributesX100()
    {
        for( int ii=0; ii< ITERATION_COUNT; ii++)
        {
            deleteStorageItemFromAttributeStore( "/"+ii+".txt" );
        }
    }

    private void writeEntryToAttributeStorage( String path )
    {
        StorageFileItem storageFileItem = new DefaultStorageFileItem( repository, new ResourceStoreRequest( path ), true, true, getContentLocator() );

        storageFileItem.getAttributes().put( SHA1_ATTRIBUTE_KEY, SHA1_ATTRIBUTE_VALUE );
        storageFileItem.getAttributes().put( "digest.md5", "f62472816fb17de974a87513e2257d63" );
        storageFileItem.getAttributes().put( "request.address", "127.0.0.1" );

        attributeStorage.putAttribute( storageFileItem );
    }

    private AbstractStorageItem getStorageItemFromAttributeStore( String path )
    {
        RepositoryItemUid repositoryItemUid = new TestRepositoryItemUid(repositoryItemUidFactory, repository, path );

        AbstractStorageItem storageItem = attributeStorage.getAttributes( repositoryItemUid );
        return storageItem;
    }

    private void deleteStorageItemFromAttributeStore( String path)
    {
        RepositoryItemUid repositoryItemUid = new TestRepositoryItemUid(repositoryItemUidFactory, repository, path );
        assertThat( "Attribute was not removed from store.", attributeStorage.deleteAttributes( repositoryItemUid ) );
    }

    private ContentLocator getContentLocator()
    {
        if( true )
        {
            return new StringContentLocator( "CONTENT" );
        }
        else
        {
            return new FileContentLocator( CONTENT_TEST_FILE, "text/plain" );
        }
    }

}
