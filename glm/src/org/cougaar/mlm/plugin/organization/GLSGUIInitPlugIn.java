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

package org.cougaar.mlm.plugin.organization;


import javax.swing.*;
import java.awt.event.*;
import java.awt.*;


import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Collection;
import java.util.Iterator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.core.domain.RootFactory;

import org.cougaar.planning.ldm.asset.Asset;

import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.TimeAspectValue;
import org.cougaar.planning.ldm.plan.Verb;

import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.PlugInAdapter;
import org.cougaar.core.plugin.PlugInDelegate;

import org.cougaar.core.util.UID;

import org.cougaar.util.Enumerator;
import org.cougaar.util.StateModelException;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.glm.ldm.Constants;

import org.cougaar.glm.ldm.asset.Organization;

import org.cougaar.glm.ldm.oplan.Oplan;

import org.cougaar.mlm.plugin.RandomButtonPusher;
import org.cougaar.mlm.plugin.UICoordinator;

/**
 * The GSLInitExpanderPlugIn will create the initial GetLogSupport task
 * with the OPLAN object.
 *
 * @author       ALPINE <alpine-software@bbn.com>
 *
 *
 */

public class GLSGUIInitPlugIn extends GLSGUIBasePlugIn {
  private static final String MIN_SLEEP_PROP =
    "org.cougaar.mlm.plugin.organization.GLSGUIInitPlugIn.minSleepTime";
  private static final String MAX_SLEEP_PROP =
    "org.cougaar.mlm.plugin.organization.GLSGUIInitPlugIn.maxSleepTime";
  private static final int MIN_SLEEP_DFLT = 60000;
  private static final int MAX_SLEEP_DFLT = 180000;
  private static final String ENABLED_PROP =
    "org.cougaar.mlm.plugin.organization.GLSGUIInitPlugIn.enabled";
  private static final String ENABLED_DFLT = "false";
  private static final String HIDDEN_PROP =
    "org.cougaar.mlm.plugin.organization.GLSGUIInitPlugIn.hidden";
  private static final String HIDDEN_DFLT = "true";
  private IncrementalSubscription sendGLSRootSubscription;

  private static UnaryPredicate sendGLSRootPredicate = new UnaryPredicate() {
	               public boolean execute(Object o) {
		        	if (o instanceof java.lang.String)
			                   return ((o.toString()).equalsIgnoreCase("sendGLSRoot"));
					return false;   
		                }
	                };

  /** My private state **/
  private MyPrivateState myPrivateState;

  private IncrementalSubscription myPrivateStateSubscription;

  private static class MyPrivateState extends RandomButtonPusher {
    int taskNumber = 0;
    boolean hidden;
    MyPrivateState() {
      super(Integer.getInteger(MIN_SLEEP_PROP, MIN_SLEEP_DFLT).intValue(),
            Integer.getInteger(MAX_SLEEP_PROP, MAX_SLEEP_DFLT).intValue(),
            "true".equalsIgnoreCase(System.getProperty(ENABLED_PROP, ENABLED_DFLT)));
      hidden = "true".equalsIgnoreCase(System.getProperty(HIDDEN_PROP, HIDDEN_DFLT));
    }
    public boolean isHidden() {
      return hidden;
    }
  }
  
  protected void sendPSP(OplanWrapper wrapper) {
	if (selfOrgAsset == null) {
		System.out.println("\n\nGLSGUIInitPlugIn HAVEN'T RECEIVED SELF ORG ASSET YET.  TRY AGAIN LATER\n\n");
	} else {
		doPublishRootGLS(selfOrgAsset, wrapper.oplan);
	}
  }
  
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
    sendGLSRootSubscription = (IncrementalSubscription)subscribe(sendGLSRootPredicate);
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
      if (!myPrivateState.isHidden()) {
        UICoordinator.layoutSecondRow(panel, myPrivateState.init("Random Send", getDelegate(), glsButton));
      }
      checkButtonEnable();
    }
  }
  
  	public void sendThePSP()
	{
		OplanWrapper wrapper = (OplanWrapper) oplanCombo.getSelectedItem();
		if (wrapper != null) sendPSP(wrapper);
	}

	protected void additionalExecute()
	{

		Collection sendIt=sendGLSRootSubscription.getAddedCollection();
		if (sendIt!=null && sendIt.size() > 0) {
			for (Iterator iterator = sendIt.iterator();iterator.hasNext();) {
				Object object = iterator.next();
				getBlackboardService().publishRemove(object);
			}
			sendThePSP();
		}

	}
  
	public void publishRootGLS(Organization me, Oplan oplan) {
		openTransaction();
		doPublishRootGLS(me, oplan);
		closeTransaction(false);
	}

private void doPublishRootGLS(Organization me, Oplan oplan) {    
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
  }
}
