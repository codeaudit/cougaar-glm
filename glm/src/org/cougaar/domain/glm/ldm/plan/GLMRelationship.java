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







