/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.glm.map;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.policy.*;


public class PSP_ALPLocator
  extends PSP_BaseAdapter
  implements PlanServiceProvider, UISubscriber
{
  private String myID;
  public String desiredOrganization;

  public PSP_ALPLocator() throws RuntimePSPException {
    super();
  }

  public PSP_ALPLocator(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  /* This PSP is referenced directly (in the URL from the client)
     and hence this shouldn't be called.
     */

  public boolean test(HttpInput query_parameters, PlanServiceContext psc) {
    super.initializeTest();
    return false; 
  }

  

  /*
    Called when a request is received from a client.
    Either gets the command ASSET to return the names of all the assets
    that contain a ScheduledContentPG or
    gets the name of the asset to plot from the client request.
  */

  public void execute( PrintStream out,
		       HttpInput query_parameters,
		       PlanServiceContext psc,
		       PlanServiceUtilities psu) throws Exception {
    try {
      myExecute(out, query_parameters, psc, psu);
    } catch (Exception e) {
      e.printStackTrace();
    };
  }

  /** The query data is one of:
    ASSET -- meaning return list of assets
    nomenclature:type id -- return asset matching nomenclature & type id
    UID: -- return asset with matching UID
    */

  private void myExecute( PrintStream out,
		       HttpInput query_parameters,
		       PlanServiceContext psc,
		       PlanServiceUtilities psu) throws Exception 
	{
    desiredOrganization = "";
    if (query_parameters.hasBody()) 
    {
      desiredOrganization = query_parameters.getBodyAsString();
      desiredOrganization = desiredOrganization.trim();
      System.out.println("POST DATA: " + desiredOrganization);
    } else 
    {
      System.out.println("WARNING: No asset to plot");
      return;
    }

    // return list of asset names
    //if (desiredOrganization.equals("ASSET")) {

	

      

      //Collection container = 
	

    // send the Location object
    if (LocationCollectorPlugIn.organizationLocations != null) 
    {
    	if(!desiredOrganization.equals("all"))
    	{
    	 	Object o = (Object)LocationCollectorPlugIn.organizationLocations.get(desiredOrganization);
	    	ObjectOutputStream p = new ObjectOutputStream(out);
		    p.writeObject(o);
		    System.out.println("Sent Locator Object");
		  }
		  else
		  {
		  	Object o = (Object)LocationCollectorPlugIn.organizationLocations;
	    	ObjectOutputStream p = new ObjectOutputStream(out);
		    p.writeObject(o);
		    System.out.println("Sent Hashtable Object");
		  }
	  }
  }

  public boolean returnsXML() {
    return true;
  }

  public boolean returnsHTML() {
    return false;
  }

  public String getDTD() {
    return "myDTD";
  }

  /* The UISubscriber interface.
     This PSP doesn't care if subscriptions change
     because it treats each request as a new request.
  */

  public void subscriptionChanged(Subscription subscription) 
  {
  }

  
  

}

 









