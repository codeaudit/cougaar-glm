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

import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.NewRoleSchedule;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.util.log.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
   * @param ldm the factory to ask to create instances
   * @param node the AggregateAsset document node itself
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
//    Schedule copySchedule = ldm.getFactory().newSimpleSchedule(new Date(newSchedule.getStartTime()),
//							       new Date(newSchedule.getEndTime()));
    // Set the Schedule
    ((NewRoleSchedule)asset.getRoleSchedule()).setAvailableSchedule(newSchedule);
  }
  
  private static String classname = AggregateAssetParser.class.getName ();
  protected Logger logger;
  protected ScheduleParser scheduleParser;
}
