/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer;

/**
 * Configuration information for connecting to a PSP
 *
 */

public interface PSPConnectionInfo {

  public static final String [] allNames = {""};

  public static final String[] clusterNames = {
    "1-64-ARBN",
    "1BDE",
    "1FSB",
    "2-7-INBN",
    "2BDE",
    "2FSB",
    "3-69-ARBN",
    "369-TRKCO-PLS",
    "3ID",
    "ACALA",
    "COSCOM",
    "DISCOM",
    "FORSCOM",
    "MCCGlobalMode",
    "MSB",
    "TAACOM"
  };

  /** the package and id are used in the URL specified by the client
   *  to connect to the PSP
   */

  /**
   * PSP_package - location of the PSP
   */
  public String getPSPPackage();

  /**
   * PSP_id - name of the PSP
   */
  public String getPSPId();
}



