package org.cougaar.domain.glm.execution.eg;

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
import org.cougaar.domain.glm.execution.common.*;

/**
 * Keeps track of the inventory report schedules and decide if/when to
 * generate a report.
 **/
public abstract class ManagerBase {
  protected EventGenerator theEventGenerator;
  private TreeSet rawSchedule = new TreeSet();
  protected FilteredSet schedule = new FilteredSet(rawSchedule);
  private boolean scheduleChanged = false;
  protected EGTableModel tableModel = null;
  protected HashMap map = new HashMap();
  private FilterGUI theFilterGUI = null;
  private ReportManagerGUI theReportManagerGUI;
  private List plugins = new ArrayList();

  public ManagerBase(EventGenerator anEventGenerator) {
    theEventGenerator = anEventGenerator;
    installPlugins();
  }

  /**
   * Get the class of the default plugin. Override this if this
   * manager has a default plugin.
   * @return null if there is no default else the class of the default
   * plugin.
   **/
  protected Class getDefaultPlugInClass() {
    return null;
  }

  /**
   * Get the interface of the plugins for this manager. Override this
   * if this manager has plugins.
   * @return null if there are no plugins else the class of the plugin interface
   **/
  protected Class getPlugInInterface() {
    return null;
  }

  /**
   * Install plugins
   **/
  private void installPlugins() {
    Class pluginInterface = getPlugInInterface();
    if (pluginInterface == null) return; // This manager has no plugins
    Class defaultPlugInClass = getDefaultPlugInClass();
    for (Iterator i = theEventGenerator.getPlugInSpecifications(pluginInterface); i.hasNext(); ) {
      PlugInSpecification plugInSpecification = (PlugInSpecification) i.next();
      try {
        Class plugInClass = plugInSpecification.getPlugInClass();
        if (plugInClass == defaultPlugInClass) continue; // Do later
        PlugIn plugin = (PlugIn) plugInClass.newInstance();
        boolean disabled = true; // All plugins disabled by default
        String parameter = plugInSpecification.getParameter();
        if (parameter != null) plugin.setParameter(parameter);
        if (theEventGenerator != null) plugin.setEventGenerator(theEventGenerator);
        addPlugIn(plugin, disabled);
      } catch (Exception e) {
        System.err.println("Exception creating " + pluginInterface.getName() + ": " + e);
      }
    }
    try {
      addPlugIn((PlugIn) defaultPlugInClass.newInstance(), false);
    } catch (Exception e) {
      System.err.println("Exception creating " + defaultPlugInClass + ": " + e);
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
        theReportManagerGUI.addToToolBar(getPlugInButton());
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
      PlugInItem plugInItem = (PlugInItem) i.next();
      String piPrefix = prefix + "plugin." + plugInItem.thePlugIn.getPlugInName() + ".";
      props.setProperty(piPrefix + "selected", plugInItem.isSelected() ? "true" : "false");
      plugInItem.thePlugIn.save(props, piPrefix);
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
      PlugInItem plugInItem = (PlugInItem) i.next();
      String piPrefix = prefix + "plugin." + plugInItem.thePlugIn.getPlugInName() + ".";
      plugInItem.setSelected(getBooleanProperty(props, piPrefix + "selected"));
      plugInItem.thePlugIn.restore(props, piPrefix);
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
            removeFromSchedule(object);
            expiredReports.add(object);
          }
        }
        if (expiredReports.size() > 0) {
          for (Iterator reports = expiredReports.iterator(); reports.hasNext(); ) {
            Timed object = (Timed) reports.next();
            boolean didExpire = object.expired(newExecutionTime);
            if (!didExpire) {
              addToSchedule(object);
              done = false;     // May need to process this again
            }
          }
          expiredReports.clear();
        }
      }
    }
    finishScheduleUpdate();
  }

  public final EGTableModel getTableModel() {
    if (tableModel == null) {
      tableModel = createTableModel();
    }
    return tableModel;
  }

  protected abstract EGTableModel createTableModel();

  protected final void postErrorMessage(String message) {
    theEventGenerator.postErrorMessage(getGUI(), message);
  }

  protected JButton getPlugInButton() {    
    JButton pluginButton = new JButton("Plugins...");
    pluginButton.setToolTipText("Select and configure plugins");
    pluginButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        postPlugInDialog();
      }
    });
    return pluginButton;
  }

  private static class PlugInItem extends JCheckBox {
    public PlugIn thePlugIn;
    public PlugInItem(PlugIn aPlugIn) {
      super(aPlugIn.getPlugInName());
      thePlugIn = aPlugIn;
    }
  }

  protected Iterator getEnabledPlugIns() {
    final Iterator all = plugins.iterator();
    return new Iterator() {
      private PlugInItem nextItem = null;
      public boolean hasNext() {
        while (nextItem == null) {
          if (!all.hasNext()) return false;
          nextItem = (PlugInItem) all.next();
          if (nextItem.isSelected()) return true;
	  nextItem = null;
        }
        return false;
      }
      public Object next() {
        Object result = nextItem.thePlugIn;
        nextItem = null;
        return result;
      }
      public void remove() {
        throw new UnsupportedOperationException("remove not supported");
      }
    };
  }

  private void postPlugInDialog() {
    JPanel box = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    for (int i = 0, n = plugins.size(); i < n; i++) {
      PlugInItem item = (PlugInItem) plugins.get(i);
      if (item.thePlugIn.getClass() == getDefaultPlugInClass()) {
          item.disable();
      }
      gbc.gridy = i;
      gbc.gridx = 0;
      gbc.anchor = gbc.WEST;
      box.add(item, gbc);
      if (item.thePlugIn.isConfigurable()) {
        final PlugIn thePlugIn = item.thePlugIn;
        JButton configureButton = new JButton("Configure...");
        configureButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            thePlugIn.configure(getGUI());
          }
        });
        gbc.gridx = 1;
        box.add(configureButton, gbc);
      }
    }
    OptionPane.
        showOptionDialog(getGUI(),
                         box,
                         "Select PlugIns",
                         OptionPane.DEFAULT_OPTION,
                         OptionPane.QUESTION_MESSAGE,
                         null, null, null);
    firePlugInChanged();
  }

  protected void firePlugInChanged() {
  }

  protected void addPlugIn(PlugIn plugin, boolean disabled) {
    PlugInItem item = new PlugInItem(plugin);
    item.setSelected(!disabled);
    plugins.add(item);
  }

  protected abstract class ManagerTableModel extends EGTableModelBase implements EGTableModel {
    private Timed[] rows = null;

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
  }
}
