// Copyright (11/99) Honeywell Inc.
// Unpublished - All rights reserved. This software was developed with funding 
// under U.S. government contract MDeryA972-97-C-0800

/* This contains the GenericPlugin class definition. */

package org.cougaar.glm.packer;

import java.lang.reflect.Constructor;

import java.util.*;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.core.domain.RootFactory;

import org.cougaar.planning.ldm.asset.Asset;

import org.cougaar.planning.ldm.plan.AllocationResultAggregator;
import org.cougaar.planning.ldm.plan.AllocationResultDistributor;
import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.MPTask;
import org.cougaar.planning.ldm.plan.NewComposition;
import org.cougaar.planning.ldm.plan.NewMPTask;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;

import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.core.plugin.util.PluginHelper;

import org.cougaar.util.UnaryPredicate;

/** Main class for the generic plugin.
  *    real work is done by processAddTask, processChangeTask, processRemoveTasks
  *    these methods called at the end of execute().
  *
  * The Generic Plugin should have three subscriptions:
  * <ol>
  * <li> To all the PlanElements it has created.  Changes to these
  *     should be handled by updating their AllocationResults
  * <li>  To tasks based on the predicate defined by getTaskPredicate.  
  * <li>  A subscription to all its assets, used to find itself.
  * </ol>
  */
abstract public class GenericPlugin extends SimplePlugin  {
  public static final boolean DEBUG = false;
  public static final boolean debugExecute = true;

  
  public boolean publishAddTest(Object o) {
    return super.publishAdd(o);
  }

  /**
    * This new Preposition is used to tag tasks that are created through
    * Packing and that should not trigger subscriptions.
    */    
  public static String INTERNAL = "INTERNAL";

  // The subscriptions for this plugin...
  private IncrementalSubscription _myPlanElements;

  private IncrementalSubscription _allTasks;

