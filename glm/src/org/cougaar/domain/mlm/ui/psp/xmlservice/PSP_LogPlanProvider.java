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
 
package org.cougaar.domain.mlm.ui.psp.xmlservice;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.lib.planserver.*;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

public class PSP_LogPlanProvider extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {
  private String myID;
  private static final String LIMIT_COMMAND = "LIMIT:";

  public PSP_LogPlanProvider() throws RuntimePSPException {
    super();
  }

  public PSP_LogPlanProvider(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
    setID(id);
  }

  public String getID() {
    return myID;
  }

  public void setID(String id) {
    myID = id;
  }

  /* This PSP is referenced directly (in the URL from the client)
     and hence this shouldn't be called.
     */

  public boolean test(HttpInput query_parameters, PlanServiceContext psc) {
    super.initializeTest();
    return false; 
  }

  /*
    Called when a request is received from a client.
    Get the POST data; parse the request; get the log plan objects
    that match the request; encode these in XML document; send the
    document to the client.
  */

  public void execute( PrintStream out,
		       HttpInput query_parameters,
		       PlanServiceContext psc,
		       PlanServiceUtilities psu) throws Exception {
			 
    String postData = null;
    boolean limit = false; // flag to limit number of items returned

    if (query_parameters.getPostData() != null) {
      postData = new String(query_parameters.getPostData());
      postData = postData.trim();
      System.out.println("POST DATA:" + postData);

      // if postData starts with the term "LIMIT"
      // then strip this off and limit the number of items returned
      if (postData.startsWith(LIMIT_COMMAND)) {
	postData = postData.substring(LIMIT_COMMAND.length());
	limit = true;
      }
      // parse request from user
      Vector terms = RequestParser.parseRequest(postData);

      // if request is a command, execute it and return
      if (terms != null) {
	Term term = (Term)terms.elementAt(0);
	Method commandMethod = term.commandMethod;
	if (commandMethod != null) {
	  Object[] args = { psc, out };
	  commandMethod.invoke(null, args);
	  return;
	}
      }

      // define predicate to retrieve selected log plan objects
      XMLObjectSelector xmlPredicate = new XMLObjectSelector(terms);

      // enter subscription and get objects from it
      Subscription subscription = 
	psc.getServerPlugInSupport().subscribe(this, xmlPredicate);
      Collection container = 
	((CollectionSubscription)subscription).getCollection();
      Vector planObjects = new Vector(container.size());
      // if user wants only a few objects, then limit the number
      // of returned objects here
      if (limit) {
        int nItems = 10;
        for (Iterator i = container.iterator(); i.hasNext();) {
	  planObjects.addElement(i.next());
	  nItems--;
	  if (nItems == 0) break;
	}
      } else 
        for (Iterator i = container.iterator(); i.hasNext();)
	  planObjects.addElement(i.next());

      // unsubscribe, don't need this subscription any more
      psc.getServerPlugInSupport().unsubscribeForSubscriber(subscription);

      // create XML document for selected plan objects
      XMLPlanObjectProvider provider = new XMLPlanObjectProvider();
      for (int i = 0; i < planObjects.size(); i++)
	provider.addPlanObject(planObjects.elementAt(i));
      Document doc = provider.getDocument();

      // send document to client
      OutputFormat format = new OutputFormat();
      format.setPreserveSpace(true);
      format.setIndent(2);
      
      XMLSerializer serializer = 
	new XMLSerializer(new PrintWriter(out), format);
      serializer.serialize(doc);
      System.out.println("Sent XML document");
    }
  }

  public boolean returnsXML() {
    return true;
  }

  public boolean returnsHTML() {
    return false;
  }



  /* This should eventually return a valid DTD; should we define the return type
     as being a DTD to enforce this?
  */

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

