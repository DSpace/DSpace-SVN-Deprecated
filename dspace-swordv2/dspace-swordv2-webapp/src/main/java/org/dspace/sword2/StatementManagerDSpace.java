package org.dspace.sword2;

import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.swordapp.server.AuthCredentials;
import org.swordapp.server.Statement;
import org.swordapp.server.StatementManager;
import org.swordapp.server.SwordAuthException;
import org.swordapp.server.SwordConfiguration;
import org.swordapp.server.SwordError;
import org.swordapp.server.SwordServerException;
import org.swordapp.server.UriRegistry;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatementManagerDSpace extends DSpaceSwordAPI implements StatementManager
{
	private static Logger log = Logger.getLogger(StatementManagerDSpace.class);

	public Statement getStatement(String stateIRI, Map<String, String> accept, AuthCredentials authCredentials, SwordConfiguration swordConfig)
			throws SwordServerException, SwordError, SwordAuthException
	{
		try
        {
            SwordContext sc = null;
            SwordConfigurationDSpace config = (SwordConfigurationDSpace) swordConfig;

            SwordAuthenticator auth = new SwordAuthenticator();
            sc = auth.authenticate(authCredentials);
            Context context = sc.getContext();

            if (log.isDebugEnabled())
            {
                log.debug(LogManager.getHeader(context, "sword_get_statement", ""));
            }

            // log the request
            String un = authCredentials.getUsername() != null ? authCredentials.getUsername() : "NONE";
            String obo = authCredentials.getOnBehalfOf() != null ? authCredentials.getOnBehalfOf() : "NONE";
            log.info(LogManager.getHeader(context, "sword_get_statement", "username=" + un + ",on_behalf_of=" + obo));

            // first thing is to figure out what we're being asked to work on
            SwordUrlManager urlManager = config.getUrlManager(context, config);
            Item item = urlManager.getItem(context, stateIRI);

			// find out, now we know what we're being asked for, whether this is allowed
			WorkflowManagerFactory.getInstance().retrieveStatement(context, item);

			String suffix = urlManager.getTypeSuffix(context, stateIRI);
			SwordStatementDisseminator disseminator = null;

			if (suffix != null)
			{
                Map<Float, List<String>> analysed = new HashMap<Float, List<String>>();
                List<String> list = new ArrayList<String>();
                list.add(suffix);
                analysed.put((float) 1.0, list);
                disseminator = SwordDisseminatorFactory.getStatementInstance(analysed);
			}
			else
			{
				// we rely on the content negotiation to do the work
				String acceptContentType = this.getHeader(accept, "Accept", null);

                // we extract from the Accept header the ordered list of content types
                TreeMap<Float, List<String>> analysed = this.analyseAccept(acceptContentType);

                // the meat of this is done by the package disseminator
                disseminator = SwordDisseminatorFactory.getStatementInstance(analysed);
			}

			Statement statement = disseminator.disseminate(context, item);
            return statement;
        }
        catch (DSpaceSwordException e)
        {
            throw new SwordServerException(e);
        }
	}
}
