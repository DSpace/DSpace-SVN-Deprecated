package org.dspace.app.statistics;

import org.dspace.services.EventService;
import org.dspace.services.model.EventListener;

public abstract class AbstractUsageEventListener implements EventListener {

	public AbstractUsageEventListener() {
		super();
	}

	/**
	 * Currently consumes all event prefixes.
	 */
	public String[] getEventNamePrefixes() {
		return new String[0];
	}

	/**
	 * Currently consumes events generated for all resources.
	 */
	public String getResourcePrefix() {
		return null;
	}

	public void setEventService(EventService service) throws Exception {
		if(service != null)
			service.registerEventListener(this);
		else
			throw new RuntimeException("EventService handed to Listener cannot be null");
	
	}
	
}