/*
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

import java.io.InputStream;
import java.io.Serializable;
import java.util.Vector;

public class MapLocationInfo implements Serializable
{

 static final long serialVersionUID = 511995200124741430L; 
  String symbol2525 = null;
  int echelon = 0;
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
  
  public void setEchelon(int ech)
  {
  	echelon = ech;
  }
  
  public int getEchelon()
  {
  	return echelon;
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
