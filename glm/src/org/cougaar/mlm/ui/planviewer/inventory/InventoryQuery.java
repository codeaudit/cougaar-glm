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
 
package org.cougaar.mlm.ui.planviewer.inventory;

import java.io.*;
import java.util.Vector;

import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JTable;

import com.klg.jclass.chart.ChartDataModel;
import com.klg.jclass.chart.JCChart;

import org.cougaar.mlm.ui.data.UISimpleInventory;

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
    return "inventory";
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

  public JPanel createChart(String title) {

    if (inventory != null) {
      inventoryChart = new InventoryChart(title, inventory);
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




