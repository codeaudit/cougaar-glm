// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/xml/parser/Attic/ItineraryParser.java,v 1.1 2000-12-15 20:18:03 mthome Exp $
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

import java.util.Date;
import java.util.Vector;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.ClusterObjectFactory;
import org.cougaar.domain.planning.ldm.plan.NewItineraryElement;
import org.cougaar.domain.planning.ldm.plan.ItineraryElement;

import org.cougaar.lib.xml.parser.DateParser;
import org.cougaar.lib.xml.parser.VerbParser;

/**
 * Copyright (c) 1999 BBN Technologies 
 */
public class ItineraryParser{
  public static Schedule getItinerary(LDMServesPlugIn ldm, Node node){
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
  
  private static ItineraryElement getItineraryElement(LDMServesPlugIn ldm, Node node){
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
