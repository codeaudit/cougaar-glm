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
 
package org.cougaar.domain.mlm.ui.psp.transportation;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Relationship;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.lib.planserver.PSPState;
import org.cougaar.util.MutableTimeSpan;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Organization;

public class PSP_Hierarchy extends PSP_Subordinates {
  public static final boolean DEBUG = false;

  /** 
   * accumulates subordinates in a set and iterates recurses over them 
   * after printing them as subords of the organization <code>org</code>
   * 
   * <pre>
   * Format is :
   * <org name=xxx>
   *  <subord name=xxx/>
   *  <subord name=xxx/>
   * </org>
   * <org name=xxx>
   *  <subord name=xxx/>
   *  <subord name=xxx/>
   * </org>
   * ....
   * </pre>
   * @param org the org to generate xml for
   **/
  protected void generateXML (PrintStream out, MyPSPState myState, Organization org) {
	Collection subordinates = org.getSubordinates(new MutableTimeSpan());
	String orgName = org.getItemIdentificationPG().getNomenclature();
	RelationshipSchedule schedule = org.getRelationshipSchedule ();
	
	if (subordinates.isEmpty ()) {
	  out.println("<org name=\""+ orgName+ "\" />");
	  return;
	}
	else
	  out.println("<org name=\""+ orgName+ "\" >");

	Set subords = new HashSet ();
	
	// Safe to iterate over subordinates because getSubordinates() returns
	// a new Collection.
	for (Iterator schedIter = subordinates.iterator(); schedIter.hasNext();) {
	  Relationship relationship = (Relationship)schedIter.next();
	  if (DEBUG)
		System.out.println ("PSP_Subordinates.getMyRelationships - self " + 
							orgName + 
							"'s relationship : " + relationship);
	  String subord =
		((Asset)schedule.getOther(relationship)).getItemIdentificationPG().getNomenclature();

	  if (DEBUG)
		System.out.println ("\tsubord is " + subord + ", subord's role is : " + 
							relationship.getRoleA());
	  if (!orgName.equals (subord)) {
		out.println("  <subord name=\""+ subord + "\"/>");
		subords.add (subord);
	  }
	}
	out.println("</org>");

	for (Iterator iter = subords.iterator(); iter.hasNext();) {
	  recurseOnSubords (out, myState, (String) iter.next ());
	}
  }
}


