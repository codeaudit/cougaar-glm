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

package org.cougaar.mlm.plugin.sample;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.oplan.*;

import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.glm.ldm.plan.Capability;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;
import org.cougaar.core.mts.MessageAddress;

import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AbstractAsset;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;

import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.planning.plugin.util.ExpanderHelper;
import org.cougaar.planning.plugin.util.PluginHelper;

import org.cougaar.util.UnaryPredicate;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Iterator;

public class StrategicTransportProjectionExpanderPlugin extends SimplePlugin {
	
  private IncrementalSubscription myExpansions;
  private IncrementalSubscription incomingTasks;
  private IncrementalSubscription nonorgassets;
  // Look for Failed Dispositions
  private IncrementalSubscription allocs;
  private int count = 0;
  long startTime, endTime;
	
  private long now;

  private final static long ONE_DAY = 24L*60L*60L*1000L;
  private final static long ONE_WEEK = 7L*ONE_DAY;

  protected void setupSubscriptions() {
    now = currentTimeMillis(); // start of run (scenario time)
    //subscribe to incoming tasks
    incomingTasks = (IncrementalSubscription) subscribe(taskPred);
    myExpansions = (IncrementalSubscription) subscribe(expansionPred);
    nonorgassets = (IncrementalSubscription) subscribe(noassetPred);
  }	

  protected void execute() {
    if ( incomingTasks.hasChanged() ) {
      for (Enumeration newtasks = incomingTasks.getAddedList(); newtasks.hasMoreElements(); ) {
	expand((Task) newtasks.nextElement());
      }
    }
    
    if (myExpansions.hasChanged()) {
      watchExpansions(myExpansions.getAddedList());
      watchExpansions(myExpansions.getChangedList());
      PluginHelper.updateAllocationResult(myExpansions);
    }
  }

  private void watchExpansions(Enumeration changedExpansions) {
    while (changedExpansions.hasMoreElements()) {
      NewExpansion exp = (NewExpansion) changedExpansions.nextElement();
      for (Iterator iter = exp.getSubTaskResults().iterator(); iter.hasNext(); ) {
        SubTaskResult str = (SubTaskResult) iter.next();
        if (str.hasChanged()) {
          Task task = str.getTask();
          AllocationResult repAR = str.getAllocationResult();
          if (repAR != null && !repAR.isSuccess()) {
            updatePreferences((NewTask) task);
          }
        }
      }
    }
  }
  
  private void expand (Task task) {
    //	System.out.println("&&&&&&&&&&&&&&&&& DRtoST Starting to expand");
    Asset mystuff = null;
    //	System.out.println("&&&&&&&&&&&&&&&&&&&&& DRtoST myComponent is: " + myComponent);
    MessageAddress me = getMessageAddress();
    Verb newverb = Verb.get(Constants.Verb.TRANSPORT);
    startTime = now;
    // increment date by 5 DAYS
    now += 5 * ONE_DAY;
    endTime = now;
    Vector mypreferences = new Vector();
    Enumeration e = nonorgassets.elements();
    NewWorkflow wf = theLDMF.newWorkflow();
    wf.setIsPropagatingToSubtasks(true);
    wf.setParentTask(task);
    AllocationResult estAR = null;
    if (!e.hasMoreElements()) { // No non-org assets
      estAR = PluginHelper.createEstimatedAllocationResult(task, theLDMF, 1.0, true);
    }
    while (e.hasMoreElements()) {
      if (++count > 10) {
        resettime();		// after every 10 tasks, change the date
      }
      NewTask subtask = theLDMF.newTask();

      mystuff = (Asset) e.nextElement();
				//}
      if ( mystuff != null ) {
        //System.out.println("&&&&&&&&&&&&&&&&&&&&& DRtoST solenoid myasset: " + mystuff);
      } else {
        System.err.println("&&&&&&&&&&&&&&&&&&&&&& DRtoST solenoid myasset is NULL!!!");
      }

				// Look for prepositionalphrase in parent task, FOR <clusterasset>
				// and copy it into the new TRANSPORT subtask
      Vector prepphrases = new Vector();
      Enumeration origpp = task.getPrepositionalPhrases();
      while (origpp.hasMoreElements()) {
        PrepositionalPhrase app = (PrepositionalPhrase) origpp.nextElement();
        if ((app.getPreposition().equals(Constants.Preposition.FOR)) && (app.getIndirectObject() instanceof Asset) ) {	
          prepphrases.addElement(app);
        }
      }
 
				// create prepositionalphrase oftype strategictransportation
      NewPrepositionalPhrase pp = theLDMF.newPrepositionalPhrase();
      pp.setPreposition(Constants.Preposition.OFTYPE);
      AbstractAsset strans = null;
      try {
          PlanningFactory ldmfactory = getFactory();
          Asset strans_proto = ldmfactory.createPrototype( Class.forName( "org.cougaar.planning.ldm.asset.AbstractAsset" ), "StrategicTransportation" );
          strans = (AbstractAsset)ldmfactory.createInstance( strans_proto );
      } catch (Exception exc) {
        System.out.println("DRtoSTExp - problem creating the abstract strategictransport asset");
        exc.printStackTrace();
      }
      pp.setIndirectObject(strans);
      prepphrases.addElement(pp);

      NewPrepositionalPhrase from = new PrepositionalPhraseImpl();
      from.setPreposition(Constants.Preposition.FROM);
      NewGeolocLocation fromgl = new GeolocLocationImpl();
      fromgl.setName("FT STEWART");
      fromgl.setGeolocCode("HKUZ");
      fromgl.setCountryStateName("GEORGIA");
      from.setIndirectObject(fromgl);
      prepphrases.addElement(from);
		
      NewPrepositionalPhrase to = new PrepositionalPhraseImpl();
      to.setPreposition(Constants.Preposition.TO);
      NewGeolocLocation togl = new GeolocLocationImpl();
      togl.setName("FT IRWIN");
      togl.setGeolocCode("HFXZ");
      togl.setCountryStateName("CALIFORNIA");
      to.setIndirectObject(togl);
      prepphrases.addElement(to);	

      // Create transport task 
      subtask.setParentTask( task );
      subtask.setDirectObject( mystuff );
      subtask.setPrepositionalPhrases( prepphrases.elements() );
      subtask.setVerb( newverb );
      subtask.setPlan( task.getPlan() );
				// create some start and end time preferences
      mypreferences.removeAllElements();
      AspectValue startAV = AspectValue.newAspectValue(AspectType.START_TIME, startTime);
      ScoringFunction startSF = ScoringFunction.createPreferredAtValue(startAV, 2);
      Preference startPref = theLDMF.newPreference(AspectType.START_TIME, startSF);
      mypreferences.addElement(startPref);
      AspectValue endAV = AspectValue.newAspectValue(AspectType.END_TIME, endTime);
      ScoringFunction endSF = ScoringFunction.createPreferredAtValue(endAV, 2);
      Preference endPref = theLDMF.newPreference(AspectType.END_TIME, endSF);
      mypreferences.addElement(endPref);
      subtask.setPreferences( mypreferences.elements() );
      subtask.setSource( me );
      wf.addTask(subtask);
      subtask.setWorkflow(wf);
      publishAdd(subtask);
    }
    publishAdd(wf);
    createTheExpansion(wf, task, estAR);
  }
    
    
  // change the start & end time of the tasks
  private void resettime() {
    // increment date by 2 DAYS
    now += 2 * ONE_DAY;
    startTime = now;
    // increment date by 5 DAYS
    now += 5 * ONE_DAY;
    endTime = now;
    //reset counter
    count = 0;
  }
  
