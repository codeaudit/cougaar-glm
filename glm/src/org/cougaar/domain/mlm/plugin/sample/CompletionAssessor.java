/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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
package org.cougaar.domain.mlm.plugin.sample;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.util.StateModelException;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.cluster.PlanElementSet;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.core.plugin.PlugInAdapter;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.util.UnaryPredicate;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.SortedMap;
import javax.swing.*;
import javax.swing.table.*;
import org.cougaar.domain.planning.ldm.plan.PlanElementImpl;
import org.cougaar.domain.mlm.plugin.UICoordinator;

/**
 * @author       ALPINE <alpine-software@bbn.com>
 * @version      $Id: CompletionAssessor.java,v 1.4 2001-08-22 20:27:41 mthome Exp $
 */

public class CompletionAssessor extends SimplePlugIn
{
  private static final boolean BRIEF = false; // True for task id only

  public static final int KIND_UNDISPOSED = 0; // No plan element
  public static final int KIND_UNKNOWN    = 1; // No allocation result
  public static final int KIND_INCOMPLETE = 2; // Confidence < 100%
  public static final int KIND_FAILURE    = 3; // Allocation failure
  public static final int KIND_SUCCESS    = 4; // Allocation success
  public static final int NKINDS = 5;

  private static class MyPrivateState implements java.io.Serializable {
    boolean undisposedEnabled = true;
    boolean unknownEnabled = true;
    boolean incompleteEnabled = true;
    boolean failureEnabled = true;
    boolean successEnabled = false;
    boolean estimatedEnabled = true;
    boolean onDemandEnabled = true;
  }

  /** frame displaying messages **/
  private JFrame frame;

  private static DateFormat logTimeFormat =
    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

  static NumberFormat confidenceFormat = NumberFormat.getPercentInstance();

  private static DecimalFormat labelCountFormat = new DecimalFormat("####0");

  private static String formatLabel(int count, String kind) {
    try {
      return (labelCountFormat.
              format(count, new StringBuffer(),
                     new FieldPosition(NumberFormat.INTEGER_FIELD)).
              append(" ").
              append(kind).
              append(" tasks").
              substring(0));
    }
    catch (Exception e) {
      e.printStackTrace();
      return "bogus";
    }
  }

