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
package org.cougaar.glm.execution.eg;

import org.cougaar.util.FilteredIterator;
import org.cougaar.util.ThemeFactory;
import org.cougaar.util.UnaryPredicate;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import org.cougaar.util.OptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.cougaar.glm.execution.common.*;
import org.cougaar.mlm.ui.planviewer.ConnectionHelper;

public class EventGenerator implements TimeGUI.Listener, TimeConstants {
  private ArrayList listeners = new ArrayList();
  private ArrayList scheduleManagers = new ArrayList();
  private HashMap clusters = new HashMap();
  private long theExecutionTime = -1;
  private double theExecutionRate = 1.0;
  private boolean isPaused = false;
  private TimeGUI theTimeGUI;
  private List timeScript = null;
  private int timeScriptCurrentIndex = -1;
  private boolean timeScriptSawHighRate = false;
  private long theSchedulingLookAhead = 10L * 60000L; // Look ahead ten minutes
  private InventoryReportManager theInventoryReportManager;
  private InventoryScheduleManager theInventoryScheduleManager;
  private FailureConsumptionReportManager theFailureConsumptionReportManager;
  private FailureConsumptionRateManager theFailureConsumptionRateManager;
  private TaskEventReportManager theTaskEventReportManager;

  private static class TimeScriptItem {
    long elapsedTime;
    long endTime;
    public TimeScriptItem(String line) {
      StringTokenizer tokens = new StringTokenizer(line, ";");
      endTime = new Date(tokens.nextToken().trim()).getTime();
      elapsedTime = Long.parseLong(tokens.nextToken().trim());
    }

    public TimeScriptItem(long endTime, long elapsedTime) {
      this.endTime = endTime;
      this.elapsedTime = elapsedTime;
    }

    public String toString() {
      return new Date(endTime).toString() + ";" + elapsedTime;
    }
  }

  public EventGenerator() {
    theInventoryReportManager =
      new InventoryReportManager(this);
    theInventoryScheduleManager =
      new InventoryScheduleManager(this, theInventoryReportManager);
    theFailureConsumptionReportManager =
      new FailureConsumptionReportManager(this);
    theFailureConsumptionRateManager =
      new FailureConsumptionRateManager(this, theFailureConsumptionReportManager);
    theTaskEventReportManager =
      new TaskEventReportManager(this);
    scheduleManagers.add(theInventoryScheduleManager);
    scheduleManagers.add(theInventoryReportManager);
    scheduleManagers.add(theFailureConsumptionRateManager);
    scheduleManagers.add(theFailureConsumptionReportManager);
    scheduleManagers.add(theTaskEventReportManager);
  }

  public void initialize() {
    initializeLogging();
    restore();
    String url = getContactURL();   // Get a toehold into the society
    try {
      getClusterInfo(url);           // The all the clusters
      Object[] handlers = {
        new EGExecutionTimeStatusHandler(this),
      };
      for (Iterator iter = clusters.values().iterator(); iter.hasNext(); ) {
        ClusterInfo clusterInfo = (ClusterInfo) iter.next();
        ExecutionListener listener = new ExecutionListener(clusterInfo, handlers);
        listener.addHandlers(theInventoryScheduleManager.getHandlers());
        listener.addHandlers(theInventoryReportManager.getHandlers());
        listener.addHandlers(theFailureConsumptionRateManager.getHandlers());
        listener.addHandlers(theFailureConsumptionReportManager.getHandlers());
        listener.addHandlers(theTaskEventReportManager.getHandlers());
	addListener(listener);
      }
    } catch (Exception re) {
      re.printStackTrace();
      displayErrorString(re.getMessage());
      System.exit(1);
    }
  }

  private void initializeLogging() {
    String prefix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    for (Iterator managers = scheduleManagers.iterator(); managers.hasNext(); ) {
      ScheduleManager manager = (ScheduleManager) managers.next();
      try {
        manager.enableLog(prefix); // Probably does nothing
      } catch (IOException ioe) {
        System.err.println("Failed to open log file for " + manager.getGUITitle());
        ioe.printStackTrace();
      }
    }
  }

