/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.glm.util;

import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.AbstractAsset;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectScorePoint;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;

/**
 * Helper class for building Allocator Plugins.
 */
public class AllocatorHelper extends org.cougaar.planning.plugin.util.AllocatorHelper 
{

    /**
     * UpdatePV looks for differences between the reported and 
     * estimated allocation results. If they are
     * different then the estimated value is set to the reported
     * value in both the cases.
     */
    public static void updatePV ( PlanElement pe, CollectionSubscription sub ) {
	
	    if (pe.getReportedResult() != null) {
	      //compare the result objects.
        // If they are NOT ==, re-set the estimated result.
        // For now ignore whether their compositions are equal.
        AllocationResult repar = pe.getReportedResult();
	      AllocationResult estar = pe.getEstimatedResult();
	      if ( (estar == null) || (! (repar == estar) ) ) {
	      	pe.setEstimatedResult(repar);
		      sub.getSubscriber().publishChange( pe );
	      }
	    }
    }
    
    /** Takes a subscription, gets the changed list and updates the changedList.*/
    public static void updateAllocationResult ( IncrementalSubscription sub ) {
	
	Enumeration changedPEs = sub.getChangedList();
	while ( changedPEs.hasMoreElements() ) {
	    PlanElement pe = (PlanElement)changedPEs.nextElement();
	    if (pe.getReportedResult() != null) {
		//compare entire pv arrays
		AllocationResult repar = pe.getReportedResult();
		AllocationResult estar = pe.getEstimatedResult();
		if ( (estar == null) || (!repar.isEqual(estar)) ) {
		    pe.setEstimatedResult(repar);
		    sub.getSubscriber().publishChange( pe );
		}
	    }
	}
    }


    /**
     * Checks if the Task is of specified OFTYPE.
     */
    public static boolean isOfType( Task t, String p, String typeid ) {
	Enumeration prepPhrases =  ( (Task)t ).getPrepositionalPhrases();
	PrepositionalPhrase pPhrase;
	String prep;
	AbstractAsset aa = null;
	String mytypeid = null;

	while ( prepPhrases.hasMoreElements() ) {
	    pPhrase = ( PrepositionalPhrase ) prepPhrases.nextElement();
	    prep = pPhrase.getPreposition();
	    if ( prep.equals( p ) ) {
		Object indirectobj = pPhrase.getIndirectObject();
		if( indirectobj instanceof AbstractAsset ) {
		    aa = (AbstractAsset) indirectobj;
		    mytypeid = aa.getTypeIdentificationPG().getTypeIdentification();
		    if ( mytypeid.equals( typeid ) )  return true;
		}
	    }
	}
	return false;
    }

    public static AllocationResult createEstimatedAllocationResult(Task t, PlanningFactory ldmf) {
	Enumeration preferences = t.getPreferences();
	if ( preferences != null && preferences.hasMoreElements() ) {
	    // do something really simple for now.
	    Vector aspects = new Vector();
	    Vector results = new Vector();
	    while (preferences.hasMoreElements()) {
		Preference pref = (Preference) preferences.nextElement();
		int at = pref.getAspectType();
		aspects.addElement(new Integer(at));
		ScoringFunction sf = pref.getScoringFunction();
		// allocate as if you can do it at the "Best" point
		double myresult = ((AspectScorePoint)sf.getBest()).getValue();
		results.addElement(new Double(myresult));
	    }
	    int[] aspectarray = new int[aspects.size()];
	    double[] resultsarray = new double[results.size()];
	    for (int i = 0; i < aspectarray.length; i++)
		aspectarray[i] = (int) ((Integer)aspects.elementAt(i)).intValue();
	    for (int j = 0; j < resultsarray.length; j++ )
		resultsarray[j] = (double) ((Double)results.elementAt(j)).doubleValue();
        
	    AllocationResult myestimate = ldmf.newAllocationResult(0.0, true, aspectarray, resultsarray);
	    return myestimate;
	}
	// if there were no preferences...return a null estimate for the allocation result (for now)
	return null;
    }

  /*
    public static void allocateToIndirectObjects ( Workflow wf, PlanningFactory ldmf, Subscriber mySubscriber ) {
	Asset a = null;
	Enumeration tasks = wf.getTasks();
	while ( tasks.hasMoreElements()) {
	    Task t = (Task) tasks.nextElement();
	    AllocationResult estimatedresult = createEstimatedAllocationResult(t, ldmf);
	    Enumeration pp = t.getPrepositionalPhrases();
	    while (pp.hasMoreElements()) {
		PrepositionalPhrase p = (PrepositionalPhrase) pp.nextElement();
		if (p.getPreposition().equals(Preposition.FOR)) {
		    Object indir = p.getIndirectObject();
		    if ( indir instanceof Organization) {
			a = (Asset) indir;
			Allocation alloc = ldmf.createAllocation(ldmf.getRealityPlan(), t, a, estimatedresult, Role.BOGUS);
			mySubscriber.publishAdd(alloc);
		    }
		}
	    }
	}
    }
  */
}
