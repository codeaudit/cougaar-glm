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

import java.util.Collection;

public class UITxNetwork implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

  private Collection allGroundNodes;
  private Collection allGroundLinks;

  public UITxNetwork() {}

  /**
   * get and set access methods for <code>AllGroundNodes</code>
   * <p>
   * Collection of UITxNode
   */
  public Collection getAllGroundNodes() {return allGroundNodes;}
  public void setAllGroundNodes(Collection allGroundNodes) {
    this.allGroundNodes = allGroundNodes;
  }

  /**
   * get and set access methods for <code>AllGroundLinks</code>
   * <p>
   * Collection of UITxLink
   */
  public Collection getAllGroundLinks() {return allGroundLinks;}
  public void setAllGroundLinks(Collection allGroundLinks) {
    this.allGroundLinks = allGroundLinks;
  }

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    pr.print(allGroundNodes, "AllGroundNodes");
    pr.print(allGroundLinks, "AllGroundLinks");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = -7412110186901022782L;

}
