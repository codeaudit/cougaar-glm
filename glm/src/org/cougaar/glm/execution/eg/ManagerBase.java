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

import javax.swing.table.AbstractTableModel;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;
import javax.swing.JComponent;
import org.cougaar.util.OptionPane;
import org.cougaar.glm.execution.common.*;

/**
 * Keeps track of the inventory report schedules and decide if/when to
 * generate a report.
 **/
public abstract class ManagerBase {
  protected EventGenerator theEventGenerator;
  private TreeSet rawSchedule = new TreeSet();
  protected FilteredSet schedule = new FilteredSet(rawSchedule);
  private boolean scheduleChanged = false;
  protected ManagerTableModel tableModel = null;
  protected HashMap map = new HashMap();
  private FilterGUI theFilterGUI = null;
  private ReportManagerGUI theReportManagerGUI;
  private List plugins = new ArrayList();
  private PrintWriter logWriter = null;
  protected static final String LOGFILE_SUFFIX = ".tsv";

  public ManagerBase(EventGenerator anEventGenerator) {
    theEventGenerator = anEventGenerator;
    installPlugins();
  }

  public void enableLog(String prefix) throws IOException {
    logWriter = new PrintWriter(new FileWriter(getLogFileName(prefix)), true);
    writeLogHeader();
  }

  protected abstract String getLogFileName(String prefix);

  /**
   * Get the class of the default plugin. Override this if this
   * manager has a default plugin.
   * @return null if there is no default else the class of the default
   * plugin.
   **/
  protected Class getDefaultPluginClass() {
    return null;
  }

  /**
   * Get the interface of the plugins for this manager. Override this
   * if this manager has plugins.
   * @return null if there are no plugins else the class of the plugin interface
   **/
  protected Class getPluginInterface() {
    return null;
  }

  /**
   * Install plugins
   **/
  private void installPlugins() {
    Class pluginInterface = getPluginInterface();
    if (pluginInterface == null) return; // This manager has no plugins
    Class defaultPluginClass = getDefaultPluginClass();
    for (Iterator i = theEventGenerator.getPluginSpecifications(pluginInterface); i.hasNext(); ) {
      PluginSpecification plugInSpecification = (PluginSpecification) i.next();
      try {
        Class plugInClass = plugInSpecification.getPluginClass();
        if (plugInClass == defaultPluginClass) continue; // Do later
        Plugin plugin = (Plugin) plugInClass.newInstance();
        boolean disabled = true; // All plugins disabled by default
        String parameter = plugInSpecification.getParameter();
        if (parameter != null) plugin.setParameter(parameter);
        if (theEventGenerator != null) plugin.setEventGenerator(theEventGenerator);
        addPlugin(plugin, disabled);
      } catch (Exception e) {
        System.err.println("Exception creating " + pluginInterface.getName() + ": " + e);
        e.printStackTrace();
      }
    }
    try {
      addPlugin((Plugin) defaultPluginClass.newInstance(), false);
    } catch (Exception e) {
      System.err.println("Exception creating " + defaultPluginClass + ": " + e);
      e.printStackTrace();
    }
  }

  public final ReportManagerGUI getGUI() {
    if (theReportManagerGUI == null) {
      theReportManagerGUI = new ReportManagerGUI(getTableModel(), this);
      Action filterAction = new AbstractAction("Filter...") {
        public void actionPerformed(ActionEvent e) {
          FilterGUI filterGUI = getFilterGUI();
          int ok =
          OptionPane.
          showOptionDialog(theReportManagerGUI,
                           filterGUI,
                           "Filter Properties",
                           OptionPane.OK_CANCEL_OPTION,
                           OptionPane.QUESTION_MESSAGE,
                           null, null, null);
          if (ok == OptionPane.OK_OPTION) {
            schedule.clearFilters();
            filterGUI.addFilters(schedule);
            tableModel.fireTableDataChanged();
          }
        }
      };
      theReportManagerGUI.addToToolBar(filterAction).setToolTipText("Edit report filter");
      if (plugins.size() > 0) {
        theReportManagerGUI.addToToolBar(getPluginButton());
      }
      List toolBarItems = getToolBarItems();
      for (Iterator i = toolBarItems.iterator(); i.hasNext(); ) {
        Object item = i.next();
        if (item instanceof Action) {
          theReportManagerGUI.addToToolBar((Action) item);
        } else if (item instanceof Component) {
          theReportManagerGUI.addToToolBar((Component) item);
        }
      }
    }
    return theReportManagerGUI;
  }

  public abstract String getGUITitle();

