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

package org.cougaar.lib.gss;

import org.cougaar.planning.ldm.asset.Asset;

import org.cougaar.planning.ldm.plan.Task;
//import org.cougaar.util.ConfigFileFinder;
import org.cougaar.util.ConfigFinder;

import java.io.File;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

// old IBM XML references
// import org.xml.sax.DocumentHandler;
// import org.xml.sax.InputSource;
// import org.xml.sax.Parser;
// import org.xml.sax.helpers.ParserFactory;

// modern xerces references
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.EntityResolver;

/**
 * The main class for the generic scheduler
 *
 */

public class GSScheduler {

    private boolean printUsedCapacities = false;

  public static final Object UNALLOCATEDKEY = new Integer (0);
    private boolean debug = false;

    private List myDebugables;

  private GSSchedulingSpecs specs;

  public GSSchedulingSpecs getSpecs () { return specs; }

  /** parse the XML specifications */
  /*
  public static GSScheduler parseSpecs (String filename, GSSpecsHandler sh) {
    if(! new File(filename).exists()){
      String e = ("GSSScheduler: could  not open gss specs file: " + filename);
      throw new RuntimeException(e);
    }
    try {
      // old IBM XML jar
      // Parser parser = ParserFactory.makeParser ("com.ibm.xml.parsers.SAXParser");
      // modern xerces jar
      SAXParser parser = new SAXParser();

      // old IBM XML jar
      // parser.setDocumentHandler (sh);
      // modern xerces jar
      parser.setContentHandler ((ContentHandler) sh);
      parser.parse (filename);

      return sh.getScheduler();
    }
//      catch(InstantiationException e) {
//        System.err.println (e.getMessage());
//        e.printStackTrace();
//      }
//      catch(IllegalAccessException e) {
//        System.err.println (e.getMessage());
//        e.printStackTrace();
//      }
//      catch(ClassNotFoundException e) {
//        System.err.println (e.getMessage());
//        e.printStackTrace();
//      }
    catch(SAXParseException saxe) {
      System.out.println ("UTILLdmXMLPlugin.getParsedDocument - got SAX Parse exception in file " + 
			  saxe.getSystemId () + " line " + saxe.getLineNumber () + " col " + saxe.getColumnNumber ()+"\n"+
			  "Exception was :");
      System.err.println(saxe.getMessage());
    }
    catch(org.xml.sax.SAXException e) {
      System.err.println (e.getMessage());
      e.printStackTrace();
    }
    catch(java.io.IOException e) {
      System.err.println (e.getMessage());
      e.printStackTrace();
    }
    return null;
  }
  */

