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
 
package org.cougaar.domain.mlm.ui.views.policy;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.cougaar.domain.mlm.ui.producers.policy.*;

/**
 * PolicyTable - displays the parameters for a policy.
 * Uses customs renderers and editors for the column with the
 * parameter values.
 */

public class PolicyTable extends JTable {
  PolicyTableModel myModel;

  /**
   * Constructer - takes a PolicyTableModel
   *
   * @param model PolicyTableModel with the actual data
   */
  public PolicyTable(PolicyTableModel model) {
    super(model);

    myModel = model;

    // Adjust row height to account for font size.
    Font tableFont = UIManager.getFont("Table.font");
    FontMetrics fontMetrics = getFontMetrics(tableFont);
    int rowHeight = fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();
    
    // fudge factor
    rowHeight += (.33) * rowHeight;
    
    setRowHeight(Math.max(getRowHeight(), rowHeight));
    
    initDefaultRenderers();

    initDefaultEditors();
  }
  
  /**
   * getCellEditor - returns the appropriate editor for the specified
   * parameter. Overwrite JTable implementation to get the correct editor
   * for the parameter.  JTable implementation gets the editor based on the
   * Class associated with the column. In a column with mixed types like the
   * parameter values, this corresponds to getting the lcd editor. Our 
   * implementation returns the appropriate editor for the specified parameter.
   * 
   * @param row int specifying the row of the value to be edited
   * @param column int specifying the column of the value to be edited
   *
   * @return TableCellEditor - editor for the specified table cell.
   */
  public TableCellEditor getCellEditor(int row, int column) {
    if (column == 0) {
      return super.getCellEditor(row, column);
    }
    
    Object value = getValueAt(row, column);
    
    UIPolicyParameterInfo paramInfo = 
      (UIPolicyParameterInfo)myModel.getPolicy().getParameters().get(row);
    
    if (paramInfo.getType() == UIPolicyParameterInfo.ENUMERATION_TYPE) {
      DefaultCellEditor enumerationEditor = 
        (DefaultCellEditor)getDefaultEditor(paramInfo.getClass());
      
      JComboBox comboBox = (JComboBox)enumerationEditor.getComponent();
      
      ArrayList enums = 
        ((UIEnumerationParameterInfo)paramInfo).getEnumeration();
      comboBox.setModel(new DefaultComboBoxModel(enums.toArray()));
      return enumerationEditor;
    } else if (paramInfo.getType() == UIPolicyParameterInfo.RANGE_TYPE) {
      DefaultCellEditor rangeEditor = 
        (DefaultCellEditor)getDefaultEditor(paramInfo.getClass());
      
      JButton button = (JButton)rangeEditor.getComponent();
      button.setText("Default: " + paramInfo.getValue());
      return rangeEditor;
    } else if (paramInfo.getType() == UIPolicyParameterInfo.KEY_TYPE) {
      DefaultCellEditor keyEditor = 
        (DefaultCellEditor)getDefaultEditor(paramInfo.getClass());
      
      JButton button = (JButton)keyEditor.getComponent();
      button.setText("Default: " + paramInfo.getValue());
      return keyEditor;
    } else if (value != null) {
      return getDefaultEditor(value.getClass());
    }  else {
      return super.getCellEditor(row, column);
    }
  }

    /**
     * Returns true if the cell at <I>row</I> and <I>column</I>
     * is editable.  Overwrite default implementation so that we can 
     * invoke range/key editor even if the parameter is not
     * editable. Range/Key editor are the only way to see
     * all the components of a Range/Key parameter.
     * the value of that cell.
     *
     * @param   row      the row whose value is to be looked up
     * @param   column   the column whose value is to be looked up
     * @return  true if the cell is editable.
     */
  public boolean isCellEditable(int row, int column) { 
    UIPolicyParameterInfo paramInfo = 
      (UIPolicyParameterInfo)myModel.getPolicy().getParameters().get(row);
    
    if ((paramInfo.getType() == UIPolicyParameterInfo.KEY_TYPE) || 
        (paramInfo.getType() == UIPolicyParameterInfo.RANGE_TYPE)) { 
      // Can only see the entries with the range/key editor.
      // Rely on range/key editor to disable the ability to make changes.
      return true;
    } else {
      return super.isCellEditable(row, column);
    }
  }

