/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.sample;

import org.cougaar.domain.glm.Constants;
import org.cougaar.core.cluster.Subscriber;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.plugin.SimplePlugIn;

import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.NewWorkflow;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.BulkEstimate;
import org.cougaar.domain.planning.ldm.plan.NewBulkEstimate;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.core.society.UID;

import org.cougaar.util.Enumerator;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Arrays;

import org.cougaar.util.UnaryPredicate;

/** PlugIn that subscribes to all BulkEstimate objects.  For each BulkEstimate
  * the PlugIn unpacks the object to get the task and the collection of preference
  * sets.  The plugin creates a root task with a verb of BulkEstimate and an expansion
  * in which the subtask is the BE task with one of the preference sets attached.
  * When the subtask gets published, a "real" plugin will dispose it and the results
  * will flow back up to our Expansion PlanElement.  When the allocation result reaches
  * the specified confidence rating, the result information will be stored and the
  * root task will be rescinded (along with everything underneath it).  If there
  * are more preference sets in the BulkEstimate, the process will repeat until all
  * preference sets have been used.  At that point, this plugin will compile all results
  * and place them in the BulkEstimate object, set the IsComplete flag to true and
  * publishChange the BulkEstimate object.   
  **/

public class ProvideEstimatesPlugIn extends SimplePlugIn {
	
	private IncrementalSubscription bulkestimates;
  private IncrementalSubscription selfasset;
  private List expsubs = new ArrayList();
  private HashMap exp_be_map = new HashMap();
	
	protected void setupSubscriptions() {
    // get all BulkEstimate objects
    bulkestimates = (IncrementalSubscription) subscribe(bulkestimatespred());
	}
	
	public synchronized void execute() {
    if (bulkestimates.hasChanged()) {
      Enumeration e = bulkestimates.getAddedList();
      while (e.hasMoreElements()) {
        BulkEstimate be = (BulkEstimate) e.nextElement();
        IncrementalSubscription toadd = provideEstimates(be, 0);
        if (toadd != null) {
          expsubs.add(toadd);
        }
		  }
    }
    if (! expsubs.isEmpty()) {
      ListIterator lit = expsubs.listIterator();
      while (lit.hasNext()) {
        IncrementalSubscription exp = (IncrementalSubscription) lit.next();
        if (exp.hasChanged()) {
          Enumeration echange = exp.getChangedList();
          while (echange.hasMoreElements()) {
            Expansion changed = (Expansion) echange.nextElement();
            BEStatus thebe = checkChange(changed, lit);
          }
        }
      }
    }
    
	}
  
  // return the subscription to add to expsubs so that we don't
  // have ConcurrentModificationException problems with the list.
  private IncrementalSubscription provideEstimates(BulkEstimate be, int prefsetnumber) {
    //System.err.println("provideEstimates being called for preference set: " + prefsetnumber);
    RootFactory factory = getFactory();
    // unpack the BulkEstimate
    Task thetask = be.getTask();
    List preferencesets = be.getPreferenceSets();
    Preference[] pset = (Preference[]) preferencesets.get(prefsetnumber);
    List plist = Arrays.asList(pset);
    Enumerator penum = new Enumerator(plist);
    // make a new copy of the task to send.
    NewTask copytask = factory.newTask();
    copytask.setPrepositionalPhrases(thetask.getPrepositionalPhrases());
    copytask.setVerb(thetask.getVerb());
    copytask.setDirectObject(thetask.getDirectObject());
    copytask.setPlan(thetask.getPlan());  
    copytask.setPreferences(penum);
    
    // create a bogus Expansion so that we have our own planelement to monitor
    Expansion newexp = createExpansion(copytask);
    // create a unary predicate for this expansion
    UnaryPredicate pred = createPredicate(newexp);
    // create a subscription
    IncrementalSubscription isub = (IncrementalSubscription) subscribe(pred);
    // save expansion-bulkestimate-preferenceset index-pred-subscription information
    BEStatus bep = new BEStatus(be, prefsetnumber, pred, isub);
    exp_be_map.put(newexp.getUID(), bep);
    return isub;
  }
  
  private Expansion createExpansion(Task sub) {
    RootFactory factory = getFactory();
    
    //make a bogus bulk estimate parent task
    NewTask betask = factory.newTask();
    betask.setVerb(Constants.Verb.BulkEstimate);
    betask.setPlan(sub.getPlan());
    publishAdd(betask);
    
    // expand the bulkestimate task
    NewWorkflow wf = factory.newWorkflow();
    wf.setParentTask(betask);
    ((NewTask)sub).setParentTask(betask);
    wf.addTask(sub);
    wf.setIsPropagatingToSubtasks(true);
    
    // create the actual Expansion PlanElement
    Expansion exp = factory.createExpansion(sub.getPlan(), betask, wf, null);
    
    // publish the expansion
    publishAdd(exp);
    
    // publish the single subtask of the workflow
    publishAdd(sub);
    
    return exp;    
  }
  
