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

public class UICarrierItineraryElement extends UIItineraryElement
                                     implements java.io.Serializable
{

    public UICarrierItineraryElement() {}

    //#############################################################
    // START ATTRIBUTES
    /**
        * @return : Activity of this carrier on this leg (UICarrierItineraryElement).
        *       1 : Loading Cargo
        *       2 : Carrying Cargo
        *       3 : Unloading Cargo
        *       4 : Refueling
        *       5 : Waiting for Port availability
        *       6 : Waiting for Cargo
        *       7 : Unassigned
        **/
    public int CarrierActivity;

    // END ATTRIBUTES
    //#############################################################

    // START ACTIVITY INFORMATION

    public int getCarrierActivity() { return CarrierActivity; }
    // END ACTIVITY INFORMATION

  public void printContent(org.cougaar.util.AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(CarrierActivity, "CarrierActivity");
  }

  public String toString() {
    return org.cougaar.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = -6023865179694189419L;

}
