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

public class AlpineAspectType implements AspectType {
  public static final int DEMANDRATE       = N_CORE_ASPECTS + 0;
  public static final int DEMANDMULTIPLIER = N_CORE_ASPECTS + 1;
  public static final int LAST_ALPINE_ASPECT = DEMANDMULTIPLIER;

  private static String[] alpineAspectTypes = null;

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
}
