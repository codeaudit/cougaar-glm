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

package org.cougaar.mlm.construction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.ClassIVConstructionMaterial;
import org.cougaar.glm.ldm.asset.NewSupplyClassPG;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.ScheduledContentPG;
import org.cougaar.glm.plugins.AssetUtils;
import org.cougaar.glm.plugins.BasicProcessor;
import org.cougaar.glm.plugins.GLMDecorationPlugin;
import org.cougaar.glm.plugins.ScheduleUtils;
import org.cougaar.glm.plugins.TimeUtils;
import org.cougaar.glm.plugins.projection.ConsumerSpec;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.ldm.measure.Count;
import org.cougaar.planning.ldm.measure.CountRate;
import org.cougaar.planning.ldm.measure.Duration;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.util.Parameters;
import org.cougaar.util.UnaryPredicate;

/*
 * Construction (ClassIV) plugin that takes construction tasks and 
 * creates GenerateProjection tasks.
 * @author  ALPINE <alpine-software@bbn.com>
 *
 **/

public class ConstructionGenerateDemandProjector extends BasicProcessor {


	// 86400000 msec/day = 1000msec/sec * 60sec/min *60min/hr * 24 hr/day
	protected static final long              MSEC_PER_DAY =  86400000;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private MessageAddress meCluster = null;
	private GLMDecorationPlugin thisPlugin_ = null;

	protected IncrementalSubscription constructionDemandSub;
	protected IncrementalSubscription generateConstructionProjectionSub;
	protected IncrementalSubscription taskMaterielPolicySub;

	private String className = "ConstructionGenerateDemandProjector";
	private Organization myOrganization = null;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public ConstructionGenerateDemandProjector
			( GLMDecorationPlugin glmPlugin, Organization myOrg ) {

		super ( glmPlugin, myOrg );
		thisPlugin_ = glmPlugin;
		this.myOrganization = myOrg;

		initializeSubscriptions();

	} // constructor

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


	protected void initializeSubscriptions () {
		constructionDemandSub = subscribe ( constructionDemandPred );

		constructionDemandSub =
			subscribe ( constructionDemandPred );

		generateConstructionProjectionSub =
			subscribe ( generateConstructionProjectionPred );

	} // initializeSubscriptions


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public void update () {

                //debug("In update method......");
		// This isn't really replan, this means need to plan
		boolean replan = false;

		if ( constructionDemandSub.getChangedList().hasMoreElements()) {
			// How would this happen???
			replan = true;
		} // if

   //debug("replan is " + replan); 
	 // At this point we have OPlan and ORG activities
		 if (replan) {
                    //debug("replan is true...."); 
			// Will do first process tasks
			// replan, reprocess everything
			processTasks (constructionDemandSub.elements());

		 } else { 
      //debug("in the else of the if, addedList.hasmore is " + 
      if (constructionDemandSub.getAddedList().hasMoreElements()) {
			// Will do second process tasks
			// expand, create and publish new tasks
			processTasks (constructionDemandSub.getAddedList());
                    }
		 } // if
	} // update


	public boolean processTasks (Enumeration tasks) {

//debug (" in processTasks");
		if (!tasks.hasMoreElements()) {
			return false;
		} // if
//debug (" <1>");

		// Project Demand
		Hashtable consumerSchedules_;
		consumerSchedules_ = createConsumerSchedules();
		if (consumerSchedules_.isEmpty() ) {
			return false;
		} // if
//debug (" <2>");

		// this should only be one element - a single constructionDemand task
		//  of a particular type
		while (tasks.hasMoreElements()) {
			if ( processTask((Task)tasks.nextElement(),
					consumerSchedules_) ) {
				return true;
			} // if
		} // while
		return false;
	} // processTasks


	public boolean processTask
		(Task task, Hashtable consumerSchedules_ ) {
                //debug (" in processTask");

		Vector demand_tasks = createDemandTasks (task, consumerSchedules_);
		if ( (demand_tasks == null) || (demand_tasks.size() == 0)) {
			return true;
		} // if
		publishExpansion (task, demand_tasks);
		return false;
	} // processTask

