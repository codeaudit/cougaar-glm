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

package org.cougaar.domain.mlm.plugin.generic;

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.component.ServiceRevokedEvent;
import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.plugin.util.PlugInHelper;
import org.cougaar.domain.glm.ldm.asset.ClassIVConstructionMaterial;
import org.cougaar.domain.glm.ldm.asset.NewSupplyClassPG;
import org.cougaar.domain.planning.ldm.DomainService;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.*;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.Parameters;
import java.util.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;


/** ExpanderPlugIn that takes arguments of verbs to determine what kinds
  * of tasks we would like to expand. The plugin also takes an argument that 
  * specifies whether the expansion should be flat 'flat=true' e.g. expand the parent by 
  * creating a workflow of subtasks that are leaf tasks - tasks in between the parent
  * and the leaves will not be explicitly spelled out in the expansion, although some
  * of the timing information will be accumulated. If the argument is 'flat=false',
  * a workflow/expansion will be created for each level of expansion.  The default
  * (if no 'flat=' argument is specified) is flat=true.
  * The PlugIn will access a database that contains information about 
  * how to expand the tasks it is interested in.
  * Please see glm/docs/UniversalExpanderPlugIn.html for database and argument details.
  * @author  ALPINE <alpine-software@bbn.com>
  * @version $Id: NewUniversalExpanderPlugIn.java,v 1.4 2001-10-03 21:29:48 bdepass Exp $
  **/

public class NewUniversalExpanderPlugIn extends ComponentPlugin {

  private IncrementalSubscription interestingTasks;
  private IncrementalSubscription myExpansions;
  private DomainService domainService;
  private RootFactory theFactory;
  private ArrayList expansionVerbs = new ArrayList();
  private boolean flat = true;

  public void load() {
    super.load();
    //get a domain service
    domainService = (DomainService) 
      getServiceBroker().getService(
      this, 
      DomainService.class,
      new ServiceRevokedListener() {
        public void serviceRevoked(ServiceRevokedEvent re) {
          if (DomainService.class.equals(re.getService())) {
            domainService = null;
          }
        }
      });
    // parse the parameters
    Collection params = getParameters();
    Iterator param_iter = params.iterator();
    while (param_iter.hasNext()) {
      String aParam = (String) param_iter.next();
      if (aParam.startsWith("flat=")) {
        // this assumes no spaces ... flat=true or flat=false
        if (aParam.endsWith("false")) {
          flat = false;
        } else if (aParam.endsWith("true")){
          flat = true;
        } else {
          throw new IllegalArgumentException("\n UNIVERSALEXPANDERPLUGIN received an " +
                                             "illegal plugin argument to flat='value'. " +
                                             "The value should either be 'true' or 'false'.");
        }
      } else {
        // assume its a verb
        expansionVerbs.add(aParam);
      }
    }
    //System.err.println("\n UniversalExpanderPlugIn Flat flag is: "+flat);
  }

  public void unload() {
    super.unload();
    if (domainService != null) {
      getServiceBroker().releaseService(this, DomainService.class, domainService);
    }
  }
    
  public void setupSubscriptions() {
    //System.out.println("In UniversalExpanderPlugin.setupSubscriptions");
    interestingTasks = (IncrementalSubscription)
      getBlackboardService().subscribe(
                                       VerbTasksPredicate(expansionVerbs));
    myExpansions = (IncrementalSubscription)
      getBlackboardService().subscribe(
                                       VerbExpansionsPredicate(expansionVerbs));

    theFactory = domainService.getFactory();
  }

