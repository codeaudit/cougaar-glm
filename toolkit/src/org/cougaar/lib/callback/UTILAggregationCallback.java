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

package org.cougaar.lib.callback;

import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.Composition;
import org.cougaar.planning.ldm.plan.Task;

import org.cougaar.util.UnaryPredicate;
 
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.cougaar.util.log.Logger;

/**
 * Filters for aggregations...
 */

public class UTILAggregationCallback extends UTILFilterCallbackAdapter {
  public UTILAggregationCallback (UTILAggregationListener listener, Logger logger) {
    super (listener, logger);
  }

  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Aggregation) {
	  UTILAggregationListener aggListener = (UTILAggregationListener)
	    myListener;
	  Composition comp = ((Aggregation)o).getComposition();
	  List parents = comp.getParentTasks();
	  for (int i = 0; i < parents.size (); i++)
	    if (aggListener.interestingParentTask ((Task) parents.get (i)))
	      return true;

	  if (parents.isEmpty ()) // a failed aggregation has no parents
	    return true;
	}
	return false;
      }
    };
  }

  public void reactToChangedFilter () {
    Map seenAggsMap = new HashMap ();
    Enumeration changedaggs = mySub.getChangedList();

    while (changedaggs.hasMoreElements()) {
      Aggregation agg = (Aggregation) changedaggs.nextElement();
      if (seenAggsMap.get (agg) == null) {
	reactToChangedAgg (agg);
	seenAggsMap.put (agg, agg);
      } else if (logger.isDebugEnabled()) 
	logger.debug ("UTILAggregationCallback : " + 
		      "Duplicate changed aggregation for task " + 
		      agg.getTask ().getUID () + " ignored.");
    }

    seenAggsMap = new HashMap ();
    Enumeration removedAggs;
    if ((removedAggs = mySub.getRemovedList()).hasMoreElements ()) {
      UTILAggregationListener listener = (UTILAggregationListener) myListener;
      while (removedAggs.hasMoreElements()) {
	Aggregation agg = (Aggregation) removedAggs.nextElement();
	if (logger.isDebugEnabled()) 
	  logger.debug ("UTILAggregationCallback " + this + 
			" found removed agg " + agg.getUID()); 
	if (seenAggsMap.get (agg) == null) {
	  if (logger.isDebugEnabled()) 
	    logger.debug ("UTILAggregationCallback " + this + 
			  " telling plugin of agg " + agg.getUID()); 
	  listener.handleRemovedAggregation(agg);
	  seenAggsMap.put (agg, agg);
	} else if (logger.isDebugEnabled()) 
	  logger.debug ("UTILAggregationCallback : " + 
			"Duplicate removed agg for task " + 
			agg.getTask ().getUID () + " ignored.");
      }
    }
  }

  /**
   * Defines protocol for dealing with Aggregations as they change over time.
   *
   * Aggregator plugins have two shots at dealing with failed aggregations:
   *  1) immediately, as they happen -- hook here is handleRescindedAggregation
   *     Here the plugin can realloc immediately upon failure. 
   *  2) the next cycle -- when the aggregation is removed, the task for that
   *     aggregation is now as it was initially, and so ready to allocate.
   *     Perhaps now the plugin can group it with other tasks and allocate
   *     differently.  
   *        NOTE : This option can result in infinite loops :
   *        if the plugin doesn't do anything different the second time, the 
   *        aggregation will fail again, be rescinded, and we're back where we 
   *        started.
   *
   * @param Aggregation to examine
   */
  protected void reactToChangedAgg (Aggregation agg) {
    UTILAggregationListener listener = (UTILAggregationListener) myListener;
    if (listener.needToRescind(agg)){
      listener.publishRemovalOfAggregation(agg);
      // need to call this instead of allocate(Task) because the cluster
      // needs to know that the current resource is rejected.
      listener.handleRescindedAggregation(agg);
    }
    else {
      listener.reportChangedAggregation(agg);
      listener.handleSuccessfulAggregation(agg);
    }
  }

}
        
        
                
                        
                
        
        
