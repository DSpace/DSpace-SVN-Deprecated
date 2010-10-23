/*
 * SearchUtils.java
 *
 * Version: $Revision: 5497 $
 *
 * Date: $Date: 2010-10-20 23:06:10 +0200 (wo, 20 okt 2010) $
 *
 * Copyright (c) 2002, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Hewlett-Packard Company nor the name of the
 * Massachusetts Institute of Technology nor the names of their
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package org.dspace.discovery;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;
import org.dspace.content.DSpaceObject;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

/**
 * Util methods used by discovery
 *
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 */
public class SearchUtils {

    private static final Logger log = Logger.getLogger(SearchUtils.class);

    private static ExtendedProperties props = null;

    private static Map<String, SolrFacetConfig[]> solrFacets = new HashMap<String, SolrFacetConfig[]>();

    private static List<String> allFacets = new ArrayList<String>();

    private static List<String> searchFilters = new ArrayList<String>();

    private static List<String> sortFields = new ArrayList<String>();

    private static List<String> dateIndexableFields = new ArrayList<String>();

    static {

        log.debug("loading configuration");
        //Method that will retrieve all the possible configs we have

        props = ExtendedProperties
                .convertProperties(ConfigurationManager.getProperties());

        try {
            File config = new File(props.getProperty("dspace.dir")
                    + "/config/dspace-solr-search.cfg");
            if (config.exists()) {
                props.combine(new ExtendedProperties(config.getAbsolutePath()));
            } else {
                ExtendedProperties defaults = new ExtendedProperties();
                defaults
                        .load(SolrServiceImpl.class
                                .getResourceAsStream("dspace-solr-search.cfg"));
                props.combine(defaults);
            }

            log.debug("combined configuration");

        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            Iterator allPropsIt = props.getKeys();
            while (allPropsIt.hasNext()) {
                String propName = String.valueOf(allPropsIt.next());
                if (propName.startsWith("solr.facets.")) {
                    String[] propVals = props.getStringArray(propName);

                    log.info("loading scope, " + propName);

                    allFacets.addAll(Arrays.asList(propVals));
                    List<SolrFacetConfig> facets = new ArrayList<SolrFacetConfig>();
                    for (String propVal : propVals) {
                        if (propVal.endsWith("_dt") || propVal.endsWith(".year")) {
                            facets.add(new SolrFacetConfig(propVal.replace("_dt", ".year"), true));

                            log.info("value, " + propVal);

                        } else {
                            facets.add(new SolrFacetConfig(propVal + "_filter", false));

                            log.info("value, " + propVal);
                        }
                    }

                    //All the values are split into date & facetfields, so now store em
                    solrFacets.put(propName.replace("solr.facets.", ""), facets.toArray(new SolrFacetConfig[facets.size()]));

                    log.info("solrFacets size: " + solrFacets.size());
                }
            }

            String[] filterFieldsProps = SearchUtils.getConfig().getStringArray("solr.search.filters");
            if (filterFieldsProps != null) {
                searchFilters.addAll(Arrays.asList(filterFieldsProps));
            }

            String[] sortFieldProps = SearchUtils.getConfig().getStringArray("solr.search.sort");
            if (sortFieldProps != null) {
                sortFields.addAll(Arrays.asList(sortFieldProps));
            }


        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public static ExtendedProperties getConfig() {

        return props;
    }

    public static SolrFacetConfig[] getFacetsForType(String type) {
        return solrFacets.get(type);
    }

    public static List<String> getAllFacets() {
        return allFacets;
    }

    public static List<String> getSearchFilters() {
        return searchFilters;
    }

    public static List<String> getSortFields() {
        return sortFields;
    }

    public static DSpaceObject findDSpaceObject(Context context, SolrDocument doc) throws SQLException {

        Integer type = (Integer) doc.getFirstValue("search.resourcetype");
        Integer id = (Integer) doc.getFirstValue("search.resourceid");
        String handle = (String) doc.getFirstValue("handle");

        if (type != null && id != null) {
            return DSpaceObject.find(context, type, id);
        } else if (handle != null) {
            return HandleManager.resolveToObject(context, handle);
        }

        return null;
    }


    public static String[] getDefaultFilters(String scope) {
        List<String> result = new ArrayList<String>();
        // Check (and add) any default filters which may be configured
        String defaultFilters = getConfig().getString("solr.default.filter");
        if (defaultFilters != null)
            result.addAll(Arrays.asList(defaultFilters.split(";")));

        if (scope != null) {
            String scopeDefaultFilters = SearchUtils.getConfig().getString("solr." + scope + ".default.filter");
            if (scopeDefaultFilters != null)
                result.addAll(Arrays.asList(scopeDefaultFilters.split(";")));
        }
        return result.toArray(new String[result.size()]);
    }

    public static List<String> getDateIndexableFields() {
        String[] dateFieldsProps = SearchUtils.getConfig().getStringArray("solr.index.type.date");
        if (dateFieldsProps != null) {
            for (String dateField : dateFieldsProps) {
                dateIndexableFields.add(dateField.trim());
            }
        }
        return dateIndexableFields;
    }

    public static class SolrFacetConfig {

        private String facetField;
        private boolean isDate;

        public SolrFacetConfig(String facetField, boolean date) {
            this.facetField = facetField;
            isDate = date;
        }

        public String getFacetField() {
            return facetField;
        }

        public boolean isDate() {
            return isDate;
        }
    }

}
