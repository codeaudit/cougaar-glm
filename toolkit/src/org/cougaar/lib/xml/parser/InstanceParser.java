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

import java.util.ArrayList;
import java.util.List;

import org.cougaar.lib.util.UTILAsset;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.NewRoleSchedule;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Creates instances specified in ldm.xml files.
 *
 * Expects a base prototype and optionally a quantity of n instances of the
 * new asset.
 */
public class InstanceParser{

  public InstanceParser (Logger logger) { 
    scheduleParser = new ScheduleParser(); 
    assetHelper    = new UTILAsset(logger);
  }

  public List getInstance(LDMServesPlugin ldm, Node node){

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
	if(child.getNodeType() == Node.ELEMENT_NODE) {
	  if(child.getNodeName().equals("schedule")){
	    newSchedule = scheduleParser.getSchedule(ldm, child);
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
   * @param ldm place to get new instances
   * @param prototype what kind of asset is it
   * @param id unique identifier within kind
   * @param newSchedule initial availability
   * @return the new asset!
   */
  protected Asset makeNewAsset (LDMServesPlugin ldm, 
				String prototype, 
				String id, 
				Schedule newSchedule) {
    // Create the instance
    Asset newAsset = assetHelper.createInstance(ldm,
						prototype, id );

//    Schedule copySchedule = ldm.getFactory().newSimpleSchedule(new Date(newSchedule.getStartTime()),
//							       new Date(newSchedule.getEndTime()));
    // Set the Schedule
    ((NewRoleSchedule)newAsset.getRoleSchedule()).setAvailableSchedule(newSchedule);

    //logger.debug ("Making new asset " + newAsset + " with UID " + 
    //newAsset.getUID());
    return newAsset;
  }

  protected ScheduleParser scheduleParser;
  protected UTILAsset assetHelper;
}