  public void execute() {
    //System.out.println("In UniversalExpanderPlugin.execute");
    if (flat) {
      for(Enumeration e = interestingTasks.getAddedList();e.hasMoreElements();) {
        Task task = (Task)e.nextElement();
        // TODO: check for flat or tree here
        Collection subtasks = findSubTasks(task);
        NewWorkflow wf = theFactory.newWorkflow();
        wf.setParentTask(task);
        Iterator stiter = subtasks.iterator();
        while (stiter.hasNext()) {
          NewTask subtask = (NewTask) stiter.next();
          subtask.setWorkflow(wf);
          wf.addTask(subtask);
          getBlackboardService().publishAdd(subtask);
        }
        // create the Expansion ...
        Expansion newexpansion = theFactory.createExpansion(
                                                            theFactory.getRealityPlan(), 
                                                            task, wf, null);
        getBlackboardService().publishAdd(newexpansion);
      } //end of for
    } else {
      Enumeration tree_enum = interestingTasks.getAddedList();
      while(tree_enum.hasMoreElements()) {
        Task t = (Task)tree_enum.nextElement();
        treeExpand(t);
      }
    }
      
    
    for(Enumeration exp = myExpansions.getChangedList();exp.hasMoreElements();) {
      Expansion myexp = (Expansion)exp.nextElement();
      if (PlugInHelper.updatePlanElement(myexp)) {
        getBlackboardService().publishChange(myexp);
      }
    }
  }  //end of execute

  
  /** Gather expansion/subtask information from our database to use
   * in creating our subtasks and workflow.
   * @param task  The parent task to expand
   * @return Collection of subtasks
   **/
  private Collection findSubTasks(Task task)  {
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
        String exptable = "${generic.database.expander.expansion_table}";
        String dbexptable = Parameters.replaceParameters(exptable);
        
        conn = DriverManager.getConnection (dbinfo, dbuser, dbpasswd );
        String parentverb = task.getVerb().toString();
        // Create a Statement
        Statement stmt = conn.createStatement ();

        //define a base query that we can use
        StringBuffer basequery = new StringBuffer(
           "select IS_LEAF_TASK, TIME_OFFSET, SUBTASK_NAME, DURATION, " +
           "PREPOSITION, PHRASE, DO_CLASS, DO_TYPE_ID, " +
           "DO_NOMENCLATURE, QUANTITY, SHIPMENTS from "+dbexptable);
        StringBuffer firstlevelquery = new StringBuffer(basequery + 
          " where ROOT_TASK_NAME = '" + parentverb + "'");
        
        //System.out.println("\n About to execute query: "+ firstlevelquery);
        rset = stmt.executeQuery(firstlevelquery.toString());
        //check to make sure we get something back from the query - resultset
        //api show's no easy way to check this - statement api says resultset will
        //never be null.
        boolean gotresult = false;
        
        while (rset.next ()) {
          gotresult = true;
          //check to see if we have a leaf task or not and if this is
          //not a flat expansion only do the initial expansion
          if ((rset.getInt(1) == 1) || (!flat)) {
            //System.out.println("\n Got a result set for immediate Leafs!");
            ArrayList subtasksToAdd = parseRow(task, rset, rset.getInt(2));
            Iterator substoadd_iter = subtasksToAdd.iterator();
            while(substoadd_iter.hasNext()) {
              Task aNewSubTask = (Task) substoadd_iter.next();
              subtasks.add(aNewSubTask);
            }
          } else {
            //System.out.println("\n Got a result set for NON immediate leafs!");
            ArrayList outstanding = new ArrayList();
            ArrayList newoutstanding = new ArrayList();
            NonLeaf newNonLeaf = new NonLeaf(rset.getInt(2), rset.getString(3));
            Statement stmt2 = conn.createStatement();
            outstanding.add(newNonLeaf);
            while (! outstanding.isEmpty()) {
              Iterator out_iter = outstanding.iterator();
              while (out_iter.hasNext()) {
                boolean foundsub = false;
                NonLeaf check = (NonLeaf) out_iter.next();
                //create another statement for more queries so we don't
                //loose the result set from the first query and statement.
                StringBuffer anotherquery = new StringBuffer(basequery +
                    " where ROOT_TASK_NAME = '" + check.getSubTaskName() + "'");
                ResultSet rset2 = stmt2.executeQuery(anotherquery.toString());
                while (rset2.next()) {
                  foundsub = true;
                  if (rset2.getInt(1) == 1) {
                    //process this row if it is a leaf
                    //get the parent offset to add to each one.
                    int newoffset = check.getOffset() + rset2.getInt(2);
                    ArrayList moreToAdd = parseRow(task, rset2, newoffset);
                    Iterator more_iter = moreToAdd.iterator();
                    while (more_iter.hasNext()) {
                      Task anotherTask = (Task) more_iter.next();
                      subtasks.add(anotherTask);
                    }
                  } else {
                    //if we did not get a leaf again - set up for another
                    //round of queries - but don't forget to accumulate the offsets
                    int anotheroffset = check.getOffset() + rset.getInt(2);
                    NonLeaf anotherNonLeaf = new NonLeaf(anotheroffset, rset2.getString(3));
                    newoutstanding.add(anotherNonLeaf);
                  }
                }  //close the while for rset2
                if (! foundsub) {
                  System.err.println("\n UNIVERSALEXPANDERPLUGIN COULD NOT FIND A SUBTASK"+
                                     " FOR "+check.getSubTaskName()+" IN DB TABLE "+
                                     dbexptable+".  PLEASE CHECK THE DB TABLE!!!");
                  Thread.dumpStack();
                }
              } //close while outstanding has next
              //clear out outstanding list and and any new outstanding
              //entries to it to query again.
              outstanding.clear();
              if (! newoutstanding.isEmpty()) {
                outstanding.addAll(newoutstanding);
                newoutstanding.clear();
              }
            }  //close outstanding is empty
            //close the second+ round of queries 
            stmt2.close();
          } // close of else
        }  // close of rset.next
        if ((! gotresult) && (flat)) {
          //this is only an error for a flat expansion as the tree expansion
          //is not keeping track of is_leaf_task
          System.err.println("\n UNIVERSALEXPANDERPLUGIN GOT NO RESULTS FOR THE QUERY: "+
                             firstlevelquery +".  THIS MEANS THE "+parentverb+
                             " TASK CAN NOT BE EXPANDED.  PLEASE CEHCK THE DB TABLE.");
          Thread.dumpStack();
        }
        // close the first round of queries
        stmt.close();

      } catch (SQLException e) {
        System.err.println("UNIVERSAL EXPANDER PLUGIN Query failed: "+e);
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

  // parse the row - but take the time offset value as an arg instead of
  // from the resultset in case it is an accumulated offset from many levels
  private ArrayList parseRow(Task parent, ResultSet rset, int timeoffset) 
    throws SQLException {
    ArrayList results = new ArrayList();
    boolean multipleshipments = false;
    try {
      String st_name = rset.getString(3);
      int duration = rset.getInt(4);
      NewTask newsubtask = createSubTask(parent, st_name, timeoffset, duration);
      String preposition = rset.getString(5);
      if (! rset.wasNull()) {
        Enumeration newphrases = createPrepositionalPhrase(newsubtask, preposition, rset.getString(6));
        newsubtask.setPrepositionalPhrases(newphrases);
      }
      String do_class = rset.getString(7);
      if (! rset.wasNull()) {
        Asset directObject = createDirectObject(do_class, rset.getString(8), 
                                                rset.getString(9));
        newsubtask.setDirectObject(directObject);
        int qty = rset.getInt(10);
        // create quantity preference
        AspectValue qtyAV = new AspectValue(AspectType.QUANTITY, qty);
        ScoringFunction qtySF = ScoringFunction.createPreferredAtValue(qtyAV, 2);
        Preference qtyPref = theFactory.newPreference(AspectType.QUANTITY, qtySF);
        newsubtask.addPreference(qtyPref);
        int shipments = rset.getInt(11);
        if ((! rset.wasNull()) && (shipments > 1)) {
          multipleshipments = true;
          ArrayList newsubtasks = createMultipleShipments(newsubtask, shipments, duration);
          return newsubtasks;
        }
      }
      results.add(newsubtask);

    } catch (SQLException se) {
      throw new SQLException("Exception in parserow..." + se);
    }
    return results;
  }


  /** Create a simple subtask out of the database
   * @param parent - parent task
   * @param st_name - new subtask's verb from db
   * @param offset new subtask's offset time from parent's start time
   * @param duration new subtask's duration
   * @return NewTask - The new subtask
   **/
  private NewTask createSubTask(Task parent, String st_verb, int offset, int duration) {
    NewTask subtask = theFactory.newTask();
    subtask.setParentTaskUID(parent.getUID());
    subtask.setVerb(Verb.getVerb(st_verb));
    subtask.setPlan(parent.getPlan());
    subtask.setContext(parent.getContext());
    // pass along parent's prep phrases for now. A new one can be added also.
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
    //make a start and end as usual - note that for generic purposes
    //even if this is a supply task, it may end up with a start and end time
    //pref that are equal
    AspectValue startAV = new AspectValue(AspectType.START_TIME, childST.getTime());
    ScoringFunction startSF = ScoringFunction.createPreferredAtValue(startAV, 2);
    Preference startPref = theFactory.newPreference(AspectType.START_TIME, startSF);
    prefs.addElement(startPref); 
    AspectValue endAV = new AspectValue(AspectType.END_TIME, childET.getTime());
    ScoringFunction endSF = ScoringFunction.createPreferredAtValue(endAV, 2);
    Preference endPref = theFactory.newPreference(AspectType.END_TIME, endSF);
    prefs.addElement(endPref);

    subtask.setPreferences(prefs.elements());
    
    return subtask;
  }

  private Enumeration createPrepositionalPhrase(Task subtask, String preposition, String phrase) {
    // for now just make the phrase a string.
    NewPrepositionalPhrase newpp = theFactory.newPrepositionalPhrase();
    newpp.setPreposition(preposition);
    newpp.setIndirectObject(phrase);
    Vector phrases = new Vector();
    phrases.add(newpp);
    //keep the phrases from the parent task
    Enumeration oldpp = subtask.getPrepositionalPhrases();
    while (oldpp.hasMoreElements()) {
      PrepositionalPhrase app = (PrepositionalPhrase) oldpp.nextElement();
      phrases.add(app);
    }
    return phrases.elements();
  }

  private Asset createDirectObject(String assetclass, String typeid, String nomenclature) {
    Asset theAsset = null;
//     try {
//       theAsset = 
//         (Asset)theFactory.createPrototype(Class.forName(assetclass), typeid, nomenclature);
      theAsset = 
        (Asset)theFactory.createPrototype(assetclass, typeid, nomenclature);
//     } catch (ClassNotFoundException cnfe) {
//       System.err.println("\n UniversalExpanderPlugin Caught exception while trying to create an asset: "+cnfe);
//       cnfe.printStackTrace();
//     }
    //need this for supply tasks...
    NewSupplyClassPG pg = 
      (NewSupplyClassPG)org.cougaar.domain.glm.ldm.asset.PropertyGroupFactory.newSupplyClassPG();
    pg.setSupplyClass(assetclass);
    pg.setSupplyType(assetclass);
    theAsset.setPropertyGroup(pg);
    
    return theAsset;
  }

  private ArrayList createMultipleShipments(Task subtask, int shipments, int duration) {
    //get some starting preference info
    int shipmentoffset = duration / shipments;
    Date startofshipments = null;
    double qtypershipment = 0;
    Enumeration taskprefs = subtask.getPreferences();
    while (taskprefs.hasMoreElements()) {
      Preference asubtaskpref = (Preference) taskprefs.nextElement();
      if (asubtaskpref.getAspectType() == AspectType.START_TIME) {
        startofshipments = new Date((long)asubtaskpref.getScoringFunction().getBest().getValue());
      } else if (asubtaskpref.getAspectType() == AspectType.QUANTITY) {
        double totalqty = asubtaskpref.getScoringFunction().getBest().getValue();
        qtypershipment = totalqty / shipments;
      }
    }
    Calendar shipmentcal = Calendar.getInstance();
    // set initial time
    shipmentcal.setTime(startofshipments);
    
    //begin creating tasks
    ArrayList shipmenttasks = new ArrayList();
    for (int i = 0; i < shipments; i++) {
      NewTask mstask = theFactory.newTask();
      mstask.setParentTaskUID(subtask.getParentTaskUID());
      mstask.setVerb(subtask.getVerb());
      mstask.setDirectObject(subtask.getDirectObject());
      mstask.setPrepositionalPhrases(subtask.getPrepositionalPhrases());
      mstask.setPlan(subtask.getPlan());
      mstask.setContext(subtask.getContext());

      //make the preferences for the multiple shipments
      //note that multiple shipments are assumed to be Supply or similar
      //tasks for which start time is meaningless.  End time should be used
      //to define when the customer expects the shipment to arrive
      Date shipmentET = null;
      //only add in the shipmentoffset if this is not the first shipment
      if (i != 0) {
        // for each shipment after the first keep adding the offset
        // for a cumulative offset affect.
        shipmentcal.add(Calendar.DATE, shipmentoffset);
      }
      shipmentET = shipmentcal.getTime();
      Vector prefs = new Vector();
      AspectValue shipmentAV = new AspectValue(AspectType.END_TIME, shipmentET.getTime());
      ScoringFunction shipmentSF = ScoringFunction.createPreferredAtValue(shipmentAV, 2);
      Preference shipmentPref = theFactory.newPreference(AspectType.END_TIME, shipmentSF);
      prefs.addElement(shipmentPref); 
      AspectValue qtyAV = new AspectValue(AspectType.QUANTITY, qtypershipment);
      ScoringFunction qtySF = ScoringFunction.createPreferredAtValue(qtyAV, 2);
      Preference qtyPref = theFactory.newPreference(AspectType.QUANTITY, qtySF);
      prefs.addElement(qtyPref);

      mstask.setPreferences(prefs.elements());
      shipmenttasks.add(mstask);
    }
    return shipmenttasks;
  }
     
  //expand this as a multi-level expansion instead of a flat(single expansion)
  private void treeExpand(Task t) {
    // list to expand
    ArrayList toexpand = new ArrayList();
    // add the top level parent
    toexpand.add(t);
    //temporary holding list to expand while we recurse
    ArrayList nexttoexpand = new ArrayList();
    while (!toexpand.isEmpty()) {
      Iterator te_iter = toexpand.iterator();
      while (te_iter.hasNext()) {
        Task aTask = (Task) te_iter.next();
        //since the tree expand does not pay attention to is_leaf_task 
        //check to see if this returns an empty collection meaning we are
        //at the end of the road
        Collection subs = findSubTasks(aTask);
        if (!subs.isEmpty()) {
          NewWorkflow wf = theFactory.newWorkflow();
          wf.setParentTask(aTask);
          Iterator siter = subs.iterator();
          while (siter.hasNext()) {
            NewTask subtask = (NewTask) siter.next();
            subtask.setWorkflow(wf);
            wf.addTask(subtask);
            //keep this subtask to expand next
            nexttoexpand.add(subtask);
            getBlackboardService().publishAdd(subtask);
          }
          // create the Expansion ...
          Expansion newexpansion = theFactory.createExpansion(
                                                              theFactory.getRealityPlan(), 
                                                              aTask, wf, null);
          getBlackboardService().publishAdd(newexpansion);
        }
      }
      //clear out toexpand since we've been through the list
      toexpand.clear();
      if (!nexttoexpand.isEmpty()) {
        //move all of the next to expands into to expand
        toexpand.addAll(nexttoexpand);
        //reset next to expand
        nexttoexpand.clear();
      }
    }

  }
    
  //
  // Predicates
  //
  protected static UnaryPredicate VerbTasksPredicate(final Collection verbs) {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Task) {
          Task task = (Task)o;
          String taskVerb = task.getVerb().toString();
          Iterator verbs_iter = verbs.iterator();
          while (verbs_iter.hasNext()) {
            String interestingVerb = (String)verbs_iter.next();
            if (interestingVerb.equals(taskVerb)) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }  
  
  protected static UnaryPredicate VerbExpansionsPredicate(final Collection verbs) {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Expansion) {
          Task task = ((Expansion)o).getTask();
          String taskVerb = task.getVerb().toString();
          Iterator verbs_iter = verbs.iterator();
          while (verbs_iter.hasNext()) {
            String interestingVerb = (String)verbs_iter.next();
            if (interestingVerb.equals(taskVerb)) {
              return true;
            }
          }
        }
        return false;
      }
    };
  } 

  //
  //inner classes
  //

  // inner class to hold some outstanding non leaf row info
  public class NonLeaf {
    private int theOffset;
    private String theSubTaskName;
    public NonLeaf(int offset, String subtask) {
      this.theOffset = offset;
      this.theSubTaskName = subtask;
    }
    public int getOffset() {
      return theOffset;
    }
    public String getSubTaskName() {
      return theSubTaskName;
    }
  }
  
}










