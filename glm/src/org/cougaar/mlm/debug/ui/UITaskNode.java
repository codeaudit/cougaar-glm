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

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;

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
    if (o instanceof MessageAddress) 
      return ((MessageAddress)o).getAddress();
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
    //if (PF instanceof org.cougaar.planning.ldm.plan.SimplePenaltyFunction) {
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

