/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation.data;

import java.util.*;

public class UICarrierItineraryElementWithAssets
    extends UICarrierItineraryElement
    implements java.io.Serializable
{

  public UICarrierItineraryElementWithAssets() {}

  //#############################################################
  // START ATTRIBUTES
  /**
   * A vector of UITAssetInfo instances
   */
  private Vector UITAssetInfoVector;
  // END ATTRIBUTES
  //#############################################################

  // START ACTIVITY INFORMATION

  /** Vector of UITAssetInfo instances */
  public Vector getUITAssetInfoVector() {
    return UITAssetInfoVector;
  }
  public void setUITAssetInfoVector(Vector v) {
    UITAssetInfoVector = v;
  }
  // END ACTIVITY INFORMATION

  public void printContent(org.cougaar.util.AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(UITAssetInfoVector, "UITAssetInfoVector");
  }

  public String toString() {
    return org.cougaar.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 7775791306970982400L;

}
