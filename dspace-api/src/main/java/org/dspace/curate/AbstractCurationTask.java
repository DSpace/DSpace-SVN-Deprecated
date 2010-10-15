/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://dspace.org/license/
 */

package org.dspace.curate;

import java.io.IOException;
import java.sql.SQLException;

import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;

/**
 * AbstractCurationTask encapsulates a few common patterns of task use,
 * resources, and convenience methods.
 * 
 * @author richardrodgers
 */
public abstract class AbstractCurationTask implements CurationTask {

    protected Curator curator = null;
    protected String taskId = null;

    @Override
    public void init(Curator curator, String taskId) throws IOException {
        this.curator = curator;
        this.taskId = taskId;
    }

    @Override
    public abstract int perform(DSpaceObject dso) throws IOException;
    
    /**
     * Distributes a task through a DSpace container - a convenience method
     * for tasks declaring the <code>@Distributive</code> property. Users must
     * override the'performItem' invoked by this method.
     * 
     * @param dso
     * @throws IOException
     */
    protected void distribute(DSpaceObject dso) throws IOException {
        try {
            if (dso instanceof Item) {
                performItem((Item)dso);
            } else if (dso instanceof Collection) {
                ItemIterator iter = ((Collection)dso).getItems();
                while (iter.hasNext()) {
                    performItem(iter.next());
                }
            } else if (dso instanceof Community) {
                Community comm = (Community)dso;
                for (Community subcomm : comm.getSubcommunities()) {
                    distribute(subcomm);
                }
                for (Collection coll : comm.getCollections()) {
                    distribute(coll);
                }
            }
        } catch (SQLException sqlE) {
            throw new IOException(sqlE.getMessage());
        }       
    }
    
    protected void performItem(Item item) throws SQLException, IOException {
        // no-op - override when using 'distribute' method
    }

    @Override
    public int perform(Context ctx, String id) throws IOException {
        DSpaceObject dso = dereference(ctx, id);
        return (dso != null) ? perform(dso) : Curator.CURATE_FAIL;
    }
    
    protected DSpaceObject dereference(Context ctx, String id) throws IOException {
        try {
            return HandleManager.resolveToObject(ctx, id);
        } catch (SQLException sqlE) {
            throw new IOException(sqlE.getMessage());
        }
    }

    protected void report(String message) {
        curator.report(message);
    }

    protected void setResult(String result) {
        curator.setResult(taskId, result);
    }
}
