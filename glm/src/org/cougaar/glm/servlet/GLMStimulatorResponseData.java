/*
 * <copyright>
 *  Copyright 1997-2002 BBNT Solutions, LLC
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

package org.cougaar.glm.servlet;

import org.cougaar.planning.servlet.data.xml.XMLable;
import org.cougaar.planning.servlet.data.xml.XMLWriter;
import java.io.*;
import java.util.*;

/** 
 * <pre>
 * Represents the returned data from a stimulator request 
 *
 * Sample output is :
 *
 * <results totalTime="0:06:069">
 *  <task id="1" time="0:00:090"/>
 *  <task id="2" time="0:02:994"/>
 *  <task id="3" time="0:02:985"/>
 * </results>
 *
 * </pre>
 */
public class GLMStimulatorResponseData implements XMLable, Serializable {
  public String totalTime;
  public Map taskTimes = new HashMap();

  public void addTaskAndTime (String uid, String time) {
    taskTimes.put (uid, time);
  }

  /**
   * Write this class out to the Writer in XML format
   * @param w output Writer
   **/
  public void toXML(XMLWriter w) throws IOException{
    w.optagln("results", "totalTime", totalTime);
    for (Iterator iter = new TreeSet(taskTimes.keySet()).iterator(); iter.hasNext();) {
      Object key = iter.next ();
      w.sitagln("task", 
		"id",   (String) key, 
		"time", (String) taskTimes.get(key));
    }
    w.cltagln("results");
  }

  public String toString () {
    StringBuffer buf = new StringBuffer ();
    buf.append ("\n************************* Testing Summary *************************\n");
    buf.append ("         Task UID             Time \n");
    for (Iterator iter = taskTimes.keySet().iterator(); iter.hasNext();) {
      Object key = iter.next ();
      buf.append("          " + key + "                       " + (String) taskTimes.get(key) + "\n");
    }
    buf.append("\n         Total Time: " + totalTime);
    buf.append("\n*******************************************************************\n");

    return buf.toString();
  }
}
