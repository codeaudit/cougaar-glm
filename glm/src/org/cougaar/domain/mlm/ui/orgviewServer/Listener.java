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
import java.net.*;
import java.util.*;

public class Listener extends Thread {
  NetMapServer nms;
  ServerSocket mbinSocket;   

  public Listener(NetMapServer nms, int port)  throws IOException {
    super("Listener:" + port);      
    this.nms=nms; 
    mbinSocket = new ServerSocket(port);
    }

  public void run() {
    while(true) {      
      try {
        Socket s = mbinSocket.accept();
          mbinRdr r = new mbinRdr(nms, s.getInputStream(), 
           s.getOutputStream(), "Socket",
                   s.getInetAddress().toString());
          r.start();
        } 
      catch (Exception e) {System.out.println(e);} 
      }
    }

  }  //  end of  class Listener
