/*
 * <Copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.organization;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.cougaar.core.cluster.ChangeReport;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AssetTransfer;
import org.cougaar.domain.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.NewSchedule;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Relationship;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;

import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.util.PlugInHelper;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.Constants;

import org.cougaar.domain.glm.ldm.asset.Organization;

/**
 * OrgReportPlugIn manages REPORTFORDUTY and REPORTFORSERVICE relationships
 * Handles both expansion and allocation of these tasks.
 * @see org.cougaar.domain.mlm.plugin.organization.AllocatorPlugInImpl
 * @see org.cougaar.core.plugin.SimplifiedPlugIn
 * @see org.cougaar.core.plugin.SimplifiedPlugInTest
 */

public class OrgReportPlugIn extends SimplePlugIn
{
  private IncrementalSubscription myAllocatableRFDTasks;
  private IncrementalSubscription myAllocatableRFSTasks;
  private IncrementalSubscription myRFDAssetTransfers;
  private IncrementalSubscription myRFSAssetTransfers;

  private IncrementalSubscription mySelfOrgs;

  //Override the setupSubscriptions() in the SimplifiedPlugIn.
  protected void setupSubscriptions() {
    // subscribe for incoming ReportForDuty Tasks 
    myAllocatableRFDTasks = 
      (IncrementalSubscription) subscribe(allRFDTaskPred());

    // subscribe for incoming ReportForService Tasks
    myAllocatableRFSTasks = 
      (IncrementalSubscription) subscribe(allRFSTaskPred());

    // subscribe to my allocations in order to propagate allocationresults
    myRFSAssetTransfers = (IncrementalSubscription)subscribe(allRFSAssetTransferPred());
    myRFDAssetTransfers = (IncrementalSubscription)subscribe(allRFDAssetTransferPred());

    // subscribe to my self orgs so I can propagate modifications
    mySelfOrgs = (IncrementalSubscription)subscribe(allSelfOrgPred());
  }
  
  public void execute() {
    // Handle REPORTFORDUTY tasks, expanding and allocating all at once
    if (myAllocatableRFDTasks.hasChanged()) {
      Enumeration newtasks = myAllocatableRFDTasks.getAddedList();
      while (newtasks.hasMoreElements()) {
        Task currentTask = (Task)newtasks.nextElement();
        allocate(currentTask);
      }
    }  
  
    // Now handle any REPORTFORSERVICE tasks in the same way
    if (myAllocatableRFSTasks.hasChanged()) {
      Enumeration newtasks = myAllocatableRFSTasks.getAddedList();
      while (newtasks.hasMoreElements()) {
        Task currentTask = (Task)newtasks.nextElement();
        allocate(currentTask);
      }
    }

    // If get back a reported result, automatically send it up.
    if (myRFDAssetTransfers.hasChanged()) {
      Enumeration changedallocs = myRFDAssetTransfers.getChangedList();
      while (changedallocs.hasMoreElements()) {
        PlanElement cpe = (PlanElement)changedallocs.nextElement();
        if (PlugInHelper.updatePlanElement(cpe)) {
          publishChange(cpe);
        }
      }
    }
  
    if (myRFSAssetTransfers.hasChanged()) {
      Enumeration changedallocs = myRFSAssetTransfers.getChangedList();
      while (changedallocs.hasMoreElements()) {
        PlanElement cpe = (PlanElement)changedallocs.nextElement();
        if (PlugInHelper.updatePlanElement(cpe)) {
          publishChange(cpe);
        }
      }
    }


    if (mySelfOrgs.hasChanged()) {
      resendAssetTransfers();
    }
  }
  
  private void allocate(Task task) {
    Organization reportingOrg = (Organization)task.getDirectObject();

    if (!reportingOrg.getClusterPG().getClusterIdentifier().equals(getClusterIdentifier())) {
      allocateRemote(task);
    } else {
      allocateLocal(task);
    }
  }

