/*
 * <copyright>
 *  Copyright 1997-2001 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.generic;

import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.NewWorkflow;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.util.PlugInHelper;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.Parameters;
import java.util.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;

/** ExpanderPlugIn that takes arguments of verbs to determine what kinds of tasks we would like to expand.
  * PlugIn will look up in a database information about how to expand the incoming tasks to create a workflow.
  * @author  ALPINE <alpine-software@bbn.com>
  * @version $Id: UniversalExpanderPlugIn.java,v 1.2 2001-04-10 17:39:10 bdepass Exp $
  **/

public class UniversalExpanderPlugIn extends SimplePlugIn {

    private IncrementalSubscription interestingTasks;
    private IncrementalSubscription myExpansions;
    
   protected static UnaryPredicate VerbTasksPredicate(final Vector verbs) {
     return new UnaryPredicate() {
	     public boolean execute(Object o) {
         if (o instanceof Task) {
           Task task = (Task)o;
           String taskVerb = task.getVerb().toString();
           Enumeration verbsenum = verbs.elements();
           while (verbsenum.hasMoreElements()) {
             String interestingVerb = (String)verbsenum.nextElement();
             if (interestingVerb.equals(taskVerb)) {
               return true;
	           }
           }
         }
         return false;
       }
     };
   }	

    protected static UnaryPredicate VerbExpansionsPredicate(final Vector verbs) {
      return new UnaryPredicate() {
	      public boolean execute(Object o) {
          if (o instanceof Expansion) {
            Task task = ((Expansion)o).getTask();
            String taskVerb = task.getVerb().toString();
            Enumeration verbsenum = verbs.elements();
            while (verbsenum.hasMoreElements()) {
              String interestingVerb = (String)verbsenum.nextElement();
              if (interestingVerb.equals(taskVerb)) {
                return true;
	            }
            }
          }
          return false;
        }
      };
    }	

    public void setupSubscriptions() {
      //System.out.println("In UniversalExpanderPlugin.setupSubscriptions");
      interestingTasks = (IncrementalSubscription)subscribe(VerbTasksPredicate(getParameters()));
      myExpansions = (IncrementalSubscription)subscribe(VerbExpansionsPredicate(getParameters()));
      
    }

   
    public void execute() {
      //System.out.println("In UniversalExpanderPlugin.execute");

	    for(Enumeration e = interestingTasks.getAddedList();e.hasMoreElements();) {
		    Task task = (Task)e.nextElement();
		    Collection subtasks = createSubTasks(task);
        NewWorkflow wf = theLDMF.newWorkflow();
		    wf.setParentTask(task);
		    Iterator stiter = subtasks.iterator();
		    while (stiter.hasNext()) {
		      NewTask subtask = (NewTask) stiter.next();
		      subtask.setWorkflow(wf);
		      wf.addTask(subtask);
		      publishAdd(subtask);
		    }
		    // create the Expansion ...??? Do we want to provide an EstimatedAllocationResult ???
		    Expansion newexpansion = theLDMF.createExpansion(theLDMF.getRealityPlan(), task, wf, null);
		    publishAdd(newexpansion);
	    } //end of for
        
      for(Enumeration exp = myExpansions.getChangedList();exp.hasMoreElements();) {
        Expansion myexp = (Expansion)exp.nextElement();
        if (PlugInHelper.updatePlanElement(myexp)) {
		      publishChange(myexp);
        }
      }
    }  //end of execute

  

