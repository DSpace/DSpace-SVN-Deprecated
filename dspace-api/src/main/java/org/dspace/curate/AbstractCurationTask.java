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
    public void init(Curator curator, String taskId) {
        this.curator = curator;
        this.taskId = taskId;
    }

    @Override
    public abstract void perform(DSpaceObject dso) throws IOException;
    
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
    public void perform(Context ctx, String id) throws IOException {
        Curator.dereference(ctx, id, this);
    }

    protected void report(String message) {
        curator.report(message);
    }

    protected void setStatus(int code) {
        curator.setStatus(taskId, code);
    }

    protected void setResult(String result) {
        curator.setResult(taskId, result);
    }
}
