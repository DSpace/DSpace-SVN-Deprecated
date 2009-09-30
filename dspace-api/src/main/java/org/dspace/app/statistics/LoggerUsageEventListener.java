package org.dspace.app.statistics;

import org.apache.log4j.Logger;
import org.dspace.app.statistics.UsageEvent.Action;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.LogManager;
import org.dspace.services.model.Event;

public class LoggerUsageEventListener extends AbstractUsageEventListener{

    /** log4j category */
    private static Logger log = Logger
            .getLogger(LoggerUsageEventListener.class);
    
	public void receiveEvent(Event event) {
		
		if(event instanceof UsageEvent)
		{
			UsageEvent ue = (UsageEvent)event;

			log.info(LogManager.getHeader(
					ue.getContext(),
					formatAction(ue.getAction(), ue.getObject()),
					formatMessage(ue.getObject()))
					);
			
		}
	}

	private static String formatAction(Action action, DSpaceObject object)
	{
		try
		{
			String objText = Constants.typeText[object.getType()].toLowerCase();
			return action.text() + "_" + objText;
		}catch(Exception e)
		{
			
		}
		return "";
		
	}
	
	private static String formatMessage(DSpaceObject object)
	{
		try
		{
			String objText = Constants.typeText[object.getType()].toLowerCase();
			String handle = object.getHandle();
			
			/* Emulate Item logger */
			if(handle != null && object instanceof Item)
				return "handle=" + object.getHandle();
			else
				return objText + ":_id=" + object.getID();
			
		}catch(Exception e)
		{
			
		}
		return "";
		
	}
}
