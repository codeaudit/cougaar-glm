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


package org.cougaar.mlm.debug.ui;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.cougaar.planning.ldm.plan.Schedule;

/** A tree node for a Schedule.
  Overrides the UITreeNode loadChildren, toString and isLeaf
  methods to dynamically display the Schedule which has no children.
  */

public class UIScheduleNode extends UITreeNode {
  Schedule schedule;
  String prefix;

  /** Create a tree node for the schedule.
    @param schedule schedule for which to create a tree node
   */
  public UIScheduleNode(Schedule schedule, String prefix) {
    super(schedule);
    this.schedule = schedule;
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
    System.out.println("UIScheduleNode:loadChildren called, but Schedule is a leaf.");
  }

  /** Return representation of a Schedule in a tree:
    The startLocation and endLocation are displayed only if they are
    specified in the schedule.
    @return From (startLocation) (startDate) to (endLocation) (endDate).
   */

  public String toString() {
    //String startLocation = schedule.getStartLocation();
    //String endLocation = schedule.getEndLocation();
    String s = "From ";
    //if (startLocation != null)
      //s = s + startLocation + " ";
    s = s + getDateLabel(new Date(schedule.getStartTime())) + " to ";
    //if (endLocation != null)
      //s = s + endLocation + " ";
    s = s + getDateLabel(new Date(schedule.getEndTime()));
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

