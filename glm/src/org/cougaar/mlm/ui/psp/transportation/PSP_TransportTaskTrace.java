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
 
package org.cougaar.mlm.ui.psp.transportation;

import java.io.*;
import java.net.*;
import java.util.*;

import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.core.util.UID;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.mlm.ui.psp.transportation.data.*;

/**
  *  This PSP Answers the following questions:
  *
  *    -) <no URL parameters>
  *         Starts with all Transport/TransportationMission Tasks found
  *         At this cluster, returns references to remote allocations
  *         which can then be "spidered" (driven by Client) to obtain
  *         all Allocations to Physical Assets made against TASKS whose
  *         VERBs = Transport/TransportationMission
  *
  *    -) ?TASK=UID
  *
  *    -) ?ASSET=UID
  *
  *    -) ?ORGANIZATION=UID
  *
  *    -) ?TX_TASK_UIDS_ONLY
  *         Return Transport/TransportationMission TASK UIDS from this Cluster.
  *         Returns vector of UID Strings.
  *
  *    -) ?MIL_ORG_LOC_ONLY
  *         Return the GeolocLocation of this Cluster.  Returns UIUnit Type
  *
  **/


/**
  ################################################################################################
  ################################################################################################
  ################################################################################################
 **/

/**
  * Structure for representing references to LogPlan objects, captured as data
  **/
class DataRef {
   public boolean isData(){ if (data == null) return false; return true; }
   public Object getData(){ return data; }
   public void setData( Object obj) { data = obj; }

   public Object data = null;
}

/**
  * A structure to represent parse trace of Task from input Task to an
  * Allocation within a Cluster.  The perspective of the data contained is
  * w/regards to an ALLOCATION.
  * Some allocations are to local assets, some to remote organizations.
  **/
class TraceRef  extends DataRef
{
   public final String STRING_NULL = "(no value)";


   public TraceRef() {
       RemoteClusterID = STRING_NULL;
       AllocTaskUID    = STRING_NULL;
       AllocAssetUID   = STRING_NULL;
       UID = STRING_NULL;
       InputTaskUID    = STRING_NULL;
       annotation      = new String();
   }

   String RemoteClusterID;
   /**
     *  UID of ALLOCATION TASK (Tsk->PE->Allocation->GetAllocTask()
     *  Only applicable when Task has been allocated to REMOTE org.
     **/
   String AllocTaskUID;
   /**
     *  UID of Local Asset to which this task has been allocated
     *  Only applicable to local asset allocations
     **/
   String AllocAssetUID;
   String UID;   // UID of the TASk which this Allocation speaks to 
   String InputTaskUID; // Input task into this cluster
   String annotation=new String();
   String answer = null;  // if null, can be from error or exception, send back annotation of parse history
}

/**
  * Structure to represent "terminal" results:   local asset identified, or error
**/
class TerminalRef   extends DataRef
{
   ///////String answer=new String();
}

/**
  ################################################################################################
  ################################################################################################
  ################################################################################################
 **/

