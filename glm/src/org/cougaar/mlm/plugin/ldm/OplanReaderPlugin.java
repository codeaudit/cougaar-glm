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

package org.cougaar.mlm.plugin.ldm;

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.logging.LoggingServiceWithPrefix;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.DomainService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OplanFactory;
import org.cougaar.glm.ldm.oplan.OplanStage;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.oplan.OrgActivityImpl;
import org.cougaar.glm.ldm.oplan.TimeSpan;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.mlm.plugin.organization.GLSConstants;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.ContextOfOplanIds;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.util.CSVUtility;
import org.cougaar.util.DBProperties;
import org.cougaar.util.TimeSpanSet;
import org.cougaar.util.UnaryPredicate;

public class OplanReaderPlugin extends ComponentPlugin implements GLSConstants {
  public static final String QUERY_FILE = "oplan.q";
  public static final String OPLAN_ID_PARAMETER = ":oplanid:";
  public static final String OPLAN_STAGE_PARAMETER = ":oplanStage:";
  public static final String AGENT_PARAMETER = ":agent:";

  private static final String OPLAN_QUERY_HANDLER = "oplanInfoQuery.handler";
  private static final String ACTIVE_STAGES_HANDLER = "activeStagesQuery.handler";
  private static final String ORG_ACTIVITY_QUERY_HANDLER = "orgActivityQuery.handler";
  private static final String LOCATION_QUERY_HANDLERS = "locationQuery.handlers";

  protected LoggingService logger;

  private Collection oplans;

  private Oplan myOplan;

  private Date cDay;

  private Map locations = new HashMap();

  private NewQueryHandler oplanQueryHandler;
  private NewQueryHandler activeStagesQueryHandler;
  private NewQueryHandler orgActivityQueryHandler;
  private DBProperties dbp;
  private String myPackage;
  private UIDService uidService;
  private PlanningFactory theFactory;

  private static class MyStages extends TreeSet {
  }

  private MyStages currentStages;

  private NewOplanPlugin myOplanPlugin = new NewOplanPlugin() {
      public LoggingService getLoggingService() {
        return logger;
      }

      public GeolocLocation getLocation(String code) {
        return (GeolocLocation) locations.get(code);
      }

      public Date getCDay() {
        return cDay;
      }

      public String getOrgCode() {
        return orgCode;
      }

      public OrgActivity makeOrgActivity(TimeSpan timeSpan,
                                         String activity, 
                                         String geoCode, 
                                         String opTempo)
      {
        return OplanReaderPlugin.this
          .makeOrgActivity(timeSpan, activity, geoCode, opTempo);
      }
    };

  private static UnaryPredicate myStagesPredicate =
    new UnaryPredicate() {
      public boolean execute(Object o) {
        return o instanceof MyStages;
      }
    };

  // Temporaries used during org activity reading
  private TimeSpanSet currentActivities = new TimeSpanSet();
  private TimeSpanSet addedActivities = new TimeSpanSet();
  private TimeSpanSet changedActivities = new TimeSpanSet();
  private TimeSpanSet removedActivities = new TimeSpanSet();

  private IncrementalSubscription glsSubscription;
  private IncrementalSubscription mySelfOrgs;

  /** for knowing when we get our self org asset **/
  private Organization selfOrgAsset = null;
  private String orgId;
  private String orgCode;

