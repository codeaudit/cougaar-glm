/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss;

import org.cougaar.domain.planning.ldm.asset.Asset;
import java.lang.reflect.Method;

/**
 * Invoke the specified method on the given asset
 *
 */

public class GSAssetMethod implements GSAssetAccessor {

  private Method method;
  private GSSchedulingSpecs specs;

  public GSAssetMethod (String methodName, String className,
			GSSchedulingSpecs specs) {
    this.specs = specs;
    try {
      method = Class.forName(className).getMethod
        (methodName, new Class[] {Asset.class});
      
    } catch (Exception e) {
      System.err.println (e.getMessage());
      e.printStackTrace();
    }
  }

  public Object value (Asset asset) {
    try {
      return method.invoke (specs, new Object[] {asset});
    } catch (Exception e) {
      System.err.println (e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  /**
   * For GSAssetAccessor- stub
   *  value returned null - give some indication why 
   */
  public void reportError (Asset asset) {
    System.err.println("GSAssetMethod.reportError - Invalid asset or specs or method for asset " + asset);
  }
}

