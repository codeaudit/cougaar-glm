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
package org.cougaar.glm.execution.eg;

import org.cougaar.planning.ldm.measure.AbstractRate;
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
public class FailureConsumptionRateManager extends ManagerBase implements ScheduleManager {
  private FailureConsumptionReportManager theFailureConsumptionReportManager;

  public FailureConsumptionRateManager(EventGenerator anEventGenerator,
                                       FailureConsumptionReportManager aFailureConsumptionReportManager)
  {
    super(anEventGenerator);
    theFailureConsumptionReportManager = aFailureConsumptionReportManager;
  }

  protected String getLogFileName(String prefix) {
    return prefix + "FCRates" + LOGFILE_SUFFIX;
  }

  public String getGUITitle() {
    return "F/C Rates";
  }

  public FilterGUI createFilterGUI(EventGenerator anEventGenerator) {
    return new FailureConsumptionRateFilterGUI(anEventGenerator);
  }

  public Object[] getHandlers() {
    return new Object[] {
      new EGFailureConsumptionRateHandler(this),
    };
  }

  public void enqueueFailureConsumptionReport(String theSource,
                                              FailureConsumptionReport aFailureConsumptionReport,
                                              Object annotation)
  {
    theFailureConsumptionReportManager
      .enqueueFailureConsumptionReport(theSource,
                                       aFailureConsumptionReport,
                                       annotation);
  }

  protected Class getPlugInInterface() {
    return FailureConsumptionPlugIn.class;
  }

  protected Class getDefaultPlugInClass() {
    return FailureConsumptionDefaultPlugIn.class;
  }

  public FailureConsumptionPlugInItem getPlugInItem(FailureConsumptionRate theFailureConsumptionRate, 
 						    FailureConsumptionSegment theFailureConsumptionSegment,
 						    FailureConsumptionPlugInItem aFailureConsumptionPlugInItem)
  {
    long theExecutionTime = theEventGenerator.getExecutionTime();
    for (Iterator i = getEnabledPlugIns(); i.hasNext(); ) {
      FailureConsumptionPlugIn thePlugIn = (FailureConsumptionPlugIn) i.next();
      FailureConsumptionPlugInItem item =
        thePlugIn.createFailureConsumptionItem(theFailureConsumptionRate, theFailureConsumptionSegment, theExecutionTime, aFailureConsumptionPlugInItem);
      if (item != null) return item;
    }
    return null;
  }

    public void firePlugInChanged() {
	synchronized (schedule) {
	    for (Iterator i = schedule.iterator(); i.hasNext(); ) {
		FailureConsumptionSegment fcs = (FailureConsumptionSegment) i.next();
		fcs.setPlugInItem(getPlugInItem(fcs.theFailureConsumptionRate, fcs, fcs.thePlugInItem));
	    }
	}
    }

  public void receiveFailureConsumptionRates(String source,
                                             FailureConsumptionRate[] failureConsumptionRates)
  {
    long executionTime = theEventGenerator.getExecutionTime();
    for (int i = 0; i < failureConsumptionRates.length; i++) {
      FailureConsumptionRate fcr = failureConsumptionRates[i];
//        System.out.println("Receive : " + fcr.theRateValue + fcr.theRateUnits);

      FailureConsumptionSegment fcs =
        (FailureConsumptionSegment) map.get(FailureConsumptionSegment.getKey(fcr));
      if (fcs != null) {
        removeFromSchedule(fcs);
      }
      if (!fcr.isRescind()) {
        fcr.theStartTime = Math.max(executionTime, fcr.theStartTime);
        if (fcr.theStartTime < fcr.theEndTime) {
          fcs = new FailureConsumptionSegment(source, fcr, this);
          addToSchedule(fcs);
        }
      }
    }
    finishScheduleUpdate();
  }

  protected ManagerTableModel createTableModel() {
    return new FailureConsumptionRateTableModel();
  }

  private static final int ENABLE_COLUMN        = 0;
  private static final int CLUSTER_COLUMN       = 1;
  private static final int START_TIME_COLUMN    = 2;
  private static final int END_TIME_COLUMN      = 3;
  private static final int ITEM_COLUMN          = 4;
  private static final int CONSUMER_COLUMN      = 5;
  private static final int CONSUMER_ID_COLUMN   = 6;
  private static final int RATE_COLUMN          = 7;
  private static final int RATE_MULT_COLUMN     = 8;
  private static final int UNITS_COLUMN         = 9;
  private static final int ANNOTATION_COLUMN    = 10;
  private static final int NCOLUMNS             = 11;
  private static final int UNITS_WIDTH =
    EGTableModelBase.getCellWidth("gallons / minute");

