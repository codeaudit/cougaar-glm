/*
 * <copyright>
 *  Copyright 2003 BBNT Solutions, LLC
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

import java.io.Serializable;

import org.cougaar.core.agent.MetricsSnapshot;

public class HbMetricsSnapshot implements Serializable
{

 private MetricsSnapshot ms = null;
 private long hbTimeout = 45000L;
 String[] metricValues;
 String IPAddress = null;
 String IPXmitPackets = null;
 String IPRcvPackets = null;
 String IPPacketsLost = null;
 int[] defaultRelevantItems = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
 int[] relevantItems;
 //int[] relevantItems;

  /*********************************************************************************************************************
  <b>Description</b>: 

  <br><br><b>Notes</b>:<br>
										- 
	*********************************************************************************************************************/
  
  public HbMetricsSnapshot()
  {
  }
  
  public HbMetricsSnapshot(MetricsSnapshot m, long timeout)
  {
     ms = m;
     hbTimeout = timeout;
     
  }
  
  public long getTimeout()
  {
  	return hbTimeout;
  }
  
  public MetricsSnapshot getMetricsSnapshot()
  {
  	return ms;
  }
  
  public String describe()
  {
  	String text = ms.describe();
  	takeApartText(text);
  	
  	//System.out.println("%%%% describe " + text);
  	//int memoryIndex = text.indexOf("freeMemory");
  	//String subText = text.substring(0, memoryIndex);
  	//return subText;
  	return text;
  }
  
  public void takeApartText(String text)
  {
  	int startIndex;
  	int endIndex;
  	  	
  	String[] itemStrings = {"directivesIn",
  	                        "directivesOut",
  	                        "notificationsIn",
  	                        "notificationsOut",
  	                        "assets",
  	                        "planelements",
  	                        "tasks",
  	                        "workflows",
  	                        "pluginCount",
  	                        "thinPluginCount",
													  "prototypeProviderCount",
													  "propertyProviderCount",
													  "cachedPrototypeCount",
													  "idleTime",
													  "freeMemory",
													  "totalMemory",
													  "threadCount"};
		metricValues = new String[itemStrings.length];
	  for(int i = 0; i < itemStrings.length; i++)
	  {
	  	startIndex = text.indexOf(itemStrings[i]);
	  	if(i == (itemStrings.length - 1))
	  	  endIndex = -1;
	  	else
	  	  endIndex = text.indexOf(itemStrings[i + 1]);
	  	if(endIndex == -1)
	  	  metricValues[i] = text.substring(startIndex);
	  	else
	  	  metricValues[i] = text.substring(startIndex, endIndex);
	  	
	  }
	  
	}
  
  
   public String[] getMetricValues()
   {
	  	return metricValues;
   }
	  
	  
	  //  compare new metrics to old metrics
	  
	  public boolean compareValues(String[] newValues, String[] oldValues)
	  {
	  	for(int i = 0; i < newValues.length; i++)
	  	{
	  		if(isInRelevantItems(i))
	  		{
	  		
		  		if(!newValues[i].equals(oldValues[i]))
		  		{
		  			 return true;
		  		}
		  	}
	  	}
	  	return false;  // returns false if values are equal
	  }
  
  //  asks if index is in array of values that we are interested in
  
  public boolean isInRelevantItems(int index)
  {
  	for(int i = 0; i < relevantItems.length; i++)
  	{
  		if(index == relevantItems[i])
  		  return true;
  	}
  	return false;
  }
  
  public void setRelevantItems(int[] rArray)
  {
  	relevantItems = rArray;
  }

}
