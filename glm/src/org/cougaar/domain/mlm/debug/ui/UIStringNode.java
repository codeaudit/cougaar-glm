/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */


package org.cougaar.domain.mlm.debug.ui;

/** A tree node for a String.
  Overrides the UITreeNode loadChildren, toString and isLeaf.
  */

public class UIStringNode extends UITreeNode {
  String s;

  /** Create a tree node for the string.
    @param String s for which to create tree node
   */
  public UIStringNode(String s) {
    super(s);
    this.s = s;
  }

  /** The String is a leaf.
    @return true
   */
  public boolean isLeaf() {
    return true;
  }

  /** This shouldn't be called because this is a leaf.
   */

  public void loadChildren() {
    System.out.println("Warning: UIStringNode:loadChildren called, but String is a leaf.");
  }

  /** Return representation of a String in a tree.
    @return String s
   */

  public String toString() {
    return s;
  }
}


