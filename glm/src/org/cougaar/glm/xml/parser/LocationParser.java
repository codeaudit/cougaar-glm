// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/xml/parser/LocationParser.java,v 1.7 2004-03-18 20:50:21 mthome Exp $
/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.glm.xml.parser;

import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

