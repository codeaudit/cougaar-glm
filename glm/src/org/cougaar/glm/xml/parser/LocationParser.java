// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/xml/parser/LocationParser.java,v 1.4 2002-11-19 17:21:46 twright Exp $
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

package org.cougaar.glm.xml.parser;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;

/**
 * Copyright (c) 1999 BBN Technologies 
 */
public class LocationParser{

  public GeolocLocation getLocation(LDMServesPlugin ldm, Node node){
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

