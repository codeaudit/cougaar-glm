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

package org.cougaar.mlm.debug.ui;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.cougaar.planning.ldm.plan.ScheduleElement;

/** A tree node for a ScheduleElement.
  Overrides the UITreeNode loadChildren, toString and isLeaf
  methods to dynamically display the Schedule which has no children.
  */

public class UIScheduleElementNode extends UITreeNode {
  ScheduleElement schedule;
  String prefix;

  /** Create a tree node for the schedule element.
    @param scheduleelement scheduleelement for which to create a tree node
   */
  public UIScheduleElementNode(ScheduleElement scheduleelement, String prefix) {
    super(scheduleelement);
    this.schedule = scheduleelement;
    this.prefix = prefix;
  }

  /** The Schedule is a leaf.
    @return true
   */
  public boolean isLeaf() {
    return true;
  }

  /** This shouldn't be called because this is a leaf.
   */

  public void loadChildren() {
    System.out.println("UIScheduleElementNode:loadChildren called, but ScheduleElement is a leaf.");
  }

  /** Return representation of a ScheduleElement in a tree:
    The startLocation and endLocation are displayed only if they are
    specified in the scheduleelement (LocationRangeScheduleElement).
    @return From (startLocation) (startDate) to (endLocation) (endDate).
   */

  public String toString() {
    //String startLocation = schedule.getStartLocation();
    //String endLocation = schedule.getEndLocation();
    String s = "From ";
    //if (startLocation != null)
      //s = s + startLocation + " ";
    s = s + getDateLabel(schedule.getStartDate()) + " to ";
    //if (endLocation != null)
      //s = s + endLocation + " ";
    s = s + getDateLabel(schedule.getEndDate());
    return prefix + s;
  }

  /** Return a string for the date and time in GMT.
   */

  private String getDateLabel(Date date) {
    DateFormat dateFormat = 
      DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.format(date);
  }

}