  public void save(Properties props, String prefix) {
    prefix += getGUITitle() + ".";
    props.setProperty(prefix + "autosend", isAutoSend() ? "true" : "false");
    props.setProperty(prefix + "autosendInvisible", isAutoSendInvisible() ? "true" : "false");
    getFilterGUI().save(props, prefix + "filter.");
    for (Iterator i = plugins.iterator(); i.hasNext(); ) {
      PluginItem plugInItem = (PluginItem) i.next();
      String piPrefix = prefix + "plugin." + plugInItem.thePlugin.getPluginName() + ".";
      props.setProperty(piPrefix + "selected", plugInItem.isSelected() ? "true" : "false");
      plugInItem.thePlugin.save(props, piPrefix);
    }
  }

  public static boolean getBooleanProperty(Properties props, String name) {
    return getBooleanProperty(props, name, false);
  }

  public static boolean getBooleanProperty(Properties props, String name, boolean dflt) {
    String value = props.getProperty(name);
    if (value == null) return dflt;
    return new Boolean(value).booleanValue();
  }

  public void restore(Properties props, String prefix) {
    prefix += getGUITitle() + ".";
    setAutoSend(getBooleanProperty(props, prefix + "autosend"));
    setAutoSendInvisible(getBooleanProperty(props, prefix + "autosendInvisible"));
    getFilterGUI().restore(props, prefix + "filter.");
    schedule.clearFilters();
    getFilterGUI().addFilters(schedule);
    for (Iterator i = plugins.iterator(); i.hasNext(); ) {
      PluginItem plugInItem = (PluginItem) i.next();
      String piPrefix = prefix + "plugin." + plugInItem.thePlugin.getPluginName() + ".";
      plugInItem.setSelected(getBooleanProperty(props, piPrefix + "selected"));
      plugInItem.thePlugin.restore(props, piPrefix);
    }
  }

  protected List getToolBarItems() {
    return new ArrayList();
  }

  private final FilterGUI getFilterGUI() {
    if (theFilterGUI == null) {
      theFilterGUI = createFilterGUI(theEventGenerator);
    }
    return theFilterGUI;
  }

  protected abstract FilterGUI createFilterGUI(EventGenerator anEventGenerator);

  protected void addToSchedule(Timed timed) {
    synchronized (schedule) {
      schedule.add(timed);
      Object key = timed.getKey();
      if (key != null) map.put(key, timed);
      scheduleChanged = true;
    }
  }

  protected void removeFromSchedule(Timed timed) {
    synchronized (schedule) {
      schedule.remove(timed);
      Object key = timed.getKey();
      if (key != null) map.remove(key);
      scheduleChanged = true;
    }
  }

//    protected final void resortSchedule(Timed timed) {
//      synchronized (schedule) {
//        schedule.remove(timed);
//        schedule.add(timed);
//        scheduleChanged = true;
//      }
//    }

  protected final void finishScheduleUpdate() {
    synchronized (schedule) {
      if (scheduleChanged) {
        if (tableModel != null) {
          tableModel.fireTableDataChanged();
        }
        scheduleChanged = false;
      }
    }
  }

  protected boolean isAutoSend() {
    return getGUI().isAutoSend();
  }

  protected void setAutoSend(boolean newAutoSend) {
    getGUI().setAutoSend(newAutoSend);
  }

  protected boolean isAutoSendInvisible() {
    return getGUI().isAutoSendInvisible();
  }

  protected void setAutoSendInvisible(boolean newAutoSend) {
    getGUI().setAutoSendInvisible(newAutoSend);
  }

  public final void advanceTime() {
    advanceTime(theEventGenerator.getExecutionTime());
  }

  public final void advanceTime(long newExecutionTime) {
    ArrayList expiredReports = new ArrayList();
    synchronized (schedule) {
      for (boolean done = false; !done; ) {
        done = true;
        Collection headSet = new ArrayList(rawSchedule.headSet(new Timed.Dummy(newExecutionTime + 1)));
        for (Iterator reports = headSet.iterator(); reports.hasNext(); ) {
          Timed object = (Timed) reports.next();
          if (isAutoSend() || object.isEnabled()) {
            expiredReports.add(object);
          }
        }
        if (expiredReports.size() > 0) {
          for (Iterator reports = expiredReports.iterator(); reports.hasNext(); ) {
            Timed object = (Timed) reports.next();
            removeFromSchedule(object); // Key may change invalidating sorted set.
            boolean didExpire = object.expired(newExecutionTime);
            if (!didExpire) {
              addToSchedule(object);
              done = false;     // May need to process this again
            } else {
              writeLog(object);
            }
          }
          expiredReports.clear();
        }
      }
    }
    finishScheduleUpdate();
  }

  // for readability
  private static final char TAB = '\t';
  private static final char QUOTE = '"';

