/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation;

import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Relationship;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.lib.planserver.PSPState;
import org.cougaar.lib.planserver.PSP_BaseAdapter;
import org.cougaar.lib.planserver.PlanServiceContext;
import org.cougaar.lib.planserver.PlanServiceProvider;
import org.cougaar.lib.planserver.PlanServiceUtilities;
import org.cougaar.lib.planserver.UISubscriber;
import org.cougaar.lib.planserver.RuntimePSPException;
import org.cougaar.lib.planserver.HttpInput;
import org.cougaar.util.UnaryPredicate;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

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
   * @
   **/
  protected void generateXML (PrintStream out, MyPSPState myState, Organization org) {
	RelationshipSchedule schedule = org.getRelationshipSchedule();
		  
	String orgName = org.getItemIdentificationPG().getNomenclature();

	if (DEBUG)
	  System.out.println ("PSP_Hierarchy.getMyRelationships - output : " + 
						  "<org name=\""+ orgName+ "\"></org>");

	out.println("<org name=\""+ orgName+ "\">");

	Set subords = new HashSet ();
	
	for (Iterator schedIter = schedule.listIterator(); schedIter.hasNext();) {
	  Relationship relationship = (Relationship)schedIter.next();

	  if (((relationship.getRoleA().equals(Constants.Role.ADMINISTRATIVESUBORDINATE)) &&
		   (relationship.getRoleB().equals(Constants.Role.ADMINISTRATIVESUPERIOR   )))){
		if (DEBUG)
		  System.out.println ("PSP_Subordinates.getMyRelationships - self " + 
							  orgName + 
							  "'s relationship : " + relationship);
		String subord = 
		  ((Asset)relationship.getA()).getItemIdentificationPG().getNomenclature();

		if (DEBUG)
		  System.out.println ("\tsubord is " + subord + ", subord's role is : " + relationship.getRoleA());

		if (!orgName.equals (subord)) {
		  if (DEBUG)
			System.out.println (orgName + " != " + subord + " so OK");
		  subords.add (subord);
		  out.println("  <subord name=\""+ subord + "\"/>");
		}
	  }
	}
	out.println("</org>");

	if (DEBUG)
	  System.out.println ("subord set is " + subords);

	for (Iterator iter = subords.iterator(); iter.hasNext();) {
	  recurseOnSubords (out, myState, (String) iter.next ());
	}
  }
}


