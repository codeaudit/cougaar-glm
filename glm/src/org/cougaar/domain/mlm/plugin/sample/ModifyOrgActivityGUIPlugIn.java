/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

/**
 * Modify an Org Activity GUI.
 * <p>
 * Allows user to look up the self org's org activity for a given
 * activity type.  Several fields in the org activity can then
 * be changed:<br>
 * <ul>
 *   <li>OpTempo</li>
 *   <li>start timespan date</li>
 *   <li>stop timespan date</li>
 * </ul>
 * <p>
 * This plugin isn't responsible for forwarding the altered org 
 * activity.  As far as this plugin is concerned, the change is 
 * only visible to it's logplan. (That's why the OrgId is fixed
 * to the self org...)
 */

package org.cougaar.domain.mlm.plugin.sample;

import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.plan.*;
import org.cougaar.domain.glm.asset.*;
import org.cougaar.domain.glm.oplan.*;

import java.util.Enumeration;
import java.util.Date;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.domain.glm.asset.Organization;
import org.cougaar.domain.glm.oplan.Oplan;
import org.cougaar.domain.glm.oplan.OrgActivity;
import org.cougaar.domain.glm.oplan.TimeSpan;

import org.cougaar.core.plugin.SimplePlugIn;

import org.cougaar.util.ShortDateFormat;

import org.cougaar.util.UnaryPredicate;

public class ModifyOrgActivityGUIPlugIn extends SimplePlugIn {

  /** org activity info **/
  private JLabel orgIdLabel;
  private JTextField typeText;
  private JTextField opTempoText;
  private JLabel cDayLabel;
  private JTextField startText;
  private JTextField thruText;

  /** status label **/
  private JLabel statusLabel;

  /** Control buttons */
  private JButton getOrgActButton;
  private JButton modifyOrgActButton;

  /** Subscription to hold collection of input tasks **/
  private IncrementalSubscription orgsSub;
  private IncrementalSubscription orgActivitiesSub;
  private IncrementalSubscription oplansSub;

  /** Date formatter for "month/day/year" **/
  private ShortDateFormat dateFormatter = new ShortDateFormat();

  /** Need this for creating new instances of certain objects **/
  private RootFactory ldmf;

  private static final String defaultOrgActivityType = "Deployment";

