/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.inventory;

import java.io.*;
import java.util.Vector;

import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JTable;

import com.klg.jclass.chart.ChartDataModel;
import com.klg.jclass.chart.JCChart;

import org.cougaar.domain.mlm.ui.data.UISimpleInventory;

public class InventoryQuery implements Query {
  String assetName;
  UISimpleInventory inventory;
  InventoryChart inventoryChart;

  public InventoryQuery(String assetName) {
    this.assetName = assetName;
  }

  /** Called to create an inventory chart from data retrieved from a file.
   */

  public InventoryQuery(UISimpleInventory inventory) {
    this.inventory = inventory;
    this.assetName = inventory.getAssetName();
  }

  public String getQueryToSend() {
    return assetName;
  }

  public String getPSP_id() {
    return "INVENTORY.PSP";
  }

  public void readReply(InputStream is) {
    inventory = null;
    try {
      ObjectInputStream p = new ObjectInputStream(is);
      inventory = (UISimpleInventory)p.readObject();
    } catch (Exception e) {
      System.out.println("Object read exception: " + e);
      return;
    }
  }

  public void save(File file) {
    try {
      FileOutputStream f = new FileOutputStream(file);
      ObjectOutputStream o = new ObjectOutputStream(f);
      o.writeObject(inventory);
      o.flush();
      f.close();
    } catch (Exception exc) {
      System.out.println("Cannot open file");
    }
  }

  public JPanel createChart(String title, InventoryExecutionTimeStatusHandler timeHandler) {
    if (inventory != null) {
      inventoryChart = new InventoryChart(title, inventory, timeHandler);
      return inventoryChart;
    }
    return null;
  }


  public JPanel reinitializeAndUpdateChart(String title) {
    if ((inventory != null)  &&
	(inventoryChart != null)) {
      inventoryChart.reinitializeAndUpdate(title, inventory);
      return inventoryChart;
    }
    return null;
  }

  public JCChart getChart() {
    return inventoryChart.getChart();
  }

 public void resetChart() {
     inventoryChart.resetChart();
 }

  public void setToCDays(boolean useCDays) {
     inventoryChart.setDisplayCDays(useCDays);   
  }

  public JTable createTable(String title) {
    if (inventory != null)
      return new JTable(new InventoryTableModel(inventory));
    return null;
  }

}




