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

package org.cougaar.mlm.debug.ui;

import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;

public class UITask {

  /** Workaround for task source and destination
    that can be strings or MessageAddresss.
    */

  private static String getClusterIdDescription(Object o) {
    String s = "";
    if (o == null)
      return s;
    if (o instanceof MessageAddress) 
      return ((MessageAddress)o).getAddress();
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
