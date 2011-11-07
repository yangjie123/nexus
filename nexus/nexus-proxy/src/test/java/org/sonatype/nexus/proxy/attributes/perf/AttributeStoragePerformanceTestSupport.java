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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.AttributeStorage;
import org.sonatype.nexus.proxy.attributes.DefaultFSAttributeStorage;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultRepositoryItemUid;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DummyRepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.item.uid.Attribute;
import org.sonatype.nexus.proxy.item.uid.IsMetadataMaintainedAttribute;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

import java.io.File;

import static org.mockito.Mockito.*;

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
    // adding the following cause the Answer stub below to fail
//    @Rule
//    public MethodRule benchmarkRun = new BenchmarkRule();

    private Repository repository = mock( Repository.class );

    private AttributeStorage attributeStorage;

    final private DummyRepositoryItemUidFactory repositoryItemUidFactory = new DummyRepositoryItemUidFactory();

    // AbstractStorageItem getAttributes( RepositoryItemUid uid );
    // void putAttribute( StorageItem item );
    // boolean deleteAttributes( RepositoryItemUid uid );

    final private String SHA1_ATTRIBUTE_KEY = "digest.sha1";
    final private String SHA1_ATTRIBUTE_VALUE = "100f4ae295695335ef5d3346b42ed81ecd063fc9";

    @Before
    public void setupRepositoryMock()
    {
        when( repository.getId() ).thenReturn( "mock-repo" );
        when( repository.createUid( anyString() ) ).thenAnswer(new Answer<RepositoryItemUid>()
        {
             public RepositoryItemUid answer(InvocationOnMock invocation)
             {
                 Object[] args = invocation.getArguments();

                 String path = (String) args[0];
                 Repository mock = (Repository) invocation.getMock();

                 return new TestRepositoryItemUid(repositoryItemUidFactory, mock, path);
             }
        });
    }

    @Before
    public void setupAttributeStorage()
    {
        this.attributeStorage = getAttributeStorage();
    }

    public abstract AttributeStorage getAttributeStorage();

    //////////////
    // Test writes
    //////////////
    @Test
    public void test0PrimePutAttribute()
    {
        writeEntryToAttributeStorage( "/prime.txt" );
    }

    @Test
    public void test1PutAttribute()
    {
        writeEntryToAttributeStorage( "/a.txt" );
    }

    @Test
    public void test2PutAttributeX100()
    {
        for( int ii=0; ii<100; ii++)
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
        for( int ii=0; ii<100; ii++)
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
        for( int ii=0; ii<100; ii++)
        {
            deleteStorageItemFromAttributeStore( "/"+ii+".txt" );
        }
    }

    private void writeEntryToAttributeStorage( String path )
    {
        StorageFileItem storageFileItem = new DefaultStorageFileItem( repository, new ResourceStoreRequest( path ), true, true, new StringContentLocator( "CONTENT" ) );

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

    class TestRepositoryItemUid extends DefaultRepositoryItemUid
    {
        TestRepositoryItemUid( RepositoryItemUidFactory factory, Repository repository, String path )
        {
            super( factory, repository, path );
        }

        @Override
         public <A extends Attribute<V>, V> V getAttributeValue( Class<A> attrClass )
         {
             if( IsMetadataMaintainedAttribute.class.getName().equals( attrClass.getName() ) )
             {
                 return (V) Boolean.TRUE;
             }
             else
             {
                return super.getAttributeValue( attrClass );
             }
         }
    }
}
