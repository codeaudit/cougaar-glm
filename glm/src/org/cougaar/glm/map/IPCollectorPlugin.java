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
import org.cougaar.core.agent.MetricsSnapshot;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.AssetGroup;
import org.cougaar.planning.ldm.asset.NewItemIdentificationPG;
//import org.cougaar.glm.ldm.asset.LocationSchedulePG;
import org.cougaar.glm.ldm.asset.MilitaryOrgPG;

import org.cougaar.planning.ldm.asset.ClusterPG;
import org.cougaar.planning.ldm.asset.ItemIdentificationPGImpl;
import org.cougaar.planning.ldm.asset.NewClusterPG;
import org.cougaar.planning.ldm.asset.NewPropertyGroup;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.ldm.asset.PropertyGroup;

import org.cougaar.glm.ldm.asset.AssignedPG;
import org.cougaar.glm.ldm.asset.AssignedPGImpl;
import org.cougaar.glm.ldm.asset.NewAssignedPG;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.OrganizationAdapter;
//import org.cougaar.glm.ldm.asset.NewLocationSchedulePG;

//import org.cougaar.glm.ldm.asset.LocationSchedulePGImpl;


import org.cougaar.glm.ldm.plan.GeolocLocation;

import org.cougaar.core.plugin.util.PluginHelper;
import org.cougaar.glm.ldm.Constants;


//import org.cougaar.glm.plugins.TaskUtils;

import org.cougaar.planning.ldm.trigger.*;
import org.cougaar.core.plugin.PluginDelegate;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.lib.util.UTILAllocate;

import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
import java.util.Enumeration;

import java.util.*;


import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;

import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;

import java.net.URLConnection;
import java.net.URL;
import java.net.InetAddress;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.FlowLayout;