  public void setTimeScript(List newScript) {
      if (newScript == null) {
          cancelTimeScript();
      } else {
          timeScript = newScript;
          timeScriptCurrentIndex = -1;
          timeScriptSawHighRate = false;
          getTimeGUI().enableControls();
          getTimeGUI().play();
      }
  }

  private void cancelTimeScript() {
    timeScript = null;
    getTimeGUI().disableControls();
  }

  private static class RatePair {
    public String theName;
    public double theRate;
    public RatePair(String aName, double aRate) {
      theName = aName;
      theRate = aRate;
    }
    public String toString() {
      return theName;
    }
  }

  private static final int DEFAULT_RATE = 1;

  private static final RatePair[] rates = {
    new RatePair("Paused", 0.0),
    new RatePair("Normal", 1.0),
    new RatePair("10X", 10.0),
    new RatePair("Hour/Minute", 60.0),
    new RatePair("Day/Minute", 24.0 * 60.0),
    new RatePair("Week/Minute", 7.0 * 24.0 * 60.0),
    new RatePair("Month/Minute", (365.24 / 12.0) * 24.0 * 60.0),
  };

  private JComboBox pauseButton = new JComboBox(rates);
  private JMenu executionRateMenu = new JMenu("Execution Rate (Normal)");

  private static class AdvanceItem {
    String label;
    long advancement;
    public AdvanceItem(String label, long advancement) {
      this.label = label;
      this.advancement = advancement;
    }
  }

  private static AdvanceItem[] advancements = {
    new AdvanceItem("10 minutes", 10 * ONE_MINUTE),
    new AdvanceItem("1 hour",          ONE_HOUR),
    new AdvanceItem("6 hours",     6 * ONE_HOUR),
    new AdvanceItem("1 day",           ONE_DAY),
    new AdvanceItem("1 week",          ONE_WEEK),
    new AdvanceItem("1 month",         ONE_MONTH),
  };

  private class PauseButtonListener implements ActionListener {
    private double oldRate= ((RatePair) rates[DEFAULT_RATE]).theRate;
    private int oldIndex = DEFAULT_RATE;
    public void actionPerformed(ActionEvent e) {
      Object object = pauseButton.getSelectedItem();
      int newIndex = pauseButton.getSelectedIndex();
      if (object instanceof RatePair) {
        RatePair rate = (RatePair) object;
        userSetRate(rate.theRate);
        oldIndex = newIndex;
        oldRate = rate.theRate;
      } else {
        String enteredRate = (String) object;
        try {
          double newRate = Double.parseDouble(enteredRate);
          if (newRate < 0.0) throw new NumberFormatException();
          userSetRate(newRate);
          oldIndex = 0;
          oldRate = newRate;
        } catch (Exception ex) {
          postErrorMessage(pauseButton, "Value must be non-negative numeric");
          if (oldIndex == 0) {
            pauseButton.setSelectedItem("" + oldRate);
          } else {
            pauseButton.setSelectedIndex(oldIndex);
          }
        }
      }
    }
    public double getRate() {
      return oldRate;
    }
  }

  private PauseButtonListener pauseButtonListener = new PauseButtonListener();

  private JPanel theGUI;

  public JComponent getGUI() {
    if (theGUI == null) {
      theGUI = new JPanel(new BorderLayout());
      JTabbedPane tabbedPane = new JTabbedPane();
      theGUI.add(tabbedPane);
      for (Iterator managers = scheduleManagers.iterator(); managers.hasNext(); ) {
        ScheduleManager manager = (ScheduleManager) managers.next();
        tabbedPane.addTab(manager.getGUITitle(), manager.getGUI());
      }
//      JPanel top = new JPanel();
//      top.add(getJMenuBar());
//      theGUI.add(top, BorderLayout.NORTH);
    }
    return theGUI;
  }

