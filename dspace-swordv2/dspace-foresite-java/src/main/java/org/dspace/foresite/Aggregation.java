/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.foresite;

import java.util.*;
import java.net.URI;

/**
 * @Author Richard Jones
 */
public interface Aggregation extends OREResource
{
	// initialising the aggregation
	void initialise(URI uri) throws OREException;

	// methods to deal with created date

	Date getCreated() throws OREException;

    void setCreated(Date created);

	// methods to deal with modified date

	Date getModified() throws OREException;

    void setModified(Date modified);

	// methods to deal with rights for the Aggregation

	List<String> getRights();

    void setRights(List<String> rights);

    void addRights(String rights);

    void clearRights();

	// methods to deal with Aggregation title metadata

	List<String> getTitles();

    void setTitles(List<String> titles);

    void addTitle(String title);

    void clearTitles();

	// methods to deal with Aggregation type information

	List<URI> getTypes() throws OREException;

    void setTypes(List<URI> types) throws OREException;

    void addType(URI type) throws OREException;

    void clearTypes() throws OREException;

	// methods to deal with URIs which are similar to the current Aggregation,
	// or which have the weaker relationship of rdfs:seeAlso

	List<URI> getSimilarTo() throws OREException;

    void setSimilarTo(List<URI> similarTo) throws OREException;

    void addSimilarTo(URI similarTo) throws OREException;

    void clearSimilarTo() throws OREException;

	List<URI> getSeeAlso() throws OREException;

	void setSeeAlso(List<URI> seeAlso) throws OREException;

	void addSeeAlso(URI seeAlso) throws OREException;

	void clearSeeAlso() throws OREException;

	// methods to work with AggregatedResources aggregated by this Aggregation,
	// including nested Aggregations

	AggregatedResource createAggregatedResource(URI uri) throws OREException;

	// FIXME: implement, auto create proxies in the back ground for other methods
	// AggregatedResource createAggregatedResource(URI uri, URI proxy) throws OREException;

	AggregatedResource createAggregatedResource(Aggregation aggregation) throws OREException;

	// FIXME: implement, auto create proxies in the back ground for other methods
	// AggregatedResource createAggregatedResource(Aggregation aggregation, URI proxy) throws OREException;

	List<AggregatedResource> getAggregatedResources() throws OREException;

    void setAggregatedResources(List<AggregatedResource> resources) throws OREException;

    void addAggregatedResource(AggregatedResource resource) throws OREException;

	void clearAggregatedResources() throws OREException;

	// methods to deal with the serlialisations of the resource maps that describe this Aggregation

    List<ReMSerialisation> getReMSerialisations() throws OREException;

    void setReMSerialisations(List<ReMSerialisation> serialisations) throws OREException;

    void addReMSerialisation(ReMSerialisation serlialisation) throws OREException;

	void clearReMSerialisations();

	void addResourceMapURI(URI uri) throws OREException;

	// ResourceMap getResourceMap() throws OREException;

	ResourceMap createResourceMap(URI uri) throws OREException;

	// rem management

	List<ResourceMap> getAuthoritative() throws OREException;

	List<ResourceMap> getResourceMaps() throws OREException;

	ResourceMap getResourceMap(URI uri) throws OREException;

	// methods to deal with Proxies aggregated by this Aggregation

	Proxy createProxy(URI proxyURI, URI aggregatedResourceURI) throws OREException;

    void addProxy(Proxy proxy) throws OREException;

    List<Proxy> getProxies() throws OREException;

    void clearProxies() throws OREException;
}