  private void allocateLocal(Task task) {
    Organization reportingOrg = (Organization)task.getDirectObject();
    Organization reportee = 
      (Organization) findIndirectObject(task, Constants.Preposition.FOR);

    Organization localReportingOrg = findLocalOrg(reportingOrg);
    if ((localReportingOrg == null) ||
        (!localReportingOrg.isSelf())) {
      System.err.println(getClusterIdentifier().toString()+
                         "/OrgReportPlugIn: unable to process " + 
                         task.getVerb() + " task - " + 
                         reportingOrg + " reporting to " + reportee + ".\n" +
                         localReportingOrg + " not local to this cluster."
                         +"\n"+localReportingOrg.getRelationshipSchedule()+"\n"
                         );
      return;
    }
    
    long startTime = (long) task.getPreferredValue(AspectType.START_TIME);
    long endTime = (long) task.getPreferredValue(AspectType.END_TIME);
    

    // Make RelationshipSchedule for the reporting org
    Collection roles = 
      (Collection) findIndirectObject(task, Constants.Preposition.AS);
    RelationshipSchedule schedule = 
      getFactory().newRelationshipSchedule(reportingOrg);
    for (Iterator iterator = roles.iterator(); iterator.hasNext();) {
      Relationship relationship = 
        getFactory().newRelationship((Role) iterator.next(),
                                     reportingOrg,
                                     reportee,
                                     startTime,
                                     endTime);
      schedule.add(relationship);
    }
    reportingOrg.setRelationshipSchedule(schedule);

    // create the transfer
    NewSchedule availSchedule = 
      getFactory().newSimpleSchedule(startTime,
                                     endTime);

    AllocationResult newEstimatedResult = 
      PlugInHelper.createEstimatedAllocationResult(task,
                                                   theLDMF,
                                                   1.0,
                                                   true);

    AssetTransfer assetTransfer = 
      getFactory().createAssetTransfer(task.getPlan(), task, 
                                       reportingOrg,
                                       availSchedule, 
                                       reportee,
                                       newEstimatedResult, 
                                       Role.ASSIGNED);
    publishAdd(assetTransfer);
  }

  private void allocateRemote(Task task) {
    Organization reportingOrg = (Organization)task.getDirectObject();
      
    AllocationResult newEstimatedResult = 
      PlugInHelper.createEstimatedAllocationResult(task,
                                                   theLDMF,
                                                   1.0,
                                                   true);
    
    Allocation allocation = 
      getFactory().createAllocation(task.getPlan(), task, 
                                    reportingOrg,
                                    newEstimatedResult, 
                                    Role.ASSIGNED);
    publishAdd(allocation);
    return;
  }

  protected Organization findLocalOrg(Organization org) {
    final String uic = org.getItemIdentificationPG().getItemIdentification();
    Organization localOrg = null;

    // Query subscription to see if clientOrg already exists
    Collection collection = query(new UnaryPredicate() {
      
      public boolean execute(Object o) {
        if ((o instanceof Organization) &&
            (((Organization)o).getItemIdentificationPG().
             getItemIdentification().equals(uic)))
          return true;
        else {
          return false;
        }
      }
    });

    if (collection.size() > 0) {
      Iterator iterator = collection.iterator();
      localOrg = (Organization)iterator.next();

      if (iterator.hasNext()) {
        throw new RuntimeException("OrgReportPlugIn - multiple assets with UIC = " + 
                                   uic);
      }
    } 

    return localOrg;
  }

  // ###############################################################
  // END Allocation
  // ###############################################################

  protected Object findIndirectObject(Task _task, String _prep) {
    PrepositionalPhrase pp = _task.getPrepositionalPhrase(_prep);
    if (pp == null)
      throw new RuntimeException("Didn't find a single \"" + _prep + 
                                 "\" Prepositional Phrase in " + _task);

    return pp.getIndirectObject();
  }

