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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.Collections;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Connection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.util.UID;

import org.cougaar.util.DBProperties;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.Parameters;
import org.cougaar.util.UnaryPredicate;
  
import org.cougaar.core.service.ServletService;

import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.ContextOfOplanIds;
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
import org.cougaar.glm.ldm.asset.Organization;

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
//   public static final String UPDATEOPLAN = "updateoplan";
  public static final String PUBLISHGLS = "publishgls";
  public static final String RESCINDGLS = "rescindgls";

  private static final String PUBLISH_ON_SELF_ORG = "PublishOnSelfOrg";

  private static final String QUERY_NAME = "OplanTimeframeQuery";

  private DBProperties dbp;
  private String database;
  private String username;
  private String password;

  private static DateFormat cDateFormat = new SimpleDateFormat("yyyy/MM/dd");

  /**
   * For making direct request on this plugin (not via servlet).
   **/
  public static class Request implements Serializable {
    private String command;
    public Request(String command) {
      this.command = command;
    }
  }

  private IncrementalSubscription glsSubscription;

  private IncrementalSubscription oplanInfoSubscription;

  private IncrementalSubscription myorgassets;

  private IncrementalSubscription stateSubscription;

  private IncrementalSubscription requestSubscription;

  /** for knowing when we get our self org asset **/
  private Organization selfOrgAsset = null;

  private static final String forRoot = "ForRoot".intern();

  // tells reply servlet when to push info
  private Object monitor = new Object();
		
  private static class MyPrivateState implements java.io.Serializable {
    boolean oplanInfoExists = false;
    boolean unpublishedChanges = false;
    boolean errorOccurred = false;
    int taskNumber = 0;
  }

  private MyPrivateState myPrivateState;

  private OplanInfo myOplanInfo;

  private static UnaryPredicate myOplanInfoPredicate = new UnaryPredicate() {
      public boolean execute(Object o) { 
        if (o instanceof OplanInfo) {
          return true;
        } else {
          return false;
        }
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
    initProperties();  //super
    grokArguments();   //super
    
    getBlackboardService().getSubscriber().setShouldBePersisted(false);

    stateSubscription = (IncrementalSubscription) getBlackboardService().subscribe(statePredicate);
    oplanInfoSubscription = (IncrementalSubscription) getBlackboardService().subscribe(myOplanInfoPredicate);
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
        publishOplan(); //reads db and sets default cDate
        String opId = parseOplanID(queryFile); //using default cDate
        OplanInfo opInfo = findOplanById(opId);
        sendGLS(opInfo);
      }
    }
        
    if (glsSubscription.hasChanged()) {
      doNotify = true;
    }

    if (oplanInfoSubscription.hasChanged()) {
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

  private void processRequests(Collection newRequests){
    for (Iterator i = newRequests.iterator(); i.hasNext(); ) {
      Request request = (Request) i.next();
      if (request.command.equals("sendoplan")) {
        publishOplan();
      } else if (request.command.equals("publishgls")) {
	publishAllRootGLS();
      }
      publishRemove(request);
    }
  }

  /** finds the selected oplan using its ID as a key
  */
  private OplanInfo findOplanById(String oplanID) {
    synchronized(oplanInfoSubscription) {
      for (Iterator iterator = oplanInfoSubscription.iterator(); 
	   iterator.hasNext();) {
	OplanInfo moi = (OplanInfo) iterator.next();
	if (oplanID.equals(moi.getOpId())) {
	  return moi;
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

  private void readOplanTimeframe() 
    throws SQLException, IOException {

    String oplanId = parseOplanID(queryFile);

    dbp = DBProperties.readQueryFile(queryFile);
    database = dbp.getProperty("Database");
    username = dbp.getProperty("Username");
    password = dbp.getProperty("Password");

    String oplan_opName;
    int min_planning_offset;
    int start_offset;
    int end_offset;

    try {
      String dbtype = dbp.getDBType();
      insureDriverClass(dbtype);
      Connection conn =  DBConnectionPool.getConnection(database, username, password);
      try {
        Statement stmt = conn.createStatement();
        String query = dbp.getQuery("OplanTimeframeQuery", 
                                    Collections.singletonMap(":oplan_id:", oplanId));
        ResultSet rs = stmt.executeQuery(query);
        if ( rs.next()) {
          if (rs.getObject(1) instanceof String)
            oplan_opName = ((String)rs.getObject(1));
          else
            oplan_opName = new String ((byte[])rs.getObject(1),"US-ASCII");
          min_planning_offset = ((Number)(rs.getObject(2))).intValue();
          start_offset = ((Number)(rs.getObject(3))).intValue();
          end_offset = ((Number)(rs.getObject(4))).intValue();
        }
        else {
          throw new SQLException("No results from query:" +query);
        }
        rs.close();
        stmt.close();
      } catch (Exception except){
        if (except instanceof SQLException) {
          throw (SQLException) except;
        }
        SQLException myEx1 = new SQLException("Query failed.");
        myEx1.initCause(except);
        throw myEx1;
      }
      finally {
        conn.close();
      }
      
    } catch (Exception e) {
      if (e instanceof SQLException) {
        throw (SQLException) e;
      }
      SQLException myEx = new SQLException("Driver not found for " + database);
      myEx.initCause(e);
      throw myEx;
    }

    long start_time = currentTimeMillis();
    OplanInfo moi = new OplanInfo(oplanId, 
                                  oplan_opName, 
                                  start_time, 
                                  min_planning_offset, 
                                  start_offset, 
                                  end_offset);
    getBlackboardService().publishAdd(moi);

  }

  private void insureDriverClass(String dbtype) throws SQLException, ClassNotFoundException {
    String driverParam = "driver." + dbtype;
    String driverClass = Parameters.findParameter(driverParam);
    if (driverClass == null) {
      // this is likely a "cougaar.rc" problem.
      // Parameters should be modified to help generate this exception:
      throw new SQLException("Unable to find driver class for \""+
                             driverParam+"\" -- check your \"cougaar.rc\"");
    }
    Class.forName(driverClass);
  }
  
  private String parseOplanID(String queryFile) {
    String oplanId = null;
    int dot = queryFile.indexOf('.');
    oplanId = queryFile.substring(0,dot);
    
    return oplanId;
  }

  private void publishOplan() {
    getBlackboardService().openTransaction();
    try {
      readOplanTimeframe();
    } catch (Exception e) {
      e.printStackTrace();
    }
    myPrivateState.oplanInfoExists = true;
    getBlackboardService().publishChange(myPrivateState);    
    getBlackboardService().closeTransactionDontReset();
    myPrivateState.unpublishedChanges=false;
  }

  private void publishAllRootGLS() {
    for (Iterator i = oplanInfoSubscription.iterator(); i.hasNext(); ) {
      OplanInfo moi = (OplanInfo) i.next();
        sendGLS(moi);
    }
  }

  public void publishRootGLS(String oplanID, String c0_date) {
    openTransaction();
    OplanInfo oi = findOplanById(oplanID);
    oi.setCDate(c0_date);
    sendGLS(oi);
    closeTransactionDontReset();
  }

  public void rescindRootGLS(String oplanID) {
    openTransaction();
    OplanInfo myOpIn = findOplanById(oplanID);
    System.out.println("rescindRootGLS() myOpIn " + myOpIn);
    for (Iterator it = glsSubscription.iterator(); it.hasNext();) {
      Task t = (Task) it.next();
      ContextOfOplanIds coi = (ContextOfOplanIds) t.getContext();
      if (coi.contains(myOpIn.getOpId())) {
	publishRemove(t);
      }
    }
    closeTransactionDontReset();
  }

  private void sendGLS(OplanInfo moi) {
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
    phrases.add(newpp);

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition("WithC0");
    newpp.setIndirectObject(new Long(moi.getCDate().getTime()));
    phrases.add(newpp);

    publishChange(myPrivateState);

    task.setPrepositionalPhrases(phrases.elements());

    // verb
    task.setVerb(Constants.Verb.GetLogSupport);

    // schedule
    long startTime = moi.getStartDay().getTime();
    long endTime = moi.getEndDay().getTime();

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
      String oplanId = parseOplanID(queryFile);
      ContextOfOplanIds context = new ContextOfOplanIds(oplanId);
      System.out.println("GLSInitPlugin: Setting context to: " + oplanId);      
      task.setContext(context);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    publishAdd(task);
    System.out.println("\n" + formatDate(System.currentTimeMillis()) + " Send Task: " + task);
  }
  
  protected static DateFormat logTimeFormat =
    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

  protected static String formatDate(long when) {
    return logTimeFormat.format(new Date(when));
  }

  private static class OplanInfo implements java.io.Serializable {
    private String opId;
    private String opName;
    private int min_planning_offset;
    private int start_offset;
    private int end_offset;
    private Date cDate;
    private Date startDay;
    private Date endDay;
    private long start_time;

    public OplanInfo(String opId, 
                     String opName, 
                     long start_time,
                     int min_planning_offset, 
                     int start_offset, 
                     int end_offset)  {
      this.opId = opId;
      this.opName = opName;
      this.start_time = start_time;
      this.min_planning_offset = min_planning_offset;
      this.start_offset = start_offset;
      this.end_offset = end_offset;

      setCDate();
    }
    public String getOpName(){
      return opName;
    }
    public String getOpId() {
      return opId;
    }
    public Date getCDate() {
      return cDate;
    }
    public void setCDate(String c0) {
       try {
         cDate = cDateFormat.parse(c0);
       }catch (ParseException pe) {
         pe.printStackTrace();
       }
       setStartDay();
       setEndDay();
    }
    public void setCDate()  {      
      long c0calc = start_time - (min_planning_offset * 86400000L);
      cDate = new Date(c0calc);
      String cDateFormatted = cDateFormat.format(cDate);
      setCDate(cDateFormatted);
    }
    public Date getStartDay() {
      return startDay;
    }
    public Date getEndDay() {
      return endDay;
    }
    public void setStartDay() {
      long startCalc = cDate.getTime() + (start_offset * 86400000L);
      startDay = new Date(startCalc);

    }
    public void setEndDay() {
      long endCalc = cDate.getTime() + (end_offset * 86400000L);  // days in milliseconds
      endDay = new Date(endCalc);
    }
  }


  private class GLSServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
      String command = request.getParameter("command");
      //System.out.println("GLSServlet got request command is: " + command);
      response.setContentType("text/html");
      try {
	PrintWriter out = response.getWriter();
	out.println("<html><head></head><body><" +request.getParameter("command") + "></body></html>");
	out.close();
      } catch (java.io.IOException ie) { ie.printStackTrace(); }

      if (command.equals(SENDOPLAN)) {
	publishOplan();
      }
//       if (command.equals(UPDATEOPLAN)) {
// 	refreshOplan();
//       }
      if (command.equals(PUBLISHGLS)) {
	//System.out.println("oplanID is " + request.getParameter("oplanID"));
 	//System.out.println("cDay is " + request.getParameter("c0_date"));
        String oplanID = request.getParameter("oplanID");
        String c0 = request.getParameter("c0_date");
	publishRootGLS(oplanID, c0);
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
      //System.out.println("GLSReplyServlet got request " + command);
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
	  for (Iterator it = oplanInfoSubscription.iterator(); it.hasNext(); ) {
	    OplanInfo myOpInfo = (OplanInfo) it.next();
	    sb.append("<oplan name=" );
	    sb.append(myOpInfo.getOpName());
	    sb.append(" id=");
	    sb.append(myOpInfo.getOpId());
            sb.append( "c0_date=");
            sb.append(cDateFormat.format(myOpInfo.getCDate()));
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