  protected void writeLog(Timed timed) {
    if (logWriter != null) {
      StringBuffer buf = new StringBuffer();
      ManagerTableModel model = getTableModel();
      int[] columns = model.getLogColumns();
      for (int i = 0; i < columns.length; i++) {
        Object o = model.getValue(columns[i], timed);
        String val = o == null ? "" : o.toString();
        boolean needQuote = val.indexOf(TAB) >= 0;
        if ( i > 0) buf.append(TAB);
        if (needQuote) buf.append(QUOTE);
        buf.append(val);
        if (needQuote) buf.append(QUOTE);
      }
      logWriter.println(buf.toString());
    }
  }

  protected void writeLogHeader() {
    if (logWriter != null) {
      StringBuffer buf = new StringBuffer();
      ManagerTableModel model = getTableModel();
      int[] columns = model.getLogColumns();
      for (int i = 0; i < columns.length; i++) {
        String val = model.getColumnName(columns[i]);
        boolean needQuote = val.indexOf(TAB) >= 0;
        if ( i > 0) buf.append(TAB);
        if (needQuote) buf.append(QUOTE);
        buf.append(val);
        if (needQuote) buf.append(QUOTE);
      }
      logWriter.println(buf.toString());
    }
  }

  public final ManagerTableModel getTableModel() {
    if (tableModel == null) {
      tableModel = createTableModel();
    }
    return tableModel;
  }

  protected abstract ManagerTableModel createTableModel();

  protected final void postErrorMessage(String message) {
    theEventGenerator.postErrorMessage(getGUI(), message);
  }

  protected JButton getPluginButton() {    
    JButton pluginButton = new JButton("Plugins...");
    pluginButton.setToolTipText("Select and configure plugins");
    pluginButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        postPluginDialog();
      }
    });
    return pluginButton;
  }

  private static class PluginItem extends JCheckBox {
    public Plugin thePlugin;
    public PluginItem(Plugin aPlugin) {
      super(aPlugin.getPluginName());
      thePlugin = aPlugin;
    }
  }

  protected Iterator getEnabledPlugins() {
    final Iterator all = plugins.iterator();
    return new Iterator() {
      private PluginItem nextItem = null;
      public boolean hasNext() {
        while (nextItem == null) {
          if (!all.hasNext()) return false;
          nextItem = (PluginItem) all.next();
          if (nextItem.isSelected()) return true;
	  nextItem = null;
        }
        return false;
      }
      public Object next() {
        Object result = nextItem.thePlugin;
        nextItem = null;
        return result;
      }
      public void remove() {
        throw new UnsupportedOperationException("remove not supported");
      }
    };
  }

  private void postPluginDialog() {
    JPanel box = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    for (int i = 0, n = plugins.size(); i < n; i++) {
      PluginItem item = (PluginItem) plugins.get(i);
      if (item.thePlugin.getClass() == getDefaultPluginClass()) {
          item.disable();
      }
      gbc.gridy = i;
      gbc.gridx = 0;
      gbc.anchor = gbc.WEST;
      box.add(item, gbc);
      if (item.thePlugin.isConfigurable()) {
        final Plugin thePlugin = item.thePlugin;
        JButton configureButton = new JButton("Configure...");
        configureButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            thePlugin.configure(getGUI());
          }
        });
        gbc.gridx = 1;
        box.add(configureButton, gbc);
      }
    }
    OptionPane.
        showOptionDialog(getGUI(),
                         box,
                         "Select Plugins",
                         OptionPane.DEFAULT_OPTION,
                         OptionPane.QUESTION_MESSAGE,
                         null, null, null);
    firePluginChanged();
  }

  protected void firePluginChanged() {
  }

  protected void addPlugin(Plugin plugin, boolean disabled) {
    PluginItem item = new PluginItem(plugin);
    item.setSelected(!disabled);
    plugins.add(item);
  }

  protected static final int ANNOTATION_WIDTH = 120;

  protected abstract class ManagerTableModel extends EGTableModelBase implements EGTableModel {
    protected int[] logColumns; // Filled in by subclass

    private Timed[] rows = null;

    public int[] getLogColumns() {
      if (logColumns == null) logColumns = new int[0];
      return logColumns;
    }

    public int getRowCount() {
      synchronized (schedule) {
        if (rows == null) {
          rows = (Timed[]) schedule.toArray(new Timed[schedule.size()]);
        }
        return rows.length;
      }
    }

    public void fireTableDataChanged() {
      synchronized (schedule) {
        rows = null;
        super.fireTableDataChanged();
      }
    }

    protected Timed getRowObject(int row) {
      synchronized (schedule) {
        if (rows == null) return null;
        if (row < rows.length) {
          return rows[row];
        }
      }
      return null;
    }

    public final Object getValueAt(int row, int col) {
      return getValue(col, getRowObject(row));
    }
    
    public abstract Object getValue(int col, Object rowObject);
  }
}
