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
 
package org.cougaar.mlm.ui.psp.plan;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;
import org.cougaar.core.util.UID;

import org.cougaar.glm.ldm.asset.Organization;

class ExternalRef
{
   String RemoteClusterID;
   String AllocTaskUID;       // UID of ALLOCATION TASK (Tsk->PE->Allocation->GetAllocTask() -- if applicable
   String UID;   // UID of this TASk
   String InputTaskUID; // Input task into this cluster
   String annotation=new String();
}

public class PSP_TaskTraversal extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber
{
    /** A zero-argument constructor is required for dynamically loaded PSPs,
        required by Class.newInstance()
        **/
    public PSP_TaskTraversal()
    {
       super();
    }

    public PSP_TaskTraversal( String pkg, String id ) throws RuntimePSPException
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
              //System.out.println("[PSP_TASKTRAVERSAL] PREDICATE CALLED true!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                          return true; }
                 return false;
          }
      };
    }

    /////private final String DEFAULT_VERB_FILTER = "Transport";

    public void execute( PrintStream out,
                          HttpInput query_parameters,
                          PlanServiceContext psc,
                          PlanServiceUtilities psu ) throws Exception
    {
            Enumeration en;

            String verbFilter =  (String)query_parameters.getFirstParameterToken("VERB", '=');
            String taskUID =  (String)query_parameters.getFirstParameterToken("UID", '=');
            ////if( verbFilter == null ) { verbFilter = DEFAULT_VERB_FILTER; }


            Vector collection = getAllTasks(psc, verbFilter);

            out.println("<HTML><BODY><FONT COLOR=mediumblue>");
            out.println("<CENTER><H3> Verb=" + verbFilter
                        + " @ " + psc.getServerPluginSupport().getClusterIDAsString()
                        + "</H3></CENTER>");

            out.println("<TABLE align=center border=1 cellPadding=1 cellSpacing=1");
            out.println("width=75% bordercolordark=#660000 bordercolorlight=#cc9966>");

            out.println("<TR>");
            out.println("<TD> <FONT color=mediumblue ><B>Trace</FONT></B> </TD>");
            out.println("</TR>");

            en = collection.elements();
            boolean tempflag = false;
            while(en.hasMoreElements() )
            {
                ExternalRef er = (ExternalRef)en.nextElement();
                out.println("<TR>");
                tempflag = false;
                if( taskUID != null ) {
                    //System.out.println("(*)>>>>>>>>>>>>>>>>>>>>>>>" + er.InputTaskUID + ",  " + taskUID);
                    if( er.annotation.indexOf(taskUID) > -1 )
                    {
                        out.println( "<TD><FONT COLOR=red>" + er.annotation + "</FONT></TD>");
                        tempflag = true;
                    }
                }
                if( tempflag == false )  {
                     out.println( "<TD>" + er.annotation + "</TD>"); }
                out.println("</TR>");
            }
            out.println("</TABLE>");
            out.println("</BODY></HTML></FONT>");
    }

    private Vector getAllTasks(PlanServiceContext psc, String verbFilter)
    {
            Enumeration en;
            Enumeration en2;

            Subscription subscription = psc.getServerPluginSupport().subscribe(this, getTaskPred());
            Collection container = ((CollectionSubscription)subscription).getCollection();
            en = new Enumerator(container);
            int numTasks = container.size();

            System.out.println("[getAllTasks], task container size=" + numTasks);

            Vector collection  = new Vector();
            boolean filtermatch = false;
            while( en.hasMoreElements() )
            {
                Task tsk = (Task)en.nextElement();
                Verb vrb = tsk.getVerb();

                System.out.println("[getAllTasks], task=" + vrb );

                //
                // VERB FILTER TEST....  NULL FILTER = MATCH EVERYTHING.
                // ELSE FILTER BY VERB.
                //
                if( verbFilter == null ){ filtermatch = true; }
                else if( true ==
                    vrb.toString().toUpperCase().startsWith(verbFilter.toString().toUpperCase() ) )
                {
                   filtermatch = true;
                }

                if( filtermatch)
                {
                    System.out.println("[getAllTasks], filtermatch == true");


                    Vector v = getChildren(tsk,
                       "Task(<FONT SIZE=-2 COLOR=green>" + tsk.getVerb() +
                       ", " + tsk.getUID() + "</FONT>"
                       + "<FONT SIZE= -2 COLOR=mediumblue>; inputTask="
                       + getUIDOfInputTask(tsk) + "</FONT>"
                       + ")",
                       verbFilter);
                    en2 = v.elements();
                    while( en2.hasMoreElements() ) {
                        collection.addElement(en2.nextElement());
                    }
                }
            }
            return collection;
    }


    /** Vector of ExternalRef instances **/
    private Vector getChildren(Task tsk, String annotation, String verb)
    {
         System.out.println(">getChildren("+annotation+")");

         Vector returnVec = new Vector();
         Enumeration en;
         Enumeration en2;
         PlanElement pe = tsk.getPlanElement();
         //System.out.println(">>pe= " + pe.toString());
         if( pe != null ){

            //####################################################################################
            // CASE, PLANELEMENT = EXPANSION

             if( pe instanceof Expansion ) {
                   //System.out.println(">PSP_TaskTraversal.getChildren.instanceofExpansion");
                   Expansion ex = (Expansion)pe;
                   en = ex.getWorkflow().getTasks();
                   while( en.hasMoreElements() ) {
                       Task tsk2 = (Task)en.nextElement();
                       Vector v = getChildren(tsk2, annotation + "->PE(exp)->Workflow->getTask()", verb);
                       en2 = v.elements();
                       while( en2.hasMoreElements() ) {
                           returnVec.addElement(en2.nextElement());
                       }
                   }
             }

            //####################################################################################
            // CASE, PLANELEMENT = ALLOCATION

            else if( pe instanceof Allocation ) {
                   //
                   // passing in returnVec, populated below
                   returnVec = parseAllocation(returnVec, tsk, annotation, verb);  // Populate rest fields
            } // end -- if allocation

            //####################################################################################
            // CASE, PLANELEMENT = AGGREGATION

            else if( pe instanceof Aggregation) {
                   //
                   // passing in returnVec, populated below
                   returnVec = parseAggregation(returnVec, tsk, annotation, verb);
            }
            else {
               System.out.println("[PSP_TaskTraversal] Unrecognized PE Type encountered: " + pe.getClass().toString() );
            }

         } // end - if pe != null
         else {
             ExternalRef er = new ExternalRef();
             er.annotation = annotation + "->getPlanElement()==NULL)";
             returnVec.addElement(er);
         }

         return returnVec;
   }

   private Vector parseAggregation(Vector returnVec, Task tsk, String annotation, String verb)
   {
         Enumeration en;
         Enumeration en2;

         //System.out.println(">PSP_TaskTraversal.getChildren.instanceofAggregation");

         ExternalRef er = new ExternalRef();
         er.UID = new String(defaultUIDString(tsk.getUID()));   /// "IMMUTABLE" FIELD -- shouldn't change
         er.InputTaskUID = getUIDOfInputTask(tsk);
         ///System.out.println("+++++++++AGGREGATION ER.UID=" + er.UID);

         Aggregation agg = (Aggregation)tsk.getPlanElement();

         String annote ="->Aggregation";
         try{
                PlanElement pe =null;
                Composition comp = agg.getComposition();
                annote += ".getComposition()";
                MPTask mp = comp.getCombinedTask();
                annote += ".getCombinedTask()";

                pe = mp.getPlanElement();
                annote += ".getPlanElement()";

                Vector v = getChildren(pe.getTask(),
                       annotation + annote, verb ); // "->Aggregation.getComposition().getCombinedTask().getComposition().getCombinedTask()", verb);
                en2 = v.elements();
                while( en2.hasMoreElements() ) {
                           returnVec.addElement(en2.nextElement());
                }
                return returnVec;

         } catch( Exception e ) {
                annote += "=>Exception(" + e.toString() + ")";
         }
         //
         // We only get here if exception thrown, eg. null pointers
         //
         er.RemoteClusterID = new String("null");
         er.AllocTaskUID = new String("null");
         er.annotation += annote;
         returnVec.addElement(er);

         return returnVec;
   }


   private Vector parseAllocation(Vector returnVec, Task tsk, String annotation, String verb)
   {
         Enumeration en;
         Enumeration en2;
         PlanElement pe = tsk.getPlanElement();

         //System.out.println(">PSP_TaskTraversal.getChildren.instanceofAllocation");

         //System.out.println(">PSP_TaskTraversal.getChildren.instanceofAllocation");
         Allocation all = (Allocation)pe;

         ExternalRef er = new ExternalRef();
         er.UID = new String(defaultUIDString(tsk.getUID()));


         String annote ="";
         //
         // ALLOCATION TO REMOTE or LOCAL ORGANIZATION ASSET
         if( all.getAsset() instanceof Organization)
         {
             //
             // DEFAULT SETTINGS FOR ALLOCATIONS
             //System.out.println("ac.getAllocationTask() => null [" + all.toString() + "]");
             er.RemoteClusterID = new String("null");
             er.AllocTaskUID = new String("null");
             er.InputTaskUID = getUIDOfInputTask(tsk);
             annote = "->PE(alloc).getAllocationTask() = NULL";

             //
             // make sure instance implements the getAllocTask() accessor?
             if( all instanceof AllocationforCollections )
             {
                          AllocationforCollections ac = (AllocationforCollections)all;
                          Task at = (Task)ac.getAllocationTask();

                          if( at != null ) {

                              er.RemoteClusterID = new String(
                                   getClusterIDAsString(
                                         at.getDestination().toString() ));
                              er.AllocTaskUID = new String( at.getUID().toString()  );
                              annote = new String(annotation + "->RemoteAllocation("
                                 + "<A HREF=\"/$"+er.RemoteClusterID
                                 +"/alpine/demo/TASKTRAVERSAL.PSP?VERB=" + at.getVerb().toString()
                                 + "?UID="
                                 + er.AllocTaskUID
                                 + "\"><FONT SIZE=-2 COLOR=green>"
                                 +  er.RemoteClusterID + ", " +  er.AllocTaskUID + "</FONT></A>"
                                 + ")" );
                          }
             } // end instanceof AllocationsForCollections
             else {
                    er.RemoteClusterID = new String(
                                   getClusterIDAsString(
                                         tsk.getDestination().toString() ));
                    er.AllocTaskUID = new String( "LocalOrganization"  );
                    String id = null;
                    if( all.getAsset().getUID() !=  null) {
                                id=defaultUIDString(all.getAsset().getUID());
                    }
                    if( id == null ) {id = "NULL_UID.TYPE=(" + all.getAsset().getClass().getName() + ")"; }

                    annote = new String(annotation + "->LocalOrganization(" + id + ")");
             }
         }
         //!Organization --
         //    LOCAL ASSET ALLOCATION
         else {
                    er.RemoteClusterID = new String(defaultUIDString(tsk.getUID()));
                    er.AllocTaskUID = new String( "LocalAsset"  );
                    String id = null;
                    if( all.getAsset().getUID() !=  null) {
                                id=defaultUIDString(all.getAsset().getUID());
                    }
                    if( id == null ) {id = "NULL_UID" + all.getAsset().getClass().getName(); }
                    annote = new String(annotation
                                    + "->LocalAllocation(" + id + ")");
         }
         er.annotation += annote;
         returnVec.addElement(er);

       return returnVec;
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

    public static String defaultUIDString(UID aUID) {
	return (aUID.getOwner() + "/" + aUID.getId());
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

}
