/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.ldm;

import org.cougaar.core.cluster.SubscriberException;
import org.cougaar.core.plugin.SimplePlugIn;

import org.cougaar.domain.planning.ldm.policy.Policy;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.Iterator;

/**
 * An instance of an LDMPlugIn that reads a Cluster's startup policy
 * from an XML file.
 *
 * This PlugIn is invoked with one or more parameters, the names of the
 * .ldm.xml files to be parsed.  The files are found using the cluster's
 * ConfigFinder.  
 *  Example from a sample cluster.ini file:
 * <PRE>
 * plugin=org.cougaar.domain.mlm.plugin.ldm.XMLPolicyPlugIn( policy.ldm.xml, shippolicy.ldm.xml )
 * </PRE>
 *
 * @author   ALPINE <alpine-software@bbn.com>
 * @version  $Id: XMLPolicyPlugIn.java,v 1.3 2001-01-10 20:55:56 jwinston Exp $
 */
public class XMLPolicyPlugIn extends SimplePlugIn
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
	throw new RuntimeException( "XMLPolicyPlugIn requires a parameter" );
      } else {
	// iterate through the list of XML Policy files to parse
	for (Iterator pi = pv.iterator(); pi.hasNext();) {
	  xmlfilename = (String) pi.next();
	  //System.out.println("XMLPolicyPlugIn processing file: " + xmlfilename);
	  globalParameters.put( "XMLFile", xmlfilename ); // Why are we doing this?
	  policyCreator = new XMLPolicyCreator(xmlfilename, theLDMF);
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



