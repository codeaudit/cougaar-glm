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

package org.cougaar.mlm.plugin.organization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.util.UID;

import org.cougaar.util.UnaryPredicate;
  
import org.cougaar.core.agent.service.uid.UIDServiceProvider;
import org.cougaar.core.agent.service.uid.UIDServiceImpl;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.servlet.ServletService;

import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.TimeAspectValue;
import org.cougaar.planning.ldm.plan.Verb;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NamedPosition;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.glm.ldm.oplan.OplanContributor;
import org.cougaar.glm.ldm.oplan.OplanCoupon;
import org.cougaar.glm.ldm.oplan.OrgActivity;

import org.cougaar.mlm.plugin.ldm.LDMSQLPlugin;
import org.cougaar.mlm.plugin.ldm.SQLOplanBase;
import org.cougaar.mlm.plugin.ldm.SQLOplanQueryHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;

/**
 * The GLSInitServlet crams all of the functionality of the GLSGUIInitPlugin, 
 * GLSGUIRescindPlugin, and SQLOplanPlugin sans GUIs into one plugin
 * The buttons are now in a client application which talks to the
 * servlets in this plugin to publish the oplan and gls tasks
 *
 **/
public class GLSInitServlet extends LDMSQLPlugin implements SQLOplanBase{

  public static final String SENDOPLAN = "sendoplan";
  public static final String UPDATEOPLAN = "updateoplan";
  public static final String PUBLISHGLS = "publishgls";
  public static final String RESCINDGLS = "rescindgls";

  private static final String PUBLISH_ON_SELF_ORG = "PublishOnSelfOrg";


  /**
   * For making direct request on this plugin (not via servlet).
   **/
  public static class Request implements Serializable {
    private String command;
    public Request(String command) {
      this.command = command;
    }
  }

  private IncrementalSubscription oplanSubscription;

  private IncrementalSubscription glsSubscription;

  private IncrementalSubscription stateSubscription;

  private IncrementalSubscription myorgassets;

  private IncrementalSubscription requestSubscription;

  /** for knowing when we get our self org asset **/
  private Organization selfOrgAsset = null;

  private ArrayList contributors;
  
  private ArrayList oplans = new ArrayList();
  private HashMap locations = new HashMap();

  private static final String forRoot = "ForRoot".intern();

  // Additions/Modifications/Deletions which have not yet been published
  private HashSet newObjects = new HashSet();
  private HashSet modifiedObjects = new HashSet();
  private HashSet removedObjects = new HashSet();

  // tells reply servlet when to push info
  private Object monitor = new Object();

  protected long myOplanTime;
	
  UIDService uidService = null;	
		
  private static class MyPrivateState implements java.io.Serializable {
    boolean oplanExists = false;
    boolean unpublishedChanges = false;
    boolean errorOccurred = false;
    int taskNumber = 0;
  }

  private MyPrivateState myPrivateState;

