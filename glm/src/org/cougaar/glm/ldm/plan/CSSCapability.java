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

package org.cougaar.glm.ldm.plan;

import org.cougaar.planning.ldm.measure.*;
import java.io.*;

/**
 * representation of a CSS capability of a resource and a Capacity.
 * example: skill type "MOS/43M" at 88 man-hours per day
 *	or FuelPumping at 1000 gallons per hour.
 **/

public final class CSSCapability implements Serializable {
  private String type;
  private Capacity capacity;

  public CSSCapability(String type, Capacity capacity) {
    this.type=type;
    this.capacity=capacity;
  }

  /** string-only constructor.  The only syntax allowed is:
   * "TYPE CAPACITY-STRING"
   * example: "MOS/36E Count=20units Duration=1days"
   */
  public CSSCapability(String s) {
    int i = s.indexOf(' ');
    type = s.substring(0,i);
    capacity = new Capacity(s.substring(i+1));
  }

  /** type of capability, e.g. a skill ("MOS/43M"), or a Role("WaterTransportation") **/
  public String getType() { return type; }

  /** a Capacity object, possibly instantaneous, which
   * represents the capacity of the asset for type of capability.
   **/
  public Capacity getCapacity() { return capacity; }

  public boolean equals(Object o) {
    if (o instanceof CSSCapability) {
      CSSCapability oc = (CSSCapability) o;
      return ( type.equals(oc.getType()) &&
               capacity.equals(oc.getCapacity()) );
    } else 
      return false;
  }

  public int hashCode() {
    return type.hashCode()+capacity.hashCode();
  }
}
