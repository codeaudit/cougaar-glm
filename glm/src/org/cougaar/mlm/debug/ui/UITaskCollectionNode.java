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

import java.util.Enumeration;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.UnaryPredicate;

/** A tree for an expandable task collection.
  */

public class UITaskCollectionNode extends UITreeNode implements UISubscriber {
  private UIPlugin uiPlugin;
  private String planName;
  private boolean childrenLoaded = false;

  /** Creates a tree for the expandable task collection in
    the specified cluster and for the specified plan by calling
    the UITreeNode constructor.
    Listens for changes to the expandable tasks in order to dynamically
    update the tree.
    @param uiPlugin this user interface plug in
    @param planName name of plan for which to display tasks
    @param clusterId cluster from which to obtain tasks
    @exception UINoPlanException thrown when the plan does not exist
    */

  public UITaskCollectionNode(UIPlugin uiPlugin, String planName,
		   MessageAddress clusterId) throws UINoPlanException {
    super();
    this.uiPlugin = uiPlugin;
    this.planName = planName;
    super.setUserObject(uiPlugin.getPlan(planName));
  }

  /** Has leaves which are the tasks.
    @return true
   */

  public boolean isLeaf() {
    return false;
  }

  /** Get this cluster's expandable tasks, which are all the input tasks,
    which are all the tasks in the Tasks container.
   */

  private static UnaryPredicate taskPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return ( o instanceof Task );
      }
    };
  }

  /** Get the expandable tasks.  
   */

  // will allready be synced on childGuard
  public void loadChildren() {
    if (!childrenLoaded) {
      System.out.println("Entered subscription for expandable tasks.");
      uiPlugin.subscribe(this, taskPredicate());
      childrenLoaded = true;
    }
  }

  /** Display the plan name in the tree.
    @return plan name
   */

  public String toString() {
    return planName;
  }

  /** UISubscriber interface.
    Notified when a task is added and update the tree.
    */

  // shouldn't be synchronized!
  public  void subscriptionChanged(IncrementalSubscription container) {
    boolean reload = false;
    Enumeration added = container.getAddedList();
    while (added.hasMoreElements()) {
      Task task = (Task)added.nextElement();
      UITaskNode node = new UITaskNode(task);
      if (node != null) {
	//	System.out.println("Added: " + task.toString() + " at " +
	//			   getChildCount());
	treeModel.insertNodeInto(node, this, getChildCount());
	reload = true;
      }
    }
    if (reload) treeModel.reload();
  }

}

