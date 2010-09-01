/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://dspace.org/license/
 */

package org.dspace.curate;

import java.io.IOException;

import org.dspace.content.DSpaceObject;
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
