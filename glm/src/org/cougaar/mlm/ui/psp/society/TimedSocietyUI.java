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
 
package org.cougaar.mlm.ui.psp.society;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.cougaar.core.domain.LDMServesPlugIn;
import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.ItineraryElement;
import org.cougaar.planning.ldm.plan.Location;
import org.cougaar.planning.ldm.plan.ScheduleElement;

import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.mlm.ui.data.UISimpleInventory;
import org.cougaar.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.mlm.ui.psp.transportation.PSP_Itinerary;

public  class  TimedSocietyUI extends SocietyUI
{
    public TimedSocietyUI(long interval) {
        TimedFetchObject tf = new TimedFetchObject();
        tf.setTimeOut(interval); // millis
        SocietyUI.setObjectFetchHelper(tf);
    }

    //#########################################################################
    // fetchXXXObject() is exposed (beyound SocietyUI API)
    // so that other UI widgets can direct access PSPs for data and still
    // have "timed" behavior enforced.
    //
    public static Object fetchSerializedObject(String urlstring)
        throws Exception {
        System.out.println("Using TimedSocietyUI.fetchSerializedObject(...");
        return SocietyUI.fetchObject(urlstring, null, false);
    }

    public static Object fetchObject(String urlstring, boolean useXML)
        throws Exception {
        System.out.println("Using TimedSocietyUI.fetchObject(...");
        return SocietyUI.fetchObject(urlstring, null, useXML);
    }

    public static Object fetchObject(String urlstring, String postfield, boolean useXML)
        throws Exception {
        System.out.println("Using TimedSocietyUI.fetchObject(...");
        return SocietyUI.fetchObject(urlstring, postfield, useXML);
    }

    /**
      * main() : TEST script and EXAMPLE of usage...
      **/
    public static void main(String argv[]){
        System.out.println("Entered TimedSocietyUI main...");
        try{
           //
           // TEST EXAMPLE BASED ON SOCIETYUI API...
           //
           TimedSocietyUI timed = new TimedSocietyUI(30000);  // MILLIS
           timed.fetchObject("http://localhost:5555/alpine/demo/ITINERARY.PSP?FORMAT=XML?MODE=CANNED", true);
           Vector v = timed.getTaskItineraries(
                       "http://localhost:5555",
                       "MCCGlobalMode",null,null,PSP_Itinerary.CANNED,
                       false,false,false,false,false,false);
           System.out.println("[TimedSocietyUI.main] v.size = " + v.size());
           v = timed.getTaskItineraries(
                       "http://localhost:5555",
                       "MCCGlobalMode",null,null,PSP_Itinerary.CANNED,
                       false,false,false,false,false,false);
           System.out.println("[TimedSocietyUI.main] v.size = " + v.size());

           //
           // GINNY, BELOW SAMPLE IS RELEVANT TO YOU:
           // TEST EXAMPLE BASED ON fetchSerializationObject() API...
           //

           // PARAM: time-out threshold (in millis)
           TimedSocietyUI timed2 = new TimedSocietyUI(30000);

           // PARAM: complete URL to PSP
           // PARAM: post data / arguments as string
           // PARAM: true if return object type is XMLizable (per Todd's ObjectStreamReader)
           //        false if return serialized object
           Object myObj = timed2.fetchObject("http://localhost:5555/alpine/demo/INVENTORY.PSP", "ASSET", false);

           System.out.println("RECEIVED: OBJECT: " + myObj.getClass().getName() );



        } catch (Exception e)
        {
           e.printStackTrace();
        }
     }
}
