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
 
package org.cougaar.mlm.ui.psp.society;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.cougaar.core.domain.LDMServesPlugin;
import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.ItineraryElement;
import org.cougaar.planning.ldm.plan.Location;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.core.util.XMLObjectFactory;

import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.mlm.ui.data.UISimpleInventory;
import org.cougaar.mlm.ui.planviewer.ConnectionHelper;

import org.w3c.dom.Element;

public class FetchObject
{
    public Object fetchObject(String urlstring, boolean useXML)
        throws Exception {
        return fetchObject(urlstring,  null, useXML);
    }

    public Object fetchObject(String urlstring, String postfield, boolean useXML)
        throws Exception {

       ConnectionHelper connection =new ConnectionHelper();
       connection.setConnection(urlstring);
       if( postfield != null) connection.sendData(postfield);
       InputStream in = connection.getInputStream();

      //URL url = new URL(urlstring);
      //URLConnection connection = url.openConnection();
      //InputStream in = connection.getInputStream();


      Object obj;
      if (useXML) {
        Element root = XMLObjectFactory.readXMLRoot(in);
        if (root == null)
	  throw new Exception("XML parse returned null!");
        obj = XMLObjectFactory.parseObject(root);
      } else {
        ObjectInputStream objIn = new ObjectInputStream(in);
        obj = objIn.readObject();
        objIn.close();
      }
      // System.out.println("FetchObject ==> returning obj");
      return obj;
    }

}
