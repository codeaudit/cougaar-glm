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

package org.cougaar.lib.xml.parser;

import java.util.Date;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.lib.util.UTILPreference;

/**
 * Parses preferences of tasks in test input files.
 *
 * Currently handles start time, end time, and cost preferences.
 * Start time is expected to have a ready at date.
 * End   time is expected to have an early, best, and latest date.
 */
public class PreferencesParser{
  
  /**
   * Get the cost preference
   */
  public static Preference getCost(RootFactory ldmf, Node node){
    NodeList  nlist    = node.getChildNodes();      
    int       nlength  = nlist.getLength();
    double    cost     = 0.0;

    for(int i = 0; i < nlength; i++){
      Node    child       = nlist.item(i);
      String  childname   = child.getNodeName();
      Double  d = new Double(child.getNodeValue());
      cost = d.doubleValue();
    }
    return UTILPreference.makeCostPreference(ldmf, cost);
  }


  /**
   * Get the start date preference
   */
  public static Preference getStartDate(RootFactory ldmf, Node node){
    NodeList  nlist     = node.getChildNodes();      
    int       nlength   = nlist.getLength();
    Date      readyatdate = null;
    
    for(int i = 0; i < nlength; i++){
      Node    child     = nlist.item(i);
      String  childname = child.getNodeName();
      
      if(child.getNodeType() == Node.ELEMENT_NODE){
	if(childname.equals("readyatdate")){
	  readyatdate = DateParser.getDate(child);
	}
      }
    }
    return UTILPreference.makeStartDatePreference(ldmf, readyatdate);
  }

  /**
   * Get the end date preference
   */
  public static Preference getEndDate(RootFactory ldmf, Node node){
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
	  earldate = DateParser.getDate(child);
	}
	else if(childname.equals("bestdate")){
	  bestdate = DateParser.getDate(child);
	}
	else if(childname.equals("latedate")){
	  latedate = DateParser.getDate(child);
	}
      }
    }
    return UTILPreference.makeEndDatePreference(ldmf, earldate, bestdate, latedate);
  }

}
