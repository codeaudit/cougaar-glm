// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/xml/parser/ItineraryParser.java,v 1.5 2003-01-23 19:53:33 mthome Exp $
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

package org.cougaar.glm.xml.parser;

import java.util.Date;
import java.util.Vector;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ClusterObjectFactory;
import org.cougaar.planning.ldm.plan.NewItineraryElement;
import org.cougaar.planning.ldm.plan.ItineraryElement;

import org.cougaar.lib.xml.parser.DateParser;
import org.cougaar.lib.xml.parser.VerbParser;

/**
 * Copyright (c) 1999 BBN Technologies 
 */
public class ItineraryParser{
  public ItineraryParser () {
    dateParser = new DateParser();
    verbParser = new VerbParser();
    locationParser = new LocationParser();
  }

  public Schedule getItinerary(LDMServesPlugin ldm, Node node){
    NodeList  nlist     = node.getChildNodes();      
    int       nlength   = nlist.getLength();

    Vector elements = new Vector(10);
    for(int i = 0; i < nlength; i++){
      Node    child     = nlist.item(i);
      String  childname = child.getNodeName();
      
      if(childname.equals("ItineraryElement")){
	elements.addElement(getItineraryElement(ldm, child));
      }
    }
    Schedule sched = ldm.getFactory().newSchedule(elements.elements());
    return sched;
  }
  
  private ItineraryElement getItineraryElement(LDMServesPlugin ldm, Node node){
    NodeList  nlist     = node.getChildNodes();      
    int       nlength   = nlist.getLength();

    NewItineraryElement element = ldm.getFactory().newItineraryElement();
    for(int i = 0; i < nlength; i++){
      Node    child     = nlist.item(i);
      String  childname = child.getNodeName();
      
      if(childname.equals("StartLocation")){
	element.setStartLocation(locationParser.getLocation(ldm, child));
      }
      else if(childname.equals("EndLocation")){
	element.setEndLocation(locationParser.getLocation(ldm, child));
      }
      else if(childname.equals("StartDate")){
	element.setStartDate(dateParser.getDate(child));
      }
      else if(childname.equals("EndDate")){
	element.setEndDate(dateParser.getDate(child));
      }
      else if(childname.equals("Role")){
	element.setRole(verbParser.getVerb(child));
      }
    }
    return element;
  }

  DateParser dateParser;
  VerbParser verbParser;
  LocationParser locationParser;
}
