package org.cougaar.domain.glm.execution.eg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Date;
import javax.swing.Action;
import javax.swing.ListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.cougaar.util.TableSorter;

/**
 * This GUI displays information about the scheduled and actual
 * reports. the display shows the actual or scheduled time for the
 * report, the item identity, and amount (if actual).
 **/
public class ReportManagerGUI extends JPanel {
  public static final Color EDITED_COLOR = Color.red;
  public static final Color NORMAL_COLOR = Color.black;
  public static final Color ODD_ROW_BACKGROUND = Color.white;
  public static final Color EVEN_ROW_BACKGROUND = new Color(240, 255, 240);
  public static final Color MARKED_CELL_BACKGROUND = new Color(150, 200, 255);

  protected JToolBar toolBar = new JToolBar();
  protected JCheckBox autoSend = new JCheckBox("Auto Send");
  protected JCheckBox autoSendInvisible = new JCheckBox("Auto Send Invisible");
  protected ManagerBase theManager;

  private static class MyTable extends JTable {
    public MyTable(TableModel model) {
      super(model);
      DefaultTableCellRenderer numberRenderer = new DefaultTableCellRenderer() {
        NumberFormat doubleFormatter = NumberFormat.getInstance();
        NumberFormat otherFormatter = NumberFormat.getInstance();
        public void setValue(Object value) {
          if (value instanceof Double) {
            double d = ((Double) value).doubleValue();
            setHorizontalAlignment(JLabel.RIGHT);
            if (Math.floor(d) == d) {
              doubleFormatter.setMinimumFractionDigits(0);
              doubleFormatter.setMaximumFractionDigits (0);
              setText(doubleFormatter.format(value) + ".   ");
            } else {
              doubleFormatter.setMinimumFractionDigits(3);
              doubleFormatter.setMaximumFractionDigits(3);
              setText(doubleFormatter.format(value));
            }
          } else if (value instanceof Number) {
            setHorizontalAlignment(JLabel.RIGHT);
            setText(otherFormatter.format(value));
          } else if (value instanceof Date) {
            setText((value == null) ? "" : EGDate.format((Date) value));
          } else {
            setHorizontalAlignment(JLabel.LEFT);
            setText((value == null ? "" : value.toString()));
          }
        }
      };
      numberRenderer.setHorizontalAlignment(JLabel.RIGHT);
      setDefaultRenderer(Number.class, numberRenderer);
      setDefaultRenderer(Object.class, numberRenderer);
      setDefaultRenderer(String.class, numberRenderer);
      setDefaultRenderer(Date.class, numberRenderer);
      setDefaultRenderer(EGDate.class, numberRenderer);
    }

    public void setDefaultRenderer(Class columnClass, TableCellRenderer renderer) {
      super.setDefaultRenderer(columnClass, new RendererWrapper(renderer));
    }
  }

  private static Font defaultFont = null;
  private static Font numberFont = null;
  private static Font firstNumberFont = null;
  private static Font firstFont = null;

  public static Font getDefaultFont() {
    if (defaultFont == null) defaultFont = new JTable().getFont();
    return defaultFont;
  }

  public static Font getFirstFont() {
    if (firstFont == null) {
      firstFont = new Font(getDefaultFont().getName(), Font.BOLD, getDefaultFont().getSize());
    }
    return firstFont;
  }

  public static Font getNumberFont() {
    if (numberFont == null) {
      numberFont = new Font("Monospaced", Font.PLAIN, getDefaultFont().getSize());
    }
    return numberFont;
  }

  public static Font getFirstNumberFont() {
    if (firstNumberFont == null) {
      firstNumberFont = new Font("Monospaced", Font.BOLD, getDefaultFont().getSize());
    }
    return firstNumberFont;
  }

  private static class RendererWrapper implements TableCellRenderer {
    private TableCellRenderer renderer;

    public RendererWrapper(TableCellRenderer renderer) {
      this.renderer = renderer;
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column)
    {
      JComponent result =
        (JComponent) renderer.getTableCellRendererComponent(table,
                                                            value,
                                                            isSelected,
                                                            hasFocus,
                                                            row,
                                                            column);
      EGTableModel model = (EGTableModel) table.getModel();
      boolean isFirst = model.cellIsFirst(row, column);
      if (value instanceof Number) {
        if (isFirst) {
          result.setFont(getFirstNumberFont());
        } else {
          result.setFont(getNumberFont());
        }
      } else {
        if (isFirst) {
          result.setFont(getFirstFont());
        }
      }
      if (model.cellHasBeenEdited(row, column)) {
        result.setForeground(EDITED_COLOR);
      } else {
        result.setForeground(NORMAL_COLOR);
      }
      if (model.cellIsMarked(row, column)) {
        result.setBackground(MARKED_CELL_BACKGROUND);
      } else if (((row / 5) & 1) == 0) {
        result.setBackground(EVEN_ROW_BACKGROUND);
      } else {
        result.setBackground(ODD_ROW_BACKGROUND);
      }
      result.setToolTipText(model.getToolTipText(row, column));
      return result;
    }
  };

