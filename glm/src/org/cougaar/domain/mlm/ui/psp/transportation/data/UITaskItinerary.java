/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation.data;

import org.cougaar.core.util.PrettyStringPrinter;
import java.util.*;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;

public class UITaskItinerary 
    extends UIItinerary
    implements java.io.Serializable, java.lang.Cloneable 
  {

  public UITaskItinerary copy() {
    try {
      return (UITaskItinerary)super.clone();
    } catch (java.lang.CloneNotSupportedException noClone) {
      throw new RuntimeException("NEVER! "+noClone);
    }
  }

  //#############################################################
  // START ATTRIBUTES
  // ATTRIBUTES ARE PUBLIC FOR BACKWARD COMPATIBILITY -- SHOULD
  // USE ACCESSORS (SEE NEXT SECTION)
  //
  /**
    * Required Location Information
    * -- obtained from LogPlan TASK TO/FROM prepositional fields
    **/
  public GeolocLocation toRequiredLocation;
  public GeolocLocation fromRequiredLocation;
  /**
    * Required Date Information
    * -- obtained from LogPlan Task Preferences
    **/
  public Date earliestPickupDate;
  public Date latestDropoffDate;

  /**
   * A vector of UITAssetInfo instances
   */
  public Vector UITAssetInfoVector;

  /**
    * Unit for whom this Task is conducted
    * -- obtained from LogPlan from FOR prepositional field.
    **/
  public String TransportedUnitName;

  protected PrettyStringPrinter printer;

  // END ATTRIBUTES

  //############################################################

  //#############################################################
  // START ACCESSORS
  public GeolocLocation getToRequiredLocation() {
    return toRequiredLocation; 
  }
  public void setToRequiredLocation(GeolocLocation loc) {
    toRequiredLocation = loc; 
  }

  public GeolocLocation getFromRequiredLocation() {
    return fromRequiredLocation; 
  }
  public void setFromRequiredLocation(GeolocLocation loc) {
    fromRequiredLocation = loc;
  }

  public Date getEarliestPickupDate() { return earliestPickupDate; }
  public void setEarliestPickupDate(Date d) { earliestPickupDate = d; }

  public Date getLatestDropoffDate() { return latestDropoffDate; }
  public void setLatestDropoffDate(Date d) { latestDropoffDate = d; }

  /** Vector of UITAssetInfo instances */
  public Vector getUITAssetInfoVector() {
    return UITAssetInfoVector;
  }
  public void setUITAssetInfoVector(Vector v) {
    UITAssetInfoVector = v;
  }

  public String getTransportedUnitName() { return TransportedUnitName; }
  public void setTransportedUnitName(String s) { TransportedUnitName = s; }
  // END ACCESSORS
  //############################################################

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    super.printContent(pr);
    //pr.print(toRequiredLocation, "ToRequiredLocation");
    //pr.print(fromRequiredLocation, "FromRequiredLocation");
    pr.print(earliestPickupDate, "EarliestPickupDate");
    pr.print(latestDropoffDate, "LatestDropoffDate");
    pr.print(UITAssetInfoVector, "UITAssetInfoVector");
    pr.print(TransportedUnitName, "TransportedUnitName");
  }

  public String toString() {
      System.out.println("uti to string");
   java.io.ByteArrayOutputStream baout = 
      new java.io.ByteArrayOutputStream();
    UIPrinter pr = new UIPrinter(baout);
    pr.printSelfPrinter(this, "toString");
    return baout.toString();
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 2007496138508717718L;

}
