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

package org.cougaar.mlm.plugin.sample;

import org.cougaar.glm.ldm.Constants;

import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.oplan.*;

import org.cougaar.core.domain.RootFactory;

import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.planning.ldm.asset.AbstractAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;

import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Predictor;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.Workflow;

import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.util.ExpanderHelper;

import org.cougaar.util.UnaryPredicate;

import java.util.*;

/**
 * This class implements a PlugIn that uses a Predictor (if available) to
 * generate new subtasks from an incoming parent Task.
 */
public class PredictorExpanderPlugIn extends SimplePlugIn
{
    
    // 
    // Begin inner class TransportTuple
    //
    private class TransportTuple
    {
        private GeolocLocation from;
        private GeolocLocation to;
        private Date start_time;
        private Date end_time;

        public TransportTuple(GeolocLocation from,
                              GeolocLocation to,
                              Date start_time,
                              Date end_time)
        {
            this.from = from;
            this.to = to;
            this.start_time = start_time;
            this.end_time = end_time;
        }

        public TransportTuple(Date start_time, Date end_time)
        {
            from = to = null;
            this.start_time = start_time;
            this.end_time = end_time;
        }

        public GeolocLocation getFrom() { return from; }
        public GeolocLocation getTo() { return to; }
        public Date getStartTime() { return start_time; }
        public Date getEndTime() { return end_time; }
    
        public void setFrom(GeolocLocation from) { this.from = from; }
        public void setTo(GeolocLocation to) { this.to = to; }
    }
    // 
    // End inner class TransportTuple
    //

    private IncrementalSubscription expandableTasks;
    private IncrementalSubscription myExpansions;
    private IncrementalSubscription orgAssets;
   
    public void setupSubscriptions()
    {
        expandableTasks = (IncrementalSubscription)subscribe(candidateTasksPred());
        myExpansions = (IncrementalSubscription)subscribe(myExpPred());
        orgAssets = (IncrementalSubscription)subscribe(orgPred());
    }

    public void execute()
    { 
        if (expandableTasks.hasChanged() ) {
            Enumeration newTasks = ((IncrementalSubscription)expandableTasks).getAddedList();
            while (newTasks.hasMoreElements()) {
                Task myTask = (Task) newTasks.nextElement();
                expand(myTask);
            }
            Enumeration changedTasks = ((IncrementalSubscription)expandableTasks).getChangedList();
            while (changedTasks.hasMoreElements()) {
                Task task = (Task)changedTasks.nextElement();
                PlanElement oldPE = task.getPlanElement();
                if (oldPE != null) {
                    publishRemove(oldPE);
                }
                expand(task);
            }
        }

        if (myExpansions.hasChanged()) {
            Enumeration changedexps = ((IncrementalSubscription)myExpansions).getChangedList();
            while (changedexps.hasMoreElements()) {
                PlanElement cpe = (PlanElement) changedexps.nextElement();
                updateAllocationResult(cpe);
            }
        }
    }

    private void expand(Task task) {
        NewWorkflow wf = theLDMF.newWorkflow();
        wf.setParentTask(task);
        wf.setIsPropagatingToSubtasks(true);
        TransportTuple tt_0 = new TransportTuple(createGeoloc("FT STEWART", "HKUZ", "GEORGIA"),
                                                 createGeoloc("FT HOOD", "HFTZ", "TEXAS"),
                                                 new Date((long)task.getPreferredValue(AspectType.START_TIME)),
                                                 new Date((long)task.getPreferredValue(AspectType.END_TIME)));
        Task subtask0 = createTransportLeg(task, wf, tt_0);
        TransportTuple tt_1 = new TransportTuple(createGeoloc("FT HOOD", "HFTZ", "TEXAS"),
                                                 createGeoloc("FT IRWIN", "HFXZ", "CALIFORNIA"),
                                                 getPrediction(AspectType.END_TIME, subtask0),
                                                 new Date((long)task.getPreferredValue(AspectType.END_TIME)));
        Task subtask1 = createTransportLeg(task, wf, tt_1);
        createTheExpansion(wf, task);
    }

