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
 
package org.cougaar.mlm.ui.planviewer.stoplight;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;

import org.cougaar.util.ThemeFactory;

import org.cougaar.mlm.ui.data.*;
import org.cougaar.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.mlm.ui.planviewer.XMLClientConfiguration;

/**
 * Create stoplight table with drill down.
 */

public class SupplyStoplight extends JPanel implements ActionListener {
  static final int CELLPADDING = 20; // add to width of cells and headers
  static final int DEFAULTHEIGHT = 400; // used in setting scroll viewport size
  static final int HEIGHTPADDING = 5; // add to height of cells 
  StoplightModel dataModel;
  Color colorRed = ThemeFactory.getCougaarRed();
  Color colorYellow = ThemeFactory.getCougaarYellow();
  Color colorGreen = ThemeFactory.getCougaarGreen();
  Color colorGray = Color.gray;
  Society society;
  EquipmentInfo equipmentInfo;
  SocietyStatus societyStatus;
  Hashtable statusFromClusters;
  JTable table;
  JLabel title;
  JButton unitButton;
  JButton updateButton;
  JButton criteriaButton;
  JButton saveButton;
  DefaultTableCellRenderer stoplightRenderer;
  TableCellRenderer stoplightColumnHeaderRenderer;
  static final String DRILLDOWNEQUIPMENT = "DDE";
  static final String DRILLDOWNUNIT = "DDU";
  static final String DRILLDOWNBOTH = "DDB";
  static final String DRILLDOWNNONE = "DDN";
  static final String DRILLUPEQUIPMENT = "DUE";
  static final String DRILLUPUNIT = "DUU";
  static final String DRILLUPBOTH = "DUB";
  static final String DRILLUPNONE = "DUN";
  static final String UPDATE = "Update";
  static final String EDIT_CRITERIA = "Edit Criteria";
  static final String SAVE = "Save";
  int selectedRow = -1;
  int selectedColumn = -1;
  // for contacting the society for updates
  Vector clusterURLs;
  String PSP_package;
  String PSP_id;
  String request;
  Component glassPane;
  DefaultListModel legendModel;
  JList legendList;
  CriteriaEditor criteriaEditor;
  DefaultListModel criteriaModel = null;
  boolean isLiveData;
  boolean isApplet;

  public SupplyStoplight(Hashtable statusFromClusters,
                         Vector clusterURLs, String PSP_package,
                         String PSP_id, String request,
                         boolean isApplet) {
    this.isApplet = isApplet;
    isLiveData = true;
    this.statusFromClusters = statusFromClusters;
    criteriaEditor = new CriteriaEditor(isApplet);
    criteriaModel = criteriaEditor.getModel();
    StatusHelper helper = new StatusHelper(statusFromClusters, criteriaModel);
    society = helper.getSociety();
    equipmentInfo = helper.getEquipmentInfo();
    // check for no equipment, and exit
    if (equipmentInfo.getEquipment(equipmentInfo.TOPEQUIPMENT) == null) {
      displayErrorString("No equipment");
      System.exit(0);
    }
    societyStatus = helper.getSocietyStatus();
    this.clusterURLs = clusterURLs;
    this.PSP_package = PSP_package;
    this.PSP_id = PSP_id;
    this.request = request;
    dataModel = new StoplightModel(society, equipmentInfo, societyStatus);
    createStoplightChart();
  }

  /** Display stoplight data from file.
   */

  public SupplyStoplight(Hashtable status) {
    isLiveData = false;
    isApplet = false;
    this.statusFromClusters = status;
    criteriaEditor = new CriteriaEditor(false);
    criteriaModel = criteriaEditor.getModel();
    StatusHelper helper = new StatusHelper(statusFromClusters, criteriaModel);
    society = helper.getSociety();
    equipmentInfo = helper.getEquipmentInfo();
    societyStatus = helper.getSocietyStatus();
    dataModel = new StoplightModel(society, equipmentInfo, societyStatus);
    createStoplightChart();
  }

  /** Display default society and equipment list and status.
   */

  public SupplyStoplight() {
    isLiveData = false;
    isApplet = false;
    request = SupplyController.REQUEST_SUPPLY;
    clusterURLs = null;
    society = new Society();
    society.createDefaultSociety();
    equipmentInfo = new EquipmentInfo();
    equipmentInfo.createDefaultEquipmentInfo();
    societyStatus = new SocietyStatus();
    setDefaultStatus();
    dataModel = new StoplightModel(society, equipmentInfo, societyStatus);
    criteriaEditor = new CriteriaEditor(false);
    criteriaModel = criteriaEditor.getModel();
    createStoplightChart();
  }


