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
 
package org.cougaar.domain.mlm.ui.report;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.*;

/**
 * Table for displaying Reports.
 */

public class ReportTable extends JTable {
  private static final int DATE_INDEX = 0;
  private static final String DATE_COLUMN_HEADER = "Date";
  private static final int DATE_COLUMN_WIDTH = 150;
  
  private static final int TEXT_INDEX = 1;
  private static final String TEXT_COLUMN_HEADER = "Text";
  private static final int TEXT_COLUMN_WIDTH = 750;

  /**
   * constructor - sets table column model to ReportTableColumnModel
   */
  public ReportTable() {
    super();

    setAutoResizeMode(AUTO_RESIZE_OFF);

    setColumnModel(new ReportTableColumnModel());
    setAutoCreateColumnsFromModel(false);

    getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setRowSelectionAllowed(true);
    setColumnSelectionAllowed(false);
    setCellSelectionEnabled(false);
  }

  /**
   * updateModel - uses Vector of Vectors to update the table model. Each 
   * element of rows should be a vector with 2 elements - date and text.
   * Column info taken from ReportTableColumnModel.
   * 
   * @see javax.swing.table.DefaultTableModel.setDataVector.
   * 
   * @param rows Vector of row vectors.
   */
  public void updateModel(Vector rows) {
    Vector columnHeaders = new Vector();

    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      TableColumn column = getColumnModel().getColumn(i);
      columnHeaders.add(column.getHeaderValue());
    }
    
    ((DefaultTableModel)getModel()).setDataVector(rows, columnHeaders);
  }
    
  /**
   * ReportTableColumnModel - column model for ReportTable
   */
  class ReportTableColumnModel extends DefaultTableColumnModel {
    
    private TableColumn []myTableColumns = 
    { new TableColumn(DATE_INDEX, DATE_COLUMN_WIDTH),
      new TableColumn(TEXT_INDEX, TEXT_COLUMN_WIDTH)
    };

    public ReportTableColumnModel() {
      super();
      
      myTableColumns[0].setHeaderValue(DATE_COLUMN_HEADER);
      myTableColumns[0].setPreferredWidth(DATE_COLUMN_WIDTH);

      myTableColumns[1].setHeaderValue(TEXT_COLUMN_HEADER);
      myTableColumns[1].setPreferredWidth(TEXT_COLUMN_WIDTH);

      this.addColumn(myTableColumns[0]);
      this.addColumn(myTableColumns[1]);
    }
  }
}





