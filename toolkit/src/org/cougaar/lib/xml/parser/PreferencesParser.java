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
