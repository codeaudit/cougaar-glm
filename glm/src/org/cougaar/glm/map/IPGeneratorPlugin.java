/*
 * <copyright>
 * Copyright 1997-2000 Defense Advanced Research Projects Agency (DARPA)
 * and Clark Software Engineering (CSE) This software to be used in
 * accordance with the COUGAAR license agreement.  The license agreement
 * and other information on the Cognitive Agent Architecture (COUGAAR)
 * Project can be found at http://www.cougaar.org or email: info@cougaar.org.
 * </copyright>
 */
package org.cougaar.glm.map;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.agent.MetricsSnapshot;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.AssetGroup;
import org.cougaar.planning.ldm.asset.NewItemIdentificationPG;

//import org.cougaar.glm.ldm.asset.LocationSchedulePG;

import org.cougaar.planning.plugin.legacy.PluginDelegate;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.lib.util.UTILAllocate;

import org.cougaar.core.mts.MessageAddress;

//import org.cougaar.core.node.NameServer;
//import org.cougaar.core.mts.MessageTransportServer;


import java.util.Enumeration;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Iterator;




import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedInputStream;
import java.net.URLConnection;
import java.net.URL;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.awt.FlowLayout;


/***********************************************************************************************************************
<b>Description</b>: collect organizationinfo

@author Frank Cooley, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/

public class IPGeneratorPlugin extends org.cougaar.planning.plugin.legacy.SimplePlugin
{
	
	public static String heartBeatVerb = "HeartBeat";
	public static String metricsVerb = "Metrics";
	public static String orgString = "Organization";
	private Organization LocationInfoOrg = null;
	private MetricsSnapshot metrics = null;
	private boolean mapInfoExists = false;
	private long heartbeatTime = 15000L;
	private long initialHBTime = 1500000L;
	private long timeoutTime = 60000L;
	private int suspendAfter = 5;
	private int restartAfter = 5;
	private int suspendCount = 0;
	private int restartCount = 0;
	private boolean testSuspend = false;
	private boolean suspended = false;
	private boolean doHeartBeat = false;
	private long lastWakeUp = 0;
	private Task lastTask = null;
	private Organization myOrganization = null;
	private String locationInfoOrgString = null;
	private int xmitPktInc = 20;
	private int rcvPktInc = 15;
	private int lostPktInc = 3;
	private IPStatistics ips = new IPStatistics();
		
	private IncrementalSubscription mapInfoOrganizationTasks;
	
  private static UnaryPredicate mapInfoOrganizationsPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Organization )
      {
      	MessageAddress c = ((Organization) o).getMessageAddress();
      	//System.out.println("&&&clusterid = " + c);
      	if(c.toString().startsWith("LocationInfo"))
      	  return true;
      	else
      	  return false;
        
      }
      return false;
    }};
    
   // Predicate for all tasks of verb with Organization object
	private IncrementalSubscription allSelfOrganizationTasks;
  private static UnaryPredicate allSelfOrganizationsPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Organization )
      {
      	return ((Organization) o).isSelf();
      }
      return false;
    }};
    
    private IncrementalSubscription allHeartBeatTasks;  
    private UnaryPredicate allHeartBeatTasksPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
    	if (o instanceof Task) {
      Task task = (Task) o;
      return task.getVerb().equals("StartHB");
    } 
    return false;
      
    }};
	  
// ---------------------------------------------------------------------------------------------------------------------
// Public Member Methods
// ---------------------------------------------------------------------------------------------------------------------

	/*********************************************************************************************************************
  <b>Description</b>: Subscribe to organizations.

	*********************************************************************************************************************/
  public void setupSubscriptions()
  {
  	System.out.println("%%%% Init ip generator");
  	mapInfoOrganizationTasks = (IncrementalSubscription)subscribe(mapInfoOrganizationsPredicate);
  	allSelfOrganizationTasks = (IncrementalSubscription)subscribe(allSelfOrganizationsPredicate);
  	allHeartBeatTasks = (IncrementalSubscription)subscribe(allHeartBeatTasksPredicate);
  	//parseParameters();
  	//heartbeatTime = initialHBTime;
  	heartbeatTime = timeoutTime;	
    wakeAfter(heartbeatTime);
    
	}

	/*********************************************************************************************************************
  <b>Description</b>: Called by infrastructure whenever something we are interested in is changed or added.

	*********************************************************************************************************************/
  public void execute()
  {
  	System.out.println("%%%% IPGeneratorPlugin " + myOrganization);
  	
  	for(Enumeration heartBeatTasks = allHeartBeatTasks.getAddedList(); heartBeatTasks.hasMoreElements();)
    {
    	Task task = (Task)heartBeatTasks.nextElement();
    	PrepositionalPhrase on = task.getPrepositionalPhrase("HBSTATUS");
      String hbStatus = (String) on.getIndirectObject();
      System.out.println("%%%% status is " + hbStatus);
      if(hbStatus.equals("True"))
      {
    	  doHeartBeat = true;
    	  if(!wasAwakened())
    	    wakeAfter(heartbeatTime);    //  have to set it here
    	}
    	else
    	  doHeartBeat = false;
    	System.out.println("%%%% got startHB for cluster " + myOrganization.getMessageAddress());
    }
     	 
      mapInfoExists = true;
  
  	
  	
  	heartbeatTime = timeoutTime;
  	System.out.println("%%%% setting HB timeout to " + heartbeatTime + " msec");
    String org = theLDMF.getMessageAddress().getAddress();
    
    //System.out.println("%%%% for cluster " + org);
    if(mapInfoExists)
    {
    	if(lastTask != null)
    	  removeLastHeartbeatTask();
    	createHeartBeatTask(theLDMF, heartBeatVerb);
    	System.out.println("Create HB task");
	  	    
	  }
	  //if(itsTime())
	    wakeAfter(heartbeatTime);
	    System.out.println("set to wakeup " + heartbeatTime);
  }
  
  
  /*********************************************************************************************************************
  <b>Description</b>: Save the task, but with a different allocation.

	*********************************************************************************************************************/
  
  public void removeLastHeartbeatTask()
  {
  	System.out.println("%%%% removing last task "  + myOrganization);
  	publishRemove(lastTask);
  }
  
  /*********************************************************************************************************************
  <b>Description</b>: Create a heartbeat task to send to locationcollector.

	*********************************************************************************************************************/
  
  public void createHeartBeatTask(PlanningFactory theLDMF, String verb)
  {
  	Vector preps = new Vector();
  	System.out.println("%%%% psp - entered task function");
    
    NewTask newtask = theLDMF.newTask();
    newtask.setDirectObject(null);
    newtask.setVerb(new Verb(verb));
      	  
	  NewPrepositionalPhrase metricsPP = theLDMF.newPrepositionalPhrase();
	  metricsPP.setPreposition("METRICS");
	  ips = buildMetrics();
	  metricsPP.setIndirectObject(ips);
	  preps.add(metricsPP);
	  
	  newtask.setPrepositionalPhrases(preps.elements());
	  publishAdd(newtask);
	  
	  System.out.println("%%%% psp - published task");
	  
  }
  
  /*********************************************************************************************************************
  <b>Description</b>: Looks at the Plugin parameters for the debug value.
	*********************************************************************************************************************/
  private void parseParameters()
  {
  	//System.out.println("&&&& parsing");
    Vector pVec = getParameters();
        
    for(int i = 0; i < pVec.size(); i++)
    {
    	//System.out.println("parsing param " + i);
    	String paramString = (String) pVec.elementAt(i);
    	if(paramString.startsWith("xmit="))
    	{
    		int equalIndex = paramString.indexOf("=");
    		paramString = paramString.substring(equalIndex + 1);
    		
    		//timeoutTime = Integer.parseInt ( paramString );
    		xmitPktInc = Integer.parseInt ( paramString );
    		//System.out.println("heartbeat = " + heartbeatTime);
    	}
    	else if(paramString.startsWith("rcv="))
    	{
    		int equalIndex = paramString.indexOf("=");
    		paramString = paramString.substring(equalIndex + 1);
    		//initialHBTime = Integer.parseInt ( paramString );
    		rcvPktInc = Integer.parseInt ( paramString );
    	}
    	else if(paramString.startsWith("lost="))
    	{
    		int equalIndex = paramString.indexOf("=");
    		paramString = paramString.substring(equalIndex + 1);
    		//suspendAfter = Integer.parseInt ( paramString );
    		lostPktInc = Integer.parseInt ( paramString );
    	}
    	else if(paramString.startsWith("timeout="))
    	{
    		int equalIndex = paramString.indexOf("=");
    		paramString = paramString.substring(equalIndex + 1);
    		//restartAfter = Integer.parseInt ( paramString );
    		timeoutTime = Integer.parseInt ( paramString );
    	}
    	else
    	  testSuspend = true;
    }
    
  }
  
  private boolean itsTime()
  {
  	long now = currentTimeMillis();
  	if((lastWakeUp + heartbeatTime) >= now)
  	{
  		lastWakeUp = now;
  	  return true;
  	}
  	else
  	  return false;
  	
  }
  /************************************************************************************
  *
  *************************************************************************************/
  
  public IPStatistics buildMetrics()
  {
  	int xrand = (int)(Math.random() * xmitPktInc);
  	int rrand = (int)(Math.random() * rcvPktInc);
  	int lrand = (int)(Math.random() * lostPktInc);
  	ips.applyIncrement(xrand, rrand, lrand);
  	return ips;
  }
  
}
