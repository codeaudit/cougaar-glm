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

import org.cougaar.domain.glm.ldm.asset.TransportationRoute;

/**
 * <code>UITaskItineraryElementCarrier</code>. 
 * <p>
 * ItineraryElement for tasks allocated to physical assets ("leaf" 
 * allocations to ships/trucks/etc).
 */

public class UITaskItineraryElementCarrier 
    extends UITaskItineraryElement
    implements org.cougaar.util.SelfPrinter, java.io.Serializable, java.lang.Cloneable
{

  public UITaskItineraryElementCarrier() { super(); }

  public UITaskItineraryElement copy() {
    try {
      return (UITaskItineraryElement)super.clone();
    } catch (java.lang.CloneNotSupportedException noClone) {
      throw new RuntimeException("NEVER! "+noClone);
    }
  }

  //#############################################################
  // START ATTRIBUTES
  // ATTRIBUTES ARE PUBLIC FOR BACKWARD COMPATIBILITY -- SHOULD
  // USE ACCESSORS (SEE NEXT SECTION)
  //

  /** Carrier fields -- identify a particular physical asset with a leg **/
  public String CarrierUID;
  public String CarrierTypeNomenclature;
  public String CarrierItemNomenclature;

  // END ATTRIBUTES
  //############################################################

  //#############################################################
  // START ACCESSORS

  public String getCarrierUID() { return CarrierUID; }
  public void setCarrierUID(String s) {
    CarrierUID = s; 
  }
  public String getCarrierTypeNomenclature() { return CarrierTypeNomenclature; }
  public void setCarrierTypeNomenclature(String s) {
    CarrierTypeNomenclature = s;
  }
  public String getCarrierItemNomenclature() { return CarrierItemNomenclature; }
  public void setCarrierItemNomenclature(String s) {
    CarrierItemNomenclature = s;
  }

  //END ACCESSORS
  //############################################################

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
  static final long serialVersionUID = 7159184020616783440L;

}


