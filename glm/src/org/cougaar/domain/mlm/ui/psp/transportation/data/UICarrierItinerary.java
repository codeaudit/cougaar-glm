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

import org.cougaar.domain.glm.plan.GeolocLocation;

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

  public void printContent(org.cougaar.util.AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(CarrierUID, "CarrierUID");
    pr.print(CarrierTypeNomenclature, "CarrierTypeNomenclature");
    pr.print(CarrierItemNomenclature, "CarrierItemNomenclature");
  }

  public String toString() {
    return org.cougaar.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = -5233450255875932645L;

}
