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

public class UITxLink implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

  private String linkID;
  private String sourceNodeID;
  private String destinationNodeID;
  private int speed;
  private int numberOfLanes;

  public UITxLink() {}

  /**
   * get and set access methods for <code>LinkID</code>
   * <p>
   * Unique identifier
   */
  public String getLinkID() {return linkID;}
  public void setLinkID(String linkID) {this.linkID=linkID;}

  /**
   * get and set access methods for <code>SourceNodeID</code>
   * <p>
   * ID of the start point
   */
  public String getSourceNodeID() {return sourceNodeID;}
  public void setSourceNodeID(String sourceNodeID) {
    this.sourceNodeID = sourceNodeID;
  }

  /**
   * get and set access methods for <code>DestinationNodeID</code>
   * <p>
   * ID of the end point
   */
  public String getDestinationNodeID() {return destinationNodeID;}
  public void setDestinationNodeID(String destinationNodeID) {
    this.destinationNodeID = destinationNodeID;
  }

  /**
   * get and set access methods for <code>Speed</code>
   * <p>
   * speed of travel over this link
   */
  public int getSpeed() {return speed;}
  public void setSpeed(int speed) {this.speed=speed;}

  /**
   * get and set access methods for <code>NumberOfLanes</code>
   * <p>
   * width of the road
   */
  public int getNumberOfLanes() {return numberOfLanes;}
  public void setNumberOfLanes(int numberOfLanes) {
    this.numberOfLanes = numberOfLanes;
  }

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    pr.print(linkID, "LinkID");
    pr.print(sourceNodeID, "SourceNodeID");
    pr.print(destinationNodeID, "DestinationNodeID");
    pr.print(speed, "Speed");
    pr.print(numberOfLanes, "NumberOfLanes");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 222623960884660202L;

}
