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

import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.PlanningFactory;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AggregateAsset;

import org.cougaar.planning.ldm.plan.NewRoleSchedule;
import org.cougaar.planning.ldm.plan.Schedule;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Date;

import org.cougaar.util.log.*;

/**
 * Creates asset from AggregateAsset xml node. <p>
 *
 * Called from UTILLdmXMLPlugin.
 */
public class AggregateAssetParser{
  private static final boolean testing = false;

  public AggregateAssetParser(Logger log) { 
    logger = log; 
    scheduleParser = new ScheduleParser();
  }

  /**
   * This function is for use with the UTILLdmXMLPlugin. <p>
   *
   * Insensitive to the case of the <aggregateasset> tag.  (So <AggregateAsset> is OK too.)
   *
   * @param ldmFactory - the factory to ask to create instances
   * @param node       - the AggregateAsset document node itself
   * @return an aggregate asset that corresponds to the node
   */
  public AggregateAsset getAggregate(LDMServesPlugin ldm, Node node){
    AggregateAsset newAsset = null;

    if (node.getNodeName().toLowerCase().equals("aggregateasset")){
      NamedNodeMap map = node.getAttributes ();

      Node prototypeNode = map.getNamedItem("prototype");
      if (prototypeNode == null) {
	prototypeNode = getNodeNamed (node.getChildNodes(), "prototype");
	prototypeNode = prototypeNode.getChildNodes().item(0);
      }
	  
      Node quantityNode  = map.getNamedItem("quantity");
      if (quantityNode == null) {
	quantityNode = getNodeNamed (node.getChildNodes(), "quantity");
	quantityNode = quantityNode.getChildNodes().item(0);
      }

      String prototype;
      int quantity;
      try {
	String quantityNodeValue = quantityNode.getNodeValue();
	quantity = (new Integer(quantityNodeValue)).intValue();
      } catch (NullPointerException npe) {
	logger.error (classname + 
		      ".getAggregate - XML syntax error : expecting *quantity* attribute on tag <" + 
		      node.getNodeName() + "> or child node.");
	return null;
      }

      try {
	prototype = prototypeNode.getNodeValue ();
      } catch (NullPointerException npe) {
	logger.error (classname + 
		      ".getAggregate - XML syntax error : expecting *prototype* attribute on tag <" + 
		      node.getNodeName() + "> or child node.");
	return null;
      }
      
      if (logger.isDebugEnabled()) 
	logger.debug ("Creating " + quantity + " of "  + prototype);

      Asset proto = ldm.getFactory().getPrototype(prototype);
	  
      if (proto == null)
	logger.error (classname + 
		      ".getAggregate - XML syntax error : no prototype " + prototype + 
		      " known. Check cluster's ldm.xml file to make sure " + prototype + 
		      "'s prototype file is included."); 

      else {
	newAsset = (AggregateAsset)ldm.getFactory().createAggregate(proto, quantity);
	if (logger.isDebugEnabled())
	  logger.debug (classname + ".getAggregate - Creating agg asset " + newAsset);

	Schedule sched = getSchedule (ldm, node.getChildNodes ());
	if (sched != null)
	  setSchedule (ldm, newAsset, sched);
      }
    }
    else {
      logger.error (classname + 
		    ".getAggregate - XML syntax error : expecting <AggregateAsset> instead got <" + 
		    node.getNodeName() + ">"); 
    }
    return newAsset;
  }

  protected Node getNodeNamed (NodeList nlist, String name) {
    for(int i = 0; i < nlist.getLength(); i++) {
      Node    child       = nlist.item(i);
      String  childname   = child.getNodeName();
      if (childname.equals (name)) {
	return child;
      }
    }
    return null;
  }
  
  protected Schedule getSchedule (LDMServesPlugin ldm, NodeList nlist) {
    // Only expect one schedule per instance
    for(int i = 0; i < nlist.getLength(); i++) {
      Node    child       = nlist.item(i);
      String  childname   = child.getNodeName();
      if(child.getNodeType() == Node.ELEMENT_NODE) {
	if(child.getNodeName().equals("schedule")){
	  return scheduleParser.getSchedule(ldm, child);
	}
      }
    }
    return null;
  }
  
  /**
   * Give the aggregate asset a default schedule.
   *
   * @param ldm         - place to get new instances
   * @param asset       - to modify
   * @param newSchedule - initial availability
   */
  protected void setSchedule (LDMServesPlugin ldm, 
			      Asset asset, 
			      Schedule newSchedule) {
    if (logger.isDebugEnabled())
      logger.debug ("setSchedule");
    Schedule copySchedule = ldm.getFactory().newSimpleSchedule(new Date(newSchedule.getStartTime()),
							       new Date(newSchedule.getEndTime()));
    // Set the Schedule
    ((NewRoleSchedule)asset.getRoleSchedule()).setAvailableSchedule(newSchedule);
  }
  
  private static String classname = AggregateAssetParser.class.getName ();
  protected Logger logger;
  protected ScheduleParser scheduleParser;
}