  /**
   * The predicate for the Socrates subscription
   **/
  private static UnaryPredicate selfOrgAssetPred = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Organization) {
	  Organization org = (Organization) o;
	  return org.isSelf();
	}
	return false;
      }
    };

  private UnaryPredicate glsTaskPredicate = new UnaryPredicate() {
      public boolean execute (Object o) {
        if (o instanceof Task) {
          Task task = (Task) o;
          Verb verb = task.getVerb();
          if (verb.equals(Constants.Verb.GetLogSupport)) {
            PrepositionalPhrase pp = task.getPrepositionalPhrase(FOR_ORGANIZATION);
            if (pp != null) {
              return pp.getIndirectObject().equals(selfOrgAsset);
            }
          }
        }
        return false;
      }
    };


  private static UnaryPredicate oplanPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Oplan);
    }
  };

  private class OrgActivityPredicate implements UnaryPredicate {
    private UID oplanUID = null;

    public void setOplan(Oplan oplan) {
      if (oplan != null)
	oplanUID = oplan.getUID();
    }

    public boolean execute(Object o) {
      if (oplanUID == null)
	return false;
      if (o instanceof OrgActivity) {
        OrgActivity oa = (OrgActivity) o;
        return oa.getOplanUID().equals(oplanUID);
      }
      return false;
    }
  }
   
  private OrgActivityPredicate orgActivityPredicate = 
    new OrgActivityPredicate();  

  public void setLoggingService(LoggingService ls) {
    logger = ls;
  }

  public void setDomainService(DomainService ds) {
    if (ds == null) {
      theFactory = null;
    } else {
      theFactory = (PlanningFactory) ds.getFactory("planning");
    }
  }

  public void setUIDService(UIDService uidService) {
    this.uidService = uidService;
  }

  protected void setupSubscriptions() {
    String myClassName = getClass().getName();
    int dot = myClassName.lastIndexOf('.');
    myPackage = myClassName.substring(0, dot);
    logger = LoggingServiceWithPrefix.add(logger, getAgentIdentifier() + ": ");
    mySelfOrgs = (IncrementalSubscription) blackboard.subscribe(selfOrgAssetPred);

    // refill oplan Collection on rehydrate
    oplans = blackboard.query(oplanPredicate);

    Collection c = blackboard.query(myStagesPredicate);
    if (c.size() > 0) {
      currentStages = (MyStages) c.iterator().next();
    } else {
      currentStages = new MyStages();
      blackboard.publishAdd(currentStages);
    }

    try {
      Collection fParams = getParameters();
      if ((fParams == null)||
          (fParams.size() == 0)) {
        dbp = DBProperties.readQueryFile(QUERY_FILE).unlock();
      }
      else {
        // Get the query file from the args.
        String qFile = (String) fParams.iterator().next();
        dbp = DBProperties.readQueryFile(qFile).unlock();        
      }
      readLocations();
      oplanQueryHandler = createQueryHandler(OPLAN_QUERY_HANDLER);
      activeStagesQueryHandler = createQueryHandler(ACTIVE_STAGES_HANDLER);
      orgActivityQueryHandler = createQueryHandler(ORG_ACTIVITY_QUERY_HANDLER);
    } catch (Exception se) {
      logger.error(this.toString()+": Initialization failed: ",se);
    }

    // test the mySelfOrgs sub to see if it has anything. If so, call processSelfOrgs
    if (! mySelfOrgs.isEmpty()) {
      if (logger.isInfoEnabled())
	logger.info(getAgentIdentifier() + " had selfOrg on sub in setupSubscriptions");
      processOrgAssets(mySelfOrgs.elements());
    }

    // If the above created the glsSub and there are gls tasks there already,
    // then perhaps we rehydrated, and we need to go get the OrgActivities
    if (glsSubscription != null && ! glsSubscription.isEmpty()) {
      if (logger.isInfoEnabled())
	logger.info(getAgentIdentifier() + " in setupSubs and now have a non empty GLS sub -- will get oplans");
      // It is very hard to process gls tasks incrementally because of
      // possible oplan removes, etc. In addition, there are
      // relatively few (e.g. one) such tasks, so we simply process the most 
      // recent gls task.

      requestOplans(lastGLSTask());
    }
  }

  // Find the most recent gls task, (i.e. one with the highest number of 
  // oplan stages.
  private Task lastGLSTask() {
    int maxStageNumber = -1;
    Task lastGLSTask = null;

    for (Iterator iterator = glsSubscription.iterator();
	 iterator.hasNext();) {
      Task task = (Task) iterator.next();
      PrepositionalPhrase stagespp = 
	task.getPrepositionalPhrase(FOR_OPLAN_STAGES);
      SortedSet oplanStages = 
	(SortedSet) stagespp.getIndirectObject();
      OplanStage lastStage = (OplanStage) oplanStages.last();
      if (lastStage.getNumber() > maxStageNumber) {
	maxStageNumber = lastStage.getNumber();
	lastGLSTask = task;
      }
    }
    return lastGLSTask;
  }

  private void readLocations() {
    NewQueryHandler[] qhs = createQueryHandlers(LOCATION_QUERY_HANDLERS);
    for (int i = 0; i < qhs.length; i++) {
      NewQueryHandler qh = qhs[i];
      if (logger.isDebugEnabled()) {
        logger.debug("readLocations with " + qh);
      }
      try {
        updateLocations(qh.readCollection());
      } catch (Exception e) {
        logger.error("Exception reading locations", e);
      }
    }
  }

  private NewQueryHandler createQueryHandler(String propName) {
    NewQueryHandler[] handlers = createQueryHandlers(propName);
    if (handlers.length < 1) {
      throw new IllegalArgumentException("No handlers found for " + propName);
    }
    if (handlers.length > 1) {
      throw new IllegalArgumentException("Multipler handlers found for " + propName);
    }
    return handlers[0];
  }

  private static Class[] queryHandlerConstructorTypes = {
    DBProperties.class,
    NewOplanPlugin.class
  };

  private NewQueryHandler[] createQueryHandlers(String propName) {
    String classNames = dbp.getProperty(propName);
    String[] handlerClassNames = CSVUtility.parse(classNames);
    NewQueryHandler[] result = new NewQueryHandler[handlerClassNames.length];
    Object[] args = {dbp, myOplanPlugin};
    for (int i = 0; i < result.length; i++) {
      String className = handlerClassNames[i];
      if (className.indexOf('.') < 0) {
        if (myPackage != null) {
          className = myPackage + "." + className;
        }
      }
      try {
        result[i] = (NewQueryHandler)
          Class.forName(className)
          .getConstructor(queryHandlerConstructorTypes)
          .newInstance(args);
      } catch (Exception e) {
        throw new RuntimeException("Could not instantiate QueryHandler: " + className, e);
      }
    }
    return result;
  }

  private void setupSubscriptions2() {
    glsSubscription = (IncrementalSubscription) blackboard.subscribe(glsTaskPredicate);
    blackboard.unsubscribe(mySelfOrgs);
    mySelfOrgs = null;
  }

  private void processOrgAssets(Enumeration e) {
    if (e.hasMoreElements()) {
      selfOrgAsset = (Organization) e.nextElement();
      orgId = selfOrgAsset.getItemIdentificationPG().getItemIdentification();
      orgCode = selfOrgAsset.getMilitaryOrgPG().getUIC();

      // Setup our other subscriptions now that we know ourself
      if (glsSubscription == null) {
        setupSubscriptions2();
      }
    }
  }

  public synchronized void execute() {    
    if (mySelfOrgs != null && mySelfOrgs.hasChanged()) {
      if (logger.isDebugEnabled())
	logger.debug(getAgentIdentifier() + ".execute: selfOrgs sub has changed");
      processOrgAssets(mySelfOrgs.getAddedList());
    } else {
      if (logger.isDebugEnabled())
	logger.debug(getAgentIdentifier() + ".execute: selfOrgs sub is " + mySelfOrgs + ((mySelfOrgs != null) ? (" and not changed. It has " + mySelfOrgs.size() + " elements.") : " so must have done processOrgAssets."));
    }

    if (glsSubscription != null && glsSubscription.hasChanged()) {
      if (logger.isDebugEnabled())
	logger.debug(getAgentIdentifier() + ".execute: have a changed glsSub - will do requestOplans");
      // It is very hard to process gls tasks incrementally because of
      // possible oplan removes, etc. In addition, there are
      // relatively few (e.g. one) such tasks, so we simply reprocess
      // all gls tasks whenever there are any changes
      
      requestOplans(lastGLSTask());
    } else if (logger.isInfoEnabled()) {
      logger.info(getAgentIdentifier() + ".execute not requesting oplans. " + ((glsSubscription != null) ? ("Have a glsSub. Not apparently changed. It has " + glsSubscription.size() + " GLS Tasks.") : "No glsSub. Have not yet apparently done processOrgAssets"));
    }
  }

  private void requestOplans(Task glsTask) {
    dbp.put(AGENT_PARAMETER, orgId);
    
    boolean oplanAdded = false; // Remember to publish it
    boolean oplanChanged = false;
    
    ContextOfOplanIds cIds = (ContextOfOplanIds) glsTask.getContext();
    // There should always be exactly one oplan
    String oplanId = (String) cIds.iterator().next();
    PrepositionalPhrase c0pp = glsTask.getPrepositionalPhrase(WITH_C0);
    long c0_date = ((Long)(c0pp.getIndirectObject())).longValue();
    cDay = new Date(c0_date);
    myOplan = findOplan(oplanId);
    dbp.put(OPLAN_ID_PARAMETER, oplanId);
    if (myOplan == null) {
      if (logger.isInfoEnabled())
	logger.info(getAgentIdentifier() + ".requestOplans: no Oplan on BBoard yet.");
      try {
	myOplan = (Oplan) oplanQueryHandler.readObject();
	if (myOplan == null) {
	  logger.error(getAgentIdentifier() + " still has null Oplan for id " + oplanId + "! This agent wont have anything to do!");
	  return;
	}
	Number num = (Number) activeStagesQueryHandler.readObject();
	int minRequiredStage;
	if (num == null) {
	  minRequiredStage = 0;
	} else {
	  minRequiredStage = num.intValue();
	}
	myOplan.setMinRequiredStage(minRequiredStage);
	myOplan.setCday(cDay);
	uidService.registerUniqueObject(myOplan);
	myOplan.setOwner(getAgentIdentifier());
	myOplan.setOplanId(oplanId);
	oplans.add(myOplan);
	if (logger.isInfoEnabled())
	  logger.info(getAgentIdentifier() + " publishing Oplan");
	blackboard.publishAdd(myOplan);
      } catch (Exception e) {
	logger.error("Exception reading oplan" + oplanId, e);
	return;
      }
    }

    currentActivities.clear();
    addedActivities.clear();
    changedActivities.clear();
    removedActivities.clear();

    orgActivityPredicate.setOplan(myOplan);
    currentActivities.addAll(blackboard.query(orgActivityPredicate));
    
    PrepositionalPhrase stagespp = glsTask.getPrepositionalPhrase(FOR_OPLAN_STAGES);
    SortedSet newStages = (SortedSet) stagespp.getIndirectObject();
    if (logger.isInfoEnabled())
      logger.info(getAgentIdentifier() + ".requestOplans: GLS task lists OplanStages size: " + newStages.size() + ". My currentStages has : " + currentStages.size());
    boolean currentStagesChanged = false;
    if (!newStages.containsAll(currentStages)) {
      // Some stages have been removed, we start all over
      for (Iterator j = currentActivities.iterator(); j.hasNext(); ) {
	OrgActivity oa = (OrgActivity) j.next();
	removedActivities.add(oa);
	j.remove();
      }
      currentStages.clear();
      currentStagesChanged = true;
    }
    
    if (logger.isInfoEnabled())
      logger.info(getAgentIdentifier() + ".requestOplans: OrgActivities to remove: " + removedActivities.size());
    
    SortedSet addedStages = new TreeSet(newStages);
    addedStages.removeAll(currentStages); // Leaves only the added stages
    for (Iterator j = addedStages.iterator(); j.hasNext(); ) {
      OplanStage stage = (OplanStage) j.next();
      dbp.put(OPLAN_STAGE_PARAMETER, String.valueOf(stage.getNumber()));
      try {
	Collection orgActivities = orgActivityQueryHandler.readCollection();
	updateOrgActivities(myOplan, orgActivities);
        } catch (Exception e) {
          logger.error("Error running org activity query", e);
        }
        currentStagesChanged |= currentStages.add(stage);
    }
    
    if (currentStagesChanged) {
      blackboard.publishChange(currentStages);
      if (currentStages.isEmpty()) {
	myOplan.setMaxActiveStage(-1); // No stages are active
      } else {
	OplanStage maxActiveStage = (OplanStage) newStages.last();
	myOplan.setMaxActiveStage(maxActiveStage.getNumber());
      }
      blackboard.publishChange(myOplan);
    }
    
    for (Iterator j = removedActivities.iterator(); j.hasNext(); ) {
      OrgActivity oa = (OrgActivity) j.next();
      blackboard.publishRemove(oa);
    }
    
    if (logger.isInfoEnabled())
      logger.info(getAgentIdentifier() + ".requestOplans: OrgActivities to add: " + addedActivities.size());
    
    for (Iterator j = addedActivities.iterator(); j.hasNext(); ) {
      OrgActivity oa = (OrgActivity) j.next();
      blackboard.publishAdd(oa);
      if (logger.isDebugEnabled())
	logger.debug("Adding OrgActivity: "+oa.getActivityType());
    }
    
    if (logger.isInfoEnabled())
      logger.info(getAgentIdentifier() + ".requestOplans: OrgActivities to change: " + changedActivities.size());
    
    for (Iterator j = changedActivities.iterator(); j.hasNext(); ) {
      OrgActivity oa = (OrgActivity) j.next();
      blackboard.publishChange(oa);
    }
  }

  // Update org activities. The supplied orgActivities override
  // currentActivities.
  private void updateOrgActivities(Oplan update,
                                   Collection orgActivities)
  {
    if (logger.isDebugEnabled()) {
      logger.debug("updateOrgActivities with " + orgActivities.size() + " activities");
    }
    synchronized (oplans) {
      String oplanID = update.getOplanId();
      Oplan oplan = findOplan(oplanID);
      if (oplan == null) {
        logger.error("GLSInitServlet.updateOrgActivities(): can't find" + " referenced Oplan " + oplanID);
        return;
      }

      // We scan all activities for overlap with each of the new
      // activities. Were there is overlap, the existing activity is
      // trimmed to not exist for the timespan of the new activity.
      // The existing activity is copied, then the time span is
      // changed.  The existing is removed and the copied version
      // is added.

      TimeSpanSet[] tsSets = {currentActivities, addedActivities, changedActivities};
      // For each new org activity, see if it overlaps a current activity
      for (Iterator i = orgActivities.iterator(); i.hasNext();) {
        OrgActivity orgActivity = (OrgActivity) i.next();
        if (logger.isDebugEnabled()) {
          logger.debug("New OrgActivity is: " + orgActivity.getActivityType() +
                       " " + orgActivity.getTimeSpan());
        }
        long newStartTime = orgActivity.getStartTime();
        long newEndTime = orgActivity.getEndTime();
        for (int k = 0; k < tsSets.length; k++) {
          TimeSpanSet tsSet = tsSets[k];
          Collection affectedActivities = tsSet.intersectingSet(orgActivity);
          for (Iterator j = affectedActivities.iterator(); j.hasNext(); ) {
            OrgActivity existingActivity = (OrgActivity) j.next();
            if (logger.isDebugEnabled()) {
              logger.debug("Fixing existing org activity " + existingActivity.getActivityType() +
                           " " + existingActivity.getTimeSpan());
            }
            tsSet.remove(existingActivity); // Don't know where it
                                            // will end up, but it
                                            // can't be here anymore
            // See if any part is still viable
            long existingStartTime = existingActivity.getStartTime();
            long existingEndTime = existingActivity.getEndTime();
            boolean existingActivityNeedsRemove = (tsSet == currentActivities);
            if (existingStartTime < newStartTime) {
              TimeSpan newTimeSpan =
                new TimeSpan(existingStartTime,
                             newStartTime);
              // Need new activity like the existingActivity
              OrgActivity newActivity = makeOrgActivity(newTimeSpan, existingActivity);
              if (logger.isDebugEnabled()) {
                logger.debug("New/Copied org activity is: "+ newActivity.getActivityType()
                            + " " + newActivity.getTimeSpan());
              }
              addedActivities.add(newActivity);
           }
            if (existingEndTime > newEndTime) { 
              //if (existingEndTime < newEndTime) {
              TimeSpan aNewTimeSpan =
                new TimeSpan(newEndTime,
                             existingEndTime);
                // Need new activity like the existingActivity
              OrgActivity aNewActivity = makeOrgActivity(aNewTimeSpan, existingActivity);
              if (logger.isDebugEnabled()) {
                logger.debug("New/Copied org activity is: "+ aNewActivity.getActivityType()
                            + " " + aNewActivity.getTimeSpan());
              }
              addedActivities.add(aNewActivity);
            }
            if (existingActivityNeedsRemove) {
              blackboard.publishRemove(existingActivity);
              if (logger.isDebugEnabled()) {
                logger.debug("Removing activity from blackboard: "+ existingActivity.getActivityType() +
                           " " + existingActivity.getTimeSpan());
              }
            }
          }
        }
        addedActivities.add(orgActivity);
      }
    }
  }

  private void updateLocations(Collection locs) {
    for (Iterator i = locs.iterator(); i.hasNext(); ) {
      GeolocLocation location = (GeolocLocation) i.next();
      String locName = location.getGeolocCode();
      locations.put(locName, location);
    }
  }

  private Oplan findOplan(String oplanId) {
    synchronized (oplans) {
      for (Iterator i = oplans.iterator(); i.hasNext();) {
        Oplan oplan = (Oplan) i.next();
        if (oplan.getOplanId().equals(oplanId))
          return oplan;
      }
      return null;
    }
  }


  private OrgActivity makeOrgActivity(TimeSpan timeSpan,
                                      String activity, 
                                      String geoCode, 
                                      String opTempo) {

    OrgActivityImpl orgActivity = OplanFactory.newOrgActivity(orgId, myOplan.getUID());
    uidService.registerUniqueObject(orgActivity);
    orgActivity.setOwner(getAgentIdentifier());
    orgActivity.setTimeSpan(timeSpan);
    orgActivity.setActivityType(activity);
    orgActivity.setGeoLoc((GeolocLocation) locations.get(geoCode));
    orgActivity.setOpTempo(opTempo);

    return orgActivity; 
  }

  private OrgActivity makeOrgActivity(TimeSpan timeSpan, OrgActivity oldActivity) {
    OrgActivityImpl orgActivity =
      OplanFactory.newOrgActivity(oldActivity.getOrgID(),
                                  oldActivity.getOplanUID());
    //orgActivity.setAll(oldActivity);
    orgActivity.setActivityName(oldActivity.getActivityName());
    orgActivity.setActivityType(oldActivity.getActivityType());
    orgActivity.setOpTempo(oldActivity.getOpTempo());
    orgActivity.setGeoLoc(oldActivity.getGeoLoc());
    orgActivity.setOwner(getAgentIdentifier());
    uidService.registerUniqueObject(orgActivity);
    orgActivity.setTimeSpan(timeSpan);
    return orgActivity;
  }

  private Oplan makeOplan() {
    return new Oplan();
  }
}
