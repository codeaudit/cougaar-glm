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

import org.cougaar.domain.mlm.ui.orgviewServer.NmsNodePlugin;

public class NodeAttr implements NmsNodePlugin {

  public void run(Hashtable Nodehash, Object arg) {
    System.out.println("NodeAttr plugin:  "+arg);
    Vector v = (Vector)arg;
    String name = (String)v.firstElement();
    Object Node[] = (Object[])Nodehash.get(name);
    if (Node == null) {
      System.out.println("Node "+name+" not found");
      return;
    }
    Node[3] = (String)v.elementAt(1);
    System.out.println("Node "+name+" set to "+Node[3]);
  }

}