    private Date getPrediction(int at, Task task)
    {
      Date date = null;
      for (Iterator iterator = orgAssets.getCollection().iterator();
           iterator.hasNext();) {
        Organization org = (Organization) iterator.next();
        if (org.isSelf()) {
          Collection stratTransRelationships = 
            org.getRelationshipSchedule().getMatchingRelationships(Constants.Role.STRATEGICTRANSPORTATIONPROVIDER);

          if (stratTransRelationships.size() != 0) {
            // we only expect one
            Relationship relationship = 
              (Relationship) stratTransRelationships.iterator().next();
            Organization transporter = 
              (Organization) org.getRelationshipSchedule().getOther(relationship);
            AllocationResult ar = 
              createEstimatedAllocationResult(task, transporter);
            date = new Date((long)ar.getValue(at));
            break;
          }
        }
      }
      return date;
    }

    private NewGeolocLocation createGeoloc(String name, String geoloc, String statename)
    {
        NewGeolocLocation gl = new GeolocLocationImpl();
        gl.setName(name);
        gl.setGeolocCode(geoloc);
        gl.setCountryStateName(statename);
        return gl;
    }

    private Task createTransportLeg(Task task, NewWorkflow wf, TransportTuple tt)
    {
        //Verb newverb = new Verb(Constants.Verb.TRANSPORT);
        NewTask subtask = theLDMF.newTask();
        Vector prepphrases = new Vector();
        //Enumeration origpp = task.getPrepositionalPhrases();
        //while (origpp.hasMoreElements()) {
        //   PrepositionalPhrase pp = (PrepositionalPhrase)origpp.nextElement();
        //    if ((pp.getPreposition().equals(Constants.Preposition.FOR)) && 
        //        (pp.getIndirectObject() instanceof Asset) ) {	
        //        prepphrases.addElement(pp);
        //    }
        // }
        NewPrepositionalPhrase newpp = theLDMF.newPrepositionalPhrase();
        newpp.setPreposition(Constants.Preposition.OFTYPE);
        AbstractAsset transasset = null;
        try {
            Asset trans_proto = theLDMF.createPrototype(Class.forName("org.cougaar.planning.ldm.asset.AbstractAsset"),
                                                        "StrategicTransportation");
            transasset = (AbstractAsset)theLDMF.createInstance(trans_proto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        newpp.setIndirectObject(transasset);
        prepphrases.addElement(newpp);
        
        NewPrepositionalPhrase frompp = theLDMF.newPrepositionalPhrase();
        frompp.setPreposition(Constants.Preposition.FROM);
        frompp.setIndirectObject(tt.getFrom());
        prepphrases.addElement(frompp);
        
        NewPrepositionalPhrase topp = theLDMF.newPrepositionalPhrase();
        topp.setPreposition(Constants.Preposition.TO);
        topp.setIndirectObject(tt.getTo());
        prepphrases.addElement(topp);
        
        subtask.setParentTask(task);
        subtask.setDirectObject(task.getDirectObject());
        subtask.setPrepositionalPhrases(prepphrases.elements());
        subtask.setVerb(task.getVerb());
        subtask.setPlan(task.getPlan());
        
        Vector prefs = new Vector();
        AspectValue startAV = new AspectValue(AspectType.START_TIME, tt.getStartTime().getTime());
        ScoringFunction startSF = ScoringFunction.createPreferredAtValue(startAV, 2);
        Preference startPref = theLDMF.newPreference(AspectType.START_TIME, startSF);
        prefs.addElement(startPref);

        AspectValue endAV = new AspectValue(AspectType.END_TIME, tt.getEndTime().getTime());
        ScoringFunction endSF = ScoringFunction.createPreferredAtValue(endAV, 2);
        Preference endPref = theLDMF.newPreference(AspectType.END_TIME, endSF);
        prefs.addElement(endPref);
        
        subtask.setPreferences(prefs.elements());
        subtask.setSource(getClusterIdentifier());
        wf.addTask(subtask);
        subtask.setWorkflow(wf);
        publishAdd(subtask);
        
        return subtask;
    }

    private void createTheExpansion(Workflow wf, Task parent) {
        PlanElement pe = theLDMF.createExpansion(theLDMF.getRealityPlan(),
                                                 parent,
                                                 wf,
                                                 null);
        Enumeration e = wf.getTasks();
        while (e.hasMoreElements()) {
            publishAdd(e.nextElement());
        }
        publishAdd(pe);
    }

    private AllocationResult createEstimatedAllocationResult(Task t, Organization org)
        {
            Predictor predictor = org.getClusterPG().getPredictor();
            //AllocationResult est_ar = null;
            if (predictor != null)
                return predictor.Predict(t, getDelegate());
            else
                return null;
        }

    private void updateAllocationResult(PlanElement cpe) {
        if (cpe.getReportedResult() != null) {
            // compare the allocationresult objects.
            // If they are not ==, re-set the estimated result.
            // For now, ignore whether their composition is equal.
            AllocationResult reportedresult = cpe.getReportedResult();
            AllocationResult estimatedresult = cpe.getEstimatedResult();
            if ( (estimatedresult == null) || (! (estimatedresult == reportedresult) ) ) {
                cpe.setEstimatedResult(reportedresult);
                // Publish the change (let superclass handle transactions)
                publishChange(cpe);
            }
        }
    }

    private static UnaryPredicate destPred() 
    {
        return new UnaryPredicate() {
                public boolean execute(Object o) {
                    Task t = (Task) o;
                    PrepositionalPhrase from_tmp = t.getPrepositionalPhrase(Constants.Preposition.FROM);
                    PrepositionalPhrase to_tmp = t.getPrepositionalPhrase(Constants.Preposition.TO);
                    if (from_tmp.getIndirectObject() instanceof GeolocLocation &&
                        to_tmp.getIndirectObject() instanceof GeolocLocation) {
                        GeolocLocation from_geo = (GeolocLocation)from_tmp.getIndirectObject();
                        GeolocLocation to_geo = (GeolocLocation)to_tmp.getIndirectObject();
                        if (from_geo.getGeolocCode().equals("HKUZ") &&
                            to_geo.getGeolocCode().equals("HFXZ")) {
                            return true;
                        }
                    }
                    return false;
                }
            };
    }

    
    private static UnaryPredicate candidateTasksPred() {
        return new UnaryPredicate() {
                private UnaryPredicate destPred = destPred();
                public boolean execute(Object o) {
                    if (o instanceof Task) {
                        Task t = (Task) o;
                        if (t.getVerb().equals(Constants.Verb.TRANSPORT)) {
                            PrepositionalPhrase pp = t.getPrepositionalPhrase(Constants.Preposition.OFTYPE);
                            if (pp != null) {
                                Object indObject = pp.getIndirectObject();
                                if (indObject instanceof Asset) {
                                    Asset asset = (Asset) indObject;
                                    String io = asset.getTypeIdentificationPG().getTypeIdentification();
                                    if (io.equals("StrategicTransportation")) {
                                        return destPred.execute(o);
                                    }
                                }
                            }
                        }
                    }
                    return false;
                }
            };
    }


    private static UnaryPredicate expTasksPred() 
    {
        return new UnaryPredicate() {
                private UnaryPredicate candidateTasksPred = candidateTasksPred();
                public boolean execute(Object o) {
                    if (candidateTasksPred.execute(o)) {
                        Task t = (Task) o;
                        if (t.getPlanElement() == null) {
                            return true;
                        }
                    }
                    return false;
                }
            };
    }
    
    private static UnaryPredicate myExpPred() {
        return new UnaryPredicate() {
                private UnaryPredicate myTaskPred = candidateTasksPred();
                public boolean execute(Object o) {
                    if (o instanceof Expansion) {
                        return myTaskPred.execute(((Expansion) o).getTask());
                    } 
                    return false;
                }
            };
    }

    private static UnaryPredicate orgPred() {
      return new UnaryPredicate() {
        public boolean execute(Object o) {
          if (o instanceof Organization) {
            return true;
          } else {
            return false;
          }
        }
      };
    }
}

