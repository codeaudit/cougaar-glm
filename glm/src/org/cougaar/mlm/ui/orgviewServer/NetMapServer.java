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
import java.net.*;
import java.util.*;

import org.cougaar.mlm.ui.orgviewServer.NmsNodePlugin;
import org.cougaar.mlm.ui.orgviewServer.NmsPipePlugin;
import org.cougaar.mlm.ui.orgviewServer.NmsPlugin;

/**
 * .<pre>
 * Alp NetMap Server   (Java)  
 *
 * Initial version   ech 9/22/99  from server test program and PSP_Map
 * 11/19/99:  make cluster icon bigger
 * </pre>
 */

public class NetMapServer {
static NetMapServer nms;
static int mbinPort=5550;            
Hashtable Nodehash;
Hashtable NodeLabelhash;
Vector Lines = new Vector();
Vector Clusters = new Vector();


  public static void main(String[] args) {
    nms = new NetMapServer();
    nms.run();
    }

  public void run() {
  String line;
  ctTokenizer t;

    try {

      // Initialize database
      Nodehash = new Hashtable();
      NodeLabelhash = new Hashtable();
      BufferedReader in = new BufferedReader(
              new FileReader(new File("alpmap", "i.nmdb")) );
      String deftype = "deftype";
      String defdattr = "3";

      while ((line=in.readLine()) != null) {  // read database file
        t= new ctTokenizer(line);
            
        if (t.v.size() > 1 && t.v.firstElement() instanceof String)  {
                String a = (String)t.v.firstElement();
                if ( a.equals("line") )  Lines.addElement(t.v);
                else if ( a.equals("default") )  {
                  deftype = get(t.v, "type", deftype);
                  defdattr = get(t.v, "dattr", defdattr);
                  }
                else {                  // Assume it is a Node
                 NODE Node = new NODE();
                 Node.name = a;
                 Node.label = (String)t.v.elementAt(1);
                 Node.type = get(t.v, "type", deftype);
                 Node.attr = get(t.v, "dattr", defdattr);
                 Nodehash.put(Node.name, Node);
                 NodeLabelhash.put(Node.label, Node);
                 }
                }
        }

//      Listener mbinLis =  new Listener(mbinPort);
//      mbinLis.start();

      mbinRdr r = new mbinRdr(nms, 
        new FileInputStream(new File("alpmap", "nms.init")),
        System.out, "Readfile", "nms.init" );
      r.start();

      } 
    catch (Exception e) {System.out.println(e);} 
    }






    public String get(Vector v, String prop, String def) {
      Enumeration e = v.elements();
      while (e.hasMoreElements()) {
        Object a = e.nextElement();
        if (a instanceof String && a.equals(prop))  {
          if (e.hasMoreElements())  a = e.nextElement(); 
          if (e.hasMoreElements())  a = e.nextElement(); 
          return (String)a;
          }
        }
      return def;
      }


   public String getTypeShape(String a) {
      if (a.equals("ALPCLUSTER"))  return  "  11 60 24  ";
      else if (a.equals("VIEW"))  return  "  11 60 30  ";
      else return  "  12 30 20 ";
      }


   public String getTypeMenu(String a) {
      if (a.equals("ALPCLUSTER"))  return  " 1 ";
      else if (a.equals("VIEW"))  return  " 4 ";
      else return  " 0 ";
      }



  }  //  end of  class NetMapServer
