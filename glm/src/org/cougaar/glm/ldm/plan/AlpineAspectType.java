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

import org.cougaar.planning.ldm.plan.AspectRate;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.FloatAspectValue;

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
