/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.ldm;

import javax.swing.*;
import java.awt.event.*;
import java.awt.LayoutManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.society.UID;

import org.cougaar.util.UnaryPredicate;
  
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.ldm.plan.NamedPosition;
import org.cougaar.domain.glm.ldm.oplan.Oplan;
import org.cougaar.domain.glm.ldm.oplan.OplanContributor;
import org.cougaar.domain.glm.ldm.oplan.OplanCoupon;
import org.cougaar.domain.mlm.plugin.UICoordinator;

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

  private ArrayList contributors;

  private ArrayList oplans = new ArrayList();
  private HashMap locations = new HashMap();

  private static class MyPrivateState implements java.io.Serializable {
    boolean oplanExists = false;
    boolean errorOccurred = false;
  }

  private MyPrivateState myPrivateState;

  private static UnaryPredicate oplanPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Oplan);
    }
  };

  private static UnaryPredicate statePredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof MyPrivateState);
    }
  };
  
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
  }

  public Oplan getOplan(String oplanID) {
    Oplan oplan;

    synchronized (oplans) {
      for (Iterator iterator = oplans.iterator();
           iterator.hasNext();) {
        oplan = (Oplan)iterator.next();
        if ((oplan.getOplanId().equals(oplanID))){
          return oplan;
        }
      }
      return null;
    }
  }

  public Oplan addOplan(String oplanID) {
    Oplan oplan;

    synchronized (oplans) {
      oplan = getOplan(oplanID);
      
      if (oplan != null) {
        System.err.println("SQLOplanPlugIn.addOplan(): " + oplanID + 
                           " already exists.");
        return oplan;
      } else {
        oplan = new Oplan("");
        getCluster().getUIDServer().registerUniqueObject(oplan);
        oplan.setOwner(getClusterIdentifier());
        oplan.setOplanId(oplanID);
        
        oplans.add(oplan);
      }
    } // end syncronization on oplans
    return oplan;
  }

  // Dummy until we have some ability to build a look up table
  public NamedPosition getLocation(String locCode) {
    return (NamedPosition) locations.get(locCode);
  }


  public boolean addLocation(GeolocLocation location) {
    String locName = location.getGeolocCode();
    if (!locations.containsKey(locName)) {
      locations.put(locName, location);
      return true;
    } else {
      return false;
    }
  }
    
  /*
   * Creates a subscription.
   */
  protected void setupSubscriptions() 
  {	
    super.setupSubscriptions();

    getSubscriber().setShouldBePersisted(false);
    oplanSubscription = (IncrementalSubscription) subscribe(oplanPredicate);
    stateSubscription = (IncrementalSubscription) subscribe(statePredicate);

    contributors = new ArrayList(13);
    // refill contributors Collection on rehydrate
    processOplanAdds(oplanSubscription.getCollection());

    createGUI();

    if (didRehydrate()) {
      checkForPrivateState(stateSubscription.elements());
    } else {
      publishAdd(new MyPrivateState());
    }

    
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

  private void checkButtonEnable() {
    // Log status
    oplanButton.setEnabled(!myPrivateState.oplanExists);
    if (myPrivateState.oplanExists) {
      oplanLabel.setText("Published successfully");
    } else if (myPrivateState.errorOccurred) {
      oplanLabel.setText("ERROR::Unable to add the Oplan to the LogPlan.");
    } else {
      oplanLabel.setText("No Oplan has been published.");
    }
  }



  protected void addOplan(Oplan oplan) {
    if (oplans == null) {
      oplans = new ArrayList();
    } else {
      // verify oplan not already on the list
    }

    oplans.add(oplan);
  }

  private void publishOplan() {
    openTransaction();
    int n = oplans.size();
    if (n > 0) {
      for (int i = 0; i < n; i++) {
        Oplan oplan = (Oplan) oplans.get(i);
        // Add the OPLAN to the LogPlan.
        publishAdd(oplan);
        // publish the subs
        for (Enumeration es = oplan.getForcePackages(); es.hasMoreElements(); ) {
          publishAdd(es.nextElement());
        }
        for (Enumeration es = oplan.getOrgActivities(); es.hasMoreElements(); ) {
          publishAdd(es.nextElement());
        }
        for (Enumeration es = oplan.getOrgRelations(); es.hasMoreElements(); ) {
          publishAdd(es.nextElement());
        }
        // policies are not oplan-associated
        for (Enumeration es = oplan.getPolicies(); es.hasMoreElements(); ) {
          publishAdd(es.nextElement());
        }

        OplanCoupon ow = new OplanCoupon(oplan.getUID(), getClusterIdentifier());
        getCluster().getUIDServer().registerUniqueObject(ow);
        publishAdd(ow);
      }
      myPrivateState.oplanExists = true;
    } else {
      myPrivateState.errorOccurred = true;
    }

    publishChange(myPrivateState);
    closeTransaction(false);
    checkButtonEnable();
  }

  private void processOplanAdds(Collection adds) {
    for (Iterator it = adds.iterator(); it.hasNext();) {
      Oplan oplan = (Oplan) it.next();
      
      IncrementalSubscription is = (IncrementalSubscription) 
	subscribe(new ContributorPredicate(oplan.getUID()));
      contributors.add(is);
    }
  }

  private void processChanges(Collection changes) {
    for (Iterator it = changes.iterator(); it.hasNext();) {
      UID oplanUID = null;
      Object o = it.next();
      if (o instanceof Oplan) {
	oplanUID = ((Oplan) o).getUID();
      } else if (o instanceof OplanContributor) {
	oplanUID = ((OplanContributor) o).getOplanUID();
      } else continue;

      Collection coupons = query( new CouponPredicate(oplanUID) );
      for (Iterator couponIt = coupons.iterator(); couponIt.hasNext();) {
	//System.out.println("SQLOplanPlugIn: publishChanging OplanCoupon");
	publishChange(couponIt.next());
      }
    }
  }

  private void processOplanDeletes(Collection deletes) {
    for (Iterator it = deletes.iterator(); it.hasNext();) {
      Oplan oplan = (Oplan) it.next();
      Collection coupons = query( new CouponPredicate(oplan.getUID()) );
      for (Iterator couponIt = coupons.iterator(); couponIt.hasNext();) {
	publishRemove(couponIt.next());
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







