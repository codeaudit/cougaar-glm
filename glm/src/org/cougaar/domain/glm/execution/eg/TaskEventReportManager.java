/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.eg;

import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.core.society.UID;
import org.cougaar.util.OptionPane;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JComponent;
import javax.swing.table.TableModel;
import org.cougaar.domain.glm.execution.common.*;

/**
 * Keeps track of the TaskEventReports and decide if/when to
 * generate a report.
 **/
public class TaskEventReportManager extends ManagerBase implements ScheduleManager {
  public TaskEventReportManager(EventGenerator anEventGenerator) {
    super(anEventGenerator);
  }

  public String getGUITitle() {
    return "TaskEvent Reports";
  }

  public FilterGUI createFilterGUI(EventGenerator anEventGenerator) {
    return new TaskEventFilterGUI(anEventGenerator);
  }

  public Object[] getHandlers() {
    return new Object[] {
      new EGTaskEventReportHandler(this),
        };
  }

  protected Class getPlugInInterface() {
    return TaskEventPlugIn.class;
  }

  protected Class getDefaultPlugInClass() {
    return TaskEventDefaultPlugIn.class;
  }

  private void applyPlugIns(TimedTaskEventReport tter) {
    long theExecutionTime = theEventGenerator.getExecutionTime();
    for (Iterator i = getEnabledPlugIns(); i.hasNext(); ) {
      TaskEventPlugIn thePlugIn = (TaskEventPlugIn) i.next();
      if (thePlugIn.apply(tter, theExecutionTime)) return;
    }
  }

