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
import java.net.*;
import java.util.*;

import org.cougaar.domain.mlm.ui.orgviewServer.NmsWebPlugin;

public class WebClusters extends Thread implements NmsWebPlugin {
Object arg;
Vector clusters=new Vector();
boolean go= true;

  public void exStart(Object arg) {
    this.arg=arg;
    start();
    }

  public void exCommand(PrintWriter out, String command) {
        System.out.println("WebClusters command: "+command);
        out.println("WebClusters: "+command);
    if (command.equals("pollnow")) {
      this.interrupt();
      }
    else if (command.equals("clusters")) {
      synchronized (this) {
        out.print("clusters [");
        Enumeration e = clusters.elements();
        while (e.hasMoreElements()) {
              out.print(e.nextElement()+" ");
          }
        out.println("]");
        }
      }
    else if (command.equals("terminate")) {
      go=false;
      this.interrupt();
      }

    }

  public void run() {
  String line;
  //    load org.cougaar.domain.mlm.ui.orgviewServer.WebClusters clusters "http://haines:5555"

    System.out.println("WebClusters started for  "+arg);
    while (go) {
      synchronized (this) {
        clusters=new Vector();
        try {
  
          URL url = new URL((String)arg + "/alpine/demo/MAP.PSP?CLUSTERS");
          URLConnection uc = url.openConnection();
          BufferedReader in = new BufferedReader(
            new InputStreamReader(uc.getInputStream()));
    
          while ((line = in.readLine()) != null) {
            clusters.addElement(line);
            }
  
          }
        catch (IOException e) {
          System.err.println("WebClusters Error:  " + e);
          }
        }
          System.out.println("WebClusters : "+clusters);
      try { Thread.sleep(120000);       }
      catch (Exception e) {System.err.println("WebClusters Sleep:  " + e);}
      }
    }


  }
