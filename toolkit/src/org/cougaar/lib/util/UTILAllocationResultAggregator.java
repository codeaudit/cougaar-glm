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

package org.cougaar.lib.util;

import java.io.Serializable;
import java.util.Enumeration;

import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AllocationResultAggregator;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.planning.ldm.plan.Location;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.TaskScoreTable;
import org.cougaar.planning.ldm.plan.Workflow;

/** Does the right computation for workflows which are made up of
   * equally important tasks with no inter-task constraints.
   * START_TIME is minimized.
   * END_TIME is maximized.
   * DURATION is overall END_TIME - overall START_TIME.
   * COST is summed.
   * DANGER is maximized.
   * RISK is maximized.
   * QUANTITY is summed.
   * INTERVAL is summed.
   * TOTAL_QUANTITY is summed.
   * TOTAL_SHIPMENTS is summed.
   * CUSTOMER_SATISFACTION is averaged.
   * Any extended aspect types are ignored.
   * 
   * For AuxiliaryQuery information, if all the query values are the same
   * across subtasks or one subtask has query info it will be place in the 
   * aggregate result.  However, if there are conflicting query values, no
   * information will be put in the aggregated result.
   * 
   * returns null when there are no subtasks or any task has no result.
   **/
public class UTILAllocationResultAggregator implements AllocationResultAggregator, Serializable {
  // dummy location
  private static final class UndefinedLocation implements Location, Serializable {
    private Object readResolve() { return UNDEFINED_LOCATION; }
  }
  public static final Location UNDEFINED_LOCATION = new UndefinedLocation();
  public static final AspectValue UNDEFINED_POD = 
    AspectValue.newAspectValue(POD, UNDEFINED_LOCATION);


  /** These are the aspects we handle **/
  static final int[] _UTIL_ASPECTS = {
    START_TIME, END_TIME, COST, DANGER, RISK, QUANTITY, INTERVAL, TOTAL_QUANTITY, TOTAL_SHIPMENTS,
    CUSTOMER_SATISFACTION, READINESS, POD_DATE, POD};

  /** This is a map from AspectType to acc index - if only we had macros! **/
  private static final int[] AM = new int[AspectType.N_CORE_ASPECTS];
  static {
    for (int i=0; i<_UTIL_ASPECTS.length; i++) { AM[_UTIL_ASPECTS[i]]=i; }
  }

