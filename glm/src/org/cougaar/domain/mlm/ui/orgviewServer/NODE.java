/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.orgviewServer;

import java.util.*;

/**
 * This is a structure definition for a Node
 */

public class NODE {
  String name;
  String label;
  String type;
  String attr;
  Vector relate;
  Vector nobjlist;

  public NODE() {
    nobjlist = new Vector();
  }

}
