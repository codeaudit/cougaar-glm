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