public class PSP_TransportTaskTrace extends PSP_BaseAdapter implements UseDirectSocketOutputStream,
                                             PlanServiceProvider, UISubscriber
{
    /** A zero-argument constructor is required for dynamically loaded PSPs,
        required by Class.newInstance()
        **/
    public PSP_TransportTaskTrace()
    {
       super();
    }

    public PSP_TransportTaskTrace( String pkg, String id ) throws RuntimePSPException
    {
        setResourceLocation(pkg, id);
    }


    public boolean test(HttpInput query_parameters, PlanServiceContext sc)
    {
        super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
        return false;  // This PSP is only accessed by direct reference.
    }


    private static UnaryPredicate getTaskPred(){
        return new UnaryPredicate() {
              public boolean execute(Object o) {
                 if (o instanceof Task) {
              //System.out.println("PREDICATE CALLED true!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                          return true; }
                 return false;
          }
      };
    }
    private static UnaryPredicate getAssetPred(){
        return new UnaryPredicate() {
              public boolean execute(Object o) {
                 if (o instanceof Asset) {
              //System.out.println("PREDICATE CALLED true!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                          return true; }
                 return false;
          }
      };
    }
    private static UnaryPredicate getOrgPred(){
        return new UnaryPredicate() {
              public boolean execute(Object o) {
                 if (o instanceof Organization) {
              //System.out.println("PREDICATE CALLED true!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                          return true; }
                 return false;
          }
      };
    }

    private static UnaryPredicate getAllocPred(){
        return new UnaryPredicate() {
              public boolean execute(Object o) {
                 if (o instanceof Allocation) {
              //System.out.println("PREDICATE CALLED true!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                          return true; }
                 return false;
          }
      };
    }

    private Subscription OrgSubscription;
    private Subscription AssetSubscription;
    private Subscription TaskSubscription;
    private Subscription AllocationSubscription;
    private void initSubscriptions(PlanServiceContext psc)
    {
          OrgSubscription = psc.getServerPluginSupport().subscribe(this, getOrgPred());
          TaskSubscription = psc.getServerPluginSupport().subscribe(this, getTaskPred());
          AssetSubscription = psc.getServerPluginSupport().subscribe(this, getAssetPred());
          AllocationSubscription = psc.getServerPluginSupport().subscribe(this, getAllocPred());
    }


  protected AbstractPrinter getAbstractPrinter(
      PrintStream out,
      HttpInput query_parameters,
      String defaultFormat) throws Exception {
    String format = defaultFormat;
    Enumeration params = query_parameters.getURLParameters().elements();
    while (params.hasMoreElements()) {
      String p = ((String)params.nextElement()).toLowerCase();
      if (p.startsWith("format=")) {
        format = p.substring("format=".length()).trim();
        if ((format.length() <= 0) ||
            !AbstractPrinter.isValidFormat(format)) {
          throw new RuntimePSPException("Invalid format!: "+format);
        }
      }
    }
    return AbstractPrinter.createPrinter(format, out);
  }


    public void execute(PrintStream out,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception {
        execute(getAbstractPrinter(out, query_parameters, "html"),
                query_parameters, psc, psu);
    }

    private boolean verbose = false;
    private String thisClusterID = null;
    public void execute( AbstractPrinter pr, //PrintStream out,
                          HttpInput query_parameters,
                          PlanServiceContext psc,
                          PlanServiceUtilities psu ) throws Exception
    {
      Object retObj;
      try{
        retObj = execute1(query_parameters, psc, psu);
      } catch (Exception e) {
        retObj = new UITTException("TOP LEVEL EXCEPTION CAUGHT:"+
                                   getExceptionMessage(e));
      }
      pr.printObject(retObj);
    }

    public Object execute1(HttpInput query_parameters,
                          PlanServiceContext psc,
                          PlanServiceUtilities psu ) throws Exception
    {
      Object retObj;

      String batchQueries =  null;
      if (query_parameters.existsParameter("VERBOSE")) {
        verbose=true;
      }
      if( query_parameters.existsParameter("BATCH") ) {
        batchQueries = query_parameters.getBodyAsString();
      }
      //
      // As we mostly use this PSP with BATCH QUERY MODE
      // there is a win only subscribing to everything once
      //
      initSubscriptions(psc);

      String verbFilter = "NOT USED";
      String taskUID = null;
      String assetUID = null;
      String orgUID = null;
      boolean TxTasksUIDsOnly = false;
      boolean MilOrgInfoOnly = false;

      if( batchQueries == null) {
        //verbFilter =  (String)query_parameters.getFirstParameterToken("VERB", '=');
        taskUID =  (String)query_parameters.getFirstParameterToken("TASK", '=');
        //
        // ASSET=, ORGANIZATION=   generated by Terminal parse on previous iteration.
        //
        assetUID =  (String)query_parameters.getFirstParameterToken("ASSET", '=');
        orgUID =  (String)query_parameters.getFirstParameterToken("ORGANIZATION", '=');
        TxTasksUIDsOnly = query_parameters.existsParameter("TX_TASK_UIDS_ONLY");
        MilOrgInfoOnly = query_parameters.existsParameter("MIL_ORG_ONLY");
        if( TxTasksUIDsOnly ) {
          /** Special Case Query -- obtain all Task UIDs **/
          retObj = getTasksUIDs();
        }
        else if ( MilOrgInfoOnly ) {
          /** Special Case Query -- obtain Military Organization Location of this Cluster **/
          retObj = getMilOrgInfo();
        }
        else {
          /** Recursive Queries handled here **/
          retObj = execute2(query_parameters, psc,psu,verbFilter,
                            taskUID,assetUID,orgUID);
        }
      }
      else {
        /** Unpack and process "BATCH" QUERIES here **/
        System.out.println("-----------------------BATCH QUERY RECEIVED= " + batchQueries);
        int count0=0;
        int count1=batchQueries.indexOf("^");
        Vector retVector = new Vector();
        while (count1 > -1) {
          String query = batchQueries.substring(count0,count1);
          count0=count1+1;
          count1 = batchQueries.indexOf("^",count1+1);

          /**
                    int ivb = query.indexOf("?VERB=");
                    if( ivb > -1 ) {
                       int nxt = query.indexOf("?",ivb+6);
                       if( nxt == -1) nxt = query.length();
                       verbFilter = query.substring(ivb+6,nxt);
                    }
           **/
          int itsk = query.indexOf("?TASK=");
          if( itsk > -1 ) {
            int nxt = query.indexOf("?",itsk+6);
            if( nxt == -1) nxt = query.length();
            taskUID = query.substring(itsk+6,nxt);
          }
          int iasst = query.indexOf("?ASSET=");
          if( iasst > -1 ) {
            int nxt = query.indexOf("?",iasst+7);
            if( nxt == -1) nxt = query.length();
            assetUID = query.substring(iasst+7,nxt);
          }
          int iorg = query.indexOf("?ORGANIZATION=");
          if( iorg > -1 ) {
            int nxt = query.indexOf("?",iorg+14);
            if( nxt == -1) nxt = query.length();
            orgUID = query.substring(iorg+14,nxt);
          }

          thisClusterID = psc.getServerPluginSupport().getClusterIDAsString();
          retVector.addElement(
             execute2(query_parameters,psc,psu,verbFilter,
                      taskUID,assetUID,orgUID));

        } // end while
        retObj = retVector;
      }
      return retObj;
    }


    /** Special Case Query handler **/
    private Object getMilOrgInfo()
    {
        Vector retVector = new Vector();
        try{
            String annotation = new String();
            UIUnit unit  = null;
            Location loc = null;
            Collection container = ((CollectionSubscription)AssetSubscription).getCollection();
            Enumeration en = new Enumerator(container);
            while (en.hasMoreElements() )
            {
                Asset ast = (Asset)en.nextElement();
                if( ast instanceof Organization )
                {
                   annotation += "(found asset instanceof organization)";
                   System.out.println(   "(found asset instanceof organization)" );
                   Organization org = (Organization)ast;
                   
                   if (org.isSelf()) 
                   {
                        annotation += 
                          "(found [relation==" + Constants.Role.SELF.getName() + ")" ;

                        MilitaryOrgPG mopg = org.getMilitaryOrgPG();
                        if( mopg == null) annotation += "(getMilitaryOrgPG()==null)";
                        loc = mopg.getHomeLocation();
                        if( loc == null) {
                            annotation += "(loc == null)";
                        }

                        System.out.println(annotation);

                        String symbol = mopg.getHierarchy2525();
                        unit = new UIUnit();
                        unit.setSelfAssetUID(defaultUIDString(org.getUID()));
                        unit.setLocation((GeolocLocation)loc);
                        unit.setMs2525IconName(symbol);
                   }
                }
                if( unit != null ) {
                    retVector.addElement(unit);
                    break;  // quit iterating
                }
            } // end while
            if( unit == null ) {
              retVector.addElement(
                 new UITTException("[MilOrgInfoOnly] Failed! Annotation:"+
                                   annotation));
            }
        } catch (Exception ex) {
             ex.printStackTrace();
        }
        return retVector;
    }

    /** Special Case Query handler **/
    private Object getTasksUIDs()
    {
        Vector results = new Vector();
        try{
            Enumeration en = ((CollectionSubscription)TaskSubscription).elements();
            while (en.hasMoreElements() )
            {
                Task tsk = (Task)en.nextElement();
                String uistr = new String(defaultUIDString(tsk.getUID()));
                results.addElement(uistr);
            }
        } catch (Exception ex) {
           ex.printStackTrace();
        }
        return results;
    }

    /**
      * A return record can be prefixed with
      *   RESULT=
      *   ERROR=        //  RESULT which is in ERROR
      *   EXCEPTION=    //  PSP algorithm unable to handle condition, no RESULT generated
      **/
    private Object execute2(HttpInput query_parameters,
                            PlanServiceContext psc,
                            PlanServiceUtilities psu,
                            String verbFilter,
                            String taskUID,
                            String assetUID,
                            String orgUID
                            ) throws Exception
    {
            Vector retVector = new Vector();

            //System.out.println("_execute(): verbFilter=" + verbFilter
            //    + ", taskUID=" + taskUID + ", assetUID=" + assetUID
            //    + ", orgUID=" + orgUID );
            Vector collection;

            if( orgUID != null ){
                // since Org = subclass of asset, need to go first.
                collection = searchForOrg(psc, orgUID);
            }
            else if( assetUID != null ){
                collection = searchForAsset(psc, assetUID);
            }
            else if( verbFilter != null) {
                collection = getAllTasks(psc, verbFilter, taskUID);
            }
            else {
               throw new RuntimePSPException("Unknown Parameter Provided");
            }

            int countResults=0;
            Enumeration en = collection.elements();
            while(en.hasMoreElements() )
            {
                Object obj = en.nextElement();
                countResults++;

                if( ((DataRef)obj).isData() ) {
                   retVector.addElement(((DataRef)obj).getData());
                }
                else if( obj instanceof TraceRef )
                {
                   TraceRef er = (TraceRef)obj;

                   Object returnObj = null;

                   if( er.answer != null ) {
                      //
                      // URL reference to be followed up for next "traversal"
                      // of log plan.
                      //
                      returnObj = new UITTReference( er.answer );
                   }
                   else {
                      returnObj = 
                        new UITTException( "PARSE_FAILURE=[" + er.UID + "]."+ 
                            er.annotation, UITTException.DATA_EXCEPTION);
                   }
                   retVector.addElement(returnObj);
                }
                //
                // TerminalRef : Answer to an ASSET=, ORGANIZATION=  qery
                // or if an ERROR encountered
                //
                else if( obj instanceof TerminalRef )
                {
                   TerminalRef tr = (TerminalRef)obj;

                   /**  Returns URL refernece to terminal Asset **/
                   obj = tr.getData();

                   retVector.addElement(obj);
                }
            } // end has more elements

            return retVector;
     }

    private Vector searchForOrg(PlanServiceContext psc, String orgUID)
    {
            Vector v = new Vector();
            Enumeration en;

            en = ((CollectionSubscription)OrgSubscription).elements();

            while( en.hasMoreElements() )
            {
                Organization org = (Organization)en.nextElement();
                if(defaultUIDString(org.getUID()).startsWith(orgUID) == true )
                {
                    TerminalRef tr = new TerminalRef();
                    tr.setData(new UITTAnswer("OrganizationUID." + orgUID));
                    v.addElement(tr);
                }
            }
            return v;
    }

    private boolean assetEqual(Asset ast, String assetUID) {
            /** $$$$$$$$$$$$$ HACK FOLLOWS!!!!!!!!!!!
              *                SUBSTITUTE HASHCODE FOR UID....!
              **/
            if( assetUID.startsWith("HASHCODE_ID") == true ) {
               String hashstring = assetUID.substring(11);
               int h = Integer.parseInt(hashstring);
               if( ast.hashCode() == h ) return true;
            }
            else if(defaultUIDString(ast.getUID()).startsWith(assetUID) == true ) {
               return true;
            }
            return false;
    }

    private Vector searchForAsset(PlanServiceContext psc, String assetUID)
    {
            Vector v = new Vector();
            Enumeration en;

            en = ((CollectionSubscription)AssetSubscription).elements();

            while( en.hasMoreElements() )
            {
                Asset ast = (Asset)en.nextElement();
                if( ast.getUID() != null) {
                     if( assetEqual(ast, assetUID) == true ) // ast.getUID().getUID().startsWith(assetUID) == true )
                     {
                         TerminalRef tr = new TerminalRef();
                         tr.setData( new UITTAnswer( getAssetResults(ast)) );
                         //tr.answer = new String("RESULT= TopLevelAssetUID." + assetUID);
                         v.addElement(tr);
                     }
                }
            }
            if( v.size() == 0 ){
                //
                // DIDN'T FIND ASSET AS TOP LEVEL ASSET... LOOK FOR IT UNDER
                // LOCAL ALLOCATIONS...
                //
                en = ((CollectionSubscription)AllocationSubscription).elements();
                while( en.hasMoreElements() )
                {
                    Allocation alloc = (Allocation)en.nextElement();
                    Asset ast = alloc.getAsset();
                    if( ast.getUID() != null) {
                        if(  assetEqual(ast, assetUID) == true) // ast.getUID().getUID().startsWith(assetUID) == true )
                        {
                            TerminalRef tr = new TerminalRef();
                            tr.setData( new UITTAnswer(getAssetResults(ast)));
                            //tr.answer = new String("RESULT= AllocationAssetUID." + assetUID);
                            v.addElement(tr);
                        }
                    }
                }
            }

            return v;
    }

    /** taskUIDFilter : optional **/
    private Vector getAllTasks(PlanServiceContext psc, String verbFilter,String taskUIDFilter)
    {
            Enumeration en;
            Enumeration en2;

            en = ((CollectionSubscription)TaskSubscription).elements();

            Vector collection  = new Vector();
 
            while( en.hasMoreElements() )
            {
                Task tsk = (Task)en.nextElement();
                Verb vrb = tsk.getVerb();

                if( true == (
                          vrb.equals(Constants.Verb.Transport) ||
                          vrb.equals(Constants.Verb.TransportationMission)  ))
                {
                    /**
                      * Continue unless taskUIDFIlter is given AND IT DOES NOT MATCH
                      **/
                    boolean uidMatch = true; // default: assume matches unless UID is given and violated
                    if( taskUIDFilter != null ) {
                       if( false == defaultUIDString(tsk.getUID()).startsWith(taskUIDFilter) ){
                           uidMatch = false;
                       }
                    }
                    //
                    // NO VERB FILTER TESTS!!!!!!!!!!!!!!!!!!!!!
                    //
                    if( (uidMatch == true))
                    {
                       //annote +=  "{[getAllTasks] MATCHED task " + tsk.getUID().getUID()
                       //            + " of verb " + vrb.toString()
                       //            + ", looking for " + taskUIDFilter
                       //            + " verb filter = " + verbFilter
                       //            + " @ cluster " + psc.getServerPluginSupport().getClusterIDAsString()
                       //            + "}" ;

                       /**
                          * For each Task which passes filter and id test... "recurse"
                          * on it.
                          **/
                        Vector v = getChildren(tsk, verbFilter,
                           "Task(" + tsk.getVerb() +
                           ", " + tsk.getUID()
                            + "; inputTask=" + getUIDOfInputTask(tsk)
                            + ")",
                            psc
                        );
                        en2 = v.elements();
                        while( en2.hasMoreElements() ) {
                            collection.addElement(en2.nextElement());
                        }

                        break;
                   }
                } // end -- if verb test fits
            }
            if( collection.size() == 0) {

                String msg = "(No Tasks found)(Looking for TransportationMission, Transport tasks)"
                             + ", UID=" + taskUIDFilter + " @"
                             + psc.getServerPluginSupport().getClusterIDAsString()
                             + ")";
                UITTException uiex = new UITTException( msg);

                collection = returnError( collection, uiex);
            }
            return collection;
    }


     /** Returns Vector of TraceRef instances **/
    private Vector getChildren(Task tsk,  String verb, String annotation, PlanServiceContext psc)
    {
         PlanElement pe = tsk.getPlanElement();
         return getChildren(tsk, pe, verb, annotation, psc);
    }

    /** verb = verbFilter **/
    private Vector getChildren(Task tsk, PlanElement pe, String verb,
                               String annotation, PlanServiceContext psc)
    {
         String annote = new String();
         Vector returnVec = new Vector();
         Enumeration en;
         Enumeration en2;

         try{
             if( pe != null ){

                //####################################################################################
                // CASE, PLANELEMENT = EXPANSION

                if( pe instanceof Expansion ) {
                    annote += "->PE(exp)";
                    Expansion ex = (Expansion)pe;
                    Workflow work = ex.getWorkflow();

                    if( work != null) annote += "->Workflow";
                    else annote += "->(Workflow==Null)";

                    en = ex.getWorkflow().getTasks();

                    if( en != null) annote += ".getTasks()";
                    else annote += ".getTasks()==Null";

                    while( en.hasMoreElements() ) {
                        Task tsk2 = (Task)en.nextElement();
                        annote += "->Task(" + defaultUIDString(tsk2.getUID()) + ")";

                        PlanElement pe2 = tsk2.getPlanElement();
                        if( pe2 != null ) {
                            Vector v = getChildren(tsk2, pe2, verb, annotation + annote, psc);
                            en2 = v.elements();
                            while( en2.hasMoreElements() ) {
                                returnVec.addElement(en2.nextElement());
                            }
                        } else {
                           annote += ".getPlanElement()==Null";
                        }
                    }
                }

                //####################################################################################
                // CASE, PLANELEMENT = ALLOCATION

                else if( pe instanceof Allocation ) {
                    //
                    // passing in returnVec, populated below
                    returnVec = parseAllocation(tsk, returnVec, (Allocation)pe,  verb, annotation, psc);  // Populate rest fields
                } // end -- if allocation

                //####################################################################################
                // CASE, PLANELEMENT = AGGREGATION

                else if( pe instanceof Aggregation) {
                    //
                    // passing in returnVec, populated below
                    returnVec = parseAggregation(tsk, returnVec, (Aggregation)pe,  verb, annotation, psc);
                }
                else {
                    throw new RuntimeException("Unknown PE type:" + pe.getClass().toString() );
                }
             } // end - if pe != null
             else {
                throw new RuntimeException("(Null PE found)");
             }
         }
         catch( Exception e ) {
                annote += "getChildren(" + getExceptionMessage( e ) + ")";
                returnVec = returnError( returnVec, new UITTException( annotation + annote));
         }
         return returnVec;
   }

   private Vector returnError( Vector returnVec, Object dataobj ) {
       return returnError( returnVec, dataobj, "No Details");
   }

   private Vector returnError( Vector returnVec,  Object dataobj, String simpleMsg)
   {
         TerminalRef tr = new TerminalRef();
         ///tr.answer = "ERROR=" + annotation;
         if( dataobj != null ) {
             tr.setData( dataobj ); // "LogPlan Parse ERROR(" + simpleMsg + ")" ); // dataobj);
         }
         returnVec.addElement(tr);
         return returnVec;
   }

   /** Returns java exception message **/
   private String getExceptionMessage( Exception e )
   {
       String annote = null;
       ByteArrayOutputStream bout = new ByteArrayOutputStream(16);
       PrintStream pout = new PrintStream(bout);
       e.printStackTrace(pout);
       pout.flush();
       String stack = bout.toString();
       int clip1 = 0;
       int clip2 = 400; if(stack.length()< 400) clip2 = stack.length();
       String clipped = stack.substring(clip1, clip2);
       //String clipped2 = ParserHelper.alphaNumeric(clipped);
       //clipped = "123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677123456789012343456677";
       return new String("CAPTURED_EXCEPTION(" + e.toString() + ", " + clipped  + ")");
   }

   private Vector parseAggregation(Task tsk, Vector returnVec,
                        Aggregation agg, String verb, String annotation, PlanServiceContext psc)
   {
         Enumeration en;
         Enumeration en2;

         String annote ="->Aggregation";
         //try{
                PlanElement pe =null;
                Composition comp = agg.getComposition();
                annote += ".getComposition()";
                MPTask mp = comp.getCombinedTask();
                annote += ".getCombinedTask()";

                pe = mp.getPlanElement();
                annote += ".getPlanElement()";

                if( pe == null ){
                    annote += "(pe==null)";
                }
                annote += "(PEtype=" + pe.getClass().toString() + ")";

                //PlanElement pe2 = (PlanElement)
                //       agg.getComposition().getCombinedTask().getComposition().getCombinedTask().getPlanElement();

                Vector v = getChildren(tsk, pe, verb, annotation + annote, psc);
                en2 = v.elements();
                while( en2.hasMoreElements() ) {
                           returnVec.addElement(en2.nextElement());
                }

         //} catch( Exception e ) {
         //       annote += "parseAggregation(" + getExceptionMessage( e ) + ")";
         //       returnVec = returnError( returnVec, annotation + annote);
         //}
         return returnVec;
   }


   /**
     * Allocation all = Allocation PE attached to Task tsk
     **/
   private Vector parseAllocation(Task tsk, Vector returnVec, Allocation all,
                         String verb, String annotation, PlanServiceContext psc)
   {
         Enumeration en;
         Enumeration en2;

         TraceRef er = new TraceRef();
         er.UID = new String(defaultUIDString(tsk.getUID()));

         String annote = annotation + "->PE(alloc)";
         //
         //
         // TRANSPORTATION MISSION TASK, ALLOCATED TO PHYSICAL ASSET
         // -- GRAB ITINERARY.
         //
         if( all.getAsset() instanceof org.cougaar.glm.ldm.asset.PhysicalAsset )
         {
              annote += "->getAsset(PhysicalAsset)";

              if( tsk.getVerb().equals(Constants.Verb.TransportationMission) )
              {
                    UIItinerary uiit = getItinerary(tsk, all);

                    uiit.setAllocOID(all.hashCode()); // We'll identify this UIItinerary with the allocation object
                    uiit.setClusterID(thisClusterID);

                    uiit.setInputTaskUID(getUIDOfInputTask(tsk));
                    uiit.setAllocTaskUID(defaultUIDString(tsk.getUID()));
                    //uiit.setAssetUID(defaultUIDString(all.getAsset().getUID()));

                    er.setData(uiit);
                    annote += "->[obtaining itinerary of TransporationMission Task whose Allocation is to Physical Asset]";
              }
         }
         //
         // ALLOCATION TO REMOTE or LOCAL ORGANIZATION ASSET
         else if( all.getAsset() instanceof Organization)
         {
             annote += "->getAsset(Organization)";
                          
             //
             // DEFAULT SETTINGS FOR ALLOCATIONS
             //System.out.println("ac.getAllocationTask() => null [" + all.toString() + "]");
             er.InputTaskUID = getUIDOfInputTask(tsk);

             //
             // make sure instance implements the getAllocTask() accessor?
             if( all instanceof AllocationforCollections )
             {
                          AllocationforCollections ac = (AllocationforCollections)all;
                          Task at = (Task)ac.getAllocationTask();

                          if( at != null ) {
                              er.RemoteClusterID =
                                 new String(
                                    verifyClusterName(
                                        getClusterIDAsString(
                                            at.getDestination().toString() ),
                                        psc));
                                        
                              er.AllocTaskUID = new String( at.getUID().toString()  );
                              annote += "->RemoteAllocation("
                                 + "/$"+er.RemoteClusterID
                                 + "/...RemoteTaskUID="
                                 + er.AllocTaskUID + ")";

                              // ############ ANSWER
                              er.answer = new String(
                                 "URL:"
                                 + "/$"+er.RemoteClusterID
                                 +"/alpine/demo/QUERYTRANSPORT.PSP" 
                                 + "?TASK="
                                 + er.AllocTaskUID
                                 + ";"
                                 );
                          }
             } // end instanceof AllocationsForCollections
             else {
                           er.RemoteClusterID =
                               new String(
                                   verifyClusterName(
                                       getClusterIDAsString(
                                             tsk.getDestination().toString() ),
                                       psc));
                                       
                            // er.AllocTaskUID = STRING_NULL;
                            er.AllocAssetUID =  defaultUIDString(all.getAsset().getUID());

                            annote += "->LocalOrganization("
                                 + all.getAsset().getUID() +")";

                            er.answer = new String(
                                 "URL:"
                                 + "/$"+er.RemoteClusterID
                                 +"/alpine/demo/QUERYTRANSPORT.PSP?ORGANIZATION="
                                 + defaultUIDString(all.getAsset().getUID())
                                 + ";"
                                 );
              }
         }
         //!Organization --
         //    LOCAL ASSET ALLOCATION
         else if( all.getAsset() != null) {

                 annote += "->getAsset(Local Asset)";

                 er.RemoteClusterID =
                     new String(
                          verifyClusterName(
                                   getClusterIDAsString(
                                         tsk.getDestination().toString() ),
                                   psc));

                 Asset ass = all.getAsset();
                 String id = null;
                 String type = null;
                 type = ass.getClass().getName();
                 if( ass.getUID() != null) {
                      id = defaultUIDString(ass.getUID());
                 } else {
                      /**
                              id = "ASSET_WITH_NULL_UID(" + type + ")";
                              throw new RuntimeException( annote + "=>" + id);

                              $$$$$$$$$$$$$ HACK FOLLOWS!!!!!!!!!!!
                              SUBSTITUTE HASHCODE FOR UID....!
                      **/
                      id = new String("HASHCODE_ID" + ass.hashCode());
                 }
                 er.AllocAssetUID =  id;
                 annote += annotation + "->LocalAsset("
                                        + id  + ")";
                 //
                 // ############ ANSWER
                 // URL: REFERENCE TO ASSET
                 // getAllocationResults(): ALLOCATION RESULTS DETAILS
                 //
                 er.answer = new String(
                                 "URL:"
                                 + "/$"+er.RemoteClusterID
                                 +"/alpine/demo/QUERYTRANSPORT.PSP?ASSET="
                                 + id
                                 + ";"
                                 //////////////////+ getAllocationResults(all)
                                 );
         }
         else{
               throw new RuntimeException(annote + "=>Allocation with NULL asset");
         }
         er.annotation += annote;
         returnVec.addElement(er);

       return returnVec;
   }

   //
   // ###################################################################
   // METHODS FOR PACKAGING RESULTS
   // RESULTS => ASSET TERMINAL/ANSWERS RETURNED TO CLIENT
   //
   private String getAssetResults(Asset all)
   {
       String results = new String();

       results += "TYPE:ASSET;";
       results += "UID:" + defaultUIDString(all.getUID()) + ";";

       return results;
   }
   //
   // getAllocationResults(): ALLOCATION RESULTS DETAILS
   // THIS METHOD CURRENTLY NOT USED.
   //
   /**
   private String getAllocationResults(Allocation all)
   {
       String results = new String();

       results += "TYPE:ALLOCATION;";
       AllocationResult ar = all.getReportedResult();
       if( ar != null )
       {
           AspectValue values[] = ar.getAspectValueResults();
           int i;
           int sz = values.length;
           for(i=0; i<sz; i++) {
              AspectValue val = (AspectValue)values[i];
              int type = val.getAspectType();
              if( type == AspectValue.START_TIME ){
                 results += "START_TIME:" + val.toString() + ";";
              }
              else if( type == AspectValue.END_TIME ) {
                 results += "END_TIME:" + val.toString() + ";";
              }
           }
       }
       return results;
   }
   **/
   // END METHODS FOR PACKAGING RESULTS
   // ###################################################################


    /** Returns "UNKNOWN" if ClusterID is not registered in White Pages **/
    private String verifyClusterName( String clustername, PlanServiceContext psc)
    {
        URL u = psc.lookupURL(clustername);
        if( u != null ) return clustername;
        else return UITTConstants.UNKNOWN_CLUSTERID_MARKER;
    }

    private String getClusterNameFromUID(String uuid)
    {
        int i = uuid.indexOf("/");
        String nme = uuid.substring(0,i);
        return nme;
    }
    /** Get rid of "<..>" prefix/suffix **/
    public String getClusterIDAsString( String rawCID)
    {
       String id  = rawCID;

       if( id.startsWith("<") ) id = id.substring(1);
       if( id.endsWith(">") ) id = id.substring(0,id.length()-1);

       return id;
    }

    public static String defaultUIDString(UID aUID) {
	return (aUID.getOwner() + "/" + aUID.getId());
    }

    /**
      * A PSP can output either HTML or XML (for now).  The server
      * should be able to ask and find out what type it is.
      **/
    public boolean returnsXML() {
         return false;
    }

    public boolean returnsHTML() {
         return true;
    }

    /**  Any PlanServiceProvider must be able to provide DTD of its
      *  output IFF it is an XML PSP... ie.  returnsXML() == true;
      *  or return null
      **/
    public String getDTD()  {
         return null;
    }


    /**
     private Vector myVectorOfTasks = new Vector();
    **/
    public void subscriptionChanged(Subscription subscription) {
    /**
             synchronized(myVectorOfTasks) {
                 Enumeration e = ((IncrementalSubscription)subscription).getAddedList();
                while (e.hasMoreElements()) {
                         Object obj = e.nextElement();
                         myVectorOfTasks.addElement(obj);
                }
             }
    **/
    }

    /**
     * .<pre>
     * Note:
     *   This method is somewhat broken -- see 
     *   <code>org.cougaar.lib.planserver.psp.PSP_PlanView</code> 
     *   for correct implementation!
     * </pre>
     */
  public static String getUIDOfInputTask(Task tsk)
   {
        String ret="root";
        if( tsk instanceof MPTask ) {
           MPTask mp = (MPTask)tsk;
           Enumeration en = mp.getParentTasks();
           while( en.hasMoreElements() ) {
              Task t = (Task)en.nextElement();
              ret += getUIDOfInputTask(t) + ";";
           }
        } else{
          UID puid = tsk.getParentTaskUID();
          if( puid != null ) {
            ret = puid.toString();
          }
        }

        if( ret == null ) ret = "null";
        return ret;
   }

   /**
     * .<pre>
     * Method encapsulating business rules  for construction of 
     * UICarrierItinerary instance which is returned to client.
     *
     * Allocation all = Allocation PE attached to Task tsk
     *
     * Note: 
     *  This method is somewhat broken -- see 
     *  <code>PSP_Carrier_Itinerary</code> 
     *  for correct implementation!
     * </pre>
     **/
   public static  UIItinerary getItinerary( Task tsk, Allocation all )
   {
       UICarrierItinerary uit = new UICarrierItinerary();

       PrepositionalPhrase ppto = tsk.getPrepositionalPhrase(Constants.Preposition.TO);
       PrepositionalPhrase ppfrom = tsk.getPrepositionalPhrase(Constants.Preposition.FROM);

       //uit.toGeoLocCode = ((GeolocLocation)ppto.getIndirectObject());
       //uit.fromGeoLocCode = ((GeolocLocation)ppfrom.getIndirectObject());
       
       PrepositionalPhrase ppof = tsk.getPrepositionalPhrase(Constants.Preposition.ITINERARYOF);

       Object io = ppof.getIndirectObject();
       String prep = ppof.getPreposition();
       if( (io instanceof Schedule ) && (prep.equalsIgnoreCase(Constants.Preposition.ITINERARYOF))
                 )
       {
                   Schedule sch = (Schedule)io;
                   uit.CarrierUID = defaultUIDString(all.getAsset().getUID());
                   uit.CarrierTypeNomenclature = all.getAsset().getTypeIdentificationPG().getNomenclature();
                   uit.CarrierItemNomenclature = all.getAsset().getItemIdentificationPG().getNomenclature();
                   uit.setScheduleElementType(sch.getScheduleElementType());
                   uit.setScheduleType(sch.getScheduleType());
                   Vector uitSchedElems = uit.getScheduleElements();
                   Enumeration ensch = sch.getAllScheduleElements();
                   while(ensch.hasMoreElements() ) {
                        ItineraryElement se = (ItineraryElement) ensch.nextElement();
                        UICarrierItineraryElement uiie= new UICarrierItineraryElement();
                        uiie.setVerbRole(se.getRole()); // Verb
                        uiie.setStartLocation((GeolocLocation)se.getStartLocation());  // GeolocLocation
                        uiie.setEndLocation((GeolocLocation)se.getEndLocation());  // GeolocLocation
                        uiie.setStartDate(se.getStartDate());  // Date
                        uiie.setEndDate(se.getEndDate());  // Date
                        uitSchedElems.addElement(uiie);
                   }
                   uit.setScheduleElements(uitSchedElems);
       } else {
                  throw new RuntimeException("Expected ItineraryOf followed by Schedule!");
       }
       return uit;
   }

}
