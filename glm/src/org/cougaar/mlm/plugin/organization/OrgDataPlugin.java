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
package org.cougaar.mlm.plugin.organization;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;

import org.cougaar.glm.ldm.oplan.TimeSpan;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.plugin.asset.AssetDataPlugin;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.oplan.OrgActivityImpl;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.plugins.TaskUtils;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.TimeSpanSet;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.util.UID;
import org.cougaar.core.mts.MessageAddress;

public class OrgDataPlugin extends AssetDataPlugin  {


  private static Calendar myCalendar = Calendar.getInstance();

  private static long DEFAULT_START_TIME = -1;
  private static long DEFAULT_END_TIME = -1;

  static {
    myCalendar.set(1990, 0, 1, 0, 0, 0);
    DEFAULT_START_TIME = myCalendar.getTime().getTime();

    myCalendar.set(2010, 0, 1, 0, 0, 0);
    DEFAULT_END_TIME = myCalendar.getTime().getTime();   

    packages.add("org.cougaar.glm.ldm.asset");
    packages.add("org.cougaar.glm.ldm.plan");
    packages.add("org.cougaar.glm.ldm.oplan");
    packages.add("org.cougaar.glm.ldm.policy");
  }


  IncrementalSubscription oplans;
  IncrementalSubscription orgActivities;
  IncrementalSubscription myOpConInfoRelaySubscription;
  IncrementalSubscription reportForDutySubscription;
  private HashMap myOpSups = new HashMap();

  MessageAddress myAgentAddr;
  MessageAddress target;

  // Temporaries used during org activity reading
  private TimeSpanSet currentRFDs = new TimeSpanSet();
  private TimeSpanSet addedRFDInfos = new TimeSpanSet();
  private TimeSpanSet removedRFDs = new TimeSpanSet();

  // report for duty tasks
  protected UnaryPredicate myRFDpredicate =  new UnaryPredicate() {
    public boolean execute (Object o) {
      if (o instanceof Task) {
	Task task = (Task) o;
	Verb verb = task.getVerb();
        // Get the ReportForDuty task where this Agent is reporting to 
        //  its OperationalSuperior
        // -- not its SupportSuperior or AdministrativeSuperior
        if (Constants.Verb.ReportForDuty.equals(verb)) {
	PrepositionalPhrase pp = 
	  task.getPrepositionalPhrase(org.cougaar.planning
				      .Constants.Preposition.AS);
	if (pp == null)
	  return false;
	Collection roles = (Collection) pp.getIndirectObject();
	if (roles == null)
	  return false;
	// This collection should contain "Subordinate"
	return roles.contains(org.cougaar.glm.ldm.Constants.Role.OPERATIONALSUBORDINATE);
        }
      }
      return false;
    }
  };
  
  // oplan
  protected static class OplanPredicate implements UnaryPredicate {
    public boolean execute(Object o) {
      return (o instanceof Oplan);
    }
  } 
  
  // org activities
  protected class OrgActivitiesPredicate implements UnaryPredicate {
    UID oplanUID_;
    public OrgActivitiesPredicate(UID uid) {
      oplanUID_ = uid;
    }
    
    public boolean execute(Object o) {
      if (o instanceof OrgActivity) {
	if (oplanUID_.equals(((OrgActivity)o).getOplanUID())) {
	  return true;
	}
      }
      return false;
    }
  }

