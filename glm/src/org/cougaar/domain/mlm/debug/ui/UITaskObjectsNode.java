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

import java.util.Enumeration;

import org.cougaar.domain.planning.ldm.asset.Asset;

/** A tree node for "task objects" associated with an allocation.
  Overrides the UITreeNode loadChildren, toString and isLeaf
  methods to dynamically display the objects which have no children.
  */

public class UITaskObjectsNode extends UITreeNode {
  Enumeration taskObjects;
  String description;

  /** Create a tree node for the task objects
    @param taskObjects the object for which to create a tree node
   */

  public UITaskObjectsNode(Enumeration taskObjects) {
    super(taskObjects);
    this.taskObjects = taskObjects;
    // compute string for this node here as it shouldn't change
    String s = "";
    while (taskObjects.hasMoreElements()) {
      Object taskObject = taskObjects.nextElement();
      if (taskObject != null) {
	if (taskObject instanceof Asset) {
	  s = s + " " + UIAsset.getDescription((Asset)taskObject);
	} else
	  s = s + " " + taskObject.toString();
      }
    }
    description = s.trim();
  }

  /** This is a leaf.
    @return true
   */
  public boolean isLeaf() {
    return true;
  }

  /** Prints an error message indicating this
    shouldn't be called because this is a leaf.
   */

  public void loadChildren() {
    System.out.println("UITaskObjectsNode:loadChildren called, but this is a leaf.");
  }

  /** Return the representation of the task objects in a tree.
    This is a list of asset names, and for physical objects, 
    the serial number, if specified.
    @return asset names followed by optional serial numbers
   */

  public String toString() {
    return description;
  }

}
