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


package org.cougaar.mlm.debug.ui;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.glm.ldm.plan.Capability;
import org.cougaar.planning.ldm.plan.ClusterObjectFactory;
import org.cougaar.planning.ldm.plan.NewPlanElement;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.asset.Asset;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Vector;

import org.cougaar.core.plugin.PlugInDelegate;

/** Supports user input of tasks.
 */

public class UserInputTask {
  NewTask task;
  UIPlugIn uiPlugIn;
  ClusterObjectFactory cof;
  ClusterIdentifier myClusterId;
  String myClusterName;
  Plan plan;
  PlugInDelegate delegate;

  /** Create a new task with the user input.
   */

  public UserInputTask(UIPlugIn uiPlugIn, PlugInDelegate delegate,
		       String destinationName,
		       String objectClusterAssetName,
		       String objectPhysicalAssetName,
		       Vector objectCapabilities,
		       int objectAssetQuantity,
		       String phrasePreposition,
		       String phraseClusterAssetName,
		       String phrasePhysicalAssetName,
		       Vector phraseCapabilities,
		       int phraseAssetQuantity,
		       String verb,
		       String startDate, String endDate, String bestDate) {
    this.uiPlugIn = uiPlugIn;
    this.delegate = delegate;
    cof = delegate.getFactory();
    myClusterId = delegate.getClusterIdentifier();
    myClusterName = myClusterId.getAddress();
    plan = cof.getRealityPlan();
    task = cof.newTask();
    task.setPlan(plan);
    task.setSource(myClusterId);
    task.setDestination(uiPlugIn.getClusterIdFromName(destinationName));
    task.setDirectObject(createAsset(objectClusterAssetName, 
				     objectPhysicalAssetName,
				     objectCapabilities, 
				     objectAssetQuantity));
    if (phrasePreposition != "") {
      NewPrepositionalPhrase phrase = cof.newPrepositionalPhrase();
      phrase.setPreposition(phrasePreposition);
      phrase.setIndirectObject(createAsset(phraseClusterAssetName, 
					   phrasePhysicalAssetName,
					   phraseCapabilities, 
					   phraseAssetQuantity));
      task.setPrepositionalPhrase(phrase);
    }
    if (verb != "")
      task.setVerb(new Verb(verb));
    // set dates in penalty function
    //if ((startDate != "") && (endDate != "") && (bestDate != ""))
    //  task.setPenaltyFunction(createPenaltyFunction(startDate, endDate, 
		//				    bestDate));
  }

  /** Create a schedule from a start and end date.
   */

  public static Schedule createSchedule(ClusterObjectFactory cof,
					String startDate, 
					String endDate) {
    DateFormat dateFormat = 
      DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    Schedule schedule = null;
    try {
      //schedule = cof.newSchedule(dateFormat.parse(startDate),
      schedule = cof.newSimpleSchedule(dateFormat.parse(startDate),
				 dateFormat.parse(endDate));
    } catch (ParseException pe) {
      System.out.println("Exception parsing dates: " + pe);
    }
    return schedule;
  }
  
  /** Create a schedule element from a start and end date.
   */

  public static ScheduleElement createScheduleElement(ClusterObjectFactory cof,
					String startDate, 
					String endDate) {
    DateFormat dateFormat = 
      DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    ScheduleElement scheduleelement = null;
    try {
      scheduleelement = cof.newScheduleElement(dateFormat.parse(startDate),
				 dateFormat.parse(endDate));
    } catch (ParseException pe) {
      System.out.println("Exception parsing dates: " + pe);
    }
    return scheduleelement;
  }

  //private PenaltyFunction createPenaltyFunction(String startDate, 
		//				String endDate, 
		//				String bestDate) {
    //DateFormat dateFormat = 
    //  DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
    //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    //PenaltyFunction penaltyFunction = null;
    //try {
    // penaltyFunction = cof.newDesiredSchedule(dateFormat.parse(startDate),
		//			       dateFormat.parse(endDate),
		//			       dateFormat.parse(bestDate));
    //} catch (ParseException pe) {
    //  System.out.println("Exception parsing dates: " + pe);
    //}
    //return penaltyFunction;
  //}

  private Asset createAsset(String clusterAssetName, String physicalAssetName,
			    Vector capabilities, int quantity) {
    if (capabilities != null) {
      System.out.println("Creating asset capabilities not supported.");
    } 
    if (clusterAssetName != ""){
      try {
	return (Asset) delegate.getFactory().createInstance( clusterAssetName.trim() , clusterAssetName.trim() );
      } catch (Exception e) {
	System.out.println("Unable to construct an organizational asset in UserInputTask.creatAsset().  Exception is " + e.toString() );
	e.printStackTrace();
      }
    }	

    if (quantity > 1) {
      System.out.println("Creating aggregate assets not supported.");
      return null;
    }

    if (physicalAssetName != "") {
      System.out.println("Creating physical assets not supported.");
      return null;
    }

    return null;
  }

  /** Create an allocation, and a plan element, and add the task
    being composed to the local log plan.
    */

  public void addToLogPlan() {
  }

}








