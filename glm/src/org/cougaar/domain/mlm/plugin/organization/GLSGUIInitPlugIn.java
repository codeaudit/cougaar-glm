/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.organization;


import javax.swing.*;
import java.awt.event.*;
import java.awt.*;


import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Plan;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.TimeAspectValue;
import org.cougaar.domain.planning.ldm.plan.Verb;

import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.PlugInAdapter;
import org.cougaar.core.plugin.PlugInDelegate;

import org.cougaar.core.society.UID;

import org.cougaar.util.Enumerator;
import org.cougaar.util.StateModelException;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.Constants;

import org.cougaar.domain.glm.ldm.asset.Organization;

import org.cougaar.domain.glm.ldm.oplan.Oplan;

import org.cougaar.domain.mlm.plugin.RandomButtonPusher;
import org.cougaar.domain.mlm.plugin.UICoordinator;

/**
 * The GSLInitExpanderPlugIn will create the initial GetLogSupport task
 * with the OPLAN object.
 *
 * @author       ALPINE <alpine-software@bbn.com>
 * @version      $Id: GLSGUIInitPlugIn.java,v 1.4 2001-04-05 19:27:52 mthome Exp $
 *
 */

public class GLSGUIInitPlugIn extends GLSGUIBasePlugIn {
  /** My private state **/
  private MyPrivateState myPrivateState;

  private IncrementalSubscription myPrivateStateSubscription;

  private static class MyPrivateState extends RandomButtonPusher {
    int taskNumber = 0;
    MyPrivateState() {
      super(Integer.getInteger("org.cougaar.domain.mlm.plugin.organization.GLSGUIInitPlugIn.minSleepTime", 60000).intValue(),
            Integer.getInteger("org.cougaar.domain.mlm.plugin.organization.GLSGUIInitPlugIn.maxSleepTime", 180000).intValue(),
            ((System.getProperty("org.cougaar.domain.mlm.plugin.organization.GLSGUIInitPlugIn.enabled") != null) ?
             "true".equalsIgnoreCase(System.getProperty("org.cougaar.domain.mlm.plugin.organization.GLSGUIInitPlugIn.enabled")) :
             false));
    }
  };
  
  protected void buttonPushed(OplanWrapper wrapper) {
    if (selfOrgAsset == null) {
      System.out.println("\n\nGLSGUIInitPlugIn HAVEN'T RECEIVED SELF ORG ASSET YET.  TRY AGAIN LATER\n\n");
    } else {
      publishRootGLS(selfOrgAsset, wrapper.oplan);
    }
  }

  protected boolean isPrivateStateOk() {
    return myPrivateState != null;
  }

  protected String getGLSLabelText(int nTasks) {
    if (myPrivateState.taskNumber == 0) {
      return "Ready to send GLS task";
    } else {
      return "Sent root GLS task " + myPrivateState.taskNumber;
    }
  }

  protected String getButtonText() {
    return "Send GLS Root";
  }

  protected String getGUITitle() {
    return "GLSInitPlugIn";
  }

  protected void createSubscriptions() {
    myPrivateStateSubscription = RandomButtonPusher.subscribe(getDelegate(), MyPrivateState.class);
  }

  protected void restorePrivateState() {
    handlePrivateState(myPrivateStateSubscription.elements());
  }

  protected void createPrivateState() {
    publishAdd(new MyPrivateState()); // Prime the pump
  }

  /* This will be called every time a new task matches the above predicate */

  protected void handlePrivateState() {
    if (myPrivateStateSubscription.hasChanged()) {
      handlePrivateState(myPrivateStateSubscription.getAddedList());
    }
  }

  private void handlePrivateState(Enumeration e){
    if (myPrivateState == null && e.hasMoreElements()) {
      myPrivateState = (MyPrivateState) e.nextElement();
      UICoordinator.layoutSecondRow(panel, myPrivateState.init("Random Send", getDelegate(), glsButton));
      checkButtonEnable();
    }
  }
  
  public void publishRootGLS(Organization me, Oplan oplan) {
    openTransaction();
    NewTask task = theLDMF.newTask();
    // ensure this is a root level task
    task.setPlan(theLDMF.getRealityPlan());
    task.setSource(this.getCluster().getClusterIdentifier());
    task.setDestination(this.getCluster().getClusterIdentifier());
    
    // set prepositional phrases
    Vector phrases = new Vector(3);
    NewPrepositionalPhrase newpp;

    // Removed following as being redundant with the task's context
//      newpp = theLDMF.newPrepositionalPhrase();
//      newpp.setPreposition(Constants.Preposition.WITH);
//      newpp.setIndirectObject(oplan);
//      phrases.add(newpp);

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(Constants.Preposition.FOR);
    newpp.setIndirectObject(me);
    phrases.add(newpp);

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition("ForRoot");
    newpp.setIndirectObject(new Integer(++myPrivateState.taskNumber));
    publishChange(myPrivateState);

    phrases.add(newpp);

    task.setPrepositionalPhrases(phrases.elements());

    // verb
    task.setVerb(Constants.Verb.GetLogSupport);

    // schedule
    long startTime = currentTimeMillis();
    long endTime;
    Date endDay = oplan.getEndDay();
    if (endDay != null) {
      endTime = endDay.getTime();
    } else {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date(startTime));
      // increment date by 3 MONTHs
      cal.add(Calendar.MONTH, 3);
      endTime = cal.getTime().getTime();
    }

    TimeAspectValue startTav = new TimeAspectValue(AspectType.START_TIME, startTime);
    TimeAspectValue endTav = new TimeAspectValue(AspectType.END_TIME, endTime);

    ScoringFunction myStartScoreFunc = ScoringFunction.createStrictlyAtValue( startTav );
    ScoringFunction myEndScoreFunc = ScoringFunction.createStrictlyAtValue( endTav );    

    Preference startPreference = theLDMF.newPreference( AspectType.START_TIME, myStartScoreFunc );
    Preference endPreference = theLDMF.newPreference( AspectType.END_TIME, myEndScoreFunc  );

    Vector preferenceVector = new Vector(2);
    preferenceVector.addElement( startPreference );
    preferenceVector.addElement( endPreference );

    task.setPreferences( preferenceVector.elements() );

    // Set the context
    try {
      UID oplanUID = oplan.getUID();
      ContextOfUIDs context = new ContextOfUIDs(oplanUID);
      System.out.println("GLSGUIInitPlugIn: Setting context to: " + oplanUID);
      task.setContext(context);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    publishAdd(task);
    System.out.println("\n" + formatDate(System.currentTimeMillis()) + " Send Task: " + task);
    closeTransaction(false);
  }
}