  private void createTheExpansion(Workflow wf, Task parent, AllocationResult estAR) {
    // make the expansion
    PlanElement pe = theLDMF.createExpansion(theLDMF.getRealityPlan(),
					     parent,
					     wf,
					     estAR);
    publishAdd(pe);
  }
  
  //If the reported allocation result has been successfully calculated
  //for the workflow set the estimated equal to the reported to send a
  //notification back up.  This is currently not used. The
  //NotificationLP is handling propagation of AllocationResults.
  
  private void updateAllocationResult(PlanElement cpe) {
    if (cpe.getReportedResult() != null) {
      // compare allocationresult objects.
      // if they are not equal, re-set the estimated result
      // for now ignore whether the composition of the results are the same.
      AllocationResult reportedresult = cpe.getReportedResult();
      AllocationResult estimatedresult = cpe.getEstimatedResult();
      if ( (estimatedresult == null) || (! (estimatedresult.equals(reportedresult)) ) ) {
        cpe.setEstimatedResult(reportedresult);
        publishChange(cpe);
      }
    }
  }
  

  // If we get a Failed Disposition (AllocationResult.isSuccess() == false),
  // then we want to change our Preferences a bit and try again...
  // N.B. if this Plugin was running in a more complex society, we may want to
  // incorporate some thread-safe code here...
  private void updatePreferences( NewTask t ) {
    Preference endTimePreference = t.getPreference(AspectType.END_TIME);
    long old_end = endTimePreference.getScoringFunction().getBest().getAspectValue().longValue();
    long new_end = old_end + ONE_WEEK;
// // This is what we did until AspectValues were immutable.
//    endTimePreference.getScoringFunction().getBest().getAspectValue().setValue(new_end);

    AspectValue endAV = AspectValue.newAspectValue(AspectType.END_TIME, new_end);
    ScoringFunction endSF = ScoringFunction.createPreferredAtValue(endAV, 2);
    Preference endPref = theLDMF.newPreference(AspectType.END_TIME, endSF);
    t.setPreference(endPref);

    publishChange((Task) t);
  }

  /**
   * Test if this object is an expansion of one of our tasks. We use
   * taskPred for the latter.
   **/
  private static UnaryPredicate expansionPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Expansion) {
        return taskPred.execute(((Expansion) o).getTask());
      } 
      return false;
    }
  };
	
  /**
   * The predicate for our incoming tasks. We are looking for
   * DETERMINEREQUIREMENT tasks of type Asset where the asset type is
   * StrategicTransportation.
   **/
  private static UnaryPredicate taskPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Task) {
        Task t = (Task) o;
        if (t.getVerb().equals(Constants.Verb.DETERMINEREQUIREMENTS)) {
          return (ExpanderHelper.isOfType(t, Constants.Preposition.OFTYPE, "StrategicTransportation"));
        }
      }
      return false;
    }
  };
	
  /**
   * This predicate selects our organic assets. These are the assets
   * we need to transport if we move.
   **/
  private static UnaryPredicate noassetPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      return ((o instanceof Asset) && !(o instanceof Organization));
    }
  };
}
