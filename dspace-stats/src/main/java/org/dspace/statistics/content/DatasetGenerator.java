/**
 * $Id$
 * $URL$
 * *************************************************************************
 * Copyright (c) 2002-2009, DuraSpace.  All rights reserved
 * Licensed under the DuraSpace Foundation License.
 *
 * A copy of the DuraSpace License has been included in this
 * distribution and is available at: http://scm.dspace.org/svn/repo/licenses/LICENSE.txt
 */
package org.dspace.statistics.content;


/**
 * Represents a single facet for filtering.
 * Can be one of the axes in a table.
 * 
 * @author kevinvandevelde at atmire.com
 * Date: 23-dec-2008
 * Time: 9:39:37
 * 
 */
public abstract class DatasetGenerator {
    
    /** The type of generator can either be CATEGORY or SERIE **/
    protected int datasetType;

    protected boolean includeTotal = false;

    public int getDatasetType(){
        return datasetType;
    }

    public void setDatasetType(int datasetType){
        this.datasetType = datasetType;
    }
    
    public boolean isIncludeTotal() {
        return includeTotal;
    }

    public void setIncludeTotal(boolean includeTotal) {
        this.includeTotal = includeTotal;
    }
}
