/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Properties;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;

public class FilterGUI extends JScrollPane {
  protected static final int     ENABLE_COLUMN = 0;
  protected static final int     ASPECT_COLUMN = 1;
  protected static final int COMPARISON_COLUMN = 2;
  protected static final int    PATTERN_COLUMN = 3;
  protected static final int        ADD_COLUMN = 4;
  protected static final int       LAST_COLUMN = 4;

  private int row = 0;
  protected EventGenerator theEventGenerator;
  private FilterAspect[] theAspects;
  private FilterTableModel model;
  private JTable table;

  /**
   * The data about one filter item.
   **/
  protected class FilterItem implements FilteredSet.Filter {
    private boolean isEnabled;                     // True if the item is enabled
    private FilterAspect theAspect;                // Which aspect of the filter is to be tested
    private FilterAspect.Comparison theComparison; // Which comparison is to be performed
    private FilterAspect.ParsedPattern thePattern; // The data pattern against which the aspect
						   // is to be compared

    public FilterItem() {
      isEnabled = true;
      theAspect = FilterAspect.nullFilterAspect;
      theComparison = theAspect.theComparisons[0];
      setPattern("");
    }

    public boolean isEmpty() {
      return (getAspect() == FilterAspect.nullFilterAspect
              && getPattern().trim().equals(""));
    }

    public Boolean isEnabled() {
      return new Boolean(isEnabled);
    }

    public void setEnabled(Boolean newEnabled) {
      if (newEnabled == null) return;
      isEnabled = newEnabled.booleanValue();
    }

    public FilterAspect getAspect() {
      return theAspect;
    }

    public void setAspect(String newAspectName) {
      for (int i = 0; i < theAspects.length; i++) {
        if (theAspects[i].theAspectName.equals(newAspectName)) {
          setAspect(theAspects[i]);
          return;
        }
      }
      throw new IllegalArgumentException("Filter aspect not found: " + newAspectName);
    }

    public void setAspect(FilterAspect newAspect) {
      /**
       * A new aspect has been selected. The comparisons and pattern are
       * adjusted to be compatible with the new aspect. If the selected
       * item of the current comparisons is in the set of possible
       * comparisons of the new aspect, the corresponding item is
       * selected in the new comparisons. Similarly, the selected
       * pattern of the new aspect is set to match the current pattern,
       * if possible.
       **/
      if (newAspect == null) return;
      theAspect = newAspect;
      setComparison(theComparison.getName()); // Use the comparison with the same name
    }

    public void setComparison(String newComparisonName) {
      FilterAspect.Comparison newComparison = theAspect.theComparisons[0];
      for (int i = 0, n = theAspect.theComparisons.length; i < n; i++) {
        FilterAspect.Comparison aComparison = theAspect.theComparisons[i];
        if (aComparison.getName().equals(newComparisonName)) {
          newComparison = aComparison;
          break;
        }
      }
      setComparison(newComparison);
    }

    public FilterAspect.Comparison getComparison() {
      return theComparison;
    }

    public void setComparison(FilterAspect.Comparison newComparison) {
      if (newComparison == null) return;
      theComparison = newComparison;
    }

    public String getPattern() {
      return thePattern.toString();
    }
    public void setPattern(String newPattern) {
      if (newPattern == null) return;
      FilterAspect.ParsedPattern parsedPattern = theAspect.parsePattern(newPattern);
      if (parsedPattern != null) {
        thePattern = parsedPattern;
      }
    }

    public FilteredSet.Filter getFilter() {
      if (isEnabled) return this;
      return null;
    }

    // Implementation of FilteredSet.Filter
    
    public Class getElementClass() {
      return theAspect.theApplicableClass;
    }

    public boolean apply(Object object) {
      if (getElementClass().isAssignableFrom(object.getClass())) {
        Object item = theAspect.getItem(object);
        return theComparison.apply(item, thePattern);
      }
      return false;
    }
  }

  private class FilterTableModel extends EGTableModelBase implements EGTableModel {
    public List filters = new ArrayList();

    public synchronized void addFilterItem(int row, FilterItem newFilterItem) {
      filters.add(row, newFilterItem);
      fireTableRowsInserted(row, row);
    }

