/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.mlm.plugin.ldm;

import org.cougaar.util.StateModelException;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.Parameters;

import org.cougaar.planning.ldm.ClusterServesPlugin;
import org.cougaar.planning.ldm.LDMPluginServesLDM;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.ContextOfOplanIds;
import org.cougaar.planning.ldm.plan.Context;

import org.cougaar.core.domain.FactoryException;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.util.UID;
import org.cougaar.core.service.DomainService;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.SubscriberException;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.component.ServiceRevokedEvent;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NamedPosition;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OrgActivity;

import org.cougaar.mlm.plugin.ldm.LDMSQLPlugin;
import org.cougaar.mlm.plugin.ldm.SQLOplanBase;
import org.cougaar.mlm.plugin.ldm.SQLOplanQueryHandler;
import org.cougaar.mlm.plugin.ldm.LDMEssentialPlugin;

import java.util.Vector;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Date;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.Serializable;


public class OplanReaderPlugin extends LDMSQLPlugin implements SQLOplanBase{
  public static final String OPLAN_ID_PARAMETER = "oplanid";

  private ArrayList oplans;
  private HashMap locations = new HashMap();

  // Additions/Modifications/Deletions which have not yet been published
  private HashSet newObjects = new HashSet();
  private HashSet modifiedObjects = new HashSet();
  private HashSet removedObjects = new HashSet();

  //private IncrementalSubscription oplanCoupons;
  private IncrementalSubscription oplanSubscription;
  private IncrementalSubscription stateSubscription;
  private IncrementalSubscription glsSubscription;
  private IncrementalSubscription mySelfOrgs;

  /** for knowing when we get our self org asset **/
  private Organization selfOrgAsset = null;


  /**
   * The predicate for the Socrates subscription
   **/
  private static UnaryPredicate selfOrgAssetPred = new UnaryPredicate() {
      public boolean execute(Object o) {
	
	if (o instanceof Organization) {
	  Organization org = (Organization) o;
	  return org.isSelf();
	}
	return false;
      }
    };

  private UnaryPredicate glsTaskPredicate = new UnaryPredicate() {
      public boolean execute (Object o) {
        if (o instanceof Task) {
          Task task = (Task) o;
          Verb verb = task.getVerb();
          if (verb.equals(Constants.Verb.GetLogSupport)) {
            PrepositionalPhrase pp = task.getPrepositionalPhrase(Constants.Preposition.FOR);
            if (pp != null) {
              return pp.getIndirectObject().equals(selfOrgAsset);
            }
          }
        }
        return false;
      }
    };


