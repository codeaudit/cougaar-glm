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

public class UIOrgItinerary 
  extends UIItinerary
  implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

  private boolean hasOrgActivityInfo;

  public UIOrgItinerary() {}

  /**
   * get and set access methods for <code>HasOrgActivityInfo</code>
   * <p>
   * Used primarily for PSP_TransportTAA
   */
  public boolean getHasOrgActivityInfo() {return hasOrgActivityInfo;}
  public void setHasOrgActivityInfo(boolean hasOrgActivityInfo) {
    this.hasOrgActivityInfo = hasOrgActivityInfo;
  }

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(hasOrgActivityInfo, "HasOrgActivityInfo");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 1507876773245125580L;

}
