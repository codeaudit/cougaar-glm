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

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.asset.AssetGroup;
import org.cougaar.domain.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.domain.glm.ldm.asset.LocationSchedulePG;
import org.cougaar.domain.glm.ldm.asset.MilitaryOrgPG;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;

import org.cougaar.domain.glm.ldm.asset.Organization;
//import org.cougaar.domain.glm.plugins.TaskUtils;

import org.cougaar.domain.planning.ldm.trigger.*;
import org.cougaar.core.plugin.PlugInDelegate;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.lib.util.UTILAllocate;

import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import java.util.Enumeration;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Iterator;


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
import java.net.URLConnection;
import java.net.URL;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.awt.FlowLayout;


/***********************************************************************************************************************
<b>Description</b>: Allocate transport tasks to roles".

@author Frank Cooley, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/

public class LocationCollectorPlugIn extends org.cougaar.core.plugin.SimplePlugIn
{
	
	public static Hashtable organizationLocations = new Hashtable();
	private IncrementalSubscription allChangeLocationTasks;
	private int lineindex = 0;
	protected static String ls = System.getProperty("line.separator");
  private UnaryPredicate allChangeLocationTasksPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
    	if (o instanceof Task) {
      Task task = (Task) o;
      return task.getVerb().equals("LocationChange");
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
    
  
// ---------------------------------------------------------------------------------------------------------------------
// Public Member Methods
// ---------------------------------------------------------------------------------------------------------------------

	/*********************************************************************************************************************
  <b>Description</b>: Subscribe to "pack the books" tasks and any changes in the inventory.

	*********************************************************************************************************************/
  public void setupSubscriptions()
  {
    allChangeLocationTasks = (IncrementalSubscription)subscribe(allChangeLocationTasksPredicate);
    allOrganizationTasks = (IncrementalSubscription)subscribe(allOrganizationsPredicate);
    
    ActionListener startListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        startButtonListener();
      }
    };

    ActionListener stopListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stopButtonListener();
      }
    };

    JFrame frame = new JFrame("Location Collector Structures");
    frame.getContentPane().setLayout(new FlowLayout());
    JPanel panel = new JPanel();
    // Create the button
    JButton startButton = new JButton("View Organization Locations");
    JButton stopButton = new JButton("View Organization Subordinates");

    // Register a listener for the button
    startButton.addActionListener(startListener);
    stopButton.addActionListener(stopListener);
    panel.add(startButton);
    panel.add(stopButton);
    frame.getContentPane().add("Center", panel);
    frame.pack();
    frame.setVisible(true);
  }


	  private void startButtonListener()
	  {
	      JFrame frame = new JFrame("Locations View");
		    frame.getContentPane().setLayout(new BorderLayout());
		    JPanel panel = new JPanel();
		    // Create the button
		    JButton startButton = new JButton("ok");
		    
		    JTextArea jTextArea1 = new JTextArea(15, 40);
		    for(Enumeration e = organizationLocations.keys(); e.hasMoreElements();)
		    {
		    	String orgName = (String) e.nextElement();
		    	MapLocationInfo mli = (MapLocationInfo) organizationLocations.get(orgName);
		    	Vector elements = mli.getScheduleElements();
		    	for(int i = 0; i < elements.size(); i++)
		    	{
		    		LocationScheduleElementImpl lse = (LocationScheduleElementImpl) elements.elementAt(i);
		    		GeolocLocation  orgLoc = (GeolocLocation) lse.getLocation();
		    		Latitude latitude = orgLoc.getLatitude();
		    		Longitude longitude = orgLoc.getLongitude();
		    		double geoLat = latitude.getDegrees();
		    		double geoLong = longitude.getDegrees();
		    		jTextArea1.insert(ls + orgName + "   " + geoLat + "   " + geoLong,lineindex++);
		    	}
		    }
		    JScrollPane jScrollPane1 = new JScrollPane(jTextArea1);
		    //jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);	    		    
		    //jScrollPane1.getViewport().add(jTextArea1, null);
		    panel.add(jScrollPane1, BorderLayout.CENTER);
		    frame.getContentPane().add("Center", panel);
		    frame.pack();
		    frame.setVisible(true);
	  }

   private void stopButtonListener()
   {
        JFrame frame = new JFrame("Subordinates View");
		    frame.getContentPane().setLayout(new BorderLayout());
		    JPanel panel = new JPanel();
		    // Create the button
		    JButton startButton = new JButton("ok");
		    JTextArea jTextArea1 = new JTextArea(15, 40);
		    for(Enumeration e = organizationLocations.keys(); e.hasMoreElements();)
		    {
		    	String orgName = (String) e.nextElement();
		    	System.out.println("&&&& print org " + orgName);
		    	MapLocationInfo mli = (MapLocationInfo) organizationLocations.get(orgName);
		    	Vector elements = mli.getRelationshipSchedule();
		    	for(int i = 0; i < elements.size(); i++)
		    	{
		    		String subordinate = (String) elements.elementAt(i);
		    		System.out.println("&&&& print subordinate " + subordinate);
		    		jTextArea1.insert(ls + orgName + "   " + subordinate,lineindex++);
		    	}
		    }
		        
		    JScrollPane jScrollPane1 = new JScrollPane(jTextArea1);		    
		    panel.add(jScrollPane1, BorderLayout.CENTER);
		    		    
		    frame.getContentPane().add("Center", panel);
		    frame.pack();
		    frame.setVisible(true);
   }


	/*********************************************************************************************************************
  <b>Description</b>: Called by infrastructure whenever something we are interested in is changed or added.

	*********************************************************************************************************************/
  public void execute()
  {
    System.out.println("&&&& LocationCollector");
    // Go through every new task we've subscribed to
    for(Enumeration changeLocationTask = allChangeLocationTasks.getAddedList(); changeLocationTask.hasMoreElements();)
    {
      Task task = (Task) changeLocationTask.nextElement();
      PrepositionalPhrase pp = task.getPrepositionalPhrase("LOCATIONINFO");
      MapLocationInfo scheduleElements = (MapLocationInfo) pp.getIndirectObject();
      PrepositionalPhrase on = task.getPrepositionalPhrase("ORGNAME");
      String orgName = (String) on.getIndirectObject();
      
      organizationLocations.put(orgName, scheduleElements);
      System.out.println("&&&& locationCollector put add for  " + orgName);
    }
    
    for(Enumeration changeLocationTask = allChangeLocationTasks.getChangedList(); changeLocationTask.hasMoreElements();)
    {
    	Task task = (Task) changeLocationTask.nextElement();
      PrepositionalPhrase pp = task.getPrepositionalPhrase("LOCATIONINFO");
      MapLocationInfo scheduleElements = (MapLocationInfo) pp.getIndirectObject();
      PrepositionalPhrase on = task.getPrepositionalPhrase("ORGNAME");
      String orgName = (String) on.getIndirectObject();
      organizationLocations.put(orgName, scheduleElements);
      System.out.println("&&&& locationCollector put change for  " + orgName);
    }
    
    for(Enumeration publishingOrganization = allOrganizationTasks.getAddedList(); publishingOrganization.hasMoreElements();)
    {
    	Organization org = (Organization) publishingOrganization.nextElement();
      //  take the "homelocation" code out of changefororg and put it here
      
      String longName = org.getUID().toString();
	    int index = longName.lastIndexOf('/');
	    String orgName = longName.substring(0, index);
	    
	    System.out.println("&&&& locationCollector put homeloc for  " + longName);
      if(!organizationLocations.containsKey(orgName))
      {
	      Vector scheduleElements = new Vector(1);
	      MilitaryOrgPG mpg = org.getMilitaryOrgPG();
	      GeolocLocation homeLoc = null;
	      String symbol = null;
	      if(mpg != null)
	      {
		      String newSymbol = mpg.getHierarchy2525();
		      homeLoc = (GeolocLocation)mpg.getHomeLocation();
		      
		      if(newSymbol != null)
		        symbol = removeDots(newSymbol);
	      }
	      else
	        break;                   // no information to store
	      long l1 = 0;               // set to forever
        long l2 = Long.MAX_VALUE;
	      NewLocationScheduleElement home = new LocationScheduleElementImpl(l1, l2, homeLoc);
	      //System.out.println("homeloc " + home);
	      scheduleElements.add(home);
	      MapLocationInfo ml = new MapLocationInfo(scheduleElements, symbol);
	      organizationLocations.put(orgName, ml);
      }
	    
    }

  }
  
  /*********************************************************************************************************************
  <b>Description</b>: Parses "1.x.3.1.1" type string to remove everything before x, remove
                      the dots and add "F".
	*********************************************************************************************************************/
	
	public String removeDots(String sym)
	{
		//  build the string from everthing beyond the x minus the dots + "F"
		String newSym = "";
		int xIndex = sym.indexOf('x');
		for(int i = xIndex + 1; i < sym.length(); i++)
		{
			if(sym.charAt(i) != '.')
			  newSym += sym.charAt(i);
		}
		newSym += "F";
		return newSym;
	}
  /*********************************************************************************************************************
  <b>Description</b>: Looks at the PlugIn parameters for the packerTime value.
	*********************************************************************************************************************/
  private void parseParameters()
  {
  	// Look through the PlugIn parameters for the packer time
    Vector pVec = getParameters();
    if (pVec.size() > 0)
    {
     
    }
  }
}
