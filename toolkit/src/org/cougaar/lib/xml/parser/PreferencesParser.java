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

package org.cougaar.lib.xml.parser;

import java.util.Date;

import org.cougaar.lib.util.UTILPreference;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <pre>
 * Parses preferences of tasks in test input files.
 *
 * Currently handles start time, end time, and cost preferences.
 * Start time is expected to have a ready at date.
 * End   time is expected to have an early, best, and latest date.
 * </pre>
 */
public class PreferencesParser{
  
  public PreferencesParser (Logger logger) {
    prefHelper = new UTILPreference(logger);
    dateParser = new DateParser();
  }

  /**
   * Get the cost preference
   */
  public Preference getCost(PlanningFactory ldmf, Node node){
    NodeList  nlist    = node.getChildNodes();      
    int       nlength  = nlist.getLength();
    double    cost     = 0.0;

    for(int i = 0; i < nlength; i++){
      Node    child       = nlist.item(i);
      String  childname   = child.getNodeName();
      Double  d = new Double(child.getNodeValue());
      cost = d.doubleValue();
    }
    return prefHelper.makeCostPreference(ldmf, cost);
  }

  /**
   * Get the quantity preference
   */
  public Preference getQuantity(PlanningFactory ldmf, Node node){
    NodeList  nlist    = node.getChildNodes();      
    int       nlength  = nlist.getLength();
    long      quantity = 0l;

    for(int i = 0; i < nlength; i++){
      Node    child       = nlist.item(i);
      String  childname   = child.getNodeName();
      Long l = new Long(child.getNodeValue());
      quantity = l.longValue();
    }
    return prefHelper.makeQuantityPreference(ldmf, quantity);
  }


  /**
   * Get the start date preference
   */
  public Preference getStartDate(PlanningFactory ldmf, Node node){
    NodeList  nlist     = node.getChildNodes();      
    int       nlength   = nlist.getLength();
    Date      readyatdate = null;
    
    for(int i = 0; i < nlength; i++){
      Node    child     = nlist.item(i);
      String  childname = child.getNodeName();
      
      if(child.getNodeType() == Node.ELEMENT_NODE){
	if(childname.equals("readyatdate")){
	  readyatdate = dateParser.getDate(child);
	}
      }
    }
    return prefHelper.makeStartDatePreference(ldmf, readyatdate);
  }

  /**
   * Get the end date preference
   */
  public Preference getEndDate(PlanningFactory ldmf, Node node){
    NodeList  nlist     = node.getChildNodes();      
    int       nlength   = nlist.getLength();
    Date      earldate  = null;
    Date      bestdate  = null;
    Date      latedate  = null;

    for(int i = 0; i < nlength; i++){
      Node    child     = nlist.item(i);
      String  childname = child.getNodeName();
      
      if(child.getNodeType() == Node.ELEMENT_NODE){
	if(childname.equals("earlydate")){
	  earldate = dateParser.getDate(child);
	}
	else if(childname.equals("bestdate")){
	  bestdate = dateParser.getDate(child);
	}
	else if(childname.equals("latedate")){
	  latedate = dateParser.getDate(child);
	}
      }
    }

    if (earldate == null || latedate == null)
      return prefHelper.makeEndDateBelowPreference(ldmf, bestdate);
    else
      return prefHelper.makeEndDatePreference(ldmf, earldate, bestdate, latedate);
  }

  protected UTILPreference prefHelper;
  protected DateParser dateParser;
}
