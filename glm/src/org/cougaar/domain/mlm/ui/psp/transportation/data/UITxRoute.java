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
