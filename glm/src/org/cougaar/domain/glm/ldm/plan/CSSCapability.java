/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm.plan;

import org.cougaar.domain.planning.ldm.measure.*;
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
