/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.glm.ldm.plan;

import java.io.Serializable;

import org.cougaar.planning.ldm.measure.Capacity;

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
