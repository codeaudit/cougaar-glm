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

package org.cougaar.mlm.plugin.ldm;

import org.cougaar.core.blackboard.SubscriberException;
import org.cougaar.planning.plugin.legacy.SimplePlugin;

import org.cougaar.planning.ldm.policy.Policy;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.Iterator;

/**
 * An instance of an LDMPlugin that reads a Cluster's startup policy
 * from an XML file.
 *
 * This Plugin is invoked with one or more parameters, the names of the
 * .ldm.xml files to be parsed.  The files are found using the cluster's
 * ConfigFinder.  
 *  Example from a sample cluster.ini file:
 * <PRE>
 * plugin=org.cougaar.mlm.plugin.ldm.XMLPolicyPlugin( policy.ldm.xml, shippolicy.ldm.xml )
 * </PRE>
 *
 * @author   ALPINE <alpine-software@bbn.com>
 *
 */
public class XMLPolicyPlugin extends SimplePlugin
{

  private XMLPolicyCreator policyCreator;
  private Properties globalParameters = new Properties();
  private String xmlfilename;
	
  protected void setupSubscriptions() {
    getSubscriber().setShouldBePersisted(false);
    if (didRehydrate()) return; // Is this right?

    try {

      Vector pv = getParameters();
      if ( pv == null ) {
	throw new RuntimeException( "XMLPolicyPlugin requires a parameter" );
      } else {
	// iterate through the list of XML Policy files to parse
	for (Iterator pi = pv.iterator(); pi.hasNext();) {
	  xmlfilename = (String) pi.next();
	  //System.out.println("XMLPolicyPlugin processing file: " + xmlfilename);
	  globalParameters.put( "XMLFile", xmlfilename ); // Why are we doing this?
	  policyCreator = new XMLPolicyCreator(xmlfilename, 
					       getConfigFinder(),
                                               theLDMF);
	  Policy policies[] = policyCreator.getPolicies();

	  for (int i=0; i<policies.length; i++) {
	    publishAdd(policies[i]);
	  }
	}
      }
    } catch ( SubscriberException se ) {
      se.printStackTrace();
    }
  }

  /** 
   * Do nothing
   */
  public void execute() {}

}



