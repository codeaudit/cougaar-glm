/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.common;

/**
 * Provides an implementation of the getClassIndex part of the
 * EGObject interface by searching for the object class in the
 * egObjectClasses array.
 **/
public class EGObjectBase {
  protected int classIndex;

  public static int findClassIndex(Class cls) {
    for (int i = 0; i < EGObject.egObjectClasses.length; i++) {
      if (EGObject.egObjectClasses[i] == cls) {
        return i;
      }
    }
    throw new RuntimeException("Class index not found for " + cls.getName());
  }

  public EGObjectBase() {
    classIndex = findClassIndex(this.getClass());
  }

  public int getClassIndex() {
    return classIndex;
  }
}
