package org.cougaar.domain.glm.ldm.plan;

import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AspectValue;

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
