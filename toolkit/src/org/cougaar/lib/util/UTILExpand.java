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

package org.cougaar.lib.util;

import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AllocationResultAggregator;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.MPTask;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.NewWorkflow;
import org.cougaar.domain.planning.ldm.plan.Plan;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.Workflow;

import org.cougaar.core.plugin.PlugInDelegate;

import org.cougaar.core.society.UID;

import org.cougaar.lib.filter.UTILPlugIn;
import org.cougaar.lib.param.ParamTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cougaar.core.blackboard.BlackboardService;

/** 
 * This class contains utility functions related to
 * subtasks and expansions.
 */

public class UTILExpand {
  private static boolean shouldPropagateRescinds = true;
  public static void setPropagateRescinds (boolean p) {
    shouldPropagateRescinds = p;
  }

  /**
   * Create a subtask.
   *
   * For when you want to be able to set all the fields of the
   * new task.
   *
   * If you just want to set a new direct object for the task,
   * see the other makeSubTask.
   *
   * @param ldmf the RootFactory
   * @param plan the log plan
   * @param parent the parent task
   * @param verb the verb
   * @param prepphrases an enum of PrepositionalPhrases
   * @param obj the direct object
   * @param preferences for sub task
   * @param priority of task
   * @param source the cluster originating the task
   * @return NewTask
   */
  public static NewTask makeSubTask(RootFactory ldmf,
				    Plan plan, 
				    Task parent, 
				    Verb verb, 
				    Enumeration prepphrases, 
				    Asset obj, 
				    Enumeration preferences,
				    byte priority,
				    ClusterIdentifier source) {
    return makeSubTask(ldmf, plan, parent.getUID(), verb, prepphrases, 
		       obj, preferences, priority, source);
  }

  /**
   * Create a subtask.
   *
   * For when you want to be able to set all the fields of the
   * new task.
   *
   * If you just want to set a new direct object for the task,
   * see the other makeSubTask.
   *
   * @param ldmf the LdmFactory
   * @param plan the log plan
   * @param parent_uid the parent task UID
   * @param verb the verb
   * @param prepphrases an enum of PrepositionalPhrases
   * @param obj the direct object
   * @param preferences for sub task
   * @param priority of task
   * @param source the cluster originating the task
   * @return NewTask
   */
  public static NewTask makeSubTask(RootFactory ldmf,
				    Plan plan, 
				    UID parent_uid, 
				    Verb verb, 
				    Enumeration prepphrases, 
				    Asset obj, 
				    Enumeration preferences,
				    byte priority,
				    ClusterIdentifier source) {
    NewTask task = ldmf.newTask();

    if(parent_uid != null)
      task.setParentTaskUID(parent_uid);
    task.setDirectObject(obj);
    task.setPrepositionalPhrases(prepphrases);
    task.setVerb(verb);
    task.setPlan(plan);
    task.setPreferences(preferences);
    task.setPriority(priority);
    task.setSource(source);
    return task;
  }
  
  /**
   * Checks the parentTask for AuxiliaryQuery.  If a query exists,
   * then propagate the query to the subtask. Since not all subtasks
   * need to propagate the query, this is not automatically part of 
   * the makeSubTask.  Only call this when needed.
   *
   * @param parentTask 
   * @param subTask 
   */

  public static void addAuxiliaryQuery (Task parentTask, NewTask subTask) {
    if (parentTask.getAuxiliaryQueryTypes()[0] != -1) {
      subTask.setAuxiliaryQueryTypes(parentTask.getAuxiliaryQueryTypes());
    }
  }

  /**
   * Clone a task.
   *
   * Used in handling rescinds.
   *
   * @param ldmf the RootFactory
   * @param taskToClone the task to be cloned
   * @return cloned copy of the original task
   * @see org.cougaar.lib.plugin.UTILAllocatorPlugInAdapter
   */
  public static NewTask cloneTask(RootFactory ldmf,
				  Task taskToClone) {
    // Warning, this assumes that the task is part of a workflow!

    Workflow flow = taskToClone.getWorkflow();
    NewTask t =  makeSubTask (ldmf,
			      taskToClone.getPlan(),

			      flow.getParentTask (),
			      taskToClone.getVerb(),
			      taskToClone.getPrepositionalPhrases(),
			      taskToClone.getDirectObject (),
			      taskToClone.getPreferences(),
			      taskToClone.getPriority(),
			      taskToClone.getSource ());
    addAuxiliaryQuery(taskToClone, t);
    return t;
  }

