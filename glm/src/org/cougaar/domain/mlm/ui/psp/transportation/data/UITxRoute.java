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

import java.util.Vector;

public class UITxRoute implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

  private String sourceNodeID;
  private String destinationNodeID;
  private Vector linkIDs;

  public UITxRoute() {}

  /**
   * get and set access methods for <code>SourceNodeID</code>
   * <p>
   * "NodeID" of the route Source Node
   */
  public String getSourceNodeID() {return sourceNodeID;}
  public void setSourceNodeID(String sourceNodeID) {
    this.sourceNodeID = sourceNodeID;
  }

  /**
   * get and set access methods for <code>DestinationNodeID</code>
   * <p>
   * "NodeID" of the route Destination Node
   */
  public String getDestinationNodeID() {return destinationNodeID;}
  public void setDestinationNodeID(String destinationNodeID) {
    this.destinationNodeID = destinationNodeID;
  }

  /**
   * get and set access methods for <code>LinkIDs</code>
   * <p>
   * Vector of Link "LinkID" Strings for the route
   */
  public Vector getLinkIDs() {return linkIDs;}
  public void setLinkIDs(Vector linkIDs) {this.linkIDs=linkIDs;}

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    pr.print(sourceNodeID, "SourceNodeID");
    pr.print(destinationNodeID, "DestinationNodeID");
    pr.print(linkIDs, "LinkIDs");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = -2068987440573904576L;

}
