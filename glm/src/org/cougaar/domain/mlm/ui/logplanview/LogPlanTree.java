package org.cougaar.domain.mlm.ui.logplanview;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.*;
import javax.swing.tree.*;

import java.awt.event.*;
import java.net.URL;

/**
 * Java JTree object used to display contents of a Cougaar Agent's BlackBoard.
 */
public class LogPlanTree extends JTree {
  private LogPlanModel model = null;

  public LogPlanTree( LogPlanModel model) {
    this.model = model;
    setModel( model);
    try  {
      jbInit();
    } catch (Exception ex) {
      System.err.println( "Caught: " + ex);
      ex.printStackTrace();
    }

    putClientProperty( "JTree.lineStyle", "Angled");

    ComponentUI ui = this.getUI();
    if ( ui instanceof BasicTreeUI)
    {
      ((BasicTreeUI)ui).setCollapsedIcon( null);
      ((BasicTreeUI)ui).setExpandedIcon( null);
    }
    this.expandRow(0);
  }

  private void jbInit() throws Exception {
  }
}


