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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JComponent;
import org.cougaar.domain.glm.execution.common.*;

/**
 * Keeps track of the inventory schedules and decide if/when to
 * generate a report.
 **/
public class InventoryScheduleManager extends ManagerBase implements ScheduleManager {
  private InventoryReportManager theInventoryReportManager;

  public InventoryScheduleManager(EventGenerator anEventGenerator,
                                InventoryReportManager anInventoryReportManager)
  {
    super(anEventGenerator);
    theInventoryReportManager = anInventoryReportManager;
  }

  public String getGUITitle() {
    return "Inventory Schedules";
  }

  public FilterGUI createFilterGUI(EventGenerator anEventGenerator) {
    return new InventoryScheduleFilterGUI(anEventGenerator);
  }

  public Object[] getHandlers() {
    return new Object[] {
      new EGInventoryReportScheduleHandler(this),
    };
  }

  protected Class getPlugInInterface() {
    return InventoryPlugIn.class;
  }

  protected Class getDefaultPlugInClass() {
    return InventoryDefaultPlugIn.class;
  }

  private void applyPlugIns(TimedInventoryReport tir) {
    long theExecutionTime = theEventGenerator.getExecutionTime();
    for (Iterator i = getEnabledPlugIns(); i.hasNext(); ) {
      InventoryPlugIn thePlugIn = (InventoryPlugIn) i.next();
      if (thePlugIn.apply(tir, theExecutionTime)) return;
    }
  }

  /**
   * step values for finding nearest following date. They must
   * alternate in sign, be monotonically decreasing and end with
   * positive 1. Each step is applied until the result crosses zero
   * and starts getting worse.
   **/
  private static final int[] steps = {
    10000, -5000, 2000, -1000, 500, -200,
    100, -50, 20, -10, 5, -2, 1
  };

  /**
   * Steps the steppable to the closest time just before the given time.
   **/
  private static void stepToTime(SteppedInventoryReportSchedule sirs, long executionTime) {
    for (int j = 0; j < steps.length; j++) {
      int step = steps[j];
      long diff;
      while ((diff = (sirs.getTime() - executionTime)) * step < 0) {
        sirs.step(step);
      }
      /************************************ debug *********************/
      sirs.step(-1);            // Make it past due so it fires immediately
    }
  }

  public void receiveInventoryReportSchedules(String source,
                                              InventoryReportSchedule[] inventoryReportSchedules) {
    long executionTime = theEventGenerator.getExecutionTime();
    for (int i = 0; i < inventoryReportSchedules.length; i++) {
      InventoryReportSchedule irs = inventoryReportSchedules[i];
      Object key = SteppedInventoryReportSchedule.getKey(source, irs);
      SteppedInventoryReportSchedule sirs = (SteppedInventoryReportSchedule) map.get(key);
      if (irs.isRescind()) {
        if (sirs != null) {
          removeFromSchedule(sirs);
        }
      } else {
        if (sirs == null) {
          sirs = new SteppedInventoryReportSchedule(source, irs, this);
        } else {
          removeFromSchedule(sirs); // We could be altering the sort order
          sirs.theInventoryReportSchedule = irs;
        }
        // Step the scheduled date until it is just after the current execution time
        stepToTime(sirs, executionTime);
        addToSchedule(sirs);
      }
    }
    finishScheduleUpdate();
    advanceTime(executionTime);
  }
    
  public void requestInventoryReport(String source, String itemIdentification, long aReportDate)
    throws IOException
  {
    Object[] handlers = {
      new EGInventoryReportHandler(this),
    };
    InventoryReportListener l =
      theEventGenerator.createInventoryReportListener(source,
                                                      handlers,
                                                      itemIdentification,
                                                      aReportDate);
    if (l != null) {
      theEventGenerator.addListener(l);
      theEventGenerator.startListener(l);
    }
  }

  public void receiveInventoryReports(String source, InventoryReport[] inventoryReports) {
    TimedInventoryReport[] reports = new TimedInventoryReport[inventoryReports.length];
    for (int i = 0; i < inventoryReports.length; i++) {
      reports[i] =
        new TimedInventoryReport(source, inventoryReports[i], theInventoryReportManager);
      applyPlugIns(reports[i]);
    }
    theInventoryReportManager.receiveInventoryReports(source, reports);
  }

  protected EGTableModel createTableModel() {
    return new InventoryScheduleTableModel();
  }

