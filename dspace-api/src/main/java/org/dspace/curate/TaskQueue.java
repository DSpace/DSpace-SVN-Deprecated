/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://dspace.org/license/
 */

package org.dspace.curate;

import java.io.IOException;
import java.util.Set;


/**
 * TaskQueue objects manage access to named queues of task entries.
 * Entries represent curation task requests that have been deferred.
 * The queue supports concurrent non-blocking writers, but controls
 * read access to a single reader possessing a ticket (first come,
 * first serve). After the read, the queue remains locked until
 * released by the reader, after which it is typically purged.
 *
 * @author richardrodgers
 */
public interface TaskQueue {
    
    String[] queueNames();

    void enqueue(String queueName, TaskQueueEntry entry) throws IOException;

    void enqueue(String queueName, Set<TaskQueueEntry> entrySet) throws IOException;
    
    Set<TaskQueueEntry> dequeue(String queueName, long ticket) throws IOException;

    void release(String queueName, long ticket, boolean removeEntries);
}
