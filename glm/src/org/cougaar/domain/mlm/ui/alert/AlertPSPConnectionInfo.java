/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.alert;

import org.cougaar.domain.mlm.ui.planviewer.PSPConnectionInfo;

/**
 * Configuration information for connecting to the ALERT.PSP
 *
 */

public class AlertPSPConnectionInfo implements PSPConnectionInfo {

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

  private static final String PSP_ID = "ALERT.PSP";

  /**
   * PSP_id - name of the PSP
   */
  public String getPSPId() {
    return PSP_ID;
  }

  private static AlertPSPConnectionInfo myCurrent = null;

  public static AlertPSPConnectionInfo current() {
    if (myCurrent == null) {
      myCurrent = new AlertPSPConnectionInfo();
    } 
    
    return myCurrent;
  }
}