  /*
   * Organization predicate.
   **/
  protected static UnaryPredicate newOrgPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof Organization);
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

  /**
   * OrgActivity predicate.
   **/
  protected static UnaryPredicate newOrgActivityPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof OrgActivity);
      }
    };
  }

  // Have to provide these on this plugin class, else the inner class
  // below will not be able to find them
  protected void openTheTransaction() {
    openTransaction();
  }

  protected void closeTheTransaction(boolean b) {
    closeTransaction(b);
  }
  
  protected void setStatus(String s) {
    setStatus(false, s);
  }

  protected void setStatus(boolean success, String s) {
    statusLabel.setForeground(
      (success ?  Color.darkGray : Color.red));
    statusLabel.setText(s);
  }

  /**
   * For GUI internal use.  Update GUI org activity info.
   * @return error string, null if success
   */
  protected String drawOrgActivity(OrgActivity orgAct, Oplan oplan) {
    Date cDay;
    if ((oplan == null) ||
        ((cDay = oplan.getCday()) == null)) {
      return "Oplan lacks cDay";
    }
    String cDateString = 
       dateFormatter.toString(
         cDay);
    if (orgAct == null) {
      orgIdLabel.setText("");
      //typeText.setText("");
      opTempoText.setText("");
      cDayLabel.setText(cDateString);
      startText.setText("");
      thruText.setText("");
      return "Missing Org Activity";
    }
    if (orgAct.getOplanUID() != oplan.getUID()) {
      return ("Wrong Oplan ("+oplan.getUID()+
              ") for this Org Activity ("+orgAct.getUID()+")!");
    }
    TimeSpan timeSpan = orgAct.getTimeSpan();
    if (timeSpan == null) {
      return "Missing Org Activity TimeSpan";
    } 
    String startDateString;
    if (timeSpan.getStartDate() == null)
      startDateString = cDateString;
    else {
      startDateString = 
        dateFormatter.toString(
            timeSpan.getStartDate());
    }
    String thruDateString;
    if (timeSpan.getThruDate() == null)
      thruDateString = cDateString;
    else {
      thruDateString = 
        dateFormatter.toString(
            timeSpan.getThruDate());
    }
    orgIdLabel.setText(orgAct.getOrgID());
    //typeText.setText(orgAct.getActivityType());
    opTempoText.setText(orgAct.getOpTempo());
    cDayLabel.setText(cDateString);
    startText.setText(startDateString);
    thruText.setText(thruDateString);
    return null;
  }

  /**
   * For GUI internal use.  change org activity to GUI's info.
   * @return error string, null if success
   */
  protected String changeOrgActivity(OrgActivity orgAct, Oplan oplan) {
    Date cDay;
    if ((oplan == null) ||
        ((cDay = oplan.getCday()) == null)) {
      return "Oplan lacks cDay";
    }
    String opTempo = opTempoText.getText().trim();
    if (opTempo.length() < 1) {
      return "Invalid OpTempo";
    }
    TimeSpan timeSpan = orgAct.getTimeSpan();
    if (timeSpan == null) {
      return "Missing Org Activity TimeSpan";
    }
    Date startDate = dateFormatter.toDate(startText.getText().trim(), false);
    if (startDate == null) {
      return "Invalid start date";
    }
    Date thruDate = dateFormatter.toDate(thruText.getText().trim(), false);
    if (thruDate == null) {
      return "Invalid thru date";
    }
    boolean changed = false;
    if (!opTempo.equals(orgAct.getOpTempo())) {
      orgAct.setOpTempo(opTempo);
      changed = true;
    }
    if (!startDate.equals(timeSpan.getStartDate())) {
      timeSpan.setStartDate(startDate);
      changed = true;
    }
    if (!thruDate.equals(timeSpan.getThruDate())) {
      timeSpan.setThruDate(thruDate);
      changed = true;
    }
    if (!changed) {
      return "No change to Org Activity";
    }
    publishChange(orgAct);
    return null;
  }

  /**
   * An ActionListener that listens to the buttons.
   */
  class DRTListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JButton button = (JButton)e.getSource();

      String activityType = typeText.getText().trim();
      Organization selfOrg;
      Oplan oplan;
      OrgActivity orgAct;
      if ((selfOrg = getSelfOrg()) == null) {
        setStatus("Missing self organization.");
      } else if ((oplan = getOplan()) == null) {
        setStatus("No Oplan yet.");
      } else if ((orgAct = getOrgActivity(selfOrg, activityType)) == null) {
        drawOrgActivity(null, oplan);
        setStatus("No \""+activityType+"\" Org activities.");
      } else {
        try {
          // Have to do this as within transaction boundary
          openTheTransaction();
          if (button == getOrgActButton) {
            String sError = drawOrgActivity(orgAct, oplan);
            if (sError == null) {
              setStatus(true, "Read Org Activity");
            } else {
              setStatus(sError);
            }
          } else {
            String sError =  changeOrgActivity(orgAct, oplan);
            if (sError == null) {
              setStatus(true, "Modified Org Activity");
            } else {
              setStatus(sError);
            }
          }
          closeTheTransaction(false);
        } catch (Exception exc) {
          setStatus("Failed: "+exc.getMessage());
          System.err.println("Could not execute button: " + 
             e.getActionCommand());
        }
      } 
    }
  }

  private String getClusterID() {
    try {
      return getCluster().getClusterIdentifier().toString();
    } catch (Exception e) {
      return "<UNKNOWN>";
    }
  }

  private void createGUI() {
    // Create buttons, labels, etc
    orgIdLabel = new JLabel();
    orgIdLabel.setForeground(Color.black);
    typeText = new JTextField(11);
    typeText.setText(defaultOrgActivityType);
    opTempoText = new JTextField(11);
    cDayLabel = new JLabel();
    cDayLabel.setForeground(Color.black);
    startText = new JTextField(11);
    thruText = new JTextField(11);
    getOrgActButton = new JButton("Get Org Activity");
    getOrgActButton.addActionListener(new DRTListener());
    modifyOrgActButton = new JButton("Modify Org Activity");
    modifyOrgActButton.addActionListener(new DRTListener());
    statusLabel = new JLabel();
    setStatus(true, "<                                  >");

    // do layout
    JFrame frame =
      new JFrame("ModifyOrgActivityGUIPlugIn  "+getClusterID());
    frame.setLocation(0,0);
    JPanel rootPanel = new JPanel((LayoutManager) null);
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(15,15,15,15);
    gbc.fill = GridBagConstraints.BOTH;
    rootPanel.setLayout(gbl);

    // org activity info
    JPanel orgActPanel = new JPanel();
    orgActPanel.setLayout(new BorderLayout());
    JLabel orgActLabel = new JLabel("Org Activity:");
    orgActLabel.setForeground(Color.blue);
    orgActPanel.add(orgActLabel, BorderLayout.NORTH);
    JPanel orgActInfoPanel = new JPanel();
    orgActInfoPanel.setLayout(new BorderLayout());
    JPanel orgActInfoTextPanel = new JPanel();
    orgActInfoTextPanel.setLayout(new GridLayout(6,1));
    orgActInfoTextPanel.add(new JLabel("OrgId:"));
    orgActInfoTextPanel.add(new JLabel("Type:"));
    orgActInfoTextPanel.add(new JLabel("OpTempo:"));
    orgActInfoTextPanel.add(new JLabel("cDay:"));
    orgActInfoTextPanel.add(new JLabel("Start Date:"));
    orgActInfoTextPanel.add(new JLabel("Thru Date:"));
    JPanel orgActInfoValuePanel = new JPanel();
    orgActInfoValuePanel.setLayout(new GridLayout(6,1));
    orgActInfoValuePanel.add(orgIdLabel);
    orgActInfoValuePanel.add(typeText);
    orgActInfoValuePanel.add(opTempoText);
    orgActInfoValuePanel.add(cDayLabel);
    orgActInfoValuePanel.add(startText);
    orgActInfoValuePanel.add(thruText);
    orgActInfoPanel.add(orgActInfoTextPanel, BorderLayout.CENTER);
    orgActInfoPanel.add(orgActInfoValuePanel, BorderLayout.EAST);
    orgActPanel.add(orgActInfoPanel, BorderLayout.CENTER);
    gbl.setConstraints(orgActPanel, gbc);
    rootPanel.add(orgActPanel);

    gbc.gridy = 2;
    gbl.setConstraints(getOrgActButton, gbc);
    rootPanel.add(getOrgActButton);

    gbc.gridy = 3;
    gbl.setConstraints(modifyOrgActButton, gbc);
    rootPanel.add(modifyOrgActButton);

    JPanel statusPanel = new JPanel();
    statusPanel.setLayout(new GridLayout(2,1));
    JLabel statusLabelLabel = new JLabel("Status:");
    statusLabelLabel.setForeground(Color.blue);
    statusPanel.add(statusLabelLabel);
    statusPanel.add(statusLabel);
    gbc.gridy = 4;
    gbl.setConstraints(statusPanel, gbc);
    rootPanel.add(statusPanel);

    frame.setContentPane(rootPanel);
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

    orgsSub = (IncrementalSubscription)subscribe(newOrgPred());
    orgActivitiesSub = (IncrementalSubscription)subscribe(newOrgActivityPred());
    oplansSub = (IncrementalSubscription)subscribe(newOplanPred());

    createGUI();
  }

  protected void execute() { }
  
  protected static String getOrgId(Organization org) {
    String s = null;
    try {
      // FOR NOW:
      s = org.getClusterPG().getClusterIdentifier().toString();
      // FOR LATER:
      //s = org.getItemIdentificationPG().getItemIdentification();
    } catch (Exception e) {}
    return s;
  }

  protected Organization getSelfOrg() {
    Enumeration eOrgs = orgsSub.elements();
    while (eOrgs.hasMoreElements()) {
      Organization org = (Organization)eOrgs.nextElement();
      
      if (org.isSelf()) {
        return org;
      }
    }
    return null;
  }

  protected OrgActivity getOrgActivity(Organization org, String activityType) {
    return getOrgActivity(getOrgId(org), activityType);
  }

  protected OrgActivity getOrgActivity(String orgId, String activityType) {
    OrgActivity orgAct = null;
    if ((orgId != null) && (activityType != null)) {
      Enumeration eOrgActs = orgActivitiesSub.elements();
      while (eOrgActs.hasMoreElements()) {
        OrgActivity oa = (OrgActivity)eOrgActs.nextElement();
        if (orgId.equals(oa.getOrgID()) &&
              activityType.equals(oa.getActivityType())) {
            orgAct = oa;
          break;
        }
      }
    }
    return orgAct;
  }

  protected Oplan getOplan() {
    Oplan oplan = null;
    // which oplan?  take the first one for now.
    Enumeration eOplans = oplansSub.elements();
    if (eOplans.hasMoreElements()) {
      oplan = (Oplan)eOplans.nextElement();
    }
    return oplan;
  }
}