  private void resendAssetTransfers() {
    // BOZO - No support for removal of a self Org
    Collection changes = mySelfOrgs.getChangedCollection();
    if ((changes == null) || 
        (changes.isEmpty())) {
      return;
    }

    for (Iterator iterator = changes.iterator();
         iterator.hasNext();) {
      Organization selfOrg = (Organization) iterator.next();
      // Determine whether or not asset transfers for the self org should be 
      // resent.  At this point, do not resend just because the relationship 
      // schedule changed. Warning - legtimate change will get lost if batched
      // with relationship schedule changes unless a separate change report is 
      // generated.
      Collection changeReports = mySelfOrgs.getChangeReports(selfOrg);
      boolean resendRequired = false;

      if ((changeReports != null) && !changeReports.isEmpty()) {
        for (Iterator reportIterator = changeReports.iterator();
             reportIterator.hasNext();) {
          ChangeReport report = (ChangeReport) reportIterator.next();
          if (!(report instanceof RelationshipSchedule.RelationshipScheduleChangeReport)) {
            resendRequired = true;
            break;
          }
        }
      } else {
        resendRequired = true;
      }
      if (resendRequired) {
        resendAssetTransfers(selfOrg, myRFSAssetTransfers.getCollection(), changeReports);
        resendAssetTransfers(selfOrg, myRFDAssetTransfers.getCollection(), changeReports);
      }
    }
  }

  /**
     Resend a collection of AssetTransfers. Asset transfers are "sent"
     by doing a publishChange. The change reports are supplied by the
     caller, but are just the change reports of the change that
     initiated this resend. Only transfers of our Organization to
     other Organizations are sent, the rest are ignored.
   **/
  private void resendAssetTransfers(Organization selfOrg,
                                    Collection transfers,
                                    Collection changeReports)
  {
    for (Iterator i = transfers.iterator(); i.hasNext();) {
      AssetTransfer at = (AssetTransfer) i.next();
      if (at.getAsset().equals(selfOrg)) {
        if (at.getAssignee().equals(selfOrg)) {
          // System.out.println("Not resending " + at);
        } else {
          at.indicateAssetChange();
          publishChange(at, changeReports);
        }
      }
    }
  }

  // #######################################################################
  // BEGIN predicates
  // #######################################################################
  
  // predicate for getting allocatable tasks of report for duty
  private static UnaryPredicate allRFDTaskPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Task) {
          Task task = (Task) o;
	  if ((task.getVerb().equals(Constants.Verb.REPORTFORDUTY)) &&
              (task.getWorkflow() == null) &&
              (task.getPlanElement() == null)) {
	    return true;
          }
	}
	return false;
      }
    };
  }

  /** Predicate for dealing with allocatable Tasks of ReportForService */
  private UnaryPredicate allRFSTaskPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Task) {
          Task task = (Task) o;
	  if ((task.getVerb().equals(Constants.Verb.REPORTFORSERVICE)) &&
              (task.getWorkflow() == null) &&
              (task.getPlanElement() == null)) {
	    return true;
          } 
        }
	return false;
      }
    };
  }

  private static UnaryPredicate allRFDAssetTransferPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof AssetTransfer) {
          Task t = ((AssetTransfer)o).getTask();
          if (t.getVerb().equals(Constants.Verb.REPORTFORDUTY)) {
            // if the PlanElement is for the correct kind of task then
            // make sure it's an assettransfer
            return true;
          }
        }
        return false;
      }
    };
  }

  private static UnaryPredicate allRFSAssetTransferPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof PlanElement) {
          Task t = ((PlanElement)o).getTask();
          if (t.getVerb().equals(Constants.Verb.REPORTFORSERVICE)) {
            // if the PlanElement is for the correct kind of task
            // then make sure it's an allocation
            if (o instanceof AssetTransfer) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }

  private static UnaryPredicate allSelfOrgPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Organization) {
          return ((Organization) o).isSelf();
        } else {
          return false;
        }
      }
    };
  }
}
