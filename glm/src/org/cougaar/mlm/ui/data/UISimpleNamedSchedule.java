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

package org.cougaar.mlm.ui.data;

import java.io.Serializable;
import java.util.Vector;

public class UISimpleNamedSchedule implements UISimpleNamedScheduleNames, Serializable {
  String name;
  Vector schedule;

  public UISimpleNamedSchedule(String name, Vector schedule) {
    this.name = name;
    this.schedule = schedule;
  }

  public String getName() {
    return name;
  }

  public Vector getSchedule() {
    return schedule;
  }

  public String toString() {
    return
      "UISimpleNamedSchedule {"+
      "\n name: "+name+
      "\n schedule: "+schedule+
      "\n}";
  }
}






