/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm.plan;

import org.cougaar.domain.glm.ldm.Constants;

/**
 * GLMRelationship - the Relationship an Organization has.
 **/


public class GLMRelationship {

  public static final String SELF = Constants.Role.SELF.getName();
  public static final String SUPERIOR = Constants.Role.SUPERIOR.getName();
  public static final String SUBORDINATE = Constants.Role.SUBORDINATE.getName();

  public static final String SUPPORTED = "Supported";
  public static final String SUPPORTING = "Supporting";

  public static boolean isDirectObject(String alpRelationship) {
    if (alpRelationship.equals(GLMRelationship.SUPPORTING) ||
        alpRelationship.equals(GLMRelationship.SELF) ||
        alpRelationship.equals(GLMRelationship.SUPERIOR)) {
      return true;
    } else if (alpRelationship.equals(GLMRelationship.SUPPORTED) ||
               alpRelationship.equals(GLMRelationship.SUBORDINATE)) {
      return false;
    } else {
      System.err.println("GLMRelationship.isDirectObject: unrecognized relationship " +
                         alpRelationship);
      return false;
    }
  }
}