  private UnaryPredicate myOpConInfoRelayPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof OpConInfoRelay) {
        OpConInfoRelay relay = (OpConInfoRelay) o;
        return (myAgentAddr.equals(relay.getSource()));
      } else {
	return false;
      }
      //return o instanceof OpConInfoRelay;
    }
  };


  protected Verb getReportVerb(Collection roles) {
    // Assuming that collection of roles never mixes subordinate with
    // provider roles.
    for (Iterator iterator = roles.iterator(); iterator.hasNext();) {
      Role role = (Role) iterator.next();

      // Does this Role match SUPERIOR/SUBORDINATE RelationshipType
      if ((role.getName().endsWith(Constants.RelationshipType.SUBORDINATE_SUFFIX)) &&
	  (role.getConverse().getName().endsWith(Constants.RelationshipType.SUPERIOR_SUFFIX))) {
	return Constants.Verb.ReportForDuty;
      }
    } 

    // Didn't get a superior/subordinate match
    return Constants.Verb.ReportForService;
  }

  protected void setupSubscriptions() {
    super.setupSubscriptions();
    // Set up before the subscriptions so predicates can reference.
    myAgentAddr  = getMessageAddress();

    oplans = 
      (IncrementalSubscription) getBlackboardService().subscribe(new OplanPredicate());
    reportForDutySubscription = 
      (IncrementalSubscription) getBlackboardService().subscribe(myRFDpredicate);
    myOpConInfoRelaySubscription = 
      (IncrementalSubscription) getBlackboardService().subscribe(myOpConInfoRelayPred);
  }

  public void execute() {
    if (oplans.hasChanged()) {
      Enumeration enum;
      if (oplans.getAddedList().hasMoreElements()) {
        enum = oplans.getAddedList();
        while (enum.hasMoreElements()) {
          Oplan oplan = (Oplan)enum.nextElement();
          UID oplanUID = oplan.getUID();
          if (orgActivities == null) {
            orgActivities = (IncrementalSubscription)
              getBlackboardService().subscribe(new OrgActivitiesPredicate(oplanUID));
          }
        }
      }
    }
    if (orgActivities != null && orgActivities.hasChanged()) {
      processPotentialChangedOpCons();

      for (Iterator j = removedRFDs.iterator(); j.hasNext(); ) {
        Task rfd = (Task) j.next();
        getBlackboardService().publishRemove(rfd);
      }
    
      if (myLogger.isInfoEnabled())
        myLogger.info(getAgentIdentifier() + " execute: RFDs to add: " + addedRFDInfos.size());
      
      for (Iterator j = addedRFDInfos.iterator(); j.hasNext(); ) {
        Task rfd = ((RFDInfo)j.next()).getTask();
        getBlackboardService().publishAdd(rfd);
        if (myLogger.isInfoEnabled())
          myLogger.info(getAgentIdentifier() +"- Adding RFD: "+rfd);
      }
    }

    if (myOpConInfoRelaySubscription.hasChanged()) {
      if (myLogger.isInfoEnabled()) {
        myLogger.info(getAgentIdentifier() +" myOpConInfoRelaySubscription has changed!");
      }
      Collection changedOpConInfoRelays =
        myOpConInfoRelaySubscription.getChangedCollection();

      createOperationalRFDs(changedOpConInfoRelays);
    }
  }

  public long getDefaultStartTime() {
    return DEFAULT_START_TIME;
  }

  public long getDefaultEndTime() {
    return DEFAULT_END_TIME;
  }

  protected void processPotentialChangedOpCons() {
    currentRFDs.clear();
    addedRFDInfos.clear();
    removedRFDs.clear();

    TimeSpanSet[] tsSets = {currentRFDs, addedRFDInfos};

    // Go thru current set of RFD tasks and extract the start
    //  and end times as well as the operational superior
    // Make RFDInfo objects to be compared against
    Collection myRFDs = getBlackboardService().query(myRFDpredicate);
    for (Iterator it = myRFDs.iterator(); it.hasNext(); ) {
      Task rfdTask = (Task) it.next();

      RFDInfo rfdInfo = new RFDInfo(rfdTask);
      currentRFDs.add(rfdInfo);
      //         if (myLogger.isInfoEnabled()) {
      //           myLogger.info(getAgentIdentifier()+" from RFD opSup is: "+rfdInfo.getMyOpSuperior());
      //           myLogger.info(getAgentIdentifier()+" startTime is: "+new Date(rfdInfo.getStartTime()) 
      //                         +" and endTime is: "+new Date(rfdInfo.getEndTime()));
      //         }
    }

    for (Iterator i = orgActivities.iterator(); i.hasNext(); ) {
      OrgActivity oa = (OrgActivity) i.next();

      String adCon = oa.getAdCon();
      String opCon = oa.getOpCon();
      long oaStart = oa.getStartTime();
      long oaEnd = oa.getEndTime();

      //need to compare the opcon from the org activity to see if its different
      // from the OperationalSuperior in the ReportForDuty tasks for the
      // corresponding timespan of the OrgActivity.  If so...
      // Send a relay to get itemId, and typeId from opcon 

      //go thru current rfds first then added
      // to check for any overlapping timespans
      for (int k = 0; k < tsSets.length; k++) {
        TimeSpanSet tsSet = tsSets[k];
        Collection affectedRFDs = tsSet.intersectingSet(oa);
        for (Iterator j = affectedRFDs.iterator(); j.hasNext(); ) {
          //found an overlapping timespan 
          // now check if opcon is different
          RFDInfo existingRFD = (RFDInfo)j.next();
          if (!existingRFD.getMyOpSuperior().equals(opCon)) {
            if (myLogger.isInfoEnabled()) {
              myLogger.info(getAgentIdentifier() +" OpCon from OrgActivity does not match overlapping RFD superior!");
            }
            //need to make a new rfd for this timspan
            tsSet.remove(existingRFD); // Don't know where it will end up,
            //  but it can't be here anymore
            // See if any part is still viable
            long existingStartTime = existingRFD.getStartTime();
            long existingEndTime = existingRFD.getEndTime();
            //if the existingActivity came from the set of currentRFDs
            //  it should be removed from blackboard ultimately because
            //  we will copy the info and make a new one for part and
            //  the newly read OA will cover the other part.
            boolean existingRFDNeedsRemove = (tsSet == currentRFDs);
            if (existingStartTime < oaStart) {
              //the new RFD starts later than the existing one so
              // there is a slice of the existing RFD at the start 
              // that is still valid.
              TimeSpan newTimeSpan = new TimeSpan(existingStartTime, oaStart);
              // Need new RFD like the existingRFD
              NewTask newRFD = makeRFD(newTimeSpan, existingRFD.getTask());
              if (myLogger.isInfoEnabled()) {
                myLogger.info(getAgentIdentifier() +" Beginning of old RFD still valid. New/Copied RFD is:" +newRFD);
              }
              addedRFDInfos.add(new RFDInfo(newRFD));
            }
            //Now need to check if the new timespan goes longer than the existing
            if (existingEndTime > oaEnd) { 
              //the new timespan ends earlier than the existing one in RFD so
              // there is a slice of the existing RFD at the end 
              // that is still valid.
              TimeSpan aNewTimeSpan = new TimeSpan(oaEnd, existingEndTime);
              // Need new RFD like the existingRFD
              NewTask aNewRFD = makeRFD(aNewTimeSpan, existingRFD.getTask());
              if (myLogger.isInfoEnabled()) {
                myLogger.info(getAgentIdentifier() +" End of old RFD still valid. New/Copied RFD is: "+ aNewRFD);
              }
              addedRFDInfos.add(new RFDInfo(aNewRFD));
            }
            if (existingRFDNeedsRemove) {
              getBlackboardService().publishRemove(existingRFD.getTask());
              if (myLogger.isInfoEnabled()) {
                myLogger.info("Removing RFD from blackboard: "+ existingRFD.getTask());
              }
            }
            //sending a relay to the opcon from the orgActivity
            MessageAddress addr = MessageAddress.getMessageAddress(opCon);
            target = addr;
        
            //extract the start and end time of the relationship from the OA timespan
            TimeSpan oaTimeSpan = new TimeSpan(oaStart, oaEnd);
        
            OpConInfoRelay opconRelay = new OpConInfoRelay(getUIDService().nextUID(),
                                                           myAgentAddr,
                                                           target,
                                                           oaTimeSpan,
                                                           null);
            if (myLogger.isInfoEnabled()) {
              myLogger.info(getAgentIdentifier()+" just created an OpConInfoRelay: "+opconRelay.toString()
                            +" from " +myAgentAddr +" to " +target);
            }
            getBlackboardService().publishAdd(opconRelay);
          }
        }
      }
    }
  }


  protected NewTask makeRFD(TimeSpan newTS, Task existingRFD) {

    Asset localClone = myPlanningFactory.cloneInstance(myLocalAsset);
    PrepositionalPhrase pp= 
      existingRFD.getPrepositionalPhrase(org.cougaar.planning
                               .Constants.Preposition.FOR);
    Asset sendTo = myPlanningFactory.cloneInstance((Asset)pp.getIndirectObject());
    
    pp = existingRFD.getPrepositionalPhrase(org.cougaar.planning
                                            .Constants.Preposition.AS);
    if (pp == null) {
      if (myLogger.isDebugEnabled()) {
        myLogger.debug(getAgentIdentifier() +" Can't make newRFD task.");
      }
    }
    Collection roles = (Collection)pp.getIndirectObject();
     
    NewTask newRFD = createReportTask(localClone, sendTo, roles, newTS.getStartTime(), newTS.getEndTime());
    return newRFD;
  }


  protected void createOperationalRFDs (Collection changedOpConInfoRelays) {
      for (Iterator changes = changedOpConInfoRelays.iterator();
           changes.hasNext();) {
        OpConInfoRelay myRelay = (OpConInfoRelay) changes.next();
        //We don't want to pick up changes that we ourselves put on this relay 
        // i.e. when we are the target
        //We only want to get the relay response here when we are the
        // source.
        TimeSpan myContent = (TimeSpan) myRelay.getContent();
        ArrayList theResponse = (ArrayList) myRelay.getResponse();
        String opConItemId = (String)theResponse.get(0);
        String opConTypeId = (String)theResponse.get(1);
        if (myLogger.isInfoEnabled()) {
          myLogger.info(getAgentIdentifier() +" Response from relay is:" +theResponse);
        }
        MessageAddress maTarget = myRelay.getTarget();
        String relTargetAddr = (String)maTarget.getAddress();
        
        Asset otherAsset =
          getAsset(myAssetClassName, opConItemId, opConTypeId, relTargetAddr);
        Relationship relationship = 
          myPlanningFactory.newRelationship((Constants.Role.OPERATIONALSUBORDINATE),
                                            (HasRelationships) myLocalAsset,
                                            (HasRelationships) otherAsset,
                                            myContent.getStartTime(),
                                            myContent.getEndTime());
        report(relationship);
      }
  }

  protected void addRelationship(String typeId, String itemId,
                                 String otherClusterId, String roleName,
                                 long start, long end) {
    super.addRelationship(typeId, itemId,
			  otherClusterId, roleName,
			  start, end);

    if (myLogger.isInfoEnabled()) {
      myLogger.info(getAgentIdentifier() + ": added relationship " +
		    " other asser typeid = " + typeId + 
		    ", itemId = " + itemId +
		    ", clusterId = " + otherClusterId +
		    ", role = " + roleName + 
		    ", start = " + start + 
		    ", end = " + end);
    }

    if (roleName.equals(org.cougaar.glm.ldm.Constants.Role.ADMINISTRATIVESUBORDINATE.toString())) {
      if (myLogger.isInfoEnabled()) {
	myLogger.info(getAgentIdentifier() + ": adding OPCON relationship ");
      }
    

      super.addRelationship(typeId, itemId, otherClusterId, 
			    org.cougaar.glm.ldm.Constants.Role.OPERATIONALSUBORDINATE.toString(),
			    start, end);
    }
  }

  private class RFDInfo implements org.cougaar.util.TimeSpan {
    Task myTask;
    long myStart;
    long myEnd;
    String myOpSuperior;

    RFDInfo(Task t) {
      myTask = t;
      myStart = TaskUtils.getStartTime(t);
      myEnd = TaskUtils.getEndTime(t);
      setMyOpSuperior(t);
    }
    
    private void setMyOpSuperior(Task t) {
      PrepositionalPhrase pp = 
        t.getPrepositionalPhrase(org.cougaar.planning
                                 .Constants.Preposition.FOR);
      myOpSuperior = ((Asset)pp.getIndirectObject()).getItemIdentificationPG().getNomenclature();
      
    }

    public String getMyOpSuperior() {
      return myOpSuperior;
    }

    public long getStartTime() {
      return myStart;
    }
    
    public long getEndTime() {
      return myEnd;
    }
    
    public Task getTask() {
      return myTask;
    }
  }

}
