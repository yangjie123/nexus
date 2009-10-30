package org.sonatype.nexus.proxy.item;

/**
 * An abstract class for ContentLocators, that adds some help.
 * 
 * @author cstamas
 */
public abstract class AbstractContentLocator
    implements ContentLocator
{
    protected long length = ContentLocator.UNKNOWN_LENGTH;

    private final String mimeType;

    protected AbstractContentLocator( String mimeType )
    {
        this( mimeType, ContentLocator.UNKNOWN_LENGTH );
    }

    protected AbstractContentLocator( String mimeType, long length )
    {
        this.mimeType = mimeType;

        setLength( length );
    }

    public long getLength()
    {
        return length;
    }

    public void setLength( long len )
    {
        if ( len > -1L )
        {
            this.length = len;
        }
        else
        {
            this.length = ContentLocator.UNKNOWN_LENGTH;
        }
    }

    public String getMimeType()
    {
        return mimeType;
    }
}
