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
 
package org.cougaar.mlm.ui.psp.transportation.data;

import java.util.Date;

import org.cougaar.planning.ldm.plan.Verb;

import org.cougaar.glm.ldm.plan.GeolocLocation;

public class UIItineraryElement implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

  protected Verb verbRole;
  protected GeolocLocation startLocation;
  protected GeolocLocation endLocation;
  protected Date startDate;
  protected Date endDate;
  protected String activityType;
  protected String opTempo;
  protected byte interpolatedInfo;

  /**
   * Byte masks for the InterpolatedInfo.
   * INTERPOLATED_START means that the startLocation 
   * and startDate are interpolated.
   */
  public static final byte INTERPOLATED_START = 1;
  public static final byte INTERPOLATED_END   = 2;
        
  /**
   * Simple mask ORs for the InterpolatedInfo.
   */
  public static final byte INTERPOLATED_NEITHER = 0;
  public static final byte INTERPOLATED_BOTH    = 3;

  public UIItineraryElement() {}

  /**
   * get and set access methods for <code>VerbRole</code>
   */
  public Verb getVerbRole() {return verbRole;}
  public void setVerbRole(Verb verbRole) {
    this.verbRole = verbRole;
  }

  /**
   * get and set access methods for <code>StartLocation</code>
   */
  public GeolocLocation getStartLocation() {return startLocation;}
  public void setStartLocation(GeolocLocation startLocation) {
    this.startLocation = startLocation;
  }

  /**
   * get and set access methods for <code>EndLocation</code>
   */
  public GeolocLocation getEndLocation() {return endLocation;}
  public void setEndLocation(GeolocLocation endLocation) {
    this.endLocation = endLocation;
  }

  /**
   * get and set access methods for <code>StartDate</code>
   */
  public Date getStartDate() {return startDate;}
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  /**
   * get and set access methods for <code>EndDate</code>
   */
  public Date getEndDate() {return endDate;}
  public void setEndDate(Date endDate) {this.endDate=endDate;}

  /**
   * get and set access methods for <code>ActivityType</code>
   * <p>
   * For UIOrgItinerary, as taken from the OrgActivity
   */
  public String getActivityType() {return activityType;}
  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  /**
   * get and set access methods for <code>OpTempo</code>
   * <p>
   * For UIOrgItinerary, as taken from the OrgActivity
   */
  public String getOpTempo() {return opTempo;}
  public void setOpTempo(String opTempo) {this.opTempo=opTempo;}

  /**
   * get and set access methods for <code>InterpolatedInfo</code>
   * <p>
   * For the UITaskItineraryElements and UICarrierItineraryElements.
   * @return one of the "INTERPOLATED_" byte values;
   */
  public void setInterpolatedInfo(byte b) {
    interpolatedInfo = b;
  }
  public byte getInterpolatedInfo() {
    return interpolatedInfo;
  }

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    pr.print(verbRole, "VerbRole");
    // pr.print(startLocation.getGeolocCode(), "StartLocation");
    // pr.print(endLocation.getGeolocCode(), "EndLocation");
    pr.print(startDate, "StartDate");
    pr.print(endDate, "EndDate");
    pr.print(activityType, "ActivityType");
    pr.print(opTempo, "OpTempo");
    pr.print(interpolatedInfo, "InterpolatedInfo");
  }

  public String toString() {
      System.out.println("uie to string");
      java.io.ByteArrayOutputStream baout = 
	  new java.io.ByteArrayOutputStream();
      UIPrinter pr = new UIPrinter(baout);
      pr.printSelfPrinter(this, "toString");
      return baout.toString();
      //return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = -9048397619395481621L;

}
