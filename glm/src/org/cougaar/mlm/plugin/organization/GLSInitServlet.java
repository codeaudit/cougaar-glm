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

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.Collections;
import java.util.TreeSet;
import java.util.SortedSet;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Connection;

import java.text.NumberFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.util.UID;

import org.cougaar.util.DBProperties;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.Parameters;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.core.plugin.ComponentPlugin;

import org.cougaar.core.service.DomainService;
import org.cougaar.core.service.ServletService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.logging.LoggingServiceWithPrefix;

import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.ContextOfOplanIds;
//import org.cougaar.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.TimeAspectValue;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.plugin.util.PluginHelper;

import org.cougaar.glm.ldm.oplan.OplanStage;
import org.cougaar.glm.ldm.asset.Organization;

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
public class GLSInitServlet extends ComponentPlugin 
  implements GLSConstants {

  public static final String GETOPINFO = "getopinfo";
  public static final String PUBLISHGLS = "publishgls";
  public static final String RESCINDGLS = "rescindgls";

  private static final String PUBLISH_ON_SELF_ORG = "PublishOnSelfOrg";

  public static final String QUERY_FILE = "oplan.q";
  private static final String TIME_QUERY_NAME = "OplanTimeframeQuery";
  private static final String STAGE_QUERY_NAME = "OplanStageQuery";
  
  public static final int LISTENING_TO_NOTHING = 0;
  public static final int LISTENING_TO_PROP_REG_SRVCS= 1;
  public static final int LISTENING_TO_PROP_FIND_PROV = 2;
  public static final int LISTENING_TO_GLS = 3;

  static NumberFormat confidenceFormat = NumberFormat.getPercentInstance();

  private DBProperties dbp;
  private String database;
  private String username;
  private String password;

  private String oplanId;

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

  private IncrementalSubscription regSrvcsSubscription;

  private IncrementalSubscription findProvSubscription;

  private IncrementalSubscription myorgassets;

  private IncrementalSubscription requestSubscription;

  private static final String forRoot = "ForRoot".intern();

  // tells reply servlet when to push info
  private Object monitor = new Object();
		
  private static class MyPrivateState implements java.io.Serializable {
    boolean oplanInfoExists = false;
    boolean unpublishedChanges = false;
    int glsTaskNumber = 0;
    int prsTaskNumber = 0;
    int pfpTaskNumber = 0;
    OplanInfo opInfo;
    int listening_to = LISTENING_TO_NOTHING;
    boolean initialSendGLS = true;
    boolean initialSendFindProv = true;
  }

  private MyPrivateState myPrivateState;


  /**
   * This predicate selects for root GLStasks injected by the GLSInitServlet
   **/
  private UnaryPredicate glsPredicate =  new UnaryPredicate() {
      public boolean execute(Object o) {
	if (!(o instanceof PlanElement)) return false;
	PlanElement pe = (PlanElement) o;
	Task task = pe.getTask();
	if (!task.getVerb().equals(GET_LOG_SUPPORT)) return false;
	if (!task.getSource().equals(getAgentIdentifier())) return false;
	if (!task.getDestination().equals(getAgentIdentifier())) return false;
	return (task.getPrepositionalPhrase(forRoot) != null);
      }
    };

  /**
   * This predicate selects for root PropagateRegisterServices tasks injected by the GLSInitServlet
   **/
  private UnaryPredicate regSrvcsPredicate =  new UnaryPredicate() {
      public boolean execute(Object o) {
	if (!(o instanceof PlanElement)) return false;
	PlanElement pe = (PlanElement) o;
	Task task = pe.getTask();
	if (!task.getVerb().equals(PROPAGATE_REGISTER_SERVICES)) return false;
	if (!task.getSource().equals(getAgentIdentifier())) return false;
	if (!task.getDestination().equals(getAgentIdentifier())) return false;
	return (task.getPrepositionalPhrase(forRoot) != null);
      }
    };

  /**
   * This predicate selects for root PropagateFindProviders tasks injected by the GLSInitServlet
   **/
  private UnaryPredicate findProvPredicate =  new UnaryPredicate() {
      public boolean execute(Object o) {
	if (!(o instanceof PlanElement)) return false;
	PlanElement pe = (PlanElement) o;
	Task task = pe.getTask();
	if (!task.getVerb().equals(PROPAGATE_FIND_PROVIDERS)) return false;
	if (!task.getSource().equals(getAgentIdentifier())) return false;
	if (!task.getDestination().equals(getAgentIdentifier())) return false;
	return (task.getPrepositionalPhrase(forRoot) != null);
      }
    };


  // Subscribe to my private state object to recover on rehydrate
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

  private LoggingService logger;
  public void setLoggingService(LoggingService ls) {
    logger = ls;
  }

  private PlanningFactory theLDMF;
  public void setDomainService(DomainService ds) {
    if (ds == null)
      theLDMF = null;
    else {
      theLDMF = (PlanningFactory)ds.getFactory("planning");
    }
  }

  
  /*
   * Creates a subscription.
   */
  protected void setupSubscriptions() 
  { 
    logger = LoggingServiceWithPrefix.add(logger, getAgentIdentifier()+": ");
    
    Collection params = getParameters();
    if ((params == null) ||
        (params.size() == 0)) {
      throw new IllegalArgumentException("GLSInitServlet: Missing plugin parameter.");
    }

    // Get the OPLAN ID from the args.
    oplanId = (String) params.iterator().next();

    try {
      dbp = DBProperties.readQueryFile(QUERY_FILE);
      database = dbp.getProperty("Database");
      username = dbp.getProperty("Username");
      password = dbp.getProperty("Password");
    }catch (IOException ioe) {
      throw new RuntimeException("Can't read query file: "+QUERY_FILE, ioe);
    }

    glsSubscription = (IncrementalSubscription) blackboard.subscribe(glsPredicate);
    myorgassets = (IncrementalSubscription) blackboard.subscribe(orgAssetPred);
    requestSubscription = (IncrementalSubscription) blackboard.subscribe(requestPredicate);
    regSrvcsSubscription = (IncrementalSubscription) blackboard.subscribe(regSrvcsPredicate);
    findProvSubscription = (IncrementalSubscription) blackboard.subscribe(findProvPredicate);

    // Set up the local private state object
    // It is put on the blackboard for persistence and recovered 
    // on rehydration
    Collection stateColl = blackboard.query(statePredicate);

    // DEBUGGING
    if (blackboard.didRehydrate()){
      if ( stateColl.isEmpty()) {
        if (logger.isErrorEnabled()) logger.error("GLSInitServlet: myPrivateState object did not persist!" );
      }
      else {
        //get c0 and next stage info from state object
        myPrivateState = (MyPrivateState)stateColl.iterator().next();
	boolean haveOpInfo = (myPrivateState.opInfo != null);
        if (logger.isDebugEnabled()) logger.debug("GLSInitServlet- stateC0 is: " + (haveOpInfo ? ((Date)(myPrivateState.opInfo.getCDate())).toString() : "<undefined -- No OpInfo yet!>")); 
        SortedSet stateSentStages = (haveOpInfo ? myPrivateState.opInfo.getSentStages() : new TreeSet()); 
        if (logger.isDebugEnabled()) logger.debug("GLSInitServlet- stateSentStages has size: " +stateSentStages.size()); 

        //get c0 and next stage info from gls task
        if (glsSubscription.isEmpty()) {
          if (logger.isWarnEnabled()) logger.warn("GLSInitServlet- glsSubscription is empty on rehydration."); 
        }
        else {
          PlanElement pe = (PlanElement) glsSubscription.first();
          Task gls = pe.getTask();

          PrepositionalPhrase stagespp = gls.getPrepositionalPhrase(FOR_OPLAN_STAGES);
          SortedSet glsStages = (SortedSet)stagespp.getIndirectObject(); 
          if (logger.isDebugEnabled()) logger.debug("GLSInitServlet- glsStages have size: " +glsStages.size()); 

          PrepositionalPhrase c0pp = gls.getPrepositionalPhrase(WITH_C0);
          long  gls_c0 = ((Long)(c0pp.getIndirectObject())).longValue();
          Date gls_c0_date = new Date(gls_c0);
          if (logger.isDebugEnabled()) logger.debug("GLSInitServlet- gls_c0_date is: " +gls_c0_date); 

          //compare c0 from private state to c0 in gls task
          //TO_DO
          //compare number of sent stages  from private state to sent stages in gls task
          if (stateSentStages.size() != glsStages.size()) {
            if (logger.isErrorEnabled()) {
              logger.error("GLSInitServlet: myPrivateState sent Stages do not match GLS task after rehyration.");
            }
          }
        }
      }
    }
    // EMD DEBUGGING

    if (stateColl.isEmpty()){
      myPrivateState = new MyPrivateState();
      blackboard.publishAdd(myPrivateState);
    }
    else {
      myPrivateState = (MyPrivateState)stateColl.iterator().next();
    }

    // register with servlet service
    try {
      servletService.register("/glsinit", new GLSServlet());
      servletService.register("/glsreply", new GLSReplyServlet());
    } catch (Exception e) {
      e.printStackTrace();
    }

    
    //if (logger.isDebugEnabled()) logger.debug("GLSInitServlet: " 
    //                                          + PUBLISH_ON_SELF_ORG + " = " + 
    //                                          globalParameters.get(PUBLISH_ON_SELF_ORG));
  }	   		 


  /**
   * Executes Plugin functionality.
   */
  public void execute(){

    switch (myPrivateState.listening_to) {

    case LISTENING_TO_PROP_REG_SRVCS:
      if (regSrvcsSubscription.hasChanged()) {
        boolean readyForFP = checkIfStageComplete(regSrvcsSubscription.getChangedCollection());
        if (readyForFP) {
          boolean hasFindProvidersStep = checkForFindProviders();
          if (hasFindProvidersStep) {
            myPrivateState.initialSendFindProv = false;
            myPrivateState.opInfo.advanceStage();
            myPrivateState.listening_to = LISTENING_TO_PROP_FIND_PROV;
            sendPropagateFindProviders();
          }
          else {
            myPrivateState.initialSendGLS = false;
            myPrivateState.opInfo.advanceStage();
            myPrivateState.listening_to = LISTENING_TO_GLS;
            sendGLS();
          }
        }
      }
      break;
    case LISTENING_TO_PROP_FIND_PROV:
      if (findProvSubscription.hasChanged()) {
        boolean readyForStage = checkIfStageComplete(findProvSubscription.getChangedCollection());
        if (readyForStage) {
          myPrivateState.listening_to = LISTENING_TO_GLS;
          if (myPrivateState.initialSendGLS) {
            myPrivateState.initialSendGLS = false;
          } else if (logger.isDebugEnabled()) {
	    logger.debug("GLSInitServlet: automatically advancing to next Stage");
	  }
	  sendRootGLS();
        }
      }
      break;

    case LISTENING_TO_GLS:
      if (glsSubscription.hasChanged()) {
        boolean readyForNext = checkIfStageComplete(glsSubscription.getChangedCollection());
        if (readyForNext) {
          myPrivateState.listening_to = LISTENING_TO_NOTHING;
          myPrivateState.opInfo.updateDisplayStage();
          blackboard.publishChange(myPrivateState);
          doNotify();
        }
      }
      break;
    }

    if (requestSubscription.hasChanged()) {
      processRequests(requestSubscription.getAddedCollection());
    }
  }

  private boolean checkForFindProviders(){
    SortedSet remaining = myPrivateState.opInfo.getRemainingStages(); 
    if (!remaining.isEmpty()) {
      int nextStageNum = ((OplanStage)(remaining.first())).getNumber();
      if ((nextStageNum & 1) != 0) { //odd stage number indicates FindProviders Stage
        if (logger.isDebugEnabled()) {
          logger.debug("GLSInitServlet: Found a FindProviders Stage");
        }
        return true;
      }
    }
    return false;
  }


  private boolean checkIfStageComplete(Collection changedRootTaskPlanElements) {
    double confidence = 0;
    if (changedRootTaskPlanElements.size() > 1 ) {
      logger.error("GLSInitServlet: Mulitple root task plan element changed - " +
		   changedRootTaskPlanElements);
    }
    for (Iterator i = changedRootTaskPlanElements.iterator(); i.hasNext(); ) {
      PlanElement pe = (PlanElement) i.next();

      AllocationResult ar = null;
      if (pe != null) {
        ar = pe.getEstimatedResult();
        confidence = ar.getConfidenceRating();
        if (logger.isDebugEnabled()) {
          switch (myPrivateState.listening_to) {
          case LISTENING_TO_NOTHING:
            logger.debug("checkIfStageComplete():  State is LISTENING_TO_NOTHING");
          break;
          case LISTENING_TO_PROP_REG_SRVCS:
            logger.debug("checkIfStageComplete():  State is LISTENING_TO_PROP_REG_SRVCS");
          break;
          case LISTENING_TO_PROP_FIND_PROV:
            logger.debug("checkIfStageComplete():  State is LISTENING_TO_PROP_FIND_PROV");
          break;
          case LISTENING_TO_GLS:
            logger.debug("checkIfStageComplete():  State is LISTENING_TO_GLS");
          break;
          } //end switch
          logger.debug("GLSInitServlet: changed task is " +pe.getTask());
          logger.debug("GLSInitServlet: confidence of changed task - " +confidence );
        }
      }
      if (confidence >= 1.0){
        //ready to move to next stage
        if (logger.isDebugEnabled()) {
          logger.debug("GLSInitServlet: Stage is Complete- Confidence is- " +confidence +"\n");
        }
        return true;
      }
    }
    return false;
  }

  private void doNotify() {
      synchronized(monitor) {
	monitor.notifyAll();
      }
  }    

  private void processRequests(Collection newRequests){
    for (Iterator i = newRequests.iterator(); i.hasNext(); ) {
      Request request = (Request) i.next();
      if (request.command.equals(GETOPINFO)) {
        publishOplan();
      } else if (request.command.equals(PUBLISHGLS)) {
	publishAllRootGLS();
      }
      blackboard.publishRemove(request);
    }
  }

  private Organization getSelfOrg() {
    for (Iterator iterator = myorgassets.iterator();
	 iterator.hasNext();) {
      Organization org = (Organization) iterator.next();

      // Pick up self org
      if (org.isSelf()) {
	return org;
      }
    }

    return null;

  }
  
  private void readOplanTimeframe() 
    throws SQLException, IOException {

    String oplan_opName;
    int min_planning_offset;
    int start_offset;
    int end_offset;
    String stage_name;
    String stage_desc;
    SortedSet ss = new TreeSet();

    try {
      String dbtype = dbp.getDBType();
      insureDriverClass(dbtype);
      Connection conn =  DBConnectionPool.getConnection(database, username, password);
      try {
        Statement stmt = conn.createStatement();
        String query = dbp.getQuery(TIME_QUERY_NAME, 
                                    Collections.singletonMap(":oplanid:", oplanId));

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

        query = dbp.getQuery(STAGE_QUERY_NAME, 
                                    Collections.singletonMap(":oplanid:", oplanId));

        rs = stmt.executeQuery(query);
        while (rs.next()) {
          if (rs.getObject(1) instanceof String) {
            stage_name = ((String)rs.getObject(1));
          }
          else
            stage_name = new String ((byte[])rs.getObject(1),"US-ASCII");
          int stage_num = ((Number)(rs.getObject(2))).intValue();
          if (rs.getObject(3) instanceof String) {
            stage_desc = ((String)rs.getObject(3));
          }
          else
            stage_desc = new String ((byte[])rs.getObject(3),"US-ASCII");
          OplanStage op = new OplanStage(stage_num, stage_name, stage_desc);
          ss.add(op);
        }



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
    myPrivateState.opInfo = new OplanInfo(oplanId, 
                                  oplan_opName, 
                                  start_time, 
                                  min_planning_offset, 
                                  start_offset, 
                                  end_offset,
                                  ss);

    if (logger.isDebugEnabled()) {
      logger.debug("GLSInitServlet: OplanStages from db are: "
                   +myPrivateState.opInfo.getRemainingStages());
    }
    
    blackboard.publishChange(myPrivateState);
    doNotify();

  }

  private void insureDriverClass(String dbtype) throws SQLException, ClassNotFoundException {
    String driverParam = "driver." + dbtype;
    String driverClass = Parameters.findParameter(driverParam);
    if (driverClass == null) {
      // this is likely a "cougaar.rc" problem.
      // Parameters should be modified to help generate this exception:
      throw new SQLException("Unable to find driver class for \""
                             +driverParam+"\" -- check your \"cougaar.rc\"");
    }
    Class.forName(driverClass);
  }
  
  private void publishOplan() {
    if (myPrivateState.oplanInfoExists) {
      // Calling this is an error
      if (logger.isWarnEnabled())
	logger.warn("Not re-fetching Oplan Info", new Throwable());
      return;
    }

    blackboard.openTransaction();
    try {
      readOplanTimeframe();
    } catch (Exception e) {
      logger.error("Failed to get oplan timeframe from DB", e);
    }
    myPrivateState.oplanInfoExists = true;
    blackboard.publishChange(myPrivateState);    
    blackboard.closeTransactionDontReset();
  }

  private void publishAllRootGLS() {
    sendGLS();
  }

  public void PublishRootTask(String oplanID, String c0_date) {
    blackboard.openTransaction();
    if (myPrivateState.initialSendGLS) {
      myPrivateState.opInfo.setCDate(c0_date); 
      myPrivateState.listening_to = LISTENING_TO_PROP_REG_SRVCS;
      sendPropagateRegisterServices();
    } else if (checkForFindProviders()) {
      myPrivateState.listening_to = LISTENING_TO_PROP_FIND_PROV;
      myPrivateState.opInfo.advanceStage();
      if (myPrivateState.initialSendFindProv) {
	myPrivateState.initialSendFindProv = false;
	sendPropagateFindProviders();
      }
    } else {
      myPrivateState.listening_to = LISTENING_TO_GLS;
      sendRootGLS();
    }
    blackboard.closeTransactionDontReset();
  }

  public void sendRootGLS() {
    SortedSet remaining =myPrivateState.opInfo.getRemainingStages(); 
    if (remaining.isEmpty()) {
      return;
    }
    myPrivateState.opInfo.advanceStage();
    sendGLS();
    blackboard.publishChange(myPrivateState);    
    if (logger.isDebugEnabled()) {
      logger.debug(getAgentIdentifier() + 
		   "sendRootGLS: remainingStages are " +
                   myPrivateState.opInfo.getRemainingStages() );
    }
  }

  private void sendPropagateRegisterServices() {
    NewTask task = theLDMF.newTask();
    // ensure this is a root level task
    task.setPlan(theLDMF.getRealityPlan());
    task.setSource(getAgentIdentifier());
    task.setDestination(getAgentIdentifier());
    
    // set prepositional phrases
    Vector phrases = new Vector(2);
    NewPrepositionalPhrase newpp;

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(FOR_ORGANIZATION);
    newpp.setIndirectObject(getSelfOrg());
    phrases.add(newpp);

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(FOR_ROOT);
    newpp.setIndirectObject(new Integer(++myPrivateState.prsTaskNumber));
    phrases.add(newpp);
    task.setPrepositionalPhrases(phrases.elements());

    // Set the context
    try {
      ContextOfOplanIds context = new ContextOfOplanIds(oplanId);
      task.setContext(context);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    // verb
    task.setVerb(PROPAGATE_REGISTER_SERVICES);

    blackboard.publishChange(myPrivateState);
    blackboard.publishAdd(task);
  }

  private void sendPropagateFindProviders() {
    NewTask task = theLDMF.newTask();
    // ensure this is a root level task
    task.setPlan(theLDMF.getRealityPlan());
    task.setSource(getAgentIdentifier());
    task.setDestination(getAgentIdentifier());
    
    // set prepositional phrases
    Vector phrases = new Vector(3);
    NewPrepositionalPhrase newpp;

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(FOR_ORGANIZATION);
    newpp.setIndirectObject(getSelfOrg());
    phrases.add(newpp);

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(FOR_ROOT);
    newpp.setIndirectObject(new Integer(++myPrivateState.pfpTaskNumber));
    phrases.add(newpp);
    phrases.add(makeForOplanStagesPhrase());

    task.setPrepositionalPhrases(phrases.elements());

    // Set the context
    try {
      ContextOfOplanIds context = new ContextOfOplanIds(oplanId);
      task.setContext(context);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    // verb
    task.setVerb(PROPAGATE_FIND_PROVIDERS);

    blackboard.publishChange(myPrivateState);
    blackboard.publishAdd(task);
  }



  private void sendGLS() {
    NewTask task = theLDMF.newTask();
    // ensure this is a root level task
    task.setPlan(theLDMF.getRealityPlan());
    task.setSource(getAgentIdentifier());
    task.setDestination(getAgentIdentifier());
    
    // set prepositional phrases
    Vector phrases = new Vector(4);
    NewPrepositionalPhrase newpp;

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(FOR_ORGANIZATION);
    newpp.setIndirectObject(getSelfOrg());
    phrases.add(newpp);

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(FOR_ROOT);
    newpp.setIndirectObject(new Integer(++myPrivateState.glsTaskNumber));
    phrases.add(newpp);

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(WITH_C0);
    newpp.setIndirectObject(new Long(myPrivateState.opInfo.getCDate().getTime()));
    phrases.add(newpp);
    phrases.add(makeForOplanStagesPhrase());
    blackboard.publishChange(myPrivateState);

    task.setPrepositionalPhrases(phrases.elements());

    // verb
    task.setVerb(GET_LOG_SUPPORT);

    // schedule
    long startTime = myPrivateState.opInfo.getStartDay().getTime();
    long endTime = myPrivateState.opInfo.getEndDay().getTime();

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
      ContextOfOplanIds context = new ContextOfOplanIds(oplanId);
      if (logger.isDebugEnabled()) {
        logger.debug("GLSInitPlugin: Setting context to: " + oplanId);      
      }
      task.setContext(context);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    blackboard.publishAdd(task);

    if (logger.isDebugEnabled()) {
      logger.debug("\n" + formatDate(System.currentTimeMillis()) 
                   + " Send Task: " + task);
    }
  }

  private NewPrepositionalPhrase makeForOplanStagesPhrase() {
    NewPrepositionalPhrase newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(FOR_OPLAN_STAGES);
    newpp.setIndirectObject(new TreeSet(myPrivateState.opInfo.getSentStages()));
    return newpp;
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
    private SortedSet remainingStages;
    private SortedSet sentStages;
    private OplanStage displayStage;

    public OplanInfo(String opId, 
                     String opName, 
                     long start_time,
                     int min_planning_offset, 
                     int start_offset, 
                     int end_offset,
                     SortedSet s)  {
      this.opId = opId;
      this.opName = opName;
      this.start_time = start_time;
      this.min_planning_offset = min_planning_offset;
      this.start_offset = start_offset;
      this.end_offset = end_offset;
      remainingStages = s;
      sentStages = new TreeSet();

      updateDisplayStage();
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
      long endCalc = cDate.getTime() + (end_offset * 86400000L);// days in milliseconds
      endDay = new Date(endCalc);
    }
    public SortedSet getRemainingStages() {
      return remainingStages;
    }

    public SortedSet getSentStages() {
      return sentStages;
    }

    public void setDisplayStage(OplanStage dispStage){
      displayStage = dispStage;
    }
    
    public OplanStage getDisplayStage() {
      return displayStage;
    }

    public void updateDisplayStage() {
      for (Iterator i = remainingStages.iterator(); i.hasNext();) {
        OplanStage os = (OplanStage) i.next();
        //check whether stage is evenly numbered indicating major stage
        if ((os.getNumber() & 1) == 0) { 
          setDisplayStage(os);
          break;
        }
      }
    }
    
    public void advanceStage() {
      OplanStage op = (OplanStage)remainingStages.first();
      sentStages.add(op);
      remainingStages.remove(op);
    }

    public void resetStages() {
      for(Iterator i = sentStages.iterator(); i.hasNext();){
        remainingStages.add((OplanStage)i.next());
      }
    }
  }


  private class GLSServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
      String command = request.getParameter("command");
      if (logger.isDebugEnabled()) logger.debug("GLSServlet got request command is: " + command);
      response.setContentType("text/html");
      try {
	PrintWriter out = response.getWriter();
	out.println("<html><head></head><body><" +request.getParameter("command") + "></body></html>");
	out.close();
      } catch (java.io.IOException ie) { ie.printStackTrace(); }

      if (command.equals(GETOPINFO)) {
	publishOplan();
      }
      if (command.equals(PUBLISHGLS)) {
	if (logger.isDebugEnabled()) logger.debug("oplanID is " + request.getParameter("oplanID"));
 	if (logger.isDebugEnabled()) logger.debug("cDay is " + request.getParameter("c0_date"));
        String oplanID = request.getParameter("oplanID");
        String c0 = request.getParameter("c0_date");
	PublishRootTask(oplanID, c0);
      }
    }
  }

  private class GLSReplyServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
      String command = request.getParameter("command");
      if (logger.isDebugEnabled()) {
        logger.debug("GLSReplyServlet got request " + command);
      }
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
          if (logger.isDebugEnabled()) {
            logger.debug("GLSInitServlet: Refreshing GLSClient.");
          }
          if (myPrivateState.opInfo != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("<oplan name=" );
            sb.append(myPrivateState.opInfo.getOpName());
            sb.append(" id=");
            sb.append(myPrivateState.opInfo.getOpId());
            sb.append(" c0_date=");
            sb.append(cDateFormat.format(myPrivateState.opInfo.getCDate()));
            sb.append(" nextStage=");

            SortedSet remaining =myPrivateState.opInfo.getRemainingStages(); 
            if (remaining.isEmpty()) {
              sb.append("All Stages Sent");
              sb.append(" stageDesc=");
            }
            else {
              sb.append(((OplanStage)(myPrivateState.opInfo.getDisplayStage())).getName());
              sb.append(" stageDesc=");
              sb.append(((OplanStage)(myPrivateState.opInfo.getDisplayStage())).getDescription());
            }
            sb.append(">");
            out.println(sb);
            if (logger.isDebugEnabled()) {
              logger.debug("GLSInitServlet: ReplyWorker stringbuffer :"+sb);
            }
          }
          
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
      } catch (java.io.IOException ie) {
        ie.printStackTrace(); 
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
