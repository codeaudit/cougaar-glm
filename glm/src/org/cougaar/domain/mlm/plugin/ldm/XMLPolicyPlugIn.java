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

/**
 * An instance of an LDMPlugIn that reads a Cluster's startup policy
 * from an XML file (of the form *.ldm.xml).
 *
 * This PlugIn is invoked with one parameter, the name of the
 * .ldm.xml file to be parsed.  This file is currently looked for 
 * in the local directory.  Additional file search capabilities will
 * be added.  Example from a sample cluster.ini file:
 * <PRE>
 * plugin=org.cougaar.domain.mlm.plugin.ldm.XMLPolicyPlugIn( policy.ldm.xml )
 * </PRE>
 *
 * @author   ALPINE <alpine-software@bbn.com>
 * @version  $Id: XMLPolicyPlugIn.java,v 1.1 2000-12-15 20:17:46 mthome Exp $
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
      getParams();
      policyCreator = new XMLPolicyCreator(xmlfilename, theLDMF);
      Policy policies[] = policyCreator.getPolicies();

      for (int i=0; i<policies.length; i++) {
	publishAdd(policies[i]);
      }
    } catch ( SubscriberException se ) {
      se.printStackTrace();
    }
  }

  /** 
   * Do nothing
   */
  public void execute() {}


  /**
   * Parse parameters passed to PlugIn
   */
  private void getParams() {
    Vector pv = getParameters();
    if ( pv == null ) {
      throw new RuntimeException( "XMLPolicyPlugIn requires a parameter" );
    } else {
      try {
	Enumeration ps = pv.elements();
	String p = (String) ps.nextElement();
	globalParameters.put( "XMLFile", p );
	xmlfilename = p;
      } catch( Exception e ) {
	e.printStackTrace();
      }
    }
  }
}



