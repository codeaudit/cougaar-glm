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

import java.io.*;
import java.util.*;

import org.cougaar.domain.mlm.ui.orgviewServer.NmsPipePlugin;

/** This is a simple example of a NetMapServer pipe plugin. **/

public class PipeTest extends Thread implements NmsPipePlugin {
InputStream is; OutputStream os;
Object arg=null;

  public void execute(InputStream is, OutputStream os, Object arg) {
    this.os=os; this.is=is; this.arg=arg;
//    start();
    read();
    }

  public void control(String command) {
    }

  public void read() {
    String line;
    PrintWriter out = new PrintWriter(os, true);
    try { 
      BufferedReader in = new BufferedReader(new InputStreamReader(is));

      while ((line = in.readLine())  != null) {
        out.println("PipeTest got: "+line);
        if ( line.equals("quit") )  break;
        }  //while
      } //try
    catch (Exception e) {System.out.println("mbinRdr:  "+e);} 

    try { 
      is.close(); if (os != null) os.close();
      } //try
    catch (Exception e) {} 
    out.close();
    }


  public static void main(String[] args) {
    PipeTest s = new PipeTest();
    s.arg = args[0];
    s.is = System.in; s.os = System.out;
//    s.start();
    s.read();
    }

  }