  /**
   * Create a subtask, using fields from parent task
   * For when you just want a copy of the parent task with
   * a different direct object.
   *
   * @param ldmf the RootFactory
   * @param parent the parent task
   * @param obj the direct object
   * @param source the cluster originating the task
   * @return NewTask
   */
  public static NewTask makeSubTask(RootFactory ldmf,
				    Task parent, 
				    Asset obj, 
				    ClusterIdentifier source) {
    NewTask task = ldmf.newTask();

    if(parent != null)
      task.setParentTaskUID(parent.getUID());
    task.setDirectObject(obj);
    task.setPrepositionalPhrases(parent.getPrepositionalPhrases());
    task.setVerb(parent.getVerb ());
    task.setPlan(parent.getPlan ());
    task.setPreferences(parent.getPreferences ());
    task.setPriority(parent.getPriority ());
    task.setSource(source);
    return task;
  }


  /**
   * Create a new Workflow from the subtask(s)

   * @param ldmf the RootFactory
   * @param subtasks a vector of subtasks
   * @return Workflow a workflow containing the subtasks
   */
  /*  public static Workflow makeWorkflow(LdmFactory ldmf, List subtasks) {
    return makeWorkflow (ldmf, subtasks, myARA);//AllocationResultAggregator.DEFAULT);
    }*/

  /**
   * Create a new Workflow from the subtask(s)
   * @param ldmf the LdmFactory
   * @param subtasks a vector of subtasks
   * @param parent task of this workflow expansion
   * @return Workflow a workflow containing the subtasks
   */
  public static Workflow makeWorkflow(RootFactory ldmf, List subtasks,
				      Task parent) {
    return makeWorkflow (ldmf, subtasks, myARA,//AllocationResultAggregator.DEFAULT,
			 parent);
  }

  /**
   * Create a new Workflow from the subtask(s)
   * @param ldmf the RootFactory
   * @param subtasks a vector of subtasks
   * @param ara subclass of AllocationResultAggregator, which allows clients to
   *        redefine how the allocation results of subtasks are rolled up
   *        into an aggregate result.  
   * @param parent task of this workflow expansion
   * @return Workflow a workflow containing the subtasks
   * @see org.cougaar.domain.planning.ldm.Plan.AllocationResultAggregator
   */
  public static Workflow makeWorkflow(RootFactory ldmf, List subtasks, 
				      AllocationResultAggregator ara,
				      Task parent) {	
    NewWorkflow wf = ldmf.newWorkflow();

    if (shouldPropagateRescinds)
      wf.setIsPropagatingToSubtasks (true);

    wf.setParentTask(parent);

    for (Iterator iter = subtasks.iterator (); iter.hasNext ();)
      ((NewTask)iter.next()).setWorkflow(wf);

    wf.setTasks(Collections.enumeration(subtasks));

    wf.setAllocationResultAggregator (ara);

    return wf;
  }

  public static void showExpansion(Expansion exp) {
    System.out.println ("--------------- exp " + exp.getUID () + " ----------------");
    Workflow wf = exp.getWorkflow ();
    int i = 0;
    for (Enumeration enum = wf.getTasks (); enum.hasMoreElements (); ) {
	showPlanElement((Task) enum.nextElement (), i++);
    }
  }

    public static void showPlanElement (Task subTask) {
	showPlanElement (subTask, -1);
    }

