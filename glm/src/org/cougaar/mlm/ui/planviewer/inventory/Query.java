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
 
package org.cougaar.mlm.ui.planviewer.inventory;

import java.io.File;
import java.io.InputStream;
import java.util.Vector;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.klg.jclass.chart.ChartDataModel;
import com.klg.jclass.chart.JCChart;

/**
 * Define an interface that must be implemented to add a query
 * to the user interface.
 */

public interface Query {

  /* Get the query to send to the cluster.
   */

  String getQueryToSend();

  /* Read the reply sent from the clusters.
   */

  void readReply(InputStream is);

  /* Save the reply sent from the clusters.
   */

  void save(File file);

  /* Create chart.  
     */

  JPanel createChart(String title);

  /* Create table.
   */

  JPanel reinitializeAndUpdateChart(String title);
    /* Reinit Chart
     */

  JTable createTable(String title);

  /** Get chart created.
   */

  JCChart getChart();

  void resetChart();

  void setToCDays(boolean useCDays);

  /* Get the identifier of the PlanServiceProvider (PSP)
     that this client wants to communicate with.
   */

  String getPSP_id();

}

