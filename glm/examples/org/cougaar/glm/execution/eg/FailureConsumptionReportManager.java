/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
package org.cougaar.glm.execution.eg;

import org.cougaar.planning.ldm.measure.Rate;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.cougaar.glm.execution.common.*;

/**
 * Keeps track of the inventory report schedules and decide if/when to
 * generate a report.
 **/
public class FailureConsumptionReportManager extends ManagerBase implements ScheduleManager {

  public FailureConsumptionReportManager(EventGenerator anEventGenerator) {
    super(anEventGenerator);
  }

  protected String getLogFileName(String prefix) {
    return prefix + "FCEvents" + LOGFILE_SUFFIX;
  }

  public String getGUITitle() {
    return "F/C Reports";
  }

  public FilterGUI createFilterGUI(EventGenerator anEventGenerator) {
    return new FailureConsumptionReportFilterGUI(anEventGenerator);
  }

  public Object[] getHandlers() {
    return new Object[0];
  }


  public void enqueueFailureConsumptionReport(String theSource,
                                              FailureConsumptionReport aFailureConsumptionReport,
                                              Object annotation)
  {
    TimedFailureConsumptionReport tfcr =
      new TimedFailureConsumptionReport(theSource, aFailureConsumptionReport, this, annotation);
    addToSchedule(tfcr);
    finishScheduleUpdate();
  }

  public void sendFailureConsumptionReport(String source,
                                           FailureConsumptionReport aFailureConsumptionReport)
    throws IOException
  {
    Object[] handlers = {};     // There is no response
    EventMonitorListener l =
      theEventGenerator.createEventMonitorListener(source, handlers, aFailureConsumptionReport);
    if (l != null) {
      theEventGenerator.addListener(l);
      theEventGenerator.startListener(l);
    }
  }

  protected ManagerTableModel createTableModel() {
    return new FailureConsumptionReportTableModel();
  }

  private static final int ENABLE_COLUMN        = 0;
  private static final int CLUSTER_COLUMN       = 1;
  private static final int RECEIVE_TIME_COLUMN  = 2;
  private static final int REPORT_TIME_COLUMN   = 3;
  private static final int CONSUMER_COLUMN      = 4;
  private static final int ITEM_COLUMN          = 5;
  private static final int QUANTITY_COLUMN      = 6;
  private static final int ANNOTATION_COLUMN    = 7;
  private static final int NCOLUMNS             = 8;

  private class FailureConsumptionReportTableModel extends ManagerTableModel {

    public FailureConsumptionReportTableModel() {
      logColumns = new int[] {
        CLUSTER_COLUMN,
        RECEIVE_TIME_COLUMN,
        REPORT_TIME_COLUMN,
        CONSUMER_COLUMN,
        ITEM_COLUMN,
        QUANTITY_COLUMN,
        ANNOTATION_COLUMN
      };
    }
    public int getColumnCount() {
      return NCOLUMNS;
    }