    protected static void showPlanElement (Task subTask, int taskNum) {
	PlanElement pe = subTask.getPlanElement ();
	String extra = "";
	if (UTILPrepPhrase.hasPrepNamed (subTask, "TASKEDTO")) 
	    extra = " TASKEDTO " + UTILPrepPhrase.getIndirectObject (subTask, "TASKEDTO"); 

// 	System.out.println ("\t" + ((taskNum != -1) ? "#" + taskNum : "") + 
// 			    " FROM " + UTILPrepPhrase.getFromLocation (subTask) + 
// 			    " TO "    + UTILPrepPhrase.getToLocation   (subTask) + extra);
	if (pe == null) {
	    System.out.println ("\t no PE yet.");
	    showPreferences (subTask);
	}
	else {
	    if (pe.getEstimatedResult () == null)
		System.out.println ("\t" + " NULL EST AR");
	    else
		showPreferences (subTask, 
				 pe.getEstimatedResult().getAspectTypes (),
				 pe.getEstimatedResult().getResult ());
	}
    }

    public static void showPreferences (Task t,
					int [] aspectTypes,
					double [] aspectValues) {
	boolean prefExceeded = false;
	Enumeration prefs = t.getPreferences ();
	Map map = new HashMap ();
	int aspectType = -1;

	for (; prefs.hasMoreElements (); ) {
	    Preference pref = (Preference) prefs.nextElement ();
	    map.put (new Integer (pref.getAspectType ()), pref);
	}

	//    String prefString = "";

	for (int i = 0; i < aspectTypes.length; i++) {
	    aspectType = aspectTypes[i];
	    Integer aspectTypeInt = new Integer (aspectType);
	    Preference pref = (Preference) map.remove (aspectTypeInt);
	    if (pref != null) {
		AspectValue av = new AspectValue (aspectType, aspectValues[i]);
		if (aspectType == AspectType.END_TIME)
		    System.out.println (printEndTime (t, av));
		else
		    System.out.println (print (av, pref));
	    }
	}
    
	//    System.out.println (prefString);
    }

    public static void showPreferences (Task t) {
	boolean prefExceeded = false;
	Enumeration prefs = t.getPreferences ();
	Map map = new HashMap ();
	int aspectType = -1;

	for (; prefs.hasMoreElements (); ) {
	    Preference pref = (Preference) prefs.nextElement ();
	    if (pref != null) {
		if (pref.getAspectType () == AspectType.END_TIME)
		    System.out.println (printEndTime (t));
		else
		    System.out.println (print (pref));
	    }
	}
    
	//    System.out.println (prefString);
    }

  protected static String print (AspectValue av, Preference pref) {
    String type = null;
    String value = null;
    String prefstr = null;
    String intermediate = "";

    try {
      double prefval = pref.getScoringFunction().getBest ().getValue ();
      switch (av.getAspectType ()) {
      case AspectType.START_TIME: 
	type = "START";
	value   = "" + new Date ((long) av.getValue ());
	prefstr = "" + new Date ((long) prefval);
	intermediate = (av.getValue () < prefval) ? " < " : "";
	break;
      case AspectType.END_TIME: 
	type = "END  ";
	value   = "" + new Date ((long) av.getValue ());
	prefstr = "" + new Date ((long) prefval);
	break;
      default:
	type = "<" + av.getAspectType () + ">";
	value   = "" + av.getValue ();
	prefstr = "" + prefval;
	break;
      }
    } catch (Exception e) {
      return "Excep";
    }


    return (type + ":p " + prefstr + intermediate + " -a " + value);
  }

  protected static String print (Preference pref) {
    String type = null;
    String value = null;
    String prefstr = null;
    try {
      double prefval = pref.getScoringFunction().getBest ().getValue ();
      switch (pref.getAspectType ()) {
      case AspectType.START_TIME: 
	type = "START";
	prefstr = "" + new Date ((long) prefval);
	break;
      case AspectType.END_TIME: 
	type = "END  ";
	prefstr = "" + new Date ((long) prefval);
	break;
      default:
	type = "<" + pref.getAspectType () + ">";
	prefstr = "" + prefval;
	break;
      }
    } catch (Exception e) {
      return "Excep";
    }

    return (type + ":p " + prefstr);
  }

