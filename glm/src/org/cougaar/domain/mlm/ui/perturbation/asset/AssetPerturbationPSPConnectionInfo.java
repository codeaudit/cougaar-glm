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
 
package org.cougaar.domain.mlm.ui.perturbation.asset;

import org.cougaar.domain.mlm.ui.planviewer.PSPConnectionInfo;

/**
 * Configuration information for connecting to the ASSETPERTURBATION.PSP
 *
 */

public class AssetPerturbationPSPConnectionInfo implements PSPConnectionInfo {

  /** the package and id are used in the URL specified by the client
   *  to connect to the PSP
   */

  /**
   * PSP_package - location of the PSP
   */
  private static final String PSP_PACKAGE = "alpine/demo";

  /**
   * PSP_package - location of the PSP
   */
  public String getPSPPackage() {
    return PSP_PACKAGE;
  }

  private static final String PSP_ID = "ASSET_PERTURBATION.PSP";

  /**
   * PSP_id - name of the PSP
   */
  public String getPSPId() {
    return PSP_ID;
  }

  private static AssetPerturbationPSPConnectionInfo myCurrent = null;

  public static AssetPerturbationPSPConnectionInfo current() {
    if (myCurrent == null) {
      myCurrent = new AssetPerturbationPSPConnectionInfo();
    } 
    
    return myCurrent;
  }
}



