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
 
package org.cougaar.mlm.ui.planviewer;

import java.io.*;
import java.util.*;

import org.cougaar.mlm.ui.psp.society.SocietyUI;
import org.cougaar.mlm.ui.psp.transportation.data.UITaskItinerary;

/**
 * Displays UIItinerary objects read from an ObjectInputStream from 
 * PSP_Itinerary
 */

public class ItineraryClient {

  public static void main(String[] args) {
      String arg = "GSS";

      if (args.length > 0) {
          arg = args[0];
      }

      Vector itins = 
        SocietyUI.getTaskItineraries("http://localhost:5555", "MCCGlobalMode",
                                     null, null, arg,
                                     false,false,false,false,false,false);
      for(Enumeration e = itins.elements();e.hasMoreElements();) {
          UITaskItinerary itin = (UITaskItinerary)e.nextElement();
          System.out.println("Got Itin : " + itin);
      }
  }

}