  public JMenuBar getJMenuBar() {
    JMenuBar top = new JMenuBar();

    JMenu optionsMenu = getOptionsMenu();
    if (false) {
      JLabel pauseLabel = new JLabel("Execution Rate: ");
      pauseLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
      pauseLabel.setForeground(Color.black);
      pauseButton.setToolTipText("Select execution rate");
      pauseButton.setEditable(false); // Doesn't work very well
      pauseButton.setSelectedIndex(DEFAULT_RATE);
      pauseButton.addActionListener(pauseButtonListener);
      setRate(((RatePair) rates[DEFAULT_RATE]).theRate);
    } else {
      ButtonGroup executionRateGroup = new ButtonGroup();
      for (int i = 0; i < rates.length; i++) {
        final RatePair rate = rates[i];
        final JRadioButtonMenuItem item = new JRadioButtonMenuItem(rates[i].theName);
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (item.isSelected()) {
              userSetRate(rate.theRate);
              executionRateMenu.setLabel("Execution Rate (" + rate.theName + ")");
            }
          }
        });
        executionRateGroup.add(item);
        executionRateMenu.add(item);
        if (i == DEFAULT_RATE) item.setSelected(true);
      }
    }
    top.add(optionsMenu);
//      top.add(getTimeGUI());
//      top.add(pauseLabel);
//      top.add(pauseButton);
    top.add(executionRateMenu);
    top.add(getAdvanceMenu());
    return top;
  }

  private JMenu getAdvanceMenu() {
    final JMenu advanceMenu = new JMenu("Advance Time");/* {
      public Dimension getMaximumSize() {
        return getPreferredSize();
      }
      };*/
    advanceMenu.setToolTipText("Select rapid time advancement");
    for (int i = 0; i < advancements.length; i++) {
      final AdvanceItem item = advancements[i];
      advanceMenu.add(new AbstractAction(item.label) {
        public void actionPerformed(ActionEvent e) {
          userAdvanceExecutionTime(item.advancement);
        }
      });
    }
    advanceMenu.add(new AbstractAction("To Time...") {
      public void actionPerformed(ActionEvent e) {
        advanceExecutionTimeTo();
      }
    });
    advanceMenu.add(new AbstractAction("Run Time Script...") {
      public void actionPerformed(ActionEvent e) {
        runTimeScript();
      }
    });
    return advanceMenu;
  }

  private JMenu getOptionsMenu() {
    JMenu optionsMenu = new JMenu("Options");
    optionsMenu.add(new AbstractAction("Scheduling Lookahead...", null) {
      public void actionPerformed(ActionEvent e) {
        setSchedulingLookAhead();
      }
    });
    return optionsMenu;
  }

  public JToolBar getJToolBar() {
    JToolBar top = new JToolBar();

    pauseButton.setEditable(false); // Doesn't work very well
    pauseButton.setSelectedIndex(DEFAULT_RATE);
    pauseButton.addActionListener(pauseButtonListener);

    setRate(((RatePair) rates[DEFAULT_RATE]).theRate);
    top.add(getTimeGUI());
    top.add(pauseButton);
    top.add(getAdvanceMenu());
    return top;
  }

  public TimeGUI getTimeGUI() {
    if (theTimeGUI == null) {
      theTimeGUI = new TimeGUI(this);
      updateTimeGUI();
    }
    return theTimeGUI;
  }

    public void timePlay() {
    }

    public void timePause() {
    }

    public void timeStop() {
        cancelTimeScript();
    }

  public void updateTimeGUI() {
    if (theTimeGUI != null && theExecutionTime > 0L) {
      theTimeGUI.setTime(theExecutionTime, theExecutionRate);
    }
  }

  public static void postErrorMessage(Component component, String message) {
    OptionPane.showOptionDialog(component,
                                message,
                                "Error",
                                OptionPane.DEFAULT_OPTION,
                                OptionPane.ERROR_MESSAGE,
                                null, null, null);
  }

  private void runTimeScript() {
    JFileChooser chooser = new JFileChooser(".");
    chooser.setDialogTitle("Select Time Script");
    if (chooser.showOpenDialog(getGUI()) == JFileChooser.APPROVE_OPTION) {
      setTimeScript(createTimeScript(chooser.getSelectedFile()));
      checkTimeScript();
    }
  }
    
  public void advanceExecutionTimeTo() {
    JPanel message = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.anchor = gbc.WEST;
    gbc.gridy = 1;
    message.add(new JLabel("Time to advance to: "), gbc);
    gbc.gridy = 2;
    message.add(new JLabel("Transition interval (seconds): "), gbc);
    gbc.gridx = 1;
    gbc.fill = gbc.HORIZONTAL;
    gbc.weightx = 1.0;
    JTextField advancement = new JTextField(30);
    advancement.setText(new EGDate(theExecutionTime).toString());
    gbc.gridy = 1;
    message.add(advancement, gbc);
    JTextField interval = new JTextField(30);
    interval.setText("30");
    gbc.gridy = 2;
    message.add(interval, gbc);
    int opt = OptionPane.showOptionDialog(getGUI(),
                                          message,
                                          "Time Advancement",
                                          OptionPane.OK_CANCEL_OPTION,
                                          OptionPane.QUESTION_MESSAGE,
                                          null, null, null);
    if (opt != OptionPane.OK_OPTION) return;
    try {
      long newTime = new EGDate(advancement.getText()).getTime();
      long transitionInterval = Long.parseLong(interval.getText()) * 1000L;
      userSetExecutionTime(newTime, transitionInterval);
    } catch (IllegalArgumentException iae) {
      postErrorMessage(theTimeGUI, "Input not recognized as a date");
    }
  }

  private void userAdvanceExecutionTime(long advancement) {
    cancelTimeScript();
    send(SetExecutionTime.createRelativeInstance(advancement,
                                                 pauseButtonListener.getRate(),
                                                 0L));
  }

  private void userSetExecutionTime(long newTime, long transitionInterval) {
    cancelTimeScript();
    send(SetExecutionTime.createAbsoluteInstance(newTime,
                                                 pauseButtonListener.getRate(),
                                                 transitionInterval));
  }
    
  private void userSetRate(double newRate) {
    cancelTimeScript();
    setRate(newRate);
  }

  private void setRate(double newRate) {
    send(SetExecutionTime.createRateChangeInstance(newRate));
  }

  public void send(SetExecutionTime params) {
    setPaused(params.theExecutionRate == 0.0);
    try {
      Iterator iter = clusters.values().iterator();
      if (iter.hasNext()) {
        ClusterInfo clusterInfo = (ClusterInfo) iter.next();
        TimeControlListener listener =
          new TimeControlListener(clusterInfo, params);
        startListener(listener);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public boolean isPaused() {
    return isPaused;
  }

  public void setPaused(boolean newPaused) {
    isPaused = newPaused;
    if (newPaused){
      if (pauseButton.getSelectedIndex() != 0) pauseButton.setSelectedIndex(0);
    } else {
      updateExecutionTime();
    }
  }

  public void addListener(Listener l) {
    listeners.add(l);
  }

  public void startListeners() {
    for (Iterator i = listeners.iterator(); i.hasNext(); ) {
      startListener((Listener) i.next());
    }
  }

  public void startListener(Listener l) {
    l.start();
  }

  public InventoryReportListener createInventoryReportListener(String source,
                                                               Object[] handlers,
                                                               String itemIdentification,
                                                               long aReportDate)
    throws IOException
  {
    ClusterInfo clusterInfo = (ClusterInfo) clusters.get(source);
    if (clusterInfo == null) {
      System.err.println("No info for " + source);
      return null;
    }
    return new InventoryReportListener(clusterInfo, handlers, itemIdentification, aReportDate);
  }

  public EventMonitorListener createEventMonitorListener(String source,
                                                         Object[] handlers,
                                                         Report aReport)
    throws IOException
  {
    ClusterInfo clusterInfo = (ClusterInfo) clusters.get(source);
    if (clusterInfo == null) {
      System.err.println("No info for " + source);
      return null;
    }
    return new EventMonitorListener(clusterInfo, handlers, aReport);
  }

  public ConstraintElementListener createConstraintElementListener(String source,
                                                                   Object[] handlers,
                                                                   TaskConstraintsRequest aRequest)
    throws IOException
  {
    ClusterInfo clusterInfo = (ClusterInfo) clusters.get(source);
    if (clusterInfo == null) {
      System.err.println("No info for " + source);
      return null;
    }
    return new ConstraintElementListener(clusterInfo, handlers, aRequest);
  }

  /**
   * Before sending the first request, ask the user for the location
   * of a cluster. Get the list of URLs and clusters from there.
   **/
  
  private String getContactURL() {
    String s = (String) OptionPane.showInputDialog(getGUI(),
      "Enter location of a cluster Log Plan Server in the form host:port",
                   "Cluster Location",
                    OptionPane.INFORMATION_MESSAGE,
                    null, null, "localhost:5555");
    if (s == null)
      System.exit(0); // if we don't know where the clusters are located, quit
    s = s.trim();
    if (s.length() == 0) {
      return "http://localhost:5555/";
    }
    int i = s.indexOf(":");
    if (i == -1) {
      return "http://" + s + ":5555/";
    }
    return "http://" + s + "/";
  }

  private void getClusterInfo(String url) {
    try {
      ConnectionHelper connection = new ConnectionHelper(url);
      Hashtable clusterURLs = connection.getClusterIdsAndURLs();
      if (clusterURLs == null) {
        throw new RuntimeException("Unable to obtain cluster identifier to URL mappings.");
      }
      for (Iterator iter = clusterURLs.keySet().iterator(); iter.hasNext(); ) {
        String clusterName = (String) iter.next();
        String clusterURL = (String) clusterURLs.get(clusterName);
        ClusterInfo clusterInfo = new ClusterInfo(clusterName, clusterURL);
        clusters.put(clusterName, clusterInfo);
      }
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      clusters = null;
      throw new RuntimeException(e.toString());
    }
  }

  /**
   * Update the current execution time and rate.
   **/
  public void setExecutionTime(String theSource, ExecutionTimeStatus anExecutionTimeStatus) {
    ClusterInfo clusterInfo = (ClusterInfo) clusters.get(theSource);
    clusterInfo.theExecutionTimeStatus = anExecutionTimeStatus;
    updateExecutionTime();
  }

  /**
   * Called after a cluster's time status has changed to decide what
   * the effect of that change is to be on the event generator.
   * Generally, we accept the latest reported execution time, and use
   * its rate.
   **/
  private void updateExecutionTime() {
    synchronized (clusters) {
      ExecutionTimeStatus latestExecutionTimeStatus = null;
      for (Iterator i = clusters.values().iterator(); i.hasNext(); ) {
        ClusterInfo clusterInfo = (ClusterInfo) i.next();
        ExecutionTimeStatus thisExecutionTimeStatus = clusterInfo.theExecutionTimeStatus;
        if (thisExecutionTimeStatus == null) {
	  continue;             // Have not yet heard from this cluster
        }
        if (latestExecutionTimeStatus == null) {
          latestExecutionTimeStatus = thisExecutionTimeStatus;
          continue;
        }
        long thisTime = thisExecutionTimeStatus.theExecutionTime;
        long thatTime = latestExecutionTimeStatus.theExecutionTime;
        if (thisTime != thatTime) {
          if (thisTime > thatTime) {
            latestExecutionTimeStatus = thisExecutionTimeStatus;
          }
          continue;
        }
        double thisRate = thisExecutionTimeStatus.theExecutionRate;
        double thatRate = latestExecutionTimeStatus.theExecutionRate;
        if (thisRate < thatRate) {
          latestExecutionTimeStatus = thisExecutionTimeStatus;
        }
      }
      if (latestExecutionTimeStatus == null) {
        return;                 // This probably never happens
      }
      theExecutionTime = latestExecutionTimeStatus.theExecutionTime;
      theExecutionRate = latestExecutionTimeStatus.theExecutionRate;
      checkTimeScript();
    }
    updateTimeGUI();
    if (!isPaused()) {
      advanceManagerTime();
    }
  }

  /**
   * Interpret the time script. If there is no time script, just
   * return. If this is there is no currently executing item, execute
   * the next (first) applicable item. If the there is a currently
   * executing item, see if the advancement is done and if so, execute
   * the next applicable item. An item is done if its endTime has been
   * reached or if the the execution rate has returned to 1.0 and the
   * endTime has been nearly reached.
   **/
  private void checkTimeScript() {
    if (timeScript == null) return;
    if (theExecutionRate != 1.0) timeScriptSawHighRate = true;
    if (timeScriptCurrentIndex < 0) {
      nextTimeScriptItem();
    } else {
      TimeScriptItem item = (TimeScriptItem) timeScript.get(timeScriptCurrentIndex);
      long timeRemaining = item.endTime - theExecutionTime;
      if (timeRemaining <= 0L || timeScriptSawHighRate && theExecutionRate == 1.0) {
        nextTimeScriptItem();
      }
    }
  }

    private void nextTimeScriptItem() {
        for (; timeScriptCurrentIndex < timeScript.size(); timeScriptCurrentIndex++) {
            if (timeScriptCurrentIndex < 0) continue; // Get off the snide
            TimeScriptItem item = (TimeScriptItem) timeScript.get(timeScriptCurrentIndex);
            System.out.println("nextTimeScriptItem: " + item);
            long timeRemaining = item.endTime - theExecutionTime;
            if (timeRemaining >= 0L) { // Do this one
                final SetExecutionTime set =
                    SetExecutionTime.createRelativeInstance(timeRemaining, 1.0, item.elapsedTime);
                new Thread("Time Script Runner") {
                    public void run() {
                        send(set);
                    }
                }.start();
                return;                   // Done for now.
            }
            timeScriptSawHighRate = false; // Done with this item
        }
        cancelTimeScript();         // Script is finished
    }

  public ScheduleManager findScheduleManager(Class managerClass) {
    for (Iterator i = scheduleManagers.iterator(); i.hasNext(); ) {
      ScheduleManager m = (ScheduleManager) i.next();
      if (m.getClass() == managerClass) return m;
    }
    return null;
  }

  private void advanceManagerTime() {
    for (Iterator i = scheduleManagers.iterator(); i.hasNext(); ) {
      ScheduleManager m = (ScheduleManager) i.next();
      m.advanceTime(theExecutionTime);
    }
  }

  public void setSchedulingLookAhead() {
    String lookAhead = (String)
      OptionPane.showInputDialog(theTimeGUI,
                                 "Enter scheduling lookahead (minutes): ",
                                 "Scheduling Lookahead",
                                 OptionPane.QUESTION_MESSAGE,
                                 null, null, new Long(getSchedulingLookAhead() / ONE_MINUTE));
    try {
      long newTime = new Long(lookAhead).longValue() * ONE_MINUTE;
      setSchedulingLookAhead(newTime);
    } catch (IllegalArgumentException iae) {
      postErrorMessage(theTimeGUI, "Input not recognized as a number");
    }
  }

  public void setSchedulingLookAhead(long newLookAhead) {
    theSchedulingLookAhead = newLookAhead;
  }

  public long getSchedulingLookAhead() {
    return theSchedulingLookAhead;
  }

  public long getExecutionTime() {
    if (theExecutionTime <= 0L) {
      throw new RuntimeException("theExecutionTime has not been set");
    }
    return theExecutionTime;
  }

  public double getExecutionRate() {
    return theExecutionRate;
  }

  private void displayErrorString(String reply) {
      OptionPane.showOptionDialog(null, reply, reply, 
                                  OptionPane.DEFAULT_OPTION,
                                  OptionPane.ERROR_MESSAGE,
                                  null, null, null);
  }

  private static void setGUIDefaults() {
    ThemeFactory.establishMetalTheme();
    EGTableModelBase.setWidths();
  }

  public String[] getClusterNames() {
    String[] result = new String[clusters.size()];
    Iterator values = clusters.values().iterator();
    for (int i = 0; i < result.length; i++) {
      result[i] = ((ClusterInfo) values.next()).theClusterName;
    }
    return result;
  }

  public void restore() {
    String prefix = getClass().getName() + ".";
    Properties props = new Properties();
    try {
      props.load(new FileInputStream("eg.properties"));
      for (Iterator managers = scheduleManagers.iterator(); managers.hasNext(); ) {
        ScheduleManager manager = (ScheduleManager) managers.next();
        manager.restore(props, prefix);
      }
    } catch (IOException ioe) {
    }
  }
    
  public boolean shutDown() {
    String prefix = getClass().getName() + ".";
    Properties props = new Properties();
    for (Iterator managers = scheduleManagers.iterator(); managers.hasNext(); ) {
      ScheduleManager manager = (ScheduleManager) managers.next();
      manager.save(props, prefix);
    }
    try {
      props.store(new FileOutputStream("eg.properties"), "Event Generator Properties");
    } catch (IOException ioe) {
      postErrorMessage(getGUI(), "Failed to write eg.properties" + ioe);
    }
    return true;
  }

  private static HashSet plugIns = new HashSet();

  /**
   * Add plugins found in a Properties object.
   **/
  private static void addPlugins(Properties props) {
    String prefix = EventGenerator.class.getName() + ".plugin.";
    for (Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
      String key = (String) e.nextElement();
      String line = "";
      if (key.startsWith(prefix)) {
        try {
          line = props.getProperty(key);
          addPlugin(line);
        } catch (ClassNotFoundException cnfe) {
          System.err.println("Plugin class not found: " + line);
        }
      }
    }
  }

  /**
   * Add plugins found listed in a file
   **/
  private static void addPlugins(String filename) {
    int n = 0;
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      try {
        String line;
        while ((line = reader.readLine()) != null) {
          try {
            addPlugin(line);
          } catch (ClassNotFoundException cnfe) {
            System.err.println("Plugin class not found: " + line);
          }
        }
      } finally {
        reader.close();
      }
    } catch (Exception e) {
      System.err.println("addPlugins:" + e);
    }
  }

  private static void addPlugin(String plugInSpecification) throws ClassNotFoundException {
    plugIns.add(new PluginSpecification(plugInSpecification));
  }

  /**
   * Get iterator of all plugin specifications that implement the given interface.
   **/
  public Iterator getPluginSpecifications(final Class interfaceClass) {
    return new FilteredIterator(plugIns.iterator(), new UnaryPredicate() {
      public boolean execute(Object o) {
        return interfaceClass.isAssignableFrom(((PluginSpecification) o).getPluginClass());
      }
    });
  }

  private static List createTimeScript(File file) {
    List script = new ArrayList();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      try {
        String line;
        TimeScriptItem prev = null;
        while ((line = reader.readLine()) != null) {
          try {
            TimeScriptItem item = new TimeScriptItem(line);
            System.out.println("createTimeScript: " + item);
            if (prev != null && item.endTime <= prev.endTime) {
              throw new RuntimeException("Time script start times not monotonic increasing");
            }
            script.add(item);
            prev = item;
          } catch (RuntimeException cnfe) {
            System.err.println(cnfe + ": " + line);
          }
        }
        if (script.isEmpty()) return null;
        return script;
      } finally {
        reader.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * If org.cougaar.useBootstrapper is true, will search for installed jar files
   * in order to load all classes.  Otherwise, will rely solely on classpath.
   *
   **/
  public static void main(String[] args) {
    if ("true".equals(System.getProperty("org.cougaar.useBootstrapper", "true"))) {
      org.cougaar.bootstrap.Bootstrapper.launch(EventGenerator.class.getName(), args);
    } else {
      launch(args);
    }
  }

  private static class TimeGUIListener
    extends MouseAdapter
    implements MouseListener, MouseMotionListener
  {
    JComponent component;
    JComponent container;
    Point startLocation;
    Point startPoint;

    public TimeGUIListener(JComponent component, JComponent container) {
      this.component = component;
      this.container = container;
    }
    public void mousePressed(MouseEvent e) {
      startPoint = e.getPoint();
      SwingUtilities.convertPointToScreen(startPoint, e.getComponent());
      startLocation = component.getLocation();
    }
    private void moveTo(MouseEvent e) {
      Point newPoint = e.getPoint();
      SwingUtilities.convertPointToScreen(newPoint, e.getComponent());
      int dx = newPoint.x - startPoint.x;
      int dy = newPoint.y - startPoint.y;
      component.setLocation(startLocation.x + dx, startLocation.y + dy);
      forceComponentInWindow(component, container);
    }

    public void mouseDragged(MouseEvent e) {
      if (startPoint != null) moveTo(e);
    }
    public void mouseClicked(MouseEvent e) {
      startPoint = null;
    }
    public void mouseReleased(MouseEvent e) {
      if (startPoint != null) {
        moveTo(e);
        startPoint = null;
      }
    }
    public void mouseMoved(MouseEvent e) {
    }
  }

  private static void forceComponentInWindow(JComponent tp, JComponent lp) {
    Dimension d = lp.getSize();
    Rectangle r = tp.getBounds();
    boolean changed = false;
    if (r.x < 0) {
      r.x = 0;
      changed = true;
    }
    if (r.x + r.width > d.width) {
      r.x = d.width - r.width;
      changed = true;
    }
    if (r.y < 0) {
      r.y = 0;
      changed = true;
    }
    if (r.y + r.height > d.height) {
      r.y = d.height - r.height;
      changed = true;
    }
    if (changed) tp.setLocation(r.x, r.y);
  }

  static public void launch(String[] args) {
    List timeScript = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-demo")) {
        setGUIDefaults();
        continue;
      }
      if (args[i].equals("-plugins")) {
        addPlugins(args[++i]);
        continue;
      }
      if (args[i].equals("-time")) {
        timeScript = createTimeScript(new File(args[++i]));
        continue;
      }
      System.err.println("Unrecognized argument: " + args[i]);
    }
    addPlugins(System.getProperties()); // Add plugins specified as system properties
    final EventGenerator eventGenerator = new EventGenerator();
    JFrame frame = new JFrame("COUGAAR Event Generator");
    frame.setContentPane(eventGenerator.getGUI());
    frame.setJMenuBar(eventGenerator.getJMenuBar());
    eventGenerator.setTimeScript(timeScript);
    final JComponent timeGUIPanel = eventGenerator.getTimeGUI();
    timeGUIPanel.setBackground(new Color(250, 240, 192));
    timeGUIPanel.setBorder(BorderFactory.createLoweredBevelBorder());
    final JLayeredPane lp = frame.getLayeredPane();
    final TimeGUIListener listener = new TimeGUIListener(timeGUIPanel, lp);
    lp.add(timeGUIPanel, new Integer(1000));
    timeGUIPanel.addMouseListener(listener);
    timeGUIPanel.addMouseMotionListener(listener);
    lp.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        forceComponentInWindow(timeGUIPanel, lp);
      }
    });
    timeGUIPanel.setSize(timeGUIPanel.getPreferredSize());
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (eventGenerator.shutDown()) {
          System.exit(0);
        }
      }
    });
    forceComponentInWindow(timeGUIPanel, lp);
    frame.pack();
    frame.setState(frame.ICONIFIED);
    timeGUIPanel.setLocation(10000, 0);
    frame.show();
    eventGenerator.initialize();
    frame.setState(frame.NORMAL);
    eventGenerator.startListeners();
  }
}
