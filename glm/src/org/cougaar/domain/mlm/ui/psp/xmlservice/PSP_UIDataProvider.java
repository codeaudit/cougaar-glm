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
import org.cougaar.lib.planserver.*;

import com.ibm.xml.parser.TXDocument;

public class PSP_UIDataProvider extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {
  private String myID;
  private static final String LIMIT_COMMAND = "LIMIT:";

  public PSP_UIDataProvider() throws RuntimePSPException {
    super();
  }

  public PSP_UIDataProvider(String pkg, String id) throws RuntimePSPException {
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
    that match the request; create ui data objects from those;
    encode these in XML document; send the document to the client.
  */

  public void execute( PrintStream out,
		       HttpInput query_parameters,
		       PlanServiceContext psc,
		       PlanServiceUtilities psu) throws Exception {

    String postData = null;
    boolean limit = false; // flag to limit number of items returned

    if (query_parameters.hasBody()) {
      postData = query_parameters.getBodyAsString();
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

      // define predicate to select log plan objects
      XMLUIObjectSelector xmlPredicate = new XMLUIObjectSelector(terms);
      
      // enter subscription 
      Subscription subscription = psc.getServerPluginSupport().subscribe(this, xmlPredicate);

      // get the org.cougaar.domain.mlm.ui.data objects selected
      // and get the fields requested
      Vector planObjects = xmlPredicate.getSelectedObjects();
      Vector requestedFields = ((Term)(terms.elementAt(0))).requestedFields;

      // unsubscribe, don't need this subscription any more
      psc.getServerPlugInSupport().unsubscribeForSubscriber(subscription);

      XMLPlanObjectProvider provider=null;
      //  TRAP ANY EXCEPTIONS DURING INTROSPECTION/XML GENERATION...
      //  OUTPUT MESSAGE, BUT DON'T WANT FULL STACK TRACE ---
      // if user wants only a few objects, then limit the number
      // of returned objects here
      try{
          // create XML document for selected plan objects
          provider = new XMLPlanObjectProvider(requestedFields);
	  int n = planObjects.size();
	  if (limit)
	    n = Math.min(n, 10);
          for (int i = 0; i < n; i++)
	    provider.addPlanObject(planObjects.elementAt(i));
      } catch (Exception e) {
	System.out.println("[PSP_UIDataProvider.execute()] Intercepted Exception during XML generation: " +  e.toString());
      }
      TXDocument doc = provider.getDocument();

      // send document to client
      doc.print(new PrintWriter(out));
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