  public static String getNameForCalendarField(int field) {
    switch (field) {
    case Calendar.MINUTE:
      return "Minutely";
    case Calendar.HOUR_OF_DAY:
    case Calendar.HOUR:
      return "Hourly";
    case Calendar.DAY_OF_MONTH: // synonym of DATE
    case Calendar.DAY_OF_WEEK:
    case Calendar.DAY_OF_YEAR:
      return "Daily";
    case Calendar.WEEK_OF_MONTH:
    case Calendar.DAY_OF_WEEK_IN_MONTH:
    case Calendar.WEEK_OF_YEAR:
      return "Weekly";
    case Calendar.MONTH:
      return "Monthly";
    case Calendar.YEAR:
      return "Yearly";
    default: return "step " + field;
    }
  }

  private class InventoryScheduleTableModel extends ManagerTableModel {
    private static final int ENABLE_COLUMN         = 0;
    private static final int CLUSTER_COLUMN        = 1;
    private static final int SCHEDULED_DATE_COLUMN = 2;
    private static final int STEP_COLUMN           = 3;
    private static final int ITEM_COLUMN           = 4;
    private static final int NCOLUMNS              = 5;
    private /*ns*/ final int ITEM_WIDTH = getCellWidth("Inventory: NSN/XXXXXXXXXXXXX");

    public int getColumnCount() {
      return NCOLUMNS;
    }

    public int getPreferredColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN: return SEND_WIDTH;
      case CLUSTER_COLUMN: return CLUSTER_WIDTH;
      case SCHEDULED_DATE_COLUMN: return DATE_WIDTH;
      case STEP_COLUMN: return STEP_WIDTH;
      case ITEM_COLUMN: return ITEM_WIDTH;
      }
      return 75;
    }

    public int  getMinColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN: return SEND_WIDTH;
      case CLUSTER_COLUMN: return CLUSTER_WIDTH / 2;
      case SCHEDULED_DATE_COLUMN: return DATE_WIDTH;
      case STEP_COLUMN: return STEP_WIDTH;
      case ITEM_COLUMN: return ITEM_WIDTH / 2;
      }
      return 75;
    }

    public int  getMaxColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN: return SEND_WIDTH;
      case CLUSTER_COLUMN: return CLUSTER_WIDTH;
      case SCHEDULED_DATE_COLUMN: return DATE_WIDTH;
      case STEP_COLUMN: return STEP_WIDTH;
      case ITEM_COLUMN: return Integer.MAX_VALUE;
      }
      return 75;
    }

    public String getColumnName(int col) {
      switch (col) {
      case ENABLE_COLUMN: return "Send";
      case CLUSTER_COLUMN: return "Cluster";
      case SCHEDULED_DATE_COLUMN: return "Scheduled Time";
      case STEP_COLUMN: return "Period";
      case ITEM_COLUMN: return "Item";
      }
      return null;
    }

    public Class getColumnClass(int col) {
      switch (col) {
      case ENABLE_COLUMN: return Boolean.class;
      case CLUSTER_COLUMN: return String.class;
      case SCHEDULED_DATE_COLUMN: return EGDate.class;
      case STEP_COLUMN: return String.class;
      case ITEM_COLUMN: return String.class;
      }
      return null;
    }

    public boolean isCellEditable(int row, int col) {
      theEventGenerator.setPaused(true);
      return col == ENABLE_COLUMN;
    }

    public boolean cellHasBeenEdited(int row, int col) {
      return false;             // No real cells are editable
    }

    public void setValueAt(Object newValue, int row, int col) {
      SteppedInventoryReportSchedule sirs = (SteppedInventoryReportSchedule) getRowObject(row);
      if (sirs == null) return;
      switch (col) {
      case ENABLE_COLUMN:
        sirs.setEnabled(((Boolean) newValue).booleanValue());
        break;
      case SCHEDULED_DATE_COLUMN:
        break;
      case STEP_COLUMN:
        break;
      case ITEM_COLUMN:
        break;
      }
    }

    public Object getValueAt(int row, int col) {
      SteppedInventoryReportSchedule sirs = (SteppedInventoryReportSchedule) getRowObject(row);
      if (sirs == null) return null;
      switch (col) {
      case ENABLE_COLUMN:
        return new Boolean(sirs.isEnabled());
      case CLUSTER_COLUMN:
        return sirs.theSource;
      case SCHEDULED_DATE_COLUMN:
        return new EGDate(sirs.getTime());
      case STEP_COLUMN:
        return getNameForCalendarField(sirs.theInventoryReportSchedule.theStep);
      case ITEM_COLUMN:
        return sirs.theInventoryReportSchedule.theItemIdentification;
      }
      return null;
    }
  }
}
