package org.sonatype.nexus.plugin.migration.artifactory.persist;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.plugin.migration.artifactory.persist.model.CMapping;
import org.sonatype.nexus.plugin.migration.artifactory.persist.model.Configuration;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

@Component( role = MappingConfiguration.class, hint = "default" )
public class DefaultMappingConfiguration
    extends AbstractLogEnabled
    implements MappingConfiguration
{

    @org.codehaus.plexus.component.annotations.Configuration( value = "${nexus-work}/conf/mapping.xml" )
    private File configurationFile;

    private ReentrantLock lock = new ReentrantLock();

    private Configuration configuration;

    public void addMapping( CMapping map )
    {
        lock.lock();
        try
        {
            getConfiguration().addUrlMapping( map );

            save();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void save()
    {
        lock.lock();

        FileLock fileLock = null;
        FileChannel channel = null;
        FileOutputStream out = null;
        try
        {
            XStream xs = getXStream();

            if ( !configurationFile.exists() )
            {
                configurationFile.getParentFile().mkdirs();
                configurationFile.createNewFile();
            }

            out = new FileOutputStream( configurationFile );
            channel = out.getChannel();
            fileLock = channel.lock( 0, Long.MAX_VALUE, false ); // exclusive lock on save

            xs.toXML( getConfiguration(), out );
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to save mapping configuration", e );
        }
        finally
        {
            release( fileLock, channel, out );

            lock.unlock();
        }
    }

    private XStream getXStream()
    {
        XStream xs = new XStream();
        xs.processAnnotations( Configuration.class );
        xs.processAnnotations( CMapping.class );
        return xs;
    }

    private Configuration getConfiguration()
    {
        if ( this.configuration != null )
        {
            return this.configuration;
        }

        lock.lock();
        FileInputStream in = null;
        FileChannel channel = null;
        FileLock fileLock = null;
        try
        {

            in = new FileInputStream( configurationFile );
            channel = in.getChannel();
            fileLock = channel.lock( 0, Long.MAX_VALUE, true );

            XStream xs = getXStream();
            Object config = xs.fromXML( in );

            if ( config != null )
            {
                this.configuration = (Configuration) config;
            }
            else
            {
                this.configuration = new Configuration();
            }

        }
        catch ( FileNotFoundException e )
        {
            this.configuration = new Configuration();
        }
        catch ( XStreamException e )
        {
            getLogger().error( "Invalid configuration XML", e );
        }
        catch ( IOException e )
        {
            getLogger().error( "Error reading configuration", e );
        }
        finally
        {
            release( fileLock, channel, in );

            lock.unlock();
        }

        return this.configuration;
    }

    private void release( FileLock fileLock, Closeable channel, Closeable stream )
    {
        if ( fileLock != null )
        {
            try
            {
                fileLock.release();
            }
            catch ( IOException e )
            {
                // just releasing lock
            }
        }

        if ( channel != null )
        {
            try
            {
                channel.close();
            }
            catch ( IOException e )
            {
                // just closing channel
            }
        }

        if ( stream != null )
        {
            try
            {
                stream.close();
            }
            catch ( IOException e )
            {
                // just closing file
            }
        }
    }

    public CMapping getMapping( String repositoryId )
    {
        if ( repositoryId == null )
        {
            return null;
        }

        List<CMapping> urls = getConfiguration().getUrlsMapping();
        for ( CMapping mapping : urls )
        {
            if ( repositoryId.equals( mapping.getArtifactoryRepositoryId() ) )
            {
                return mapping;
            }
        }

        return null;
    }

}
