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
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JComponent;
import org.cougaar.domain.glm.execution.common.*;

/**
 * Keeps track of the inventory report schedules and decide if/when to
 * generate a report.
 **/
public class InventoryReportManager extends ManagerBase implements ScheduleManager {
  public InventoryReportManager(EventGenerator anEventGenerator) {
    super(anEventGenerator);
  }

  public String getGUITitle() {
    return "Inventory Reports";
  }

  public FilterGUI createFilterGUI(EventGenerator anEventGenerator) {
    return new InventoryReportFilterGUI(anEventGenerator);
  }

  public Object[] getHandlers() {
    return new Object[0];
  }

  public void receiveInventoryReports(String source, TimedInventoryReport[] reports) {
    for (int i = 0; i < reports.length; i++) {
      addToSchedule(reports[i]);
    }
    finishScheduleUpdate();
  }

  public void sendInventoryReport(String source, InventoryReport anInventoryReport)
    throws IOException
  {
    Object[] handlers = {};     // There is no response
    EventMonitorListener l =
      theEventGenerator.createEventMonitorListener(source, handlers, anInventoryReport);
    if (l != null) {
      theEventGenerator.addListener(l);
      theEventGenerator.startListener(l);
    }
  }

  protected EGTableModel createTableModel() {
    return new InventoryReportTableModel();
  }

  private class InventoryReportTableModel extends ManagerTableModel {
    private static final int ENABLE_COLUMN        = 0;
    private static final int CLUSTER_COLUMN       = 1;
    private static final int RECEIVE_TIME_COLUMN  = 2;
    private static final int REPORT_TIME_COLUMN   = 3;
    private static final int ITEM_COLUMN          = 4;
    private static final int QUANTITY_COLUMN      = 5;
    private static final int NCOLUMNS             = 6;
    private final int ITEM_WIDTH = getCellWidth("Inventory: NSN/XXXXXXXXXXXXX");

    public int getColumnCount() {
      return NCOLUMNS;
    }

