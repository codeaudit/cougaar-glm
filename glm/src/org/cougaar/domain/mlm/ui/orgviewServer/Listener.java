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
