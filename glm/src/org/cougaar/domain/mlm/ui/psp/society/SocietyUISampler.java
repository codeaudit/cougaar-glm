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
 
package org.cougaar.domain.mlm.ui.psp.society;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.cougaar.domain.planning.ldm.plan.ItineraryElement;
import org.cougaar.domain.planning.ldm.plan.Location;
import org.cougaar.domain.planning.ldm.plan.ScheduleElement;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.mlm.ui.data.UISimpleInventory;
import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.domain.mlm.ui.psp.plan.UIOrgRelationship;
import org.cougaar.domain.mlm.ui.psp.transportation.data.*;
import org.cougaar.domain.mlm.ui.psp.transportation.PSP_Itinerary;

/**
  *  Helper class.
  *  Breaks out "Sample" and "Test" code for exercising SocietyUI
  *  interface.
  **/

public  class  SocietyUISampler
{
     private String hostUrlString;

     public SocietyUISampler() {
        this("http://localhost:5555");
     }

     public SocietyUISampler( String urlstr ) {
          if(  urlstr.endsWith("/") == true ) {
              System.err.println(
                  "[SocietyUISampler()] Remove trailing slash in host url string: " + urlstr );
          }
          hostUrlString = urlstr;
     }

     //#########################################################################
     // TEST -- OBTAIN ALL TRANSPORT/TRANSPORTATION-MISSION TASKS at Cluster
     // Returns Vector of TASK UIDS (String)
     //
     public Vector testGetTransportationTasks(String ClusterName)
     {
         Vector v =  SocietyUI.getTTasks(hostUrlString + "/$" + ClusterName );
         System.out.println("[SocietyUISampler.getTTasks]: return vector size=" + v.size() );
         Enumeration en = v.elements();
         while(en.hasMoreElements() ) {
            String elem = (String)en.nextElement();
            System.out.println("TTask=" + elem);
         }
         return v;
     }


     //#########################################################################
     // TEST -- FOR Named Cluster, obtain all UITaskItineraries
     //
     // @param mode :  either of these values:
     //      PSP_Itinerary.CANNED // Return canned itineraries
     //      PSP_Itinerary.GSS // Retrive from simple GSS/SimpleMultileg Transporation Society
     //      PSP_Itinerary.LIVE  // Retrieve from full TOPS Society
     //
     public void testGetUITaskItineraries( String ClusterName, String mode )
     {
          //
          // transported_unit_name_filter == null, return ALL TaskItineraries
          //
          Vector results = SocietyUI.getTaskItineraries(hostUrlString,
                                               ClusterName, null, null, mode,
                                                        false, false, false, false, false, false);

          Enumeration en = results.elements();
          while(en.hasMoreElements() )
          {
                 UITaskItinerary it = (UITaskItinerary)en.nextElement();
                 System.out.println("[SocietyUISampler.testGetUITaskItineraries]: UITaskItinerary="
                                     + it.toString() );
                 Vector elems = it.getScheduleElements();
                 Enumeration en2 = elems.elements();
                 while(en2.hasMoreElements()) {
                     UITaskItineraryElement elem = (UITaskItineraryElement)en2.nextElement();
                     System.out.println("[SocietyUISampler.testGetUITaskItineraries]:"
                                        + "            UITaskItineraryElement= (start="
                                        + elem.getStartLocation()  +")"
                                        +"(end=" + elem.getEndLocation() +")" );
                 }
          }
     }

     //#########################################################################
     // TEST -- FOR NAMED CLUSTER -- OBTAINED TRANSPORTATION NETWORK DATA
     // (IF EXISTS)
     //
     public void testGetTxNetwork( String ClusterName )
     {
         UITxNetwork network = SocietyUI.getTxNetwork(hostUrlString + "/$" + ClusterName);
         Collection nodes = network.getAllGroundNodes();
         Collection links = network.getAllGroundLinks();
         System.out.println("[SocietyUISampler.testGetTxNetwork]: # Nodes=" + nodes.size() );
         System.out.println("[SocietyUISampler.testGetTxNetwork]: # Links=" + links.size() );

     }


     //#########################################################################
     // TEST -- OBTAIN UI Org Relationships for all Clusters
     //
     public void testGetAllOrgRelationships(String ClusterName)
     {
         Vector relations =  SocietyUI.getNormalizedOrganizationRelationships(
                               hostUrlString + "/$" + ClusterName,
                               null); // <== null OrgUID, returns all Orgs and relationships
         Enumeration en = relations.elements();
         while( en.hasMoreElements() ) {
             UIOrgRelationship uit = (UIOrgRelationship)en.nextElement();
             System.out.println("[SocietyUISampler.testGetAllOrgRelationships]"
                                + " UIOrgRelationship=" + uit.toString() );
         }
     }

     //#########################################################################
     // TEST -- OBTAIN UI Organization Itinerary for named Cluster (Unit)
     //
     // ClusterName must be equal to Unit name!
     //
     public void testGetOrgItinerary(String ClusterName)
     {
         //UIUnit unit = SocietyUI.getOrgInfo(hostUrlString + "/$" + ClusterName);
         UIOrgItinerary org =
              SocietyUI.getOrganizationItinerary(
                               hostUrlString + "/$" + ClusterName,
                               ClusterName); // unit.getSelfAssetUID());
         if( org  != null)
         {
             Enumeration en = org.getScheduleElements().elements();
             while( en.hasMoreElements() ) {
                 UIOrgItineraryElement uit = (UIOrgItineraryElement)en.nextElement();
                 System.out.println("[SocietyUISampler.testGetOrgItinerary]"
                                    + "UIOrgItineraryElement=" + uit.toString() );
             }
         }
         else
         {
             System.out.println("[SocietyUISampler.testGetOrgItinerary]"
                                  + " Null Organization Itinerary returned.");
         }
     }

     //#########################################################################
     // TEST -- OBTAIN ORGANIZATION LOCATION
     // Returns UIUnit
     //
     public void testGetOrgInfo( String ClusterName )
     {
         UIUnit unit =
                   SocietyUI.getOrgInfo(hostUrlString + "/$" + ClusterName);

         if( unit != null ) System.out.println("UIUnit=" + unit.toString());
         else System.out.println("UIUnit=null");
     }

}