    public int getPreferredColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN: return SEND_WIDTH;
      case CLUSTER_COLUMN: return CLUSTER_WIDTH;
      case RECEIVE_TIME_COLUMN: return DATE_WIDTH;
      case REPORT_TIME_COLUMN: return DATE_WIDTH;
      case ITEM_COLUMN: return ITEM_WIDTH;
      case QUANTITY_COLUMN: return QUANTITY_WIDTH;
      }
      return 75;
    }

    public int  getMinColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN: return SEND_WIDTH;
      case CLUSTER_COLUMN: return CLUSTER_WIDTH / 2;
      case RECEIVE_TIME_COLUMN: return DATE_WIDTH;
      case REPORT_TIME_COLUMN: return DATE_WIDTH;
      case ITEM_COLUMN: return ITEM_WIDTH / 2;
      case QUANTITY_COLUMN: return 50;
      }
      return 75;
    }

    public int  getMaxColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN: return SEND_WIDTH;
      case CLUSTER_COLUMN: return CLUSTER_WIDTH;
      case RECEIVE_TIME_COLUMN: return DATE_WIDTH;
      case REPORT_TIME_COLUMN: return DATE_WIDTH;
      case ITEM_COLUMN: return Integer.MAX_VALUE;
      case QUANTITY_COLUMN: return Integer.MAX_VALUE;
      }
      return 75;
    }

    public String getColumnName(int col) {
      switch (col) {
      case ENABLE_COLUMN: return "Send";
      case CLUSTER_COLUMN: return "Cluster";
      case RECEIVE_TIME_COLUMN: return "Receive Time";
      case REPORT_TIME_COLUMN: return "Report Time";
      case ITEM_COLUMN: return "Item";
      case QUANTITY_COLUMN: return "Quantity";
      }
      return null;
    }

    public Class getColumnClass(int col) {
      switch (col) {
      case ENABLE_COLUMN: return Boolean.class;
      case CLUSTER_COLUMN: return String.class;
      case RECEIVE_TIME_COLUMN: return EGDate.class;
      case REPORT_TIME_COLUMN: return EGDate.class;
      case ITEM_COLUMN: return String.class;
      case QUANTITY_COLUMN: return Double.class;
      }
      return null;
    }

    public boolean isCellEditable(int row, int col) {
      theEventGenerator.setPaused(true);
      switch (col) {
      case ENABLE_COLUMN: return true;
      case RECEIVE_TIME_COLUMN: return true;
      case REPORT_TIME_COLUMN: return true;
      case ITEM_COLUMN: return false;
      case QUANTITY_COLUMN: return true;
      }
      return false;
    }

    public boolean cellHasBeenEdited(int row, int col) {
      TimedInventoryReport tir = (TimedInventoryReport) getRowObject(row);
      if (tir == null) return false;
      if (tir.theOriginalInventoryReport == null) return false;
      switch (col) {
      case ENABLE_COLUMN: return false; // May have been edited, but appearance doesn't change
      case RECEIVE_TIME_COLUMN:
        return (tir.theOriginalInventoryReport.theReceivedDate
                != tir.theInventoryReport.theReceivedDate);
      case REPORT_TIME_COLUMN:
        return (tir.theOriginalInventoryReport.theReportDate
                != tir.theInventoryReport.theReportDate);
      case ITEM_COLUMN:
        return false;
      case QUANTITY_COLUMN:
        if (tir.theOriginalInventoryReport.theQuantity
            != tir.theInventoryReport.theQuantity) {
          return true;
        } else {
          return false;
        }
      }
      return false;
    }

    public void setValueAt(Object newValue, int row, int col) {
      TimedInventoryReport tir = (TimedInventoryReport) getRowObject(row);
      if (tir == null) return;
      String stringValue = "";
      if (newValue instanceof String) stringValue = (String) newValue;
      switch (col) {
      case ENABLE_COLUMN:
        tir.setEnabled(((Boolean) newValue).booleanValue());
        break;
      case RECEIVE_TIME_COLUMN:
        assureEditCopy(tir);
        removeFromSchedule(tir);
        if (stringValue.equals("")) {
          tir.theInventoryReport.theReceivedDate = tir.theOriginalInventoryReport.theReceivedDate;
        } else {
          tir.theInventoryReport.theReceivedDate = new Date(stringValue).getTime();
        }
        addToSchedule(tir);
        finishScheduleUpdate();
        break;
      case REPORT_TIME_COLUMN:
        assureEditCopy(tir);
        if (stringValue.equals("")) {
          tir.theInventoryReport.theReportDate = tir.theOriginalInventoryReport.theReportDate;
        } else {
          tir.theInventoryReport.theReportDate = new Date(stringValue).getTime();
        }
        break;
      case ITEM_COLUMN:
        break;
      case QUANTITY_COLUMN:
        assureEditCopy(tir);
        try {
          if (stringValue.equals("")) {
          tir.theInventoryReport.theQuantity = tir.theOriginalInventoryReport.theQuantity;
          } else {
            tir.theInventoryReport.theQuantity = Double.parseDouble(stringValue);
          }
        } catch (NumberFormatException nfe) {
          postErrorMessage("Value must be numeric");
        }
        break;
      }
    }

    private void assureEditCopy(TimedInventoryReport tir) {
      tir.getModifiableInventoryReport();
    }

    public Object getValueAt(int row, int col) {
      TimedInventoryReport tir = (TimedInventoryReport) getRowObject(row);
      if (tir == null) return null;
      switch (col) {
      case ENABLE_COLUMN:
        return new Boolean(tir.isEnabled());
      case CLUSTER_COLUMN:
        return tir.theSource;
      case RECEIVE_TIME_COLUMN:
        return new EGDate(tir.getTime());
      case REPORT_TIME_COLUMN:
        return new EGDate(tir.theInventoryReport.theReportDate);
      case ITEM_COLUMN:
        return tir.theInventoryReport.theItemIdentification;
      case QUANTITY_COLUMN:
        return new Double(tir.theInventoryReport.theQuantity);
      }
      return null;
    }
  }
}
