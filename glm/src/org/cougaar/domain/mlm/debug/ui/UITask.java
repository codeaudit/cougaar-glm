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

package org.cougaar.domain.mlm.debug.ui;

import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;

import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.core.society.Message;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.asset.Asset;

public class UITask {

  /** Workaround for task source and destination
    that can be strings or ClusterIdentifiers.
    */

  private static String getClusterIdDescription(Object o) {
    String s = "";
    if (o == null)
      return s;
    if (o instanceof ClusterIdentifier) 
      return ((ClusterIdentifier)o).getAddress();
    if (o instanceof String) {
      System.out.println("Warning: task source or destination is a string: " + o);
      return (String)o;
    } else {
      System.out.println("Warning: task source or destination is a " + o.getClass().toString() + " with value " + o.toString());
      return o.toString();
    }
  }

  /** Return representation of a task in a tree.
    For physical objects, return the name and serial number (if non-empty)
    and for cluster objects, return the cluster id.
    @param task the task for which to return a description
    @return the string Source -> Destination Verb Objects Role
    should return Source -> Destination 
    Verb DirectOjbect PrepPhrase DesiredSchedule
   */

  public static String getDescription(Task task) {
    String sourceString = getClusterIdDescription(task.getSource());
    String dstnString = getClusterIdDescription(task.getDestination());
    Verb verb = task.getVerb();
    String verbString = "";
    if (verb != null)
      verbString = verb.toString();
    String directObjectString = UIAsset.getDescription((Asset)task.getDirectObject());

    Enumeration prepPhrases = task.getPrepositionalPhrases();
    String prepPhraseString = "";
    PrepositionalPhrase p;
    if (prepPhrases != null) {}
    while (prepPhrases.hasMoreElements()) {
      p = (PrepositionalPhrase)prepPhrases.nextElement();
      if (p != null)
	if ( p.getIndirectObject() instanceof Asset ) {
	  prepPhraseString = prepPhraseString + " " + 	
	    p.getPreposition() + " " +
	    UIAsset.getDescription((Asset)p.getIndirectObject());
          /*
	} else if ( p.getIndirectObject() instanceof Requisition ) {
	  prepPhraseString = prepPhraseString + " " + 	
	    p.getPreposition() + " " +
	    UIAsset.getDescription((Requisition)p.getIndirectObject());
	} else if ( p.getIndirectObject() instanceof SupplySource ) {
	  prepPhraseString = prepPhraseString + " " + 	
	    p.getPreposition() + " " +
	    UIAsset.getDescription((SupplySource)p.getIndirectObject());
          */
	}
    }
    
  prepPhraseString.trim();

  String scheduleString = "";
  //PenaltyFunction pf = task.getPenaltyFunction();
  //if (pf != null) {
  //  Date date = pf.getDesiredScheduleBestDate();
  //  if (date != null)
  //    scheduleString = getDateString(date);
  //}

  String description = "";
  //    if ((sourceId != null) || (dstnId != null))
  if ((sourceString.length() != 0) || (dstnString.length() != 0))
       description = sourceString + "->" + dstnString + " ";
       description = description +  " " + verbString + " " + 
  directObjectString + " " + prepPhraseString + " " + scheduleString;
  return description;
}

  public static String getDateString(Date date) {
    DateFormat dateFormat = 
      DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.format(date);
  }

}
