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

package org.cougaar.lib.util;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.ItineraryElement;
import org.cougaar.planning.ldm.plan.Location;
import org.cougaar.planning.ldm.plan.NewItineraryElement;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.Verb;

public class UTILItinerary {

  /**
   * Utility methods for creating a ItineraryElements, which will be
   * indirectObjects to the PrepPhrase ItineraryOf.
   * @param ldmf the PlanningFactory
   * @param v the Verb that describes the role of the ItineraryElement
   * @return NewItineraryElement
   */
  public NewItineraryElement makeItineraryElement(PlanningFactory ldmf, Verb v){
    NewItineraryElement ie = ldmf.newItineraryElement();
    ie.setRole(v);
    return ie;
  }

  /**
   * sets a leg of the itinerary
   * @param leg the itineraryElement to set
   * @param startLoc starting location of the leg
   * @param endLoc ending location of the leg, should be same as startLoc if
   *        the leg is either LOAD or UNLOAD
   * @param start starting time of the leg
   * @param end ending time of the leg
   */
  public void setItineraryLeg(NewItineraryElement leg, 
				     Location startLoc,
				     Location endLoc,
				     Date start,
				     Date end) {
    leg.setStartLocation(startLoc);
    leg.setEndLocation(endLoc);
    leg.setStartDate(start);
    leg.setEndDate(end);
  }

  /**
   * Return the first leg of this itinerary with the given role
   *
   * @param role role of the itinerary element to return
   * @param itin itinerary through which to search
   * @return the first itinerary leg found in the enumeration 
   *   returned by the Schedule
   * @return null if no element is found
   */
  public ItineraryElement getItineraryLeg(Verb role,
						 Schedule itin) {
    Enumeration schedule_elts = itin.getAllScheduleElements();
    while (schedule_elts.hasMoreElements()) {
      ItineraryElement itin_elt = (ItineraryElement)
	schedule_elts.nextElement();

      if (itin_elt.getRole().equals(role))
	return itin_elt;
    }
    
    return null;
  }
  
  /**
   * Return all the legs of this itinerary with the given role
   *
   * NOTE that the vector is expected to be empty or at least 
   * initialized and ready to be added to- no initialization is done
   * 
   * @param role role of the itinerary element to return 
   * @param itin itinerary through which to search
   * @param vec_to_return of itinerary elements to return
   */
  public void getItineraryLeg(Verb role,
				     Schedule itin,
				     Vector vec_to_return) {
    Enumeration schedule_elts = itin.getAllScheduleElements();
    while (schedule_elts.hasMoreElements()) {
      ItineraryElement itin_elt = (ItineraryElement)
	schedule_elts.nextElement();
      
      if (itin_elt.getRole().equals(role))
	vec_to_return.add(itin_elt);
    }
    
  }
  
  /**
   * makes the itinerary
   * @param ldmf The factory
   * @param itineraryElements an enum of all the legs.
   * @return Schedule the itinerary
   */
  public Schedule makeItinerary(PlanningFactory ldmf, Enumeration itineraryElements){
    return ldmf.newSchedule(itineraryElements);
  }
}