    public int getPreferredColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN:        return SEND_WIDTH;
      case CLUSTER_COLUMN:       return CLUSTER_WIDTH;
      case RECEIVE_TIME_COLUMN:  return DATE_WIDTH;
      case REPORT_TIME_COLUMN:   return DATE_WIDTH;
      case CONSUMER_COLUMN:      return CONSUMER_WIDTH;
      case ITEM_COLUMN:          return ITEM_WIDTH;
      case QUANTITY_COLUMN:      return QUANTITY_WIDTH;
      case ANNOTATION_COLUMN:    return ANNOTATION_WIDTH;
      }
      return 75;
    }

    public int  getMinColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN:        return SEND_WIDTH;
      case CLUSTER_COLUMN:       return CLUSTER_WIDTH / 2;
      case RECEIVE_TIME_COLUMN:  return DATE_WIDTH / 2;
      case REPORT_TIME_COLUMN:   return DATE_WIDTH / 2;
      case CONSUMER_COLUMN:      return CONSUMER_WIDTH / 2;
      case ITEM_COLUMN:          return ITEM_WIDTH / 2;
      case QUANTITY_COLUMN:      return QUANTITY_WIDTH / 2;
      case ANNOTATION_COLUMN:    return ANNOTATION_WIDTH / 2;
      }
      return 75;
    }

    public int  getMaxColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN:        return SEND_WIDTH;
      case CLUSTER_COLUMN:       return CLUSTER_WIDTH;
      case RECEIVE_TIME_COLUMN:  return DATE_WIDTH;
      case REPORT_TIME_COLUMN:   return DATE_WIDTH;
      case CONSUMER_COLUMN:      return Integer.MAX_VALUE;
      case ITEM_COLUMN:          return Integer.MAX_VALUE;
      case QUANTITY_COLUMN:      return Integer.MAX_VALUE;
      case ANNOTATION_COLUMN:    return Integer.MAX_VALUE;
      }
      return 75;
    }

    public String getColumnName(int col) {
      switch (col) {
      case ENABLE_COLUMN:        return "Send";
      case CLUSTER_COLUMN:       return "Cluster";
      case RECEIVE_TIME_COLUMN:  return "Receive Time";
      case REPORT_TIME_COLUMN:   return "Report Time";
      case CONSUMER_COLUMN:      return "Consumer";
      case ITEM_COLUMN:          return "Item";
      case QUANTITY_COLUMN:      return "Quantity";
      case ANNOTATION_COLUMN:    return "Comment";
      }
      return null;
    }

    public Class getColumnClass(int col) {
      switch (col) {
      case ENABLE_COLUMN:        return Boolean.class;
      case CLUSTER_COLUMN:       return String.class;
      case RECEIVE_TIME_COLUMN:  return EGDate.class;
      case REPORT_TIME_COLUMN:   return EGDate.class;
      case CONSUMER_COLUMN:      return String.class;
      case ITEM_COLUMN:          return String.class;
      case QUANTITY_COLUMN:      return Double.class;
      case ANNOTATION_COLUMN:    return Object.class;
      }
      return null;
    }

    public boolean isCellEditable(int row, int col) {
      theEventGenerator.setPaused(true);
      switch (col) {
      case ENABLE_COLUMN:       return true;
      case CLUSTER_COLUMN:      return false;
      case RECEIVE_TIME_COLUMN: return true;
      case CONSUMER_COLUMN:     return false;
      case ITEM_COLUMN:         return false;
      case QUANTITY_COLUMN:     return true;
      }
      return false;
    }

    public boolean cellHasBeenEdited(int row, int col) {
      TimedFailureConsumptionReport tfcr = (TimedFailureConsumptionReport) getRowObject(row);
      if (tfcr == null) return false;
      if (tfcr.theOriginalFailureConsumptionReport == null) return false;
      switch (col) {
      case ENABLE_COLUMN:
        return false;
      case RECEIVE_TIME_COLUMN:
        return (tfcr.theFailureConsumptionReport.theReceivedDate
                != tfcr.theOriginalFailureConsumptionReport.theReceivedDate);
      case REPORT_TIME_COLUMN:
        return (tfcr.theFailureConsumptionReport.theReportDate
                != tfcr.theOriginalFailureConsumptionReport.theReportDate);
      case CONSUMER_COLUMN:
      case ITEM_COLUMN:
        return false;
      case QUANTITY_COLUMN:
        return (tfcr.theFailureConsumptionReport.theQuantity
                != tfcr.theOriginalFailureConsumptionReport.theQuantity);
      }
      return false;
    }

    public void setValueAt(Object newValue, int row, int col) {
      TimedFailureConsumptionReport tfcr = (TimedFailureConsumptionReport) getRowObject(row);
      if (tfcr == null) return;
      String stringValue = null;
      if (newValue instanceof String) stringValue = (String) newValue;
      switch (col) {
      case ENABLE_COLUMN:
        tfcr.setEnabled(((Boolean) newValue).booleanValue());
        break;
      case RECEIVE_TIME_COLUMN:
        assureEditCopy(tfcr);
        removeFromSchedule(tfcr);
        if (stringValue.equals("")) {
          tfcr.theFailureConsumptionReport.theReceivedDate =
            tfcr.theOriginalFailureConsumptionReport.theReceivedDate;
        } else {
          tfcr.theFailureConsumptionReport.theReceivedDate = new Date((String) newValue).getTime();
        }
        addToSchedule(tfcr);
        finishScheduleUpdate();
        break;
      case REPORT_TIME_COLUMN:
        assureEditCopy(tfcr);
        tfcr.theFailureConsumptionReport.theReportDate = new Date((String) newValue).getTime();
        break;
      case CONSUMER_COLUMN:
      case ITEM_COLUMN:
        break;
      case QUANTITY_COLUMN:
        assureEditCopy(tfcr);
        try {
          if (stringValue.equals("")) {
            tfcr.theFailureConsumptionReport.theQuantity =
              tfcr.theOriginalFailureConsumptionReport.theQuantity;
          } else {
            tfcr.theFailureConsumptionReport.theQuantity =
              Double.parseDouble(((String) newValue));
          }
        } catch (NumberFormatException nfe) {
          postErrorMessage("Value must be numeric");
        }
        break;
      }
    }

    private void assureEditCopy(TimedFailureConsumptionReport tfcr) {
      if (tfcr.theOriginalFailureConsumptionReport == null) {
        tfcr.theOriginalFailureConsumptionReport =
          new FailureConsumptionReport(tfcr.theFailureConsumptionReport);
      }
    }

    public Object getValue(int col, Object rowObject) {
      TimedFailureConsumptionReport tfcr = (TimedFailureConsumptionReport) rowObject;
      if (tfcr == null) return null;
      switch (col) {
      case ENABLE_COLUMN:
        return new Boolean(tfcr.isEnabled());
      case CLUSTER_COLUMN:
        return tfcr.theSource;
      case RECEIVE_TIME_COLUMN:
        return new EGDate(tfcr.theFailureConsumptionReport.theReceivedDate);
      case REPORT_TIME_COLUMN:
        return new EGDate(tfcr.theFailureConsumptionReport.theReportDate);
      case CONSUMER_COLUMN:
        return tfcr.theFailureConsumptionReport.theConsumer;
      case ITEM_COLUMN:
        return tfcr.theFailureConsumptionReport.theItemIdentification;
      case QUANTITY_COLUMN:
        return new Double(tfcr.theFailureConsumptionReport.theQuantity);
      case ANNOTATION_COLUMN:
        return tfcr.getAnnotation();
      }
      return null;
    }
  }
}
