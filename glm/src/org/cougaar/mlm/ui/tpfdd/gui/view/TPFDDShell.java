
/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

package org.cougaar.mlm.ui.tpfdd.gui.view;


import java.security.AccessControlException;

import java.util.Date;

import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JSeparator;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JApplet;
import javax.swing.JOptionPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Vector;
import java.util.Iterator;

import org.cougaar.mlm.ui.psp.transportation.PSP_Itinerary;

import org.cougaar.mlm.ui.tpfdd.util.PathString;
import org.cougaar.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.mlm.ui.tpfdd.util.Debug;
import org.cougaar.mlm.ui.tpfdd.util.ExceptionTools;

import org.cougaar.mlm.ui.tpfdd.gui.model.SimpleProducer;

import org.cougaar.mlm.ui.tpfdd.aggregation.Server;
import org.cougaar.mlm.ui.tpfdd.aggregation.ServerCommands;

import org.cougaar.mlm.ui.tpfdd.producer.ThreadedProducer;
import org.cougaar.mlm.ui.tpfdd.producer.LogPlanProducer;
import org.cougaar.mlm.ui.tpfdd.producer.ItineraryProducer;
import org.cougaar.mlm.ui.tpfdd.producer.PSPClientConfig;
import org.cougaar.mlm.ui.tpfdd.producer.ClusterCache;


