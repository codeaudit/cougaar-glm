/*
 * <copyright>
 *  Copyright 1997-2003 Clark Software Engineering (CSE)
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

public class IPStatistics implements Serializable
{
	String org = null;
  String iPAddress = "not ready";
  int iPRcvPackets = 5;
  int iPXmitPackets = 5;
  int iPLostPackets = 1;
  double geoLat = 0;
  double geoLong = 0;
  
  public IPStatistics()
  {
  	
  }
  
  public IPStatistics(String ip, int rcv, int xmit, int lost, double lat, double lon)
  {
  	iPAddress = ip;
  	iPRcvPackets = rcv;
  	iPXmitPackets = xmit;
  	iPLostPackets = lost;
  	geoLat = lat;
  	geoLong = lon;
  }
  
  public void applyIncrement(int rcv, int xmit, int lost)
  {
  	iPRcvPackets += rcv;
    iPXmitPackets += xmit;
    iPLostPackets += lost;
  }
  
  public void setNodeInfo(String ip, double lat, double lon)
  {
  	iPAddress = ip;
  	geoLat = lat;
  	geoLong = lon;
  }
  public String toString()
  {
  	return iPAddress + ":" + iPRcvPackets + ":" + iPXmitPackets + ":" + iPLostPackets + ":" + geoLat + ":" + geoLong;
  }

}