/***********************************************************************************************************************
<b>Description</b>: collect organizationinfo

@author Frank Cooley, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/

public class IPCollectorPlugin extends org.cougaar.core.plugin.SimplePlugin
{
	private ActionListener dumpProfileListener = null;
	public static Hashtable organizationLocations = new Hashtable();
	public static Hashtable organizationRelationships = new Hashtable();
	public static Hashtable heartBeatOrgs = new Hashtable();
	public static Hashtable metricsOrgs = new Hashtable();
	public static Hashtable stringOrgs = new Hashtable();
	public static Hashtable hbkaVectors = new Hashtable();
	private Hashtable orgLookup = new Hashtable();
	private Hashtable failedClusters = new Hashtable();
	private String[] data;
	private String myOrgString = null;
	private boolean[] clusterStatus;
	private int lastSelection = -1;
	private int lastOrg = 0;
	public int[] metricsArray;
	JList nodeList = null;
	public static String heartBeatVerb = "HeartBeatFailure";
	//private long timeout = 45000L;
	private long timeout = 300000L;
	private Task lastCloneTask = null;
	private final static String UTC = "UTC/RTOrg";
	private static Calendar myCalendar = Calendar.getInstance();
	private static long DEFAULT_START_TIME = -1;
  private static long DEFAULT_END_TIME = -1;
  
  double geoLat = 0;
	double geoLong = 0;
	private String ipAddress = null;
  
  static {
    myCalendar.set(1990, 0, 1, 0, 0, 0);
    DEFAULT_START_TIME = myCalendar.getTime().getTime();

    myCalendar.set(2010, 0, 1, 0, 0, 0);
    DEFAULT_END_TIME = myCalendar.getTime().getTime();   
  }
  
  static
	  {
	    try
	    {
	      // See if the property exists or use the local address if the property is not defined
	      String url = System.getProperty("regurl");
	      if (url == null)
	      {
	        url = "http://" + (InetAddress.getLocalHost()).getHostAddress() + ":5555";
	      }
	      //System.out.println("%%%% ip is " + url);
	      	
	     
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	      
	    }
	  }
  
  private Organization selfOrg = null;
	
	private IncrementalSubscription allChangeLocationTasks;
	private int lineindex = 0;
	protected static String ls = System.getProperty("line.separator");
	public static boolean locationDebug = false;
  private UnaryPredicate allChangeLocationTasksPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
    	if (o instanceof Task) {
      Task task = (Task) o;
      return task.getVerb().equals("LocationChange");
    } 
    return false;
      
    }};
  private IncrementalSubscription allHeartBeatTasks;  
  private UnaryPredicate allHeartBeatTasksPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
    	if (o instanceof Task) {
      Task task = (Task) o;
      return task.getVerb().equals("HeartBeat");
    } 
    return false;
      
    }};
    
    private IncrementalSubscription allOrganizationTasks;
    private static UnaryPredicate allOrganizationsPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Organization )
      {
      	
      	if(((Organization) o).getUID().toString().startsWith("MapInfo"))
      	{
      		
      	  return false;
      	}
      	else
      	{
      		
      	  return true;
      	}
      }
      return false;
    }};
    
    private IncrementalSubscription allSelfOrganizationTasks;
    private static UnaryPredicate allSelfOrganizationsPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Organization )
      {
      	return ((Organization) o).isSelf();
      }
      return false;
    }};
    
    private IncrementalSubscription heartbeatAllocations;
  private UnaryPredicate heartbeatAllocationsPredicate = new UnaryPredicate()
  {
     public boolean execute(Object o)
     {
       if (o instanceof Allocation) 
       {
        Allocation alloc = (Allocation) o;
        return alloc.getTask().getVerb().equals("HeartBeat");
       } 
         return false;
     }
  };
    
  
// ---------------------------------------------------------------------------------------------------------------------
// Public Member Methods
// ---------------------------------------------------------------------------------------------------------------------

	/*********************************************************************************************************************
  <b>Description</b>: Subscribe to organizations.

	*********************************************************************************************************************/
  public void setupSubscriptions()
  {
  	System.out.println("IP Collector Init");
  	allOrganizationTasks = (IncrementalSubscription)subscribe(allOrganizationsPredicate);
    allHeartBeatTasks = (IncrementalSubscription)subscribe(allHeartBeatTasksPredicate);
    allSelfOrganizationTasks = (IncrementalSubscription)subscribe(allSelfOrganizationsPredicate);
    parseParameters();
    
  }
  
	/*********************************************************************************************************************
  <b>Description</b>: Called by infrastructure whenever something we are interested in is changed or added.

	*********************************************************************************************************************/
  public void execute()
  {
  	
  	if(locationDebug)
      System.out.println("%%%% LocationCollector Execute");
    // Go through every new task we've subscribed to
    if(selfOrg == null)
	    for(Enumeration selfOrganization = allSelfOrganizationTasks.getAddedList(); selfOrganization.hasMoreElements();)
	    {
	    	try
	    	{
	    	  ipAddress = (InetAddress.getLocalHost()).getHostAddress();
	    	  System.out.println("%%%% ip is " + ipAddress);
	      }
		    catch(Exception e)
		    {
		    	e.printStackTrace();
		    }
		    
		    Organization org = (Organization) selfOrganization.nextElement();
	    	MilitaryOrgPG mpg = org.getMilitaryOrgPG();
	      GeolocLocation homeLoc = null;
	      String symbol = null;
	      if(mpg != null)
	      {
		      homeLoc = (GeolocLocation)mpg.getHomeLocation();
		      long l1 = 0;               // set to forever
          long l2 = Long.MAX_VALUE;
	        NewLocationScheduleElement home = new LocationScheduleElementImpl(l1, l2, homeLoc);
	        System.out.println("homeloc " + home);
	        
	        
	        GeolocLocation  orgLoc = (GeolocLocation) home.getLocation();
		   		Latitude latitude = orgLoc.getLatitude();
	    		Longitude longitude = orgLoc.getLongitude();
		   		geoLat = latitude.getDegrees();
		   		geoLong = longitude.getDegrees();
		   		System.out.println("lat/long " + geoLat + "   " + geoLong);
	        
	        
	        
		      //System.out.println("%%%% home is " + homeLoc);
		    }
	    	
	    	
	      selfOrg   = org;
	      ClusterIdentifier c = org.getClusterIdentifier();
	      myOrgString = c.toString();
	      if(locationDebug)
           System.out.println("%%%% LocationCollector SelfOrg " + c);
	    }
    
    
    
    
        
    for(Enumeration heartBeatTasks = allHeartBeatTasks.getAddedList(); heartBeatTasks.hasMoreElements();)
    {
    	Task hbTask = (Task) heartBeatTasks.nextElement();
    	System.out.println("&&&& locationCollector Heartbeat ");
    	
    	if(locationDebug)
	      System.out.println("&&&& locationCollector Heartbeat for  " + myOrgString);
    	PrepositionalPhrase met = hbTask.getPrepositionalPhrase("METRICS");
    	IPStatistics ips = (IPStatistics) met.getIndirectObject();
    	ips.setNodeInfo(ipAddress, geoLat, geoLong);
    	
    	createStringAndNotify(theLDMF, myOrgString, "Started", ips);
        
    }
    
    
    System.out.println("%%%% LocationCollector Handle Heartbeat");   
    
    
    wakeAfter(60000L);
    
  }
  
  
  /*********************************************************************************************************************
  <b>Description</b>: Looks at the Plugin parameters for the debug value.
	*********************************************************************************************************************/
  private void parseParameters()
  {
  	System.out.println("&&&& ip collector parsing");
  	
  
  }
  
  
  
  /*********************************************************************************************************************
  <b>Description</b>: Create a heartbeat task to send to locationcollector.

	*********************************************************************************************************************/
  
  public void createStringAndNotify(RootFactory theLDMF, String org, String action, IPStatistics ms)
  {
  	Vector preps = new Vector();
  	long time = currentTimeMillis();
  	String outString = org + ":" + ms + ":" + time;
  	System.out.println("%%%% outstring is " + outString);
  	NewTask task = theLDMF.newTask();
	  task.setDirectObject(null);
	  task.setVerb(new Verb("IPStatistics")); 
	  NewPrepositionalPhrase metricsPP = theLDMF.newPrepositionalPhrase();
	  metricsPP.setPreposition("METRICS");
	  metricsPP.setIndirectObject(outString);
	  preps.add(metricsPP);
	  
	  task.setPrepositionalPhrases(preps.elements());
	  
	  publishAdd(task);
	  System.out.println("%%%% published ipstat task");
  }
  
  /*********************************************************************************************************************
  <b>Description</b>: creates an xml srting from metricssnapshot object

  <br><b>Notes</b>:<br>
	                  - 

  <br>
  @param metrics metricssnapshot object
  
	*********************************************************************************************************************/
	public String convertToXML(HbMetricsSnapshot metrics)
	{
		if(metrics == null)
		  return null;
		//System.out.println("metrics " + metrics.describe());
		String xmlString = metrics.describe();
		xmlString = xmlString.replace('\n', ' ');
		xmlString = xmlString.replace('\t', ' ');
		return xmlString;
	}    
	
  
}