  /** For debugging, populate the default society and equipment lists
    with status. */

  public void setDefaultStatus() {
    Enumeration enumUnits = society.getAllUnits();
    while (enumUnits.hasMoreElements()) {
      Unit unit = (Unit)enumUnits.nextElement();
      String unitName = unit.getName();
      Enumeration enumEquipment = equipmentInfo.getAllEquipment();
      while (enumEquipment.hasMoreElements()) {
        Equipment equipment = (Equipment)enumEquipment.nextElement();
        String equipmentName = equipment.getName();
        societyStatus.setStatus(unitName, equipmentName, UIUnitStatus.GREEN);
      }
    }
  }


  private void createStoplightChart() {
    // Create the table
    table = new JTable(dataModel);
    table.setRowHeight(table.getRowHeight() + HEIGHTPADDING);

    // Turn off auto-resizing and set column sizes below.
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    // set stoplight renderer
    stoplightRenderer = new DefaultTableCellRenderer() {
      public void setValue(Object value) {
        String s = (String)value;
        setText(s);
        this.setForeground(Color.white);
        if (s.equals(UIUnitStatus.GREEN))
          this.setBackground(colorGreen);
        else if (s.equals(UIUnitStatus.YELLOW)) {
          this.setBackground(colorYellow);
          this.setForeground(Color.black);
        } else if (s.equals(UIUnitStatus.RED))
          this.setBackground(colorRed);
        else if (s.equals(UIUnitStatus.GRAY))
          this.setBackground(colorGray);
      }
    };
    stoplightRenderer.setHorizontalAlignment(JLabel.CENTER);
    customizeColumnDisplay();
    initColumnSizes();

    // Finish setting up the table.
    JScrollPane scrollpane = new JScrollPane(table);
    scrollpane.setBorder(new BevelBorder(BevelBorder.LOWERED));
    table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredScrollableViewportSize().width, DEFAULTHEIGHT));
    // if we're reading from a file, we don't know the original command
    if (request == null) 
      title = new JLabel("Status");
    else if (request.equals(SupplyController.REQUEST_SUPPLY))
      title = new JLabel("Supply Status", JLabel.CENTER);
    else if (request.equals(SupplyController.REQUEST_TRANSPORT))
      title = new JLabel("Transport Status", JLabel.CENTER);
    else
      title = new JLabel("Status");
    setLayout(new BorderLayout());
    add(scrollpane, BorderLayout.CENTER);
    JPanel titlePanel = new JPanel();
    unitButton = new JButton(society.TOPUNIT);
    unitButton.setEnabled(true);
    unitButton.setActionCommand(DRILLUPUNIT);
    unitButton.addActionListener(this);
    unitButton.setToolTipText("Click to display status of this unit.");
    titlePanel.setLayout(new BorderLayout());
    titlePanel.add(title, BorderLayout.NORTH);
    titlePanel.add(unitButton, BorderLayout.SOUTH);
    add(titlePanel, BorderLayout.NORTH);

    // legend Model is identical to criteria model,
    // except that it includes an entry for "not applicable, gray"
    JPanel legendPanel = new JPanel(new BorderLayout());
    legendModel = new DefaultListModel();
    // copy the criteria model, because we only want the legend
    // to change, when we apply the new criteria
    for (int i = 0; i < criteriaModel.size(); i++)
      legendModel.addElement(criteriaModel.getElementAt(i));
    legendModel.addElement(new CriteriaParameters("", 0, "", 0, UIUnitStatus.GRAY));
    legendList = new JList(legendModel);
    DefaultListCellRenderer renderer = new CriteriaListCellRenderer();
    renderer.setFont(new Font("SansSerif", Font.BOLD, 14));
    legendList.setCellRenderer(renderer);
    //    legendList.setVisibleRowCount(4);
    JScrollPane scroll = new JScrollPane(legendList);
    // ensure that the scroll area has the same background as other areas
    scroll.getViewport().getView().setBackground(scroll.getBackground());
    legendPanel.add(scroll, "Center");
    legendPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new BorderLayout());
    bottomPanel.add(legendPanel, BorderLayout.NORTH);
    bottomPanel.add(createButtonsPanel(), BorderLayout.SOUTH);
    add(bottomPanel, BorderLayout.SOUTH);

    table.addMouseListener(new MouseTableListener());
    table.getTableHeader().addMouseListener(new MouseTableHeaderListener());
  }

  private JButton createButton(String label, boolean enabled) {
    JButton button = new JButton(label);
    button.setEnabled(enabled);
    if (enabled) {
      button.setActionCommand(label);
      button.addActionListener(this);
    }
    return button;
  }

  private JPanel createButtonsPanel() {
    JPanel panel = new JPanel();
    panel.add(createButton("Print", false));
    panel.add(createButton("Report",false));

    if (isApplet)
      saveButton = createButton(SAVE, false);
    else {
      saveButton = createButton(SAVE, true);
      saveButton.setToolTipText("Save data for this chart to a file.");
    }
    panel.add(saveButton);

    // only enable update button if have live data
    updateButton = createButton(UPDATE, isLiveData);
    updateButton.setToolTipText("Click to update status from the cluster society.");
    panel.add(updateButton);
    criteriaButton = createButton(EDIT_CRITERIA, true);
    panel.add(criteriaButton);
    return panel;
  }


  /** Set stoplight renderer to draw background of cells
    in the color indicated.
   */

  private void customizeColumnDisplay() {
    TableColumnModel model = table.getColumnModel();
    for (int i = 1; i < model.getColumnCount(); i++) {
      TableColumn column = model.getColumn(i);
      column.setCellRenderer(stoplightRenderer);
    }
    DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
    cellRenderer.setToolTipText("Double click to display status of items in this equipment class or group.");
    model.getColumn(0).setCellRenderer(cellRenderer);
    for (int i = 0; i < model.getColumnCount(); i++) {
      TableColumn column = model.getColumn(i);
      StoplightColumnHeaderRenderer r = new StoplightColumnHeaderRenderer();
      if (i == 0)
        r.setToolTipText("Double click to display status of this class or group of equipment");
      else
        r.setToolTipText("Double click to display status of subordinates.");
      column.setHeaderRenderer(r);
    }
  }

  private void displayErrorString(String reply) {
    JOptionPane.showMessageDialog(null, reply, reply, 
                                  JOptionPane.ERROR_MESSAGE);
  }

  private void printMetrics(Hashtable status) {
    Enumeration keys = status.keys();
    while (keys.hasMoreElements()) {
      UILateStatus uil = (UILateStatus)status.get(keys.nextElement());
      System.out.println(uil.getUnitName() + " " +
                         uil.getEquipmentName() + " " +
                         uil.getEquipmentNomenclature());
      UIStoplightMetrics metrics = uil.getStoplightMetrics();
      ArrayList al = metrics.get();
      for (int i = 0; i < al.size(); i++)
        System.out.println("Quantity: " + al.get(i) +
                           " Days Late: " + i);
    }
  }

  private void save() {
    JFileChooser fc = new JFileChooser();
    if (fc.showSaveDialog(getTopLevelAncestor()) != JFileChooser.APPROVE_OPTION)
      return; // user cancelled
    File file = fc.getSelectedFile();
    try {
      FileOutputStream f = new FileOutputStream(file);
      ObjectOutputStream o = new ObjectOutputStream(f);
      o.writeObject(statusFromClusters);
      o.flush();
      f.close();
    } catch (Exception exc) {
      System.out.println("Cannot open file");
    }
  }

  /** Handle user menu selection of type of drill down.
   */

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    //    System.out.println("Action: " + command);

    if (command.equals(DRILLUPUNIT)) {
      String unitName = unitButton.getText();
      if (unitName.equals(society.TOPUNIT)) // top of chain
        return;
      unitButton.setText(society.getUnit(unitName).getSuperiorName());
      dataModel.modify(command, 1, 1); // arbitrary row and column
    } else if (command.equals(UPDATE)) {
      if (clusterURLs == null) // ignore update button if no society
        return;
      updateFromClusters();
      updateButton.setText("Updating");
      updateButton.setEnabled(false);
      captureUserEvents();
    } else if (command.equals(EDIT_CRITERIA)) {
      JFrame f = new JFrame("Edit Criteria");
      // if user closes criteria editor window, let them create a new one
      f.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e1) {
          criteriaButton.setEnabled(true);
        }
      });
      JPanel criteriaPanel = criteriaEditor.createDisplay(this);
      f.getContentPane().add(criteriaPanel);
      f.pack();
      f.setVisible(true);
      criteriaButton.setEnabled(false);
    } else if (command.equals(CriteriaEditor.APPLY)) {
      System.out.println("Applying new criteria");
      criteriaModel = criteriaEditor.getModel();
      applyNewCriteria();
    } else if (command.equals(SAVE)) {
      save();
    } else {
      System.out.println("WARNING: Unknown command ignored: " + command);
      return;
    }
    dataModel.fireTableStructureChanged();
    customizeColumnDisplay();
    initColumnSizes();
  }

  private void captureUserEvents() {
    glassPane = null;
    JFrame myFrame = 
      (JFrame)SwingUtilities.getAncestorOfClass(JFrame.class, updateButton);
    if (myFrame != null)
      glassPane = myFrame.getGlassPane();
    else {
      JApplet myApplet =
        (JApplet)SwingUtilities.getAncestorOfClass(JApplet.class, updateButton);
      if (myApplet != null)
        glassPane = myApplet.getGlassPane();
    }
    if (glassPane != null) {
      glassPane.setVisible(true);
      // disabled until cursor setting bug in swing is fixed
      // glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      glassPane.addMouseListener(new MouseAdapter() {});
      glassPane.addMouseMotionListener(new MouseMotionAdapter() {});
    } else
      System.out.println("WARNING: Glass pane is null");
  }

  private void applyNewCriteria() {
      if (statusFromClusters != null) {
          StatusHelper helper = new StatusHelper(statusFromClusters, criteriaModel);
          society = helper.getSociety();
          equipmentInfo = helper.getEquipmentInfo();
          societyStatus = helper.getSocietyStatus();
      }
    //    societyStatus.printSocietyStatus();
    String equipmentName = (String)dataModel.getColumnName(0);
    String unitName = unitButton.getText();
    StoplightModel newDataModel = 
      new StoplightModel(society, equipmentInfo, societyStatus);
    newDataModel.setUnitAndEquipment(unitName, equipmentName);
    table.setModel(newDataModel);
    dataModel = newDataModel;
    customizeColumnDisplay();
    initColumnSizes();
    legendModel.clear();
    for (int i = 0; i < criteriaModel.size(); i++)
      legendModel.addElement(criteriaModel.getElementAt(i));
    legendModel.addElement(new CriteriaParameters("", 0, "", 0, UIUnitStatus.GRAY));
    legendList.setModel(legendModel);
  }

  /** This is called when the user selects the update button.
    The construct method is run in a separate thread.  When it
    completes the event thread runs the finished method,
    updating the society, equipmentInfo, and societyStatus
    and redisplaying the table with this new information.
    */

  private void updateFromClusters() {
    final SwingWorker worker = new SwingWorker() {
      Hashtable totalStatus = null;
      String equipmentName;
      String unitName;

      public Object construct() {
        // get current unit and equipment superior names
        equipmentName = (String)dataModel.getColumnName(0);
        unitName = unitButton.getText();
        return getDataFromClusters(); // ignored
      }
      
      private Hashtable getDataFromClusters() {
        Hashtable status = null;
        InputStream is = null;
        for (int i = 0; i < clusterURLs.size(); i++) {
          String clusterURL = (String)clusterURLs.elementAt(i);
          System.out.println("Getting status from cluster: " + clusterURL);
          try {
            ConnectionHelper connection = 
              new ConnectionHelper(clusterURL, PSP_package, PSP_id);
            connection.sendData(request);
            is = connection.getInputStream();
            ObjectInputStream p = new ObjectInputStream(is);
            status = (Hashtable)p.readObject();
          } catch (Exception e) {
            System.out.println("Object read exception: " + e);
          }
          System.out.println("Received results");
          if (status == null) {
            System.out.println("No data");
            continue;
          }
          if (status.isEmpty()) {
            System.out.println("No data");
            continue;
          }
          //      printMetrics(status);
          if (totalStatus == null)
            totalStatus = status;
          else
            totalStatus.putAll(status);
        }
        return totalStatus;
      }

      public void finished() {
        if (totalStatus != null) {
          StatusHelper helper = new StatusHelper(totalStatus, criteriaModel);
          society = helper.getSociety();
          equipmentInfo = helper.getEquipmentInfo();
          societyStatus = helper.getSocietyStatus();
          StoplightModel newDataModel = 
            new StoplightModel(society, equipmentInfo, societyStatus);
          newDataModel.setUnitAndEquipment(unitName, equipmentName);
          table.setModel(newDataModel);
          dataModel = newDataModel;
          customizeColumnDisplay();
          initColumnSizes();
          glassPane = null;
          JFrame myFrame = 
            (JFrame)SwingUtilities.getAncestorOfClass(JFrame.class, updateButton);
          if (myFrame != null)
            glassPane = myFrame.getGlassPane();
          else {
            JApplet myApplet =
              (JApplet)SwingUtilities.getAncestorOfClass(JApplet.class, updateButton);
            if (myApplet != null)
              glassPane = myApplet.getGlassPane();
          }
          if (glassPane != null) {
            glassPane.setVisible(false);
            // disabled until cursor setting bug in swing is fixed
            // glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          } else
            System.out.println("WARNING: Glass pane is null");
          updateButton.setText(UPDATE);
          updateButton.setEnabled(true);
        }
      }
    };
  }

  /** Initialize column sizes to be big enough for the data displayed.
    For all but the first column, use the width of the header.
    For the first column, get the maximum of the values (equipment names)
    displayed, but make the column no smaller than minimumCellWidth.
    Set min, max, and preferred widths on the columns, otherwise it
    doesn't work.
    */

  private void initColumnSizes() {
    TableColumn column = null;
    Component comp = null;
    int minimumCellWidth = 100;
    int nColumns = dataModel.getColumnCount();
    int nRows = table.getRowCount();

    // for all but the first column, use the width of the header
    for (int i = 0; i < nColumns; i++) {
     column = table.getColumnModel().getColumn(i);
     comp = column.getHeaderRenderer().
       getTableCellRendererComponent(null, column.getHeaderValue(), 
                                     false, false, 0, 0);
     int w = Math.max(comp.getPreferredSize().width + CELLPADDING, 
                      minimumCellWidth);
     for (int j = 0; j < nRows; j++) {
       comp = table.getDefaultRenderer(dataModel.getColumnClass(0)).
         getTableCellRendererComponent(
                                       table, dataModel.getValueAt(j, i),
                                       false, false, j, 0);
       w = Math.max(comp.getPreferredSize().width + CELLPADDING, w);
     }
     column.setPreferredWidth(w);
     column.setMinWidth(w);
     column.setMaxWidth(w);
    }
    table.revalidate();
  }


  public static void main(String[] args) {
    JFrame f = new JFrame("Supplies");
    SupplyStoplight b = new SupplyStoplight();
    f.getContentPane().add(b);
    f.pack();
    f.setVisible(true);
  }


 
  /** Called when mouse is double clicked in table.
    Ignores all double clicks except in column 0.
    Does drill down on equipment.
    */

class MouseTableListener extends MouseAdapter {

  public void mouseClicked(MouseEvent e) {
    int nClicks = e.getClickCount();
    int mousedRow = table.rowAtPoint(e.getPoint());
    int mousedColumn = table.columnAtPoint(e.getPoint());
    if (nClicks != 2)
      return;
    if (mousedColumn != 0)
      return;
    String equipmentName = (String)dataModel.getValueAt(mousedRow, 0);
    if (equipmentInfo.getEquipment(equipmentName).getLevel() == 0)
      return;
    dataModel.modify(StoplightModel.DRILLDOWNEQUIPMENT, 
                     mousedRow, mousedColumn);
    dataModel.fireTableStructureChanged();
    customizeColumnDisplay();
    initColumnSizes();
  }
}

  /** Called when mouse is double clicked in column headers.
    Does drill down on unit and drill up on equipment (when in column 0).
    */

class MouseTableHeaderListener extends MouseAdapter {

  public void mouseClicked(MouseEvent e) {
    int nClicks = e.getClickCount();
    int mousedRow = table.rowAtPoint(e.getPoint());
    int mousedColumn = table.columnAtPoint(e.getPoint());
    if (nClicks != 2)
      return;
    if (mousedColumn == 0) {
      String equipmentName = dataModel.getColumnName(0);
      if (equipmentName.equals(equipmentInfo.TOPEQUIPMENT)) // top of chain
        return;
      dataModel.modify(StoplightModel.DRILLUPEQUIPMENT, 
                       mousedRow, mousedColumn);
    } else {
      String unitName = dataModel.getColumnName(mousedColumn);
      if (society.getUnit(unitName).getLevel() == 0)
        return;
      unitButton.setText(unitName);
      dataModel.modify(StoplightModel.DRILLDOWNUNIT, 
                       mousedRow, mousedColumn);
    }
    dataModel.fireTableStructureChanged();
    customizeColumnDisplay();
    initColumnSizes();
  }
}

  class StoplightColumnHeaderRenderer extends JLabel implements TableCellRenderer {

    public StoplightColumnHeaderRenderer() {
      super();
      this.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      setHorizontalAlignment(JLabel.CENTER);
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, int column) {
      setText(value.toString());
      return this;
    }

    // the getToolTipText method gets called, but not the 
    // getToolTipLocation method
    //    public String getToolTipText() {
    //      return "my tool tip text";
    //    }

    //    public Point getToolTipLocation(MouseEvent e) {
    //      System.out.println("Calling get tool tip location");
    //      return new Point(0, 0);
    //    }


  }

}

