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

import java.util.Date;
import java.util.Enumeration;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.asset.Asset;

/** A tree node for a Task.
  Overrides the UITreeNode loadChildren, toString and isLeaf
  methods to dynamically display the Task.
  */

public class UITaskNode extends UITreeNode {
  Task task;

  /** Create a tree node for the task.
    @param task task for which to create tree node
   */
  public UITaskNode(Task task) {
    super(task);
    this.task = task;
  }

  /** The task is not a leaf.
    @return false
   */
  public boolean isLeaf() {
    return false;
  }

  /** Get description of source and destination clusters.
   */

  private static String getClusterIdDescription(Object o) {
    String s = "";
    if (o == null)
      return s;
    if (o instanceof ClusterIdentifier) 
      return ((ClusterIdentifier)o).getAddress();
    return o.toString();
  }

  /** The task's children are string representations of:
    source->destination
    verb
    direct object
    prepositional phrases (preposition, indirect object)
    milstrip number (if prepositional phrase references a requisition)
    */

  public void loadChildren() {
    int i = 0; // child position
    String sourceString = getClusterIdDescription(task.getSource());
    String dstnString = getClusterIdDescription(task.getDestination());
    String description = sourceString + "->" + dstnString;
    insert(new UIStringNode(description), i++);

    Verb verb = task.getVerb();
    String verbString = "";
    if (verb != null)
      verbString = verb.toString();
    insert(new UIStringNode(verbString), i++);

    String directObjectString = 
      UIAsset.getDescription((Asset)task.getDirectObject());
    insert(new UIStringNode(directObjectString), i++);

    //PenaltyFunction PF = task.getPenaltyFunction();
    //if (PF instanceof org.cougaar.domain.planning.ldm.plan.SimplePenaltyFunction) {
    //    Date earliestDate = PF.getDesiredScheduleEarliestDate();
    //    insert(new UIStringNode(UITask.getDateString(earliestDate)), i++);
    //    Date latestDate = PF.getDesiredScheduleLatestDate();
    //    insert(new UIStringNode(UITask.getDateString(latestDate)), i++);
    //    Date bestDate = PF.getDesiredScheduleBestDate();
    //    insert(new UIStringNode(UITask.getDateString(bestDate)), i++);
    //}

    Enumeration prepPhrases = task.getPrepositionalPhrases();
    if (prepPhrases != null) {
      String prepPhraseString = "";
      PrepositionalPhrase p;
      while (prepPhrases.hasMoreElements()) {
	p = (PrepositionalPhrase)prepPhrases.nextElement();
	if (p != null) {
	  Object indirectObject = p.getIndirectObject();
	  if (indirectObject instanceof Asset ) {
	    prepPhraseString = p.getPreposition() + " " +
	      UIAsset.getDescription((Asset)indirectObject);
	    prepPhraseString.trim();
	    insert(new UIStringNode(prepPhraseString), i++);
          /*
	  } else if (indirectObject instanceof Requisition) {
	    //	    prepPhraseString = p.getPreposition() + " " +
	    //	      UIAsset.getDescription((Requisition)indirectObject);
	    //	    prepPhraseString.trim();
	    //	    insert(new UIStringNode(prepPhraseString), i++);
	    //	    String milstrip = "     " +
	    //	      ((Requisition)indirectObject).getRawMILSTRIP();
	    //	    insert(new UIStringNode(milstrip), i++);
            insert(new UIRequisitionNode((Requisition)indirectObject),i++);
	  } else if (indirectObject instanceof SupplySource) {
	    //	    prepPhraseString = p.getPreposition() + " " +
	    //	      UIAsset.getDescription((SupplySource)indirectObject);
	    //	    prepPhraseString.trim();
	    //	    insert(new UIStringNode(prepPhraseString), i++);
	    //	    insert(new UISupplySourceNode((SupplySource)indirectObject), i++);
	    if (indirectObject instanceof DepotSupplySource) {
	      insert(new UIDepotSupplySourceNode((DepotSupplySource)indirectObject), i++);
	    } else if (indirectObject instanceof DRMSSupplySource) {
	      insert(new UIDRMSSupplySourceNode((DRMSSupplySource)indirectObject), i++);
	    } else if (indirectObject instanceof VendorSupplySource) {
	      insert(new UIVendorSupplySourceNode((VendorSupplySource)indirectObject), i++);
	    }
          */
	  } else if (indirectObject != null) {
	    // indirectObject not Asset, Requisition, or SupplySource
	    prepPhraseString = p.getPreposition() + " " +
	      indirectObject.toString();
	    prepPhraseString.trim();
	    insert(new UIStringNode(prepPhraseString), i++);
	  }
	}
      }
    }
  }

  /** Return representation of a task in a tree, just the word "Task"
    as the task contents are returned as the children.
    @return Task
   */

  public String toString() {
    Verb verb = task.getVerb();
    if (verb != null)
      return verb.toString();
    else
      return "Task";
  }
}

