/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */


package org.cougaar.mlm.debug.ui;

import java.util.Enumeration;

import org.cougaar.planning.ldm.asset.Asset;

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
