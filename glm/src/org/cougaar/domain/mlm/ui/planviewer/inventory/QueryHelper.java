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
 

package org.cougaar.domain.mlm.ui.planviewer.inventory;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.io.InputStream;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import org.cougaar.domain.mlm.ui.data.UISimpleInventory;
import org.cougaar.domain.mlm.ui.data.UISimpleSchedule;
import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.domain.mlm.ui.planviewer.XMLClientConfiguration;

import org.cougaar.util.ThemeFactory;


public class QueryHelper implements ActionListener,
				    ItemListener,
                                    InventoryDayRolloverListener {
  static final String UPDATE = "Update";
  static final String RESET = "Reset";
  static final String CLEAR = "Clear";
  static final String SAVE = "Save";
  static final String MOVIE_MODE = "Movie Mode";
  static final String CDAY_MODE = InventoryChart.CDAY_MODE;

  String clusterURL;
  String PSP_package;
  String PSP_id;
  Container container;
  Query query;
  String clusterName;
  String assetName;
  boolean doDisplayTable;
  boolean isApplet;
  public JFrame frame;

  InventoryExecutionTimeStatusHandler timeStatusHandler;

  /* Called by application or applet; if its an applet, then
     use the container passed as an argument.
   */

  public QueryHelper(Query query, String clusterURL, 
                     boolean isApplet, Container container,
                     boolean doDisplayTable,
		     InventoryExecutionTimeStatusHandler aTimeStatusHandler) {
    this.query = query;
    this.clusterURL = clusterURL;
    this.doDisplayTable = doDisplayTable;
    this.isApplet = isApplet;
    clusterName = clusterURL.substring(clusterURL.indexOf('$')+1, 
                                 clusterURL.length()-1);
    assetName = query.getQueryToSend();
    int indx = assetName.indexOf(":");
    if (indx > -1) assetName = assetName.substring(indx+1);
    createFrame(assetName+" at "+clusterName);
    PSP_package = XMLClientConfiguration.PSP_package;
    timeStatusHandler = aTimeStatusHandler;
    doQuery();
  }

  /** Called by an application when data is retrieved from a file.
   */

  public QueryHelper(Query query) {
    this.query = query;
    assetName = query.getQueryToSend();
    int indx = assetName.indexOf(":");
    if (indx > -1) assetName = assetName.substring(indx+1);
    createFrame(assetName);
    displayChart();
  }


  // sets up a window for the inventory chart

  private void createFrame() {
      createFrame("ALP");
  }

  private void createFrame(String title) {
    frame = new JFrame(title);
    frame.setSize(800, 500);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - 800/2,
                      screenSize.height/2 - 400/2);
    frame.setVisible(true);
    container = frame.getContentPane();

    frame.addWindowListener(new WindowAdapter() {
	public void windowClosed(WindowEvent e) {
	    removeAsDayRolloverListener();
	}});
  }

  private void removeAsDayRolloverListener() {
      if(timeStatusHandler != null) {
	  timeStatusHandler.removeDayRolloverListener(this) ;
      }
  }


  private void doQuery() {
    PSP_id = query.getPSP_id();
    submit();
  }

  /*  Submit request to the cluster, read response(s) and display results.
   */

  private void submit() {
 
    System.out.println("Submitting: fresh chart ");

    executeQuery();
    
    displayChart();
    if (doDisplayTable)
      displayTable(query);
  }


 private void doUpdate() {
 
    System.out.println("Submitting: fresh chart ");
    System.out.println("QueryHelper::doUpdate: Thread priority: " + Thread.currentThread().getPriority());

    executeQuery();
    updateChart();

  }


  private void executeQuery() {
    String request = query.getQueryToSend();
    InputStream is = null;

    System.out.println("ExecutingQuery: " + request + " to: " + clusterURL +
                       " for: " + PSP_id);

    try {
      ConnectionHelper connection = 
        new ConnectionHelper(clusterURL, PSP_package, PSP_id);
      connection.sendData(request);
      is = connection.getInputStream();
    } catch (Exception e) {
      displayErrorString(e.toString());
    }

    query.readReply(is);

  }


  
  private void displayErrorString(String reply) {
    JOptionPane.showMessageDialog(null, reply, reply, 
                                  JOptionPane.ERROR_MESSAGE);
  }

  private void displayChart() {
    JPanel chart = query.createChart(clusterName,
				     timeStatusHandler);

    // for scrolling chart, doesn't work
    //    if (chart != null) {
      //      JScrollPane scrollpane = new JScrollPane(chart);
      //      scrollpane.setBorder(new BevelBorder(BevelBorder.LOWERED));
      //      scrollpane.setPreferredSize(new Dimension(300, 100));
      //      container.setLayout(new BorderLayout());
      //      container.add(scrollpane, BorderLayout.CENTER);
      //      JButton updateButton = new JButton("update");
      //      updateButton.setActionCommand(UPDATE);
      //    updateButton.addActionListener(this);
      //      container.add(updateButton, BorderLayout.SOUTH);
      //      container.validate();
      //    }
    
    if (chart != null) {
      container.setLayout(new BorderLayout());
      container.add(chart, BorderLayout.CENTER);
      chart.setBorder(new BevelBorder(BevelBorder.LOWERED));
      container.add(createButtonsPanel(), BorderLayout.SOUTH);
      container.validate();
    } else
      displayErrorString("No data received");
    
  }


  private void updateChart() {
    JPanel chart = 
	query.reinitializeAndUpdateChart(clusterName);

    // for scrolling chart, doesn't work
    //    if (chart != null) {
      //      JScrollPane scrollpane = new JScrollPane(chart);
      //      scrollpane.setBorder(new BevelBorder(BevelBorder.LOWERED));
      //      scrollpane.setPreferredSize(new Dimension(300, 100));
      //      container.setLayout(new BorderLayout());
      //      container.add(scrollpane, BorderLayout.CENTER);
      //      JButton updateButton = new JButton("update");
      //      updateButton.setActionCommand(UPDATE);
      //    updateButton.addActionListener(this);
      //      container.add(updateButton, BorderLayout.SOUTH);
      //      container.validate();
      //    }
    
    if (chart != null) {
	/* MWD Remove
      container.setLayout(new BorderLayout());
      container.add(chart, BorderLayout.CENTER);
      chart.setBorder(new BevelBorder(BevelBorder.LOWERED));
      container.add(createButtonsPanel(), BorderLayout.SOUTH);
      container.validate();
	*/
    } else
      displayErrorString("No data received");
    
  }

  private void displayTable(Query query) {
    JTable table = query.createTable(clusterName);
    if (table != null) {
      // create a separate frame for it
      JFrame frame = new JFrame("Table for "+assetName+" at "+clusterName);
      Container c = frame.getContentPane();
      c.setLayout(new BorderLayout());
      c.add(new JScrollPane(table), BorderLayout.CENTER);
      frame.pack();
      frame.setVisible(true);
    } else
      displayErrorString("No data received");
  }

  private JButton createButton(String label, boolean enabled) {
    JButton button = new JButton(label);
    button.setEnabled(enabled);
    button.setActionCommand(label);
    button.addActionListener(this);
    return button;
  }

  private JPanel createButtonsPanel() {
    JPanel panel = new JPanel();
    //panel.add(createButton("Print", false));
    //panel.add(createButton("Report", true));

    // if it's an applet, disable the save button
    if (isApplet)
      panel.add(createButton("Save", false));
    else {
      JButton saveButton = createButton(SAVE, true);
      saveButton.setToolTipText("Save data for this chart to a file.");
      panel.add(saveButton);
    }

    boolean enableUpdate = true;
    if((clusterURL == null) || (PSP_id == null)) {
	enableUpdate = false;
    }
    JButton updateButton = createButton(UPDATE,enableUpdate);

    panel.add(updateButton);
    JButton resetButton = createButton(RESET, true);
    resetButton.setToolTipText("Hold and drag over chart to zoom in.\n Click here to reset to original view.");
    panel.add(resetButton);
//     JButton clearButton = createButton(CLEAR, true);
//     clearButton.setToolTipText("Clears shortfall snakes from graph");
//     panel.add(clearButton);

    if(timeStatusHandler != null) {
	JCheckBox movieModeCheck = new JCheckBox(MOVIE_MODE,false);
	movieModeCheck.setActionCommand(MOVIE_MODE);
	movieModeCheck.addItemListener(this);
	panel.add(movieModeCheck);
    }

    /*** MWD CDay toggling not ready for prime time **/
    JCheckBox cdaysModeCheck = new JCheckBox(CDAY_MODE,false);
    cdaysModeCheck.setActionCommand(CDAY_MODE);
    cdaysModeCheck.addItemListener(this);
    //Moving to InventoryChart.java
    //panel.add(cdaysModeCheck);
    
    return panel;
  }

  public void itemStateChanged(ItemEvent e) {
      if(e.getSource() instanceof JCheckBox) {
	  JCheckBox source = (JCheckBox) e.getSource();
	  if(source.getActionCommand().equals(MOVIE_MODE)) {
	      if(timeStatusHandler != null) {
		  if(e.getStateChange() == e.SELECTED){ 
		      System.out.println("Query Helper::Turning on Movies");
		      timeStatusHandler.addDayRolloverListener(this);
		  }
		  else {
		      System.out.println("Query Helper::Turning off Movies");	
		      timeStatusHandler.removeDayRolloverListener(this);
		  }
	      }
	  }
	  else if(source.getActionCommand().equals(CDAY_MODE)) {
	      query.setToCDays(e.getStateChange() == e.SELECTED);
	  }
      }
  }

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();

    if (command.equals(RESET)){
	query.resetChart();
    }
//     else if (command.equals(CLEAR)) 
//       ((InventoryChart)query.getChart()).setViewsVisible(false);
    else if (command.equals(UPDATE)) {
	doUpdate();
	//System.out.println("Should be doing update (QueryHelper::actionPerformed)");
    }
    else if (command.equals(SAVE)) {
      JFileChooser fc = new JFileChooser();
      if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
        query.save(fc.getSelectedFile());
    }
  }

    //  Invoked when a day rolls over from the universal time status
    //  handler.  Basically does the same as doUpdate(), except
    //  screen update is done through the event queue.
    public void dayRolloverUpdate(long date, double rate) {
	executeQuery();
	SwingUtilities.invokeLater(new ChartUpdatePost());
    }


    public class ChartUpdatePost implements Runnable {
	public synchronized void run() {
	    updateChart();
	}
    }

    public static void windowOnInventory(UISimpleInventory inventory) {
	ThemeFactory.establishMetalTheme();
	InventoryQuery iq = 
	    new InventoryQuery(inventory);
	QueryHelper qh = new QueryHelper(iq);

	qh.frame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }});

    }
	

    public static void main(String args[]) {
      windowOnInventory(InventoryChart.getMockInventory());
    }
}




