/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.xml.parser;

import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;

import org.cougaar.domain.planning.ldm.plan.NewRoleSchedule;
import org.cougaar.domain.planning.ldm.plan.Schedule;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Date;

/**
 * Creates asset from AggregateAsset xml node.
 *
 * Called from UTILLdmXMLPlugIn.
 */
public class AggregateAssetParser{
  private static final boolean testing = false;

  /**
   * This function is for use with the UTILLdmXMLPlugIn.
   *
   * Insensitive to the case of the <aggregateasset> tag.  (So <AggregateAsset> is OK too.)
   *
   * @param ldmFactory - the factory to ask to create instances
   * @param node       - the AggregateAsset document node itself
   * @return an aggregate asset that corresponds to the node
   * @see org.cougaar.lib.plugin.UTILLdmXMLPlugIn#getAssets
   */
  public static AggregateAsset getAggregate(LDMServesPlugIn ldm, Node node){
    AggregateAsset newAsset = null;

    if (node.getNodeName().toLowerCase().equals("aggregateasset")){
      NamedNodeMap map = node.getAttributes ();
      mergeAttrsAndNodes (map, node.getChildNodes ());

      Node prototypeNode = map.getNamedItem("prototype");
      Node quantityNode  = map.getNamedItem("quantity");

      String prototype;
      int quantity;
      try {
	quantity = (new Integer(quantityNode.getNodeValue())).intValue();
      } catch (NullPointerException npe) {
	System.out.println (classname + 
			    ".getAggregate - XML syntax error : expecting *quantity* attribute on tag <" + 
			    node.getNodeName() + "> or child node.");
	return null;
      }

      try {
	prototype = prototypeNode.getNodeValue ();
      } catch (NullPointerException npe) {
	System.out.println (classname + 
			    ".getAggregate - XML syntax error : expecting *prototype* attribute on tag <" + 
			    node.getNodeName() + "> or child node.");
	return null;
      }
      
      if (testing) 
	System.out.println ("Creating " + quantity + " of "  + prototype);

      Asset proto = ldm.getFactory().getPrototype(prototype);
	  
      if (proto == null)
	System.out.println (classname + 
			    ".getAggregate - XML syntax error : no prototype " + prototype + 
			    " known. Check cluster's ldm.xml file to make sure " + prototype + 
			    "'s prototype file is included."); 

      else {
	newAsset = (AggregateAsset)ldm.getFactory().createAggregate(proto, quantity);
	if (testing)
	  System.out.println (classname + ".getAggregate - Creating agg asset " + newAsset);

	Schedule sched = getSchedule (ldm, node.getChildNodes ());
	if (sched != null)
	  setSchedule (ldm, newAsset, sched);
      }
    }
    else {
      System.out.println (classname + 
			  ".getAggregate - XML syntax error : expecting <AggregateAsset> instead got <" + 
			  node.getNodeName() + ">"); 
    }
    return newAsset;
  }

  protected static Schedule getSchedule (LDMServesPlugIn ldm, NodeList nlist) {
    // Only expect one schedule per instance
      for(int i = 0; i < nlist.getLength(); i++) {
	Node    child       = nlist.item(i);
	String  childname   = child.getNodeName();
	if(child.getNodeType() == Node.ELEMENT_NODE) {
	  if(child.getNodeName().equals("schedule")){
	    return ScheduleParser.getSchedule(ldm, child);
	  }
	}
      }
    return null;
  }
  
  /**
   * Merges attribute node name map with child node list.
   * If same name appears both as attribute and child node, child node is ignored.
   *
   * Alters the input map.
   *
   * @param map  - attribute map (not actually a java map)
   * @param list - list of child nodes (not a java list)
   */
  protected static void mergeAttrsAndNodes (NamedNodeMap map, NodeList list) {
    for (int i = 0; i < list.getLength (); i++) {
      Node node = list.item (i);
      if(node.getNodeType() == Node.ELEMENT_NODE) {
	if (map.getNamedItem (node.getNodeName ()) == null)
	  map.setNamedItem (node);
	else 
	  System.out.println (classname + 
			      ".getAggregate - XML syntax warning : same name <" +
			      node.getNodeName () +
			      "> appears both as an attribute and a child node.  Ignoring child node.");
      }
    }
  }
  
  /**
   * Give the aggregate asset a default schedule.
   *
   * @param ldm         - place to get new instances
   * @param asset       - to modify
   * @param newSchedule - initial availability
   */
  protected static void setSchedule (LDMServesPlugIn ldm, 
				     Asset asset, 
				     Schedule newSchedule) {
    if (testing)
      System.out.println ("setSchedule");
    Schedule copySchedule = ldm.getFactory().newSimpleSchedule(new Date(newSchedule.getStartTime()),
							       new Date(newSchedule.getEndTime()));
    // Set the Schedule
      ((NewRoleSchedule)asset.getRoleSchedule()).setAvailableSchedule(newSchedule);
  }
  
  /** no one instance should ever be created */
  private AggregateAssetParser(){}
  
  private static String classname = AggregateAssetParser.class.getName ();
}
