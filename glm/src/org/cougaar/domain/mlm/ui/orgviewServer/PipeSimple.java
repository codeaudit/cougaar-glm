/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.orgviewServer;

import java.io.*;
import java.util.*;

import org.cougaar.domain.mlm.ui.orgviewServer.NmsPipePlugin;

/** This is a simple example of a NetMapServer pipe plugin. */

public class PipeSimple extends Thread implements NmsPipePlugin {
InputStream is; OutputStream os;
Object arg=null;

  public void execute(InputStream is, OutputStream os, Object arg) {
    this.os=os; this.is=is; this.arg=arg;
    start();
  }

  public void control(String command) {
  }

  public void run() {
    PrintWriter out = new PrintWriter(os, true);
    for (int i=0; i<9; i++)  {
      out.println("object "+arg+" "+(1+i%3));
      try { Thread.sleep(5000); }
      catch (Exception e) {}
    }
    out.close();
  }

}