  public AllocationResult calculate(Workflow wf, TaskScoreTable tst, AllocationResult currentar) {
    AspectValue[] acc = new AspectValue[_UTIL_ASPECTS.length];
    
    acc[AM[START_TIME]] = AspectValue.newAspectValue(START_TIME, Long.MAX_VALUE);
    acc[AM[END_TIME]] =  AspectValue.newAspectValue(START_TIME, 0L);
    // duration is computed from end values of start and end
    acc[AM[COST]] =  AspectValue.newAspectValue(START_TIME, 0);
    acc[AM[DANGER]] =  AspectValue.newAspectValue(DANGER, 0);
    acc[AM[RISK]] =  AspectValue.newAspectValue(RISK, 0);
    acc[AM[QUANTITY]] =  AspectValue.newAspectValue(QUANTITY, 0);
    acc[AM[INTERVAL]] =  AspectValue.newAspectValue(INTERVAL, 0);
    acc[AM[TOTAL_QUANTITY]] = AspectValue.newAspectValue(TOTAL_QUANTITY,0.0);
    acc[AM[TOTAL_SHIPMENTS]] = AspectValue.newAspectValue(TOTAL_SHIPMENTS,0.0);
    acc[AM[CUSTOMER_SATISFACTION]] = AspectValue.newAspectValue(CUSTOMER_SATISFACTION,1.0); // start at best
    acc[AM[READINESS]] = AspectValue.newAspectValue(READINESS,1.0);
    acc[AM[POD_DATE]] = AspectValue.newAspectValue(POD_DATE,0L);
    acc[AM[POD]] = UNDEFINED_POD;

    int count = 0;
    boolean suc = true;
    double rating = 0.0;
      
    Enumeration tasks = wf.getTasks();
    if (tasks == null || (! tasks.hasMoreElements())) return null;
      
    String auxqsummary[] = new String[AuxiliaryQueryType.AQTYPE_COUNT];
    // initialize all values to UNDEFINED for comparison purposes below.
    final String UNDEFINED = "UNDEFINED";
    for (int aqs = 0; aqs < auxqsummary.length; aqs++) {
      auxqsummary[aqs] = UNDEFINED;
    }

    int tstSize = tst.size ();
    for (int i = 0; i < tstSize; i++) {
      Task t = tst.getTask (i);
      count++;
      AllocationResult ar = tst.getAllocationResult(i);

      if (ar == null) {
	return null; // bail if undefined
      }

      suc = suc && ar.isSuccess();
      rating += ar.getConfidenceRating();
        
      int[] definedaspects = ar.getAspectTypes();
      for (int b = 0; b < definedaspects.length; b++) {
	// accumulate the values for the defined aspects
	switch (definedaspects[b]) {
	case START_TIME: 
          acc[AM[START_TIME]] = acc[AM[START_TIME]].dupAspectValue(Math.min(acc[AM[START_TIME]].longValue(),
                                                                    ar.getAspectValue(START_TIME).longValue()));
	  break;
	case END_TIME:
          acc[AM[END_TIME]] = acc[AM[END_TIME]].dupAspectValue(Math.max(acc[AM[END_TIME]].longValue(),
                                                                ar.getAspectValue(END_TIME).longValue()));

	  break;
	case POD_DATE:
          acc[AM[POD_DATE]] = acc[AM[POD_DATE]].dupAspectValue(Math.max(acc[AM[POD_DATE]].longValue(),
                                                                ar.getAspectValue(POD_DATE).longValue()));
	  break;
        case DURATION:
          break;

	case COST:
          acc[AM[COST]] = acc[AM[COST]].dupAspectValue(acc[AM[COST]].floatValue() + ar.getAspectValue(COST).floatValue());
	  break;
	case DANGER:
          acc[AM[DANGER]] = acc[AM[DANGER]].dupAspectValue(Math.max(acc[AM[DANGER]].longValue(),
                                                            ar.getAspectValue(DANGER).longValue()));

	  break;
	case RISK:
          acc[AM[RISK]] = acc[AM[RISK]].dupAspectValue(Math.max(acc[AM[RISK]].longValue(),
                                                            ar.getAspectValue(RISK).longValue()));
	  break;
	case QUANTITY: 
          acc[AM[QUANTITY]] = acc[AM[QUANTITY]].dupAspectValue(acc[AM[QUANTITY]].floatValue() + ar.getAspectValue(QUANTITY).floatValue());
	  break;
	  // for now simply add the repetitve task values
	case INTERVAL: 
          acc[AM[INTERVAL]] = acc[AM[INTERVAL]].dupAspectValue(acc[AM[INTERVAL]].longValue() + ar.getAspectValue(INTERVAL).longValue());
	  break;
	case TOTAL_QUANTITY: 
          acc[AM[TOTAL_QUANTITY]] = acc[AM[TOTAL_QUANTITY]].dupAspectValue(acc[AM[TOTAL_QUANTITY]].floatValue() + ar.getAspectValue(TOTAL_QUANTITY).floatValue());
	  break;
	case TOTAL_SHIPMENTS: 
          acc[AM[TOTAL_SHIPMENTS]] = acc[AM[TOTAL_SHIPMENTS]].dupAspectValue(acc[AM[TOTAL_SHIPMENTS]].floatValue() + ar.getAspectValue(TOTAL_SHIPMENTS).floatValue());
	  break;
	  //end of repetitive task specific aspects
	case CUSTOMER_SATISFACTION: 
          acc[AM[CUSTOMER_SATISFACTION]] = acc[AM[CUSTOMER_SATISFACTION]].dupAspectValue(acc[AM[CUSTOMER_SATISFACTION]].floatValue() + ar.getAspectValue(CUSTOMER_SATISFACTION).floatValue());
	  break;
	}
      }
        
      // Sum up the auxiliaryquery data.  If there are conflicting data
      // values, send back nothing for that type.  If only one subtask
      // has information about a querytype, send it back in the 
      // aggregated result.
      for (int aq = 0; aq < AuxiliaryQueryType.AQTYPE_COUNT; aq++) {
		String data = ar.auxiliaryQuery(aq);
		if (data != null) {
		  String sumdata = auxqsummary[aq];
		  // if sumdata = null, there has already been a conflict.
		  if (sumdata != null) {
			if (sumdata.equals(UNDEFINED)) {
			  // there's not a value yet, so use this one.
			  auxqsummary[aq] = data;
			} else if (! data.equals(sumdata)) {
			  if ((aq == AuxiliaryQueryType.POE_DATE) ||
				  (aq == AuxiliaryQueryType.POD_DATE)){
				// if we have two different dates, use the earlier one
				String mydata = "";
				if (data.compareTo(sumdata) < 0){
				  mydata = data;
				}else {
				  mydata = sumdata;
				}
				auxqsummary[aq] = mydata;
			  }	
			  else
				// there's a conflict, pass back null
				//	      auxqsummary[aq] = "<conflict>";
				auxqsummary[aq] = null;
			}
			
		  }
		}
      }

    } // end of looping through all subtasks
      
    acc[AM[DURATION]] =  AspectValue.newAspectValue(DURATION, acc[AM[END_TIME]].longValue()-acc[AM[START_TIME]].longValue());

    acc[AM[CUSTOMER_SATISFACTION]] = acc[AM[CUSTOMER_SATISFACTION]].dupAspectValue(acc[AM[CUSTOMER_SATISFACTION]].floatValue()/ count);

    rating /= count;

    boolean delta = false;
      
    // only check the defined aspects and make sure that the currentar is not null
    if (currentar == null) {
      delta = true;		// if the current ar == null then set delta true
    } else {
      int[] caraspects = currentar.getAspectTypes();
      if (caraspects.length != acc.length) {
	//if the current ar length is different than the length of the new
	// calculations (acc) there's been a change
	delta = true;
      } else {
	for (int i = 0; i < caraspects.length; i++) {
	  int da = caraspects[i];
	  if (! acc[AM[da]].equals(currentar.getAspectValue(da))) {
	    delta = true;
	    break;
	  }
	}
      }
      
      if (!delta) {
	if (currentar.isSuccess() != suc) {
	  delta = true;
	} else if (Math.abs(currentar.getConfidenceRating() - rating) > SIGNIFICANT_CONFIDENCE_RATING_DELTA) {
	  delta = true;
	}
      }
    }

    if (delta) {
      AllocationResult artoreturn = new AllocationResult(rating, suc, acc);
      for (int aqt = 0; aqt < auxqsummary.length; aqt++) {
		String aqdata = auxqsummary[aqt];
		if ( (aqdata !=null) && (aqdata != UNDEFINED) ) {
		  artoreturn.addAuxiliaryQueryInfo(aqt, aqdata);
		}
      }
      return artoreturn;
    } else {
      return currentar;
    }
  }
}
