// 
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

package org.cougaar.glm.xml.parser;

import java.util.Date;
import java.util.Vector;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.plugins.MaintainedItem;
import org.cougaar.lib.xml.parser.DateParser;
import org.cougaar.lib.xml.parser.DirectObjectParser;
import org.cougaar.lib.xml.parser.PreferencesParser;
import org.cougaar.lib.xml.parser.VerbParser;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Copyright (c) 1999-2002 BBN Technologies 
 */
public class TaskParser{
  private static String PREPO="PREPO";

  public TaskParser (Logger log) { 
    logger = log; 
    verbParser = new VerbParser ();
    dateParser = new DateParser ();
    preferencesParser = new PreferencesParser (log);
    directObjectParser = new DirectObjectParser (log);
    locationParser = new LocationParser ();
    itineraryParser = new ItineraryParser ();

    if (logger.isInfoEnabled ())
      logger.info ("TaskParser created.");
  }

  public Task getTask(LDMServesPlugin ldm,
		      MessageAddress clusterIdentifier, 
		      PlanningFactory ldmf, 
		      Node node){
    NewTask task = null;
    
    if(node.getNodeName().equals("task")){
      NodeList  nlist    = node.getChildNodes();      
      int       nlength  = nlist.getLength();
      
      task = ldmf.newTask();
      task.setPlan(ldmf.getRealityPlan());
      task.setSource(clusterIdentifier);
      task.setDestination(clusterIdentifier);

      Vector prep_phrases = new Vector();
      for(int i = 0; i < nlength; i++){
	Node    child       = nlist.item(i);
	String  childname   = child.getNodeName();

	if(child.getNodeType() == Node.TEXT_NODE) {
	  continue;
	}
        if(child.getNodeType() == Node.ELEMENT_NODE){

	  if(childname.equals("verb")){
	    Verb verb = verbParser.getVerb(child);
	    task.setVerb(verb);
	    if (logger.isDebugEnabled()) logger.debug ("TaskParser - on task " + task.getUID () + " set verb to " + verb);
	  }
	  else if(childname.equals("directobject")){
	    task.setDirectObject(directObjectParser.getDirectObject(ldm, child));
	    if (logger.isDebugEnabled()) logger.debug ("TaskParser - on task " + task.getUID () + " set d.o. to " + task.getDirectObject());
	  }
	  else if(childname.equals("from")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.FROM);
	    newpp.setIndirectObject(locationParser.getLocation(ldm, child));
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("to")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.TO);
	    newpp.setIndirectObject(locationParser.getLocation(ldm, child));
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("with")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.WITH);
	    newpp.setIndirectObject(getStuff(ldm, child));
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("using")){
	    // same as with
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.USING);
	    newpp.setIndirectObject(getStuff(ldm, child));
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("maintaining")){
	    // same as with
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.MAINTAINING);
	    newpp.setIndirectObject(getMaintainingObject(ldm, child));
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("for")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.FOR);
	    String forunit = getTagContents(child);
	    newpp.setIndirectObject(forunit);
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("oftype")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.OFTYPE);
	    newpp.setIndirectObject(getStuff(ldm,child));
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.toUpperCase().equals(PREPO)){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(PREPO);
	    newpp.setIndirectObject(PREPO);
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("readyat")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.READYAT);
	    Date readyat = dateParser.getDate(child);
	    Schedule sched = ldmf.newSimpleSchedule(readyat, readyat);
	    newpp.setIndirectObject(sched);
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("startdate")){
	    Preference p = preferencesParser.getStartDate(ldmf, child);
	    task.addPreference(p);
	  }
	  else if(childname.equals("enddate")){
	    Preference p = preferencesParser.getEndDate(ldmf, child);
	    task.addPreference(p);
	  }
	  else if (childname.equals("cost")){
	    Preference p = preferencesParser.getCost(ldmf, child);
	    task.addPreference(p);
	  }
	  else if (childname.equals("quantity")){
	    Preference p = preferencesParser.getQuantity(ldmf, child);
	    task.addPreference(p);
	  }
	  else if(childname.equals("ItineraryOf")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.ITINERARYOF);
	    newpp.setIndirectObject(itineraryParser.getItinerary(ldm, child));
	    //task.setPrepositionalPhrase(newpp);
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("refill")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.REFILL);
	    prep_phrases.addElement(newpp);
	  }
	  else if (childname.equals("RespondTo")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.RESPONDTO);
	    String AEForg =  child.getFirstChild().getNodeValue();
	    newpp.setIndirectObject(AEForg);
	    prep_phrases.addElement(newpp);
	    
