/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */


package org.cougaar.mlm.debug.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.TimeZone;
import java.util.Vector;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.ClusterObjectFactory;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.plugin.legacy.PluginDelegate;

/** Supports user input of tasks.
 */

public class UserInputTask {
  NewTask task;
  UIPlugin uiPlugin;
  ClusterObjectFactory cof;
  MessageAddress myClusterId;
  String myClusterName;
  Plan plan;
  PluginDelegate delegate;

  /** Create a new task with the user input.
   */

  public UserInputTask(UIPlugin uiPlugin, PluginDelegate delegate,
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
    this.uiPlugin = uiPlugin;
    this.delegate = delegate;
    cof = delegate.getFactory();
    myClusterId = delegate.getMessageAddress();
    myClusterName = myClusterId.getAddress();
    plan = cof.getRealityPlan();
    task = cof.newTask();
    task.setPlan(plan);
    task.setSource(myClusterId);
    task.setDestination(uiPlugin.getClusterIdFromName(destinationName));
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
      task.setPrepositionalPhrases(phrase);
    }
    if (verb != "")
      task.setVerb(Verb.get(verb));
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








