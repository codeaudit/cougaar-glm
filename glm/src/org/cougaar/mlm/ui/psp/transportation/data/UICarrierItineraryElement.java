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
 
package org.cougaar.mlm.ui.psp.transportation.data;

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

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(CarrierActivity, "CarrierActivity");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = -6023865179694189419L;

}