// LMC: 11/22/99 AEF now sets this as a String.......
	    //	    newpp.setPreposition(AirforceGrammar.RESPONDTO);
// 	    Asset AFOrg = new AbstractAsset();
// 	    try {
// 	      NewTypeIdentificationPG typeId = (NewTypeIdentificationPG)AFOrg.getTypeIdentificationPG();
// 	      typeId.setTypeIdentification(child.getFirstChild().getNodeValue());
// 	      typeId.setNomenclature(child.getFirstChild().getNodeValue());
// 	    } catch (Exception exc) {
// 	      System.err.println("problem creating the AbstractAsset for RespondTo");
//	    }
// 	    	    newpp.setIndirectObject(AFOrg);
// 	    //task.setPrepositionalPhrase(newpp);
// 	    prep_phrases.addElement(newpp);
	  }
	  else if (childname.equals("FromTask")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.FROMTASK);
	    Asset AEF = ldmf.createInstance (task.getDirectObject ());
	    try {
	      NewTypeIdentificationPG typeId = (NewTypeIdentificationPG)AEF.getTypeIdentificationPG();
	      typeId.setTypeIdentification(child.getFirstChild().getNodeValue());
	      typeId.setNomenclature(child.getFirstChild().getNodeValue());
	    } catch (Exception exc) {
	      logger.error("problem creating the AbstractAsset for FromTask", exc);
	    }
	    newpp.setIndirectObject(AEF);
	    prep_phrases.addElement(newpp);
	  }
	  else {
	    if (logger.isDebugEnabled()) 
	      logger.debug ("TaskParser - found non-specific prep : creating marker prep for tag " + childname);
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(childname);
	    newpp.setIndirectObject(childname);
	    prep_phrases.addElement(newpp);
	  }
	}
	task.setPrepositionalPhrases(prep_phrases.elements());
      }
    }
    return task;
  }

  private Object getStuff(LDMServesPlugin ldm, Node node){
    Object object = null;
    
    NodeList  nlist    = node.getChildNodes();      
    int       nlength  = nlist.getLength();

    for(int i = 0; i < nlength; i++){
      Node    child       = nlist.item(i);
      String  childname   = child.getNodeName();

      if(child.getNodeType() == Node.ELEMENT_NODE){
	if(childname.equals("asset")){
	  // object = AssetParser.getAsset(ldm, child);
	  object = directObjectParser.getDirectObject(ldm, node);
	  break;
	}
      } else if (child.getNodeType() == Node.TEXT_NODE) {
	String data = child.getNodeValue().trim();
	if(data.length() != 0){
	  object = data;
	  break;
	}
      }
    }

    return object;
  }

  private static Object getMaintainingObject(LDMServesPlugin ldm, Node node){
    String type = null, typeID = null, itemID = "maintainedItem", nomen = null;

    NodeList  nlist    = node.getChildNodes();      
    int       nlength  = nlist.getLength();

    for(int i = 0; i < nlength; i++){
      Node    child       = nlist.item(i);
      String  childname   = child.getNodeName();
      if(child.getNodeType() == Node.ELEMENT_NODE){
	if(childname.equals("type"))
	  type = getTagContents (child);
	else if (childname.equals("typeID"))
	  typeID = getTagContents (child);
	else if (childname.equals("nomen"))
	  nomen = getTagContents (child);
      }
    }

    return new MaintainedItem (type, typeID, itemID, nomen);
  }

  private static String getTagContents(Node node){
    Asset asset = null;
    Node data = node.getFirstChild();
    return data.getNodeValue();
  }

  private Logger logger;
  VerbParser verbParser;
  DateParser dateParser;
  PreferencesParser preferencesParser;
  DirectObjectParser directObjectParser;
  LocationParser locationParser;
  ItineraryParser itineraryParser;
}
