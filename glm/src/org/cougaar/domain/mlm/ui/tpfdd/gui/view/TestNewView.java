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
package org.cougaar.domain.mlm.ui.tpfdd.gui.view;


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

import org.cougaar.domain.mlm.ui.psp.transportation.PSP_Itinerary;

import org.cougaar.domain.mlm.ui.tpfdd.util.PathString;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.tpfdd.util.ExceptionTools;

import org.cougaar.domain.mlm.ui.tpfdd.gui.model.SimpleProducer;

import org.cougaar.domain.mlm.ui.tpfdd.aggregation.Server;
import org.cougaar.domain.mlm.ui.tpfdd.aggregation.ServerCommands;

import org.cougaar.domain.mlm.ui.tpfdd.producer.ThreadedProducer;
import org.cougaar.domain.mlm.ui.tpfdd.producer.LogPlanProducer;
import org.cougaar.domain.mlm.ui.tpfdd.producer.ItineraryProducer;
import org.cougaar.domain.mlm.ui.tpfdd.producer.PSPClientConfig;
import org.cougaar.domain.mlm.ui.tpfdd.producer.ClusterCache;


public class TestNewView extends JApplet {    

    private ClusterCache clusterCache;
    Vector newViews = new Vector();

    private ClusterCache getclusterCache()
    {
	if ( clusterCache == null ) {
	    try {
		Debug.out("TS:gCC new CC");
		clusterCache = new ClusterCache(true);
	    } catch (RuntimeException ivjExc) {
// 		handleException(ivjExc);
	    }
	}
	return clusterCache;
    }

//     private void resetView()
//     {
// 	Debug.out("TS:resetView enter");
// 	getcontainerPanel().removeAll();
// 	if ( getstackItem().isSelected() ) { // tabbing view
// 	    gettabbingPane().removeAll(); // free children of constraints before killing
// 	    ivjtabbingPane = null;
// 	    getcontainerPanel().add(gettabbingPane(), BorderLayout.CENTER);
// 	}
// 	else { // tiling view
// 	    gettilingPane().removeAll();
// 	    ivjtilingPane = null;
// 	    getcontainerPanel().add(gettilingPane(), BorderLayout.CENTER);
// 	}
// 	getcontainerPanel().validate(); // this is necessary, not sure why; invalidate doesn't work
// 	Debug.out("TS:resetView leave");
//     }
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

	String startDateString = System.getProperty("cdayDate", "07/04/00");
	//	SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yy HH:mm:ss");
	//	String date = formatter.format(new Date());
	if ( showLogPlan ) {
	    LogPlanViewPlus logPlanView = new LogPlanViewPlus(childProvider, clusterCache, this, 12);
	    JPanel logPlanPanel = new JPanel();
	    logPlanPanel.setLayout(new BorderLayout());
	    logPlanPanel.add(logPlanView, BorderLayout.CENTER);
	    if ( newWindow ) {
		JFrame logPlanFrame = new JFrame();
		logPlanFrame.getContentPane().add(logPlanPanel, BorderLayout.CENTER);
		logPlanFrame.setTitle(startDateString + " " + myUnitNameString + "/" + myCarrierString + "/" + myCargoString);
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
		ganttChartFrame.setTitle(startDateString + " " + myUnitNameString + "/" + myCarrierString + "/"
					 + myCargoString);
		ganttChartFrame.setSize(640, 480);
		ganttChartFrame.setVisible(true);
	    }
	    else
		newViews.add(ganttChartPanel);
	    childProvider.addRowConsumer(ganttChartView.getWidget());
	}
	
// 	if ( !newWindow )
// 	    resetView();


	System.out.println("query is "+ query);
	childProvider.request("Aggregation", PSPClientConfig.UIDataPSP_id, query);

    }

    public void makeWindows(String unitName) {
	String[] myarray = new String[2];
	myarray[0] = unitName;
	myarray[1] = "UIC/"+unitName;
	QueryData query = new QueryData();
	query.setUnitNames(myarray);
	query.setOwnWindow(true);
	query.setCarrierIncluded(false);
	query.setCargoIncluded(false);
	//	query.setOtherCommand("Send Hierarchy");
	int nodeTypes[];
	nodeTypes = new int[2];
 	nodeTypes[0] = TaskNode.ITINERARY;
 	nodeTypes[1] = TaskNode.ITINERARY_LEG;
	//	nodeTypes[0] = TaskNode.CARGO_TYPE;
	query.setNodeTypes(nodeTypes);
	newView(true,true,query,getclusterCache(),true);
	
    }


    /**
     * main entrypoint - starts the part when it is run as an application
     * @param args String[]
     */
    public static void main(String[] args)
    {
	TestNewView x = new TestNewView();
	x.makeWindows("1BDE-3ID");

// 	try {
// 	    JFrame frame = new JFrame();
// 	    TPFDDShell aTPFDDShell;
// 	    Class iiCls = Class.forName("org.cougaar.domain.mlm.ui.tpfdd.gui.view.TPFDDShell");
// 	    ClassLoader iiClsLoader = iiCls.getClassLoader();
// 	    aTPFDDShell = (TPFDDShell)java.beans.Beans.instantiate(iiClsLoader,
// 							  "org.cougaar.domain.mlm.ui.tpfdd.gui.view.TPFDDShell");
// 	    frame.getContentPane().add(aTPFDDShell, BorderLayout.CENTER);
// 	    frame.setSize(aTPFDDShell.getSize());
// 	    frame.addWindowListener(new WindowAdapter() {
// 		    public void windowClosing(WindowEvent e) {
// 			System.exit(0);
// 		    };
// 		});
// 	    frame.setVisible(true);
// 	    aTPFDDShell.start();
// 	}
// 	catch (Exception exception) {
// 	    System.err.println("Exception occurred in main() of TPFDDShell");
// 	    exception.printStackTrace(System.out);
// 	}
    }
}
