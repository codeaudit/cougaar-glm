/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.views.policy;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.cougaar.domain.mlm.ui.producers.policy.UIBoundedParameterInfo;
import org.cougaar.domain.mlm.ui.producers.policy.UIPolicyInfo;
import org.cougaar.domain.mlm.ui.producers.policy.UIPolicyParameterInfo;

public class PolicyTableModel extends AbstractTableModel {
  private final String[] myColumnNames = {"Parameter", "Value"};
  private UIPolicyInfo myPolicy = null;
  private PolicyClient myClient = null;

  public UIPolicyInfo getPolicy() {
    return myPolicy;
  }
  
  public void setPolicy(UIPolicyInfo policy) {
    myPolicy = policy;
    fireTableChanged(new TableModelEvent(this));
  }


  public void setPolicyClient(PolicyClient client) {
    myClient = client;
  }

  public String getColumnName(int column) {
    return myColumnNames[column];
  }
  
  public Class getColumnClass(int column) {
    return String.class;
  }
  
  // Only allow editing of value
  public boolean isCellEditable(int row, int column) {
    if (column == 1)
      return true;
    else
      return false;
  }
  
  public int getColumnCount() { return myColumnNames.length; }
  
  public int getRowCount() { 
    if (myPolicy!=null) {
      return myPolicy.getParameters().size();
    } else {
      return 0;
    }
  }
  
  public Object getValueAt(int row, int column) {
    if (myPolicy!=null) {
      UIPolicyParameterInfo parameter = 
        (UIPolicyParameterInfo)myPolicy.getParameters().get(row);
      if (column == 0) {
        return parameter.getName();
      } else if (column == 1) {
        return parameter.getValue();
      } else {
        ArrayIndexOutOfBoundsException e = 
          new ArrayIndexOutOfBoundsException("PolicyTableModel.getValueAt " +
                                             "- invalid column " +
                                             column);
        throw(e);
      }
    } else {
      return "";
    }
  }
  
  public void setValueAt(Object value, int row, int column) {
    // Only allowed to modify the value column
    if (!isCellEditable(row, column)) {
      ArrayIndexOutOfBoundsException e = 
        new ArrayIndexOutOfBoundsException("PolicyTableModel.setValueAt " +
                                           "- invalid column " +
                                           column);
      throw(e); 
    }
    
    if (myClient!=null) {
      UIPolicyParameterInfo pp;
      pp = (UIPolicyParameterInfo)myPolicy.getParameters().get(row);
      
      pp.setValue(value);
      fireTableChanged(new TableModelEvent(this, row, row, column));
    }
  }
}