	protected Vector createDemandTasks
		(Task parent_task, Hashtable consumerSchedules_) {

                //debug (" in createDemandTasks");

		Asset consumer;
		String consumer_id;
		ConstructionDemandSpec spec;
		Schedule consumer_sched;
    //create materiel_sched off start and end preferences off of parent task
    long start = 0;
    long end = 0;
    Enumeration parentprefs = parent_task.getPreferences();
    while (parentprefs.hasMoreElements()) {
      Preference apref = (Preference)parentprefs.nextElement();
      if (apref.getAspectType() == AspectType.START_TIME) {
        start = (long)apref.getScoringFunction().getBest().getValue();
      } else if (apref.getAspectType() == AspectType.END_TIME) {
        end = (long)apref.getScoringFunction().getBest().getValue();
      }
    }
    Schedule materiel_sched = ScheduleUtils.buildSimpleQuantitySchedule(1, start, end);

    //System.out.println("&&& parent task is: " + parent_task);
    //System.out.println("\n &&&&&&& materiel_sched is "+materiel_sched);

		// Project demand for each consumer in the cluster.
		Enumeration assets = consumerSchedules_.keys();
		Vector tasks = new Vector();
		//Vector glm_oplans = thisPlugin_.getOPlans();
                //debug (" glm_oplans is " + glm_oplans);
		while (assets.hasMoreElements()) {


			consumer = (Asset) assets.nextElement();
                        //debug (" working with asset " + consumer);
			consumer_sched = (Schedule) consumerSchedules_.get(consumer);
       spec = new ConstructionDemandSpec (consumer, consumer_sched,
                             materiel_sched, ldmFactory_ );

                   
       addAssetsToConsumerSpec(parent_task, spec);

				Task dTask = newDemandTask (parent_task, consumer, spec);
				tasks.add (dTask);
		} // while
		return tasks;
	} // createDemandTasks

    /**
     *  Looks up the required assets from the asset table and adds these assets to
     *  the demand spec.  The rates a calculated and added also. 
     */
    private void addAssetsToConsumerSpec(Task task, ConstructionDemandSpec spec) {

        String taskVerb = task.getVerb().toString();

	//Collection subtasks = new ArrayList();
      
        try {
            String rawdb = Parameters.replaceParameters("${construction.database}");
            int colonIndex1 = rawdb.indexOf(':');
            int colonIndex2 = rawdb.indexOf(':', colonIndex1+1);
            String dbType = rawdb.substring(colonIndex1+1, colonIndex2);  
            Class.forName(Parameters.findParameter("driver."+dbType));
            Connection conn = null;
            ResultSet rset = null;

            try {

                // Connect to the database
                // You must put a database name after the @ sign in the connection URL.
                // You can use either the fully specified SQL*net syntax or a short cut
                // syntax as <host>:<port>:<sid>.  The example uses the short cut syntax.
                //get the db info out of the cougaarrc file
                String dbinfo = Parameters.replaceParameters(rawdb);
                String rawuser = "${construction.user}";
                String dbuser = Parameters.replaceParameters(rawuser);
                String rawpasswd = "${construction.password}";
                String dbpasswd = Parameters.replaceParameters(rawpasswd);

		conn = DriverManager.getConnection (dbinfo, dbuser, dbpasswd );

                String parentverb = task.getVerb().toString();
                // Create a Statement
                Statement stmt = conn.createStatement ();
	  
                //First get the first order leaf tasks
                StringBuffer query = new StringBuffer("select NSN, NOMENCLATURE, MATERIEL_QTY, MATERIEL_UOM from CONSTR_TASK_MATERIEL where TASK_NAME = ");
	        query.append("'" + taskVerb + "'");

                //System.out.println("\n About to execute query: "+ query);
	        rset = stmt.executeQuery(query.toString());

                while (rset.next ()) {
                   String nsn = rset.getString(1);
                   String nomenclature = rset.getString(2);
                   double quantity = rset.getDouble(3);
                   String units = rset.getString(4);
                
                   //System.out.println("\n Got a result set for the query: " +
                   //    nsn + ", " + nomenclature + ", " + quantity + ", " + units);

                   long dayDuration = (PluginHelper.getEndTime(task) - PluginHelper.getStartTime(task))/
                                        MSEC_PER_DAY;
                   //System.out.println("task duration is " + dayDuration);

                   // just average out the total task count over the time period
                   CountRate rate = new CountRate(new Count(quantity, Count.UNITS), 
                                                  new Duration(dayDuration, Duration.DAYS));
                   //System.out.println("calculated rate of " + rate);

                   //Asset theAsset = new ClassIVConstructionMaterial();
                   ClassIVConstructionMaterial theAsset = 
                           (ClassIVConstructionMaterial)ldmFactory_.createPrototype(
                                "ClassIVConstructionMaterial", nomenclature);
                   NewSupplyClassPG pg = (NewSupplyClassPG)org.cougaar.glm.ldm.asset.PropertyGroupFactory.newSupplyClassPG();
                   pg.setSupplyClass("ClassIVConstructionMaterial");
                   pg.setSupplyType("ClassIVConstructionMaterial");
                   theAsset.setPropertyGroup(pg);
                   
                   String fullnsn = new String("NSN/");
                   fullnsn = fullnsn.concat(nsn);
                   NewTypeIdentificationPG tipg = (NewTypeIdentificationPG)org.cougaar.glm.ldm.asset.PropertyGroupFactory.newTypeIdentificationPG();
                   tipg.setTypeIdentification(fullnsn);
                   tipg.setNomenclature(nomenclature);
                   theAsset.setPropertyGroup(tipg);

                   spec.addAsset(theAsset, rate);

                }

                // close this round of queries
                stmt.close();

            }
            catch (SQLException e) {
                System.err.println("Query failed: "+e);
            } 
            finally {
                if (conn != null) conn.close();
            }
        } 
        catch (Exception e) {
            System.err.println("Caught exception while executing a query: "+e);
            e.printStackTrace();
        }
      
        //System.out.println("\n About to return: " + subtasks.size() + "Subtasks for Workflow");
        //return subtasks;

    }
      

