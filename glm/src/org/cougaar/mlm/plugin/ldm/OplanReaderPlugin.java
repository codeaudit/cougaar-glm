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
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.Parameters;

import org.cougaar.planning.ldm.ClusterServesPlugin;
import org.cougaar.planning.ldm.LDMPluginServesLDM;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;

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

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NamedPosition;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OplanCoupon;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.oplan.OplanContributor;
import org.cougaar.glm.ldm.oplan.OrgRelation;

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

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.Serializable;

import java.sql.*;

public class OplanReaderPlugin extends LDMSQLPlugin implements SQLOplanBase{

  private ArrayList oplans;
  private HashMap locations = new HashMap();

  // Additions/Modifications/Deletions which have not yet been published
  private HashSet newObjects = new HashSet();
  private HashSet modifiedObjects = new HashSet();
  private HashSet removedObjects = new HashSet();

  private IncrementalSubscription oplanCoupons;
  private IncrementalSubscription oplanSubscription;
  private IncrementalSubscription stateSubscription;

  private static UnaryPredicate oplanCouponPred = new UnaryPredicate() {
      public boolean execute(Object o) { 
        if (o instanceof OplanCoupon) {
          return true;
        } else {
          return false;
        }
      }
    };


  private static UnaryPredicate oplanPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Oplan);
    }
  };

  private static class OplanCouponMapPredicate implements UnaryPredicate {
    UID _couponUID;
    public OplanCouponMapPredicate(UID couponUID) {
      _couponUID = couponUID;
    }
    public boolean execute(Object o) {
      if (o instanceof OplanCouponMap) {
	if (((OplanCouponMap ) o).getCouponUID().equals(_couponUID)) {
	  return true;
	}
      }
      return false;
    }
  }


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
    oplanSubscription = (IncrementalSubscription) getBlackboardService().subscribe(oplanPredicate);

    oplans = new ArrayList();
    // refill oplan Collection on rehydrate
    processOplanAdds(oplanSubscription.getCollection());

//     if (!getBlackboardService().didRehydrate()) {
      try {
	// set up initial properties - use super method
	initProperties();
      } catch (SubscriberException se) {
	System.err.println(this.toString()+": Initialization failed: "+se);
      }
//     }

    oplanCoupons = (IncrementalSubscription)subscribe(oplanCouponPred);
  }

  public synchronized void execute() {    
    if (oplanCoupons.hasChanged()) {
      Collection adds = oplanCoupons.getAddedCollection();
      if (adds != null) {
	for (Iterator addIterator = adds.iterator(); addIterator.hasNext();) {
	  OplanCoupon ow = (OplanCoupon) addIterator.next();
	    requestOplan(ow);
	}
      }
      // Don't be clever about changes for now, just get the whole thing again.
      Collection changes = oplanCoupons.getChangedCollection();
      if (changes != null) {
	for (Iterator changeIterator = changes.iterator(); changeIterator.hasNext();) {
	  OplanCoupon ow = (OplanCoupon) changeIterator.next();
	    requestOplan(ow);
	}
      }
      Collection deletes = oplanCoupons.getRemovedCollection();
      if (deletes != null) {
	for (Iterator delIterator = deletes.iterator(); delIterator.hasNext();) {
	  OplanCoupon ow = (OplanCoupon) delIterator.next();
	    removeOplan(ow);
	}
      }
    }
  }
  private void requestOplan(final OplanCoupon oplanCoupon) {
    //does the job of grokArguments in super class     
    queryFile = oplanCoupon.getOplanQueryFile();

    try {
      // parse the query file into our query vectors and parameters
      parseQueryFile();

      grokQueries();
    } catch (SubscriberException se) {
      System.err.println(this.toString()+": Initialization failed: "+se);
    }
    UID couponUID = oplanCoupon.getUID();
    publishOplanObjects(couponUID);
    publishOplanPostProcessing();
  }

  /**
   * Removes all matching Oplan components from the logplan
   **/
  private void removeOplan(OplanCoupon ow) {

    String orgID = getMessageAddress().toString();
    if (ow.getUID() != null) {
      //get glue objects that map this coupon uid to an oplan uid
      OplanCouponMapPredicate ocp = new OplanCouponMapPredicate(ow.getUID());
      Collection oplanObjs = query(ocp);

      for (Iterator it = oplanObjs.iterator(); it.hasNext();) {
        UID opUID = ((OplanCouponMap)it.next()).getContributorUID();
        OplanPredicate op = new OplanPredicate(opUID, orgID);
        Collection removedOplans = query(op);
        for (Iterator iter= removedOplans.iterator(); iter.hasNext();) {
          publishRemove(iter.next());
        }
      }
    }
  }

  private static class OplanCouponMap implements Serializable {
    UID _couponUID;
    UID _contributorUID;

    public OplanCouponMap(UID couponUID, UID contributorUID) {
      _couponUID=couponUID;
      _contributorUID = contributorUID;
    }
    
    public void setCouponUID (UID couponUID) {
      _couponUID=couponUID;
    }

    public UID getCouponUID() {
      return _couponUID;
    }
    public void setContributorUID (UID contributorUID) {
      _contributorUID = contributorUID;
    }

    public UID getContributorUID() {
      return _contributorUID;
    }
    
  }

  private static class OplanPredicate implements UnaryPredicate {
    private UID _oplanUID;
    private String _orgID;

    public OplanPredicate(UID oplanUID, String orgID) {
      _oplanUID = oplanUID;
      _orgID = orgID;
    }

    public boolean execute(Object o) {
      if (o instanceof Oplan) {
	if (((Oplan) o).getUID().equals(_oplanUID)) {
	  return true;
	}
	return false;
      }

      if (o instanceof OplanContributor) {
	OplanContributor oc = (OplanContributor) o;
	System.out.println("Predicate: found OplanContributor");
	if (oc.getOplanUID().equals(_oplanUID))  {
	  System.out.println("Predicate: has correct OplanUID " + oc.getOplanUID());
	  if (oc instanceof OrgActivity) {
	    if (((OrgActivity) oc).getOrgID().equals(_orgID)) {
	      System.out.println("Predicate: correct OrgID in OrgActivity");
	      return true;
	    } else {
	      return false;
	    }
	  } else if (oc instanceof OrgRelation) {
	    if (((OrgRelation) oc).getOrgID().equals(_orgID)) {
	      return true;
	    } else {
	      return false;
	    }
	  }
	  //System.out.println("Predicate: found a good one! " + o);
	  return true;
	}
      }
      //System.out.println("Predicate: Sorry, play again soon");
      return false;
    }
  }


  private void publishOplanObjects(UID couponUID) {
    for (Iterator iterator = newObjects.iterator();
         iterator.hasNext();) {
      Object object = iterator.next();
      getBlackboardService().publishAdd(object);
      if (object instanceof Oplan) {
        //only stuff oplanUID, orgactivities not needed
        OplanCouponMap uidMap = new OplanCouponMap(couponUID,((Oplan)object).getUID());
        getBlackboardService().publishAdd(uidMap);
      }
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
      
      boolean found = false;
      if (oplan == null) {
        oplan = addOplan(oplanID);
      }
      else {
        found = true;
      }
      
      oplan.setOperationName(update.getOperationName());
      oplan.setPriority(update.getPriority());
      oplan.setCday(update.getCday());
      oplan.setEndDay(update.getEndDay());

      if (found) {
        modifiedObjects.add(oplan);
      } else {
        newObjects.add(oplan);
      }
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

