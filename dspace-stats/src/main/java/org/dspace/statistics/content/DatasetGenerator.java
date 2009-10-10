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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    public int DatasetType;

    /*
    public static final int CATEGORY = 0;

    public static final int SERIE = 1;
    */

    public boolean includeTotal = false;


    public int getDatasetType(){
        return DatasetType;
    }

    public void setDatasetType(int datasetType){
        DatasetType = datasetType;
    }

    /*
    public boolean isCategory(){
        return DatasetType == CATEGORY;
    }

    public boolean isSerie(){
        return DatasetType == SERIE;
    }
    */

    public boolean isIncludeTotal() {
        return includeTotal;
    }

    public void setIncludeTotal(boolean includeTotal) {
        this.includeTotal = includeTotal;
    }
}
