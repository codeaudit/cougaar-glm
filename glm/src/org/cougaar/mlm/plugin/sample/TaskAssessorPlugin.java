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
package org.cougaar.mlm.plugin.sample;

import java.awt.BorderLayout;
import java.awt.Container;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AssetTransfer;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

/**
 * The TaskAssessorPlugin monitors the disposition of Tasks and
 * displays a summary.
 *
 * @author       ALPINE <alpine-software@bbn.com>
 */

public class TaskAssessorPlugin extends SimplePlugin
{
  /** frame displaying messages **/
  static JFrame frame;

  private Comparator taskComparator = new Comparator() {
    public int compare(Object l, Object r) {
      Task lt = (Task) l;
      Task rt = (Task) r;
      return lt.getUID().compareTo(rt.getUID());
    }
  };

  private class MyTreeSet extends TreeSet {
    public MyTreeSet(Comparator comparator) {
      super(comparator);
    }

    public synchronized boolean add(Object o) {
      taskArray = null;
      return super.add(o);
    }
    public synchronized boolean remove(Object o) {
      taskArray = null;
      return super.remove(o);
    }
  }

  private Task[] taskArray = null;

  private MyTreeSet tasks = new MyTreeSet(taskComparator);
  
  private Task[] getTaskArray() {
    if (taskArray == null) {
      taskArray = (Task[]) tasks.toArray(new Task[tasks.size()]);
    }
    return taskArray;
  }

  private NumberFormat confidenceFormat = NumberFormat.getPercentInstance();

  private class MyTableModel extends AbstractTableModel {
    public int getRowCount() {
      return tasks.size();
    }

    public int getColumnCount() {
      return 4;
    }

    public String getColumnName(int column) {
      switch (column) {
      case 0: return "Task ID";
      case 1: return "Disposition";
      case 2: return "Status";
      case 3: return "Confidence";
      }
      return null;
    }

    public Object getValueAt(int row, int column) {
      synchronized (tasks) {
	Task[] ary = getTaskArray();
	if (row >= ary.length) return null;
	Task task = ary[row];
	PlanElement pe = task.getPlanElement();
	AllocationResult ar = null;
	if (pe != null) {
	  ar = pe.getEstimatedResult();
	}
	switch (column) {
	case 0:			// The task id
	  return task.getUID();
	case 1:
	  if (pe == null)                  return "not disposed";
	  if (pe instanceof Allocation)    return "Allocation";
	  if (pe instanceof Aggregation)   return "Aggregation";
	  if (pe instanceof Expansion)     return "Expansion";
	  if (pe instanceof AssetTransfer) return "AssetTransfer";
	  return pe.getClass().getName();
	case 2:			// The failed status
	  if (ar == null) return "Unknown";
	  if (ar.isSuccess()) return "Succeeded";
	  return "Failed";
	case 3:			// The confidence rating
	  if (ar == null) return "Unknown";
	  return confidenceFormat.format(ar.getConfidenceRating());
	}
	return "???";
      }
    }
  }

  /** Display Task Results  **/
  MyTableModel tableModel = new MyTableModel();
  JTable taskList = new JTable(tableModel);
  JLabel allocCountLabel = new JLabel("   0 failed tasks");
  JScrollPane scrollPane = new JScrollPane(taskList,
					   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					   JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  
  /** Subscription to hold collection of input tasks **/
  private IncrementalSubscription myalloc;

  /** Look for tasks **/
  private static UnaryPredicate allocPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof Task || o instanceof PlanElement);
      }
    };
  }

  /** GUI to display failed task info **/
  private void createGUI() {
    frame = new JFrame("TaskAssessorPlugin for " + getMessageAddress());
    frame.setLocation(0,160);
    Container panel = frame.getContentPane();
    panel.add(allocCountLabel, BorderLayout.NORTH);
    panel.add(scrollPane, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Overrides the setupSubscriptions() in the SimplePlugin.
   */
  protected void setupSubscriptions() {
    getSubscriber().setShouldBePersisted(false);
    myalloc = (IncrementalSubscription)subscribe(allocPred());
    createGUI();
  }

  /** Need this for creating new instances of certain objects **/
  private void updateGUI() {
  }

  private void checkAdd(Enumeration enum) {
    while (enum.hasMoreElements()) {
      Object object = enum.nextElement();
      if (object instanceof Task) {
	Task task = (Task) object;
	tasks.add(task);
      }
      tableModel.fireTableDataChanged();
    }
  }

  private void checkChange(Enumeration enum) {
    checkAdd(enum);
  }

  private void checkRemove(Enumeration enum) {
    while (enum.hasMoreElements()) {
      Object object = enum.nextElement();
      if (object instanceof Task) {
	if (tasks.contains(object)) {
	  tasks.remove(object);
	}
      }
      tableModel.fireTableDataChanged();
    }
  }

  /** This will be called every time a task matches the above predicate
   * Note: Failed Tasks only come through on the changed list. 
   * Since Tasks are changed by other Plugins after we see them
   * here, we need to keep track of the ones we've seen so we don't 
   * act on them more than once.
   **/
  public void execute() {
    checkAdd(myalloc.getAddedList());
    checkChange(myalloc.getChangedList());
    checkRemove(myalloc.getRemovedList());
  }
}
