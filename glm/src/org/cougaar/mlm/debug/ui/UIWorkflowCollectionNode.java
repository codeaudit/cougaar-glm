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
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.util.UnaryPredicate;

/** A tree for an allocatable workflow collection.
  */

public class UIWorkflowCollectionNode extends UITreeNode implements UISubscriber {
  private UIPlugin uiPlugin;
  private String planName;
  private boolean childrenLoaded = false;

  /** Creates a tree for a workflow collection in the specified cluster
    and for the specified plan by calling the UITreeNode constructor.
    @param uiPlugin this user interface plug in
    @param planName name of plan for which to display workflows
    @param clusterId cluster from which to obtain workflows
    @exception UINoPlanException thrown when the plan does not exist
    */

  public UIWorkflowCollectionNode(UIPlugin uiPlugin, String planName,
		  MessageAddress clusterId) throws UINoPlanException {
    super();
    this.uiPlugin = uiPlugin;
    this.planName = planName;
    super.setUserObject(uiPlugin.getPlan(planName));
  }

  /** Has leaves which are the Workflows.
    @return false
   */

  public boolean isLeaf() {
    return false;
  }

  /** Get the Workflows.
   */

  private static UnaryPredicate workflowPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	//System.out.println("Predicate called with: " + o.toString());
	return (o instanceof Expansion);
      }
    };
  }

  public synchronized void loadChildren() {
    if (!childrenLoaded) {
      uiPlugin.subscribe(this, workflowPredicate());
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
    Actually returns dispositions which are expansions.
    Only pay attention to the added list.
    */

  public synchronized void subscriptionChanged(IncrementalSubscription container) {
    //System.out.println("Container changed");
    boolean reload = false;
    Enumeration added = container.getAddedList();
    while (added.hasMoreElements()) {
      Expansion expansion = (Expansion)added.nextElement();
      UIWorkflowNode node = new UIWorkflowNode(expansion.getWorkflow());
      if (node != null) {
	//System.out.println("Added: " + workflow.toString() + " at " +
	//		   getChildCount());
	treeModel.insertNodeInto(node, this, getChildCount());
	reload = true;
      }
    }
    if (reload) treeModel.reload();
  }

}

