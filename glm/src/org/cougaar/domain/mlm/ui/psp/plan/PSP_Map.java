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
 
package org.cougaar.domain.mlm.ui.psp.plan;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.cougaar.core.cluster.*;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.policy.*;

public class PSP_Map extends PSP_BaseAdapter 
        implements PlanServiceProvider, UISubscriber
{
    /** A zero-argument constructor is required for dynamically loaded PSPs,
        required by Class.newInstance()
        **/
    public PSP_Map()
    {
       super();
    }

    public PSP_Map( String pkg, String id ) throws RuntimePSPException
    {
        setResourceLocation(pkg, id);
    }


    public boolean test(HttpInput query_parameters, PlanServiceContext sc)
    {
        super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
        return false;  // This PSP is only accessed by direct reference.
    }

    public void execute( PrintStream out,
                          HttpInput query_parameters,
                          PlanServiceContext psc,
                          PlanServiceUtilities psu ) throws Exception  {
      String line;
      try {

        if (query_parameters.existsParameter("RELATIONS"))  {

          Subscription subscription = psc.getServerPlugInSupport().subscribe(this, getOrgPred());
          Enumeration en = ((CollectionSubscription)subscription).elements();
          boolean empty = true;

          Organization self = null;
          // FIRST PASS - OBTAIN SELF ASSET
          while( en.hasMoreElements() )  {
             Organization org = (Organization)en.nextElement();
             
             if (org.isSelf()) {
               self = org;
               break;
             }
          }

          // Walk self orgs relationship schedule
          if (self != null) {
            String id = self.getClusterPG().getClusterIdentifier().toString();
            RelationshipSchedule schedule = self.getRelationshipSchedule();

            for (Iterator iterator = new ArrayList(schedule).iterator();
                 iterator.hasNext();) {
              Relationship relationship = (Relationship)iterator.next();
              Organization other = 
                (Organization) schedule.getOther(relationship);

              out.println("relate  '"+id+"' '" +
                          self.getUID().getUID() + "' '" + 
                          schedule.getMyRole(relationship) + "' '" + 
                          other.getUID().getUID() +"' '" +
                          schedule.getOtherRole(relationship) +"'");
            }
          }
        } else if (query_parameters.existsParameter("CLUSTERS"))  {
          Vector urls = new Vector();
          Vector names = new Vector();
          if (psc == null) {
            out.println("PSP_Map error:  psc is null ");
            return;
          }
          psc.getAllURLsAndNames(urls, names);
          if (names == null) {
            out.println("PSP_Map error:  getAllURLsAndNames returned null names ");
            return;
          }
          int sz = names.size();
          int i;
          for(i=0;i<sz;i++)    {
            String n = (String)names.elementAt(i);
            out.println("entry "+n);
          }
        } else if (query_parameters.existsParameter("TASKS"))  {
          Subscription subscription = 
            psc.getServerPluginSupport().subscribe(this, getTaskPred());
          int numTasks = ((CollectionSubscription)subscription).size();
          out.println("numtasks "+numTasks);
        } else {
          out.println("PSP_Map error: Parameter not found");
        }
      } catch (Exception e) {
        out.println("PSP_Map Exception: "+e);
      }
    }



    private static UnaryPredicate getOrgPred(){
        return new UnaryPredicate() {
              public boolean execute(Object o) {
                 if (o instanceof Organization) {
                          return true; }
                 return false;
          }
      };
    }

    public void subscriptionChanged(Subscription subscription) {
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

    private static UnaryPredicate getTaskPred(){
        return new UnaryPredicate() {
              public boolean execute(Object o) {
                 if (o instanceof Task) {
                          return true; }
                 return false;
          }
      };
    }



  }












