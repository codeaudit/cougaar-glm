/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation.data;

public class UIOrgItineraryElement 
  extends UIItineraryElement
  implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

  private String oplanActivity;
  private String oplanOptempo;

  public UIOrgItineraryElement() {}

  /**
   * get and set access methods for <code>OplanActivity</code>
   */
  public String getOplanActivity() {return oplanActivity;}
  public void setOplanActivity(String oplanActivity) {
    this.oplanActivity = oplanActivity;
  }

  /**
   * get and set access methods for <code>OplanOptempo</code>
   */
  public String getOplanOptempo() {return oplanOptempo;}
  public void setOplanOptempo(String oplanOptempo) {
    this.oplanOptempo = oplanOptempo;
  }

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(oplanActivity, "OplanActivity");
    pr.print(oplanOptempo, "OplanOptempo");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 4957707555215685613L;

}
