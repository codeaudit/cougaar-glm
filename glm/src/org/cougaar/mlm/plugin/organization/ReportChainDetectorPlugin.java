/*
 * <copyright>
 *  Copyright 1997-2004 BBNT Solutions, LLC
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

package org.cougaar.mlm.plugin.organization;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.core.mts.MessageAddress;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.EventService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.UIDService;

import org.cougaar.glm.ldm.asset.Organization;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.ClusterPG;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.predicate.TaskPredicate;

/**
 * Plugin for noticing when SUPERIOR/SUBORDINATE report relationship
 * chain is complete for whole society.
 * This Plugin goes in every agent in the chain, with an argument
 * indicating the expected number of subordinates.
 * Each agent relays to its superior when all of its subordinates
 * have reported in with the relay. At the head of the report chain,
 * a Cougaar Event is published when the chain is complete.
 * This can be used to signal when it is safe to send Tasks
 * that must reach every agent in the report chain.
 * See, for example, GLSInitServlet.
 **/
public class ReportChainDetectorPlugin
  extends ComponentPlugin
{
  private LoggingService logSvc = null;
  private UIDService uidService = null;
  private EventService evtSvc = null;
  
  private int numSubs = 0;
  private ReportChainState reportChainState = null;
  
  private IncrementalSubscription readySub = null;
  private IncrementalSubscription dutySub = null;
  private IncrementalSubscription stateSub = null;
  private IncrementalSubscription selfOrgSub = null;
  
  // Subscribe to relays indicating subordinates are ready
  private class ReadyPredicate
    implements UnaryPredicate
  {
    public ReadyPredicate() { }
    
    public boolean execute( Object obj ) {
      return (obj instanceof ReportChainReadyRelay );
    }
  }
  
  // Get the ReportForDuty task where this Agent is reporting to its Superior
  // -- not its SupportSuperior
  private class DutyPredicate
    extends TaskPredicate
  {
    public DutyPredicate() { super(); }
    public boolean execute( Task task ) {
      // Change this to get on Superior, not SupportSuperior
      if (task.getVerb().equals( Verb.get("ReportForDuty") )) {
	// Look at the roles in the AS prepositional phrase. That's
	// what's used to construct that the AssetTransfer.
	PrepositionalPhrase pp = 
	  task.getPrepositionalPhrase(org.cougaar.planning
				      .Constants.Preposition.AS);
	if (pp == null)
	  return false;
	Collection roles = (Collection) pp.getIndirectObject();
	if (roles == null)
	  return false;
	// This collection should contain "Subordinate"
	return roles.contains(org.cougaar.glm.ldm.Constants.Role.SUBORDINATE);
      }
      return false;
    }
  }
  
  // Subscribe the the ReportChainState objects
  private class ReportChainStatePredicate
    implements UnaryPredicate
  {
    public ReportChainStatePredicate() { }
    
    public boolean execute( Object obj ) {
      return (obj instanceof ReportChainState);
    }
  }
    
  /**
   * The predicate for the Self org subscription
   **/
  private static class SelfOrgAssetPredicate implements UnaryPredicate {
    public SelfOrgAssetPredicate() { }
  
    public boolean execute(Object o) {
      
      if (o instanceof Organization) {
	Organization org = (Organization) o;
	return org.isLocal();
      }
      return false;
    }
  };

  // Get the various services
  public void setLoggingService( LoggingService logSvc ) {
    this.logSvc = logSvc;
  }
  
  public void setEventService( EventService evtSvc ) {
    this.evtSvc = evtSvc;
  }
  
  public void setUIDService( UIDService uidService ) {
    this.uidService = uidService;
  }
  
  private void debug(String msg) {
    if (logSvc.isDebugEnabled()) {
      logSvc.debug("ReportChainDetectorPlugin: " + getSelf() + ": " + msg);	
    }	
  }

  public void setupSubscriptions() {
    readySub = (IncrementalSubscription)
      getBlackboardService().subscribe( new ReadyPredicate() );
    dutySub = (IncrementalSubscription)
      getBlackboardService().subscribe( new DutyPredicate() );
    stateSub = (IncrementalSubscription)
      getBlackboardService().subscribe( new ReportChainStatePredicate() );
    selfOrgSub = (IncrementalSubscription) 
      getBlackboardService().subscribe( new SelfOrgAssetPredicate());    

    List params = (List) getParameters();
    numSubs = Integer.parseInt( params.get(0).toString() );
    debug("init " + params.get(0) + " subs");

    // Initialize reportChainState
    getState();
  }

  public void execute() {

    // Check whether we're ready to send ReportChainReadyRelay
    if (!getState().isChainReady()) {
      // First handle ReadyForOplan
      // If more people reported in, or we just reported in, now might
      // be ready to sendReportChainReady
      if ((dutySub.getAddedCollection().size() > 0) ||
	  (readySub.getAddedCollection().size() > 0) ||
	  (selfOrgSub.hasChanged()) ) {
	
	// Don't get the collections unless we logging debug level messages
	if (logSvc.isDebugEnabled()) {
	  if (dutySub.getAddedCollection().size() > 0) 
	    debug("ReportForDuty seen.");
	  if (readySub.getAddedCollection().size() > 0)
	    debug("ReportChainReadyRelay received.");
	  if (selfOrgSub.hasChanged()) {
	    debug("Self Org has changed.");
	  }
	}

	if (isChainReady()) {
	  sendReportChainReady();
	}
      }
    }
  }
  
  public void log( String message ) {
    if (logSvc.isInfoEnabled())
      logSvc.info( message );
  }
  
  public void event( String type, String message ) {
    if (evtSvc != null) {
      if (evtSvc.isEventEnabled()) {
	evtSvc.event("[" + type + "] " + message);
      }
    }    	
  }
  
  // Is this the top of the report chain?
  public boolean isChainHead() {
    //    boolean nameSaysHead = getSelf().toString().equals("NCA") || getSelf().toString().equals("OSD.GOV");
    // the above is the non-general solution.

    // The OrgDataPlugin creates the ReportForDuty tasks,
    // and that happens when that plugin loads.
    // So there is no room for timing problems if we just look
    // for those tasks. Note of course that there are 2 kinds of RFD
    // tasks. Superior & SupportSuperior. I want Superior - although
    // NCA should have neither.

    // A naive solution had been to look for the Superior relationship
    // on the selfOrg. The problem with that is that 
    // the superior relationship happens only after the OrgReportPlugin
    // closes its first transaction (allowing an LP to run).
    // So an agent with no subs might have this plugin run before
    // the OrgReportPlugin, and therefore be done with its
    // subs before it has created its superior relationships,
    // so it might think it was the Chain Head, so it
    // sends the event, and never reports to its superior,
    // so the real head never reports it is done.

    // So correct logic is to return false if have no self org,
    // false if have a ReportForDuty to a SUPERIOR,
    // false if have a relationship with a superior, otherwise true
    return !hasSuperior();
  }
  
  // Does this agent have a superior organization?
  // If the selfOrg is not here yet, assume that it does.
  // If have a ReportForDuty, then it does.
  // In other words, only return false if the selfOrg is there,
  // and there is no ReportForDuty task and no SUPERIOR relationship
  private boolean hasSuperior() {
    Organization selfOrg = (Organization) selfOrgSub.first();

    if (selfOrg == null) {
      // If we dont yet have a self org, assume were going
      // to have a superior later, to avoid being the one with no superior
      debug("No selfOrg");
      return true;
    }

    // If there is a ReportForDuty task that points to someone,
    // that someone is a superior
    if (getSuperior() != null) {
      debug("getSuperior not null");
      return true;
    } else {
      int rfds = dutySub.size();
      if (rfds > 0) {
	debug("getSuperior is null. RFD sub shows " + rfds + ".");
	return true;
      }
    }

    // This next is true once the LP sends the RFD task
    Collection superiorRelationships = 
      selfOrg.getRelationshipSchedule().getMatchingRelationships(org.cougaar.glm.ldm.Constants.Role.SUPERIOR); 
    if (superiorRelationships.size() == 0) {
      debug("No superior relationships");
      return false;
    }

    return true;
  }

  // If all our subordinates have reported they are ready, and either
  // we have created the ReportForDuty task or we have no superior (and need none),
  // then the report chain is ready for tasks
  public boolean isChainReady() {
    Organization selfOrg = (Organization) selfOrgSub.first();

    if (selfOrg == null) {
      return false;
    }

    Collection subordinateRelationships = 
      selfOrg.getRelationshipSchedule().getMatchingRelationships(org.cougaar.glm.ldm.Constants.Role.SUBORDINATE);

    // Return true iff
    //   a) my RelationshipSchedule has the expected number of subordinates
    //   b) all my subs have reported (via ReportChainReadyRelay) that all their subs 
    //   have reported to them
    if ((subordinateRelationships.size() == numSubs) &&
	(readySub.size() == numSubs ) &&
	((getSuperior() != null) || isChainHead() ))
      {
	return true;
      } else {
	debug("Report Chain Not Ready: " + readySub.size() + " reporting.");
	return false;
      }
  }
  
  // Get the Current report state object from the BBoard (create if necc)
  public ReportChainState getState() {
    if (reportChainState == null) {
      if (stateSub.size() == 0) {
	ReportChainState RC = new ReportChainState( uidService.nextUID() );
	getBlackboardService().publishAdd( RC );
	reportChainState = RC;
      } else {
	Iterator states = stateSub.iterator();
	reportChainState = (ReportChainState) states.next();
      }
    }

    return reportChainState;
  }

  public MessageAddress getSelf() {
    return getAgentIdentifier();
  }
  
  // Get the address for our superior
  private MessageAddress superior = null;

  // Use the ReportForDuty task we subscribed to -- just grab the direct object
  public MessageAddress getSuperior() {
    if (superior != null)
      return superior;

    if (dutySub.size() == 0) {
      debug("No ReportForDuty found.");
      return null;
    }
    
    // FIXME: There are 2 kinds of superiors. I only want the SUPERIOR,
    // not the SupportSuperior (ADMINISTRATIVE_SUPERIOR?)

    // Question: Is it possible for the RFD task to be there, but
    // for any of these other "return null" escapes to happen, without
    // a broken RFD task?
    // Put another way: Is it possible for this getSuperior() task to 
    // return null for any agent that has a superior, if no error occurs?

    Iterator sups = dutySub.iterator();
    Task dutyT = (Task) sups.next();
    if (dutyT == null) return null;
    PrepositionalPhrase pp = dutyT.getPrepositionalPhrase(org.cougaar.planning.Constants.Preposition.FOR);
    if (pp == null)
      return null;
    Asset a = (Asset) pp.getIndirectObject();
    if (a == null)
      return null;
    ClusterPG cpg = a.getClusterPG();
    if (cpg == null)
      return null;
    return cpg.getMessageAddress();
  }
  
  // Send a relay to our Superior indicating when our piece of the report
  // chain is ready, ie we're ready for Tasks
  public void sendReportChainReady() {
    ReportChainState state = getState();
    if ((getSuperior() == null) && !isChainHead()) {
      // We should have a superior to report in to, but don't have it yet
      return;
    }
    
    //  If we have not already transitioned to ReportChainReady state (ie dont do this twice)
    if (!state.isChainReady()) {
      state.setChainReady( true );
      getBlackboardService().publishChange( state );
      
      debug("Report Chain Ready.");
      
      // Head of report chain (ie NCA) sends a CougaarEvent
      // Indicating entire society has created report chain
      if (isChainHead()) {
	event( "STATUS", "ReportChainDetectorPlugin: Report Chain Ready");
      }
      
      // Agent with a superior sends a relay to the superior
      if (!isChainHead()) {
	ReportChainReadyRelay opr =
	  new ReportChainReadyRelay( uidService.nextUID(),
			   getSelf(),
			   getSuperior(),
			   getSelf() );
	getBlackboardService().publishAdd( opr );
      }
    }
  }
}