	protected Task newDemandTask (Task parent_task, Asset consumer,
			 ConsumerSpec spec) {

		// create prepositions for the new Supply task
		Vector pp_vector = new Vector();

		pp_vector.addElement ( newPrepositionalPhrase("DemandSpec", spec) );

		pp_vector.addElement ( newPrepositionalPhrase
			(Constants.Preposition.OFTYPE, "ClassIVConstructionMaterial") );

		NewTask t =  (NewTask) buildTask (parent_task,
				Constants.Verb.GENERATEPROJECTIONS,
				//ConstructionConstants.Verb.GENERATE_CONSTRUCTION_DEMAND,
				consumer, pp_vector);

		return t;
	} // newDemandTask


	/**
	 * Create a quantity schedule for each type of consumer
	 * representing the number of that type of consumer over time.
	**/
	private Hashtable createConsumerSchedules() {
		Hashtable consumerSchedules_ = new Hashtable();
		consumerSchedules_.clear();

		ScheduledContentPG scp;
		int qty;
		Asset consumer, asset;

		asset = myOrganization;

		scp = null;
		qty = 1;
		consumer = asset.getPrototype();
		if (consumer == null) {
  			// don't expect this to happen
  			//debug("asset: "+asset+" has null prototype");
  			consumer = asset;
		} // if

		// CDW - The previous error message probably displayed.
		// So what?  I'll set it myself, thank you very much.
		consumer = myOrganization;

		Schedule role_sched = asset.getRoleSchedule().getAvailableSchedule();
		if (role_sched == null) {
			// error
			debug("createConsumerSchedule no available schedule for asset "+
				AssetUtils.assetDesc(asset));
			//continue;
		} // if

		// Start and end time of the consumers in the cluster
		long start = role_sched.getStartTime();
		long end = role_sched.getEndTime();
		if (start >= end-1) {
			// error
			debug("createConsumerSchedule  bad times "+
				TimeUtils.dateString(start)+" to "+TimeUtils.dateString(end));
			//continue;
		} // if
		Schedule sched = (Schedule) consumerSchedules_.get(consumer);
		// ScheduleUtils schedule methods have a granularity - so 1000 = 1 sec
		// so everything is scheduled on an even second
		if (sched != null) {
			sched = ScheduleUtils.adjustSchedule(sched, start, end, qty, 1000);
		} else {
			sched = ScheduleUtils.buildSimpleQuantitySchedule(qty, start, end, 1000);
		} // if

		consumerSchedules_.put (consumer,sched);

		return consumerSchedules_;
	} // createConsumerSchedules
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    private UnaryPredicate constructionDemandPred = new UnaryPredicate () {
      public boolean execute ( Object o ) {
        if ( o instanceof Task ) {
         Task t = (Task) o;
         if ( 
              (t.getVerb().equals(ConstructionConstants.Verb.Lay_Gravel)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Lay_Mesh)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Pour_Concrete)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Cut_Trees)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Remove_Stumps)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Doze_Area)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Scrape_Area)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Steam_Roll_Area)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Compact_Area)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Spray_Sealer)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Build_Frames)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Establish_Sanitation)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Establish_Power)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Build_Walls)) ||
              (t.getVerb().equals(ConstructionConstants.Verb.Construct_Roofing)) ) {

           //debug ("Found an input task to process: " + t.getVerb());
           return true;
	      }
	}
        return false;
      }
    };

	private UnaryPredicate generateConstructionProjectionPred
			= new UnaryPredicate () {
		public boolean execute ( Object o ) {
			if ( o instanceof Task) {
				Task t = (Task) o;
				if ( t.getVerb().equals ("GENERATECONSTRUCTIONDEMAND") ) {
					return true;
				} // if
			} // if
		return false;
		} // execute
	}; // generateConstructionProjectionPred
		
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	public void debug ( String msg ) {
		System.out.println ( "\n" + className + ": " + msg );
	} // debug

} // ConstructionGenerateDemandProjector