  protected static String printEndTime (Task t, AspectValue av) {
    String type = null;
    String value = null;
    //    String prefstr = null;

    //    double prefval = pref.getScoringFunction().getBest ().getValue ();
    type = "END  ";
    Date avDate = new Date ((long) av.getValue ());
    value   = "" + avDate;
    //    prefstr = "" + new Date ((long) prefval);
    Date early = UTILPreference.getEarlyDate(t);
    Date best  = UTILPreference.getBestDate(t);
    Date late  = UTILPreference.getLateDate(t);
    String earlyMark = (avDate.before (early)) ? " <" : "";
    String lateMark  = (avDate.after  (late )) ? " >" : "";

    return 
	(type + ":p-e " + early + earlyMark + " a " + value) + "\n" + 
	(type + ":p-b " + best  + " a " + value) + "\n" + 
	(type + ":p-l " + late  + lateMark + " a " + value);
  }

  protected static String printEndTime (Task t) {
    String type = null;
    String value = null;
    //    String prefstr = null;

    //    double prefval = pref.getScoringFunction().getBest ().getValue ();
    type = "END  ";

    //    prefstr = "" + new Date ((long) prefval);
    Date early = UTILPreference.getEarlyDate(t);
    Date best  = UTILPreference.getBestDate(t);
    Date late  = UTILPreference.getLateDate(t);

    return 
	(type + ":p-e " + early + "\n") + 
	(type + ":p-b " + best  + "\n") + 
	(type + ":p-l " + late);
  }

  /**
   * Creates the Expansion.
   *
   * Sets the estimated allocation result of the plan element to NULL.
   *
   * @param ldmf RootFactory
   * @param wf a workflow
   * @return Expansion 
   */
  public static Expansion makeExpansion(RootFactory ldmf, Workflow wf) {
    Task t = wf.getParentTask();
    Expansion exp = ldmf.createExpansion(t.getPlan(), t, wf, null);
    return exp;
  }

  /**
   * Creates the Expansion.
   *
   * Estimated allocation result of the expansion is set to the values of the
   * best values of the task, with a confidence of MEDIUM_CONFIDENCE (0.5).
   *
   * @param ldmf RootFactory
   * @param wf a workflow
   * @return Expansion 
   * @see UTILAllocate#MEDIUM_CONFIDENCE
   */
  public static Expansion makeExpansionWithConfidence(RootFactory ldmf, Workflow wf) {
    Task t = wf.getParentTask();
    Enumeration prefs = t.getPreferences();
    List aspect_values = new ArrayList();

    while(prefs.hasMoreElements()){
      Preference p = (Preference)prefs.nextElement();
      aspect_values.add(new AspectValue(p.getAspectType(), UTILPreference.getPreferenceBestValue(p)));
    }

    AllocationResult ar = 
      ldmf.newAVAllocationResult(UTILAllocate.MEDIUM_CONFIDENCE,
				 true, (AspectValue[])aspect_values.toArray(new AspectValue[0]));

    Expansion exp = ldmf.createExpansion(t.getPlan(), t, wf, ar);
    return exp;
  }

  /**
   * Creates a failed Expansion.
   *
   * Sets the estimated allocation result of the plan element to an 
   * empty failed alloc result.
   *
   * @param ldmf RootFactory
   * @param t task to make a failed expansion for
   * @return Expansion 
   */
  public static Expansion makeFailedExpansion(UTILPlugIn creator,
					      RootFactory ldmf, Task t) {
    AllocationResult failedAR = 
      ldmf.newAllocationResult(1.0, false,
			       new int[1], new double[1]);

    Expansion exp = ldmf.createExpansion(t.getPlan(), t, ldmf.newWorkflow(), failedAR);

    if (creator != null)
      creator.showDebugIfFailure ();

    return exp;
  }

  /** 
   * This method Expands the given Task.
   * @param ldmf the LDMFactory
   * @param plugin the PlugInDelegate
   * @param pluginName string representation of the plugin's name
   * @param paramTable used for myExtraOutput
   * @param myExtraOutput used to turn on extra outputs
   * @param t the task to be expanded.
   * @param subtasks the expanded subtasks
   */

  public static void handleTask(RootFactory ldmf, PlugInDelegate plugin, String pluginName,
				boolean wantConfidence, boolean myExtraOutput,
				Task t, List subtasks) {
	handleTask (ldmf, plugin.getBlackboardService (), pluginName, wantConfidence, myExtraOutput, t, subtasks);
  }
  
