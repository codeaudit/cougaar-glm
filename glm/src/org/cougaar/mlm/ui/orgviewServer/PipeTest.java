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
 
package org.cougaar.mlm.ui.orgviewServer;

import java.io.*;
import java.util.*;

import org.cougaar.mlm.ui.orgviewServer.NmsPipePlugin;

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