  static class TaskComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      if (!(o1 instanceof Task) ||
          !(o2 instanceof Task)) {
        throw new IllegalArgumentException("Not a Task");
      }
      Task task1 = (Task) o1;
      Task task2 = (Task) o2;
      return task1.getUID().toString().compareTo(task2.getUID().toString());
    }
  }

  static TaskComparator taskComparator = new TaskComparator();

  class MyModel extends AbstractTableModel {
    private TreeMap elements = new TreeMap(taskComparator);
    private Task[] elementArray = null;
    private boolean[] includedKinds = new boolean[NKINDS]; // The kinds currently included in elementArray

    public synchronized boolean contains(Task task) {
      return elements.containsKey(task);
    }

    public int getKind(Task task) {
      Wrapper wrapper = (Wrapper) elements.get(task);
      if (wrapper == null) return KIND_UNDISPOSED;
      return wrapper.getKind();
    }

    public synchronized void add(Task task) {
      if (!contains(task)) {
        Wrapper wrapper = new Wrapper(task);
        elements.put(task, wrapper);
        int previousRow = -1;
        SortedMap headMap = elements.headMap(task);
        if (headMap.size() > 0) {
          Task previousTask = (Task) headMap.lastKey();
          Wrapper previousWrapper = (Wrapper) headMap.get(previousTask);
          previousRow = previousWrapper.getRow();
        }
        if (includedKinds[wrapper.getKind()]) {
          elementArray = null;
          int row = previousRow + 1;
          fireTableRowsInserted(row, row);
        } else {
          wrapper.setRow(previousRow);
        }
      }
    }
    public synchronized void remove(Task task) {
      if (contains(task)) {
        Wrapper wrapper = (Wrapper) elements.remove(task);
        if (includedKinds[wrapper.getKind()]) {
          elementArray = null;
          int row = wrapper.getRow();
          fireTableRowsDeleted(row, row);
        }
      }
    }

    public synchronized void change(Task task) {
      if (contains(task)) {
        Wrapper wrapper = (Wrapper) elements.get(task);
        boolean wasIncluded = includedKinds[wrapper.getKind()];
        change(wrapper);
        boolean isIncluded = includedKinds[wrapper.getKind()];
        if (isIncluded) {
          if (wasIncluded) {
            fireTableRowsUpdated(wrapper.getRow(), wrapper.getRow());
          } else {
            elementArray = null;
            int row = wrapper.getRow() + 1;
            fireTableRowsInserted(row, row);
          }
        } else {
          if (wasIncluded) {
            elementArray = null;
            int row = wrapper.getRow();
            fireTableRowsDeleted(row, row);
          } else {
            // No visibility change
          }
        }
      }
    }

    private void change(Wrapper wrapper) {
      wrapper.updateKind();
    }

    public synchronized void removeAll() {
      elements.clear();
      elementArray = null;
      fireTableDataChanged();
    }

    public synchronized void changeAll() {
      for (Iterator iterator = elements.values().iterator(); iterator.hasNext(); ) {
        change((Wrapper) iterator.next());
      }
      elementArray = null;
      fireTableDataChanged();
    }

    private int getKindCount(int kind) {
      int count = 0;
      for (Iterator iter = elements.values().iterator(); iter.hasNext(); ) {
        Wrapper wrapper = (Wrapper) iter.next();
        if (wrapper.getKind() == kind) {
          count++;
        }
      }
      return count;
    }

    public int getUndisposedCount() {
      return getKindCount(KIND_UNDISPOSED);
    }

    public int getUnknownCount() {
      return getKindCount(KIND_UNKNOWN);
    }

    public int getIncompleteCount() {
      return getKindCount(KIND_INCOMPLETE);
    }

    public int getFailureCount() {
      return getKindCount(KIND_FAILURE);
    }

    public int getSuccessCount() {
      return getKindCount(KIND_SUCCESS);
    }

    public synchronized int getRowCount() {
      insureElementArray();
      return elementArray.length;
    }

    public int getColumnCount() {
      return 4;
    }

    public Class getColumnClass(int column) {
      switch (column) {
      case 0: return Boolean.class;
      case 1: return String.class;
      case 2: return Task.class;
      case 3: return PlanElement.class;
      default: return String.class;
      }
    }

    public String getColumnName(int column) {
      switch (column) {
      case 0: return "Ok";
      case 1: return "Conf";
      case 2: return "Task";
      case 3: return "Disposition";
      }
      return null;
    }

    private void insureElementArray() {
      if (elementArray == null) {
        includedKinds[KIND_UNDISPOSED] = myPrivateState.undisposedEnabled;
        includedKinds[KIND_UNKNOWN]    = myPrivateState.unknownEnabled;
        includedKinds[KIND_INCOMPLETE] = myPrivateState.incompleteEnabled;
        includedKinds[KIND_FAILURE]    = myPrivateState.failureEnabled;
        includedKinds[KIND_SUCCESS]    = myPrivateState.successEnabled;
        int count = 0;
        for (Iterator iter = elements.values().iterator(); iter.hasNext(); ) {
          Wrapper wrapper = (Wrapper) iter.next();
          wrapper.setRow(count);
          if (includedKinds[wrapper.getKind()]) {
            count++;
          }
        }
        elementArray = new Task[count];
        count = 0;
        for (Iterator iter = elements.values().iterator(); iter.hasNext(); ) {
          Wrapper wrapper = (Wrapper) iter.next();
          if (includedKinds[wrapper.getKind()]) {
            elementArray[count++] = wrapper.getTask();
          }
        }
      }
    }

    public synchronized Task getTask(int row) {
      insureElementArray();
      if (row >= elementArray.length) return null;
      return elementArray[row];
    }

    public synchronized Object getValueAt(int row, int column) {
      Task task = getTask(row);
      Object result = "<null>";
      PlanElement pe = getPlanElement(task);
      switch (column) {
      case 0:
        if (pe == null) {
          result = Boolean.FALSE;
        } else {
          AllocationResult ar = getAllocationResult(pe);
          if (ar == null) {
            result = Boolean.TRUE;
          } else {
            result = new Boolean(ar.isSuccess());
          }
        }
        break;

      case 1:
        if (pe == null) {
          result = "??";
        } else {
          AllocationResult ar = getAllocationResult(pe);
          if (ar == null) {
            result = "??";
          } else {
            result = confidenceFormat.format(ar.getConfidenceRating());
          }
        }
        break;

      case 2:
        result = task;
        break;

      case 3:
        if (pe == null) {
          result = "Not Disposed";
        } else {
          result = pe;
        }
        break;
      }
      return result;
    }
  }

  private AllocationResult getAllocationResult(PlanElement pe) {
    if (showEstimated.isSelected()) {
      return pe.getEstimatedResult();
    } else {
      return pe.getReportedResult();
    }
  }

  private class Wrapper {
    private int kind;
    private Task task;
    private int row = -1;

    Wrapper(Task task) {
      this.task = task;
      updateKind();
    }

    private void updateKind() {
      PlanElement pe = getPlanElement(task);
      if (pe == null) {
        kind = KIND_UNDISPOSED;
      } else {
        AllocationResult ar = getAllocationResult(pe);
        if (ar == null) {
          kind = KIND_UNKNOWN;
        } else if (ar.getConfidenceRating() < 1.0) {
          kind = KIND_INCOMPLETE;
        } else if (ar.isSuccess()) {
          kind = KIND_SUCCESS;
        } else {
          kind = KIND_FAILURE;
        }
      }
    }

    public int getKind() {
      return kind;
    }

    public Task getTask() {
      return task;
    }

    public int getRow() {
      return row;
    }

    public void setRow(int newRow) {
      row = newRow;
    }
  }

  /** Display Allocation Results  **/
  static class WhiteBlankIcon implements Icon {
    public int getIconHeight() {
      return 13;
    }
    public int getIconWidth() {
      return 13;
    }
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(Color.black);
      g.drawRect(x, y, 11, 11);
      g.setColor(Color.white);
      g.drawRect(x + 1, y + 1, 11, 11);
    }
  };

  static class WhiteCheckIcon extends WhiteBlankIcon implements Icon {
    public void paintIcon(Component c, Graphics g, int x, int y) {
      super.paintIcon(c, g, x, y);
      g.setColor(Color.white);
      g.drawLine(x + 3, y + 5, x + 3, y + 9);
      g.drawLine(x + 4, y + 5, x + 4, y + 9);
      g.drawLine(x + 5, y + 7, x + 9, y + 3);
      g.drawLine(x + 5, y + 8, x + 9, y + 4);
    }
  };

  Icon whiteCheckIcon = new WhiteCheckIcon();
  Icon whiteBlankIcon = new WhiteBlankIcon();
  JRadioButton showEstimated = new JRadioButton("Show Estimated Result");
  JRadioButton showReported = new JRadioButton("Show Reported Result");
  ButtonGroup showGroup = new ButtonGroup();
  JRadioButton showOnDemand = new JRadioButton("Show On Demand");
  JRadioButton showContinuously = new JRadioButton("Show Continuously");
  ButtonGroup demandGroup = new ButtonGroup();
  JButton updateButton = new JButton("Update");
  JCheckBox undisposedCountLabel =
    new JCheckBox(formatLabel(0, "undisposed"), whiteBlankIcon, true);
  JCheckBox unknownCountLabel =
    new JCheckBox(formatLabel(0, "unknown"), whiteBlankIcon, true);
  JCheckBox incompleteCountLabel =
    new JCheckBox(formatLabel(0, "incomplete"), null, true);
  JCheckBox failureCountLabel =
    new JCheckBox(formatLabel(0, "failed"), whiteBlankIcon, true);
  JCheckBox successCountLabel =
    new JCheckBox(formatLabel(0, "successful"), whiteBlankIcon, false);
  MyModel model;
  JTable table;
  JScrollPane pane;

  static JTable createTable(MyModel model) {
    try {
      return new JTable(model);
    }
    catch (RuntimeException e) {
      e.printStackTrace();
    }
    return null;
  }
  /** Subscription to hold collection of input tasks **/
  private PlanElementSet planElementSet = new PlanElementSet();
  private IncrementalSubscription myPlanElements;

  /** Look for dispositions **/
  private static UnaryPredicate planElementP = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof PlanElement);
    }
  };

  /** Look for tasks **/
  private IncrementalSubscription myTasks;
  private static UnaryPredicate taskP = new UnaryPredicate() {
    public boolean execute(Object o) {
      return o instanceof Task;
    }
  };

  /** Look for private state **/
  private MyPrivateState myPrivateState;
  private IncrementalSubscription myState;
  private static UnaryPredicate stateP = new UnaryPredicate() {
    public boolean execute(Object o) {
      return o instanceof MyPrivateState;
    }
  };

  private void fixedWidth(TableColumnModel columnModel, int column, int width) {
    columnModel.getColumn(column).setPreferredWidth(width);
    columnModel.getColumn(column).setMaxWidth(width);
    columnModel.getColumn(column).setMinWidth(width);
  }

  private Color getForeground(int kind) {
    switch (kind) {
    default:
    case KIND_UNDISPOSED: return Color.white;
    case KIND_UNKNOWN: return Color.white;
    case KIND_INCOMPLETE: return Color.black;
    case KIND_FAILURE: return Color.white;
    case KIND_SUCCESS: return Color.white;
    }
  }

  private Color getBackground(int kind) {
    switch (kind) {
    case KIND_UNDISPOSED: return new Color(150, 0, 150);
    case KIND_UNKNOWN: return new Color(0, 0, 200);
    case KIND_INCOMPLETE: return Color.yellow;
    case KIND_FAILURE: return new Color(200, 0, 0);
    case KIND_SUCCESS: return new Color(0, 150, 0);
    default: return Color.white;
    }
  }
 
 /** GUI to display failed disposition info **/
  private void createGUI() {
    model = new MyModel();
    table = createTable(model);
    pane = new JScrollPane(table,
                           JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                           JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    TableColumnModel columnModel = table.getColumnModel();
    fixedWidth(columnModel, 0,  20);
    fixedWidth(columnModel, 1,  40);
    table.setAutoResizeMode(table.AUTO_RESIZE_LAST_COLUMN);
    TableCellRenderer renderer = new TableCellRenderer() {
      private JLabel taskLabel = null;
      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column)
      {
        Task task = model.getTask(row);
        if (taskLabel == null) {
          taskLabel = new JLabel();
          taskLabel.setFont(table.getFont());
          taskLabel.setOpaque(true);
          taskLabel.setForeground(Color.black);
          taskLabel.setBackground(Color.white);
        }
        int kind = model.getKind(task);
        taskLabel.setForeground(getForeground(kind));
        taskLabel.setBackground(getBackground(kind));
        if (value != null) {
          taskLabel.setText(value.toString());
          taskLabel.setToolTipText(value.toString());
        } else {
          taskLabel.setText("<null>");
          taskLabel.setToolTipText("");
        }
        return taskLabel;
      }
    };
    table.setDefaultRenderer(Task.class, renderer);
    table.setDefaultRenderer(PlanElement.class, renderer);
    table.setDefaultRenderer(String.class, renderer);
    frame = new JFrame(getClusterIdentifier() + " Completion Assessor");
    Container panel = frame.getContentPane();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    Insets buttonInsets = new Insets(0, 5, 0, 5);
    Insets paneInsets = new Insets(0, 0, 0, 0);
    gbc.insets = buttonInsets;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    undisposedCountLabel.setSelectedIcon(whiteCheckIcon);
    undisposedCountLabel.setBackground(getBackground(KIND_UNDISPOSED));
    undisposedCountLabel.setForeground(getForeground(KIND_UNDISPOSED));
    undisposedCountLabel.setToolTipText("Tasks which have not been disposed");
    panel.add(undisposedCountLabel, gbc);
    gbc.gridy = 1;
    unknownCountLabel.setSelectedIcon(whiteCheckIcon);
    unknownCountLabel.setBackground(getBackground(KIND_UNKNOWN));
    unknownCountLabel.setForeground(getForeground(KIND_UNKNOWN));
    unknownCountLabel.setToolTipText("Tasks with no allocation result");
    panel.add(unknownCountLabel, gbc);
    gbc.gridy = 2;
    incompleteCountLabel.setBackground(getBackground(KIND_INCOMPLETE));
    incompleteCountLabel.setForeground(getForeground(KIND_INCOMPLETE));
    incompleteCountLabel.setToolTipText("Tasks with confidence ratind < 100%");
    panel.add(incompleteCountLabel, gbc);
    gbc.gridy = 3;
    failureCountLabel.setSelectedIcon(whiteCheckIcon);
    failureCountLabel.setBackground(getBackground(KIND_FAILURE));
    failureCountLabel.setForeground(getForeground(KIND_FAILURE));
    failureCountLabel.setToolTipText("Tasks with failed disposition");
    panel.add(failureCountLabel, gbc);
    gbc.gridy = 4;
    successCountLabel.setSelectedIcon(whiteCheckIcon);
    successCountLabel.setBackground(getBackground(KIND_SUCCESS));
    successCountLabel.setForeground(getForeground(KIND_SUCCESS));
    successCountLabel.setToolTipText("Tasks with successful disposition");
    panel.add(successCountLabel, gbc);
    gbc.gridx = 1;
    gbc.gridy = 0;
    panel.add(showEstimated, gbc);
    gbc.gridy = 1;
    panel.add(showReported, gbc);
    showGroup.add(showEstimated);
    showGroup.add(showReported);
    gbc.gridy = 2;
    panel.add(showOnDemand, gbc);
    gbc.gridy = 3;
    panel.add(showContinuously, gbc);
    demandGroup.add(showContinuously);
    demandGroup.add(showOnDemand);
    gbc.gridy = 4;
    panel.add(updateButton, gbc);
    ActionListener updateAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        update();
      }
    };
    updateButton.addActionListener(updateAction);
    updateButton.setEnabled(false);

    undisposedCountLabel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setUndisposedEnabled(undisposedCountLabel.isSelected());
      }
    });
    unknownCountLabel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setUnknownEnabled(unknownCountLabel.isSelected());
      }
    });
    incompleteCountLabel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setIncompleteEnabled(incompleteCountLabel.isSelected());
      }
    });
    failureCountLabel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setFailureEnabled(failureCountLabel.isSelected());
      }
    });
    successCountLabel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setSuccessEnabled(successCountLabel.isSelected());
      }
    });
    showEstimated.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setEstimatedEnabled(showEstimated.isSelected());
      }
    });
    showReported.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setEstimatedEnabled(!showReported.isSelected());
      }
    });
    showOnDemand.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setOnDemandEnabled(showOnDemand.isSelected());
      }
    });
    showContinuously.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setOnDemandEnabled(!showContinuously.isSelected());
      }
    });
    undisposedCountLabel.setSelected(myPrivateState.undisposedEnabled);
    unknownCountLabel.setSelected(myPrivateState.unknownEnabled);
    incompleteCountLabel.setSelected(myPrivateState.incompleteEnabled);
    failureCountLabel.setSelected(myPrivateState.failureEnabled);
    successCountLabel.setSelected(myPrivateState.successEnabled);
    showEstimated.setSelected(myPrivateState.estimatedEnabled);
    showReported.setSelected(!myPrivateState.estimatedEnabled);
    showOnDemand.setSelected(myPrivateState.onDemandEnabled);
    showContinuously.setSelected(!myPrivateState.onDemandEnabled);

    gbc.insets = paneInsets;
    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridheight = 1;
    gbc.gridwidth = 4;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    panel.add(pane, gbc);
    frame.pack();
    UICoordinator.setBounds(frame);
    frame.setVisible(true);
  }

  public void unload() {
    super.unload();
    frame.dispose();
  }

  private void setUndisposedEnabled(boolean v) {
    if (v != myPrivateState.undisposedEnabled) {
      myPrivateState.undisposedEnabled = v;
      buttonsChanged();
    }
  }
  private void setUnknownEnabled(boolean v) {
    if (v != myPrivateState.unknownEnabled) {
      myPrivateState.unknownEnabled = v;
      buttonsChanged();
    }
  }
  private void setIncompleteEnabled(boolean v) {
    if (v != myPrivateState.incompleteEnabled) {
      myPrivateState.incompleteEnabled = v;
      buttonsChanged();
    }
  }
  private void setFailureEnabled(boolean v) {
    if (v != myPrivateState.failureEnabled) {
      myPrivateState.failureEnabled = v;
      buttonsChanged();
    }
  }
  private void setSuccessEnabled(boolean v) {
    if (v != myPrivateState.successEnabled) {
      myPrivateState.successEnabled = v;
      buttonsChanged();
    }
  }
  private void setEstimatedEnabled(boolean v) {
    if (v != myPrivateState.estimatedEnabled) {
      myPrivateState.estimatedEnabled = v;
      buttonsChanged();
    }
  }
  private void setOnDemandEnabled(boolean v) {
    if (v != myPrivateState.onDemandEnabled) {
      myPrivateState.onDemandEnabled = v;
      buttonsChanged();
    }
  }
  private void buttonsChanged() {
    changeAll();
    openTransaction();
    publishChange(myPrivateState);
    closeTransaction(false);
  }

  /**
   * Overrides the setupSubscriptions() in the SimplePlugIn.
   */
  protected void setupSubscriptions() {
    getSubscriber().setShouldBePersisted(false);
    myState = (IncrementalSubscription) subscribe(stateP);
    if (!didRehydrate()) {
      publishAdd(new MyPrivateState());
    }
  }

  private void setupMainSubscriptions() {
    myPlanElements = (IncrementalSubscription) subscribe(planElementP, planElementSet, true);
    myTasks        = (IncrementalSubscription) subscribe(taskP);
  }

  private void updateLabels() {
    undisposedCountLabel.setText(formatLabel(model.getUndisposedCount(), "undisposed"));
    unknownCountLabel.setText(formatLabel(model.getUnknownCount(), "unknown"));
    incompleteCountLabel.setText(formatLabel(model.getIncompleteCount(), "incomplete"));
    failureCountLabel.setText(formatLabel(model.getFailureCount(), "failed"));
    successCountLabel.setText(formatLabel(model.getSuccessCount(), "successful"));
  }

  private PlanElement getPlanElement(Task task) {
    return planElementSet.findPlanElement(task);
  }

  private void addToModel(final Task task) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        model.add(task);
        updateLabels();
      }
    });
  }

  private void changeInModel(final Task task) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (model.contains(task)) {
          model.change(task);
          updateLabels();
        }
      }
    });
  }

  private void removeFromModel(final Task task) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        model.remove(task);
        updateLabels();
      }
    });
  }

  private void removeAllFromModel() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        model.removeAll();
        updateLabels();
      }
    });
  }

  private void changeInModel(PlanElement pe) {
    Task task = pe.getTask();
    if (task != null) {
      changeInModel(task);
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          model.changeAll();
          updateLabels();
        }
      });
    }
  }

  private void changeAll() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        model.changeAll();
        updateLabels();
      }
    });
  }

  private void addTasks(Enumeration tasks) {
    while (tasks.hasMoreElements()) {
      addToModel((Task) tasks.nextElement());
    }
  }

  private void changeTasks(Enumeration tasks) {
    while (tasks.hasMoreElements()) {
      changeInModel((Task) tasks.nextElement());
    }
  }

  private void removeTasks(Enumeration tasks) {
    while (tasks.hasMoreElements()) {
      removeFromModel((Task) tasks.nextElement());
    }
  }

  private void changeDispositions(Enumeration dispositions) {
    while (dispositions.hasMoreElements()) {
      changeInModel((PlanElement) dispositions.nextElement());
    }
  }

  /* CCV2 execute method */
  /* This will be called every time a alloc matches the above predicate */
  /* Note: Failed Dispositions only come through on the changed list. 
     Since Dispositions are changed by other PlugIns after we see them
     here, we need to keep track of the ones we've seen so we don't 
     act on them more than once.
   */
  public synchronized void execute() {
    if (myPrivateState == null) {
      if (myState.hasChanged()) {
        checkMyState(myState.getAddedList());
      }
    }
    if (myPrivateState != null) {
      if (!myPrivateState.onDemandEnabled) {
        addTasks(myTasks.getAddedList());
        changeTasks(myTasks.getChangedList());
        removeTasks(myTasks.getRemovedList());
        changeDispositions(myPlanElements.getAddedList());
        changeDispositions(myPlanElements.getChangedList());
        changeDispositions(myPlanElements.getRemovedList());
      } else if (myTasks.hasChanged() || myPlanElements.hasChanged()) {
        updateButton.setEnabled(true);
      }
    }
  }

  private void checkMyState(Enumeration states) {
    while (states.hasMoreElements()) {
      myPrivateState = (MyPrivateState) states.nextElement();
      createGUI();
      setupMainSubscriptions();
      unsubscribe(myState);
      myState = null;
      update();
      break;
    }
  }

  private void update() {
    updateButton.setEnabled(false);
    removeAllFromModel();
    addTasks(myTasks.elements());
    changeDispositions(myPlanElements.elements());
  }
}
