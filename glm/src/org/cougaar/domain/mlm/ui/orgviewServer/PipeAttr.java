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
 
package org.cougaar.domain.mlm.ui.orgviewServer;

import java.io.*;
import java.util.*;

import org.cougaar.domain.mlm.ui.orgviewServer.NmsPipePlugin;

/**
 * This is a simple example of a NetMapServer pipe plugin.
 */

public class PipeAttr extends Thread implements NmsPipePlugin {
InputStream is; OutputStream os;
Object arg=null;
boolean go= true;

  public void execute(InputStream is, OutputStream os, Object arg) {
    this.os=os; this.is=is; this.arg=arg;
    start();
    }

  public void control(String command) {
    if (command.equals("pollnow")) {
      this.interrupt();
      }
    else if (command.equals("terminate")) {
      go=false;
      this.interrupt();
      }

    }

  public void run() {
    System.out.println("PipeAttr plugin:  "+arg);
    PrintWriter out = new PrintWriter(os, true);
    for (int i=0; i<9; i++)  {
      if (!go)  break;
      out.println("object "+arg+" "+(1+i%3));
      try { Thread.sleep(5000); }
      catch (Exception e) {}
      }
    out.close();
    }

// The main method is not used or needed when running as a plugin;
// it allows the plugin to run by itself for testing.
  public static void main(String[] args) {
    PipeAttr s = new PipeAttr();
    s.arg = args[0];
    s.is = System.in; s.os = System.out;
    s.start();
    }

  }
