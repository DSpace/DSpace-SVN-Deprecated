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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.ItemIterator;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.PluginManager;
import org.dspace.handle.HandleManager;

/**
 * Curator orchestrates and manages the application of a one or more curation
 * tasks to a DSpace object. It provides common services and runtime
 * environment to the tasks.
 * 
 * @author richardrodgers
 */
public class Curator {

    // status code values
    public static final int CURATE_NOTASK = -2;
    public static final int CURATE_UNSET = -1;
    public static final int CURATE_SUCCESS = 0;
    public static final int CURATE_FAIL = 1;
    public static final int CURATE_SKIP = 2;

    private static Logger log = Logger.getLogger(Curator.class);
    private Map<String, TaskContext> ctxMap = new HashMap<String, TaskContext>();
    private List<String> perfList = new ArrayList<String>();
    private String reporter = null;

    public Curator() {
        ;
    }

    /**
     * Add a task to the set to be performed. Caller should make no assumptions
     * on execution ordering.
     * 
     * @param taskName - logical name of task
     * @return this curator - to support concatenating invocation style
     */
    public Curator addTask(String taskName) {
        CurationTask task = (CurationTask)PluginManager.getNamedPlugin(CurationTask.class, taskName);
        if (task != null) {
            task.init(this, taskName);
            ctxMap.put(taskName, new TaskContext(task, taskName));
            // performance order currently FIFO - to be revisited
            perfList.add(taskName);
        } else {
            log.error("Task: '" + taskName + "' does not resolve");
        }
        return this;
    }

    public Curator setReporter(String reporter) {
        this.reporter = reporter;
        return this;
    }

    public void curate(Context c, String id) throws Exception {
        if (id == null) {
           log.error("curate - null id");
           return;            
        }
        try {
            DSpaceObject dso = HandleManager.resolveToObject(c, id);
            if (dso != null) {
                curate(dso);
            } else {
                for (String taskName : perfList) {
                    TaskContext ctx = ctxMap.get(taskName);
                    ctx.task.perform(c, id);
                }
            }
        } catch (SQLException sqlE) {
            throw new IOException(sqlE.getMessage());
        }
    }

    public void curate(DSpaceObject dso) throws Exception {
        if (dso == null) {
            log.error("curate - null dso");
            return;
        }
        int type = dso.getType();
        for (String taskName : perfList) {
            TaskContext ctx = ctxMap.get(taskName);
            CurationTask task = ctx.task;
            // do we need to iterate over the object ?
            if (type == Constants.ITEM ||
                task.getClass().isAnnotationPresent(Distributive.class)) {
                task.perform(dso);
            } else if (type == Constants.COLLECTION) {
                doCollection(task, (Collection)dso);
            } else if (type == Constants.COMMUNITY) {
                doCommunity(task, (Community)dso);
            }
        }
    }
    
    public void queue(Context c, String id, String queueId) throws Exception {
        TaskQueue taskQ = (TaskQueue)PluginManager.getSinglePlugin(TaskQueue.class);
        taskQ.enqueue(queueId, new TaskQueueEntry(c.getCurrentUser().getName(),
                                    System.currentTimeMillis(), perfList, id));
    }
    
    public void clear() {
        ctxMap.clear();
        perfList.clear();
    }

    public void report(String message) {
        // Stub for now
        if ("-".equals(reporter)) {
            System.out.println(message);
        }
    }

    public int getStatus(String taskName) {
        TaskContext ctx = ctxMap.get(taskName);
        return (ctx != null) ? ctx.statusCode : CURATE_NOTASK;
    }

    public void setStatus(String taskName, int code) {
        TaskContext ctx = ctxMap.get(taskName);
        if (ctx != null) {
            ctx.setStatus(code);
        }
    }

    public String getResult(String taskName) {
        TaskContext ctx = ctxMap.get(taskName);
        return (ctx != null) ? ctx.result : null;
    }

    public void setResult(String taskName, String result) {
        TaskContext ctx = ctxMap.get(taskName);
        if (ctx != null) {
            ctx.setResult(result);
        }
    }

    public static DSpaceObject dereference(Context ctx, String id, CurationTask task) throws IOException {
        try {
            DSpaceObject dso = HandleManager.resolveToObject(ctx, id);
            if (task != null) {
                if (dso != null) {
                    task.perform(dso);
                } else {
                    log.error("Id: '" + id + "' not resolvable");
                }
            }
            return dso;
        } catch (SQLException sqlE) {
            throw new IOException(sqlE.getMessage());
        }
    }

    public static boolean isContainer(DSpaceObject dso) {
        return (dso.getType() == Constants.COMMUNITY ||
                dso.getType() == Constants.COLLECTION);
    }

    static void doCommunity(CurationTask task, Community comm) throws IOException {
        try {
            task.perform(comm);
            for (Community subcomm : comm.getSubcommunities()) {
                doCommunity(task, subcomm);
            }
            for (Collection coll : comm.getCollections()) {
                doCollection(task, coll);
            }
        } catch (SQLException sqlE) {
            throw new IOException(sqlE.getMessage());
        }
    }

    static void doCollection(CurationTask task, Collection coll) throws IOException {
        try {
            task.perform(coll);
            ItemIterator iter = coll.getItems();
            while (iter.hasNext()) {
                task.perform(iter.next());
            }
        } catch (SQLException sqlE) {
            throw new IOException(sqlE.getMessage());
        }
    }

    private class TaskContext {
        CurationTask task;
        String taskName;
        int statusCode;
        String result;

        public TaskContext(CurationTask task, String name) {
            this.task = task;
            taskName = name;
            statusCode = CURATE_UNSET;
        }

        public void setStatus(int code) {
            statusCode = code;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}
