/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.util;

import java.util.Vector;
import java.util.Enumeration;
import java.io.Serializable;
import org.cougaar.domain.planning.ldm.plan.AllocationResultAggregator;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.domain.planning.ldm.plan.Workflow;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.TaskScoreTable;

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

  static final int[] _UTIL_ASPECTS = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14};
  public AllocationResult calculate(Workflow wf, TaskScoreTable tst, AllocationResult currentar) {
    double acc[] = new double[AspectType._ASPECT_COUNT+2];
    acc[START_TIME] = Double.MAX_VALUE;
    acc[END_TIME] = 0.0;
    // duration is computed from end values of start and end
    acc[COST] = 0.0;
    acc[DANGER] = 0.0;
    acc[RISK] = 0.0;
    acc[QUANTITY] = 0.0;
    acc[INTERVAL] = 0.0;
    acc[TOTAL_QUANTITY] = 0.0;
    acc[TOTAL_SHIPMENTS] = 0.0;
    acc[CUSTOMER_SATISFACTION] = 1.0; // start at best
    acc[READINESS] = 1.0;
    acc[POD_DATE] = 0.0;
    acc[POD] = 0.0;

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
	case START_TIME: acc[START_TIME] = Math.min(acc[START_TIME], ar.getValue(START_TIME));
	  break;
	case END_TIME: acc[END_TIME] = Math.max(acc[END_TIME], ar.getValue(END_TIME));
	  break;
	case POD_DATE: acc[POD_DATE] = Math.max(acc[POD_DATE], ar.getValue(POD_DATE));
	  break;
	  // compute duration later
	case COST: acc[COST] += ar.getValue(COST);
	  break;
	case DANGER: acc[DANGER] = Math.max(acc[DANGER], ar.getValue(DANGER));
	  break;
	case RISK: acc[RISK] = Math.max(acc[RISK], ar.getValue(RISK));
	  break;
	case QUANTITY: acc[QUANTITY] += ar.getValue(QUANTITY);
	  break;
	  // for now simply add the repetitve task values
	case INTERVAL: acc[INTERVAL] += ar.getValue(INTERVAL);
	  break;
	case TOTAL_QUANTITY: acc[TOTAL_QUANTITY] += ar.getValue(TOTAL_QUANTITY);
	  break;
	case TOTAL_SHIPMENTS: acc[TOTAL_SHIPMENTS] += ar.getValue(TOTAL_SHIPMENTS);
	  break;
	  //end of repetitive task specific aspects
	case CUSTOMER_SATISFACTION: acc[CUSTOMER_SATISFACTION] += ar.getValue(CUSTOMER_SATISFACTION);
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
      
    acc[DURATION] = acc[END_TIME] - acc[START_TIME];
    acc[CUSTOMER_SATISFACTION] /= count;

    rating /= count;

    boolean delta = false;
    //for (int i = 0; i <= _LAST_ASPECT; i++) {
    //if (acc[i] != currentar.getValue(i)) {
    //delta = true;
    //break;
    //}
    //}
      
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
	  if (acc[da] != currentar.getValue(da)) {
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
      AllocationResult artoreturn = new AllocationResult(rating, suc, _UTIL_ASPECTS, acc);
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
};
