/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.supply;

import javax.swing.JApplet;

public class SupplyStoplightApplet extends JApplet {

  /** Query the cluster from which the applet was loaded.
    Calls supply controller with code base, container and cluster to contact.
   */

  public void init() {
    new SupplyController(getCodeBase(), getContentPane(), "Supply");
  }

}


