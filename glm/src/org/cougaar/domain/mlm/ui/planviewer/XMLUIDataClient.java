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
 * Displays forms that allow user to select cluster, plan objects,
 * and specific plan objects.  POSTs parameters to Plan Service Provider
 * on appropriate cluster, and displays results.
 */

public class XMLUIDataClient {

  public static void main(String[] args) {
    new XMLDisplay(XMLClientConfiguration.uiDataProviderTitle,
                   XMLClientConfiguration.PSP_package,
                   XMLClientConfiguration.UIDataPSP_id);
  }

}
