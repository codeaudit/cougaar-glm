/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.sample;

import org.cougaar.domain.glm.ldm.Constants;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Calendar;
import java.util.Date;

import java.awt.Color;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.domain.planning.ldm.asset.AbstractAsset;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.glm.ldm.asset.Organization;

import org.cougaar.domain.glm.ldm.oplan.Oplan;

import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.TimeAspectValue;
import org.cougaar.domain.planning.ldm.plan.Verb;

import org.cougaar.core.plugin.SimplePlugIn;

import org.cougaar.util.UnaryPredicate;

/**
 * Source Cluster Start/Stop GUI for Strategic Transportation Tasks.
 * <p>
 * Based on plugins used in minitestconfig GLS*PlugIn, especially
 * GLSGUIPlugIn and GLSExpanderPlugIn.  One could use a source configuration 
 * similar to minitestconfig, but that would require a multi-node source 
 * (since GLS is sent to subordinates, and the source shouldn't list itself 
 * as a subordinate) and extra plugins (Oplan forwarding).
 * <p>
 * For a source cluster all we want is to generate and remove our
 * DetermineRequirements Task.
 */

public class StrategicTransportSourceGUIPlugIn extends SimplePlugIn {
  /** frame for 1-button UI **/
  static JFrame frame;

  /** for feedback to user on whether DetermineRequirements was successful **/
  private JLabel statusLabel;

  /** Running task **/
  Task sourceTask;

  /** Control buttons */
  private JButton startButton;
  private JButton stopButton;

  /** A panel to hold the GUI **/
  JPanel panel = new JPanel((LayoutManager) null);

  /** Subscription to hold collection of input tasks **/
  private IncrementalSubscription selfOrgsSub;
  private IncrementalSubscription oplansSub;

  /** Need this for creating new instances of certain objects **/
  RootFactory ldmf;

