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

import java.util.*;

/**
 * All UITaskItineraryElement instances are a subclass of this class.
 * Currently there are three:
 * <p>
 * @see UITaskItineraryElementCarrier
 * @see UITaskItineraryElementOrg
 */

public abstract class UITaskItineraryElement
    extends UIItineraryElement
{

  public UITaskItineraryElement() { super(); TransportationMode = 0; }

  public UITaskItineraryElement copy() {
    try {
      return (UITaskItineraryElement)super.clone();
    } catch (java.lang.CloneNotSupportedException noClone) {
      throw new RuntimeException("NEVER! "+noClone);
    }
  }

  //#############################################################
  // START ATTRIBUTES
  // ATTRIBUTES ARE PUBLIC FOR BACKWARD COMPATIBILITY -- SHOULD
  // USE ACCESSORS (SEE NEXT SECTION)
  //

  /* allows one to debug tasks */
  private String taskUID;

  /** 
   * Only Carriers really know if they are sea/air/ground.  Itinerary
   * tasks handed off to other organizations might be combinations
   * (e.g. ground+air+ground, etc).  However, UI might want to label
   * some Orgs as "AIR" orgs, etc, so this info is here.
   **/
  public final static int SEA_MODE = 1;
  public final static int AIR_MODE = 2;
  public final static int GROUND_MODE = 3;
  public final static int NONE_MODE = 9;

  /** 
   * @see SEA_MODE
   * @see AIR_MODE
   * @see GROUND_MODE
   * @see NONE_MODE
   **/
  public int TransportationMode;

  /** from preferences **/
  protected Date startEarliestDate;
  protected Date endEarliestDate;
  protected Date endBestDate;
  protected Date endLatestDate;


  // 

  protected boolean isDirectElement = true;
  protected boolean isOverlapElement = false;

  // END ATTRIBUTES
  //############################################################

  //#############################################################
  // START ACCESSORS

  public String getTaskUID() { return taskUID; }
  public void setTaskUID(String s) {
    taskUID = s;
  }

  public int getTransportationMode() { return TransportationMode; }
  public void setTransportationMode(int i) {
    TransportationMode = i;
  }

  public Date getStartEarliestDate() { return startEarliestDate; }
  public void setStartEarliestDate(Date d) {
    startEarliestDate = d;
  }

  public void setStartAllDates(Date d) {
    // shortcut
    startDate = d;
    startEarliestDate = d;
  }

  public Date getEndEarliestDate() { return endEarliestDate; }
  public void setEndEarliestDate(Date d) {
    endEarliestDate = d;
  }

  public Date getEndBestDate() { return endBestDate; }
  public void setEndBestDate(Date d) {
    endBestDate = d;
  }

  public Date getEndLatestDate() { return endLatestDate; }
  public void setEndLatestDate(Date d) {
    endLatestDate = d;
  }

  public void setEndAllDates(Date d) {
    // shortcut
    endDate = d;
    endEarliestDate = d;
    endBestDate = d;
    endLatestDate = d;
  }

  /**
     * Get the value of isDirectElement.
     * @return Value of isDirectElement.
     */
  public boolean getIsDirectElement() {return isDirectElement;}
  
  /**
     * Set the value of isDirectElement.
     * @param v  Value to assign to isDirectElement.
     */
  public void setIsDirectElement(boolean  v) {this.isDirectElement = v;}
  

  /**
     * Get the value of isOverlapElement.
     * @return Value of isOverlapElement.
     */
  public boolean getIsOverlapElement() {return isOverlapElement;}
  
  /**
     * Set the value of isOverlapElement.
     * @param v  Value to assign to isOverlapElement.
     */
  public void setIsOverlapElement(boolean  v) {this.isOverlapElement = v;}
  



  //END ACCESSORS
  //############################################################

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(taskUID, "taskUID");
    pr.print(TransportationMode, "TransportationMode");
    pr.print(startEarliestDate, "StartEarliestDate");
    pr.print(endEarliestDate, "EndEarliestDate");
    pr.print(endBestDate, "EndBestDate");
    pr.print(endLatestDate, "EndLatestDate");
    pr.print(isDirectElement, "IsDirectElement");
    pr.print(isOverlapElement, "IsOverlapElement");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = -3600972075844065408L;

}
