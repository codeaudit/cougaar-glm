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