  /**
   * Self Organization predicate.
   **/
  protected static UnaryPredicate newSelfOrgPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Organization) {
          return ((Organization)o).isSelf();
        }
        return false;
      }
    };
  }

  /**
   * Oplan predicate.
   **/
  protected static UnaryPredicate newOplanPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof Oplan);
      }
    };
  }

  /** Lazy Accessor that caches a reference to the ClusterObjectFactory,
   * provided I am in a state other than UNITIALIZED, UNLOADED, or LOADED.
   * @return ClusterObjectFactory 
   */

  // Have to provide these on this plugin class, else the inner class
  // below will not be able to find them
  public void openTheTransaction() {
    openTransaction();
  }

  public void closeTheTransaction(boolean b) {
    closeTransaction(b);
  }
  
  public void setStatus(String s) {
    setStatus(false, s);
  }

  public void setStatus(boolean success, String s) {
    statusLabel.setForeground(
	(success ? Color.darkGray : Color.red));
    statusLabel.setText(s);
  }

  /**
   * An ActionListener that listens to the DetermineRequirementsTasks 
   * buttons.
   */
  class DRTListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JButton button = (JButton)e.getSource();

      Organization selfOrg;
      Oplan oplan;
      if ((selfOrg = getSelfOrg()) == null) {
        setStatus("No self Organization yet.");
      } else if ((oplan = getOplan()) == null) {
        setStatus("No Oplan yet.");
      } else {
        try {
          // Have to do this as within transaction boundary
          openTheTransaction();
          if (button == startButton) {
            sourceTask = startSource(oplan, selfOrg);
          } else {
            stopSource(sourceTask);
          }
          closeTheTransaction(false);
          if (button == startButton) {
            setStatus(true, "Source Started");
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
          } else {
            setStatus(true, "Source Stopped");
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
	  }
        } catch (Exception exc) {
          setStatus("Failed: "+exc.getMessage());
          System.err.println("Could not execute button: " + 
             e.getActionCommand());
        }
      } 
    }
  }

  private void createGUI() {
    // create buttons and labels
    startButton = new JButton("Start Source");
    stopButton = new JButton("Stop Source");
    startButton.addActionListener(new DRTListener());
    stopButton.addActionListener(new DRTListener());
    stopButton.setEnabled(false);
    statusLabel = new JLabel("<                               >");

    // do layout
    frame = new JFrame("StrategicTransportSourceGUIPlugIn");
    frame.setLocation(0,0);
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    panel.setLayout(gbl);
    gbc.insets = new Insets(15,15,15,15);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbl.setConstraints(startButton, gbc);
    panel.add(startButton);
    gbc.gridx = 1;
    gbl.setConstraints(stopButton, gbc);
    panel.add(stopButton);
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 2;
    gbl.setConstraints(statusLabel, gbc);
    panel.add(statusLabel);
    frame.setContentPane(panel);
    frame.pack();
    frame.setVisible(true);
    setStatus(true, "Ready");
  }


  /**
 * Overrides the setupSubscriptions() in the SimplePlugIn.
 */
  protected void setupSubscriptions() {
    ldmf = theLDMF;

    getSubscriber().setShouldBePersisted(false);

    selfOrgsSub = (IncrementalSubscription)subscribe(newSelfOrgPred());
    oplansSub = (IncrementalSubscription)subscribe(newOplanPred());

    createGUI();
  }

  public synchronized void execute() { }
  
  protected Organization getSelfOrg() {
    Enumeration eSelfOrgs = selfOrgsSub.elements();
    if (eSelfOrgs.hasMoreElements()) {
      Organization orgSelf = (Organization)eSelfOrgs.nextElement();
      return orgSelf;
    }
    return null;
  }

  protected Oplan getOplan() {
    Enumeration eOplans = oplansSub.elements();
    if (eOplans.hasMoreElements()) {
      Oplan oplan = (Oplan)eOplans.nextElement();
      return oplan;
    }
    return null;
  }

  protected Task startSource(
        Oplan oplan, Organization selfOrg) {
    ClusterIdentifier clusterID = this.getCluster().getClusterIdentifier();

    NewTask task = ldmf.newTask();

    // Plan reality
    task.setPlan(ldmf.getRealityPlan());

    // Source clusterID
    task.setSource(clusterID);

    // Destination clusterID
    task.setDestination(clusterID);
    
    // Phrases
    Vector prepphrases = new Vector();
    NewPrepositionalPhrase pp;

    //   OfType StrategicTranportation
    pp = ldmf.newPrepositionalPhrase();
    pp.setPreposition(Constants.Preposition.OFTYPE);
    AbstractAsset strans = null;
    try {
      RootFactory ldmfactory = getFactory();
      Asset strans_proto = ldmfactory.createPrototype(
         Class.forName( "org.cougaar.domain.planning.ldm.asset.AbstractAsset" ),
         "StrategicTransportation" );
      strans = (AbstractAsset)ldmfactory.createInstance( strans_proto );
    } catch (Exception exc) {
      System.out.println("Unable to create abstract strategictransport\n"+exc);
    }
    pp.setIndirectObject(strans);
    prepphrases.addElement(pp);

    //   For self
    pp = ldmf.newPrepositionalPhrase();
    pp.setPreposition(Constants.Preposition.FOR);
    pp.setIndirectObject(selfOrg);
    prepphrases.addElement(pp);

    task.setPrepositionalPhrases( prepphrases.elements() );

    // Verb DetermineRequirements
    task.setVerb(new Verb(Constants.Verb.DETERMINEREQUIREMENTS));

    // Preferences
    task.setPreferences( getPreferences() );

    publishAdd(task);

    return task;
  }

  protected void stopSource(Task t) {
    publishRemove(t);
  }

  protected Enumeration getPreferences() {
    // schedule
    Calendar rightNow = Calendar.getInstance();
    Date startDate = rightNow.getTime();
    // increment date by 3 MONTHs
    rightNow.add(Calendar.MONTH, 3);
    Date endDate = rightNow.getTime();
    
    TimeAspectValue startTav = 
      new TimeAspectValue( AspectType.START_TIME, startDate);
    TimeAspectValue endTav = 
      new TimeAspectValue( AspectType.END_TIME, endDate);

    ScoringFunction myStartScoreFunc = 
      ScoringFunction.createStrictlyAtValue( startTav );
    ScoringFunction myEndScoreFunc = 
      ScoringFunction.createStrictlyAtValue( endTav );    

    Preference startPreference = 
      ldmf.newPreference( AspectType.START_TIME, myStartScoreFunc );
    Preference endPreference = 
      ldmf.newPreference( AspectType.END_TIME, myEndScoreFunc  );

    Vector preferenceVector = new Vector(2);
    preferenceVector.addElement( startPreference );
    preferenceVector.addElement( endPreference );

    return preferenceVector.elements();
  }
}
