/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.xmlservice;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Vector;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.UnaryPredicate;

import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.w3c.dom.Document;

import org.cougaar.domain.mlm.ui.data.UIAssetImpl;

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

