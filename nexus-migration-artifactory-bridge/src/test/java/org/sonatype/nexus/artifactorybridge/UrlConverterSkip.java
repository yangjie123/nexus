package org.sonatype.nexus.artifactorybridge;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.plugin.migration.artifactory.persist.MappingConfiguration;
import org.sonatype.nexus.plugin.migration.artifactory.persist.model.CMapping;

public class UrlConverterSkip
    extends PlexusTestCase
{

    private UrlConverter urlConverter;

    @Override
    protected void setUp()
        throws Exception

        {
        urlConverter = lookup( UrlConverter.class );
        MappingConfiguration cfg = lookup( MappingConfiguration.class );
        cfg.addMapping( new CMapping( "repo1", null, "central" ) );
        cfg.addMapping( new CMapping( "libs-local", "libs-local", null ) );
    }

    public void testConvert()
        throws Exception
    {
        String url;

        url = urlConverter.convert( "/repo1/org/apache/maven/2.0.9/maven-2.0.9.zip" );
        assertEquals( "/content/repositories/central/org/apache/maven/2.0.9/maven-2.0.9.zip", url );

        url = urlConverter.convert( "/libs-local/local/lib/1.0-SNAPSHOT/lib-1.0-SNAPSHOT.jar" );
        assertEquals( "/content/groups/libs-local/local/lib/1.0-SNAPSHOT/lib-1.0-SNAPSHOT.jar", url );

        assertNull( urlConverter.convert( "/" ) );
        assertNull( urlConverter.convert( null ) );
        assertNull( urlConverter.convert( "dummy" ) );
    }

}
