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