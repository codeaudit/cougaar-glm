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

import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.AspectRate;
import org.cougaar.planning.ldm.plan.FloatAspectValue;

import org.cougaar.planning.ldm.measure.CountRate;

public class AlpineAspectType implements AspectType {
  public static final int DEMANDRATE       = N_CORE_ASPECTS + 0;
  public static final int DEMANDMULTIPLIER = N_CORE_ASPECTS + 1;
  public static final int LAST_ALPINE_ASPECT = DEMANDMULTIPLIER;

  private static String[] alpineAspectTypes = null;

  public static void init() {
    // for use by the PlanningDomain to force type registration
  }

  public static String[] getAspectTypes() {
    if (alpineAspectTypes == null) {
      alpineAspectTypes = new String[LAST_ALPINE_ASPECT + 1];
      for (int i = 0; i < alpineAspectTypes.length; i++) {
        alpineAspectTypes[i] = aspectTypeToString(i);
      }
    }
    return alpineAspectTypes;
  }

  public static String aspectTypeToString(int aspectType) {
    switch (aspectType) {
    case DEMANDRATE: return "DEMANDRATE";
    case DEMANDMULTIPLIER: return "DEMANDMULTIPLIER";
    default:
      return AspectValue.aspectTypeToString(aspectType);
    }
  }

  /** Start time of given Task **/
  public static final Factory DemandRate = new Factory () { 
      public int getKey() { return DEMANDRATE; }
      public String getName() { return "DEMANDRATE"; }
      public AspectValue newAspectValue(Object o) { return AspectRate.create(DEMANDRATE,o); }
      public AspectValue newAspectValue(long o) { return AspectRate.create(DEMANDRATE, o); }
      public AspectValue newAspectValue(double o) { return AspectRate.create(DEMANDRATE, o); }
      public AspectValue newAspectValue(float o) { return AspectRate.create(DEMANDRATE, o); }
      public AspectValue newAspectValue(int o) { return AspectRate.create(DEMANDRATE, o); }

    };
  /** Start time of given Task **/
  public static final Factory DemandMultiplier = new Factory () { 
      public int getKey() { return DEMANDMULTIPLIER; }
      public String getName() { return "DEMANDMULTIPLIER"; }
      public AspectValue newAspectValue(Object o) { return FloatAspectValue.create(DEMANDMULTIPLIER,o); }
      public AspectValue newAspectValue(long o) { return FloatAspectValue.create(DEMANDMULTIPLIER, (float) o); }
      public AspectValue newAspectValue(double o) { return FloatAspectValue.create(DEMANDMULTIPLIER,(float) o); }
      public AspectValue newAspectValue(float o) { return FloatAspectValue.create(DEMANDMULTIPLIER,o); }
      public AspectValue newAspectValue(int o) { return FloatAspectValue.create(DEMANDMULTIPLIER, (float)o); }
    };

  static {
    registry.registerFactory(DemandRate);
    registry.registerFactory(DemandMultiplier);
  }


}
