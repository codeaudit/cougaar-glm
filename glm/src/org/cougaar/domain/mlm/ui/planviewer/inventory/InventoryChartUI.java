/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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

