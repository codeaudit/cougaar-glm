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
  
          URL url = new URL((String)arg + "/alpine/demo/ALPMAP.PSP?CLUSTERS");
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
