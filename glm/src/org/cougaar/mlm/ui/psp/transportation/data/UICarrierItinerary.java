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

import org.cougaar.glm.ldm.plan.GeolocLocation;

public class UICarrierItinerary extends UIItinerary
                              implements java.io.Serializable {

  public UICarrierItinerary() {}


  //#############################################################
  // START ATTRIBUTES
  // ATTRIBUTES ARE PUBLIC FOR BACKWARD COMPATIBILITY -- SHOULD
  // USE ACCESSORS (SEE NEXT SECTION)

  public String CarrierUID;   // Physical Asset which has been Allocated to Task identified by
                              // super.getAllocTaskUID()

  public String CarrierTypeNomenclature;  // Type Nomenclature of Physical Asset ... (eg. B747)
  public String CarrierItemNomenclature;  // Item Nomenclature of Physical Asset ... (eg. TailNumber)

  // END ATTRIBUTES
  //#############################################################


  //#############################################################
  // START ACCESSORS
  public String getCarrierUID() { return CarrierUID; }
  public void setCarrierUID(String s) { CarrierUID = s; }

  public String getCarrierTypeNomenclature() {
    return  CarrierTypeNomenclature; 
  }
  public void setCarrierTypeNomenclature(String s) {
    CarrierTypeNomenclature = s;
  }

  public String getCarrierItemNomenclature() {
    return  CarrierItemNomenclature; 
  }
  public void setCarrierItemNomenclature(String s) {
    CarrierItemNomenclature = s;
  }
  // END ACCESSORS
  //#############################################################

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(CarrierUID, "CarrierUID");
    pr.print(CarrierTypeNomenclature, "CarrierTypeNomenclature");
    pr.print(CarrierItemNomenclature, "CarrierItemNomenclature");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = -5233450255875932645L;

}