  private static UnaryPredicate oplanPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Oplan);
    }
  };


  private static class OrgActivityPredicate implements UnaryPredicate {
    private Oplan myOplan= null;
    //    private String myOrgId = "";

    public void setOplan(Oplan oplan) {
      myOplan = oplan;
    }

    public boolean execute(Object o) {
      if (myOplan == null)  {
        return false;
      }
      return ((o instanceof OrgActivity) &&
              (((OrgActivity) o).getOplanUID().equals(myOplan.getUID())));
    }
  }
   
  private static OrgActivityPredicate orgActivityPredicate = 
    new OrgActivityPredicate();  


  protected void setupSubscriptions() {
    // get the domain service
    if (theFactory == null) {
      domainService = (DomainService) getBindingSite()
	.getServiceBroker().getService(this, DomainService.class,
				       new ServiceRevokedListener() {
					   public void serviceRevoked(ServiceRevokedEvent re) {
					     theFactory = null;
					   }
					 });
    }
    theFactory = ((PlanningFactory) domainService.getFactory("planning"));

    mySelfOrgs = (IncrementalSubscription) getBlackboardService().subscribe(selfOrgAssetPred);
    oplanSubscription = (IncrementalSubscription) getBlackboardService().subscribe(oplanPredicate);

    oplans = new ArrayList();
    // refill oplan Collection on rehydrate
    processOplanAdds(oplanSubscription.getCollection());

    try {
      // set up initial properties - use super method
      initProperties();
    } catch (SubscriberException se) {
      System.err.println(this.toString()+": Initialization failed: "+se);
    }
    
    //oplanCoupons = (IncrementalSubscription)subscribe(oplanCouponPred);
  }


  private void setupSubscriptions2() {
    glsSubscription = (IncrementalSubscription) getBlackboardService().subscribe(glsTaskPredicate);
  }

  private void processOrgAssets(Enumeration e) {
    if (e.hasMoreElements()) {
      selfOrgAsset = (Organization) e.nextElement();
      // Setup our other subscriptions now that we know ourself
      if (glsSubscription == null) {
        setupSubscriptions2();
      }
    }
  }

  public synchronized void execute() {    
    if (mySelfOrgs.hasChanged()) {
      processOrgAssets(mySelfOrgs.getAddedList());
    }
    if (glsSubscription == null) 
      {
        return; // Still waiting for ourself
      }
    if (glsSubscription.hasChanged()) {
      Collection adds = glsSubscription.getAddedCollection();
      if (adds != null) {
	for (Iterator addIterator = adds.iterator(); addIterator.hasNext();) {
	  Task task = (Task) addIterator.next();
          //System.out.println("OplanReaderPlugin: got GLSTask "+task.getUID() +" for "+getMessageAddress().toString());
          requestOplan(task);
	}
      }
    }
  }

  private void requestOplan(Task t) {
    ContextOfOplanIds cIds = (ContextOfOplanIds)t.getContext();
    String oplanId = cIds.get(0);
    if (oplanId != null) {
      queryFile = oplanId+".oplan.q";
    }

    PrepositionalPhrase prepp = t.getPrepositionalPhrase("WithC0");
    long c0_date = ((Long)(prepp.getIndirectObject())).longValue();
    Date cDay = new Date(c0_date);

    Oplan myOplan = addOplan(oplanId);
    myOplan.setCday(cDay);

    newObjects.add(myOplan);
    publishOplanObjects();
    publishOplanPostProcessing();

    try {
      // parse q file into query vectors and parameters, initialize query handlers
      parseQueryFile();
      
      //runs thru and kicks off all query handlers to go to db
      grokQueries();
    } catch (SubscriberException se) {
      System.err.println(this.toString()+": Initialization failed: "+se);
    }
    publishOplanObjects();
    publishOplanPostProcessing();
  }

  private void publishOplanObjects() {
    for (Iterator iterator = newObjects.iterator();
         iterator.hasNext();) {
      Object object = iterator.next();
      getBlackboardService().publishAdd(object);
    }
    for (Iterator iterator = modifiedObjects.iterator();
         iterator.hasNext();) {
      Object object = iterator.next();
      getBlackboardService().publishChange(object);
    }
    for (Iterator iterator = removedObjects.iterator();
         iterator.hasNext();) {
      Object object = iterator.next();
      getBlackboardService().publishRemove(object);
    }
  }

  private void publishOplanPostProcessing() {
    newObjects.clear();
    modifiedObjects.clear();
    removedObjects.clear();
  }
 
  public void updateOplanInfo(Oplan update) {
    synchronized (oplans) {
      String oplanID = update.getOplanId();
      Oplan oplan = getOplan(oplanID);
      modifiedObjects.add(oplan);
    }
  }
      
  // Replace org activies for Org.
  public void updateOrgActivities(Oplan update,
                                  String orgId,
                                  Collection orgActivities) {

    synchronized (oplans) {
      String oplanID = update.getOplanId();
      Oplan oplan = getOplan(oplanID);
      
      if (oplan == null) {
        System.err.println("GLSInitServlet.updateOrgActivities(): can't find" +
                           " referenced Oplan " + oplanID);
        return;
      }
      
      // Execute a query to get all existing org activities associated with
      // the oplan
      orgActivityPredicate.setOplan(oplan);
      Collection existingOrgActivities = getBlackboardService().query(orgActivityPredicate);
      for (Iterator iterator = existingOrgActivities.iterator();
           iterator.hasNext();) {
        OrgActivity orgActivity = (OrgActivity) iterator.next();
        removedObjects.add(orgActivity);
      }

      for (Iterator iterator = orgActivities.iterator();
           iterator.hasNext();) {
        OrgActivity orgActivity = (OrgActivity) iterator.next();
        newObjects.add(orgActivity);
      }
    }
  }

  // Used by query handlers to get location info
  public NamedPosition getLocation(String locCode) {
    return (NamedPosition) locations.get(locCode);
  }

  // Used by query handlers to update location info
  public void updateLocation(GeolocLocation location) {
    String locName = location.getGeolocCode();
    locations.put(locName, location);
  }

  public Oplan getOplan(String oplanID) {

    synchronized (oplans) {
      for (Iterator iterator = oplans.iterator();
           iterator.hasNext();) {
        Oplan oplan = (Oplan) iterator.next();
        if ((oplan.getOplanId().equals(oplanID))){
          return oplan;
        }
      }
      return null;
    }
  }
  private Oplan addOplan(String oplanID) {
    Oplan oplan;

    synchronized (oplans) {
      oplan = getOplan(oplanID);
      
      if (oplan != null) {
        System.err.println("OplanReaderPlugin.addOplan(): " + oplanID + 
                           " already exists.");
        return oplan;
      } else {
        oplan =  new Oplan();

	getUIDServer().registerUniqueObject(oplan);
        oplan.setOwner(getMessageAddress());
        oplan.setOplanId(oplanID);
        oplans.add(oplan);
      }
    } // end syncronization on oplans
    return oplan;
  }

  private void processOplanAdds(Collection adds) {
    for (Iterator it = adds.iterator(); it.hasNext();) {
      Oplan oplan = (Oplan) it.next();
      oplans.add(oplan);
    }
  }
}
