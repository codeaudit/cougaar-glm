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
 
package org.cougaar.domain.mlm.ui.planviewer.inventory;

import javax.swing.JApplet;
import org.cougaar.util.OptionPane;
import org.cougaar.util.ThemeFactory;
import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;

public class InventoryChartUI extends JApplet {

  public void init() {
    ThemeFactory.establishMetalTheme();
    new InventorySelector(getCodeBase(), getContentPane());
  }

  /** If called with an argument, then it is the data filename.
   */
  public static void main(String[] args) {
    String arg0 = null;
    if (args.length != 0) {
      for (int i = 0; i < args.length; i++) {
        if (args[i].startsWith("-")) {
          if (args[i].equals("-demo"))
            ThemeFactory.establishMetalTheme();
        } else if (arg0 == null) {
          arg0 = args[i];
        }
      }
    }
    if (arg0 != null) {
      new InventorySelector(arg0);
    } else {
	// display dialog box for location of LogPlanServer
      String clusterHost = "localhost";
      String clusterPort = "5555";
      String msg = "Enter cluster Log Plan Server location as host:port";
      String s = ConnectionHelper.getClusterHostPort(null, msg);
      if (s == null)
        System.exit(0); // if we don't know where the clusters are located, quit
      s = s.trim();
      if (s.length() != 0) {
        int i = s.indexOf(":");
        if (i != -1) {
          clusterHost = s.substring(0, i);
          clusterPort = s.substring(i+1);
        }
      }
      new InventorySelector(clusterHost, clusterPort);
    }
  }

}

