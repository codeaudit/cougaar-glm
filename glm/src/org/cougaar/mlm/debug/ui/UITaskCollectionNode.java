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

