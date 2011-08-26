/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.foresite.test;

import org.junit.Test;

/**
 * @Author Richard Jones
 */
public class Compliance09
{
	/**
	 * Test that the namespaces of output rdf are the correct ones as appearing
	 * in the ORE specification
	 */
	@Test
    public void testSerialisedNamespaces()
    {
        // we need to test on the following:
		//
		// - rdf
		// - rdfs
		// - dc
		// - dcterms
		// - owl
		// - foaf
	}

	/**
	 * Test each of the data model entities to ensure that they are exposing the
	 * correct type
	 */
	@Test
	public void testEntityTypes()
	{
		// We need to test the following entities
		//
		// - Aggregation : ore:Aggregation
		// - ResourceMap : ore:ResourceMap
		// - AggregatedResource : ore:AggregatedResource
		// - Proxy : ore:Proxy

		// we need to make sure that we do this for all the methods of
		// creating each of these entities, through the OREFactory, and
		// through the createX methods on each of the objects
	}

	/**
	 * Test to ensure that our URIs only work if they are the appropriate
	 * type (i.e. protocol-based or not, depending on case)
	 *
	 */
	@Test
	public void testURIs()
	{
		// We need to test the following entities
		//
		// - Aggregation : protocol based
		// - AggregatedResource: protocol based
		// - ResourceMap: protocol based
		// - Proxy : unclear!
	}

	/**
	 * Ensure that there is always at least one authoritative representation
	 * of the aggregation
	 */
	@Test
	public void testAuthoritative()
	{
		// create and remove resource maps to see if the aggregation /always/
		// has at least one authoritative resource map
	}

	/**
	 * Ensure that the resource map graph is always connected, whatever happens
	 */
	@Test
	public void testConnectedness()
	{

	}

	/**
	 * Test to ensure that REMs always have a creator, and that there is a
	 * default one if none is added, but that the default one is not present
	 * if one is set
	 */
	@Test
	public void testREMCreators()
	{
		
	}

	/**
	 * Test to ensure that REMs always have a modified date when serialised,
	 * which is either the current time, or the one which has been set
	 */
	@Test
	public void testREMModified()
	{

	}

	/**
	 * Ensure that REMs always contain at least one triple for ore:aggregates
	 */
	@Test
	public void testOREAggregates()
	{

	}

	/**
	 * Ensure that the rules the specification lays out for which URIs must be
	 * different are adhered to
	 */
	@Test
	public void testURIDifference()
	{
		// we need to test
		//
		// AggregatedResources cannot have the same URI as their Aggregation
		// Aggregations cannot have the same URI as their ResourceMap
	}

	/**
	 * Test to make sure that we can add URIs as types for the relevant objects
	 * (and not other objects), and make sure that the ORE native types always
	 * are present
	 */
	@Test
	public void testTypes()
	{
		// we need to do type testing on:
		//
		// Aggregations
		// AggregatedResources
	}

	/**
	 * Test to make sure that we can tell aggregated resources who they are
	 * aggregated by, and can manipulate those values in a consistent resource
	 * map
	 */
	@Test
	public void testIsAggregatedBy()
	{

	}

	/**
	 * Ensure that aggregations can be successfully added and used as
	 * aggregated resources to other aggregations
	 */
	@Test
	public void testAggregationNesting()
	{

	}

	/**
	 * We need to test that proxies have exactly the right ore:proxyIn and ore:proxyFor settings
	 *
	 * Also make sure that there is one and only one triple expressing this relationship
	 *
	 * What happens to proxies when aggregated resources are removed?
	 */
	@Test
	public void testProxy()
	{

	}

	/**
	 * Test that we can assert relationships between proxies and query them
	 * appropriately
	 *
	 * FIXME: may be a special case of createTriple
	 */
	@Test
	public void testProxyRelations()
	{

	}

	/**
	 * Make sure that lineages are singular, and relate to external proxies only
	 */
	@Test
	public void testLineage()
	{

	}

	/**
	 * Make sure that Agent nodes are working appropriately whether they are
	 * a blank node or are constructed with a URI
	 */
	public void testAgentNodeIds()
	{

	}
}