  // return the BEStatus to do things in the execute method
  private BEStatus checkChange(Expansion exp, ListIterator lit) {
    AllocationResult represult = exp.getReportedResult();
    double alloccr = represult.getConfidenceRating();
    BEStatus bes = (BEStatus) exp_be_map.get(exp.getUID());
    if (bes != null) {
      BulkEstimate mybe = bes.getBE();
      double becr = mybe.getConfidenceRating();
      // see if we have matched the confidencerating we want
      if (becr == alloccr) {
        // if we have then fill in the allocationresult for this pref set.
        ((NewBulkEstimate)mybe).setSingleResult(bes.getIndex(), represult);
        // rescind the fake bulkestimate root task and everything below it
        publishRemove(exp.getTask());
        // remove the expansion from the exp subs list
        lit.remove();
        // unsubscribe to this allocation
        unsubscribe(bes.getIncSubscription());
        // create the next estimate
        IncrementalSubscription wantadded = nextEstimate(bes);
        if (wantadded != null) {
          lit.add(wantadded);
        }

      }
    }
    // if we couldn't find that status or the confidence rating wasn't met
    // don't do anything, wait for another change with another result.
    
    // return the BEStatus even if its null
    return bes;
  }
  
  // return any subscriptions to add so that the add is done in execute to
  // get rid of ConcurrentModificationExceptions with the expsub List.
  private IncrementalSubscription nextEstimate(BEStatus astatus) {
    // if we call provideestimates return the subscription to be added to the expsubs list
    IncrementalSubscription add = null;
    // get the last index and see if we have more preference sets to try
    BulkEstimate be = astatus.getBE();
    int lastindex = astatus.getIndex();
    int newindex = lastindex + 1;
    int prefsize = be.getPreferenceSets().size();
    //System.err.println("next Estimate being called: lastindex = " + lastindex
                        //+ "  newindex = " + newindex +
                       // "   prefsize = " + prefsize);
    if (newindex < prefsize ) {
      // set off another round of estimates with the next preference set
      add = provideEstimates(be, newindex);
    } else if (newindex == prefsize) {
      // if the newindex is = to the pref size we might be done with this BulkEstimate
      // check the preference set size against the allocationresult size
      int arsize = be.getAllocationResults().length;
      if (arsize == prefsize) {
        // set the isComplete flag and publish the change on the BulkEstimate
        ((NewBulkEstimate)be).setIsComplete(true);
        publishChange(be);
      } else {
        System.err.println("WARNING!!!! BulkEstimate final result number does not " +
                          "match the preference set number.  BulkEstimate may not be able to complete estimates!!");
      }
    } else {
      // if we got here there is a problem with the index
      System.err.println("WARNING!!! BulkEstimate index problem.  BulkEstimate may not be able to complete estimates!!");
    }
    return add;
  }
  
  
  // utility inner class to keep track of the bulk estimate and the
  // preference set used (marked by the index)
  class BEStatus {
    int index;
    BulkEstimate thebe;
    UnaryPredicate thepred;
    IncrementalSubscription theisub;
    public BEStatus(BulkEstimate abe, int anindex, UnaryPredicate apred, IncrementalSubscription asub) {
      thebe = abe;
      index = anindex;
      thepred = apred;
      theisub = asub;
    }
    public int getIndex() {return index;}
    public BulkEstimate getBE() {return thebe;}
    public UnaryPredicate getPredicate() {return thepred;}
    public IncrementalSubscription getIncSubscription() {return theisub;}
  }
  
  // utility to create unary predicates for the expansion
  private UnaryPredicate createPredicate(Expansion anexp) {
    UnaryPredicate newpred = new AllocIDUnaryPredicate(anexp);
    return newpred;
  }
  // special unarypredicate subclass that compares expansion uids
  private final class AllocIDUnaryPredicate implements UnaryPredicate {
    private UID aluid;

    public AllocIDUnaryPredicate(Expansion anexp) {
      super();
      aluid = anexp.getUID();
    }
    
    public boolean execute(Object o) {
      if (o instanceof Expansion) {
        UID testid = ((Expansion)o).getUID();
        if (testid.equals(aluid)) {
          return true;
        }
      }
      return false;
    }
  }


    

	
  // predicate for getting all BulkEstimate objects
  private static UnaryPredicate bulkestimatespred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof BulkEstimate);
      }
    };
  }
  
  
}
