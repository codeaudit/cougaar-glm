/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

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

}






