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
 
package org.cougaar.mlm.ui.psp.xmlservice;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Vector;

import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.UnaryPredicate;

import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.w3c.dom.Document;

import org.cougaar.mlm.ui.data.UIAssetImpl;

public class PSP_Maintenance extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {
  private String myID;

  public PSP_Maintenance() throws RuntimePSPException {
    super();
  }

  public PSP_Maintenance(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  /* This PSP is referenced directly (in the URL from the client)
     and hence this shouldn't be called.
     */

  public boolean test(HttpInput query_parameters, PlanServiceContext psc) {
    super.initializeTest();
    return false; 
  }

  /** Subscribe to assets from which we'll get the schedule
    from the ScheduleContentPG.
    */

  private static UnaryPredicate myPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Asset)
	return true;
      else
	return false;
    }
  };

  /*
    Called when a request is received from a client.
  */

  public void execute( PrintStream out,
		       HttpInput query_parameters,
		       PlanServiceContext psc,
		       PlanServiceUtilities psu) throws Exception {

    Subscription subscription = 
      psc.getServerPlugInSupport().subscribe(this, myPredicate);

    Vector assets = new Vector(((CollectionSubscription)subscription).getCollection());

    // unsubscribe, don't need this subscription any more
    psc.getServerPlugInSupport().unsubscribeForSubscriber(subscription);

    // add UI data versions of all the assets to the XML document
    // just retrieve typeIdentification and property fields
    Vector requestedFields = new Vector(2);
    requestedFields.addElement("typeIdentification");
    requestedFields.addElement("property");
    XMLPlanObjectProvider provider = 
      new XMLPlanObjectProvider(requestedFields);
    try {
      for (int i = 0; i < assets.size(); i++)
	provider.addPlanObject(new UIAssetImpl((Asset)assets.elementAt(i)));
    } catch (Exception e) {
      System.out.println("PSP_Maintenance: " + e);
    }

    // send the XML document to the client
    Document doc = provider.getDocument();
    provider.printDocument(); // for debugging

    OutputFormat format = new OutputFormat();
    format.setPreserveSpace(true);
    format.setIndent(2);
    
    XMLSerializer serializer = 
       new XMLSerializer(new PrintWriter(out), format);
    serializer.serialize(doc);

    System.out.println("Sent XML document");
  }

  public boolean returnsXML() {
    return true;
  }

  public boolean returnsHTML() {
    return false;
  }

  public String getDTD() {
    return "myDTD";
  }

  /* The UISubscriber interface.
     This PSP doesn't care if subscriptions change
     because it treats each request as a new request.
  */

  public void subscriptionChanged(Subscription subscription) {
  }


}

