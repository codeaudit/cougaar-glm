/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.society;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.domain.mlm.ui.psp.transportation.data.*;

/**
 * Test_PSP_TransportLinksNodes.
 * <p>
 * This class can be used to test a running PSP_TransportLinksNodes PlugIn.
 * It works just like a browser, except it uses object serialization to 
 * receive the data instead of HTML.
 * <p>
 * Make sure to specify a cluster which will contain a network, e.g.
 * CONUSGround or TheaterGround in TOPS, etc.
 * <p>
 * Sample usage (depending upon configuration):<code>
 *   java -classpath %libpaths%;. org.cougaar.domain.mlm.ui.psp.society.Test_PSP_TransportLinksNodes localhost:5555/$CONUSGround/alpine/demo/QUERYTRANSPORT_NETWORK.PSP?NETWORK_DATA
 *</code>
 */

public class Test_PSP_TransportLinksNodes {
  public static void main(String[] args) {
    String urlString = getURLString(args);
    if (urlString == null)
      return;
    System.out.println("Fetching "+urlString);
    InputStream in;
    try {
      URL url = new URL(urlString);
      URLConnection connection = url.openConnection();
      in = connection.getInputStream();
    } catch (Exception e) {
      System.err.println("Connect failure: "+e);
      return;
    }
    try {
      ObjectInputStream objIn = new ObjectInputStream(in);
      Object obj = objIn.readObject();
      if (obj instanceof UITxNetwork) {
	System.out.println("Got network:");
	System.out.println(((UITxNetwork)obj).toString());
      } else if (obj instanceof Collection) {
	Iterator colIter = ((Collection)obj).iterator();
	if (!colIter.hasNext())
	  System.out.println("Empty collection");
	else {
	  int i = 0;
	  do {
	    Object colObj = colIter.next();
	    if (colObj instanceof UITxNode)
	      System.out.println("Node["+i+"]:\n"+(UITxNode)colObj);
	    else if (colObj instanceof UITxLink)
	      System.out.println("Link["+i+"]:\n"+(UITxLink)colObj);
            else
	      System.out.println("Unknown["+i+"]: "+colObj.getClass());
	    i++;
	  } while (colIter.hasNext());
	}
      } else
	System.out.println("Unknown: "+obj.getClass());
      in.close();
    } catch (Exception e) {
      System.err.println("Read failure: "+e);
      return;
    }
  }

  public static String getURLString(String[] args) {
    if (args.length != 1) {
      System.err.println("Missing URL argument");
      return null;
    }
    String urlString = args[0];
    if (!urlString.startsWith("http://")) {
      if (urlString.indexOf("://") >= 0) {
	System.err.println("HTTP only!");
	return null;
      }
      urlString = "http://"+urlString;
    }
    if (urlString.indexOf(".PSP?") < 0) {
      System.err.println("Expect \".PSP?\" request!");
      return null;
    }
    if (!urlString.endsWith("_DATA")) {
      System.err.println("Expect \"_DATA\" request!");
      return null;
    }
    return urlString;
  }
}