    private Collection createSubTasks(Task task)  {
      // initialize this with root to kick off the first pass
      //Collection subroots = new ArrayList();
      //subroots.add("root");
      Collection subtasks = new ArrayList();
      
      try {
        // Load the Oracle JDBC driver
        Class.forName ("oracle.jdbc.driver.OracleDriver");
        Connection conn = null;
        ResultSet rset = null;

        try {
    	    // Connect to the database
    	    // You must put a database name after the @ sign in the connection URL.
    	    // You can use either the fully specified SQL*net syntax or a short cut
    	    // syntax as <host>:<port>:<sid>.  The example uses the short cut syntax.
         // get the db info out of the cougaarrc file
         String rawdb = "${generic.database.expander.database}";
         String dbinfo = Parameters.replaceParameters(rawdb);
         String rawuser = "${generic.database.expander.user}";
         String dbuser = Parameters.replaceParameters(rawuser);
         String rawpasswd = "${generic.database.expander.password}";
         String dbpasswd = Parameters.replaceParameters(rawpasswd);

         conn = DriverManager.getConnection ("jdbc:oracle:thin:@" + dbinfo, dbuser, dbpasswd );
         String parentverb = task.getVerb().toString();
	       // Create a Statement
    	   Statement stmt = conn.createStatement ();
	  
	       //First get the first order leaf tasks
         StringBuffer immediateleafquery = new StringBuffer("select SUBTASK_NAME, OFFSET, DURATION from CONSTR_TASK_EXP where ROOT_TASK_NAME = ");
	       immediateleafquery.append("'" + parentverb + "' and IS_LEAF_TASK = '1'");

	       //System.out.println("\n About to execute query: "+ immediateleafquery);
	       rset = stmt.executeQuery(immediateleafquery.toString());

    	    while (rset.next ()) {
           //System.out.println("\n Got a result set for immediate Leafs!");
      	    String st_name = rset.getString(1);
           int offset = rset.getInt(2);
	         int duration = rset.getInt(3);
	         Task newsubtask = createSubTask(task, st_name, offset, duration);
	         subtasks.add(newsubtask);
	       }

	       // close this round of queries
	       stmt.close();
	       stmt = conn.createStatement();

         StringBuffer secondaryleafquery = new StringBuffer("select cte.subtask_name, t1.offset + cte.offset, cte.duration from constr_task_exp cte, (select cte1.subtask_name, cte1.is_leaf_task, cte1.offset, cte1.duration from constr_task_exp cte1 where is_leaf_task = 0 and root_task_name = '");
	       secondaryleafquery.append( parentverb + "') t1 where cte.root_task_name = t1.subtask_name order by t1.offset, t1.subtask_name" );
         //System.out.println("\n About to execute query: "+ secondaryleafquery);
	       rset = stmt.executeQuery(secondaryleafquery.toString());

    	   while (rset.next ()) {
          //System.out.println("\n Got a result set for secondary Leafs!");
      	   String st_name = rset.getString(1);
          int offset = rset.getInt(2);
	        int duration = rset.getInt(3);
	        Task newsubtask = createSubTask(task, st_name, offset, duration);
	        subtasks.add(newsubtask);
	      }

	      // close this round of queries
	      stmt.close();
      } catch (SQLException e) {
	      System.err.println("Query failed: "+e);
      } finally {
	      if (conn != null)
          conn.close();
        }
    } catch (Exception e) {
      System.err.println("Caught exception while executing a query: "+e);
      e.printStackTrace();
    }
    //System.out.println("\n UniversalExpanderPlugIn About to return: " + subtasks.size() + "Subtasks for Workflow");
    return subtasks;
  }


  /** Create a simple subtask out of the database
    * @param parent - parent task
    * @param st_name - new subtask's verb from db
    * @param offset new subtask's offset time from parent's start time
    * @param duration new subtask's duration
    * @return Task - The new subtask
    **/
  private Task createSubTask(Task parent, String st_verb, int offset, int duration) {
    NewTask subtask = theLDMF.newTask();
    subtask.setParentTask(parent);
    subtask.setVerb(Verb.getVerb(st_verb));
    subtask.setPlan(parent.getPlan());
    // use parent's prep phrases for now.
    subtask.setPrepositionalPhrases(parent.getPrepositionalPhrases());
    //set up preferences - first find the start time of the parent task
    Date childST = null;
    Date childET = null;
    Enumeration parentprefs = parent.getPreferences();
    while (parentprefs.hasMoreElements()) {
      Preference apref = (Preference)parentprefs.nextElement();
      if (apref.getAspectType() == AspectType.START_TIME) {
	      Date parentstarttime = new Date( (long)apref.getScoringFunction().getBest().getValue());
	      Calendar cal = Calendar.getInstance();
	      cal.setTime(parentstarttime);
	      cal.add(Calendar.DATE, offset);
        childST = cal.getTime();
        cal.add(Calendar.DATE, duration);
        childET = cal.getTime();
      }
      break;
    }
	
    Vector prefs = new Vector();
    AspectValue startAV = new AspectValue(AspectType.START_TIME, childST.getTime());
    ScoringFunction startSF = ScoringFunction.createPreferredAtValue(startAV, 2);
    Preference startPref = theLDMF.newPreference(AspectType.START_TIME, startSF);
    prefs.addElement(startPref); 
    AspectValue endAV = new AspectValue(AspectType.END_TIME, childET.getTime());
    ScoringFunction endSF = ScoringFunction.createPreferredAtValue(endAV, 2);
    Preference endPref = theLDMF.newPreference(AspectType.END_TIME, endSF);
    prefs.addElement(endPref);

    subtask.setPreferences(prefs.elements());

    return subtask;
  }

}