/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
 
package org.cougaar.mlm.ui.psp.xmlservice;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Vector;

import org.cougaar.lib.planserver.PlanServiceContext;

/* This class processes commands sent to a PSP.  
   The RequestParser discovers the method names from introspection
   and invokes the method matching the command.
 */

public class CommandHandler {

  /*
    Called if the client requested a list of cluster names and URLs.
    Gets the list from the plan server and returns it to the client.
  */

  public static void LIST_CLUSTERS(PlanServiceContext psc, PrintStream out) throws Exception {
    Vector urls = new Vector();
    Vector names = new Vector();
    psc.getAllURLsAndNames(urls, names);
    ObjectOutputStream oos = new ObjectOutputStream(out);
    for (int i = 0; i < names.size(); i++) {
      System.out.println("Cluster: " + names.elementAt(i) +
			 "URL: " + urls.elementAt(i));
      oos.writeObject(names.elementAt(i));
      oos.writeObject(urls.elementAt(i));
      oos.flush();
    }
  }

}
