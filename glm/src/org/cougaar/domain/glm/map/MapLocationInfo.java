package org.cougaar.domain.glm.map;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.planning.ldm.plan.LocationScheduleElement;

import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;

import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.net.URLConnection;
import java.net.URL;
import java.io.Serializable;

import java.util.Vector;
import java.util.Hashtable;

public class MapLocationInfo implements Serializable
{

  
  String symbol2525 = null;
  Vector scheduleElements = null;
  String UID = null;
  Vector relationshipSched = null;

  /*********************************************************************************************************************
  <b>Description</b>: The data input stream opened when the data stream KeepAlive PSP is contacted.

  <br><br><b>Notes</b>:<br>
										- 
	*********************************************************************************************************************/
  private InputStream is = null; 
  public MapLocationInfo()
  {
  }
  
  public MapLocationInfo(Vector elements, String symbol)
  {
     
     symbol2525 = symbol;
     scheduleElements = elements;
  }
  
  public String getSymbol()
  {
  	return symbol2525;
  }
  
  public Vector getScheduleElements()
  {
  	return scheduleElements;
  }
  
  public void setRelationshipSchedule(Vector r)
  {
  	relationshipSched = r;
  }
  
  public Vector getRelationshipSchedule()
  {
  	return relationshipSched;
  }
  
  public void setUID(String s)
  {
  	UID = s;
  }
  
  public String getUID()
  {
  	return UID;
  }

}
