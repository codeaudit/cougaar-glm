/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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


package org.cougaar.mlm.debug.ui;

/** A tree node for a String.
  Overrides the UITreeNode loadChildren, toString and isLeaf.
  */

public class UIStringNode extends UITreeNode {
  String s;

  /** Create a tree node for the string.
    @param s String for which to create tree node
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


