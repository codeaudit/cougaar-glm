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
package org.cougaar.domain.glm.map;

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.plan.LocationScheduleElement;

import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.asset.AssetGroup;
import org.cougaar.domain.planning.ldm.asset.NewItemIdentificationPG;

import org.cougaar.domain.glm.ldm.asset.LocationSchedulePG;
import org.cougaar.domain.glm.ldm.asset.MilitaryOrgPG;
//import org.cougaar.domain.glm.plugins.TaskUtils;


import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;

import org.cougaar.domain.planning.ldm.trigger.*;
import org.cougaar.core.plugin.PlugInDelegate;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.lib.util.UTILAllocate;

import org.cougaar.domain.glm.map.MapLocationInfo;

import org.cougaar.core.cluster.ClusterIdentifier;

import java.util.Enumeration;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Iterator;

//temportary
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
import java.io.Serializable;



import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;


/***********************************************************************************************************************
<b>Description</b>: Allocate organization tasks to LocationInfo".

@author Frank Cooley, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/

public class ChangeForOrganizationPlugIn extends org.cougaar.core.plugin.SimplePlugIn
{
	private Hashtable oldLocations = new Hashtable(1);
	private Organization LocationInfoOrg = null;
	private Organization myOrganization = null;
	private boolean mapInfoExists = false;
	private boolean debug = false;
	MapLocationInfo mli = null;
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
    
    private IncrementalSubscription mapInfoOrganizationTasks;
  private static UnaryPredicate mapInfoOrganizationsPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Organization )
      {
      	ClusterIdentifier c = ((Organization) o).getClusterIdentifier();
      	//System.out.println("&&&clusterid = " + c);
      	if(c.toString().startsWith("LocationInfo"))
      	  return true;
      	else
      	  return false;
        
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
    allSelfOrganizationTasks = (IncrementalSubscription)subscribe(allSelfOrganizationsPredicate);
    mapInfoOrganizationTasks = (IncrementalSubscription)subscribe(mapInfoOrganizationsPredicate);
    parseParameters();
  }


	/*********************************************************************************************************************
  <b>Description</b>: Called by infrastructure whenever something we are interested in is changed or added.

	*********************************************************************************************************************/
  public void execute()
  {

     int locNumber = -1;
     int id = -1;
     String idNum = null;

     Organization lio = null;

     //for(Enumeration mapInfoOrganization = mapInfoOrganizationTasks.getAddedList(); mapInfoOrganization.hasMoreElements();)
     for (Iterator orgIter = mapInfoOrganizationTasks.getCollection().iterator(); orgIter.hasNext();)
     {
     	
     	if(debug)
    	  System.out.println("&&& ChangeForOrganization - mapinfo org received");
      //LocationInfoOrg = (Organization) mapInfoOrganization.nextElement();
//      LocationInfoOrg = (Organization) orgIter.next();
      lio = (Organization) orgIter.next();
    	ClusterIdentifier c = lio.getClusterIdentifier();
    	idNum = c.toString().substring("LocationInfo".length());
//System.out.println("&&&clusterid = " + c);
      
    	
    	if ((LocationInfoOrg == null) && (idNum.length() == 0))
    	{
    	  LocationInfoOrg = lio;
//System.out.println("&&&No Loc Number");
      }
      else if (idNum.length() != 0)
      {
        id = Integer.parseInt(idNum);
        
        if (id > locNumber)
        {
          locNumber = id;
      	  LocationInfoOrg = lio;
//System.out.println("&&&Loc Number: " + locNumber);
        }
      }
    

      mapInfoExists = true;
     }
      if(myOrganization != null && LocationInfoOrg != null)
      {
      	Task task = createLocationTask(myOrganization);
	      if(task != null && mapInfoExists)
	      {
		      publishAdd(task);
		      Allocation allocation = theLDMF.createAllocation(task.getPlan(), task, LocationInfoOrg, null, Role.AVAILABLE);
          publishAdd(allocation);
          if(debug)
		        System.out.println("&&&& publishing location task in response to mapinfo init");
		    }
      }
     
    
    // Go through every new task we've subscribed to
    for(Enumeration selfOrganization = allSelfOrganizationTasks.getAddedList(); selfOrganization.hasMoreElements();)
    {
    	if(debug)
    	  System.out.println("&&& ChangeForOrganization - add");
      Organization org = (Organization) selfOrganization.nextElement();
      myOrganization   = org;
      //  get the locationSchedulePG
      Task task = createLocationTask(org);
      if(task != null && mapInfoExists)
      {
	      publishAdd(task);
	      Allocation allocation = theLDMF.createAllocation(task.getPlan(), task, LocationInfoOrg, null, Role.AVAILABLE);
        publishAdd(allocation);
        if(debug)
	        System.out.println("&&&& changefororg publish added locationchange task");
	    }
    }
    
    for(Enumeration selfOrganization = allSelfOrganizationTasks.getChangedList(); selfOrganization.hasMoreElements();)
    {
    	if(debug)
    	  System.out.println("&&& ChangeForOrganization - change");
    	Organization org = (Organization) selfOrganization.nextElement();
      //  get the locationSchedulePG
      Task task = createLocationTask(org);
      if(task != null && mapInfoExists)
      {
      	//System.out.println("&&&& changefororg publish changed locationchange task");
	      publishAdd(task);
	      Allocation allocation = theLDMF.createAllocation(task.getPlan(), task, LocationInfoOrg, null, Role.AVAILABLE);
        publishAdd(allocation);
	      
	    }
    }
    
     
    
  }
  
  /*********************************************************************************************************************
  <b>Description</b>: Creates the "LocationChange task.
	*********************************************************************************************************************/
	public Task createLocationTask(Organization org)
	{
		  LocationSchedulePG lspg = org.getLocationSchedulePG();
		  RelationshipSchedule relationshipSched = org.getRelationshipSchedule();
		  
		  //System.out.println("rel sched = " + relationshipSched);
		    
		  Role role = Role.getRole("AdministrativeSubordinate"); 
		  Collection orgCollection = relationshipSched.getMatchingRelationships(role);
		  
		  /*Vector relations = new Vector(1);
		  for (Iterator relIter = orgCollection.iterator(); relIter.hasNext();)
      {
        Relationship rel = (Relationship) relIter.next();
        String a = rel.getA().toString();
        int beginString = a.lastIndexOf('/');
        String subordinate = a.substring(beginString + 1, a.length() - 1);
        //System.out.println("relationship " + subordinate);
        relations.add(subordinate);
      }*/
		  //System.out.println("&&&& Collection of role relations " + orgCollection);
		  //System.out.println("locationschedule " + lspg);
      //  create a "changelocation" task with a prepositional phrase of "New Location" and
      //  an indirect object of type MapLOcationInfo
      //  then publish add or publish change the the task to the location collector (MapInfo cluster)
      //  whose role is specified in a orgallocations.xml file as "LocationCollector" 
     Vector scheduleElements = new Vector(1);
      MilitaryOrgPG mpg = org.getMilitaryOrgPG();
      GeolocLocation homeLoc = null;
      int echelonNumber = 0;
      String symbol = null;
      if(mpg != null)
      {
	      String newSymbol = mpg.getHierarchy2525();
	      homeLoc = (GeolocLocation)mpg.getHomeLocation();
	      
	      if(newSymbol != null)
	        symbol = removeDots(newSymbol);
	        
	      String echelon = mpg.getEchelon();
	      if(echelon != null)
	      {
	      	//System.out.println("echelon string is " + echelon);
	      	try
	      	{
	          echelonNumber = Integer.parseInt(echelon);
	        }
	        catch(Exception e)
	        {
	        	//System.out.println("echelon is not a number");
	        }
	      }      
      }
     
      long l1 = 0;
      long l2 = Long.MAX_VALUE;
     if(lspg != null)
     { 
      Schedule schedule = lspg.getSchedule();
      //System.out.println("schedule " + schedule);
      Enumeration e = schedule.getAllScheduleElements();
      
     
      while(e.hasMoreElements())
      {
      	LocationScheduleElement lse = (LocationScheduleElement)e.nextElement();
      	//System.out.println("locationschedule " + schedule);
      	scheduleElements.add(lse);
      	
      }
      if(compareElements(scheduleElements, org))
      {
      	//System.out.println("&&&& location schedule elements are equal");
        return null;  //  it hasn't changed so don't publish it
      }            
      //  get the HomeLocation GeolocLocation stuff
      
      if(scheduleElements.size() > 0)
      {
      	//LocationScheduleElement l = (LocationScheduleElement)scheduleElements.elementAt(0);
      	LocationScheduleElement l = findLowestStart(scheduleElements);
      	if(l != null)
      	{
	      	long start = l.getStartTime();
	      	//long end = l.getEndTime();
	      	l1 = 0;
	      	l2 = start;
      	}
      	
      }
     } 
     
     Vector relations = new Vector(1);
		  for (Iterator relIter = orgCollection.iterator(); relIter.hasNext();)
      {
        Relationship rel = (Relationship) relIter.next();
        String a = rel.getA().toString();
        int beginString = a.lastIndexOf('/');
        String subordinate = a.substring(beginString + 1, a.length() - 1);
        //System.out.println("relationship " + subordinate);
        relations.add(subordinate);
      }
      
      NewLocationScheduleElement home = new LocationScheduleElementImpl(l1, l2, homeLoc);
      //System.out.println("homeloc " + home);
      scheduleElements.add(home);
     if(scheduleElements.size() > 0)
     {
      MapLocationInfo ml = new MapLocationInfo(scheduleElements, symbol);
	    NewTask task = theLDMF.newTask();
	    task.setDirectObject(null);
	    task.setVerb(new Verb("LocationChange")); 
	     
	    Vector preps = new Vector();
	    
	    NewPrepositionalPhrase orgNamePP = theLDMF.newPrepositionalPhrase();
	    String longName = org.getUID().toString();
	    int index = longName.lastIndexOf('/');
	    String orgName = longName.substring(0, index);
	    if(debug)
	      System.out.println("creating location change task for  " + orgName);
	    orgNamePP.setPreposition("ORGNAME");
	    orgNamePP.setIndirectObject(orgName);
	    preps.add(orgNamePP);
	    
	    ml.setUID(orgName);
	    ml.setRelationshipSchedule(relations);
	    ml.setEchelon(echelonNumber);
	    NewPrepositionalPhrase npp = theLDMF.newPrepositionalPhrase();
	    npp.setPreposition("LOCATIONINFO");
	    npp.setIndirectObject(ml);
	    preps.add(npp);
	    	    
	    task.setPrepositionalPhrases(preps.elements());
	    
	    return task;
	   }
	   else
	    return null;
	}
	
	/*********************************************************************************************************************
  <b>Description</b>: Find the LocationScheduleElement with earliest start time.
	*********************************************************************************************************************/
  public LocationScheduleElement findLowestStart(Vector scheduleElements)
  {
  	LocationScheduleElement thisLSE = null;
  	//long maxTime = 2147483647;
  	long maxTime = Long.MAX_VALUE;
  	//System.out.println("max long " + maxTime + " " + new Date(maxTime));
  	for(int i = 0; i < scheduleElements.size(); i++)
  	{
  		LocationScheduleElement l = (LocationScheduleElement)scheduleElements.elementAt(i);
  		long start = l.getStartTime();
  		//System.out.println("member start " + new Date(start));
      long end = l.getEndTime();
      if(start < maxTime)
      {
      	thisLSE = l;
      	maxTime = start;
      }
      
  	}
  	return thisLSE;
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

		if (xIndex <0 ) 
		{
			xIndex = sym.indexOf('X');
		}

		for(int i = xIndex + 1; i < sym.length(); i++)
		{
			if(sym.charAt(i) != '.')
			  newSym += sym.charAt(i);
		}
		newSym += "F";
		return newSym;
	}
	
	/*********************************************************************************************************************
  <b>Description</b>: compare locationschedule elements

	*********************************************************************************************************************/
	public boolean compareElements(Vector newLocationsVector, Organization org)
	{
		String orgName = org.getUID().toString();
		Vector oldLocationsVector = null;
		if(oldLocations.containsKey(orgName))
		{
			oldLocationsVector = (Vector) oldLocations.get(orgName);
			//System.out.println("&&& compare oldvector size" + oldLocationsVector.size() + " to newVector size " + newLocationsVector.size());
			if(oldLocationsVector.size() != newLocationsVector.size())
			{
				oldLocations.put(orgName, newLocationsVector);
			  return false;
			}
			else
			{
				for(int i = 0; i < oldLocationsVector.size(); i++)
				{
					LocationScheduleElement oldLse = (LocationScheduleElement) oldLocationsVector.elementAt(i);
					LocationScheduleElement newLse = (LocationScheduleElement) newLocationsVector.elementAt(i);
					if(oldLse != newLse)
					  return false;
				}
				return true;
			}
			
			
		}
		else  //  add this org to hashtable
		{
			oldLocations.put(orgName, newLocationsVector);
			return false;
		}
	}
	

  private void parseParameters()
  {
      Vector pVec = getParameters();
    if (pVec.size() > 0)
    {
      debug = true;
    }
  }
	
	
}
