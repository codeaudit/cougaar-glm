// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/gss/plugins/Attic/GLMGSSAllocatorPlugIn.java,v 1.4 2001-08-22 20:27:21 mthome Exp $
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

package org.cougaar.domain.glm.gss.plugins;

import org.cougaar.domain.glm.gss.GLMSpecsHandler;
import org.cougaar.domain.glm.ldm.asset.Organization;

import org.cougaar.lib.gss.GSSpecsHandler;
import org.cougaar.lib.gss.plugins.UTILGSSAllocatorPlugIn;

import org.cougaar.domain.glm.callback.GLMOrganizationCallback;
import org.cougaar.domain.glm.callback.GLMOrganizationListener;

import java.util.Enumeration;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.AuxiliaryQueryType;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.lib.callback.UTILAllocationCallback;
import org.cougaar.lib.callback.UTILFilterCallback;
import org.cougaar.lib.callback.UTILGenericListener;
import org.cougaar.lib.callback.UTILWorkflowCallback;

import org.cougaar.lib.gss.GSTaskGroup;

import org.cougaar.lib.util.UTILAllocate;

import java.util.Iterator;
import java.util.List;

import org.cougaar.lib.filter.UTILAllocatorPlugIn;


public class GLMGSSAllocatorPlugIn extends UTILGSSAllocatorPlugIn implements GLMOrganizationListener {
  public GSSpecsHandler getSpecsHandler() { return new GLMSpecsHandler(); }

  public void makePlanElement (Asset anAsset, GSTaskGroup group) {
    for (Iterator iter = group.getTasks ().iterator (); iter.hasNext (); ) {
      Task t = (Task) iter.next ();

      if (myExtraOutput)
	UTILAllocate.setDebug (true);

      double confidence = UTILAllocate.HIGHEST_CONFIDENCE;
      if (anAsset instanceof Organization) confidence = UTILAllocate.MEDIUM_CONFIDENCE;

      PlanElement allocation = UTILAllocate.makeAllocation(this,
							   ldmf, realityPlan, t, anAsset,
							   group.getCurrentStart(), 
							   group.getCurrentEnd(), 
							   confidence,
							   Role.getRole("Transporter"));
      if (t.getAuxiliaryQueryTypes()[0] != -1) {
    	int[] aqr = t.getAuxiliaryQueryTypes();
	for (int q= 0; q < aqr.length; q++) {
	  int checktype = aqr[q];
	  if (checktype == AuxiliaryQueryType.UNIT_SOURCED ) {
	    String data = anAsset.getItemIdentificationPG().getItemIdentification();
	    UTILAllocate.addQueryResultToAR(allocation, AuxiliaryQueryType.UNIT_SOURCED, data);
	  }
	}
      }

      if (myExtraOutput)
	UTILAllocate.setDebug (false);

      publishAdd(allocation);

      /*
      if (myExtraOutput) {
	if (allocation instanceof FailedAllocation)
	  System.out.println(getName () + " : Making failed allocation for task " +t.getUID());
	else
	  System.out.println(getName () + " : Making allocation for task " +t.getUID());
      }
      if (myExtraExtraOutput)
	System.out.println("\tfrom " + group.getCurrentStart() + 
			   " to " + group.getCurrentEnd());
      */
    }
  }

  /**
   * Place to handle new orgs.
   */

  public void handleNewOrganization     (Enumeration e) {
    if (myExtraOutput) {
      if (e.hasMoreElements ())
        System.out.println (getName ());
      while (e.hasMoreElements()) {
        Organization currentOrg = (Organization)e.nextElement();
      }
    }
  }
  
  public void handleChangedOrganization (Enumeration e) {
    if (myExtraOutput) {
      if (e.hasMoreElements ())
        System.out.println (getName ());
      while (e.hasMoreElements()) {
        Organization currentOrg = (Organization)e.nextElement();
        System.out.println ("\tGot changed org : " + currentOrg);
      }
    }
  }

}
