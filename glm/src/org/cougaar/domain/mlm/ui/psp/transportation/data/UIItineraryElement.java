/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation.data;

import java.util.Date;

import org.cougaar.domain.planning.ldm.plan.Verb;

import org.cougaar.domain.glm.plan.GeolocLocation;

public class UIItineraryElement implements org.cougaar.util.SelfPrinter, java.io.Serializable {

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
   * @returns one of the "INTERPOLATED_" byte values;
   */
  public void setInterpolatedInfo(byte b) {
    interpolatedInfo = b;
  }
  public byte getInterpolatedInfo() {
    return interpolatedInfo;
  }

  public void printContent(org.cougaar.util.AsciiPrinter pr) {
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
      //return org.cougaar.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = -9048397619395481621L;

}
