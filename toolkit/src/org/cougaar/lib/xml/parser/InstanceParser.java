/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.xml.parser;

import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Schedule;

import org.cougaar.domain.planning.ldm.plan.NewRoleSchedule;

import org.cougaar.lib.util.UTILAsset;

import java.util.Date;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Creates instances specified in ldm.xml files.
 *
 * Expects a base prototype and optionally a quantity of n instances of the
 * new asset.
 */
public class InstanceParser{

  public static List getInstance(LDMServesPlugIn ldm, Node node){

    Schedule newSchedule = null; 
    List newAssets = new ArrayList ();

    if(node.getNodeName().equals("instance")){
      NodeList  nlist    = node.getChildNodes();      
      int       nlength  = nlist.getLength();

      String id        = node.getAttributes().getNamedItem("id").getNodeValue();
      String prototype = node.getAttributes().getNamedItem("prototype").getNodeValue();
      
      // Only expect one schedule per instance but what the heck.
      for(int i = 0; i < nlength; i++) {
	Node    child       = nlist.item(i);
	String  childname   = child.getNodeName();
	if(child.getNodeType() == Node.ELEMENT_NODE) {
	  if(child.getNodeName().equals("schedule")){
	    newSchedule = ScheduleParser.getSchedule(ldm, child);
	  }
	}
      }

      Integer intQ = new Integer (1);

      if (node.getAttributes().getNamedItem("quantity") != null) {
	String quantity = node.getAttributes().getNamedItem("quantity").getNodeValue();
	intQ = new Integer(quantity);
      }

      for (int j = 0; j < intQ.intValue(); j++) {
	String newID = id;
	if (intQ.intValue () > 1)
	  newID = newID + "-" + j;
	newAssets.add (makeNewAsset(ldm, prototype, newID, newSchedule));
      }

      return newAssets;
    }
    // Should not get here should throw a runtimeexception
    return null;
  }

  /**
   * Make a new asset of the specified prototype and bumper #.
   * Also gives it a default schedule.
   *
   * @param ldm   - place to get new instances
   * @param prototype    - what kind of asset is it
   * @param id           - unique identifier within kind
   * @param newScheduler - initial availability
   * @return the new asset!
   */
    protected static Asset makeNewAsset (LDMServesPlugIn ldm, 
					 String prototype, 
					 String id, 
					 Schedule newSchedule) {
      // Create the instance
      Asset newAsset = UTILAsset.createInstance(ldm,
					       prototype, id );

      Schedule copySchedule = ldm.getFactory().newSimpleSchedule(new Date(newSchedule.getStartTime()),
								 new Date(newSchedule.getEndTime()));
      // Set the Schedule
      ((NewRoleSchedule)newAsset.getRoleSchedule()).setAvailableSchedule(newSchedule);

      //System.out.println ("Making new asset " + newAsset + " with UID " + 
      //newAsset.getUID());
      return newAsset;
    }
}