  public static void handleTask(RootFactory ldmf, BlackboardService blackboard, String pluginName,
				boolean wantConfidence, boolean myExtraOutput,
				Task t, List subtasks) {
    if (subtasks.isEmpty ()) {
      throw new UTILPlugInException(pluginName+".handleTask - WARNING : getSubtasks returned empty vector!");
    }

    // WARNING: The following MPTask code is somewhat GlobalSea specific.
    // However, in general if we try to create expansions containing
    // MPTasks, we crash, so this is a slightly better solution for now.
    if (myExtraOutput){
      System.out.println(pluginName + 
			 ".handleTask: Subtask(s) created for " +
			 ((t instanceof MPTask)?"MPT":"t") + "ask :" + 
			 t.getUID());
    }

    Workflow wf = null;

    if (t instanceof MPTask) {
      // Handling for the special case in which the subtasks of 
      // an expanded MPTask are ALL also MPTasks.
      Iterator sub_t_i = subtasks.iterator();
      boolean contains_mptasks = false;
      while (sub_t_i.hasNext()) {
	if (sub_t_i.next() instanceof MPTask)
	 contains_mptasks = true;
	else if (contains_mptasks == true)
	  throw new UTILPlugInException(pluginName +
					".handleTask : ERROR: Found expansion with mixed Task and MPTask children.");
      } // while
      
      if (contains_mptasks)
	wf = makeWorkflow(ldmf, subtasks, t /*parent*/);
    } 

    // General case
    // If we haven't created a workflow yet, we want the "normal" case

    if (wf == null) {
      wf = UTILExpand.makeWorkflow (ldmf, subtasks, t);
    } 

    /*
    if (myExtraOutput){
      System.out.println(pluginName + ".handleTask: Workflow created.");
    }
    */

    Expansion exp = null;
    if(wantConfidence){
      exp = UTILExpand.makeExpansionWithConfidence (ldmf, wf);
    }
    else{
      exp = UTILExpand.makeExpansion (ldmf, wf);
    }

    if (myExtraOutput){
      System.out.println(pluginName + ".handleTask: Expansion created. (" +
			 exp.getUID() + ")");
    }
    
    for (Iterator i = subtasks.iterator (); i.hasNext ();) {
      blackboard.publishAdd (i.next());
    }
	//	plugin.publishAdd(wf); // Mike Thome says never publish the workflow
    blackboard.publishAdd(exp);

    if (myExtraOutput){
      System.out.println(pluginName + ".handleTask: Expansion published. Workflow has " + 
			 UTILAllocate.enumToList(exp.getWorkflow ().getTasks()).size () + " subtasks." );
    }

  }

  /** 
   * who uses this anymore anyway?  TOPS?
   */
  public static Expansion handleTaskPrime(RootFactory ldmf, PlugInDelegate plugin, String pluginName,
				boolean wantConfidence, boolean myExtraOutput,
										  Task t, List subtasks) {
	return handleTaskPrime (ldmf, plugin.getBlackboardService(), pluginName, wantConfidence, myExtraOutput, t, subtasks);
  }
  
