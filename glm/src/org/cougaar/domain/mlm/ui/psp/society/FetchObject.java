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

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.ItineraryElement;
import org.cougaar.domain.planning.ldm.plan.Location;
import org.cougaar.domain.planning.ldm.plan.ScheduleElement;
import org.cougaar.core.util.XMLObjectFactory;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.mlm.ui.data.UISimpleInventory;
import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;

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
