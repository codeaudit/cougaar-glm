// Copyright (11/99) Honeywell Inc.
// Unpublished - All rights reserved. This software was developed with funding 
// under U.S. government contract MDeryA972-97-C-0800

/* This contains the GenericPlugin class definition. */

package org.cougaar.domain.glm.packer;

import java.lang.reflect.Constructor;

import java.util.*;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.AllocationResultAggregator;
import org.cougaar.domain.planning.ldm.plan.AllocationResultDistributor;
import org.cougaar.domain.planning.ldm.plan.Aggregation;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.MPTask;
import org.cougaar.domain.planning.ldm.plan.NewComposition;
import org.cougaar.domain.planning.ldm.plan.NewMPTask;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.NewWorkflow;
import org.cougaar.domain.planning.ldm.plan.Plan;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;

import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.Workflow;

import org.cougaar.core.plugin.PlugInDelegate;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.util.ExpanderHelper;

import org.cougaar.core.society.UID;

import org.cougaar.util.StateModelException;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.asset.ALPAsset;
import org.cougaar.domain.glm.ldm.asset.Organization;

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
abstract public class GenericPlugin extends SimplePlugIn  {
  public static final boolean DEBUG = false;

  
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
      System.err.println("HTC GenericPlugin: Class " + classname + " not found.");
      return null;
    }
    if (cls != null) {
      try {
        retval = cls.newInstance();
      } catch (Exception e) {
        System.err.println("HTC GenericPlugin: Error creating instance of " + classname);
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
      System.err.println("Error publishing expansion - " + expan.getUID());
    }

    return;
  }

  /** Creates <em>and publishes</em> aggregations for all of the tasks
   * in the input argument.  Also records the UID of the aggregations
   * in the ARTable.  For robustness, catches all errors we could
   * foresee, prints error messages and returns a boolean indicating
   * success or failure.  
   * @see htcar.ARTable
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
      System.out.println("HTC PACKER JAVA: Top of create Aggregation.");
    }
    
    // Create Composition
    NewComposition comp = getFactory().newComposition();
    
    comp.setCombinedTask(childTask); 
    comp.setDistributor(distributor);
    
    if (DEBUG) {
      System.out.println("HTC PACKER JAVA: Created comp.");
    }
    
    Iterator taskIterator = parentTasks.iterator();
    while (taskIterator.hasNext()) {
      Task parentTask = (Task) taskIterator.next();
      
      if (plan == null) {
        plan = parentTask.getPlan();
        
        if (plan == null) {
          plan = getFactory().getRealityPlan();
          
          if (plan == null ) {
            System.err.println("HTC GenericPlugin: Unable to find plan for aggregation.");
            return false;
          }
        }
      }
      
      if (DEBUG) {
        System.out.println("HTC PACKER JAVA: Top of while loop.");
      }
      
      Aggregation aggregation = 
        getFactory().createAggregation(plan, parentTask, comp, null);
      
      if (DEBUG) {
        System.out.println("HTC PACKER JAVA: built the aggregation.");
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
      System.out.println("HTC PACKER JAVA: After the while loop.");
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



    /** This does the following:
      * <UL>
      * <li> gets all the tasks
      * <li> filters them on the basis of taskPred
      * <li> Use createAggregation to collect Tasks into Containers
      * <li> do the preference aggregation dance
      * </UL>
      */
  public boolean doAggregating(AggregationClosure ac, UnaryPredicate taskPred,
			       String PrefAggName, String AllResDestribName){

    AllocationResultDistributor ard;
    PreferenceAggregator prefagg;

    try {
      ard = (AllocationResultDistributor)make_instance(AllResDestribName);
      prefagg = (PreferenceAggregator)make_instance(PrefAggName);
    } catch (java.lang.ClassCastException cce) {
      System.err.println("HTC GenericPlugin:  While packing, got error attempting to instantiate a class argument.");
      System.err.println("HTC GenericPlugin:  Error was:");
      System.err.println(cce);
      return false;
    }

    // get the list of matching tasks
    Collection tasks = tasksForPred(taskPred);

    Plan plan = null;
    if (tasks.size() > 0) {
      Iterator iterator = tasks.iterator();
      plan = ((Task)iterator.next()).getPlan();
    }
    if (plan == null) {
      plan = getFactory().getRealityPlan();
    }

    NewMPTask newtask = ac.newTask();
    try {
      ArrayList prefs = prefagg.aggregatePreferences(tasks.iterator(),
                                                     getFactory());
      // BOZO
      newtask.setPreferences(new Vector(prefagg.aggregatePreferences(tasks.iterator(),
                                                                     getFactory())).elements());

    } catch (java.lang.Exception e) {
      System.err.println("HTC GenericPlugin:  While aggregating preferences for:");
      System.err.println("HTC GenericPlugin:  " + tasks);
      System.err.println("HTC GenericPlugin:  Got the following error/exception:");
      System.err.println(e);
      return false;
    }			     
    boolean ret = createAggregation(tasks.iterator(), newtask, plan, ard);
    return ret;
  }

  /** Called from cycle when something has changed or the plugin was triggered. 
    */
  protected void execute() {
    // now we handle updates to our plan elements
    if (_myPlanElements != null) {
      ExpanderHelper.updateAllocationResult(_myPlanElements);
    } else if (DEBUG) {
      System.out.println("HTC GenericPlugin: _myPlanElements subscription is missing!");
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
      System.out.println("HTC GenericPlugin: setting up subscriptions for Packer.");


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



  /**
    * This is a simple utility function that helps us avoid a lot of code
    * repetition.  Invokes the nullary constructor for the class named by
    * the argument and catches exceptions
    * @return null if the class cannot be instantiated for one reason or
    * other, and returns the instance, otherwise.
    */
  public static Object make_instance(String className)  {
    Object ret = null;
    Constructor method;
    try {
      Class targetClass = Class.forName(className);
      // we always want the nullary constructor
      Class[] args = new Class[]{};
      method = targetClass.getConstructor(args);
      ret = method.newInstance(args);
    } catch (java.lang.ClassNotFoundException e) {
      System.err.println("HTC GenericPlugin: ClassNotFoundException for " + className);
    } catch (java.lang.NoSuchMethodException nsme) {
      System.err.println("HTC GenericPlugin: Constructor method not found for " + className);
    } catch (java.lang.Exception ie) {
      System.err.println("HTC GenericPlugin:  Failed to make an instance of " + className);
      System.err.println("HTC GenericPlugin:  Exception was:");
      System.err.println(ie);
    }
    return ret;
  }


  protected Collection tasksForPred(UnaryPredicate up) {
    return query(up);
  }

}