  public void tableChanged(TableModelEvent e) {
    super.tableChanged(e);

    //If all the rows have changed - clear selection and remove editor
    //Don't understand why JTable isn't doing this - sounds like a bug
    //to me.
    //BOZO - had to look at the TableModelEvent code to figure this out so
    //don't count on long term viability.
    if ((e.getFirstRow() == 0) &&
        (e.getLastRow() == Integer.MAX_VALUE) &&
        (e.getColumn() == TableModelEvent.ALL_COLUMNS)) {
      clearSelection();
      removeEditor();
    }
  }

  /**
   * initDefaultEditors - set up default editors for parameters. 
   * Use JTable default for Strings
   */
  private void initDefaultEditors() {
    final IntegerField integerField = new IntegerField(0, 5);
    integerField.setHorizontalAlignment(IntegerField.RIGHT);
    
    PolicyCellEditor integerEditor = 
      new PolicyCellEditor(integerField) {
        //Override DefaultCellEditor's getCellEditorValue method
        //to return an Integer, not a String:
        public Object getCellEditorValue() {
          return new Integer(integerField.getValue());
        }
      };
    setDefaultEditor(Integer.class, integerEditor);

    final LongField longField = new LongField(0, 5);
    longField.setHorizontalAlignment(LongField.RIGHT);
    
    PolicyCellEditor longEditor = 
      new PolicyCellEditor(longField) {
        //Override DefaultCellEditor's getCellEditorValue method
        //to return an Long, not a String:
        public Object getCellEditorValue() {
          return new Long(longField.getValue());
        }
      };
    setDefaultEditor(Long.class, longEditor);
    
    final DoubleField doubleField = new DoubleField(0, 10, 
                                                    new DecimalFormat());
    doubleField.setHorizontalAlignment(DoubleField.RIGHT);
    
    DefaultCellEditor decimalEditor = 
      new PolicyCellEditor(doubleField) {
        //Override DefaultCellEditor's getCellEditorValue method
        //to return an Double, not a String:
        public Object getCellEditorValue() {
          return new Double(doubleField.getValue());
        }
      };
    setDefaultEditor(Double.class, decimalEditor);
    
    setDefaultEditor(UIEnumerationParameterInfo.class, 
                     new DefaultCellEditor(new JComboBox()));

    JButton button  = new JButton("Edit Range Parameter");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = getSelectedRow();
        UIRangeParameterInfo paramInfo = 
        (UIRangeParameterInfo)myModel.getPolicy().getParameters().get(index);

        RangeEditor.showEditor((JFrame)getTopLevelAncestor(), paramInfo);
      }
    });
    setDefaultEditor(UIRangeParameterInfo.class,
                     new InitRangeEditor(button));

    button  = new JButton("Edit Key Parameter");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = getSelectedRow();
        UIKeyParameterInfo paramInfo = 
        (UIKeyParameterInfo)myModel.getPolicy().getParameters().get(index);

        KeyEditor.showEditor((JFrame)getTopLevelAncestor(), paramInfo);
      }
    });
    setDefaultEditor(UIKeyParameterInfo.class,
                     new InitKeyEditor(button));
  }

  private class PolicyCellEditor extends DefaultCellEditor {
    
    public PolicyCellEditor(JTextField textField) {
      super(textField);
    }

    public boolean stopCellEditing() {
      boolean success;

      try {
        success = super.stopCellEditing();
      } catch (UIParameterInfoIllegalValueException piive) {
        success = false;
        
        //Make sure that current editor is removed
        removeEditor();

        JOptionPane.showMessageDialog(null,
                                      piive.getMessage(),
             
                         "Input Error",
                                      JOptionPane.ERROR_MESSAGE);
      }

      return success;
    }

    protected void fireEditingStopped() {
      super.fireEditingStopped();
    }
  } // end PolicyCellEditor

  /**
   * initDefaultRenderer - set up default renders for parameters. 
   * Use JTable default for Strings, Booleans
   */
  private void initDefaultRenderers() {
    DefaultTableCellRenderer integerRenderer =  new DefaultTableCellRenderer() {
      NumberFormat myFormatter = NumberFormat.getInstance();
            
      public void setValue(Object value) {
        setText((value == null) ? "" : myFormatter.format(value));
      }
    };
    integerRenderer.setHorizontalAlignment(JLabel.RIGHT);

    setDefaultRenderer(Integer.class, integerRenderer);
    setDefaultRenderer(Long.class, integerRenderer);

    DefaultTableCellRenderer doubleRenderer = new DefaultTableCellRenderer() {
      DecimalFormat myFormatter = new DecimalFormat();
            
      public void setValue(Object value) {
        setText((value == null) ? "" : myFormatter.format(value));
      }
    };
    doubleRenderer.setHorizontalAlignment(JLabel.RIGHT);
    setDefaultRenderer(Double.class, doubleRenderer);    

    setDefaultRenderer(UIEnumerationParameterInfo.class, 
                       new ComboBoxRenderer());

    setDefaultRenderer(UIRangeParameterInfo.class,
                       new RangeRenderer());

    setDefaultRenderer(UIKeyParameterInfo.class,
                       new KeyRenderer());

    // Renderer for String and Class parameters -
    // can't store as the default for String.class or we'll never get to
    // the PolicyCellRenderer code.
    setDefaultRenderer(UIPolicyParameterInfo.class, 
                       new DefaultTableCellRenderer());

    setDefaultRenderer(Object.class, new PolicyCellRenderer());
  }
  
  /**
   * PolicyCellRenderer - returns renderer based on the parameter type
   */
  private class PolicyCellRenderer extends DefaultTableCellRenderer {
    
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
      if ((column == 0) ||
          (myModel.getPolicy() == null)) {
        return super.getTableCellRendererComponent(table, 
                                                   value,
                                                   isSelected,
                                                   hasFocus,
                                                   row,
                                                   column);
      }
      
      UIPolicyParameterInfo policyParameter = 
        (UIPolicyParameterInfo)myModel.getPolicy().getParameters().get(row);

      TableCellRenderer renderer;
      String toolTipText = policyParameter.validDataInfo(); 

      switch (policyParameter.getType()) {
      case UIPolicyParameterInfo.BOOLEAN_TYPE:
        renderer = getDefaultRenderer(Boolean.class);
        break;

      case UIPolicyParameterInfo.INTEGER_TYPE:
        renderer = getDefaultRenderer(Integer.class);
        break;

      case UIPolicyParameterInfo.LONG_TYPE:
        renderer = getDefaultRenderer(Long.class);
        break;

      case UIPolicyParameterInfo.DOUBLE_TYPE:
        renderer = getDefaultRenderer(Double.class);
        break;
        
      case UIPolicyParameterInfo.ENUMERATION_TYPE:
        renderer = getDefaultRenderer(UIEnumerationParameterInfo.class);
        break;

      case UIPolicyParameterInfo.RANGE_TYPE:
        renderer = getDefaultRenderer(UIRangeParameterInfo.class);
        break;

      case UIPolicyParameterInfo.KEY_TYPE:
        renderer = getDefaultRenderer(UIKeyParameterInfo.class);
        break;

      default: 
        renderer = getDefaultRenderer(UIPolicyParameterInfo.class);
        break;
      }

      if (renderer instanceof JComponent) {
        ((JComponent)renderer).setToolTipText(toolTipText);
      }
      
      return renderer.getTableCellRendererComponent(table, 
                                                    value,
                                                    isSelected,
                                                    hasFocus,
                                                    row,
                                                    column);
    }
  } // End PolicyCellRenderer
  
  /**
   * ComboBoxRenderer - used for rendering UIEnumerationParameterInfo
   */
  private class ComboBoxRenderer extends JComboBox 
    implements TableCellRenderer {

    public ComboBoxRenderer() {
      super();
      setEditable(false);
    }

    public Component getTableCellRendererComponent(JTable table, 
                                                   Object value,
                                                   boolean isSelected, 
                                                   boolean hasFocus, 
                                                   int row, 
                                                   int column) {
      UIEnumerationParameterInfo enumParam = 
        (UIEnumerationParameterInfo)myModel.getPolicy().getParameters().get(row);

      setModel(new DefaultComboBoxModel(enumParam.getEnumeration().toArray()));
      setSelectedItem(value);

      if (isSelected) {
        super.setForeground(table.getSelectionForeground());
        super.setBackground(table.getSelectionBackground());
      }
      else {
        super.setForeground(table.getForeground());
        super.setBackground(table.getBackground());
      }

      return this;
    }
  } // End ComboBoxRenderer

  /**
   * RangeRenderer - used for rendering UIRangeParameterInfo
   */
  private class RangeRenderer extends DefaultTableCellRenderer
    implements TableCellRenderer {

    public RangeRenderer() {
      super();
    }

    public Component getTableCellRendererComponent(JTable table, 
                                                   Object value,
                                                   boolean isSelected, 
                                                   boolean hasFocus, 
                                                   int row, 
                                                   int column) {
      super.getTableCellRendererComponent(table, value, isSelected,
                                          hasFocus, row, column);
      UIRangeParameterInfo rangeParam = 
        (UIRangeParameterInfo)myModel.getPolicy().getParameters().get(row);

      if ((rangeParam.getValue() != null) &&
          (!rangeParam.getValue().equals(""))) {
        setValue("Default: " + rangeParam.getValue());
      } else {
        setValue("Default: UNDEFINED");
      }

      return this;
    }
  } // End RangeRenderer

  private class InitRangeEditor extends PolicyCellEditor {
    
    public InitRangeEditor(JButton button) {
      super(new JTextField());
      editorComponent = button;
      setClickCountToStart(1); //This is usually 1 or 2.
      
      //Must do this so that editing stops when appropriate.
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fireEditingStopped();
        }
      });
    }
  }

  /**
   * KeyRenderer - used for rendering UIKeyParameterInfo
   */
  private class KeyRenderer extends DefaultTableCellRenderer
    implements TableCellRenderer {

    public KeyRenderer() {
      super();
    }

    public Component getTableCellRendererComponent(JTable table, 
                                                   Object value,
                                                   boolean isSelected, 
                                                   boolean hasFocus, 
                                                   int row, 
                                                   int column) {
      super.getTableCellRendererComponent(table, value, isSelected,
                                          hasFocus, row, column);
      UIKeyParameterInfo keyParam = 
        (UIKeyParameterInfo)myModel.getPolicy().getParameters().get(row);

      if ((keyParam.getValue() != null) &&
          (!keyParam.getValue().equals(""))) {
        setValue("Default: " + keyParam.getValue());
      } else {
        setValue("Default: UNDEFINED");
      }

      return this;
    }
  } // End KeyRenderer

  private class InitKeyEditor extends PolicyCellEditor {
    
    public InitKeyEditor(JButton button) {
      super(new JTextField());
      editorComponent = button;
      setClickCountToStart(1); //This is usually 1 or 2.
      
      //Must do this so that editing stops when appropriate.
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fireEditingStopped();
        }
      });

    }
  } // end PolicyCellEditor

}
 