    public synchronized void removeFilterItem(int row) {
      filters.remove(row);
      fireTableRowsDeleted(row, row);
    }

    public synchronized void addFilters(FilteredSet filteredSet) {
      for (Iterator i = filters.iterator(); i.hasNext(); ) {
        FilterItem filterItem = (FilterItem) i.next();
        FilteredSet.Filter filter = filterItem.getFilter();
        if (filter != null) filteredSet.addFilter(filter);
      }
    }

    public int getColumnCount() {
      return LAST_COLUMN + 1;
    }

    public String getColumnName(int col) {
      switch (col) {
      case ENABLE_COLUMN: return "Enable";
      case ASPECT_COLUMN: return "Feature";
      case COMPARISON_COLUMN: return "Comparison";
      case PATTERN_COLUMN: return "Pattern";
      case ADD_COLUMN: return "";
      }
      return null;
    }

    public Class getColumnClass(int col) {
      switch (col) {
      case ENABLE_COLUMN: return Boolean.class;
      case ASPECT_COLUMN: return FilterAspect.class;
      case COMPARISON_COLUMN: return FilterAspect.Comparison.class;
      case PATTERN_COLUMN: return String.class;
      case ADD_COLUMN: return JButton.class;
      }
      return Object.class;
    }

    public int getMinColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN: return new JCheckBox("").getPreferredSize().width;
      case ASPECT_COLUMN: return getWidestAspectWidth();
      case COMPARISON_COLUMN: return getWidestComparisonWidth();
      case PATTERN_COLUMN: return getWidestPatternWidth();
      case ADD_COLUMN: return getCellWidth("Add");
      }
      return 75;
    }

    public int getMaxColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN: return getCellWidth("Enable");
      case ASPECT_COLUMN: return getWidestAspectWidth();
      case COMPARISON_COLUMN: return getWidestComparisonWidth();
      case PATTERN_COLUMN: return Integer.MAX_VALUE;
      case ADD_COLUMN: return getCellWidth("Add");
      }
      return 75;
    }

    public int getPreferredColumnWidth(int col) {
      switch (col) {
      case ENABLE_COLUMN: return getCellWidth("Enable");
      case ASPECT_COLUMN: return getWidestAspectWidth();
      case COMPARISON_COLUMN: return getWidestComparisonWidth();
      case PATTERN_COLUMN: return getWidestPatternWidth();
      case ADD_COLUMN: return getCellWidth("Add");
      }
      return 75;
    }

    private int theWidestAspectWidth = -1;

    private int getWidestAspectWidth() {
      if (theWidestAspectWidth < 0) {
        theWidestAspectWidth = 40;
        for (int i = 0; i < theAspects.length; i++) {
          theWidestAspectWidth = Math.max(theWidestAspectWidth,
                                          getCellWidth(theAspects[i].theAspectName));
        }
      }
      return theWidestAspectWidth;
    }

    private int theWidestComparisonWidth = -1;

    private int getWidestComparisonWidth() {
      if (theWidestComparisonWidth < 0) {
        theWidestComparisonWidth = 40;
        for (int i = 0; i < theAspects.length; i++) {
          FilterAspect.Comparison[] theComparisons = theAspects[i].theComparisons;
          for (int j = 0; j < theComparisons.length; j++) {
            theWidestComparisonWidth = Math.max(theWidestComparisonWidth,
                                                getCellWidth(theComparisons[j].getName()));
          }
        }
      }
      return theWidestComparisonWidth;
    }

    private int theWidestPatternWidth = -1;

    private int getWidestPatternWidth() {
      if (theWidestPatternWidth < 0) {
        theWidestPatternWidth = getCellWidth("12/12/2000 23:59 (Wed)");
        for (int i = 0; i < theAspects.length; i++) {
          String[] thePatterns = theAspects[i].theCommonPatterns;
          for (int j = 0; j < thePatterns.length; j++) {
            theWidestPatternWidth = Math.max(theWidestPatternWidth,
                                             getCellWidth(thePatterns[j]));
          }
        }
      }
      return theWidestPatternWidth;
    }

    public synchronized int getRowCount() {
      return filters.size();
    }

    public synchronized FilterItem getRowObject(int row) {
      return (FilterItem) filters.get(row);
    }

    public synchronized boolean cellHasBeenEdited(int row, int col) {
      return false;
    }

    public synchronized boolean isCellEditable(int row, int col) {
      FilterItem filterItem = getRowObject(row);
      if (filterItem == null) return false;
      switch (col) {
      case ENABLE_COLUMN: return true;
      case ASPECT_COLUMN: return true;
      case COMPARISON_COLUMN: return true;
      case PATTERN_COLUMN: return true;
      case ADD_COLUMN: return false;
      }
      return false;
    }

    public synchronized Object getValueAt(int row, int col) {
      FilterItem filterItem = getRowObject(row);
      if (filterItem == null) return null;
      switch (col) {
      case ENABLE_COLUMN: return filterItem.isEnabled();
      case ASPECT_COLUMN: return filterItem.getAspect();
      case COMPARISON_COLUMN: return filterItem.getComparison();
      case PATTERN_COLUMN: return filterItem.getPattern();
      case ADD_COLUMN: return "&&";
      }
      return null;
    }

    public synchronized void setValueAt(Object newValue, int row, int col) {
      FilterItem filterItem = getRowObject(row);
      if (filterItem == null) return;
      switch (col) {
      case ENABLE_COLUMN:
        filterItem.setEnabled((Boolean) newValue);
        break;
      case ASPECT_COLUMN:
        filterItem.setAspect((FilterAspect) newValue);
        break;
      case COMPARISON_COLUMN:
        filterItem.setComparison((FilterAspect.Comparison) newValue);
        break;
      case PATTERN_COLUMN:
        filterItem.setPattern((String) newValue);
        break;
      case ADD_COLUMN:
        return;
      }
      fireTableRowsUpdated(row, row);
    }
  }

  private class FilterAspectEditor extends DefaultCellEditor {
    public FilterAspectEditor(FilterAspect[] someAspects) {
      super(new JComboBox(someAspects));
    }
  }

  /**
   * Customize the DefaultCellEditor to switch JComboBoxes depending
   * on the FilterAspect of the row being edited. The JComboBoxes are
   * maintained in a HashMap keyed by the FilterAspect of the row and
   * the editorComponent of the superclass is switched as needed.
   **/
  private abstract class SwitchedJComboBoxEditor extends DefaultCellEditor {
    public SwitchedJComboBoxEditor() {
      super(new JComboBox());
    }

    protected abstract Object[] getItems(FilterAspect theAspect);

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int col)
    {
      FilterItem filterItem = model.getRowObject(row);
      FilterAspect theAspect = filterItem.getAspect();
      JComboBox comboBox = (JComboBox) editorComponent;
      comboBox.removeAllItems();
      Object[] items = getItems(theAspect);
      for (int i = 0; i < items.length; i++) {
        comboBox.addItem(items[i]);
      }
      comboBox.setEditable(items[0].equals(""));
      return super.getTableCellEditorComponent(table, value, isSelected, row, col);
    }
  }

  /**
   * Customize the SwitchedJComboBoxEditor based on theComparisons of
   * the FilterAspect of the row being edited.
   **/
  private class ComparisonEditor extends SwitchedJComboBoxEditor {
    protected Object[] getItems(FilterAspect theAspect) {
      return theAspect.theComparisons;
    }
  }

  /**
   * Customize the DefaultCellEditor based on theCommonPatterns of the
   * FilterAspect of the row being edited.
   **/
  private class PatternEditor extends SwitchedJComboBoxEditor {
    protected Object[] getItems(FilterAspect theAspect) {
      return theAspect.theCommonPatterns;
    }
  }

  public FilterGUI(EventGenerator anEventGenerator) {
    theEventGenerator = anEventGenerator;
    theAspects = getAspects();
    model = new FilterTableModel();
    table = new JTable(model);
    table.setRowHeight(new JLabel("XXX").getPreferredSize().height);
    table.setColumnSelectionAllowed(false);
    table.setRowSelectionAllowed(false);
    table.setDefaultEditor(FilterAspect.class, new FilterAspectEditor(theAspects));
    table.setDefaultEditor(FilterAspect.Comparison.class, new ComparisonEditor());
    table.getColumnModel()
      .getColumn(PATTERN_COLUMN)
      .setCellEditor(new PatternEditor());
    for (int col = 0; col <= LAST_COLUMN; col++) {
      table.getColumnModel().getColumn(col).setPreferredWidth(model.getPreferredColumnWidth(col));
      table.getColumnModel().getColumn(col).setMinWidth(model.getMinColumnWidth(col));
      table.getColumnModel().getColumn(col).setMaxWidth(model.getMaxColumnWidth(col));
    }
    table.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int col = table.columnAtPoint(e.getPoint());
        if (col == ADD_COLUMN) {
          int row = table.rowAtPoint(e.getPoint());
          FilterItem item = model.getRowObject(row);
          if (item.isEmpty()) {
            if (model.getRowCount() > 1) model.removeFilterItem(row);
          } else {
            addFilter(row + 1);
          }
        }
      }
    });
    table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredSize().width,
                                                           400));
    setViewportView(table);
    addFilter(0);
  }

  public void addFilter(int row) {
    model.addFilterItem(row, new FilterItem());
  }

  public void addFilters(FilteredSet filteredSet) {
    model.addFilters(filteredSet);
  }

  public Dimension getMinimumSize() {
    Dimension minimumSize = super.getMinimumSize();
    if (minimumSize.height > 300) return minimumSize;
    return new Dimension(minimumSize.width, 300);
  }

  public Dimension getPreferredSize() {
    Dimension preferredSize = super.getPreferredSize();
    if (preferredSize.height > 300) return preferredSize;
    return new Dimension(preferredSize.width, 300);
  }

  private FilterAspect[] getAspects() {
    ArrayList aspects = new ArrayList();
    aspects.add(FilterAspect.nullFilterAspect);
    aspects.add(new FilterAspect.Str_ng("Cluster",
                                        Timed.class,
                                        FilterAspect.stringComparisons,
                                        getClusterPatterns()) {
      public Object getItem(Object from) {
        return ((Timed) from).getCluster().toUpperCase();
      }
    });
    addAspects(aspects);
    return (FilterAspect[]) aspects.toArray(new FilterAspect[aspects.size()]);
  }

  protected void addAspects(List aspects) {
  }

  private String[] getClusterPatterns() {
    String[] names = theEventGenerator.getClusterNames();
    String[] result = new String[names.length + 1];
    result[0] = "";
    System.arraycopy(names, 0, result, 1, names.length);
    return result;
  }

  public synchronized void save(Properties props, String prefix) {
    props.setProperty(prefix + "count", Integer.toString(model.filters.size()));
    for (int i = 0, n = model.filters.size(); i < n; i++) {
      String thisPrefix = prefix + i + ".";
      FilterItem filterItem = (FilterItem) model.filters.get(i);
      props.setProperty(thisPrefix + "aspect", filterItem.getAspect().theAspectName);
      props.setProperty(thisPrefix + "enabled", filterItem.isEnabled().toString());
      props.setProperty(thisPrefix + "comparison", filterItem.getComparison().getName());
      props.setProperty(thisPrefix + "pattern", filterItem.getPattern());
    }
  }

  public synchronized void restore(Properties props, String prefix) {
    String countString = props.getProperty(prefix + "count");
    if (countString == null) return;
    int n = Integer.parseInt(countString);
    if (n > 0) {
      if (model.getRowObject(0).isEmpty()) model.removeFilterItem(0);
      for (int i = 0; i < n; i++) {
        String thisPrefix = prefix + i + ".";
        FilterItem filterItem = new FilterItem();
        String aspectName = props.getProperty(thisPrefix + "aspect");
        if (aspectName.equals("")) continue;
        filterItem.setAspect(aspectName);
        filterItem.setEnabled(Boolean.valueOf(props.getProperty(thisPrefix + "enabled")));
        filterItem.setComparison(props.getProperty(thisPrefix + "comparison"));
        filterItem.setPattern(props.getProperty(thisPrefix + "pattern"));
        model.addFilterItem(i, filterItem);
      }
      if (model.getRowCount() == 0) {
        addFilter(0);
      }
    }
  }
}