  /** parse the XML specifications */
  public static GSScheduler parseSpecs (String gssfile, EntityResolver specialResolver, GSSpecsHandler sh) {
    try {
      InputStream inputStream = ConfigFinder.getInstance().open(gssfile);

      // old IBM XML jar
      // Parser parser = ParserFactory.makeParser ("com.ibm.xml.parsers.SAXParser");
      // modern xerces jar
      SAXParser parser = new SAXParser();

      // old IBM XML jar
      // parser.setDocumentHandler (sh);
      // modern xerces jar
      parser.setContentHandler ((ContentHandler) sh);

      parser.setEntityResolver (specialResolver);
      InputSource is = new InputSource (inputStream);
      is.setSystemId (gssfile);
      parser.parse (is);

      return sh.getScheduler();
    }
//      catch(InstantiationException e) {
//        System.err.println ("Instantion exception : " + e.getMessage());
//        e.printStackTrace();
//      }
//      catch(IllegalAccessException e) {
//        System.err.println ("IllegalAccess exception : " + e.getMessage());
//        e.printStackTrace();
//      }
//      catch(ClassNotFoundException e) {
//        System.err.println ("ClassNotFound exception : " + e.getMessage());
//        e.printStackTrace();
//      }
    catch(SAXParseException saxe) {
      System.out.println ("UTILLdmXMLPlugin.getParsedDocument - got SAX Parse exception in file " + 
			  saxe.getSystemId () + " line " + saxe.getLineNumber () + " col " + saxe.getColumnNumber ()+"\n"+
			  "Exception was :");
      System.err.println(saxe.getMessage());
    }
    catch(org.xml.sax.SAXException e) {
      System.err.println ("SAX exception : " + e.getMessage());
      e.printStackTrace();
    }
    catch(java.io.IOException e) {
      System.err.println ("IO exception : " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public GSScheduler (GSSchedulingSpecs ss, List debugables) {
    specs = ss;
    
    myDebugables = debugables;
    if (ss instanceof GSDebugable)
      myDebugables.add (ss);
  }

    public void setDebug (boolean debug) {
	if (debug)
	    System.out.println ("GSScheduler - debug is ON.");

	for (int i = 0; i < myDebugables.size (); i++)
	    ((GSDebugable)myDebugables.get (i)).setDebug (debug);

	this.debug = debug;
    }

    public void setPrintUsedCapacities(boolean printUsedCapacities){
	this.printUsedCapacities = printUsedCapacities;
    }

  public boolean interestingTask (Task task) {
    return specs.interestingTask (task);
  }

  /**
   * BOZO still missing clustering of tasks and pretask and posttask
   * BOZO durations
   * BOZO pretask, task, and posttask duration all functions of both
   * BOZO task and resource
   */
  public void schedule (List tasks,
			GSSchedulerResult result) {
    List assets = result.getAssets ();
    boolean useAligned = result.getAligned ();
    boolean useUnaligned  = result.getUnaligned ();
    List unallocated = new ArrayList ();

    for (Iterator iter = tasks.iterator(); iter.hasNext ();) {
      Task task = (Task) iter.next();
      if (! specs.interestingTask (task))
        continue;

      // set up for task assignment
      Asset bestAsset = null;
      GSTaskGroup bestGroup = null;
      double bestScore = Double.POSITIVE_INFINITY;
      GSTaskGroup origBestGroup = null;

      // find best asset and group
      for (Iterator assetIter = assets.iterator (); assetIter.hasNext ();) {
        Asset asset = (Asset) assetIter.next();
        if (! specs.isCapable (asset, task)){
          continue;
	}
        List groups = result.getTaskGroups (asset);
        if (groups == null)
          groups = Collections.EMPTY_LIST;
	
	// There is a bug in the schedule method that prefers assignment
	// to already-created groups.  This means that if you assign a
	// task to a group, it's almost impossible (as long as you keep
	// getting reasonably mutually assignable tasks) to add a new task
	// to a new asset even if the score is significantly better.  The fix
	// is to only not create a new group if you can't assign the
	//    task to this asset via any of the existing groups
	boolean foundGroup = false;

	//	if (debug && groups.size () > 0)
	//	    System.out.println ("GSScheduler - " + asset + " has " + groups.size () + " groups");

        // try to put into existing group
        if (useAligned) {
	  for (Iterator groupIter = groups.iterator (); groupIter.hasNext ();) {
	    GSTaskGroup oldg = (GSTaskGroup) groupIter.next();

	    if (oldg.isFrozen())
	      continue;
            if (! specs.canBeGrouped (task, oldg.representativeTask())) {
		if (debug)
		    System.out.println ("GSScheduler.scheduleWithScore - task " + task + 
					"\n\t cannot be grouped with" + oldg.representativeTask ());
              continue;
	    }

	    // Need to call this BEFORE we add the task to the group
	    double taskScore = specs.scoreForTask(task, oldg);

            GSTaskGroup newg = oldg.duplicate();
            newg.addTask (task);
            if (! newg.isConsistent (asset)) {
		if (debug)
		    System.out.println ("GSScheduler.scheduleWithScore - " + 
					"group with new task is not consistent with asset " + asset);
              continue;
	    }

	    // Set this to true now that we know that we have a group 
	    // for this task on this asset
	    foundGroup = true;

	    List gs = new ArrayList ();
	    Collections.copy (groups, gs);
	    gs.remove (oldg);

	    //Get the score for the asset-task pairing:
	    double assetTaskScore = specs.scoreForAssetTaskPair(asset, task);
	    double assetScore     = specs.scoreForAsset(asset, newg);
	    double bestTimeScore  = newg.pickBestTime (asset, new Vector(gs));
            double score = bestTimeScore * assetScore * assetTaskScore * taskScore;

// 	    if (debug && (asset instanceof CargoShip)) {
// 		System.out.println ("Scheduler (a) - asset " + asset + " score is " + score +
// 				    " time " + bestTimeScore +
// 				    " assetTaskScore " + assetTaskScore +
// 				    " assetScore " + assetScore +
// 				    " taskScore " + taskScore);
// 	    }

            if ((origBestGroup == null) || (score < bestScore)) {
              bestScore = score;
              bestAsset = asset;
              bestGroup = newg;
              origBestGroup = oldg;
            }
          }
        }

        // try to make new group if no existing group
        if(!foundGroup) {
          long d = specs.taskDuration (task, asset);
	  // Need to call this BEFORE we add the task to the group
	  // so we get some funny semantics (i.e. passing in null)
	  double taskScore = specs.scoreForTask(task, null);

          GSTaskGroup tg = new GSTaskGroup (task, d, specs);

	  if (debug) 
	      System.out.println ("GSScheduler - Creating new group for " + asset);

          if (! tg.isConsistent (asset)) {
	      if (debug)
		  System.out.println ("GSScheduler.scheduleWithScore - " + 
				      "single new task is not consistent with asset " + asset);
            continue;
	  }
	  else if (debug)
	      System.out.println ("GSScheduler.scheduleWithScore - group now has " + 
				  tg.getTasks ().size ());

	  double assetTaskScore = specs.scoreForAssetTaskPair(asset, task);
          double score = tg.pickBestTime (asset, useUnaligned ? null : new Vector (groups)) * 
	      specs.scoreForAsset(asset, tg) * assetTaskScore * taskScore;

// 	  if (debug && (asset instanceof CargoShip))
// 	      System.out.println ("Scheduler - asset " + asset + " score is " + score);

          if (score < bestScore) {
            bestScore = score;
            bestAsset = asset;
            bestGroup = tg;
	    origBestGroup = null;
          }
        }
      }

      // update set of assignments
      if (bestAsset == null) {
        unallocated.add (new GSTaskGroup(task, 0l, specs));
        continue;
      }
      List tasks2 = result.getTaskGroups (bestAsset);
      if (tasks2 == null) {
        tasks2 = new ArrayList (5);
        result.putTaskGroups (bestAsset, tasks2);
      }
      if (origBestGroup != null)
        tasks2.remove (origBestGroup);
      tasks2.add (bestGroup);
    }
    result.setUnhandledTasks (unallocated);
    if(printUsedCapacities)
	result.printUsedCapacities();
  }

  /** 
   * Pust for unit testing.  Has problems with MB5.2.4b.  
   * We need to wait until we have crew member properties since
   * this test script depends on that.

  public static final void main (String[] args) {
    org.cougaar.core.domain.RootFactory ldmf = new org.cougaar.core.domain.RootFactory (null, null);
    GSScheduler s = parseSpecs ("tops/gss/aircrew.xml");
    Asset asset1 = new Asset();
    org.cougaar.planning.ldm.asset.NewAirForcePersonPG prop1 =
      org.cougaar.planning.ldm.asset.PropertyGroupFactory.newAirForcePersonPG();
    prop1.setQualifiedAircraftType ("C-17");
    prop1.setAircraftPosition ("Pilot");
    asset1.setAirForcePersonPG (prop1);
    org.cougaar.glm.ldm.asset.NewWaterSelfPropulsionPG prop4 =
      org.cougaar.planning.ldm.asset.PropertyGroupFactory.newWaterSelfPropulsionPG();
    prop4.setCruiseSpeed (org.cougaar.planning.ldm.measure.Speed.newMetersPerSecond (3150.0));
    asset1.setWaterSelfPropulsionPG (prop4);
    Asset asset2 = new Asset();
    org.cougaar.glm.ldm.asset.NewAirVehiclePG prop2 =
      org.cougaar.planning.ldm.asset.PropertyGroupFactory.newAirVehiclePG();
    prop2.setVehicleType ("C-17");
    asset2.setAirVehiclePG (prop2);
    Asset asset3 = new Asset();
    org.cougaar.planning.ldm.asset.NewAirForcePersonPG prop3 =
      org.cougaar.planning.ldm.asset.PropertyGroupFactory.newAirForcePersonPG();
    prop3.setQualifiedAircraftType ("C-141");
    prop3.setAircraftPosition ("Pilot");
    asset3.setAirForcePersonPG (prop3);
    asset3.setWaterSelfPropulsionPG (prop4);
    org.cougaar.planning.ldm.plan.TaskImpl task1 =
      new org.cougaar.planning.ldm.plan.TaskImpl (1);
    task1.setVerb (new org.cougaar.planning.ldm.plan.Verb ("Supply"));
    org.cougaar.planning.ldm.plan.PrepositionalPhraseImpl pp1 =
      new org.cougaar.planning.ldm.plan.PrepositionalPhraseImpl();
    pp1.setPreposition ("Using");
    pp1.setIndirectObject (asset2);
    java.util.Date d1 = new java.util.Date ();
    org.cougaar.planning.ldm.plan.PrepositionalPhraseImpl pp3 =
      new org.cougaar.planning.ldm.plan.PrepositionalPhraseImpl();
    pp3.setPreposition ("From");
    org.cougaar.glm.ldm.plan.GeolocLocationImpl gl1 =
      new org.cougaar.glm.ldm.plan.GeolocLocationImpl();
    gl1.setLatitude (org.cougaar.planning.ldm.measure.Latitude.newLatitude (0.0));
    gl1.setLongitude (org.cougaar.planning.ldm.measure.Longitude.newLongitude (0.0));
    gl1.setGeolocCode ("aaa");
    gl1.setName ("aaa");
    pp3.setIndirectObject (gl1);
    org.cougaar.planning.ldm.plan.PrepositionalPhraseImpl pp4 =
      new org.cougaar.planning.ldm.plan.PrepositionalPhraseImpl();
    pp4.setPreposition ("To");
    org.cougaar.glm.ldm.plan.GeolocLocationImpl gl2 =
      new org.cougaar.glm.ldm.plan.GeolocLocationImpl();
    gl2.setLatitude (org.cougaar.planning.ldm.measure.Latitude.newLatitude (10.0));
    gl2.setLongitude (org.cougaar.planning.ldm.measure.Longitude.newLongitude (10.0));
    gl2.setGeolocCode ("bbb");
    gl2.setName ("bbb");
    pp4.setIndirectObject (gl2);
    Vector v1 = new Vector (4);
    v1.addElement (pp1);
    v1.addElement (pp3);
    v1.addElement (pp4);
    task1.setPrepositionalPhrases (v1.elements());
    task1.setSource (new org.cougaar.core.agent.ClusterIdentifier ("abc"));
    Vector v5 = new Vector();
    org.cougaar.planning.ldm.plan.Preference pref1 =
      com.bbn.tops.util.TOPSPreference.makeStartDatePreference (ldmf, d1);
    v5.addElement (pref1);
    org.cougaar.planning.ldm.plan.Preference pref2 =
      com.bbn.tops.util.TOPSPreference.makeEndDatePreference
        (ldmf, new java.util.Date (d1.getTime() + 500000),
         new java.util.Date (d1.getTime() + 1000000),
         new java.util.Date (d1.getTime() + 2000000));
    v5.addElement (pref2);
    task1.setPreferences (v5.elements());
    org.cougaar.planning.ldm.plan.TaskImpl task2 =
      new org.cougaar.planning.ldm.plan.TaskImpl (2);
    task2.setVerb (new org.cougaar.planning.ldm.plan.Verb ("Transport"));
    task2.setPrepositionalPhrases (v1.elements());
    task2.setSource (new org.cougaar.core.agent.ClusterIdentifier ("abc"));
    task2.setPreferences (v5.elements());
    System.out.println ("Looking for true, got " +
                        s.specs.interestingTask (task1));
    System.out.println ("Looking for false, got " +
                        s.specs.interestingTask (task2));
    System.out.println ("Looking for true, got " +
                        s.specs.isCapable (asset1, task1));
    System.out.println ("Looking for false, got " +
                        s.specs.isCapable (asset3, task1));
    double[] arr1 = {1};
    double[] arr2 = {2};
    System.out.println ("Looking for true, got " +
                        s.specs.withinCapacities (asset1, arr1));
    System.out.println ("Looking for false, got " +
                        s.specs.withinCapacities (asset1, arr2));
    System.out.println ("Looking for 2, got " +
                        s.specs.incrementCapacities(task1, arr1)[0]);
    Vector tasks = new Vector (4);
    tasks.addElement (task1);
    task2.setVerb (new org.cougaar.planning.ldm.plan.Verb ("Supply"));
    tasks.addElement (task2);
    Vector assets = new Vector (4);
    assets.addElement (asset1);
    assets.addElement (asset3);
    org.cougaar.planning.ldm.plan.TaskImpl task3 =
      new org.cougaar.planning.ldm.plan.TaskImpl (3);
    task3.setVerb (new org.cougaar.planning.ldm.plan.Verb ("Supply"));
    task3.setPrepositionalPhrases (v1.elements());
    task3.setSource (new org.cougaar.core.agent.ClusterIdentifier ("abc"));
    task3.setPreferences (v5.elements());
    tasks.addElement (task3);
    org.cougaar.planning.ldm.plan.TaskImpl task4 =
      new org.cougaar.planning.ldm.plan.TaskImpl (4);
    task4.setVerb (new org.cougaar.planning.ldm.plan.Verb ("Supply"));
    task4.setPrepositionalPhrases (v1.elements());
    task4.setSource (new org.cougaar.core.agent.ClusterIdentifier ("abc"));
    task4.setPreferences (v5.elements());
    tasks.addElement (task4);
    org.cougaar.planning.ldm.plan.TaskImpl task5 =
      new org.cougaar.planning.ldm.plan.TaskImpl (5);
    task5.setVerb (new org.cougaar.planning.ldm.plan.Verb ("Supply"));
    task5.setPrepositionalPhrases (v1.elements());
    task5.setSource (new org.cougaar.core.agent.ClusterIdentifier ("abc"));
    task5.setPreferences (v5.elements());
    tasks.addElement (task5);
    Hashtable ht = s.schedule(tasks.elements(), assets.elements());
    Vector unall = (Vector) ht.get (UNALLOCATEDKEY);
    System.out.println ("There were " + unall.size() + " unallocated tasks");
    ht.remove (UNALLOCATEDKEY);
    Enumeration keys = ht.keys();
    while (keys.hasMoreElements()) {
      Asset a = (Asset) keys.nextElement();
      Vector v = (Vector) ht.get (a);
      System.out.println ("Asset " + a + " has schedule:");
      for (int i = 0; i < v.size(); i++) {
        GSTaskGroup tg = (GSTaskGroup) v.elementAt(i);
        System.out.println ("  Start = " + tg.getCurrentStart() +
                            " End = " + tg.getCurrentEnd());
        Vector tasks2 = (Vector) tg.getTasks();
        for (int j = 0; j < tasks2.size(); j++) {
          Task t = (Task) tasks2.elementAt(j);
          System.out.println ("    " + t);
        }
      }
    }
  }
   */

}