  /**
    * Predicate that finds all Tasks, <em>except</em> those explicitly
    * tagged as INTERNAL.  (These are created by the Sizer.)
    * @see Sizer
    */
  private UnaryPredicate allTaskPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Task) {
	if (((Task)o).getPrepositionalPhrase(INTERNAL) == null) {
	  return true;
	} else {
	  return false;
	}
      }
      return false;
    }
  };
  

  /**
   * Utility method
   */
  private Object classForString(String classname) {
    Class cls = null;
    Object retval = null;
    try {
      cls = Class.forName(classname);
    } catch (Exception e) {
      System.err.println("GenericPlugin: Class " + classname + " not found.");
      return null;
    }
    if (cls != null) {
      try {
        retval = cls.newInstance();
      } catch (Exception e) {
        System.err.println("GenericPlugin: Error creating instance of " + classname);
        return null;
      }
    }
    return retval;
  }
  /**
   *
   *
   */
  public void createExpansion(Iterator tasksForWorkflow, Task parentTask) {
    // this needs to publish the Expansion, all the subtasks and
    // the workflow
    if (parentTask.getPlan() == null) {
      throw (new RuntimeException("Unable to find plan for expansion - parentTask is " + parentTask.getUID()));
    }

    NewWorkflow workflow = getFactory().newWorkflow();
    workflow.setIsPropagatingToSubtasks(true);
    workflow.setParentTask(parentTask);

    workflow.setAllocationResultAggregator(AllocationResultAggregator.DEFAULT);
    
    while(tasksForWorkflow.hasNext()) {
      NewTask subTask = (NewTask)tasksForWorkflow.next();
      subTask.setWorkflow(workflow);
      workflow.addTask(subTask);

      if (!publishAdd(subTask)) {
	System.err.println("Error publishing task - " + subTask.getUID());
      }
    }

    if (!publishAdd(workflow)) {
      System.err.println("Error publishing workflow - " + workflow);
    }

    
    Expansion expan = getFactory().createExpansion(parentTask.getPlan(), 
                                                   parentTask, workflow, 
                                                   null);
    if (!publishAdd(expan)) {
      System.err.println("Error publishing expansion - " + expan.getUID() + ".\n" +
                         "Task: " + expan.getTask().getUID() + 
                         " task commit date: " + expan.getTask().getCommitmentDate() +
                         " task plan element: " + expan.getTask().getPlanElement());
    }

    return;
  }

  /** Creates <em>and publishes</em> aggregations for all of the tasks
   * in the input argument.  For robustness, catches all errors we could
   * foresee, prints error messages and returns a boolean indicating
   * success or failure.  
   */
  public boolean createAggregation(Iterator tasksToAgg, MPTask childTask, Plan plan,
                                   AllocationResultDistributor distributor) {
    // Save into a vector because we want to use the enumeration in multiple
    // places
    ArrayList parentTasks = new ArrayList();
    while (tasksToAgg.hasNext()) {
      parentTasks.add((Task)tasksToAgg.next());
    }
    
    if (DEBUG) {
      System.out.println("GenericPlugin: Top of create Aggregation.");
    }
    
    // Create Composition
    NewComposition comp = getFactory().newComposition();
    
    comp.setCombinedTask(childTask); 
    comp.setDistributor(distributor);
    
    if (DEBUG) {
      System.out.println("GenericPlugin: Created comp.");
    }
    
    Iterator taskIterator = parentTasks.iterator();
    while (taskIterator.hasNext()) {
      Task parentTask = (Task) taskIterator.next();
      
      if (plan == null) {
        plan = parentTask.getPlan();
        
        if (plan == null) {
          plan = getFactory().getRealityPlan();
          
          if (plan == null ) {
            System.err.println("GenericPlugin: Unable to find plan for aggregation.");
            return false;
          }
        }
      }
      
      if (DEBUG) {
        System.out.println("GenericPlugin: Top of while loop.");
      }
      
      Aggregation aggregation = 
        getFactory().createAggregation(plan, parentTask, comp, null);
      
      if (DEBUG) {
        System.out.println("GenericPlugin: built the aggregation.");
      }
      
      if (!publishAdd(aggregation)) {
        System.err.println("Error publishing aggregation - " + 
                           aggregation.getUID());
      }
      
      /* BOZO - Work around for persistence bug - need to publish the 
       * aggregation before adding it to the composition.  Should not be 
       * required since this all occurs inside of a single transaction.
       */
      comp.addAggregation(aggregation);
      
    }

    if (DEBUG) {
      System.out.println("GenericPlugin: After the while loop.");
    }
    
    if (!publishAdd(comp)) {
      System.err.println("Error publishing composition " + 
                         comp);
    }
    
    NewMPTask mpt = (NewMPTask)childTask;
    
    mpt.setPlan(plan);
    mpt.setComposition(comp);
    //BOZO !!!
    mpt.setParentTasks(new Vector(parentTasks).elements());
    
    if (!publishAdd(childTask)) {
      System.err.println("Error publishing mpt - " + mpt.getUID());
    }
    
    return true;
  }



  /** Called from cycle when something has changed or the plugin was triggered. 
    */
  protected void execute() {
    // now we handle updates to our plan elements
    if (_myPlanElements != null) {
      updateAllocationResult(_myPlanElements);
    } else if (DEBUG) {
      System.out.println("GenericPlugin: _myPlanElements subscription is missing!");
    }

    processNewTasks( _allTasks.getAddedList());

    processChangedTasks( _allTasks.getChangedList());

    processRemovedTasks( _allTasks.getRemovedList());
  }

  /**
   * processNewTasks - called during execute to handle new tasks.  
   * plugins should implement as appropriate.
   *
   * @param newTasks Enumeration of new tasks
   */
  public abstract void processNewTasks(Enumeration newTasks);

  /**
   * processChangedTasks - called during execute to handle changed tasks. 
   * plugins should implement as appropriate.
   *
   * @param changedTasks Enumeration of changed tasks
   */
  public abstract void processChangedTasks(Enumeration changedTasks);

  /**
   * processRemovedTasks - called during execute to handle removed tasks. 
   * plugins should implement as appropriate.
   *
   * @param removedTasks Enumeration of removed tasks
   */
  public abstract void processRemovedTasks(Enumeration removedTasks);
    
  /**
   * getTaskPredicate - returns task predicate for task subscription
   * Default implementation subscribes to all non-internal tasks. Derived classes
   * should probably implement a more specific version.
   * 
   * @return UnaryPredicate - task predicate to be used.
   */
  public UnaryPredicate getTaskPredicate() {
    return allTaskPred;
  }

  /**
   * getPlanElementPredicate - returns predicate which screens for plan 
   * elements which will need to have allocation results set. In this case, 
   * plan elements associated with Ammunition Supply tasks
   * 
   * 
   * @return UnaryPredicate screens for plan elements which the packer is 
   * reponsible
   */
  public abstract UnaryPredicate getPlanElementPredicate();


  /**
   * setupSubscriptions - set up subscriptions to assets, tasks, and planElements.
   * Uses predicates returned by getTaskPredicate() & getPlanElementPredicate
   */
  protected void setupSubscriptions() {
    if (DEBUG) {
      System.out.println("GenericPlugin: setting up subscriptions for Packer.");


      System.out.println("GenericPlugin.setupSubscriptions: didRehydrate() " + 
                         didRehydrate() +
                         " _allTasks " + _allTasks);
    }

    _allTasks = (IncrementalSubscription)subscribe(getTaskPredicate());
    
    _myPlanElements = (IncrementalSubscription)subscribe(getPlanElementPredicate());
    
  }


  /**
   * Creates an generic plugin  
   */
  public GenericPlugin() {
  }


   /** Function that tests if the first argument is of a given class
    */
  public boolean testInstance(Object arg, String ty) {
      boolean test = true;
      try {
	test = (Class.forName(ty)).isInstance(arg);
      }
      catch (ClassNotFoundException e) {
	System.err.println(e.getMessage());
	test = false;
      }
      finally {
	return test;
      }
  }

  /**
    * Provides access to a protected method.
    */
  public RootFactory getGPFactory() {
    return getFactory();
  }

  /**
    * Provides access to a protected method.
    */
  public ClusterIdentifier getGPClusterIdentifier() {
    return getClusterIdentifier();
  }

  protected Collection tasksForPred(UnaryPredicate up) {
    return query(up);
  }

  protected void updateAllocationResult(IncrementalSubscription planElements) {
    PluginHelper.updateAllocationResult(planElements);
  }

}



