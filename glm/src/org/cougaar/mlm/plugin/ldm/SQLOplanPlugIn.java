/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

import javax.swing.*;
import java.awt.event.*;
import java.awt.LayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.util.UID;

import org.cougaar.util.UnaryPredicate;
  
import org.cougaar.core.agent.service.uid.UIDServiceProvider;
import org.cougaar.core.agent.service.uid.UIDServiceImpl;
import org.cougaar.core.service.UIDService;

//import org.cougaar.core.component.ServiceRevokedListener;
//import org.cougaar.core.component.ServiceRevokedEvent;

import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NamedPosition;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OplanContributor;
import org.cougaar.glm.ldm.oplan.OplanCoupon;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.mlm.plugin.UICoordinator;

import java.io.*;

/**
 * The SQLOplanPlugIn instantiates the Oplan and adds it to the LogPlan.  Essentially
 * a copy of OplanPlugIn.java except that it uses QueryHandlers to load Oplan info from
 * a database.
 *
 **/
public class SQLOplanPlugIn extends LDMSQLPlugIn {
  public static final String OPLAN_ID_PARAMETER = "oplanid";
  /** frame for 1-button UI **/
  private JFrame frame;

  /** for feedback to user on whether root GLS was successful **/
  private JLabel oplanLabel;
  protected JButton oplanButton;

  private IncrementalSubscription oplanSubscription;

  private IncrementalSubscription stateSubscription;
  private IncrementalSubscription publishTheOplanSubscription;
  

  private ArrayList contributors;
  
  private boolean readyToPublishOplan = false;
  private boolean UTBPublishOplan()
  {
	if(!readyToPublishOplan) {
		System.out.println("OPLAN CANNOT BE PUBLISHED AGAIN.");
		return false;
	}
	else
	{
		readyToPublishOplan=false;
		publishOplanFromInsideTx();
		return true;
	}
  }  

  private ArrayList oplans = new ArrayList();
  private HashMap locations = new HashMap();
  
  // Additions/Modifications/Deletions which have not yet been published
  private HashSet newObjects = new HashSet();
  private HashSet modifiedObjects = new HashSet();
  private HashSet removedObjects = new HashSet();

  private JLabel updateLabel;
  protected JButton updateButton;

  protected long myOplanTime;
	
  UIDService uidService = null;	
		
  private static class MyPrivateState implements java.io.Serializable {
    boolean oplanExists = false;
    boolean unpublishedChanges = false;
    boolean errorOccurred = false;
  }

  private MyPrivateState myPrivateState;

