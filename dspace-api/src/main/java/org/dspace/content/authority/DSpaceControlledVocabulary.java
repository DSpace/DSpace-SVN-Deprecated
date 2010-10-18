/*
 * DSpaceControlledVocabulary.java
 *
 * Version: $Revision: $
 *
 * Date: $Date: $
 *
 * Copyright (c) 2002-2005, Hewlett-Packard Company and Massachusetts
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

package org.dspace.content.authority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import org.apache.log4j.Logger;

import org.dspace.core.ConfigurationManager;
import org.dspace.core.SelfNamedPlugin;

/**
 * ChoiceAuthority source that reads the JSPUI-style hierarchical vocabularies
 * from ${dspace.dir}/config/controlled-vocabularies/*.xml and turns them into
 * autocompleting authorities.
 *
 * Configuration:
 *   This MUST be configured as a self-named plugin, e.g.:
 *     plugin.selfnamed.org.dspace.content.authority.ChoiceAuthority = \
 *        org.dspace.content.authority.DSpaceControlledVocabulary
 *
 * It AUTOMATICALLY configures a plugin instance for each XML file in the
 * controlled vocabularies directory. The name of the plugin is the basename
 * of the file; e.g., "${dspace.dir}/config/controlled-vocabularies/nsi.xml"
 * would generate a plugin called "nsi".
 *
 * Each configured plugin comes with three configuration options:
 *   vocabulary.plugin._plugin_.hierarchy.store = <true|false>    # Store entire hierarchy along with selected value. Default: TRUE
 *   vocabulary.plugin._plugin_.hierarchy.suggest = <true|false>  # Display entire hierarchy in the suggestion list.  Default: TRUE
 *   vocabulary.plugin._plugin_.delimiter = "<string>"              # Delimiter to use when building hierarchy strings. Default: "::"
 *
 *
 * @author Michael B. Klein
 *
 */

public class DSpaceControlledVocabulary extends SelfNamedPlugin implements ChoiceAuthority
{

	private static Logger log = Logger.getLogger(DSpaceControlledVocabulary.class);
    private static String xpathTemplate = "//node[contains(translate(@label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]";
    private static String idTemplate = "//node[@id = '%s']";
    private static String pluginNames[] = null;

    private String vocabularyName = null;
    private InputSource vocabulary = null;
    private Boolean suggestHierarchy = true;
    private Boolean storeHierarchy = true;
    private String hierarchyDelimiter = "::";

    public DSpaceControlledVocabulary()
    {
    	super();
    }

    public static String[] getPluginNames()
    {
        if (pluginNames == null)
        {
        	class xmlFilter implements java.io.FilenameFilter
            {
        		public boolean accept(File dir, String name)
                {
        			return name.endsWith(".xml");
        		}
        	}
            String vocabulariesPath = ConfigurationManager.getProperty("dspace.dir") + "/config/controlled-vocabularies/";
        	String[] xmlFiles = (new File(vocabulariesPath)).list(new xmlFilter());
        	List<String> names = new ArrayList<String>();
        	for (String filename : xmlFiles)
            {
        		names.add((new File(filename)).getName().replace(".xml",""));
        	}
        	pluginNames = names.toArray(new String[names.size()]);
            log.info("Got plugin names = "+Arrays.deepToString(pluginNames));
        }
        return pluginNames;
    }

    private void init()
    {
    	if (vocabulary == null)
        {
        	log.info("Initializing " + this.getClass().getName());
        	vocabularyName = this.getPluginInstanceName();
            String vocabulariesPath = ConfigurationManager.getProperty("dspace.dir") + "/config/controlled-vocabularies/";
            String configurationPrefix = "vocabulary.plugin." + vocabularyName;
            storeHierarchy = ConfigurationManager.getBooleanProperty(configurationPrefix + ".hierarchy.store", storeHierarchy);
            suggestHierarchy = ConfigurationManager.getBooleanProperty(configurationPrefix + ".hierarchy.suggest", suggestHierarchy);
            String configuredDelimiter = ConfigurationManager.getProperty(configurationPrefix + ".delimiter");
            if (configuredDelimiter != null)
            {
            	hierarchyDelimiter = configuredDelimiter.replaceAll("(^\"|\"$)","");
            }
        	String filename = vocabulariesPath + vocabularyName + ".xml";
        	log.info("Loading " + filename);
            vocabulary = new InputSource(filename);
    	}
    }

    private String buildString(Node node)
    {
    	if (node.getNodeType() == Node.DOCUMENT_NODE)
        {
    		return("");
    	}
        else
        {
    		String parentValue = buildString(node.getParentNode());
    		Node currentLabel = node.getAttributes().getNamedItem("label");
    		if (currentLabel != null)
            {
    			String currentValue = currentLabel.getNodeValue();
    			if (parentValue.equals(""))
                {
    				return currentValue;
    			}
                else
                {
    				return(parentValue + this.hierarchyDelimiter + currentValue);
    			}
    		}
            else
            {
    			return(parentValue);
    		}
    	}
    }

    public Choices getMatches(String text, int collection, int start, int limit, String locale)
    {
    	init();
    	log.debug("Getting matches for '" + text + "'");
    	String xpathExpression = String.format(xpathTemplate, text.replaceAll("'", "&apos;").toLowerCase());
    	XPath xpath = XPathFactory.newInstance().newXPath();
    	Choice[] choices;
    	try {
    		NodeList results = (NodeList)xpath.evaluate(xpathExpression, vocabulary, XPathConstants.NODESET);
    		String[] authorities  = new String[results.getLength()];
    		String[] values = new String[results.getLength()];
    		String[] labels  = new String[results.getLength()];
        	for (int i=0; i<results.getLength(); i++)
            {
        		Node node = results.item(i);
        		String hierarchy = this.buildString(node);
        		if (this.suggestHierarchy)
                {
        			labels[i] = hierarchy;
        		}
                else
                {
        			labels[i] = node.getAttributes().getNamedItem("label").getNodeValue();
        		}
        		if (this.storeHierarchy)
                {
        			values[i] = hierarchy;
        		}
                else
                {
        			values[i] = node.getAttributes().getNamedItem("label").getNodeValue();
        		}
        		authorities[i] = node.getAttributes().getNamedItem("id").getNodeValue();
        	}
        	int resultCount = Math.min(labels.length-start, limit);
        	choices = new Choice[resultCount];
        	if (resultCount > 0)
            {
            	for (int i=0; i<resultCount; i++)
                {
           			choices[i] = new Choice(authorities[start+i],values[start+i],labels[start+i]);
            	}
        	}
    	} catch(XPathExpressionException e) {
    		choices = new Choice[0];
    	}
    	return new Choices(choices, 0, choices.length, Choices.CF_AMBIGUOUS, false);
    }

    public Choices getBestMatch(String text, int collection, String locale)
    {
    	init();
    	log.debug("Getting best match for '" + text + "'");
        return getMatches(text, collection, 0, 2, locale);
    }

    public String getLabel(String key, String locale)
    {
    	init();
    	String xpathExpression = String.format(idTemplate, key);
    	XPath xpath = XPathFactory.newInstance().newXPath();
    	try {
    		Node node = (Node)xpath.evaluate(xpathExpression, vocabulary, XPathConstants.NODE);
    		return node.getAttributes().getNamedItem("label").getNodeValue();
    	} catch(XPathExpressionException e) {
    		return("");
    	}
    }
}