/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer;

import java.io.*;
import java.util.*;

import org.cougaar.domain.mlm.ui.psp.society.SocietyUI;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItinerary;

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
