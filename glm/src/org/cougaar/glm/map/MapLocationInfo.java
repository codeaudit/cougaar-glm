/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
