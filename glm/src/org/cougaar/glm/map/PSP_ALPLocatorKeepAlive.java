/*
 * <copyright>
 *  Copyright 1997-2001 Clark Software Engineering (CSE) 
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
package org.cougaar.glm.map;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.Subscription;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.glm.ldm.*;import org.cougaar.glm.ldm.*;import org.cougaar.glm.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.policy.*;



/***********************************************************************************************************************
<b>Description</b>: KeepAlive PSP that sends order total updates to the KeepAlive stream.  This PSP is used by the BOL
										Admin UI tool.

<br><br><b>Notes</b>:<br>
									- 

@author Eric B. Martin, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/
public class PSP_ALPLocatorKeepAlive extends org.cougaar.lib.planserver.PSP_BaseAdapter implements org.cougaar.lib.planserver.PlanServiceProvider, org.cougaar.lib.planserver.KeepAlive, org.cougaar.lib.planserver.UISubscriber
{
	
	
	/*********************************************************************************************************************
  <b>Description</b>: Holds the loutput stream/semaphore object pairs of each incomming PSP keepalive request.

  <br><br><b>Notes</b>:<br>
										- The stream object is the key of each entry in the hashtable
	*********************************************************************************************************************/
	private Hashtable streamList = new Hashtable(1);
	
	/****************************************************************************************************
	******************************************************************************************************/
	//private IncrementalSubscription allChangeLocationTasks = null;;
	private Subscription allChangeLocationTasks = null;
  private UnaryPredicate allChangeLocationTasksPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Task) {
      Task task = (Task) o;
      return task.getVerb().equals("LocationChange");
    } 
    return false;
    }};
	/*********************************************************************************************************************
  <b>Description</b>: Default constructor.  This constructor simply calls its super class default constructor to ensure
  										the instance is properly constructed.

  <br><b>Notes</b>:<br>
	                  - 
	*********************************************************************************************************************/
  public PSP_ALPLocatorKeepAlive()
  {
    super();
  }

	/*********************************************************************************************************************
  <b>Description</b>: Constructor.  This constructor sets the PSP's resource location according to the parameters
  										passed in.

  <br><b>Notes</b>:<br>
	                  - 

  <br>
  @param pkg The package id
  @param id The PSP name

  @throws RuntimePSPException
	*********************************************************************************************************************/
  public PSP_ALPLocatorKeepAlive(String pkg, String id) throws org.cougaar.lib.planserver.RuntimePSPException
  {
    setResourceLocation(pkg, id);
  }

	/*********************************************************************************************************************
  <b>Description</b>: Sends the LocationScheduleElementChanges to the KeepAlive stream when they change.

  <br><b>Notes</b>:<br>
	                  - Uses a BOLPSPState object to hold PSP configuration and HTTP request data<BR>
	                  - Catches all Throwable objects and prints a stack trace to the HTTP response output

  <br>
  @param out HTTP response socket stream
  @param queryParameters HTTP parameter data and connection information
  @param psc Current Plan Service Context object
  @param psu Utility functions for the PSP

  @throws Exception 
	*********************************************************************************************************************/
  public void execute(PrintStream out, HttpInput queryParameters, PlanServiceContext psc, PlanServiceUtilities psu) throws Exception
  {
  	System.out.println("Keepalive entered");
  	
  	if (allChangeLocationTasks == null)
		{
			allChangeLocationTasks = psc.getServerPlugInSupport().subscribe(this, allChangeLocationTasksPredicate);
		}
  	
  	
		// Make a semaphore object to wait against while the KeepAlive connection is maintained
		Object semaphore = new Object();
		synchronized(semaphore)
		{
			// Add the PSP output stream and the semaphore to the stream list
			
			System.out.println("add stream " + out);
			streamList.put(out, semaphore);
			try
			{
				// Wait to be notified that the KeepAlive stream has been closed and return
				semaphore.wait();
			}
			catch (InterruptedException e)
			{
				System.out.println("exception from wait in keepalive");
		  }
		  
		}
  }
  /*********************************************************************************************************************
  <b>Description</b>: Notification method which is invoked when the bookOrdersSubscription object subscription has been
  										changed.  This method will send the data stream to every PSP connection in the streamList,
  										un-blocking the KeepAlive threads of the streams that get exceptions when they are written to and
  										removes those streams from the streamList hashtable.

  <br><b>Notes</b>:<br>
	                  - 

  <br>
  @param subscription Subscription object that changed
  
  @see #execute(PrintStream, HttpInput, PlanServiceContext, PlanServiceUtilities)
  @see #streamList
	*********************************************************************************************************************/
	public void subscriptionChanged(Subscription subscription)
	{
		// Go through every new task we've subscribed to  ((IncrementalSubscription)subscription).getAddedList()
    for(Enumeration changeLocationTask = ((IncrementalSubscription)subscription).getAddedList(); changeLocationTask.hasMoreElements();)
    {
      Task task = (Task) changeLocationTask.nextElement();
      PrepositionalPhrase pp = task.getPrepositionalPhrase("LOCATIONINFO");
      MapLocationInfo scheduleElements = (MapLocationInfo) pp.getIndirectObject();
      System.out.println("added task " + task);
      sendToStreams(scheduleElements);
      
    }
    
    for(Enumeration changeLocationTask = ((IncrementalSubscription)subscription).getChangedList(); changeLocationTask.hasMoreElements();)
    {
    	Task task = (Task) changeLocationTask.nextElement();
      PrepositionalPhrase pp = task.getPrepositionalPhrase("LOCATIONINFO");
      MapLocationInfo scheduleElements = (MapLocationInfo) pp.getIndirectObject();
      System.out.println("changed task " + task);
      sendToStreams(scheduleElements);
      
    }

		
	}

	/*********************************************************************************************************************
  <b>Description</b>: Sends an admin status page to the AdminTool status window.

  <br><b>Notes</b>:<br>
	                  - 

  <br>
  @param pspState Current state of the PSP including HTTP request parameters
  @param out Output stream
  @param psc Plan Service Context object
  @param psu Plan Service Utility object
	*********************************************************************************************************************/
	public void sendToStreams(MapLocationInfo mli)
	{
    PrintStream out = null;
		String orgName = mli.getUID();
		System.out.println("send to streams");
		// Go through the list of all the current KeepAlive connections and send the data to each one
		for (Enumeration streams = streamList.keys(); streams.hasMoreElements();)
		{
			try
			{
				out = (PrintStream)streams.nextElement();
			  
				// Get the current stream and send the data to it
			 if(mli != null)
			 {
			 	System.out.println("Sending org name from keepalive " + orgName + " to " + out);
				
				// Get the current stream and send the data to it
				
				out.println("<DATA " + orgName + ">");
				out.flush();
			 }
			}
			// Catch any exception and assume it means the KeepAlive stream is closed so there is no longer any
			// need to send data to the current stream
			catch (Exception e)
			{
				// Remove the stream from the stream list and notify the KeepAlive thread to exit
				Object semaphore = streamList.remove(out);
				synchronized(semaphore)
				{
					semaphore.notify();
				}
			}
		}
	}

	

	/*********************************************************************************************************************
  <b>Description</b>: Required by the PlanServiceProvider interface.

  <br><b>Notes</b>:<br>
	                  - Always returns false

  <br>
  @return True if PSP returns XML, false otherwise
	*********************************************************************************************************************/
	public boolean returnsXML()
	{
		return(false);
	}
	
	/*********************************************************************************************************************
  <b>Description</b>: Required by the PlanServiceProvider interface.

  <br><b>Notes</b>:<br>
	                  - Always returns true

  <br>
  @return True if PSP returns HTML, false otherwise
	*********************************************************************************************************************/
	public boolean returnsHTML()
	{
		return(true);
	}

	/*********************************************************************************************************************
  <b>Description</b>: Required by the PlanServiceProvider interface.

  <br><b>Notes</b>:<br>
	                  - Always returns null

  <br>
  @return DTD String
	*********************************************************************************************************************/
	public String getDTD()
	{
		return(null);
	}
	
	/*********************************************************************************************************************
  <b>Description</b>: Required by the PlanServiceProvider interface.

  <br><b>Notes</b>:<br>
	                  - Always returns false

  <br>
  @return True if interested, false otherwise
	*********************************************************************************************************************/
	public boolean test(HttpInput queryParameters, PlanServiceContext sc)
	{
		super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
		return(false);  // This PSP is only accessed by direct reference.
	}
}
