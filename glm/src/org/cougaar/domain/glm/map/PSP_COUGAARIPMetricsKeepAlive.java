/*
 * <copyright>
 * Copyright 1997-2000 Defense Advanced Research Projects Agency (DARPA)
 * and Clark Software Engineering (CSE) This software to be used in
 * accordance with the COUGAAR license agreement.  The license agreement
 * and other information on the Cognitive Agent Architecture (COUGAAR)
 * Project can be found at http://www.cougaar.org or email: info@cougaar.org.
 * </copyright>
 */
package org.cougaar.domain.glm.map;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import org.cougaar.core.cluster.MetricsSnapshot;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.cluster.Subscription;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.policy.*;



/***********************************************************************************************************************
<b>Description</b>: KeepAlive PSP that sends order total updates to the KeepAlive stream.  This PSP is used by the BOL
										Admin UI tool.

<br><br><b>Notes</b>:<br>
									- 

@author Eric B. Martin, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/
public class PSP_COUGAARIPMetricsKeepAlive extends org.cougaar.lib.planserver.PSP_BaseAdapter implements org.cougaar.lib.planserver.PlanServiceProvider, org.cougaar.lib.planserver.KeepAlive, org.cougaar.lib.planserver.UISubscriber
{
	
	
	/*********************************************************************************************************************
  <b>Description</b>: Holds the loutput stream/semaphore object pairs of each incomming PSP keepalive request.

  <br><br><b>Notes</b>:<br>
										- The stream object is the key of each entry in the hashtable
	*********************************************************************************************************************/
	private Hashtable streamList = new Hashtable(1);
	private String ips = "not ready:";
	
	/****************************************************************************************************
	******************************************************************************************************/
	//private IncrementalSubscription allChangeLocationTasks = null;;
	private Subscription allChangeLocationTasks = null;
  private UnaryPredicate allChangeLocationTasksPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Task) {
      Task task = (Task) o;
      return task.getVerb().equals("IPStatistics");
    } 
    return false;
    }};
	/*********************************************************************************************************************
  <b>Description</b>: Default constructor.  This constructor simply calls its super class default constructor to ensure
  										the instance is properly constructed.

  <br><b>Notes</b>:<br>
	                  - 
	*********************************************************************************************************************/
  public PSP_COUGAARIPMetricsKeepAlive()
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
  public PSP_COUGAARIPMetricsKeepAlive(String pkg, String id) throws org.cougaar.lib.planserver.RuntimePSPException
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
  	//System.out.println("Keepalive entered");
  	//System.out.println("object is " + this);
  	  	
  	if (allChangeLocationTasks == null)
		{
			allChangeLocationTasks = psc.getServerPlugInSupport().subscribe(this, allChangeLocationTasksPredicate);
		}
		
		// Make a semaphore object to wait against while the KeepAlive connection is maintained
		Object semaphore = new Object();
		synchronized(semaphore)
		{
			// Add the PSP output stream and the semaphore to the stream list
			
			//System.out.println("add stream " + out);
			streamList.put(out, semaphore);
			/*IPStatistics ips = new IPStatistics("not ready",
    	                                    0,
    	                                    0,
    	                                    0,
    	                                    0,
    	                                    0);*/
	    	try
	     {
	      //sendToStreams("not ready:" + ips.toString() + ":" + System.currentTimeMillis());
	      sendToStreams(ips);
	     }
	     catch(Exception e)
	     {
	     	e.printStackTrace();
	     }
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
      PrepositionalPhrase pp = task.getPrepositionalPhrase("METRICS");
      ips = (String) pp.getIndirectObject();
     // System.out.println("added task " + task);
     try
     {
      sendToStreams(ips);
     }
     catch(Exception e)
     {
     	e.printStackTrace();
     }
      
    }
    
    for(Enumeration changeLocationTask = ((IncrementalSubscription)subscription).getChangedList(); changeLocationTask.hasMoreElements();)
    {
    	Task task = (Task) changeLocationTask.nextElement();
      PrepositionalPhrase pp = task.getPrepositionalPhrase("METRICS");
      //IPStatistics ips = (IPStatistics) pp.getIndirectObject();
      String ips = (String) pp.getIndirectObject();
     //System.out.println("changed task " + task);
      try
     {
      sendToStreams(ips);
     }
     catch(Exception e)
     {
     	e.printStackTrace();
     }
      
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
	public void sendToStreams(String ips)  throws IOException
	{
    PrintStream out = null;
		
		System.out.println("send to streams " + ips);
		// Go through the list of all the current KeepAlive connections and send the data to each one
		for (Enumeration streams = streamList.keys(); streams.hasMoreElements();)
		{
			try
			{
				out = (PrintStream)streams.nextElement();
			  
				// Get the current stream and send the data to it
			 				
				// Get the current stream and send the data to it
				
				out.println("<DATA " + ips + ">");
				out.flush();
			
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