  private class FailureConsumptionRateTableModel extends ManagerTableModel {

    public FailureConsumptionRateTableModel() {
      logColumns = new int[] {
        CLUSTER_COLUMN,
        START_TIME_COLUMN,
        END_TIME_COLUMN,
        ITEM_COLUMN,
        CONSUMER_COLUMN,
        CONSUMER_ID_COLUMN,
        RATE_COLUMN,
        RATE_MULT_COLUMN,
        UNITS_COLUMN,
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
      case START_TIME_COLUMN:    return DATE_WIDTH;
      case END_TIME_COLUMN:      return DATE_WIDTH;
      case CONSUMER_COLUMN:      return CONSUMER_WIDTH;
      case CONSUMER_ID_COLUMN:   return ITEM_WIDTH;
      case ITEM_COLUMN:          return ITEM_WIDTH;
      case RATE_COLUMN:          return QUANTITY_WIDTH;
      case RATE_MULT_COLUMN:     return QUANTITY_WIDTH;
      case UNITS_COLUMN:         return UNITS_WIDTH;
      case ANNOTATION_COLUMN:    return ANNOTATION_WIDTH;
      }
      return 75;
    }

    public int  getMinColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN:        return SEND_WIDTH;
      case CLUSTER_COLUMN:       return CLUSTER_WIDTH / 2;
      case START_TIME_COLUMN:    return DATE_WIDTH / 2;
      case END_TIME_COLUMN:      return DATE_WIDTH / 2;
      case CONSUMER_COLUMN:      return CONSUMER_WIDTH / 2;
      case CONSUMER_ID_COLUMN:   return ITEM_WIDTH / 2;
      case ITEM_COLUMN:          return ITEM_WIDTH / 2;
      case RATE_COLUMN:          return QUANTITY_WIDTH / 2;
      case RATE_MULT_COLUMN:     return QUANTITY_WIDTH / 2;
      case UNITS_COLUMN:         return UNITS_WIDTH / 2;
      case ANNOTATION_COLUMN:    return ANNOTATION_WIDTH / 2;
      }
      return 75;
    }

    public int  getMaxColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN:        return SEND_WIDTH;
      case CLUSTER_COLUMN:       return CLUSTER_WIDTH;
      case START_TIME_COLUMN:    return DATE_WIDTH;
      case END_TIME_COLUMN:      return DATE_WIDTH;
      case CONSUMER_COLUMN:      return Integer.MAX_VALUE;
      case CONSUMER_ID_COLUMN:   return Integer.MAX_VALUE;
      case ITEM_COLUMN:          return Integer.MAX_VALUE;
      case RATE_COLUMN:          return Integer.MAX_VALUE;
      case RATE_MULT_COLUMN:     return Integer.MAX_VALUE;
      case UNITS_COLUMN:         return Integer.MAX_VALUE;
      case ANNOTATION_COLUMN:    return Integer.MAX_VALUE;
      }
      return 75;
    }

    public String getColumnName(int col) {
      switch (col) {
      case ENABLE_COLUMN:        return "Send";
      case CLUSTER_COLUMN:       return "Cluster";
      case START_TIME_COLUMN:    return "Start Time";
      case END_TIME_COLUMN:      return "End Time";
      case CONSUMER_COLUMN:      return "Consumer";
      case CONSUMER_ID_COLUMN:   return "Consumer Id";
      case ITEM_COLUMN:          return "Item";
      case RATE_COLUMN:          return "Rate";
      case RATE_MULT_COLUMN:     return "Rate Multiplier";
      case UNITS_COLUMN:         return "Units";
      case ANNOTATION_COLUMN:    return "Comment";
      }
      return null;
    }

    public Class getColumnClass(int col) {
      switch (col) {
      case ENABLE_COLUMN:        return Boolean.class;
      case CLUSTER_COLUMN:       return String.class;
      case START_TIME_COLUMN:    return EGDate.class;
      case END_TIME_COLUMN:      return EGDate.class;
      case CONSUMER_COLUMN:      return String.class;
      case CONSUMER_ID_COLUMN:   return String.class;
      case ITEM_COLUMN:          return String.class;
      case RATE_COLUMN:          return Double.class;
      case RATE_MULT_COLUMN:     return Double.class;
      case UNITS_COLUMN:         return String.class;
      case ANNOTATION_COLUMN:    return Object.class;
      }
      return null;
    }

    public boolean isCellEditable(int row, int col) {
      theEventGenerator.setPaused(true);
      switch (col) {
      case ENABLE_COLUMN:        return true;
      case CLUSTER_COLUMN:       return false;
      case START_TIME_COLUMN:    return false;
      case CONSUMER_COLUMN:      return false;
      case CONSUMER_ID_COLUMN:   return false;
      case ITEM_COLUMN:          return false;
      case RATE_COLUMN:          return true;
      case RATE_MULT_COLUMN:     return false; // Erika - should this be "true"?
      case UNITS_COLUMN:         return false;
      case ANNOTATION_COLUMN:    return false;
      }
      return false;
    }

    public boolean cellHasBeenEdited(int row, int col) {
      FailureConsumptionSegment fcs = (FailureConsumptionSegment) getRowObject(row);
      if (fcs == null) return false;
      if (fcs.theOriginalFailureConsumptionRate == null) return false;
      switch (col) {
      case ENABLE_COLUMN:
        return false;
      case START_TIME_COLUMN:
        return false;
      case END_TIME_COLUMN:
        return false;
      case CONSUMER_COLUMN:
        return false;
      case CONSUMER_ID_COLUMN:
        return false;
      case ITEM_COLUMN:
        return false;
      case RATE_COLUMN:
        return (fcs.theFailureConsumptionRate.theRateValue
                != fcs.theOriginalFailureConsumptionRate.theRateValue);
      case RATE_MULT_COLUMN:
        return false;	     // Erika: should this be "true"?
      }
      return false;
    }

    public void setValueAt(Object newValue, int row, int col) {
      FailureConsumptionSegment fcs = (FailureConsumptionSegment) getRowObject(row);
      if (fcs == null) return;
      switch (col) {
      case ENABLE_COLUMN:
        fcs.setEnabled(((Boolean) newValue).booleanValue());
        break;
      case START_TIME_COLUMN:
        break;
      case END_TIME_COLUMN:
        break;
      case CONSUMER_COLUMN:
        break;
      case CONSUMER_ID_COLUMN:
        break;
      case ITEM_COLUMN:
        break;
      case RATE_COLUMN:
        assureEditCopy(fcs);
        try {
          fcs.theFailureConsumptionRate.theRateValue =
            Double.parseDouble(((String) newValue));
        } catch (NumberFormatException nfe) {
          postErrorMessage("Value must be numeric");
        }
        break;
      case RATE_MULT_COLUMN:
        break;		// Erika: should this be "true"?
      }
    }

    private void assureEditCopy(FailureConsumptionSegment fcs) {
      if (fcs.theOriginalFailureConsumptionRate == null) {
        fcs.theOriginalFailureConsumptionRate =
          new FailureConsumptionRate(fcs.theFailureConsumptionRate);
      }
    }

    public Object getValue(int col, Object rowObject) {
      FailureConsumptionSegment fcs = (FailureConsumptionSegment) rowObject;
      if (fcs == null) return null;
      switch (col) {
      case ENABLE_COLUMN:
        return new Boolean(fcs.isEnabled());
      case CLUSTER_COLUMN:
        return fcs.theSource;
      case START_TIME_COLUMN:
        return new EGDate(fcs.theFailureConsumptionRate.theStartTime);
      case END_TIME_COLUMN:
        return new EGDate(fcs.theFailureConsumptionRate.theEndTime);
      case CONSUMER_COLUMN:
        return fcs.theFailureConsumptionRate.theConsumer;
      case CONSUMER_ID_COLUMN:
        return fcs.theFailureConsumptionRate.theConsumerId;
      case ITEM_COLUMN:
        return fcs.theFailureConsumptionRate.theItemIdentification;
      case RATE_COLUMN: {
        int units = fcs.getUnits();
        return new Double(fcs.theFailureConsumptionRate.theRateValue);
      }
      case RATE_MULT_COLUMN: {
        int units = fcs.getUnits();
        return new Double(fcs.theFailureConsumptionRate.theRateMultiplier);
      }
      case UNITS_COLUMN:
        return fcs.theFailureConsumptionRate.theRateUnits;
      case ANNOTATION_COLUMN:
        return fcs.getAnnotation();
      }
      return null;
    }
  }
}
