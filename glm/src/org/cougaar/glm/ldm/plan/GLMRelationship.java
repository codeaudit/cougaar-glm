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

import org.cougaar.glm.ldm.Constants;

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







