/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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

package org.cougaar.lib.xml.parser;

import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses dates that are a part of schedule tags or preferences.
 */
public class DateParser{
  /**
   * Includes fix for jdk1.2.1 "bug" (months are 0 based)
   *
   * Calendar.set sets the date to be month+1 i.e. 0 = Jan
   *
   * @author Gordon Vidaver 7/2/99
   */
  public Date getDate(Node node){
    Calendar  cal       = Calendar.getInstance();
    NodeList  nlist     = node.getChildNodes();      
    int       nlength   = nlist.getLength();
    int       year      = -1;
    int       month     = -1;
    int       day       = -1;
    int       hour      = -1;
    int       minute    = -1;
    int       second    = -1;

    for(int i = 0; i < nlength; i++){
      Node    child      = nlist.item(i);
      String  childname  = child.getNodeName();

      if(child.getNodeType() == Node.ELEMENT_NODE){
	if(childname.equals("year")){
	  Node data = child.getFirstChild();
	  Integer j = new Integer(data.getNodeValue());
	  year = j.intValue();
	}
	else if(childname.equals("month")){
	  Node data = child.getFirstChild();
	  Integer j = new Integer(data.getNodeValue());
	  month = j.intValue();
	}
	else if(childname.equals("day")){
	  Node data = child.getFirstChild();
	  Integer j = new Integer(data.getNodeValue());
	  day = j.intValue();
	}
	else if(childname.equals("hour")){
	  Node data = child.getFirstChild();
	  Integer j = new Integer(data.getNodeValue());
	  hour = j.intValue();
	}
	else if(childname.equals("minute")){
	  Node data = child.getFirstChild();
	  Integer j = new Integer(data.getNodeValue());
	  minute = j.intValue();
	}
	else if(childname.equals("second")){
	  Node data = child.getFirstChild();
	  Integer j = new Integer(data.getNodeValue());
	  second = j.intValue();
	}
      }
    }

    // reset all the fields, including the MILLISECONDS field in 
    // the calendar
    cal.clear();

    cal.set(year, month-1, day, hour, minute, second);
    // Note jdk1.2 "bug"^^

    return cal.getTime();
  }
}
