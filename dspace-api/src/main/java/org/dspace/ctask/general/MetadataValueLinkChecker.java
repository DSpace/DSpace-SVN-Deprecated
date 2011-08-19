package org.dspace.ctask.general;

import org.dspace.content.DCValue;
import org.dspace.content.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * A link checker that builds upon the BasicLinkChecker to check URLs that
 * appear in all metadata fields where the field starts with http:// or https://
 *
 * Of course thi assumes that there is no extra metadata following the URL.
 *
 * @author Stuart Lewis
 */
public class MetadataValueLinkChecker extends BasicLinkChecker {

    protected List<String> getURLs(Item item)
    {
        // Get all metadata elements that start with http:// or https://
        DCValue[] urls = item.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        ArrayList<String> theURLs = new ArrayList<String>();
        for (DCValue url : urls)
        {
            if ((url.value.startsWith("http://")) || (url.value.startsWith("https://")))
            {
                theURLs.add(url.value);
            }
        }
        return theURLs;
    }
}