  /** 
   * who uses this anymore anyway?   TOPS?
   */
  public static Expansion handleTaskPrime(RootFactory ldmf, BlackboardService blackboard, String pluginName,
				boolean wantConfidence, boolean myExtraOutput,
				Task t, List subtasks) {
    if (subtasks.isEmpty ()) {
      throw new UTILPlugInException(pluginName+".handleTask - WARNING : getSubtasks returned empty vector!");
    }

    // WARNING: The following MPTask code is somewhat GlobalSea specific.
    // However, in general if we try to create expansions containing
    // MPTasks, we crash, so this is a slightly better solution for now.
    if (myExtraOutput){
      System.out.println(pluginName + 
			 ".handleTask: Subtask(s) created for " +
			 ((t instanceof MPTask)?"MPT":"t") + "ask :" + 
			 t.getUID());
    }

    Workflow wf = null;

    if (t instanceof MPTask) {
      // Handling for the special case in which the subtasks of 
      // an expanded MPTask are ALL also MPTasks.
      Iterator sub_t_i = subtasks.iterator();
      boolean contains_mptasks = false;
      while (sub_t_i.hasNext()) {
	if (sub_t_i.next() instanceof MPTask)
	 contains_mptasks = true;
	else if (contains_mptasks == true)
	  throw new UTILPlugInException(pluginName +
					".handleTask : ERROR: Found expansion with mixed Task and MPTask children.");
      } // while
      
      if (contains_mptasks)
	wf = makeWorkflow(ldmf, subtasks, t /*parent*/);
    } 

    // General case
    // If we haven't created a workflow yet, we want the "normal" case

    if (wf == null) {
      wf = UTILExpand.makeWorkflow (ldmf, subtasks, t);
    } 

    /*
    if (myExtraOutput){
      System.out.println(pluginName + ".handleTask: Workflow created.");
    }
    */

    Expansion exp = null;
    if(wantConfidence){
      exp = UTILExpand.makeExpansionWithConfidence (ldmf, wf);
    }
    else{
      exp = UTILExpand.makeExpansion (ldmf, wf);
    }

    if (myExtraOutput){
      System.out.println(pluginName + ".handleTask: Expansion created. (" +
			 exp.getUID() + ")");
    }
    
    for (Iterator i = subtasks.iterator (); i.hasNext ();) {
      blackboard.publishAdd (i.next());
    }
    blackboard.publishAdd(exp);

    if (myExtraOutput){
      System.out.println(pluginName + ".handleTask: Expansion published. Workflow has " + 
			 UTILAllocate.enumToList(exp.getWorkflow ().getTasks()).size () + " subtasks." );
    }

    return exp;

  }

  public static Enumeration createDividedPreferences(RootFactory ldmf, Task t, Date begin, Date end) {
    Enumeration taskPrefs = t.getPreferences();
    Vector newPrefs = new Vector();
    Date start = null;
    Date early = null;
    Date best  = null;
    Date late  = null;
    while (taskPrefs.hasMoreElements()) {
      Preference thisPref = (Preference) taskPrefs.nextElement();
      if (thisPref.getAspectType() == AspectType.START_TIME) {
	start = UTILPreference.getReadyAt(t);
      } else if (thisPref.getAspectType() == AspectType.END_TIME) {
	early = UTILPreference.getEarlyDate(t);
	best = UTILPreference.getBestDate(t);
	late = UTILPreference.getLateDate(t);
      } else {
	newPrefs.addElement(thisPref);
      }
    }
    if (start == null || early == null || best == null || late == null ||
	early.before(start) || best.before(early) || late.before(best) ||
	begin.before(start) || end.after(late)) {
      throw new UTILRuntimeException ("UTILExpand.createDividedPreferences : task\n\t" + t +
				      "\n\thas bad time preferences.");
    }

    long whole_duration   = late.getTime() - start.getTime();
    long partial_duration = end.getTime()  - begin.getTime();
    double time_fragment = (double)partial_duration / (double)whole_duration;
    
    long early_to_best = (long) (((double) (best.getTime() - early.getTime())) * time_fragment);
    long best_to_late  = (long) (((double) (late.getTime() - best.getTime ())) * time_fragment);

    Date newEarly = new Date(end.getTime() - (early_to_best + best_to_late));
    Date newBest  = new Date(end.getTime() - best_to_late);

    if (debugAlot) {
      System.out.println ("createDivided - begin " + begin + 
			  " e " + newEarly + 
			  " b " + newBest + 
			  " l " + end);
      System.out.println ("createDivided - task's start " + start +
			  " e " + early + 
			  " b " + best + 
			  " l " + late);
    }

    newPrefs.addElement(UTILPreference.makeStartDatePreference(ldmf, begin));
    newPrefs.addElement(UTILPreference.makeEndDatePreference(ldmf, 
							     newEarly,
							     newBest,
							     end));
    return newPrefs.elements();

  }

  private static AllocationResultAggregator myARA = new UTILAllocationResultAggregator ();
  private static boolean debugAlot = false;
}