  private static UnaryPredicate oplanPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Oplan);
    }
  };
  

  private static UnaryPredicate orgAssetPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Organization);
    }
  };

  /**
   * This predicate selects for root tasks injected by the GLSGUIInitPlugin
   **/
  private UnaryPredicate glsPredicate =  new UnaryPredicate() {
      public boolean execute(Object o) {
	if (!(o instanceof Task)) return false;
	Task task = (Task) o;
	if (!task.getVerb().equals(Constants.Verb.GETLOGSUPPORT)) return false;
	if (!task.getSource().equals(getClusterIdentifier())) return false;
	if (!task.getDestination().equals(getClusterIdentifier())) return false;
	return (task.getPrepositionalPhrase(forRoot) != null);
      }
    };

  private static UnaryPredicate statePredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof MyPrivateState);
    }
  };
  
  private static UnaryPredicate requestPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Request);
    }
  };
  
  private static class OrgActivityPredicate implements UnaryPredicate {
    private Oplan myOplan= null;
    private String myOrgId = "";

    public void setOplan(Oplan oplan) {
      myOplan = oplan;
    }

    public void setOrgId(String orgId) {
      myOrgId = orgId;
    }

    public boolean execute(Object o) {
      if ((myOplan == null) ||
          (myOrgId.equals(""))) {
        return false;
      }
    
      return ((o instanceof OrgActivity) &&
              (((OrgActivity) o).getOrgID().equals(myOrgId)) &&
              (((OrgActivity) o).getOplanUID().equals(myOplan.getUID())));
    }
  }   

  private static OrgActivityPredicate orgActivityPredicate = 
    new OrgActivityPredicate();


  private ServletService servletService;
  /** Sets the servlet service. Called by introspection on start
   **/
  public void setServletService(ServletService ss) {
    servletService = ss;
  }

  /**
   * Executes Plugin functionality.
   */
  public void execute(){

    boolean doNotify = false;

    if (stateSubscription.hasChanged()) {
      checkForPrivateState(stateSubscription.getAddedList());
    }

    if (myorgassets.hasChanged()) {
      handleMyOrgAssets(myorgassets.getAddedList());

      if ((selfOrgAsset != null) &&
          (Boolean.valueOf((String) globalParameters.get(PUBLISH_ON_SELF_ORG)).booleanValue())) {
        publishOplanAndGLS();
      }
    }

    if (oplanSubscription.hasChanged()) {
      Collection adds = oplanSubscription.getAddedCollection();
      if (adds != null) {
	processOplanAdds(adds);
      }
      Collection changes = oplanSubscription.getChangedCollection();
      if (changes != null) {
	processChanges(changes);
      }
      Collection deletes = oplanSubscription.getRemovedCollection();
      if (deletes !=null) {
	processOplanDeletes(deletes);
      }

      doNotify = true;
    }

    if (glsSubscription.hasChanged()) {
      doNotify = true;
    }

    if (requestSubscription.hasChanged()) {
      processRequests(requestSubscription.getAddedCollection());
    }

    for (Iterator it = contributors.iterator(); it.hasNext();) {
      IncrementalSubscription is = (IncrementalSubscription) it.next();
      
      // we don't care about adds, just deletes and changes
      if (is.hasChanged()) {
	Collection changes = is.getChangedCollection();
	if (changes != null) {
	  processChanges(changes);
	}

	// Deletes of OplanContributors are treated as changes to the Oplan
	changes = is.getRemovedCollection();
	if (changes != null) {
	  processChanges(changes);
	}	
      }
    }

    // update gui, if needed
    if (doNotify) {
      synchronized(monitor) {
	monitor.notifyAll();
      }
    }

  }

  private void processRequests(Collection newRequests) {
    for (Iterator i = newRequests.iterator(); i.hasNext(); ) {
      Request request = (Request) i.next();
      if (request.command.equals("sendoplan")) {
        publishOplanObjects();
        publishOplanPostProcessing();
      } else if (request.command.equals("publishgls")) {
	publishAllRootGLS();
      }
      publishRemove(request);
    }
  }

  /** finds the published oplan using its ID as a key
      @return oplan
  */
  private Oplan findOplanById(String oplanID) {
    synchronized(oplanSubscription) {
      for (Iterator iterator = oplanSubscription.iterator(); 
	   iterator.hasNext();) {
	Oplan oplan = (Oplan) iterator.next();
	if (oplanID.equals(oplan.getOplanId())) {
	  return oplan;
	}
      }
      return null;
    }
  }

  public Oplan getOplan(String oplanID) {

    synchronized (oplans) {
      for (Iterator iterator = oplans.iterator();
           iterator.hasNext();) {
        Oplan oplan = (Oplan) iterator.next();
        if ((oplan.getOplanId().equals(oplanID))){
          return oplan;
        }
      }
      return null;
    }
  }


  public void updateOplanInfo(Oplan update) {
    synchronized (oplans) {
      String oplanID = update.getOplanId();
      Oplan oplan = getOplan(oplanID);
      
      if (oplan == null) {
        oplan = addOplan(oplanID);
      }
      
      oplan.setOperationName(update.getOperationName());
      oplan.setPriority(update.getPriority());
      oplan.setCday(update.getCday());
      oplan.setEndDay(update.getEndDay());

      boolean found = false;
      if (oplanSubscription != null) {
        Collection published = oplanSubscription.getCollection();
        for (Iterator iterator = published.iterator();
             iterator.hasNext();) {
          if (((Oplan) iterator.next()).getOplanId().equals(oplanID)) {
            found = true;
            break;
          }
        }
      }

      if (found) {
        modifiedObjects.add(oplan);

        if (myPrivateState != null) {
          myPrivateState.unpublishedChanges = true;
        }
      } else {
        newObjects.add(oplan);
      }
    }
  }

  // Replace org activies for Org.
  public void updateOrgActivities(Oplan update,
                                  String orgId,
                                  Collection orgActivities) {

    synchronized (oplans) {
      String oplanID = update.getOplanId();
      Oplan oplan = getOplan(oplanID);
      
      if (oplan == null) {
        System.err.println("GLSInitServlet.updateOrgActivities(): can't find" +
                           " referenced Oplan " + oplanID);
        return;
      }
      
      // Execute a query to get all existing org activities associated with
      // the oplan
      orgActivityPredicate.setOplan(oplan);
      orgActivityPredicate.setOrgId(orgId);
      Collection existingOrgActivities = getBlackboardService().query(orgActivityPredicate);
      for (Iterator iterator = existingOrgActivities.iterator();
           iterator.hasNext();) {
        OrgActivity orgActivity = (OrgActivity) iterator.next();
        
        if (orgActivity.getOrgID().equals(orgId)) {
          removedObjects.add(orgActivity);
        }
      }

      for (Iterator iterator = orgActivities.iterator();
           iterator.hasNext();) {
        OrgActivity orgActivity = (OrgActivity) iterator.next();
        newObjects.add(orgActivity);
      }

      if (myPrivateState != null) {
        myPrivateState.unpublishedChanges = true;
      }
    }
  }

  // Used by query handlers to get location info
  public NamedPosition getLocation(String locCode) {
    return (NamedPosition) locations.get(locCode);
  }

  // Used by query handlers to update location info
  public void updateLocation(GeolocLocation location) {
    String locName = location.getGeolocCode();
    locations.put(locName, location);
  }

  /*
   * Creates a subscription.
   */
  protected void setupSubscriptions() 
  {	
    super.setupSubscriptions();
    
    getBlackboardService().getSubscriber().setShouldBePersisted(false);
    oplanSubscription = (IncrementalSubscription) getBlackboardService().subscribe(oplanPredicate);
    stateSubscription = (IncrementalSubscription) getBlackboardService().subscribe(statePredicate);

    glsSubscription = (IncrementalSubscription) getBlackboardService().subscribe(glsPredicate);

    myorgassets = (IncrementalSubscription) subscribe(orgAssetPred);
    requestSubscription = (IncrementalSubscription) subscribe(requestPredicate);

    contributors = new ArrayList(13);
    // refill contributors Collection on rehydrate
    processOplanAdds(oplanSubscription.getCollection());

    if (getBlackboardService().didRehydrate()) {
      checkForPrivateState(stateSubscription.elements());
    } else {
      getBlackboardService().publishAdd(new MyPrivateState());
    }

    // register with servlet service
    try {
      servletService.register("/glsinit", new GLSServlet());
      servletService.register("/glsreply", new GLSReplyServlet());
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("GLSInitServlet: " + PUBLISH_ON_SELF_ORG + " = " + 
                       globalParameters.get(PUBLISH_ON_SELF_ORG));
  }	   		 

  protected void initProperties() {
    // default package for QueryHandler
    super.initProperties();
    String exptid = System.getProperty("org.cougaar.experiment.id");
    
    if (exptid != null) {
      globalParameters.put("exptid", exptid);
    }
  }
  

  private void handleMyOrgAssets(Enumeration e) {
    while (e.hasMoreElements()) {
      Organization org = (Organization)e.nextElement();

      // Pick up self org
      if (org.isSelf()) {
        selfOrgAsset = org;
      }
    }
  }

  private Oplan addOplan(String oplanID) {
    Oplan oplan;

    synchronized (oplans) {
      oplan = getOplan(oplanID);
      
      if (oplan != null) {
        System.err.println("GLSInitServlet.addOplan(): " + oplanID + 
                           " already exists.");
        return oplan;
      } else {
        oplan =  new Oplan();

	getUIDServer().registerUniqueObject(oplan);
        oplan.setOwner(getClusterIdentifier());
        oplan.setOplanId(oplanID);
        oplans.add(oplan);
      }
    } // end syncronization on oplans
    return oplan;
  }

  private void checkForPrivateState(Enumeration e) {
    if (myPrivateState == null) {
      while(e.hasMoreElements()) {
        myPrivateState = (MyPrivateState) e.nextElement();
      }
    }
  }

  private void publishOplanObjects() {
    for (Iterator iterator = newObjects.iterator();
         iterator.hasNext();) {
      Object object = iterator.next();
      getBlackboardService().publishAdd(object);

      if (object instanceof Oplan) {
        OplanCoupon ow = new OplanCoupon(((Oplan) object).getUID(), 
                                         getClusterIdentifier());
        getUIDServer().registerUniqueObject(ow);
        getBlackboardService().publishAdd(ow);

        myPrivateState.oplanExists = true;
      }
    }

    for (Iterator iterator = modifiedObjects.iterator();
         iterator.hasNext();) {
      Object object = iterator.next();
      getBlackboardService().publishChange(object);
    }

    for (Iterator iterator = removedObjects.iterator();
         iterator.hasNext();) {
      Object object = iterator.next();
      getBlackboardService().publishRemove(object);
    }

    getBlackboardService().publishChange(myPrivateState);
  }
  
  private void publishOplanPostProcessing() {
    newObjects.clear();
    modifiedObjects.clear();
    removedObjects.clear();

    myPrivateState.unpublishedChanges = false;
  }
  
  private void publishOplan() {
	// Need to make separate add/remove/modify lists
	getBlackboardService().openTransaction();
	publishOplanObjects();
	getBlackboardService().closeTransactionDontReset();
	publishOplanPostProcessing();
  }

  private void refreshOplan() {
    for (Enumeration e = queries.elements(); e.hasMoreElements();) {
      SQLOplanQueryHandler qh = (SQLOplanQueryHandler) e.nextElement();
      qh.update();
    }
  }

  private void processOplanAdds(Collection adds) {
    for (Iterator it = adds.iterator(); it.hasNext();) {
      Oplan oplan = (Oplan) it.next();
      
      IncrementalSubscription is = (IncrementalSubscription) 
	getBlackboardService().subscribe(new ContributorPredicate(oplan.getUID()));
      contributors.add(is);
    }
  }

  private void processChanges(Collection changes) {
    HashSet changedOplans = new HashSet();

    for (Iterator iterator = changes.iterator(); iterator.hasNext();) {
      UID oplanUID = null;
      Object o = iterator.next();
      if (o instanceof Oplan) {
	oplanUID = ((Oplan) o).getUID();
      } else if (o instanceof OplanContributor) {
	oplanUID = ((OplanContributor) o).getOplanUID();
      } else continue;
      
      changedOplans.add(oplanUID);
    } 

    // Publish once for each changed oplan
    for (Iterator iterator = changedOplans.iterator(); iterator.hasNext();) {
      Collection coupons = 
        getBlackboardService().query(new CouponPredicate((UID) iterator.next()));
      for (Iterator couponIt = coupons.iterator(); couponIt.hasNext();) {
	System.out.println("GLSInitServlet: publishChanging OplanCoupon");
	getBlackboardService().publishChange(couponIt.next());
      }
    }
  }

  private void processOplanDeletes(Collection deletes) {
    for (Iterator it = deletes.iterator(); it.hasNext();) {
      Oplan oplan = (Oplan) it.next();
      Collection coupons = getBlackboardService().query(new CouponPredicate(oplan.getUID()));
      for (Iterator couponIt = coupons.iterator(); couponIt.hasNext();) {
	getBlackboardService().publishRemove(couponIt.next());
      }
    }
  }

  private class CouponPredicate implements UnaryPredicate {
    UID _oplanUID;
    public CouponPredicate(UID oplanUID) {
      _oplanUID = oplanUID;
    }
    public boolean execute(Object o) {
      if (o instanceof OplanCoupon) {
	if (((OplanCoupon ) o).getOplanUID().equals(_oplanUID)) {
	  return true;
	}
      }
      return false;
    }
  }

  private class ContributorPredicate implements UnaryPredicate {
    UID _oplanUID;
    public ContributorPredicate(UID oplanUID) {
      _oplanUID = oplanUID;
    }
    public boolean execute(Object o) {
      if (o instanceof OplanContributor) {
	if (((OplanContributor ) o).getOplanUID().equals(_oplanUID)) {
	  return true;
	}
      }
      return false;
    }
  }

  private void publishAllRootGLS() {
    for (Iterator i = oplanSubscription.iterator(); i.hasNext(); ) {
      Oplan oplan = (Oplan) i.next();
      doPublishRootGLS(oplan);
    }
  }

  public void publishRootGLS(String oplanID) {
    openTransaction();
    Oplan oplan = findOplanById(oplanID);
    System.out.println("publishRootGLS() oplan " + oplan);
    doPublishRootGLS(oplan);
    closeTransactionDontReset();
  }


  public void rescindRootGLS(String oplanID) {
    openTransaction();
    Oplan oplan = findOplanById(oplanID);
    System.out.println("rescindRootGLS() oplan " + oplan);
    for (Iterator it = glsSubscription.iterator(); it.hasNext();) {
      Task t = (Task) it.next();
      ContextOfUIDs cui = (ContextOfUIDs) t.getContext();
      if (cui.contains(oplan.getUID())) {
	publishRemove(t);
      }
    }
    closeTransactionDontReset();
  }

  private void doPublishRootGLS(Oplan oplan) {    
    NewTask task = theLDMF.newTask();
    // ensure this is a root level task
    task.setPlan(theLDMF.getRealityPlan());
    task.setSource(getClusterIdentifier());
    task.setDestination(getClusterIdentifier());
    
    // set prepositional phrases
    Vector phrases = new Vector(3);
    NewPrepositionalPhrase newpp;

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(Constants.Preposition.FOR);
    newpp.setIndirectObject(selfOrgAsset);
    phrases.add(newpp);

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition("ForRoot");
    newpp.setIndirectObject(new Integer(++myPrivateState.taskNumber));
    publishChange(myPrivateState);

    phrases.add(newpp);

    task.setPrepositionalPhrases(phrases.elements());

    // verb
    task.setVerb(Constants.Verb.GetLogSupport);

    // schedule
    long startTime = currentTimeMillis();
    long endTime;
    Date endDay = oplan.getEndDay();
    if (endDay != null) {
      endTime = endDay.getTime();
    } else {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date(startTime));
      // increment date by 3 MONTHs
      cal.add(Calendar.MONTH, 3);
      endTime = cal.getTime().getTime();
    }

    AspectValue startTav = TimeAspectValue.create(AspectType.START_TIME, startTime);
    AspectValue endTav = TimeAspectValue.create(AspectType.END_TIME, endTime);

    ScoringFunction myStartScoreFunc = ScoringFunction.createStrictlyAtValue( startTav );
    ScoringFunction myEndScoreFunc = ScoringFunction.createStrictlyAtValue( endTav );    

    Preference startPreference = theLDMF.newPreference( AspectType.START_TIME, myStartScoreFunc );
    Preference endPreference = theLDMF.newPreference( AspectType.END_TIME, myEndScoreFunc  );

    Vector preferenceVector = new Vector(2);
    preferenceVector.addElement( startPreference );
    preferenceVector.addElement( endPreference );

    task.setPreferences( preferenceVector.elements() );

    // Set the context
    try {
      UID oplanUID = oplan.getUID();
      ContextOfUIDs context = new ContextOfUIDs(oplanUID);
      System.out.println("GLSGUIInitPlugin: Setting context to: " + oplanUID);
      task.setContext(context);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    publishAdd(task);
    System.out.println("\n" + formatDate(System.currentTimeMillis()) + " Send Task: " + task);
  }

  private void publishOplanAndGLS() {
    publishOplanObjects();
    
    for (Iterator iterator = newObjects.iterator();
         iterator.hasNext();) {
      Object object = iterator.next();

      if (object instanceof Oplan) {
        doPublishRootGLS((Oplan) object);
      }
    }

    publishOplanPostProcessing();

    System.out.println("Published from oplan and gls from updateOrgActivies");
  }
  
  protected static DateFormat logTimeFormat =
    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

  protected static String formatDate(long when) {
    return logTimeFormat.format(new Date(when));
  }


  private class GLSServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
      String command = request.getParameter("command");
      //System.out.println("GLSServlet got request " + command);
      response.setContentType("text/html");
      try {
	PrintWriter out = response.getWriter();
	out.println("<html><head></head><body><" +request.getParameter("command") + "></body></html>");
	out.close();
      } catch (java.io.IOException ie) { ie.printStackTrace(); }

      if (command.equals(SENDOPLAN)) {
	publishOplan();
      }
      if (command.equals(UPDATEOPLAN)) {
	refreshOplan();
      }
      if (command.equals(PUBLISHGLS)) {
	//System.out.println("oplanID is " + request.getParameter("oplanID"));
	publishRootGLS(request.getParameter("oplanID"));
      }
      if (command.equals(RESCINDGLS)) {
	//System.out.println("oplanID is " + request.getParameter("oplanID"));
	rescindRootGLS(request.getParameter("oplanID"));
      }
    }

  }

  private class GLSReplyServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
      String command = request.getParameter("command");
      //System.out.println("GLSServlet got request " + command);
      // make this smarter?
      ReplyWorker worker = new ReplyWorker(request, response);
      worker.execute();
    }
  }

  private class ReplyWorker {
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    public ReplyWorker(HttpServletRequest request, HttpServletResponse response) {
      this.request = request;
      this.response = response;
    }

    // bug - this keeps writing even if the listener client goes away
    public void execute() {
      try {
	response.setContentType("text/xml");
	PrintWriter out = response.getWriter();
	// keep writing back to the client
  	while(true) {
	  StringBuffer sb = new StringBuffer();
	  for (Iterator it = oplanSubscription.iterator(); it.hasNext(); ) {
	    Oplan oplan = (Oplan) it.next();
	    sb.append("<oplan name=" );
	    sb.append(oplan.getOperationName());
	    sb.append(" id=");
	    sb.append(oplan.getOplanId());
	    sb.append(">");
	    out.println(sb);
	    //System.out.println(sb);
	  }
	  out.println("<GLS " + glsSubscription.size() + ">");
	  //System.out.println("GLS " + glsSubscription.size());
	  out.flush();
  	  synchronized(monitor) {
  	    try {
	      // sit until notified that gls or oplan has changed
  	      monitor.wait();
  	    } catch (InterruptedException ie) {
	      ie.printStackTrace();
  	    }
  	  }
  	}
      } catch (java.io.IOException ie) {ie.printStackTrace(); }
    }
  }
}







