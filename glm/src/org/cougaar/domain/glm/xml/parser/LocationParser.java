// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/xml/parser/Attic/LocationParser.java,v 1.6 2001-03-30 16:46:01 mthome Exp $
/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.xml.parser;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.glm.ldm.GLMFactory;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.ldm.plan.NewGeolocLocation;

/**
 * Copyright (c) 1999 BBN Technologies 
 */
public class LocationParser{

  public static GeolocLocation getLocation(LDMServesPlugIn ldm, Node node){
    GLMFactory af = (GLMFactory)ldm.getFactory("glm");

    NodeList  nlist      = node.getChildNodes();      
    int       nlength    = nlist.getLength();
    double    latitude   = Double.NaN;
    double    longitude  = Double.NaN;
    String    geoloc     = null;
    String    name       = null;
    String    type       = null;
    String    statecode  = null;
    String    statename  = null;

    for(int i = 0; i < nlength; i++){
      Node    child      = nlist.item(i);
      String  childname  = child.getNodeName();
      
      if(child.getNodeType() == Node.ELEMENT_NODE){
	
	if(childname.equals("geoloc")){
	  Node data = child.getFirstChild();
	  geoloc = data.getNodeValue();
//  	  System.out.println("LocationParser: geoloc = " + geoloc);
	}
	else if(childname.equals("latitude")){
	  Node data = child.getFirstChild();
	  Double d = new Double(data.getNodeValue());
	  latitude = d.doubleValue();
//  	  System.out.println("LocationParser: latitude = " + latitude);
	}
	else if(childname.equals("longitude")){
	  Node data = child.getFirstChild();
	  Double d = new Double(data.getNodeValue());
	  longitude = d.doubleValue();
//  	  System.out.println("LocationParser: longitude = " + longitude);
	}
	else if(childname.equals("name")){
	  Node data = child.getFirstChild();
	  name = data.getNodeValue();
//  	  System.out.println("LocationParser: name = " + name);
	}
	else if(childname.equals("type")){
	  Node data = child.getFirstChild();
	  type = data.getNodeValue();
//  	  System.out.println("LocationParser: type = " + type);
	}
	else if(childname.equals("statecode")){
	  Node data = child.getFirstChild();
	  statecode = data.getNodeValue();
//  	  System.out.println("LocationParser: name = " + statecode);
	}
	else if(childname.equals("statename")){
	  Node data = child.getFirstChild();
	  statename = data.getNodeValue();
//  	  System.out.println("LocationParser: name = " + statename);
	}
      }
    }
	
    
    NewGeolocLocation loc = af.newGeolocLocation();
    loc.setGeolocCode(geoloc);
    loc.setLatitude(Latitude.newLatitude(latitude));
    loc.setLongitude(Longitude.newLongitude(longitude));
    loc.setName(name);
    loc.setInstallationTypeCode(type);
    loc.setCountryStateCode(statecode);
    loc.setCountryStateName(statename);
    return loc;
  }
}

