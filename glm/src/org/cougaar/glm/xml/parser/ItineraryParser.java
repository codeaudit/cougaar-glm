// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/xml/parser/ItineraryParser.java,v 1.7 2004-03-18 20:50:21 mthome Exp $
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

import java.util.Vector;

import org.cougaar.lib.xml.parser.DateParser;
import org.cougaar.lib.xml.parser.VerbParser;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.plan.ItineraryElement;
import org.cougaar.planning.ldm.plan.NewItineraryElement;
import org.cougaar.planning.ldm.plan.Schedule;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
