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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.UnaryPredicate;

import org.w3c.dom.Document;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import org.cougaar.mlm.ui.data.UIMPTaskImpl;
import org.cougaar.mlm.ui.data.UITaskImpl;

public class PSP_FailedAllocation extends PSP_BaseAdapter implements PlanServiceProvider, KeepAlive, UISubscriber
{
  private Vector failedAllocations;

  /** A zero-argument constructor is required for dynamically loaded PSPs.
   */

  public PSP_FailedAllocation() {
    super();
    failedAllocations = new Vector();
  }

  /** Used to match incoming requests to this PSP.
   */
    
  public PSP_FailedAllocation( String pkg, String id ) throws RuntimePSPException {
    setResourceLocation(pkg, id);
    failedAllocations = new Vector();
  }

  /* Subscribe to failed allocations associated with supply tasks.
   */

  private static UnaryPredicate myPredicate = 
    new org.cougaar.planning.ldm.predicate.DispositionPredicate() {
        public boolean execute(Disposition o) {
          //Task task = o.getTask();
          //    if (task.getVerb().equals(Constants.Verb.SUPPLY))
          return true;
          //return false;
        }
      };

  /** PlanServiceProvider interface.
   */

  /* This PSP is referenced directly (in the URL from the client)
     and hence this shouldn't be called.
     */

  public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  /* Execute loop on the PSP; periodically wakes up and sends
     failed allocations to the client.
     */

  public void execute( PrintStream out,
                       HttpInput query_parameters,
                       PlanServiceContext psc,
                       PlanServiceUtilities psu) throws Exception
  {
    Subscription subscription = 
        psc.getServerPluginSupport().subscribe(this, myPredicate);
    // loop through the failed allocations we've seen since last awakened
    // and send the tasks to the client as XML encoded UIData objects
    while( true ) {
      XMLPlanObjectProvider provider = null;
      Vector planObjects = new Vector(0);

      synchronized (failedAllocations) {
        Enumeration e = failedAllocations.elements();
        while (e.hasMoreElements()) {
          Disposition failedAllocation = (Disposition)e.nextElement();
          if (!failedAllocation.isSuccess()) {
            System.out.println("CHECKING FAILED ALLOCATIONS");
            Task task = failedAllocation.getTask();
            if (MPTask.class.isInstance(task))
              planObjects.addElement(new UIMPTaskImpl((MPTask)task));
            else
              planObjects.addElement(new UITaskImpl(task));
          }
        }
        // discard the failed allocations we've seen 
        failedAllocations.setSize(0); 
      }
      
      // create XML document for selected plan objects
      Vector requestedFields = null; // send all fields for now
      try {
        provider = new XMLPlanObjectProvider(requestedFields);
        for (int i = 0; i < planObjects.size(); i++)
          provider.addPlanObject(planObjects.elementAt(i));
      } catch (Exception e) {
        System.out.println("[PSP_UIDataProvider.execute()] Intercepted Exception during XML generation: " +  e.toString());
      }
      // send document to client
      // first writes document to buffer and counts bytes
      // then prepends content length to buffer actually sent
      if (planObjects.size() > 0) {
        Document doc = provider.getDocument();
        ByteArrayOutputStream serviceOut = new ByteArrayOutputStream(512);

	OutputFormat format = new OutputFormat();
	format.setPreserveSpace(true);
	format.setIndent(2);

	XMLSerializer serializer = 
	  new XMLSerializer(new PrintWriter(serviceOut), format);

	serializer.serialize(doc);

        String header  = new String("\n\rContent-Length:" + serviceOut.size()
                                    + "\r\n");
        System.out.println("Sent bytes: " + serviceOut.size());
        byte headerbytes[] = header.getBytes();
        synchronized (out) {
          out.write(headerbytes);
          serviceOut.writeTo(out);
          out.flush();
        }


        System.out.println("Sent XML document");
      }
      // go to sleep for awhile
      try { 
        Thread.sleep(5000); 
      } catch (Exception e) { 
        e.printStackTrace();
      }
    }
  }

  // for debugging only
  private void printDocument(Document doc) {
    try {
      OutputFormat format = new OutputFormat();
      format.setPreserveSpace(true);
      format.setIndent(2);

      PrintWriter out = new PrintWriter(System.out);
      XMLSerializer serializer = new XMLSerializer(out, format);
      serializer.serialize(doc);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** PlanServiceProvider interface; the following are unused currently. 
   */

  /** A PSP can output either HTML or XML (for now).  
   */

  public boolean returnsXML() {
    return true;
  }

  public boolean returnsHTML() {
    return false;
  }

  /**  Return DTD if this PSP returns XML, else return null. Unused.
   */

  public String getDTD()  {
    return null;
  }

  /** End unused PlanServiceProvider methods.
   */


  /* UISubsriber interface.
     Called when receive more failed allocations.
     Just add them to the list that will be sent to the user
     the next time that this PSP is run.
   */

  public void subscriptionChanged(Subscription subscription) {
    synchronized(failedAllocations) {
      Enumeration e = ((IncrementalSubscription)subscription).getAddedList();
      while (e.hasMoreElements()) {
        System.out.println("ADDED FAILED ALLOCATION");
        Disposition fa = (Disposition)e.nextElement();
        failedAllocations.addElement(fa);
      }
    }
  }

}
