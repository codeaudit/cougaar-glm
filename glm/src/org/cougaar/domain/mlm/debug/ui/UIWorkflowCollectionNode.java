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
import java.util.Enumeration;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Workflow;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;

/** A tree for an allocatable workflow collection.
  */

public class UIWorkflowCollectionNode extends UITreeNode implements UISubscriber {
  private UIPlugIn uiPlugIn;
  private String planName;
  private boolean childrenLoaded = false;

  /** Creates a tree for a workflow collection in the specified cluster
    and for the specified plan by calling the UITreeNode constructor.
    @param uiPlugIn this user interface plug in
    @param planName name of plan for which to display workflows
    @param clusterId cluster from which to obtain workflows
    @exception UINoPlanException thrown when the plan does not exist
    */

  public UIWorkflowCollectionNode(UIPlugIn uiPlugIn, String planName,
		  ClusterIdentifier clusterId) throws UINoPlanException {
    super();
    this.uiPlugIn = uiPlugIn;
    this.planName = planName;
    super.setUserObject(uiPlugIn.getPlan(planName));
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
      uiPlugIn.subscribe(this, workflowPredicate());
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

