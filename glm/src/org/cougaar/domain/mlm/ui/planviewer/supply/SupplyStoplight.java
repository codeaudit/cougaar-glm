/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.supply;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;

import org.cougaar.util.ThemeFactory;

import org.cougaar.domain.mlm.ui.data.UISupplyStatus;
import org.cougaar.domain.mlm.ui.data.UIUnitStatus;
import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.domain.mlm.ui.planviewer.XMLClientConfiguration;

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
  JTable table;
  JLabel title;
  JButton unitButton;
  JButton updateButton;
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
  int selectedRow = -1;
  int selectedColumn = -1;
  // for contacting the society for updates
  Vector clusterURLs;
  String PSP_package;
  String PSP_id;
  String request;
  Component glassPane;
  static final String RED_LEGEND = ">  20%  >  1 day late or  > 10% >  6 days late";
  static final String YELLOW_LEGEND = ">= 10%  >  1 day late or  >= 5%  >  6 days late";
  static final String GREEN_LEGEND = "<  10%  >=  1 day late and  < 5%  >  6 days late";
  static final String GRAY_LEGEND = "Not applicable";

  public SupplyStoplight(Society society, EquipmentInfo equipmentInfo,
                         SocietyStatus societyStatus,
                         Vector clusterURLs, String PSP_package, 
                         String PSP_id, String request) {
    this.society = society;
    this.equipmentInfo = equipmentInfo;
    this.societyStatus = societyStatus;
    this.clusterURLs = clusterURLs;
    this.PSP_package = PSP_package;
    this.PSP_id = PSP_id;
    this.request = request;
    dataModel = new StoplightModel(society, equipmentInfo, societyStatus);
    createStoplightChart();
  }

  /** Display default society and equipment list and status.
   */

  public SupplyStoplight() {
    clusterURLs = null;
    society = new Society();
    society.createDefaultSociety();
    equipmentInfo = new EquipmentInfo();
    equipmentInfo.createDefaultEquipmentInfo();
    societyStatus = new SocietyStatus();
    setDefaultStatus();
    dataModel = new StoplightModel(society, equipmentInfo, societyStatus);
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
    if (request.equals(SupplyController.REQUEST_SUPPLY))
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
    
    JPanel legendPanel = new JPanel();
    legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
    legendPanel.add(createLabel(RED_LEGEND, ThemeFactory.getCougaarRed()));
    legendPanel.add(createLabel(YELLOW_LEGEND, ThemeFactory.getCougaarYellow()));
    legendPanel.add(createLabel(GREEN_LEGEND, ThemeFactory.getCougaarGreen()));
    legendPanel.add(createLabel(GRAY_LEGEND, Color.gray));
    legendPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new BorderLayout());
    bottomPanel.add(legendPanel, BorderLayout.NORTH);
    bottomPanel.add(createButtonsPanel(), BorderLayout.SOUTH);
    add(bottomPanel, BorderLayout.SOUTH);
    // end new code

    //    add(createButtonsPanel(), BorderLayout.SOUTH);

    table.addMouseListener(new MouseTableListener());
    table.getTableHeader().addMouseListener(new MouseTableHeaderListener());
  }

    private JLabel createLabel(String s, Color c) {
    JLabel l = new JLabel(s, new ColoredSquare(c), SwingConstants.TRAILING);
    l.setFont(new Font("SansSerif", Font.BOLD, 14));
    l.setForeground(Color.black);
    return l;
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
    panel.add(createButton("Save", false));
    updateButton = createButton(UPDATE, true);
    updateButton.setToolTipText("Click to update status from the cluster society.");
    panel.add(updateButton);
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
      getDataFromClusters();
      updateButton.setText("Updating");
      updateButton.setEnabled(false);
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
    } else {
      System.out.println("WARNING: Unknown command ignored: " + command);
      return;
    }
    dataModel.fireTableStructureChanged();
    customizeColumnDisplay();
    initColumnSizes();
  }

  /** This is called when the user selects the update button.
    The construct method is run in a separate thread.  When it
    completes the event thread runs the finished method,
    updating the society, equipmentInfo, and societyStatus
    and redisplaying the table with this new information.
    */

  private void getDataFromClusters() {
    final SwingWorker worker = new SwingWorker() {
      UISupplyStatus status = null;
      String equipmentName;
      String unitName;

      public Object construct() {
        // get current unit and equipment superior names
        equipmentName = (String)dataModel.getColumnName(0);
        unitName = unitButton.getText();
        InputStream is = null;
        UISupplyStatus bnStatus = null;
        for (int i = 0; i < clusterURLs.size(); i++) {
          String clusterURL = (String)clusterURLs.elementAt(i);
          try {
            ConnectionHelper connection = 
              new ConnectionHelper(clusterURL, PSP_package, PSP_id);
            connection.sendData(request);
            // ADD TIMEOUT HERE
            is = connection.getInputStream();
            ObjectInputStream p = new ObjectInputStream(is);
            if (status == null)
              status = (UISupplyStatus)p.readObject();
            else
              bnStatus = (UISupplyStatus)p.readObject();
          } catch (Exception e) {
            System.out.println("Couldn't read reply: " + e);
            bnStatus = null;
          }
          // add this battalion's status to statuses from entire society
          if (bnStatus != null) {
            Vector bnStatuses = bnStatus.getUnitStatuses();
            for (int j = 0; j < bnStatuses.size(); j++)
              status.addUnitStatus((UIUnitStatus)bnStatuses.elementAt(j));
          }
        }
        return status; // ignored
      }

      public void finished() {
        if (status != null) {
          StatusHelper helper = new StatusHelper(status);
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




    class ColoredSquare implements Icon {
        Color color;
        public ColoredSquare(Color c) {
            this.color = c;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color oldColor = g.getColor();
            g.setColor(color);
            g.fill3DRect(x,y,getIconWidth(), getIconHeight(), true);
            g.setColor(oldColor);
        }
        public int getIconWidth() { return 12; }
        public int getIconHeight() { return 12; }

    }

}

  

    