  private static UnaryPredicate oplanPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Oplan);
    }
  };
  
  private static UnaryPredicate publishTheOplanPredicate = new UnaryPredicate() {
     public boolean execute(Object o) {
	if (o instanceof java.lang.String)
            return (o.toString().equalsIgnoreCase("publishTheOplan"));
	return false;
     }
  };

  private static UnaryPredicate statePredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof MyPrivateState);
    }
  };
  
  private static class OrgActivityPredicate implements UnaryPredicate {
    private Oplan myOplan= null;
    private String myOrgId = "";

    public void setOplan(Oplan oplan) {
      myOplan = oplan;
    }

    public void setOrgId(String orgId) {
      myOrgId = orgId;
    }

    public boolean execute(Object o) {
      if ((myOplan == null) ||
          (myOrgId.equals(""))) {
        return false;
      }
    
      return ((o instanceof OrgActivity) &&
              (((OrgActivity) o).getOrgID().equals(myOrgId)) &&
              (((OrgActivity) o).getOplanUID().equals(myOplan.getUID())));
    }
  }   

  private static OrgActivityPredicate orgActivityPredicate = 
    new OrgActivityPredicate();

  /**
   * Executes PlugIn functionality.
   */
  public void execute(){
    if (stateSubscription.hasChanged()) {
      checkForPrivateState(stateSubscription.getAddedList());
    }
    if (oplanSubscription.hasChanged()) {
      Collection adds = oplanSubscription.getAddedCollection();
      if (adds != null) {
	processOplanAdds(adds);
      }
      Collection changes = oplanSubscription.getChangedCollection();
      if (changes != null) {
	processChanges(changes);
      }
      Collection deletes = oplanSubscription.getRemovedCollection();
      if (deletes !=null) {
	processOplanDeletes(deletes);
      }
    }

    for (Iterator it = contributors.iterator(); it.hasNext();) {
      IncrementalSubscription is = (IncrementalSubscription) it.next();
      
      // we don't care about adds, just deletes and changes
      if (is.hasChanged()) {
	Collection changes = is.getChangedCollection();
	if (changes != null) {
	  processChanges(changes);
	}

	// Deletes of OplanContributors are treated as changes to the Oplan
	changes = is.getRemovedCollection();
	if (changes != null) {
	  processChanges(changes);
	}	
      }
    }
    Collection publishIt=publishTheOplanSubscription.getAddedCollection();
    if (publishIt!=null && publishIt.size() > 0) {
	for (Iterator iterator = publishIt.iterator();iterator.hasNext();) {
		Object object = iterator.next();
		getBlackboardService().publishRemove(object);
	}
	UTBPublishOplan();
    }
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


  public void updateOplanInfo(Oplan update) {
    synchronized (oplans) {
      String oplanID = update.getOplanId();
      Oplan oplan = getOplan(oplanID);
      
      if (oplan == null) {
        oplan = addOplan(oplanID);
      }
      
      oplan.setOperationName(update.getOperationName());
      oplan.setPriority(update.getPriority());
      oplan.setCday(update.getCday());
      oplan.setEndDay(update.getEndDay());

      boolean found = false;
      if (oplanSubscription != null) {
        Collection published = oplanSubscription.getCollection();
        for (Iterator iterator = published.iterator();
             iterator.hasNext();) {
          if (((Oplan) iterator.next()).getOplanId().equals(oplanID)) {
            found = true;
            break;
          }
        }
      }

      if (found) {
        modifiedObjects.add(oplan);

        if (myPrivateState != null) {
          myPrivateState.unpublishedChanges = true;
        }
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
        System.err.println("SQLOplanPlugIn.updateOrgActivities(): can't find" +
                           " referenced Oplan " + oplanID);
        return;
      }
      
      // Execute a query to get all existing org activities associated with
      // the oplan
      orgActivityPredicate.setOplan(oplan);
      orgActivityPredicate.setOrgId(orgId);
      Collection existingOrgActivities = getBlackboardService().query(orgActivityPredicate);
      for (Iterator iterator = existingOrgActivities.iterator();
           iterator.hasNext();) {
        OrgActivity orgActivity = (OrgActivity) iterator.next();
        
        if (orgActivity.getOrgID().equals(orgId)) {
          removedObjects.add(orgActivity);
        }
      }

      for (Iterator iterator = orgActivities.iterator();
           iterator.hasNext();) {
        OrgActivity orgActivity = (OrgActivity) iterator.next();
        newObjects.add(orgActivity);
      }

      if (myPrivateState != null) {
        myPrivateState.unpublishedChanges = true;
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

  /*
   * Creates a subscription.
   */
  protected void setupSubscriptions() 
  {	
    super.setupSubscriptions();
    
    // get the UIDService   	
    //    uidService = (UIDService) getBindingSite().getServiceBroker().getService(this, UIDService.class, null);
    
		
    getBlackboardService().getSubscriber().setShouldBePersisted(false);
    oplanSubscription = (IncrementalSubscription) getBlackboardService().subscribe(oplanPredicate);
    stateSubscription = (IncrementalSubscription) getBlackboardService().subscribe(statePredicate);

    contributors = new ArrayList(13);
    // refill contributors Collection on rehydrate
    processOplanAdds(oplanSubscription.getCollection());

    createGUI();
    readyToPublishOplan=true;
    publishTheOplanSubscription = (IncrementalSubscription) getBlackboardService().subscribe(publishTheOplanPredicate);

    if (getBlackboardService().didRehydrate()) {
      checkForPrivateState(stateSubscription.elements());
    } else {
      getBlackboardService().publishAdd(new MyPrivateState());
    }
  }	   		 

  protected void initProperties() {
    // default package for QueryHandler
    super.initProperties();
    String exptid = System.getProperty("org.cougaar.experiment.id");
    
    if (exptid != null) {
      globalParameters.put("exptid", exptid);
    }
  }
  

  private Oplan addOplan(String oplanID) {
    Oplan oplan;

    synchronized (oplans) {
      oplan = getOplan(oplanID);
      
      if (oplan != null) {
        System.err.println("SQLOplanPlugIn.addOplan(): " + oplanID + 
                           " already exists.");
        return oplan;
      } else {
        oplan =  new Oplan();
        //getCluster().getUID...
	getUIDServer().registerUniqueObject(oplan);
        //uidService.registerUniqueObject(oplan);
        oplan.setOwner(getClusterIdentifier());
        oplan.setOplanId(oplanID);
        
        oplans.add(oplan);
      }
    } // end syncronization on oplans
    return oplan;
  }

  private void checkForPrivateState(Enumeration e) {
    if (myPrivateState == null) {
      while(e.hasMoreElements()) {
        myPrivateState = (MyPrivateState) e.nextElement();
        checkButtonEnable();
      }
    }
  }

  private void createGUI() {
    frame = new JFrame("OplanPlugIn");
    JPanel panel = new JPanel((LayoutManager) null);
    // Create the button
    oplanButton = new JButton("Publish Oplan");
    oplanLabel = new JLabel("No Oplan has been published.");

    // Register a listener for the check box
    OplanButtonListener myOplanListener = new OplanButtonListener();
    oplanButton.addActionListener(myOplanListener);
    oplanButton.setEnabled(false);
    UICoordinator.layoutButtonAndLabel(panel, oplanButton, oplanLabel);
	frame.getRootPane().setDefaultButton(oplanButton); // hitting return sends the oplan

    // Create the button
    updateButton = new JButton("Update Oplan");
    updateLabel = new JLabel("No Oplan has been published.");

    // Register a listener for the check box
    UpdateButtonListener myUpdateListener = new UpdateButtonListener();
    updateButton.addActionListener(myUpdateListener);
    updateButton.setEnabled(false);
    UICoordinator.layoutButtonAndLabel(panel, updateButton, updateLabel);

    frame.setContentPane(panel);
    frame.pack();
    UICoordinator.setBounds(frame);
    frame.setVisible(true);
  }
 
  /** An ActionListener that listens to the GLS buttons. */
  class OplanButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      publishOplan();
    }
  }

  /** An ActionListener that listens to the update button. */
  class UpdateButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      refreshOplan();
    }
  }

  private void checkButtonEnable() {
    // Log status
    oplanButton.setEnabled(!myPrivateState.oplanExists || 
                           myPrivateState.unpublishedChanges);
    if (myPrivateState.oplanExists) {
      oplanLabel.setText("Published successfully");
    } else if (myPrivateState.errorOccurred) {
      oplanLabel.setText("ERROR::Unable to add the Oplan to the LogPlan.");
    } else {
      oplanLabel.setText("No Oplan has been published.");
    }

    updateButton.setEnabled(myPrivateState.oplanExists);
    if (myPrivateState.oplanExists) {
      updateLabel.setText("Oplan can be updated.");
    } else {
      updateLabel.setText("No Oplan to update.");
    }

  }
  /*
  protected void addOplan(Oplan oplan) {
    if (oplans == null) {
      oplans = new ArrayList();
    } else {
      // verify oplan not already on the list
    }

    oplans.add(oplan);
  }
  */
  private void publishOplanObjects() {
    for (Iterator iterator = newObjects.iterator();
         iterator.hasNext();) {
      Object object = iterator.next();
      getBlackboardService().publishAdd(object);

      if (object instanceof Oplan) {
        OplanCoupon ow = new OplanCoupon(((Oplan) object).getUID(), 
                                         getClusterIdentifier());
        getUIDServer().registerUniqueObject(ow);
        getBlackboardService().publishAdd(ow);

        myPrivateState.oplanExists = true;
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

    getBlackboardService().publishChange(myPrivateState);
  }
  
  private void publishOplanPostProcessing() {
    newObjects.clear();
    modifiedObjects.clear();
    removedObjects.clear();

    myPrivateState.unpublishedChanges = false;

    checkButtonEnable();
  }
  
  private void publishOplan() {
	// Need to make separate add/remove/modify lists
	getBlackboardService().openTransaction();
	publishOplanObjects();
	getBlackboardService().closeTransaction(false);
	publishOplanPostProcessing();
  }

  private void publishOplanFromInsideTx() {
	// Need to make separate add/remove/modify lists
	publishOplanObjects();
	publishOplanPostProcessing();
  }

  private void refreshOplan() {
    for (Enumeration e = queries.elements(); e.hasMoreElements();) {
      SQLOplanQueryHandler qh = (SQLOplanQueryHandler) e.nextElement();
      qh.update();
    }
    checkButtonEnable();
  }

  private void processOplanAdds(Collection adds) {
    for (Iterator it = adds.iterator(); it.hasNext();) {
      Oplan oplan = (Oplan) it.next();
      
      IncrementalSubscription is = (IncrementalSubscription) 
	getBlackboardService().subscribe(new ContributorPredicate(oplan.getUID()));
      contributors.add(is);
    }
  }

  private void processChanges(Collection changes) {
    HashSet changedOplans = new HashSet();

    for (Iterator iterator = changes.iterator(); iterator.hasNext();) {
      UID oplanUID = null;
      Object o = iterator.next();
      if (o instanceof Oplan) {
	oplanUID = ((Oplan) o).getUID();
      } else if (o instanceof OplanContributor) {
	oplanUID = ((OplanContributor) o).getOplanUID();
      } else continue;
      
      changedOplans.add(oplanUID);
    } 

    // Publish once for each changed oplan
    for (Iterator iterator = changedOplans.iterator(); iterator.hasNext();) {
      Collection coupons = 
        getBlackboardService().query(new CouponPredicate((UID) iterator.next()));
      for (Iterator couponIt = coupons.iterator(); couponIt.hasNext();) {
	System.out.println("SQLOplanPlugIn: publishChanging OplanCoupon");
	getBlackboardService().publishChange(couponIt.next());
      }
    }
  }

  private void processOplanDeletes(Collection deletes) {
    for (Iterator it = deletes.iterator(); it.hasNext();) {
      Oplan oplan = (Oplan) it.next();
      Collection coupons = getBlackboardService().query(new CouponPredicate(oplan.getUID()));
      for (Iterator couponIt = coupons.iterator(); couponIt.hasNext();) {
	getBlackboardService().publishRemove(couponIt.next());
      }
    }
  }

  private class CouponPredicate implements UnaryPredicate {
    UID _oplanUID;
    public CouponPredicate(UID oplanUID) {
      _oplanUID = oplanUID;
    }
    public boolean execute(Object o) {
      if (o instanceof OplanCoupon) {
	if (((OplanCoupon ) o).getOplanUID().equals(_oplanUID)) {
	  return true;
	}
      }
      return false;
    }
  }

  private class ContributorPredicate implements UnaryPredicate {
    UID _oplanUID;
    public ContributorPredicate(UID oplanUID) {
      _oplanUID = oplanUID;
    }
    public boolean execute(Object o) {
      if (o instanceof OplanContributor) {
	if (((OplanContributor ) o).getOplanUID().equals(_oplanUID)) {
	  return true;
	}
      }
      return false;
    }
  }
}







