// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/xml/parser/Attic/TaskParser.java,v 1.2 2000-12-20 18:18:39 mthome Exp $
/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.xml.parser;

import java.util.Date;
import java.util.Vector;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.domain.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AbstractAsset;

import org.cougaar.domain.glm.ldm.Constants;

import org.cougaar.lib.xml.parser.AssetParser;
import org.cougaar.lib.xml.parser.DateParser;
import org.cougaar.lib.xml.parser.DirectObjectParser;
import org.cougaar.lib.xml.parser.PreferencesParser;
import org.cougaar.lib.xml.parser.VerbParser;

/**
 * Copyright (c) 1999 BBN Technologies 
 */
public class TaskParser{

  public static Task getTask(LDMServesPlugIn ldm,
			     ClusterServesPlugIn cluster, 
			     RootFactory ldmf, 
			     Node node){
    NewTask task = null;
    
    if(node.getNodeName().equals("task")){
      NodeList  nlist    = node.getChildNodes();      
      int       nlength  = nlist.getLength();
      
      task = ldmf.newTask();
      task.setPlan(ldmf.getRealityPlan());
      task.setSource(cluster.getClusterIdentifier());
      task.setDestination(cluster.getClusterIdentifier());

      Vector prep_phrases = new Vector();
      for(int i = 0; i < nlength; i++){
	Node    child       = nlist.item(i);
	String  childname   = child.getNodeName();

	if(child.getNodeType() == Node.TEXT_NODE) {
	  continue;
	}
        if(child.getNodeType() == Node.ELEMENT_NODE){

	  if(childname.equals("verb")){
	    Verb verb = VerbParser.getVerb(child);
	    task.setVerb(verb);
	  }
	  else if(childname.equals("directobject")){
	    task.setDirectObject(DirectObjectParser.getDirectObject(ldm, child));
	  }
	  else if(childname.equals("from")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.FROM);
	    newpp.setIndirectObject(LocationParser.getLocation(ldm, child));
	    //task.setPrepositionalPhrase(newpp);
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("to")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.TO);
	    newpp.setIndirectObject(LocationParser.getLocation(ldm, child));
	    //task.setPrepositionalPhrase(newpp);
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("with")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.WITH);
	    newpp.setIndirectObject(getStuff(ldm, child));
	    //task.setPrepositionalPhrase(newpp);
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("using")){
	    // same as with
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.USING);
	    newpp.setIndirectObject(getStuff(ldm, child));
	    //task.setPrepositionalPhrase(newpp);
	    prep_phrases.addElement(newpp);
	  }
          /*
	  else if(childname.equals("via")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.VIA);
	    newpp.setIndirectObject(NetworkParser.getRouteSet(ldm, child));
	    //task.setPrepositionalPhrase(newpp);
	    prep_phrases.addElement(newpp);
	  }
          */
	  else if(childname.equals("for")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.FOR);
	    String forunit = getForUnit(child);
	    newpp.setIndirectObject(forunit);
	    //task.setPrepositionalPhrase(newpp);
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("readyat")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.READYAT);
	    Date readyat = DateParser.getDate(child);
	    Schedule sched = ldmf.newSimpleSchedule(readyat, readyat);
	    newpp.setIndirectObject(sched);
	    //task.setPrepositionalPhrase(newpp);
	    prep_phrases.addElement(newpp);
	  }
	  else if(childname.equals("startdate")){
	    Preference p = PreferencesParser.getStartDate(ldmf, child);
	    task.addPreference(p);
	  }
	  else if(childname.equals("enddate")){
	    Preference p = PreferencesParser.getEndDate(ldmf, child);
	    task.addPreference(p);
	  }
	  else if (childname.equals("cost")){
	    Preference p = PreferencesParser.getCost(ldmf, child);
	    task.addPreference(p);
	  }
	  else if(childname.equals("ItineraryOf")){
	    NewPrepositionalPhrase newpp = ldmf.newPrepositionalPhrase();
	    newpp.setPreposition(Constants.Preposition.ITINERARYOF);
	    newpp.setIndirectObject(ItineraryParser.getItinerary(ldm, child));
	    //task.setPrepositionalPhrase(newpp);
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
	    //	    newpp.setPreposition(AirforceGrammar.FROMTASK);
	    Asset AEF = new AbstractAsset();
	    try {
	      NewTypeIdentificationPG typeId = (NewTypeIdentificationPG)AEF.getTypeIdentificationPG();
	      typeId.setTypeIdentification(child.getFirstChild().getNodeValue());
	      typeId.setNomenclature(child.getFirstChild().getNodeValue());
	    } catch (Exception exc) {
	      System.err.println("problem creating the AbstractAsset for FromTask");
	    }
	    newpp.setIndirectObject(AEF);
	    //task.setPrepositionalPhrase(newpp);
	    prep_phrases.addElement(newpp);
	  }
	}
	task.setPrepositionalPhrases(prep_phrases.elements());
      }
    }
    return task;
  }

  private static Object getStuff(LDMServesPlugIn ldm, Node node){
    Object object = null;
    
    if(node.getNodeName().equals("with") ||
       node.getNodeName().equals("using")){
      NodeList  nlist    = node.getChildNodes();      
      int       nlength  = nlist.getLength();

      for(int i = 0; i < nlength; i++){
	Node    child       = nlist.item(i);
	String  childname   = child.getNodeName();
        if(child.getNodeType() == Node.ELEMENT_NODE){

	  if(childname.equals("asset")){
            object = AssetParser.getAsset(ldm, child);
          }
        } else if (child.getNodeType() == Node.TEXT_NODE) {
	  String data = child.getNodeValue().trim();
	  if(data.length() != 0){
	    object = data;
	  }

	}
      }
    }
    return object;
  }

  private static String getForUnit(Node node){
    Asset asset = null;
    Node data = node.getFirstChild();
    return data.getNodeValue();
  }
}
