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
package org.cougaar.domain.glm.packer;


import java.util.ArrayList;
import java.util.Vector;

import org.cougaar.domain.planning.ldm.plan.NewMPTask;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Plan;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.AllocationResultDistributor;

class Filler {
  private Sizer _sz;
  
  private GenericPlugin _gp;

  /**
    * This is the generator for the Multi-parent tasks that
    * are the "product" of the aggregation.
    */
  private AggregationClosure _ac;

  private PreferenceAggregator _pa;

  /**
    * The AllocationResultDistributor that should be used on
    * any Containers created by this Filler
    */
  private AllocationResultDistributor _ard;

  private static int TRANSPORT_TONS;

  Filler (Sizer sz, GenericPlugin gp, AggregationClosure ac,
	  AllocationResultDistributor ard,
	  PreferenceAggregator pa) {
    _sz = sz;
    _gp = gp;
    _ac = ac;
    _ard = ard;
    _pa = pa;
  }

  /**
    * This is the driving function in the whole packing process.
    */
  public void execute () {
    boolean finished = false;

    while (!finished) {
      // initialize the aggregation
      ArrayList agglist = new ArrayList();
      double amount = 0.0;
      while (_ac.getQuantity() - amount > 0.0) {
	Task t = _sz.provide(_ac.getQuantity() - amount);
	if (t == null ) {
	  finished = true;
	  break;
	} else {
	  // if we reach here, t is a Task that provides
	  // some amount towards our overall amount
	  double provided  = t.getPreferredValue(AspectType.QUANTITY);
	  amount += provided;
	  agglist.add(t);
	}
      }
      if (!agglist.isEmpty()) {
	// now we do the aggregation
	NewMPTask mpt = _ac.newTask();

        
        //BOZO
	mpt.setPreferences(new Vector(_pa.aggregatePreferences(agglist.iterator(),
                                                               _gp.getGPFactory())).
                           elements());
        TRANSPORT_TONS += mpt.getPreferredValue(AspectType.QUANTITY);
	Plan plan = ((Task)agglist.get(0)).getPlan();
	_gp.createAggregation(agglist.iterator(), mpt, plan, _ard);
      }
    }
    System.out.println("Packer - current aggregated requested transport: " +
                       TRANSPORT_TONS + " tons.");
  }

}


