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

public class UITTMarker 
    extends UIString 
    implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

  private int type =UNEXPECTED;

  public static final int COLLECTION_END      =1;
  public static final int UNEXPECTED          =2;

  public UITTMarker(String s) { this(s, UNEXPECTED); }
  public UITTMarker(String s, int t) {STR = s; type = t;}

  public void setType(int t) {type = t;}

  public boolean isCollectionEnd() { return (type == COLLECTION_END); }
 
  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    super.printContent(pr);
    pr.print(type, "Type");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 3690767762453327790L;

}
