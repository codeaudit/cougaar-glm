// Copyright (10/99) Honeywell Inc.
// Unpublished - All rights reserved. This software was developed with funding 
// under U.S. government contract MDA972-97-C-0800

package org.cougaar.domain.glm.packer;

import java.io.Serializable;

import java.util.Vector;
import java.util.Enumeration;

import org.cougaar.domain.planning.ldm.plan.AllocationResultDistributor;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AspectScorePoint;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.TaskScoreTable;




/**
  * This class is similar to the DefaultDistributor, but allocates quantities
  * proportionally, based on input task quantities, rather than just 
  * evenly.  Code cribbed extensively from the definition of DefaultDistributor
  * @see org.cougaar.domain.planning.ldm.plan.AllocationResultDistributor.DefaultDistributor
  */
public class ProportionalDistributor implements AllocationResultDistributor, Serializable {
  public static ProportionalDistributor DEFAULT_PROPORTIONAL_DISTRIBUTOR  =
    new ProportionalDistributor();

  public ProportionalDistributor () {}

  public TaskScoreTable calculate(Vector parents, AllocationResult ar) {
    if (GenericPlugin.DEBUG) {
      System.out.println("HTC ProportionalDistributor: start running...");
    }

    int l = parents.size();

    if (l == 0 || ar == null) return null;

    if (!ar.isDefined(AspectType.QUANTITY)) {
      // if there's no quantity in the Allocation result, then we
      // can just use the Default Distributor
      if (GenericPlugin.DEBUG) {
        System.out.println("HTC ProportionalDistributor: done running...");
      }

      return AllocationResultDistributor.DEFAULT.calculate(parents, ar);
    } else {
      double quantAchieved = ar.getValue(AspectType.QUANTITY);
      AllocationResult results[] = new AllocationResult[l];
      double quantProportions[] = new double[l];
      // the following block serves to set the quantProportions array
      {
	// these variables are lexically scoped here --- just used 
	// to compute proportional share...
      	double totalRequestedQuant = 0.0;
	double quantsRequested[] = new double[l];
	for (int i = 0; i < l; i++) {
	  double thisQuant = getTaskQuantity(((Task)parents.get(i)));
	  if (thisQuant == -1.0) {
	    // no quantity was requested, set to zero
	    System.err.println("HTC ProportionalDistributor: attempting to allocate a proportional share of quantity to a Task which requests no quantity.");
	    thisQuant = 0.0;
	  }
	  totalRequestedQuant += thisQuant;
	  quantsRequested[i] = thisQuant;
	}
	if (totalRequestedQuant == 0.0) {
	  // make sure we catch the boundary condition!
	  for (int i = 0; i < l; i++) {
	    quantProportions[i] = 0.0;
	  }
	} else {
	  for (int i = 0; i < l; i++) {
	    quantProportions[i] = quantsRequested[i]/totalRequestedQuant;
	  }
	}
      }

      // build a result for each parent task
      for (int i = 0; i < l; i++) {

	// create a value vector and fill in the values for the
	// defined aspects ONLY.
	int[] types = ar.getAspectTypes();
	double acc[] = new double[types.length];
	for (int x = 0; x < types.length; x++) {
	  // if the aspect is COST divide evenly across parents
	  if (types[x] == AspectType.COST) {
	    acc[x] = ar.getValue(types[x]) / l;
	  } else if (types[x] == AspectType.QUANTITY) {
	    // if the aspect is QUANTITY, we'll have to divide
	    // proportionally across parents
	    acc[x] = ar.getValue(types[x]) * quantProportions[i];
	  } else {
	    acc[x] = ar.getValue(types[x]);
	  }
	}
	
	results[i] = new AllocationResult(ar.getConfidenceRating(),
					  ar.isSuccess(),
					  types,
					  acc);
	// fill in the auxiliaryquery info
	// each of the new allocationresults(for the parents)
	// will have the SAME
	// auxiliaryquery info that the allocationresult (of the child) has.  
	for (int aq = 0; aq < AuxiliaryQueryType.AQTYPE_COUNT; aq++) {
	  String info = ar.auxiliaryQuery(aq);
	  if (info != null) {
	    results[i].addAuxiliaryQueryInfo(aq, info);
	  }
	}
      }

      Task tasks[] = new Task[l];
      parents.copyInto(tasks);

      if (GenericPlugin.DEBUG) {
        System.out.println("HTC ProportionalDistributor: done running...");
      }
      return new TaskScoreTable(tasks, results);
    }
  }

  // the following cribbed from LCG/CGI
  public double getTaskQuantity (Task task) {
    return getTaskAspectValue(task,AspectType.QUANTITY);
  } /* end method getTaskQuantity */

  /**
    * @return value of the preference on the aspect of the task, or
    * -1.0 if not defined.
    * @param at The aspect type.
    */
  protected static double getTaskAspectValue(Task task, int at) {
    //
    // Grab the designated aspect value.
    //        
    double value = -1.0d;
    for (Enumeration preferences = task.getPreferences();
	  preferences.hasMoreElements();) {
      Preference preference = (Preference) preferences.nextElement() ;
      if (preference.getAspectType() == at) {
	ScoringFunction sf = preference.getScoringFunction();
	AspectScorePoint asp = sf.getBest();
	AspectValue av = asp.getAspectValue();
	value = av.getValue();
      }
    }
    return (value) ;
  }
  
}





