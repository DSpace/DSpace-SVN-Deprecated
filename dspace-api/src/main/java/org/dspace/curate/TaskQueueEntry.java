/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://dspace.org/license/
 */

package org.dspace.curate;

import java.util.Arrays;
import java.util.List;


/**
 * TaskQueueEntry defines the record or entry in the named task queues.
 * Regular immutable value object class.
 * 
 * @author richardrodgers
 */
public final class TaskQueueEntry {
    
    private final String epersonId;
    private final String submitTime;
    private final String tasks;
    private final String objId;
    
    public TaskQueueEntry(String epersonId, long submitTime,
                          List<String> taskNames, String objId) {
        this.epersonId = epersonId;
        this.submitTime = Long.toString(submitTime);
        StringBuilder sb = new StringBuilder();
        for (String tName : taskNames) {
            sb.append(tName).append(",");
        }
        this.tasks = sb.substring(0, sb.length() - 1);
        this.objId = objId;
    }
    
    public TaskQueueEntry(String entry) {
        String[] tokens = entry.split("\\|");
        epersonId = tokens[0];
        submitTime = tokens[1];
        tasks = tokens[2];
        objId = tokens[3];
    }
    
    public String getEpersonId() {
        return epersonId;
    }
    
    public long getSubmitTime() {
        return Long.valueOf(submitTime);
    }
    
    public List<String> getTaskNames() {
        return Arrays.asList(tasks.split(","));
    }
    
    public String getObjectId() {
        return objId;
    }
    
    @Override
    public String toString() {
        return epersonId + "|" + submitTime + "|" + tasks + "|" + objId;
    }
}