  public void receiveTaskEventReports(String source,
                                      TaskEventReport[] taskEventReports)
  {
    long executionTime = theEventGenerator.getExecutionTime();
    Collection taskUIDs = new HashSet();
    for (int i = 0; i < taskEventReports.length; i++) {
      TaskEventReport ter = taskEventReports[i];
      taskUIDs.add(ter.theTaskEventId.theTaskUID);
      Object key = TimedTaskEventReport.getKey(source, ter);
      TimedTaskEventReport tter = (TimedTaskEventReport) map.get(key);
      if (tter != null) {
        removeFromSchedule(tter);
      }
      if (!ter.isRescind()) {
        if (tter != null) {
          tter.theTaskEventReport = ter;
        } else {
          tter = new TimedTaskEventReport(source, ter, this);
        }
        applyPlugIns(tter);
        addToSchedule(tter);
      }
    }
    finishScheduleUpdate();
    try {
      sendTaskConstraintRequest(source, taskUIDs);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  protected void removeFromSchedule(Timed timed) {
    TimedTaskEventReport tter = (TimedTaskEventReport) timed;
    changeEventGroup(tter, null);
    super.removeFromSchedule(timed);
  }

  public void sendTaskEventReport(String source,
                                  TaskEventReport aTaskEventReport)
    throws IOException
  {
    Object[] handlers = {};     // There is no response
    EventMonitorListener l =
      theEventGenerator.createEventMonitorListener(source, handlers, aTaskEventReport);
    if (l != null) {
      theEventGenerator.addListener(l);
      theEventGenerator.startListener(l);
    }
  }

  /**
   * Process a new set of inter-event constraints. For each element,
   * find the task events named by the task event ids and their
   * current groups, if any. If the groups are both null, create a new
   * group and assign both task events to it. If one group is null and
   * the other isn't assign the task event that is not yet in a group
   * to the group of the other task event. If both groups are non-null
   * and identical, do nothing. If both groups are non-null and
   * different reassign all task events of one group to the other.
   **/
  public void receiveConstraintElements(String source, ConstraintElement[] constraints) {
    for (int i = 0; i < constraints.length; i++) {
      ConstraintElement e = constraints[i];
      TimedTaskEventReport thisTTER = (TimedTaskEventReport) map.get(e.thisEventId);
      if (thisTTER == null) continue; // Haven't received this yet
      TimedTaskEventReport thatTTER = (TimedTaskEventReport) map.get(e.thatEventId);
      if (thatTTER == null) continue; // Haven't received that yet
      if (thisTTER.theEventGroup == null) {
        if (thatTTER.theEventGroup == null) {
          // Neither event has a group yet; create a new group for both
          EventGroup newEventGroup = new EventGroup(thisTTER.theTaskEventReport.theTaskEventId);
          changeEventGroup(thisTTER, newEventGroup);
          changeEventGroup(thatTTER, newEventGroup);
//            System.out.println("New event group " + newEventGroup);
        } else {
          // thatEventGroup exists; add thisTTER to it
          changeEventGroup(thisTTER, thatTTER.theEventGroup);
//            System.out.println("Add to event group " + thatEventGroup);
        }
      } else {
        if (thatTTER.theEventGroup == null) {
          // thisEventGroup exists; add thatTTER to it
          changeEventGroup(thatTTER, thisTTER.theEventGroup);
//            System.out.println("Add to event group " + thisEventGroup);
        } else if (thisTTER.theEventGroup != thatTTER.theEventGroup) {
          // Both groups exist and are different; merge thatEventGroup with thisEventGroup
          for (Iterator iter = thatTTER.theEventGroup.iterator(); iter.hasNext(); ) {
            TimedTaskEventReport tter = (TimedTaskEventReport) iter.next();
            tter.theEventGroup = null; // Prevent trying to remove from the iterator base
            changeEventGroup(tter, thisTTER.theEventGroup);
          }
          System.out.println("Merge to event group " + thisTTER.theEventGroup);
        }
      }
    }
    finishScheduleUpdate();
  }

  private void changeEventGroup(TimedTaskEventReport tter, EventGroup newEventGroup) {
    super.removeFromSchedule(tter);   // We are changing the sort order,
                                // must not be in the sorted set while
                                // we do so
    if (tter.theEventGroup != null) {
      tter.theEventGroup.remove(tter);
      tter.theEventGroup = null;
    }
    if (newEventGroup != null) {
      tter.theEventGroup = newEventGroup;
      newEventGroup.add(tter);
    }
    super.addToSchedule(tter);
  }

  public void sendTaskConstraintRequest(String source, Collection taskUIDs)
    throws IOException
  {
    Object[] handlers = {new EGConstraintElementHandler(this)};
    TaskConstraintsRequest request =
      new TaskConstraintsRequest((UID[]) taskUIDs.toArray(new UID[taskUIDs.size()]));
    ConstraintElementListener l =
      theEventGenerator.createConstraintElementListener(source, handlers, request);
    if (l != null) {
      theEventGenerator.addListener(l);
      theEventGenerator.startListener(l);
    }
  }

  protected EGTableModel createTableModel() {
    return new MyTableModel();
  }

  private class MyTableModel extends ManagerTableModel {
    private static final int ENABLE_COLUMN        = 0;
    private static final int CLUSTER_COLUMN       = 1;
    private static final int RECEIVE_TIME_COLUMN  = 2;
    private static final int REPORT_TIME_COLUMN   = 3;
    private static final int GROUP_ID_COLUMN      = 4;
    private static final int TASK_UID_COLUMN      = 5;
    private static final int DESC_COLUMN          = 6;
    private static final int ASPECT_TYPE_COLUMN   = 7;
    private static final int ASPECT_VALUE_COLUMN  = 8;
    private static final int NCOLUMNS             = 9;
    private /*ns*/ final int ASPECT_TYPE_WIDTH = getCellWidth("TOTAL_SHIPMENTS");
    private /*ns*/ final int TASK_UID_WIDTH = CLUSTER_WIDTH + getCellWidth("/XXXXXXXXXXXXX");
    private /*ns*/ final int DESC_WIDTH = getCellWidth("XXXXXXXXXXXXXXXXXXXXXXXXX");

    private EventGroup markedEventGroup = null;

    public void cellSelectionChanged(int row, int col) {
      col = GROUP_ID_COLUMN;
      EventGroup newEventGroup = null;
      TimedTaskEventReport tter = (TimedTaskEventReport) getRowObject(row);
      if (tter != null) {
        newEventGroup = tter.theEventGroup;
      }
      if (newEventGroup != markedEventGroup) {
        markedEventGroup = newEventGroup;
        fireTableDataChanged();
      }
    }

    public boolean cellIsMarked(int row, int col) {
      if (true || col == GROUP_ID_COLUMN) {
        if (markedEventGroup != null) {
          TimedTaskEventReport tter = (TimedTaskEventReport) getRowObject(row);
          return markedEventGroup == tter.theEventGroup;
        }
      }
      return false;
    }

    public int getColumnCount() {
      return NCOLUMNS;
    }

    public int getPreferredColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN: return SEND_WIDTH;
      case CLUSTER_COLUMN: return CLUSTER_WIDTH;
      case RECEIVE_TIME_COLUMN: return DATE_WIDTH;
      case REPORT_TIME_COLUMN: return DATE_WIDTH;
      case GROUP_ID_COLUMN: return TASK_UID_WIDTH;
      case TASK_UID_COLUMN: return TASK_UID_WIDTH;
      case DESC_COLUMN: return DESC_WIDTH;
      case ASPECT_TYPE_COLUMN: return ASPECT_TYPE_WIDTH;
      case ASPECT_VALUE_COLUMN: return DATE_WIDTH;
      }
      return 75;
    }

    public int  getMinColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN: return SEND_WIDTH;
      case CLUSTER_COLUMN: return CLUSTER_WIDTH / 2;
      case RECEIVE_TIME_COLUMN: return DATE_WIDTH;
      case REPORT_TIME_COLUMN: return DATE_WIDTH;
      case GROUP_ID_COLUMN: return TASK_UID_WIDTH / 2;
      case TASK_UID_COLUMN: return TASK_UID_WIDTH / 2;
      case DESC_COLUMN: return DESC_WIDTH / 4;
      case ASPECT_TYPE_COLUMN: return 75;
      case ASPECT_VALUE_COLUMN: return 75;
      }
      return 75;
    }

    public int  getMaxColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN: return SEND_WIDTH;
      case CLUSTER_COLUMN: return CLUSTER_WIDTH;
      case RECEIVE_TIME_COLUMN: return DATE_WIDTH;
      case REPORT_TIME_COLUMN: return DATE_WIDTH;
      case GROUP_ID_COLUMN: return Integer.MAX_VALUE;
      case TASK_UID_COLUMN: return Integer.MAX_VALUE;
      case DESC_COLUMN: return Integer.MAX_VALUE;
      case ASPECT_TYPE_COLUMN: return Integer.MAX_VALUE;
      case ASPECT_VALUE_COLUMN: return Integer.MAX_VALUE;
      }
      return 75;
    }

    public String getColumnName(int col) {
      switch (col) {
      case ENABLE_COLUMN: return "Send";
      case CLUSTER_COLUMN: return "Cluster";
      case RECEIVE_TIME_COLUMN: return "Receive Date";
      case REPORT_TIME_COLUMN: return "Report Date";
      case GROUP_ID_COLUMN: return "Group Id";
      case TASK_UID_COLUMN: return "Task Id";
      case DESC_COLUMN: return "Description";
      case ASPECT_TYPE_COLUMN: return "Aspect Type";
      case ASPECT_VALUE_COLUMN: return "Aspect Value";
      }
      return null;
    }

    public Class getColumnClass(int col) {
      switch (col) {
      case ENABLE_COLUMN: return Boolean.class;
      case CLUSTER_COLUMN: return String.class;
      case RECEIVE_TIME_COLUMN: return String.class;
      case REPORT_TIME_COLUMN: return String.class;
      case GROUP_ID_COLUMN: return String.class;
      case TASK_UID_COLUMN: return String.class;
      case DESC_COLUMN: return String.class;
      case ASPECT_TYPE_COLUMN: return String.class;
      case ASPECT_VALUE_COLUMN: return Object.class;
      }
      return null;
    }

    public boolean isCellEditable(int row, int col) {
      TimedTaskEventReport ter = (TimedTaskEventReport) getRowObject(row);
      if (ter == null) return false;
      theEventGenerator.setPaused(true);
      switch (col) {
      case ENABLE_COLUMN: return true;
      case CLUSTER_COLUMN: return false;
      case RECEIVE_TIME_COLUMN: return true;
      case REPORT_TIME_COLUMN: return true;
      case GROUP_ID_COLUMN: return false;
      case TASK_UID_COLUMN: return false;
      case DESC_COLUMN: return false;
      case ASPECT_TYPE_COLUMN: return false;
      case ASPECT_VALUE_COLUMN:
        switch (ter.theTaskEventReport.theTaskEventId.theAspectType) {
        case AspectType.START_TIME:
        case AspectType.END_TIME:
        case AspectType.QUANTITY:
          return true;
        }
      }
      return false;
    }

    public boolean cellHasBeenEdited(int row, int col) {
      TimedTaskEventReport ter = (TimedTaskEventReport) getRowObject(row);
      if (ter == null) return false;
      if (ter.theOriginalTaskEventReport == null) return false;
      switch (col) {
      case ENABLE_COLUMN:
        return false;
      case RECEIVE_TIME_COLUMN:
        return ter.theTaskEventReport.theReceivedDate != ter.theOriginalTaskEventReport.theReceivedDate;
      case REPORT_TIME_COLUMN:
        return ter.theTaskEventReport.theReportDate != ter.theOriginalTaskEventReport.theReportDate;
      case GROUP_ID_COLUMN:
        return false;
      case TASK_UID_COLUMN:
        return false;
      case DESC_COLUMN:
        return false;
      case ASPECT_TYPE_COLUMN:
        return false;
      case ASPECT_VALUE_COLUMN:
        return ter.theTaskEventReport.theAspectValue != ter.theOriginalTaskEventReport.theAspectValue;
      }
      return false;
    }

    public void setValueAt(Object newValue, int row, int col) {
      TimedTaskEventReport ter = (TimedTaskEventReport) getRowObject(row);
      if (ter == null) return;
      if (ter.theEventGroup != null) {
        int answer =
            OptionPane.showOptionDialog(getGUI(),
                                        "Do you want to modify all constrained values?",
                                        "Change Constrained Values",
                                        OptionPane.YES_NO_CANCEL_OPTION,
                                        OptionPane.QUESTION_MESSAGE,
                                        null, null, null);
        switch (answer) {
          default: return;
        case OptionPane.YES_OPTION:
          for (Iterator i = ter.theEventGroup.iterator(); i.hasNext(); ) {
            setValue(newValue, (TimedTaskEventReport) i.next(), col);
          }
          fireTableDataChanged();
          break;
        case OptionPane.NO_OPTION:
          setValue(newValue, ter, col);
          break;
        }
      } else {
        setValue(newValue, ter, col);
      }
    }

    private void setValue(Object newValue, TimedTaskEventReport ter, int col) {
      String stringValue = "";
      if (newValue instanceof String) stringValue = (String) newValue;
      switch (col) {
      case ENABLE_COLUMN:
        ter.setEnabled(((Boolean) newValue).booleanValue());
        break;
      case RECEIVE_TIME_COLUMN:
        assureEditCopy(ter);
        TaskEventReportManager.super.removeFromSchedule(ter);
        if (stringValue.equals("")) {
          ter.theTaskEventReport.theReceivedDate = ter.theOriginalTaskEventReport.theReceivedDate;
        } else {
          ter.theTaskEventReport.theReceivedDate = new EGDate(stringValue).getTime();
        }
        TaskEventReportManager.super.addToSchedule(ter);
        break;
      case REPORT_TIME_COLUMN:
        assureEditCopy(ter);
        if (stringValue.equals("")) {
          ter.theTaskEventReport.theReportDate = ter.theOriginalTaskEventReport.theReportDate;
        } else {
          ter.theTaskEventReport.theReportDate = new EGDate(stringValue).getTime();
        }
        break;
      case GROUP_ID_COLUMN:
        break;
      case TASK_UID_COLUMN:
        break;
      case DESC_COLUMN:
        break;
      case ASPECT_TYPE_COLUMN:
        break;
      case ASPECT_VALUE_COLUMN:
        assureEditCopy(ter);
        switch (ter.theTaskEventReport.theTaskEventId.theAspectType) {
        case AspectType.START_TIME:
        case AspectType.END_TIME:
          if (stringValue.equals("")) {
            ter.theTaskEventReport.theAspectValue = ter.theOriginalTaskEventReport.theAspectValue;
          } else {
            ter.theTaskEventReport.theAspectValue = (double) new EGDate(stringValue).getTime();
          }
          break;
        case AspectType.QUANTITY:
          assureEditCopy(ter);
          try {
            if (stringValue.equals("")) {
              ter.theTaskEventReport.theAspectValue = ter.theOriginalTaskEventReport.theAspectValue;
            } else {
              ter.theTaskEventReport.theAspectValue = Double.parseDouble(stringValue);
            }
          } catch (NumberFormatException nfe) {
            postErrorMessage("Value must be numeric");
          }
          break;
        }
        break;
      }
    }

    private void assureEditCopy(TimedTaskEventReport ter) {
      ter.getModifiableTaskEventReport();
    }

    public Object getValueAt(int row, int col) {
      TimedTaskEventReport ter = (TimedTaskEventReport) getRowObject(row);
      if (ter == null) return null;
      switch (col) {
      case ENABLE_COLUMN:
        return new Boolean(ter.isEnabled());
      case CLUSTER_COLUMN:
        return ter.theSource;
      case RECEIVE_TIME_COLUMN:
        return new EGDate(ter.theTaskEventReport.theReceivedDate);
      case REPORT_TIME_COLUMN:
        return new EGDate(ter.theTaskEventReport.theReportDate);
      case GROUP_ID_COLUMN:
        return ter.theEventGroup == null ? null : ter.theEventGroup.theGroupId;
      case TASK_UID_COLUMN:
        return ter.theTaskEventReport.theTaskEventId.theTaskUID;
      case DESC_COLUMN:
        return ter.theTaskEventReport.theShortDescription;
      case ASPECT_TYPE_COLUMN:
        switch (ter.theTaskEventReport.theTaskEventId.theAspectType) {
        case AspectType.START_TIME:
          return "START_TIME";
        case AspectType.END_TIME:
          return "END_TIME";
        case AspectType.QUANTITY:
          return "QUANTITY";
        }
        return "ASPECT_TYPE " + ter.theTaskEventReport.theTaskEventId.theAspectType;
      case ASPECT_VALUE_COLUMN:
        switch (ter.theTaskEventReport.theTaskEventId.theAspectType) {
        case AspectType.START_TIME:
          return new EGDate((long) ter.theTaskEventReport.theAspectValue).toString();
        case AspectType.END_TIME:
          return new EGDate((long) ter.theTaskEventReport.theAspectValue).toString();
        case AspectType.QUANTITY:
          return new Double(ter.theTaskEventReport.theAspectValue);
        }
        return new Double(ter.theTaskEventReport.theAspectValue);
      }
      return null;
    }
    public String getToolTipText(int row, int column) {
      Timed object = getRowObject(row);
      if (object instanceof TimedTaskEventReport) {
        return ((TimedTaskEventReport) object).theTaskEventReport.theFullDescription;
      }
      return null;
    }
  }
}
