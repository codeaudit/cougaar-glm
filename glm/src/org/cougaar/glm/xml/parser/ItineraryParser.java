// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/xml/parser/ItineraryParser.java,v 1.2 2002-02-12 17:48:07 jwinston Exp $
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

import java.util.Date;
import java.util.Vector;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.core.domain.LDMServesPlugin;
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
  public static Schedule getItinerary(LDMServesPlugin ldm, Node node){
    NodeList  nlist     = node.getChildNodes();      
    int       nlength   = nlist.getLength();

    Vector elements = new Vector(10);
    for(int i = 0; i < nlength; i++){
      Node    child     = nlist.item(i);
      String  childname = child.getNodeName();
      
      if(childname.equals("ItineraryElement")){
	elements.addElement(ItineraryParser.getItineraryElement(ldm, child));
      }
    }
    Schedule sched = ldm.getFactory().newSchedule(elements.elements());
    return sched;
  }
  
  private static ItineraryElement getItineraryElement(LDMServesPlugin ldm, Node node){
    NodeList  nlist     = node.getChildNodes();      
    int       nlength   = nlist.getLength();

    NewItineraryElement element = ldm.getFactory().newItineraryElement();
    for(int i = 0; i < nlength; i++){
      Node    child     = nlist.item(i);
      String  childname = child.getNodeName();
      
      if(childname.equals("StartLocation")){
	element.setStartLocation(LocationParser.getLocation(ldm, child));
      }
      else if(childname.equals("EndLocation")){
	element.setEndLocation(LocationParser.getLocation(ldm, child));
      }
      else if(childname.equals("StartDate")){
	element.setStartDate(DateParser.getDate(child));
      }
      else if(childname.equals("EndDate")){
	element.setEndDate(DateParser.getDate(child));
      }
      else if(childname.equals("Role")){
	element.setRole(VerbParser.getVerb(child));
      }
    }
    return element;
  }
}