  private static class MyTableSorter extends TableSorter implements EGTableModel {
    public MyTableSorter(EGTableModel model) {
      super(model);
    }
    public int getPreferredColumnWidth(int col) {
      return ((EGTableModel) model).getPreferredColumnWidth(col);
    }
    public int getMinColumnWidth(int col) {
      return ((EGTableModel) model).getMinColumnWidth(col);
    }
    public int getMaxColumnWidth(int col) {
      return ((EGTableModel) model).getMaxColumnWidth(col);
    }
    public String getToolTipText(int row, int col) {
      return ((EGTableModel) model).getToolTipText(row, col);
    }
    public boolean cellHasBeenEdited(int row, int col) {
      return ((EGTableModel) model).cellHasBeenEdited(row, col);
    }
    public boolean cellIsMarked(int row, int col) {
      return ((EGTableModel) model).cellIsMarked(row, col);
    }
    
    public boolean cellIsFirst(int row, int col) {
      if (row == 0) return true;
      Object thisValue = getValueAt(row, col);
      if (thisValue == null) return true;
      Object prevValue = getValueAt(row-1, col);
      if (prevValue == null) return true;
      return !thisValue.equals(prevValue);
    }
    public void fireTableDataChanged() {
      ((EGTableModel) model).fireTableDataChanged();
    }
    public void cellSelectionChanged(int row, int column) {
      ((EGTableModel) model).cellSelectionChanged(row, column);
    }
    public int compareRowsByColumn(int row1, int row2, int column) {
      return ((EGTableModel) model).compareRowsByColumn(row1, row2, column);
    }
    public void tableChanged(TableModelEvent e) {
      super.tableChanged(e);
      sort(e.getSource());
    }
  }

  public ReportManagerGUI(final EGTableModel tableModel, ManagerBase aManager) {
    super(new BorderLayout());
    theManager = aManager;
    ActionListener autoSendListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	if (autoSendInvisible.isSelected()) {
          new Thread("Deferred advanceTime") {
            public void run() {
              theManager.advanceTime();
            }
          }.start();
	}
      }
    };

    autoSend.setToolTipText("Automatically send reports when report time is due");
    autoSend.addActionListener(autoSendListener);
    addToToolBar(autoSend);

    autoSendInvisible.setToolTipText("Send undisplayed (filtered out) reports when due");
    autoSendInvisible.addActionListener(autoSendListener);
    addToToolBar(autoSendInvisible);

    add(toolBar, BorderLayout.NORTH);
//      final MyTableSorter tableSorter = new MyTableSorter(tableModel);
//      final JTable table = new MyTable(tableSorter);
//      tableSorter.addMouseListenerToHeaderInTable(table);
    final JTable table = new MyTable(tableModel);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setCellSelectionEnabled(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    ListSelectionListener l = new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
	int row = e.getFirstIndex();
	if (row != -1) {
	    tableModel.cellSelectionChanged(row, table.getSelectedColumn());
	}
      }
    };
    table.getSelectionModel().addListSelectionListener(l);
    table.getColumnModel().getSelectionModel().addListSelectionListener(l);
    int totalPreferredWidth = 0;
    for (int col = 0, ncols = tableModel.getColumnCount(); col < ncols; col++) {
      table.getColumnModel().getColumn(col).setMinWidth(tableModel.getMinColumnWidth(col));
      table.getColumnModel().getColumn(col).setMaxWidth(tableModel.getMaxColumnWidth(col));
      int preferredWidth = tableModel.getPreferredColumnWidth(col);
      table.getColumnModel().getColumn(col).setPreferredWidth(preferredWidth);
      totalPreferredWidth += preferredWidth;
    }
    table.setPreferredScrollableViewportSize
      (new Dimension(table.getPreferredSize().width,
                     table.getPreferredScrollableViewportSize().height));
    JScrollPane pane = new JScrollPane(table);
    pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    add(pane);
  }

  public boolean isAutoSend() {
    return autoSend.isSelected();
  }

  public void setAutoSend(boolean newAutoSend) {
    autoSend.setSelected(newAutoSend);
  }

  public boolean isAutoSendInvisible() {
    return autoSendInvisible.isSelected();
  }

  public void setAutoSendInvisible(boolean newAutoSendInvisible) {
    autoSendInvisible.setSelected(newAutoSendInvisible);
  }

  public JButton addToToolBar(Action action) {
    return toolBar.add(action);
  }

  public void addToToolBar(Component component) {
    toolBar.add(component);
  }
}