public class TPFDDShell extends JApplet implements ActionListener,
  ServerCommands
{
    private JMenu ivjactionMenu = null;
    private JMenu ivjclusterMenu = null;
    private JMenuItem ivjtaskItem = null;
    private JMenuItem ivjnewViewItem = null;
    private JMenuItem ivjretryAggregationInitItem = null;
    private JMenuItem ivjreloadAggregationItem = null;
    private JMenuBar ivjTPFDDShellMenuBar = null;
    private JCheckBoxMenuItem ivjmessageItem = null;
    private JCheckBoxMenuItem ivjganttItem = null;
    private JSeparator ivjpanelMenuSeparator = null;
    private JCheckBoxMenuItem ivjlogPlanItem = null;
    private JMenu ivjpanelMenu = null;
    private JRadioButtonMenuItem ivjstackItem = null;
    private JRadioButtonMenuItem ivjtileItem = null;
    private JPanel ivjcontainerPanel = null;
    private JPanel ivjmessageTab = null;
    private JPanel ivjganttTab = null;
    private JPanel ivjlogPlanTab = null;
    private JTabbedPane ivjtabbingPane = null;
    private JPanel ivjtilingPane = null;

  private static boolean debug = 
    "true".equals (System.getProperty ("org.cougaar.mlm.ui.tpfdd.gui.view.TPFDDShell.debug"));
  

  private JLabel machineText = null;

    // user code members
    // gui
    private Vector clusterMenuItems;
    private JMenuItem selectedClusterItem = null;
    private ClusterQuery clusterQuery = null;
    private GanttChartView ganttChartView = null;
    private LogPlanView logPlanView = null;
    private JScrollPane debugScroller = null;
    private JScrollPane statusScroller = null;
    private JLabel startupLabel = null;
    private OutputHandler messageHandler = null;

    // data store
    private ClusterCache clusterCache;
    private ClientPlanElementProvider provider;
    private String[] clusterNames;
    private ItineraryProducer itineraryProducer;

    // applet settable parameters
    private String startDateString;
    private int fontSize;

    /**
     * Gets the applet information.
     * @return String
     */
    public String getAppletInfo()
    {
	return "COUGAAR Transportation Movement Visualization Tool (c) 1999-2000 Ascent Technology";
    }

    private LogPlanView getlogPlanView()
    {
	if ( logPlanView == null ) {
	    try {
		Debug.out("TS:gP new LPV");
		logPlanView = new LogPlanView(getprovider(), getclusterCache(), this, fontSize);
	    } catch (RuntimeException ivjExc) {
		handleException(ivjExc);
	    }
	}
	return logPlanView;
    }

    private ClusterCache getclusterCache()
    {
	if ( clusterCache == null ) {
	    try {
		Debug.out("TS:gCC new CC");
		clusterCache = new ClusterCache(true);
	    } catch (RuntimeException ivjExc) {
		handleException(ivjExc);
	    }
	}
	return clusterCache;
    }
	    
    private ClientPlanElementProvider getprovider()
    {
	if ( provider == null ) {
	    try {
		Debug.out("TS:gP new PEP");
		provider = new ClientPlanElementProvider(getclusterCache(), new PassAll(), false, true);
	    } catch (Exception ivjExc) {
		handleException(ivjExc);
	    }
	}
	return provider;
    }

    private JScrollPane getDebugScroller()
    {
	if ( debugScroller == null ) {
	    try {
		MessageArea debugMessages =
		    new MessageArea("Debugging log -- for programmers and testers\n", 10, 80);
		debugMessages.setEditable(false);
		debugScroller = new JScrollPane(debugMessages);
		debugMessages.setScrollBar(debugScroller.getVerticalScrollBar());
		debugScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		Debug.getHandler().addConsumer(debugMessages);
	    } catch (Exception ivjExc) {
		handleException(ivjExc);
	    }
	}
	return debugScroller;
    }

    private OutputHandler getmessageHandler()
    {
	if ( messageHandler == null ) {
	    try {
		messageHandler =
		    new OutputHandler(new SimpleProducer("Output Handler"), true);
	    }
	    catch (Exception ivjExc) {
		handleException(ivjExc);
	    }
	}
	return messageHandler;
    }
		    
    private JScrollPane getStatusScroller()
    {
	if ( statusScroller == null ) {
	    try {
		MessageArea statusMessages =
		    new MessageArea("Status log -- for administrators and interested users\n", 10, 80);
		statusMessages.setEditable(false);
		statusScroller = new JScrollPane(statusMessages);
		statusMessages.setScrollBar(statusScroller.getVerticalScrollBar());
		statusScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		getmessageHandler().addConsumer(statusMessages);
	    } catch (Exception ivjExc) {
		handleException(ivjExc);
	    }
	}
	return statusScroller;
    }
    
    /**
     * Return the containerPanel property value.
     * @return JPanel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JPanel getcontainerPanel()
    {
	if (ivjcontainerPanel == null) {
	    try {
		ivjcontainerPanel = new JPanel();
		ivjcontainerPanel.setName("containerPanel");
		ivjcontainerPanel.setLayout(new BorderLayout());
		// user code begin {1}
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjcontainerPanel;
    }

    /**
     * Return the messageTab property value.
     * @return JPanel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JPanel getmessageTab()
    {
	if (ivjmessageTab == null) {
	    try {
		ivjmessageTab = new JPanel();
		ivjmessageTab.setName("messageTab");
		ivjmessageTab.setLayout(new GridLayout(2, 1));
		// user code begin {1}
		ivjmessageTab.add(getStatusScroller());
		ivjmessageTab.add(getDebugScroller());
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjmessageTab;
    }

    /**
     * Return the messageItem property value.
     * @return JCheckBoxMenuItem
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JCheckBoxMenuItem getmessageItem() {
	if (ivjmessageItem == null) {
	    try {
		ivjmessageItem = new JCheckBoxMenuItem();
		ivjmessageItem.setName("messageItem");
		ivjmessageItem.setToolTipText("Log of diagnostic messages");
		ivjmessageItem.setText("Messages");
		ivjmessageItem.setSelected(true);
		// user code begin {1}
		ivjmessageItem.addActionListener(this);
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjmessageItem;
    }

    /**
     * Return the ganttItem property value.
     * @return JCheckBoxMenuItem
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JCheckBoxMenuItem getganttItem() {
	if (ivjganttItem == null) {
	    try {
		ivjganttItem = new JCheckBoxMenuItem();
		ivjganttItem.setName("ganttItem");
		ivjganttItem.setToolTipText("Time-phased display of various cluster-object movements,");
		ivjganttItem.setText("Gantt Chart View");
		ivjganttItem.setSelected(true);
		// user code begin {1}
		ivjganttItem.addActionListener(this);
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjganttItem;
    }

    /**
     * Return the clusterMenu property value.
     * @return JMenu
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JMenu getclusterMenu()
    {
	if (ivjclusterMenu == null) {
	    try {
		ivjclusterMenu = new JMenu();
		ivjclusterMenu.setName("clusterMenu");
		ivjclusterMenu.setText("Select Cluster");
		// user code begin {1}
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjclusterMenu;
    }

    private JMenuItem getretryAggregationInitItem()
    {
	if (ivjretryAggregationInitItem == null) {
	    try {
		ivjretryAggregationInitItem = new JMenuItem();
		ivjretryAggregationInitItem.setName("retryAggregationInitItem");
		ivjretryAggregationInitItem.setToolTipText("Reset or retry connection to aggregation server");
		ivjretryAggregationInitItem.setText("Reset Aggregation Server Connection");
		ivjretryAggregationInitItem.addActionListener(this);
	    } catch (Exception ivjExc) {
		handleException(ivjExc);
	    }
	}
	return ivjretryAggregationInitItem;
    }
	    
    private JMenuItem getreloadAggregationItem()
    {
	if (ivjreloadAggregationItem == null) {
	    try {
		ivjreloadAggregationItem = new JMenuItem();
		ivjreloadAggregationItem.setName("reloadAggregationItem");
		ivjreloadAggregationItem.setToolTipText("Force Aggregation Server to make new snapshot of cluster data");
		ivjreloadAggregationItem.setText("Reload Aggregation Server");
		ivjreloadAggregationItem.addActionListener(this);
	    } catch (Exception ivjExc) {
		handleException(ivjExc);
	    }
	}
	return ivjreloadAggregationItem;
    }

    /**
     * Return the ganttTab property value.
     * @return JPanel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JPanel getganttTab() {
	if (ivjganttTab == null) {
	    try {
		ivjganttTab = new JPanel();
		ivjganttTab.setName("ganttTab");
		ivjganttTab.setLayout(new BorderLayout());
		// user code begin {1}
		ganttChartView = new GanttChartView(getprovider(), startDateString);
		Debug.out("TS:gP new GCV");
		getprovider().addRowConsumer(ganttChartView.getWidget());
		ivjganttTab.add(ganttChartView, BorderLayout.CENTER);
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjganttTab;
    }

    /**
     * Return the panelMenuSeparator property value.
     * @return JSeparator
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JSeparator getpanelMenuSeparator() {
	if (ivjpanelMenuSeparator == null) {
	    try {
		ivjpanelMenuSeparator = new JSeparator();
		ivjpanelMenuSeparator.setName("panelMenuSeparator");
		// user code begin {1}
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjpanelMenuSeparator;
    }
    
    /**
     * Return the logPlanItem property value.
     * @return JCheckBoxMenuItem
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JCheckBoxMenuItem getlogPlanItem()
    {
	if (ivjlogPlanItem == null) {
	    try {
		ivjlogPlanItem = new JCheckBoxMenuItem();
		ivjlogPlanItem.setName("logPlanItem");
		ivjlogPlanItem.setToolTipText("Hierarchical view of all known tasks.");
		ivjlogPlanItem.setText("LogPlan View");
		ivjlogPlanItem.setSelected(true);
		// user code begin {1}
		ivjlogPlanItem.addActionListener(this);
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjlogPlanItem;
    }
    
    /**
     * Return the logPlanTab property value.
     * @return JPanel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JPanel getlogPlanTab()
    {
	if (ivjlogPlanTab == null) {
	    try {
		ivjlogPlanTab = new JPanel();
		ivjlogPlanTab.setName("logPlanTab");
		ivjlogPlanTab.setLayout(new BorderLayout());
		// user code begin {1}
		logPlanView = getlogPlanView();
		getprovider().addItemPoolConsumer(logPlanView);
		ivjlogPlanTab.add(logPlanView, BorderLayout.CENTER);
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjlogPlanTab;
    }
    /**
     * Return the actionMenu property value.
     * @return JMenu
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JMenu getactionMenu()
    {
	if (ivjactionMenu == null) {
	    try {
		ivjactionMenu = new JMenu();
		ivjactionMenu.setName("actionMenu");
		ivjactionMenu.setText("Actions");
		ivjactionMenu.add(getnewViewItem());
		ivjactionMenu.add(getretryAggregationInitItem());
		ivjactionMenu.add(getreloadAggregationItem());
		// user code begin {1}
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjactionMenu;
    }
    
    /**
     * Return the panelMenu property value.
     * @return JMenu
     */

    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JMenu getpanelMenu()
    {
	if (ivjpanelMenu == null) {
	    try {
		ivjpanelMenu = new JMenu();
		ivjpanelMenu.setName("panelMenu");
		ivjpanelMenu.setToolTipText("Commands for manipulating panel arrangment.");
		ivjpanelMenu.setText("Panel");
		ivjpanelMenu.add(getmessageItem());
		ivjpanelMenu.add(getlogPlanItem());
		ivjpanelMenu.add(getganttItem());
		ivjpanelMenu.add(getpanelMenuSeparator());
		ivjpanelMenu.add(gettileItem());
		ivjpanelMenu.add(getstackItem());
		// user code begin {1}
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjpanelMenu;
    }

    private JMenuItem gettaskItem()
    {
	if (ivjtaskItem == null) {
	    try {
		ivjtaskItem = new JMenuItem();
		ivjtaskItem.setName("taskItem");
		ivjtaskItem.setToolTipText("Request all tasks (raw LogPlan form)");
		ivjtaskItem.setText("Tasks");
		ivjtaskItem.addActionListener(this);
	    } catch (Exception ivjExc) {
		handleException(ivjExc);
	    }
	}
	return ivjtaskItem;
    }

    /**
     * Return the newViewItem property value.
     * @return JMenuItem
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JMenuItem getnewViewItem()
    {
	if (ivjnewViewItem == null) {
	    try {
		ivjnewViewItem = new JMenuItem();
		ivjnewViewItem.setName("newViewItem");
		ivjnewViewItem.setToolTipText("Query cluster with a dialog form");
		ivjnewViewItem.setText("New TPFDD View...");
		// user code begin {1}
		ivjnewViewItem.addActionListener(this);
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjnewViewItem;
    }

    /**
     * Return the stackItem property value.
     * @return JRadioButtonMenuItem
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JRadioButtonMenuItem getstackItem()
    {
	if (ivjstackItem == null) {
	    try {
		ivjstackItem = new JRadioButtonMenuItem();
		ivjstackItem.setName("stackItem");
		ivjstackItem.setToolTipText("Tabbed view; frontmost takes up entire display");
		ivjstackItem.setText("Stack");
		ivjstackItem.setSelected(true);
		// user code begin {1}
		ivjstackItem.addActionListener(this);
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjstackItem;
    }
    
    /**
     * Return the tabbingPane property value.
     * @return JTabbedPane
     */

    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JTabbedPane gettabbingPane()
    {
	if (ivjtabbingPane == null) {
	    try {
		ivjtabbingPane = new JTabbedPane();
		ivjtabbingPane.setName("tabbingPane");
		// user code begin {1}
		if ( getlogPlanItem().isSelected() )
		    ivjtabbingPane.addTab("Log Plan", getlogPlanTab());
		if ( getganttItem().isSelected() )
		    ivjtabbingPane.addTab("Gantt Chart", getganttTab());
		if ( getmessageItem().isSelected() )
		    ivjtabbingPane.addTab("Messages", getmessageTab());
		for ( int i = 0; i < newViews.size(); i++ )
		    ivjtabbingPane.addTab("View " + (i + 1), (JPanel)(newViews.get(i)));
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjtabbingPane;
    }

    private JPanel gettilingPane()
    {
	if ( ivjtilingPane == null ) {
	    try {
		ivjtilingPane = new JPanel();
		ivjtilingPane.setName("tilingPane");
		boolean m = getmessageItem().isSelected();
		boolean l = getlogPlanItem().isSelected();
		boolean g = getganttItem().isSelected();
		int numTabs = (m ? 1 : 0) + (l ? 1 : 0) + (g ? 1 : 0) + newViews.size();
		int largestFactor = 1;
		for ( int i = numTabs / 2; i > 1; i-- )
		    if ( numTabs == (numTabs / i) * i ) {
			largestFactor = i;
			break;
		    }
		ivjtilingPane.setLayout(new GridLayout(numTabs / largestFactor, largestFactor));
		if ( m ) ivjtilingPane.add(getmessageTab());
		if ( l ) ivjtilingPane.add(getlogPlanTab());
		if ( g ) ivjtilingPane.add(getganttTab());
		for ( int i = 0; i < newViews.size(); i++ )
		    ivjtilingPane.add("View " + (i + 1), (JPanel)(newViews.get(i)));
	    } catch ( Exception ivjExc ) {
		handleException(ivjExc);
	    }
	}
	return ivjtilingPane;
    }

    
    /**
     * Return the tileItem property value.
     * @return JRadioButtonMenuItem
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JRadioButtonMenuItem gettileItem()
    {
	if (ivjtileItem == null) {
	    try {
		ivjtileItem = new JRadioButtonMenuItem();
		ivjtileItem.setName("tileItem");
		ivjtileItem.setToolTipText("Arrange windows to fully cover main panel with no overlap.");
		ivjtileItem.setText("Tile");
		ivjtileItem.setSelected(false);
		// user code begin {1}
		ivjtileItem.addActionListener(this);
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjtileItem;
    }
    
    /**
     * Return the TPFDDShellMenuBar property value.
     * @return JMenuBar
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private JMenuBar getTPFDDShellMenuBar()
    {
	if (ivjTPFDDShellMenuBar == null) {
	    try {
		ivjTPFDDShellMenuBar = new JMenuBar();
		ivjTPFDDShellMenuBar.setName("TPFDDShellMenuBar");
		ivjTPFDDShellMenuBar.add(getactionMenu());
		ivjTPFDDShellMenuBar.add(getclusterMenu());
		ivjTPFDDShellMenuBar.add(getpanelMenu());
		ivjTPFDDShellMenuBar.add(createSourceText());
                
		// user code begin {1}
		// user code end
	    } catch (Exception ivjExc) {
		// user code begin {2}
		// user code end
		handleException(ivjExc);
	    }
	}
	return ivjTPFDDShellMenuBar;
    }
    

  private JPanel createSourceText() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JLabel label = new JLabel("Aggregation Server Machine:  ");
    machineText = new JLabel("               ");
    
    // Make everything show up prettily
    //panel.setBorder(BorderFactory.createLineBorder(Color.black));
    JPanel miniPanel = new JPanel();
    miniPanel.add(label);
    miniPanel.add(machineText);
   panel.add(miniPanel);

    return panel;
  }


    private ClusterQuery getclusterQuery()
    {
	if ( clusterQuery == null ) {
	    try {
		if (debug) 
		  System.out.println ("TPFDDShell.getclusterQuery - demand host is " + getclusterCache().getDemandHost ());
		clusterQuery = new ClusterQuery(provider, getclusterCache().getDemandHost ());
		clusterQuery.addActionListener(this);
	    }
	    catch ( Exception e ) {
		handleException(e);
	    }
	}
	return clusterQuery;
    }

    private void handleException(Exception e)
    {
	OutputHandler.out(ExceptionTools.toString("TS:hE", e));
    }

    public String[] getclusterNames()
    {
	if ( clusterNames == null ) {
	    try {
		Vector clusterVec = getclusterCache().getclusterNames();
		clusterNames = new String[clusterVec.size()];
		int eye = 0;
		for ( Iterator i = clusterVec.iterator(); i.hasNext(); )
		    clusterNames[eye++] = (String)(i.next());
	    }
	    catch ( Exception e ) {
		handleException(e);
	    }
	}
	return clusterNames;
    }

	
    public boolean initAggregationProducer()
    {
	String ccHost = getDocumentBase().getHost();
	if ( ccHost == null || ccHost.length() == 0 )
	    ccHost = "localhost";
        String realHost = getclusterCache().clientGuiSetHost(ccHost);
        String demandHost = getclusterCache().clientGuiSetDemandHost(ccHost);
        machineText.setText(realHost);
	// true = cold (newly produced, purging old ones) producer
	getclusterCache().getLogPlanProducer("Aggregation", true);
	// fill out the unit hierarchy shell to help user see overview and make new filter requests;
	// we get it from the server to retain flexibility.
	QueryData hierarchyQuery = new QueryData();
	hierarchyQuery.setOtherCommand(SEND_HIERARCHY);
	getprovider().request("Aggregation", PSPClientConfig.UIDataPSP_id, hierarchyQuery);
	return true;
    }

    private void resetView()
    {
	Debug.out("TS:resetView enter");
	getcontainerPanel().removeAll();
	if ( getstackItem().isSelected() ) { // tabbing view
	    gettabbingPane().removeAll(); // free children of constraints before killing
	    ivjtabbingPane = null;
	    getcontainerPanel().add(gettabbingPane(), BorderLayout.CENTER);
	}
	else { // tiling view
	    gettilingPane().removeAll();
	    ivjtilingPane = null;
	    getcontainerPanel().add(gettilingPane(), BorderLayout.CENTER);
	}
	getcontainerPanel().validate(); // this is necessary, not sure why; invalidate doesn't work
	Debug.out("TS:resetView leave");
    }

    public void init()
    {
	Debug.setHandler(new Debug(new SimpleProducer("Debug Handler"), false));

	String fontSizeString = getParameter("fontSize");
	if ( fontSizeString == null )
	    fontSizeString = "12";
	fontSize = Integer.parseInt(fontSizeString);
	if ( fontSize < 8 )
	    fontSize = 8;
	
	startDateString = getParameter("startDateString");
	if ( startDateString == null )
	    startDateString = System.getProperty("cdayDate", "07/04/00");
	
        //	String debugParam = getParameter("debug");
	//if ( debugParam != null )
        //  Debug.set(debugParam.equalsIgnoreCase("true"));
        Debug.set(true);
	// init text areas (inside scrollers) so they'll catch all messages from the start
	getDebugScroller();
	getStatusScroller();
	OutputHandler.out("Starting ALPINE Viewer");
	
	boolean stupidLinuxAppletviewerBug = false;
	String doStupidLinuxAppletviewerBug = null;
	try {
	    doStupidLinuxAppletviewerBug =
		System.getProperty("stupidLinuxAppletviewerBug");
	}
	catch ( AccessControlException e ) {
	}
	if ( doStupidLinuxAppletviewerBug != null &&
	     doStupidLinuxAppletviewerBug.equalsIgnoreCase("true") )
	    stupidLinuxAppletviewerBug = true;

	try {
	    setName("TPFDDShell");
	    setSize(1000, 700);
	    if ( stupidLinuxAppletviewerBug )
		setVisible(true);
	    setJMenuBar(getTPFDDShellMenuBar());
	    setContentPane(getcontainerPanel());
	    // user code begin {1}
	    resetView();
	    // user code end
	} catch (Exception ivjExc) {
	    // user code begin {2}
	    // user code end
	    handleException(ivjExc);
	}
    }
    
    // Called when swingworker is done constructing cluster name array.
    public void setClusterMenu()
    {
	String[] items = getclusterNames();
	getclusterMenu().removeAll();
	clusterMenuItems = new Vector();
	for ( int i = 0; i < items.length; i++ ) {
	    JCheckBoxMenuItem item = new JCheckBoxMenuItem();
	    item.setName(items[i]);
	    item.setText(items[i]);
	    item.addActionListener(this);
	    getclusterMenu().add(item);
	    clusterMenuItems.add(item);
	    if ( i == 0 ) {
		selectedClusterItem = item;
		item.setSelected(true);
	    }
	    else
		item.setSelected(false);
	}
	getnewViewItem().setEnabled(true);

	OutputHandler.out("TS:sCM ...cluster proxy server initialization successful.");
    }

    public void backgroundInitAggregationProducer()
    {
	new TPFDDSwingWorker(this)
	    {
		private boolean result;

		public void construct() {
		    result = parent.initAggregationProducer();
		}
		
		public void finished() {
		}
	    };
    }

    public void start()
    {
	setClusterMenu();
	backgroundInitAggregationProducer();
    }
    
    /*
     * Event Handling routines
     */
    public void actionPerformed(ActionEvent event)
    {
	String command = event.getActionCommand();
	Object source = event.getSource();

	Debug.out("TS:aP command " + command);
	if ( source instanceof JMenuItem ) {
	    JMenuItem item = (JMenuItem)source;
	    // Action Menu
	    if ( source == getnewViewItem() ) {
		getclusterQuery().setVisible(true);
	    }
	    else if ( source == getretryAggregationInitItem() ) {
		backgroundInitAggregationProducer();
	    }
	    else if ( source == getreloadAggregationItem() ) {
		QueryData query = new QueryData();
		query.setOtherCommand("RELOAD yourself");
		provider.request("Aggregation", PSPClientConfig.UIDataPSP_id, query);
	    }
	    // Panel Menu
	    else if ( source == getmessageItem() || source == getlogPlanItem() || source == getganttItem() ) {
		resetView();
	    }
	    else if ( source == gettileItem() ) {
		if ( !gettileItem().isSelected() ) // was selected; make it stay selected
		    gettileItem().setSelected(true);
		else {
		    getstackItem().setSelected(false);
		    resetView();
		}
	    }
	    else if ( source == getstackItem() ) {
		if ( !getstackItem().isSelected() ) // was selected; make it stay selected
		    getstackItem().setSelected(true);
		else {
		    gettileItem().setSelected(false);
		    resetView();
		}
	    }
	    else if ( clusterMenuItems.contains(item) ) {
		selectedClusterItem.setSelected(false);
		selectedClusterItem = item;
		selectedClusterItem.setSelected(true);

		OutputHandler.out("TS:aP Changed selected cluster to: " + item.getText());
	    }
	    else
		OutputHandler.out("TS:aP Error: unknown JMenuItem source: " + source);
	}
	else if ( source instanceof JButton ) {
	    if ( source == clusterQuery.getsendQueryButton() ) {
		getclusterQuery().setVisible(false);
		QueryData query = getclusterQuery().getLastQuery();
		newView(true, false, query, getclusterCache(), query.isOwnWindow());
	    }
	    else
		OutputHandler.out("TS:aP Error: unknown JButton source: " + source);
	}
	else
	    OutputHandler.out("TS:aP Error: unknown bean source: " + source);
    }

    Vector newViews = new Vector();

    public void newView(boolean showTPFDD, boolean showLogPlan, QueryData query, ClusterCache clusterCache,
			boolean newWindow)
    {
	String myUnitNameString, myCarrierString, myCargoString;
	String[] unitNames = query.getUnitNames();
	
	if ( unitNames != null ) {
	    myUnitNameString = unitNames[0];
	    for ( int i = 1; i < unitNames.length; i++ )
		if ( i > 0 )
		    myUnitNameString += ", " + unitNames[i];
	}
	else
	    myUnitNameString = "All units";
			    
	String[] carrierNames = query.getCarrierNames();
	String[] carrierTypes = query.getCarrierTypes();
	if ( carrierNames != null ) {
	    myCarrierString = carrierNames[0];
	    for ( int i = 1; i < carrierNames.length; i++ )
		if ( i > 0 )
		    myCarrierString += ", " + carrierNames[i];
	}
	else if ( carrierTypes != null ) {
	    myCarrierString = carrierTypes[0];
	    for ( int i = 1; i < carrierTypes.length; i++ )
		if ( i > 0 )
		    myCarrierString += ", " + carrierTypes[i];
	}
	else
	    myCarrierString = "All carriers";
	
	String[] cargoNames = query.getCargoNames();
	String[] cargoTypes = query.getCargoTypes();
	if ( cargoNames != null ) {
	    myCargoString = cargoNames[0];
	    for ( int i = 1; i < cargoNames.length; i++ )
		if ( i > 0 )
		    myCargoString += ", " + cargoNames[i];
	}
	else if ( cargoTypes != null ) {
	    myCargoString = cargoTypes[0];
	    for ( int i = 1; i < cargoTypes.length; i++ )
		if ( i > 0 )
		    myCargoString += ", " + cargoTypes[i];
	}
	else
	    myCargoString = "All cargo";

	ClientPlanElementProvider childProvider = new ClientPlanElementProvider(clusterCache, query, false, false);

	SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yy HH:mm:ss");
	String date = formatter.format(new Date());
	if ( showLogPlan ) {
	    LogPlanView logPlanView = new LogPlanView(childProvider, clusterCache, this, fontSize);
	    JPanel logPlanPanel = new JPanel();
	    logPlanPanel.setLayout(new BorderLayout());
	    logPlanPanel.add(logPlanView, BorderLayout.CENTER);
	    if ( newWindow ) {
		JFrame logPlanFrame = new JFrame();
		logPlanFrame.getContentPane().add(logPlanPanel, BorderLayout.CENTER);
		logPlanFrame.setTitle(date + " " + myUnitNameString + "/" + myCarrierString + "/" + myCargoString);
		logPlanFrame.setSize(640, 480);
		logPlanFrame.setVisible(true);
	    }
	    else
		newViews.add(logPlanPanel);
	    childProvider.addItemPoolConsumer(logPlanView);
	}

	if ( showTPFDD ) {
	    GanttChartView ganttChartView = new GanttChartView(childProvider, startDateString);
	    JPanel ganttChartPanel = new JPanel();
	    ganttChartPanel.setLayout(new BorderLayout());
	    ganttChartPanel.add(ganttChartView, BorderLayout.CENTER);
	    if ( newWindow ) {
		JFrame ganttChartFrame = new JFrame();
		ganttChartFrame.getContentPane().add(ganttChartPanel, BorderLayout.CENTER);
		ganttChartFrame.setTitle(date + " " + myUnitNameString + "/" + myCarrierString + "/"
					 + myCargoString);
		ganttChartFrame.setSize(640, 480);
		ganttChartFrame.setVisible(true);
	    }
	    else
		newViews.add(ganttChartPanel);
	    childProvider.addRowConsumer(ganttChartView.getWidget());
	}
	
	if ( !newWindow )
	    resetView();


	if (debug) 
	  System.out.println("query is "+ query);
	childProvider.request("Aggregation", PSPClientConfig.UIDataPSP_id, query);

    }

    /**
     * main entrypoint - starts the part when it is run as an application
     * @param args String[]
     */
    public static void main(String[] args)
    {
	try {
	    JFrame frame = new JFrame();
	    TPFDDShell aTPFDDShell;
	    Class iiCls = Class.forName("org.cougaar.mlm.ui.tpfdd.gui.view.TPFDDShell");
	    ClassLoader iiClsLoader = iiCls.getClassLoader();
	    aTPFDDShell = (TPFDDShell)java.beans.Beans.instantiate(iiClsLoader,
							  "org.cougaar.mlm.ui.tpfdd.gui.view.TPFDDShell");
	    frame.getContentPane().add(aTPFDDShell, BorderLayout.CENTER);
	    frame.setSize(aTPFDDShell.getSize());
	    frame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			System.exit(0);
		    };
		});
	    frame.setVisible(true);
	    aTPFDDShell.start();
	    frame.setTitle ("TPFDDShell");
	}
	catch (Exception exception) {
	    System.err.println("Exception occurred in main() of TPFDDShell");
	    exception.printStackTrace(System.out);
	}
    }
}
