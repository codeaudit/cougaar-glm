/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.debug.ui;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.util.Enumeration;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/** Support a dynamically expanded and updated tree node.  
  This overrides methods defined in DefaultMutableTreeNode (swing class) 
  to provide dynamic loading of child nodes.  This also defines
  object listener methods to listen for changes to the data, and
  update the tree model which updates the display.
  This class is extended by numerous classes which display the various
  cluster objects (plans, plan elements, tasks, allocations, etc.)
  as nodes in a tree.
  */

public class UITreeNode extends DefaultMutableTreeNode {
  private boolean hasLoaded;
  DefaultTreeModel treeModel;
  protected final Object childGuard = new Object();

  /** Construct a new tree node.  Should subsequently call setUserObject
    to set the object associated with this tree node.
    */

  public UITreeNode() {
    hasLoaded = false;
  }

  /** Construct a new tree node for the specified object.
    @param userObject object for which to create tree node
   */

  public UITreeNode(Object userObject) {
    super(userObject);
    hasLoaded = false;
  }

  /** Set the object associated with this tree node; typically used
    in conjunction with the zero-arg constructor.
    @param userObject object to associate with tree node
    */

  public void setUserObject(Object userObject) {
    super.setUserObject(userObject);
  }

  /** Methods called by TreeModel; overidden here to provide
    dynamic expansion of the tree - only get node values
    when the user "opens" the parent node.
    */

  /** Override this in subclasses to return whether or not
    the user object is a leaf.
    @return false
    */

  public boolean isLeaf() {
    return false;
  }

  /** Called by TreeModel prior to displaying children.  
    Load children if they're not already loaded.
    @return number of children from DefaultMutableTreeNode.getChildCount
    */

  public int getChildCount() {
    synchronized (childGuard) {
      if (!hasLoaded) {
        hasLoaded = true;
        loadChildren();
      }
    }
    return super.getChildCount();
  }

  /** Called by TreeModel prior to displaying children.  
    Load children if they're not already loaded.
    @return true if allows children, from DefaultMutableTreeNode.getAllowsChildren 
    */

  public boolean getAllowsChildren() {
    synchronized (childGuard) {
      if (!hasLoaded) {
        hasLoaded = true;
        loadChildren();
      }
    }
    return super.getAllowsChildren();
  }

  /** Called when node has changed to invalidate its children.
    First, removeAllChildren, then set the hasLoaded flag to false,
    because otherwise removeAllChildren will first try to load the children!
   */

  public void invalidateChildren() {
    synchronized (childGuard) {
      removeAllChildren();
      hasLoaded = false;
    }
  }

  /** Override this in subclasses to load children.
   */

  public void loadChildren() {
  }

  /** Find the node for the user object in the children nodes of the tree.
   */

  public DefaultMutableTreeNode findUserObject(Object o) {
    if (children != null) {
      for (int i = 0; i < children.size(); i++) {
	DefaultMutableTreeNode node = 
	  (DefaultMutableTreeNode)children.elementAt(i);
	if (node.getUserObject().equals(o))
	  return node;
      }
    }
    return null;
  }

  /** Used by subclasses to remove objects from the tree,
    when the objects are removed from their containers.
    */

  public void removeObjectFromTree(Object o) {
    DefaultMutableTreeNode node = findUserObject(o);
    if (node != null)
      treeModel.removeNodeFromParent(node);
  }

  /** Called by UITreeDisplay to set the tree model
    associated with the tree that this node is in.
    The tree model is used to insert and delete nodes in the tree.
    @param treeModel the tree model associated with this tree
    */

  public void setTreeModel(DefaultTreeModel treeModel) {
    this.treeModel = treeModel;
  }

  /** Save the string representations of the objects in the tree.
    Called from UITreeDisplay when the user selects the "Save" menu item.
    */

  void saveTree(String pathname) {
    FileOutputStream fileOutStream = null;

    try {
      fileOutStream = new FileOutputStream(pathname);
    } catch (Exception e) {
      new UIDisplayError("Error writing file: " + pathname + " " + e);
    }
    PrintWriter printWriter = new PrintWriter(fileOutStream);
    Enumeration nodes = preorderEnumeration();
    while (nodes.hasMoreElements())
      printWriter.println(nodes.nextElement().toString());
    if (printWriter.checkError())
      new UIDisplayError("Error writing file: " + pathname);
    printWriter.close();
  }
}



