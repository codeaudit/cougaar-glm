/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer;

import javax.swing.JApplet;

/**
 * Displays forms that allow user to select cluster, plan objects,
 * and specific plan objects.  POSTs parameters to Plan Service Provider
 * on appropriate cluster, and displays results.
 */

public class XMLApplet extends JApplet {

  public void init() {
    System.out.println("Code Base URL is:" + getCodeBase());
    new XMLDisplay(getContentPane(), getCodeBase().toString());
  }
}
