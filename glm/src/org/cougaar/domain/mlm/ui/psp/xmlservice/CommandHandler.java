/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.xmlservice;

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
