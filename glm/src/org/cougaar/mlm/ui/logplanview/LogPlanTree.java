/*
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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
package org.cougaar.mlm.ui.logplanview;

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


