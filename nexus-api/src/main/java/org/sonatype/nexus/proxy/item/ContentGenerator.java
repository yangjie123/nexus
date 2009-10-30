package org.sonatype.nexus.proxy.item;

import javax.inject.Singleton;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plugin.ExtensionPoint;

/**
 * A content generator is a special component, that is able to generate content on-the-fly, that will be substituted
 * with the content coming from the Local Storage. The size if handled automatically (ContentLocator carries it),
 * but the developer should choose how to handle the item's creation date! Important, since Nexus REST API
 * handles conditional GET's, and the "generated" file will have the _same_ timestamp coming from the LocalRepository
 * unless explicitly overridden in generateContent() method body with code like item.setModified(long)!
 * 
 * @author cstamas
 */
@ExtensionPoint
@Singleton
public interface ContentGenerator
{
    public static final String CONTENT_GENERATOR_ID = "contentGenerator";

    ContentLocator generateContent( Repository repository, String path, StorageFileItem item )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException;
}
