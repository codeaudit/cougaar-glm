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

package org.cougaar.mlm.plugin.ldm;

import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.cougaar.core.blackboard.SubscriberException;
import org.cougaar.planning.ldm.policy.Policy;
import org.cougaar.planning.plugin.legacy.SimplePlugin;

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



