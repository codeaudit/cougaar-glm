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

import java.util.Vector;

import org.cougaar.domain.planning.ldm.plan.ScheduleElement;
import org.cougaar.domain.planning.ldm.plan.ScheduleElementType;

import org.cougaar.domain.glm.plan.GeolocLocation;


public class UIItinerary implements org.cougaar.util.SelfPrinter, java.io.Serializable {

  private long allocOID;
  private String assetUID;
  private String allocTaskUID;
  private String clusterID;
  private String inputTaskUID;
  private String scheduleElementType;
  private String scheduleType;
  private Vector scheduleElements = new Vector();
  private GeolocLocation toGeoLocCode;
  private GeolocLocation fromGeoLocCode;
  private String annotation;

  public UIItinerary() {}

  /**
   * get and set access methods for <code>AllocOID</code>
   * <p>
   * Implementation detail: OID (hashcode) of allocation to which this UIItinerary is derived.
   */
  public long getAllocOID() {return allocOID;}
  public void setAllocOID(long allocOID) {
    this.allocOID = allocOID;
  }

  /**
   * get and set access methods for <code>AssetUID</code>
   * <p>
   * Physical Asset which has been allocated to Task identified by AllocTaskUID
   */
  public String getAssetUID() {return assetUID;}
  public void setAssetUID(String assetUID) {
    this.assetUID = assetUID;
  }

  /**
   * get and set access methods for <code>AllocTaskUID</code>
   * <p>
   * Task to whose Allocation (to a physical asset) this UIItinerary is derived
   */
  public String getAllocTaskUID() {return allocTaskUID;}
  public void setAllocTaskUID(String allocTaskUID) {
    this.allocTaskUID = allocTaskUID;
  }

  /**
   * get and set access methods for <code>ClusterID</code>
   * <p>
   * String form of ClusterID of cluster from which this schedule was obtained
   */
  public String getClusterID() {return clusterID;}
  public void setClusterID(String clusterID) {
    this.clusterID = clusterID;
  }

  /**
   * get and set access methods for <code>InputTaskUID</code>
   * <p>
   * Input Task into cluster from AllocTaskUID is a child
   */
  public String getInputTaskUID() {return inputTaskUID;}
  public void setInputTaskUID(String inputTaskUID) {
    this.inputTaskUID = inputTaskUID;
  }

  /**
   * get and set access methods for <code>ScheduleElementType</code>
   */
  public String getScheduleElementType() {return scheduleElementType;}
  public void setScheduleElementType(String scheduleElementType) {
    this.scheduleElementType = scheduleElementType;
  }
  public void setScheduleElementType(Class scheduleElementClass) {
    this.scheduleElementType = 
      scheduleElementClassToString(scheduleElementClass);
  }

  /**
   * get and set access methods for <code>ScheduleType</code>
   */
  public String getScheduleType() {return scheduleType;}
  public void setScheduleType(String scheduleType) {
    this.scheduleType = scheduleType;
  }

  /**
   * get and set access methods for <code>ScheduleElements</code>
   * <p>
   * Contains UIItineraryElement instances
   */
  public Vector getScheduleElements() {return scheduleElements;}
  public void setScheduleElements(Vector scheduleElements) {
    this.scheduleElements = scheduleElements;
  }

  /**
   * get and set access methods for <code>ToGeoLocCode</code>
   */
  public GeolocLocation getToGeoLocCode() {return toGeoLocCode;}
  public void setToGeoLocCode(GeolocLocation toGeoLocCode) {
    this.toGeoLocCode = toGeoLocCode;
  }

  /**
   * get and set access methods for <code>FromGeoLocCode</code>
   */
  public GeolocLocation getFromGeoLocCode() {return fromGeoLocCode;}
  public void setFromGeoLocCode(GeolocLocation fromGeoLocCode) {
    this.fromGeoLocCode = fromGeoLocCode;
  }

  /**
   * get and set access methods for <code>Annotation</code>
   * <p>
   * developer comments added by parser at server
   */
  public String getAnnotation() {return annotation;}
  public void setAnnotation(String annotation) {
    this.annotation = annotation;
  }

  public void printContent(org.cougaar.util.AsciiPrinter pr) {
    pr.print(allocOID, "AllocOID");
    pr.print(assetUID, "AssetUID");
    pr.print(allocTaskUID, "AllocTaskUID");
    pr.print(clusterID, "ClusterID");
    pr.print(inputTaskUID, "InputTaskUID");
    pr.print(scheduleElementType, "ScheduleElementType");
    pr.print(scheduleType, "ScheduleType");
    pr.print(scheduleElements, "ScheduleElements");
    //pr.print(toGeoLocCode.getGeolocCode(), "ToGeoLocCode");
    //pr.print(fromGeoLocCode.getGeolocCode(), "FromGeoLocCode");
    pr.print(annotation, "Annotation");
  }

  public String toString() {
      System.out.println("ui to string");
   java.io.ByteArrayOutputStream baout = 
      new java.io.ByteArrayOutputStream();
    UIPrinter pr = new UIPrinter(baout);
    pr.printSelfPrinter(this, "toString");
    return baout.toString();
  }

  public static String scheduleElementClassToString(Class typeClass) {
    String type;

    if (!ScheduleElement.class.isAssignableFrom(typeClass)) {
      type = "Unknown";
    } else if (typeClass == ScheduleElementType.LOCATION) {
      type = "Location";
    } else if (typeClass == ScheduleElementType.LOCATIONRANGE) {
      type = "LocationRange";
    } else {
      type = "ScheduleElement";
    }

    return type;
  }


  /** set 3/29/00 **/
  static final long serialVersionUID = -867805144082970779L;
}
