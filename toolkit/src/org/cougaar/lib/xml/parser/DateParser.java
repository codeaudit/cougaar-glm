/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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
  public static Date getDate(Node node){
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
