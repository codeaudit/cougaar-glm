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
  
import org.cougaar.core.service.UIDService;
import org.cougaar.core.service.ServletService;

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
public class GLSInitServlet extends LDMSQLPlugin {

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

  private IncrementalSubscription myorgassets;

  private IncrementalSubscription stateSubscription;

  private IncrementalSubscription requestSubscription;

  /** for knowing when we get our self org asset **/
  private Organization selfOrgAsset = null;

  private ArrayList contributors;

  private static final String forRoot = "ForRoot".intern();

  // tells reply servlet when to push info
  private Object monitor = new Object();

  protected long myOplanTime;
	
  UIDService uidService = null;	
		
  private static class MyPrivateState implements java.io.Serializable {
    boolean oplanCouponExists = false;
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

  /**
   * This predicate selects for root tasks injected by the GLSGUIInitPlugin
   **/
  private UnaryPredicate glsPredicate =  new UnaryPredicate() {
      public boolean execute(Object o) {
	if (!(o instanceof Task)) return false;
	Task task = (Task) o;
	if (!task.getVerb().equals(Constants.Verb.GETLOGSUPPORT)) return false;
	if (!task.getSource().equals(getMessageAddress())) return false;
	if (!task.getDestination().equals(getMessageAddress())) return false;
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

  private static UnaryPredicate orgAssetPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Organization);
    }
  };

  private ServletService servletService;
  /** Sets the servlet service. Called by introspection on start
   **/
  public void setServletService(ServletService ss) {
    servletService = ss;
  }


  /*
   * Creates a subscription.
   */
  protected void setupSubscriptions() 
  {
    initProperties();
    grokArguments();
    
    getBlackboardService().getSubscriber().setShouldBePersisted(false);

    oplanSubscription = (IncrementalSubscription) getBlackboardService().subscribe(oplanPredicate);
    stateSubscription = (IncrementalSubscription) getBlackboardService().subscribe(statePredicate);
    glsSubscription = (IncrementalSubscription) getBlackboardService().subscribe(glsPredicate);
    myorgassets = (IncrementalSubscription) subscribe(orgAssetPred);
    requestSubscription = (IncrementalSubscription) subscribe(requestPredicate);

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
        //just publish oplan coupon here
        publishOplanCoupon();
        //publishOplanAndGLS();
      }
    }
    
    if (oplanSubscription.hasChanged()) {
      //only care about adds, not changes or deletes
      Collection adds = oplanSubscription.getAddedCollection();
      if ((adds != null) &&
          (selfOrgAsset != null) &&
          (Boolean.valueOf((String) globalParameters.get(PUBLISH_ON_SELF_ORG)).booleanValue())) {
        for (Iterator i = adds.iterator(); i.hasNext(); ) {
          Oplan oplan = (Oplan) i.next();
          doPublishRootGLS(oplan);
        }
      }
      doNotify = true;
    }
    
    if (glsSubscription.hasChanged()) {
      doNotify = true;
    }
    
    if (requestSubscription.hasChanged()) {
      processRequests(requestSubscription.getAddedCollection());
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
        publishOplanCoupon();
        myPrivateState.unpublishedChanges=false;
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
  

  private void handleMyOrgAssets(Enumeration e) {
    while (e.hasMoreElements()) {
      Organization org = (Organization)e.nextElement();

      // Pick up self org
      if (org.isSelf()) {
        selfOrgAsset = org;
      }
    }
  }

  private void checkForPrivateState(Enumeration e) {
    if (myPrivateState == null) {
      while(e.hasMoreElements()) {
        myPrivateState = (MyPrivateState) e.nextElement();
      }
    }
  }

  private void publishOplanCoupon() {
    OplanCoupon ow = new OplanCoupon(getMessageAddress());
    ow.setOplanQueryFile(queryFile);
    String oplanID = parseOplanID(queryFile);
    ow.setOplanID(oplanID);
    
    getUIDServer().registerUniqueObject(ow);
    getBlackboardService().publishAdd(ow);

    myPrivateState.oplanCouponExists = true;
    getBlackboardService().publishChange(myPrivateState);    
    //System.out.println("GLSInitServlet, globalParameters are: "+globalParameters);
  }
  
  private String parseOplanID(String queryFile) {
    String oplanId = null;
    int dot = queryFile.indexOf('.');
    oplanId = queryFile.substring(0,dot);
    
    return oplanId;
  }

  private void publishOplan() {
    // Need to make separate add/remove/modify lists
    getBlackboardService().openTransaction();
    publishOplanCoupon();
    getBlackboardService().closeTransactionDontReset();
    myPrivateState.unpublishedChanges=false;
  }

  private void refreshOplan( ){
    //Need to publish change to oplan coupon which will cause each agent
    //  to go back to db and compare new info with their current info.
    openTransaction();
    String oplanID = parseOplanID(queryFile);
    // Publish once for each changed oplan
    Collection coupons = getBlackboardService().query(new CouponPredicate(oplanID));
    for (Iterator couponIt = coupons.iterator(); couponIt.hasNext();) {
      getBlackboardService().publishChange(couponIt.next());
    }
    closeTransactionDontReset();
  }

  private class CouponPredicate implements UnaryPredicate {
    String _oplanID;
    public CouponPredicate(String oplanID) {
      _oplanID = oplanID;
    }
    public boolean execute(Object o) {
      if (o instanceof OplanCoupon) {
	if (((OplanCoupon ) o).getOplanID().equals(_oplanID)) {
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
    task.setSource(getMessageAddress());
    task.setDestination(getMessageAddress());
    
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
      System.out.println("GLSInitPlugin: Setting context to: " + oplanUID);
      task.setContext(context);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    publishAdd(task);
    System.out.println("\n" + formatDate(System.currentTimeMillis()) + " Send Task: " + task);
  }

//   private void publishOplanAndGLS() {
//     publishOplanCoupon();

//     //May not actually have the oplan at this point
    
//     for (Iterator iterator = oplanSubscription.iterator();
//          iterator.hasNext();) {
//       Object object = iterator.next();

//       if (object instanceof Oplan) {
//         doPublishRootGLS((Oplan) object);
//       }
//     }
//     myPrivateState.unpublishedChanges=false;
//   }
  
  protected static DateFormat logTimeFormat =
    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

  protected static String formatDate(long when) {
    return logTimeFormat.format(new Date(when));
  }


  private class GLSServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
      String command = request.getParameter("command");
      //System.out.println("GLSServlet got request command is" + command);
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
        // publish a change to the OplanCoupon
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







