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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.util.UID;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.oplan.OrgActivityImpl;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.plugins.TaskUtils;

import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;


import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.plugin.asset.AssetDataPlugin;

import org.cougaar.util.MutableTimeSpan;
import org.cougaar.util.NonOverlappingTimeSpanSet;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.TimeSpanSet;
import org.cougaar.util.UnaryPredicate;

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


  IncrementalSubscription myOplanSubscription;
  IncrementalSubscription myOrgActivitySubscription;
  IncrementalSubscription myOpConInfoRelaySubscription;
  IncrementalSubscription myRFDSubscription;
  IncrementalSubscription myOrganizationSubscription;

  MessageAddress myAgentAddr;

  // report for duty tasks
  protected UnaryPredicate myRFDPredicate =  new UnaryPredicate() {
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
	  if (pp == null) {
	    return false;
	  }

	  Collection roles = (Collection) pp.getIndirectObject();
	  
	  if (roles == null) {
	    return false;
	  } else {
	    // This collection should contain "Subordinate"
	    return roles.contains(org.cougaar.glm.ldm.Constants.Role.OPERATIONALSUBORDINATE);
	  }
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
    String orgId_;
    public OrgActivitiesPredicate(UID uid, String orgId) {
      oplanUID_ = uid;
      orgId_ = orgId;
    }
    
    public boolean execute(Object o) {
      if (o instanceof OrgActivity) {
	if (oplanUID_.equals(((OrgActivity)o).getOplanUID()) && orgId_.equals(((OrgActivity)o).getOrgID())) {
	  return true;
	}
      }
      return false;
    }
  }

  private UnaryPredicate myOpConInfoRelayPredicate = new UnaryPredicate() {
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

  private static class OrganizationPredicate implements UnaryPredicate {
    public boolean execute(Object o) {
      return o instanceof Organization;
    }
  }


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

    myOplanSubscription = 
      (IncrementalSubscription) getBlackboardService().subscribe(new OplanPredicate());
    myRFDSubscription = 
      (IncrementalSubscription) getBlackboardService().subscribe(myRFDPredicate);
    myOpConInfoRelaySubscription = 
      (IncrementalSubscription) getBlackboardService().subscribe(myOpConInfoRelayPredicate);

    myOrganizationSubscription =       
      (IncrementalSubscription) getBlackboardService().subscribe(new OrganizationPredicate());
  }
  

  public void execute() {
    if (myOplanSubscription.hasChanged()) {

      for (Iterator iterator = myOplanSubscription.getAddedCollection().iterator();
	   iterator.hasNext();) {
	Oplan oplan = (Oplan)iterator.next();
	UID oplanUID = oplan.getUID();
	if (myOrgActivitySubscription == null) {
	  myOrgActivitySubscription = (IncrementalSubscription)
	    getBlackboardService().subscribe(new OrgActivitiesPredicate(oplanUID, getSelfOrg().getMessageAddress().toString()));
	}
      }
    }

    if ((myOrgActivitySubscription != null) && 
	(myOrgActivitySubscription.hasChanged())) {
      processPotentialChangedOpCons();
    }

    if (myOpConInfoRelaySubscription.hasChanged()) {
      if (myLogger.isInfoEnabled()) {
        myLogger.info("myOpConInfoRelaySubscription has changed!");
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
    NonOverlappingTimeSpanSet currentRFDInfos = 
      buildRFDInfos(myRFDSubscription);
    TimeSpanSet addedRFDInfos = new TimeSpanSet();
    TimeSpanSet removedRFDInfos = new TimeSpanSet();

    Collection opConInfoRelays = new ArrayList();

    myLogger.shout("Creating timeSpanSet with OASub: " + myOrgActivitySubscription);
    NonOverlappingTimeSpanSet orgActivities = 
      new NonOverlappingTimeSpanSet(myOrgActivitySubscription);
    NonOverlappingTimeSpanSet opconInfos = 
      buildOpConInfos(orgActivities);


    for (Iterator iterator = opconInfos.iterator(); 
	 iterator.hasNext(); ) {
      OpConInfo opconInfo = (OpConInfo) iterator.next();

      Collection affectedRFDInfos = currentRFDInfos.intersectingSet(opconInfo);

      TimeSpanSet rfdSchedule = new TimeSpanSet(affectedRFDInfos);

      boolean covered = false;
      for (Iterator rfdIterator = rfdSchedule.iterator();
	   rfdIterator.hasNext();) {
	RFDInfo existingRFDInfo = (RFDInfo) rfdIterator.next();

	// Check that RFDs map to opCon/Start/End of org activity.
	// If different, remove and generate new.
	// DON'T TRY TO BE CLEVER
	if (covered) {
	  removedRFDInfos.add(existingRFDInfo);
	} else if ((existingRFDInfo.getOpSuperior().equals(opconInfo.getOpCon())) &&
		   (existingRFDInfo.getStartTime() == opconInfo.getStartTime()) &&
		   (existingRFDInfo.getEndTime() == opconInfo.getEndTime())) {
	  covered = true;
	} else {
	  if (myLogger.isInfoEnabled()) {
	    myLogger.info("opconInfo - " + opconInfo + 
			  " does not match overlapping RFD - " +
			  existingRFDInfo.getTask());
	  }
	  
	  // Need to make a new rfd for this timespan
	  // Don't know where it will end up,
	  //  but it can't be here anymore
	  removedRFDInfos.add(existingRFDInfo);

	  // May not be able to make the new RFD task but no point in 
	  // generating additional work
	  covered = true;

	    // Look for local version of OpCon asset
	  Organization localOpCon = findLocalOrganization(opconInfo.getOpCon());
	  
	  // If no local version, send an OpConInfoRelay to agent. Will be
	  // able to build a local version of the asset with the relay 
	  // response
	  if (localOpCon == null) {
	    boolean relayExists = false;
	    
	    // Check whether I've already created a relay to this OpCon in
	    // the current execute cycle
	    for (Iterator relayIterator = opConInfoRelays.iterator();
		 relayIterator.hasNext();) {
	      OpConInfoRelay addedRelay = 
		(OpConInfoRelay) relayIterator.next();
	      if (addedRelay.getTarget().equals(MessageAddress.getMessageAddress(opconInfo.getOpCon()))) {
		relayExists = true;
		break;
	      }
	    }
	    
	    // Have not created a relay so create/publish one
	    if (!relayExists) {
	      OpConInfoRelay opConRelay = createOpConInfoRelay(opconInfo.getOpCon());
	      getBlackboardService().publishAdd(opConRelay);
	      opConInfoRelays.add(opConRelay);
	    }
	  } else {
	    // Have a local copy of the OpCon asset so I can go ahead and
	    // make the corresponding RFDs
	    
	    Task rfdTask = createOpConRFD(localOpCon, 
					  opconInfo.getStartTime(), 
					  opconInfo.getEndTime()); 
	    addedRFDInfos.add(new RFDInfo(rfdTask));
	    if (myLogger.isInfoEnabled()) {
	      myLogger.info("New is:" + rfdTask);
	    }
	  }
	}
      } // loop over rfds
    } // loop over opconinfos

    if (myLogger.isInfoEnabled())
      myLogger.info("processPotentialChangedOpCons: adding  " + 
		    addedRFDInfos.size() + " RFDs , removing " +
		    removedRFDInfos.size() + " RFDs.");


    for (Iterator j = removedRFDInfos.iterator(); j.hasNext(); ) {
      Task rfd = ((RFDInfo) j.next()).getTask();
      getBlackboardService().publishRemove(rfd);

      if (myLogger.isInfoEnabled())
	myLogger.info("Removing RFD: " + rfd);
    }
    
    
    for (Iterator j = addedRFDInfos.iterator(); j.hasNext(); ) {
      Task rfd = ((RFDInfo) j.next()).getTask();
      getBlackboardService().publishAdd(rfd);

      if (myLogger.isInfoEnabled())
	myLogger.info("Adding RFD: " + rfd);
    }
  }


  protected void addRelationship(String typeId, String itemId,
                                 String otherAgentId, String roleName,
                                 long start, long end) {
    super.addRelationship(typeId, itemId,
			  otherAgentId, roleName,
			  start, end);

    if (myLogger.isInfoEnabled()) {
      myLogger.info("added relationship " +
		    " other asset typeid = " + typeId + 
		    ", itemId = " + itemId +
		    ", agentId = " + otherAgentId +
		    ", role = " + roleName + 
		    ", start = " + start + 
		    ", end = " + end);
    }

    if (roleName.equals(Constants.Role.ADMINISTRATIVESUBORDINATE.toString())) {
      if (myLogger.isInfoEnabled()) {
	myLogger.info("adding OPCON relationship ");
      }
    

      super.addRelationship(typeId, itemId, otherAgentId, 
			    Constants.Role.OPERATIONALSUBORDINATE.toString(),
			    start, end);
    }
  }

  private Task createOpConRFD(Asset opCon, long startTime, long endTime) {
    if (myLogger.isInfoEnabled()) {
      myLogger.info("creating an RFD from " + myAgentAddr + 
		    " to "  + opCon);
    }
    
    Asset localClone = myPlanningFactory.cloneInstance(myLocalAsset);
    Asset sendTo = myPlanningFactory.cloneInstance(opCon);
    
    Collection roles = new ArrayList();
    roles.add(org.cougaar.glm.ldm.Constants.Role.OPERATIONALSUBORDINATE);
    
    Task rfdTask = createReportTask(localClone, sendTo, roles, startTime, 
				    endTime);
    return rfdTask;
  }

  private OpConInfoRelay createOpConInfoRelay(String opCon) {
    //sending a relay to the opcon from the orgActivity
    MessageAddress targetAddr = 
      MessageAddress.getMessageAddress(opCon);
    OpConInfoRelay opconRelay = 
      new OpConInfoRelay(getUIDService().nextUID(),
			 myAgentAddr,
			 targetAddr,
			 null,
			 null);
    if (myLogger.isInfoEnabled()) {
      myLogger.info("just created an OpConInfoRelay: " + 
		    opconRelay.toString()
		    + " from " + myAgentAddr + " to "  + targetAddr);
    }
    
    return opconRelay;
  }

  private void createOperationalRFDs (Collection changedOpConInfoRelays) {
    HashMap relayMap = new HashMap();

    NonOverlappingTimeSpanSet rfdInfos = buildRFDInfos(myRFDSubscription);

    NonOverlappingTimeSpanSet orgActivities = 
      new NonOverlappingTimeSpanSet(myOrgActivitySubscription);
    NonOverlappingTimeSpanSet opconInfos =  buildOpConInfos(orgActivities);


    for (Iterator changes = changedOpConInfoRelays.iterator();
	 changes.hasNext();) {
      OpConInfoRelay relay = (OpConInfoRelay) changes.next();
      
      TimeSpan content = (TimeSpan) relay.getContent();
      ArrayList response = (ArrayList) relay.getResponse();

      if (myLogger.isInfoEnabled()) {
	myLogger.info("OpConInfoRelay content = " + content +
		      " response = " + response);
      }      

      if ((response == null) || 
	  (response.size() < 2)) {
	if (myLogger.isDebugEnabled()) {
	  myLogger.debug("ignoring invalid response.");
	}
      } else {
	String opConItemId = (String)response.get(0);
	String opConTypeId = (String)response.get(1);

	if (relayMap.containsKey(opConItemId)) {
	  if (myLogger.isInfoEnabled()) {
	    myLogger.info("ignoring OpConInfoRelay " + relay +
			  " already added required RFDs.");
	  }
	} else {
	  relayMap.put(opConItemId, relay);
	  boolean usedRelay = false;
	  

	  // Iterate over OrgActivities to see whether there are RFDs for 
	  // OpCons specified by the OrgActivities.
	  for (Iterator iterator = opconInfos.iterator(); 
	       iterator.hasNext(); ) {
	    OpConInfo opconInfo = (OpConInfo) iterator.next();
	    
	    // Only interested if OpCon is the same. Trust that OpConInfoRelays
	    // have been sent out for other missing OpCons.
	    if (opconInfo.getOpCon().equals(opConItemId)) {
	      // Is time span already covered?
	      Collection affectedRFDInfos = 
		rfdInfos.intersectingSet(opconInfo);
	      
	      if (!(affectedRFDInfos.isEmpty())) {
		// Existence of any RFDs means that we had the info to make
		// all RFDs to this OpCon. Let processPotentialChangedOpCons()
		// ensure that entire time period is covered.
		continue;
	      }

	      // Make RFD
	      MessageAddress maTarget = relay.getTarget();
	      String relTargetAddr = (String)maTarget.getAddress();
	      
	      Asset otherAsset = getAsset(myAssetClassName, 
					  opConItemId, 
					  opConTypeId, 
					  maTarget.getAddress());
	      Relationship relationship = 
		myPlanningFactory.newRelationship((Constants.Role.OPERATIONALSUBORDINATE),
						  (HasRelationships) myLocalAsset,
						  (HasRelationships) otherAsset,
						  opconInfo.getStartTime(),
						  opconInfo.getEndTime());
	      report(relationship);
	      
	      if (myLogger.isInfoEnabled()) {
		myLogger.info(" published RFD to " +
			      maTarget + " for " + 
			      new Date(opconInfo.getStartTime()) + " to " +
			      new Date(opconInfo.getEndTime()));
	      }
	      
	      usedRelay = true;
	    }
	  }

	  if ((!usedRelay) && (myLogger.isInfoEnabled())) {
	    myLogger.info("ignoring OpConInfoRelay " + relay + 
			  " required RFDs already exist.");
	  }
	}
      }
      // General clean up
      publishRemove(relay);
    } // loop over changed opconinfo relays
  }
  

  private Organization findLocalOrganization(String itemID) {
    // Find matching Organization 
    for (Iterator iterator = myOrganizationSubscription.iterator();
	 iterator.hasNext();) {
      Organization organization = (Organization) iterator.next();
      if (organization.getItemIdentificationPG().getItemIdentification().equals(itemID)) {
	return organization;
      }
    }

    return null;
  }

  private Organization getSelfOrg() {
    // Find matching Organization 
    for (Iterator iterator = myOrganizationSubscription.iterator();
	 iterator.hasNext();) {
      Organization organization = (Organization) iterator.next();
      if (organization.isSelf()) {
	return organization;
      }
    }

    return null;
  }

  private NonOverlappingTimeSpanSet buildRFDInfos(Collection rfdTasks) {
	  
    NonOverlappingTimeSpanSet rfdInfos = new NonOverlappingTimeSpanSet();

    // Go thru current set of RFD tasks and extract the start
    //  and end times as well as the operational superior
    // Make RFDInfo objects to be compared against
    for (Iterator it = rfdTasks.iterator(); it.hasNext(); ) {
      Task rfdTask = (Task) it.next();
      
      RFDInfo rfdInfo = new RFDInfo(rfdTask);
      rfdInfos.add(rfdInfo);
    }
	
    return rfdInfos;
  }

  private Relationship getAdConRelationship() {
    // Depends on the assumption that an Organization ALWAYS has an 
    // AdCon and that AdCon does not change.
    Organization selfOrg = getSelfOrg();
    if (selfOrg == null) {
      // ERROR - should never have OrgActivities w/o a self org.
    } else {
      RelationshipSchedule relationshipSchedule = 
	selfOrg.getRelationshipSchedule();
      Collection relationships = 
	relationshipSchedule.getMatchingRelationships(Constants.Role.ADMINISTRATIVESUPERIOR);
      if (relationships.isEmpty()) {
	if (myLogger.isWarnEnabled())
	  myLogger.warn("getAdConRelationship found no AdministrativeSuperior!");
      } else if (relationships.size() != 1) {
	myLogger.error("getAdConRelationship - " +
		       " found multiple administrative superiors " +
		       relationships + " choice is random.");
      }      

      for (Iterator relationshipIterator = relationships.iterator();
	   relationshipIterator.hasNext();) {
	return (Relationship) relationshipIterator.next();
      } 
    }
    return null;
  }

  private void handlePreOrgActivityOpCon(Relationship adConRelationship, 
					 TimeSpanSet orgActivities,
					 TimeSpanSet currentRFDInfos, 
					 TimeSpanSet addedRFDInfos,
					 TimeSpanSet removedRFDInfos) {
  

    Organization adCon = null;

    if (adConRelationship != null) {
      adCon = 
        (Organization) getSelfOrg().getRelationshipSchedule().getOther(adConRelationship);
    }
    boolean covered = false;

    TimeSpan firstOrgActivity = (TimeSpan) orgActivities.first();

    // Before OrgActivities
    for (Iterator iterator = currentRFDInfos.iterator();
	 iterator.hasNext();) {
      RFDInfo rfdInfo = (RFDInfo) iterator.next();
	  
      if (rfdInfo.getEndTime() <= firstOrgActivity.getStartTime()) {
	// Verify that OpCon == AdCon
	// Want a single default OpCon which starts with the AdCon and ends 
	// when the OrgActivities start
        if (adCon == null) {
	  removedRFDInfos.add(rfdInfo); 
        } else if ((adCon.getItemIdentificationPG().getItemIdentification().equals(rfdInfo.getOpSuperior())) && 
		   (rfdInfo.getStartTime() == adConRelationship.getStartTime()) ||
		   (rfdInfo.getEndTime() == firstOrgActivity.getStartTime())) {
	  covered = true;
	} else {
	  removedRFDInfos.add(rfdInfo);
	}
      } else {
	// moved into time period covered by org activities
	break;
      }
    }
	  
    if (!covered) {
      Task rfdTask = createOpConRFD(adCon, 
				    adConRelationship.getStartTime(), 
				    firstOrgActivity.getStartTime());
      addedRFDInfos.add(new RFDInfo(rfdTask));
    }
  }


  private void handlePostOrgActivityOpCon(Relationship adConRelationship, 
					  TimeSpanSet orgActivities,
					  TimeSpanSet currentRFDInfos, 
					  TimeSpanSet addedRFDInfos,
					  TimeSpanSet removedRFDInfos) {

    Organization adCon = null;

    if (adConRelationship != null) {
      adCon = 
        (Organization) getSelfOrg().getRelationshipSchedule().getOther(adConRelationship);
    }
    boolean covered = false;

    TimeSpan lastOrgActivity = (TimeSpan) orgActivities.last();

    for (Iterator iterator = currentRFDInfos.iterator();
	 iterator.hasNext();) {
      RFDInfo rfdInfo = (RFDInfo) iterator.next();
      
      if (rfdInfo.getStartTime() >= lastOrgActivity.getEndTime()) {
	// Verify that OpCon == AdCon
	// Want a single default opCon which starts when Org Activities end and
	// continues to the same end date as the AdCon
        if (adCon == null) {
	  removedRFDInfos.add(rfdInfo);
        } else if ((adCon.getItemIdentificationPG().getItemIdentification().equals(rfdInfo.getOpSuperior())) || 
		   (rfdInfo.getStartTime() == lastOrgActivity.getEndTime()) ||
		   (rfdInfo.getEndTime() == adConRelationship.getEndTime())) {
	  covered = true;
	} else {
	  removedRFDInfos.add(rfdInfo);
	}
      }
    }

    if (!covered) {
      Task rfdTask = createOpConRFD(adCon, 
				    lastOrgActivity.getEndTime(),
				    adConRelationship.getEndTime());
      addedRFDInfos.add(new RFDInfo(rfdTask));
    }
  }

  private NonOverlappingTimeSpanSet buildOpConInfos(NonOverlappingTimeSpanSet orgActivities) {
    NonOverlappingTimeSpanSet opconInfos = new NonOverlappingTimeSpanSet();
    String currentOpCon = null;
    

    Relationship adConRelationship = getAdConRelationship();
    Organization adCon = null;

    if (adConRelationship == null) {
      // FIXME: Not an error for the older style oplan.xml societies
      myLogger.error("buildOpConInfos - no AdConRelationship.");
      return opconInfos;
    } else {
      adCon = 
        (Organization) getSelfOrg().getRelationshipSchedule().getOther(adConRelationship);
    
      if (myLogger.isDebugEnabled()) {
	myLogger.debug("adConRelationship = " + adConRelationship);
      }
    }
    String adconName = 
      adCon.getItemIdentificationPG().getItemIdentification();

    String opconName;
    long startTime;
    long endTime;

    if (orgActivities.isEmpty()) {
      if (adCon != null) {
	OpConInfo adconInfo = 
	  new OpConInfo(adconName,
			adConRelationship.getStartTime(),
			adConRelationship.getEndTime());
	opconInfos.add(adconInfo);
      }
      return opconInfos;
    }
    

    OrgActivity firstOrgActivity = (OrgActivity) orgActivities.first();

    if ((adCon!= null) &&
	(firstOrgActivity.getStartTime() > 
	 adConRelationship.getStartTime())) {
      // Start with adcon Info
      opconName = adconName;
      startTime = adConRelationship.getStartTime();
      endTime = adConRelationship.getEndTime();
    } else {
      opconName = firstOrgActivity.getOpCon();
      startTime = firstOrgActivity.getStartTime();
      endTime = firstOrgActivity.getEndTime();
    }

      
    for (Iterator iterator = orgActivities.iterator();
	 iterator.hasNext();) {
      OrgActivity orgActivity = (OrgActivity) iterator.next();

      if (!orgActivity.getOpCon().equals(opconName)) {
	OpConInfo opConInfo = new OpConInfo(opconName, startTime, 
					    endTime);
	opconInfos.add(opConInfo);

	opconName = orgActivity.getOpCon();
	startTime = orgActivity.getStartTime();
      } 
	
      endTime = orgActivity.getEndTime(); 
    }

    if ((adCon == null) || 
	(adConRelationship.getEndTime() < endTime)) {
	OpConInfo opConInfo = new OpConInfo(opconName, startTime, 
					    endTime);
	opconInfos.add(opConInfo);
    } else {
      if (!opconName.equals(adconName)) {
	OpConInfo opConInfo = new OpConInfo(opconName, startTime, 
					    endTime);
	opconInfos.add(opConInfo);
	startTime = endTime;
      } 
      
      OpConInfo opConInfo = new OpConInfo(adconName, startTime, 
					  adConRelationship.getEndTime());
      opconInfos.add(opConInfo);
    }
    
    if (myLogger.isDebugEnabled()) {
      myLogger.debug("buildOpConInfos returning  " + 
		     opconInfos);
    }

    return opconInfos;
  }

    
  private static class OpConInfo extends MutableTimeSpan {
    String myOpConName = null;

    public OpConInfo(String opConName, TimeSpan timeSpan) {
      this(opConName, timeSpan.getStartTime(), timeSpan.getEndTime());
    }

    public OpConInfo(String opConName, long startTime, long endTime) {
      super();

      setTimeSpan(startTime, endTime);
      myOpConName = opConName;
    }

    public String getOpCon() {
      return myOpConName;
    }

    public boolean equals(Object o) {
      if (o instanceof OpConInfo) {
	OpConInfo opConInfo = (OpConInfo) o;
	return ((opConInfo.getStartTime() == getStartTime()) &&
		(opConInfo.getEndTime() == getEndTime()) &&
		(opConInfo.getOpCon().equals(getOpCon())));
      } else {
	return false;
      }
    }

    public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append("start=" + new Date(getStartTime()) +
		 ", end=" + new Date(getEndTime()));
      
      buf.append(", opCon=" + myOpConName);
      buf.append("]");
      
      return buf.toString();
    }
  }

  private static class RFDInfo implements org.cougaar.util.TimeSpan {
    Task myTask;
    long myStart;
    long myEnd;
    String myOpSuperior;

    RFDInfo(Task t) {
      myTask = t;
      myStart = TaskUtils.getStartTime(t);
      myEnd = TaskUtils.getEndTime(t);
      setOpSuperior(t);
    }
    
    private void setOpSuperior(Task t) {
      PrepositionalPhrase pp = 
        t.getPrepositionalPhrase(org.cougaar.planning
                                 .Constants.Preposition.FOR);
      myOpSuperior = ((Asset)pp.getIndirectObject()).getItemIdentificationPG().getItemIdentification();
    }

    public String getOpSuperior() {
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







