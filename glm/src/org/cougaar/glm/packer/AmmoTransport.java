// Copyright (9/99) Honeywell Inc.
// Unpublished - All rights reserved. This software was developed with funding 
// under U.S. government contract MDA972-97-C-0800

package org.cougaar.glm.packer;

// utils
import java.util.*;

import org.cougaar.core.agent.ClusterIdentifier;

// factories
import org.cougaar.core.domain.RootFactory;

// tasks
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.NewMPTask;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Priority;

// Assets, etc.
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.planning.ldm.asset.PropertyGroupSchedule;

import org.cougaar.glm.ldm.Constants;

import org.cougaar.glm.ldm.asset.GLMAsset;
import org.cougaar.glm.ldm.asset.Container;
import org.cougaar.glm.ldm.asset.NewContentsPG;
import org.cougaar.glm.ldm.asset.NewMovabilityPG;
import org.cougaar.glm.ldm.asset.NewPhysicalPG;
import org.cougaar.glm.ldm.asset.PhysicalPG;
import org.cougaar.glm.ldm.asset.PropertyGroupFactory;

import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.GeolocLocationImpl;





public class AmmoTransport extends AggregationClosure {
  public static final String AMMO_CATEGORY_CODE = "MBB";
  public static final String MILVAN_NSN = "NSN/8115001682275";
  public static final double PACKING_LIMIT = 13.9; /* short tons */

  private static Asset MILVAN_PROTOTYPE = null;
  private static long COUNTER = 0;


  private GeolocLocation mySource;
  private GeolocLocation myDestination;

  public static Collection getTransportGroups(Collection tasks) {
    HashMap destMap = new HashMap();
    
    for (Iterator iterator = tasks.iterator(); iterator.hasNext();) {
      Task task = (Task) iterator.next();
      GeolocLocation destination = getDestination(task);
      Collection destTasks = (Collection) destMap.get(destination);
      if (destTasks == null) {
        destTasks = new ArrayList();
        destMap.put(destination, destTasks);
      }
      destTasks.add(task);
    }
    
    return destMap.values();
  }

  public AmmoTransport(ArrayList tasks) {
    for (Iterator iterator = tasks.iterator(); iterator.hasNext();) {
      Task t = (Task) iterator.next();
      GeolocLocation taskSource = getSource(t);
      GeolocLocation taskDestination = getDestination(t);
      
      if ((taskSource == null) || (taskDestination == null)) {
        System.err.println("AmmoTransport(): task without a source/destination");
      } else if ((mySource == null) || (myDestination == null)) {
        mySource = taskSource;
        myDestination = taskDestination;
      } else if (!(mySource.getGeolocCode().equals(taskSource.getGeolocCode()))) {
        System.err.println("AmmoTransport(): " + mySource + " not equal to " + 
                           taskSource);
      } else if (!(myDestination.getGeolocCode().equals(taskDestination.getGeolocCode()))) {
        System.err.println("AmmoTransport(): " + myDestination + 
                           " not equal to " + taskDestination);
      }
    }
  }

  /**
   * returns max quanitity in short tons
   *
   * BOZO - unit picked because it agreed with the incoming supply requests
   */
  public double getQuantity() {
    return PACKING_LIMIT;
  }

  /**
   * returns appropriate transport source location
   * 
   * Currently hardcoded because incoming supply tasks don't have that
   * info
   */
  public static GeolocLocation getSource(Task task) {
    return Geolocs.blueGrass();
  }

  /**
   * returns appropriate transport destination location
   * 
   */
  public static GeolocLocation getDestination(Task task) {
    PrepositionalPhrase phrase = 
        task.getPrepositionalPhrase(Constants.Preposition.TO);
      GeolocLocation destination = null;
      if (phrase != null) {
        destination = (GeolocLocation) phrase.getIndirectObject(); 
      }
    return destination;
  }
  
  public boolean validTask(Task t) {
    return (mySource.getGeolocCode().equals(getSource(t).getGeolocCode()) && 
            myDestination.getGeolocCode().equals(getDestination(t).getGeolocCode()));
  }

  /**
   * Creates a Transport task, per the interface published by TOPS.
   */
  public NewMPTask newTask() {
    if (_gp == null) {
      System.err.println("AmmoTransport:  Error!  AmmoTransport not properly initialized: setGenericPlugin not called.");
      return null;
    }
    
    if ((mySource == null) ||
        (myDestination == null)) {
      System.err.println("AmmoTransport:  Error!  AmmoTransport not properly initialized: some parameter(s) are null.");
      return null;
    }
    
    Asset milvan = makeMilvan();
    if (milvan == null) {
      return null;
    }
    
    NewMPTask task = _gp.getGPFactory().newMPTask();
    task.setVerb(Constants.Verb.Transport);
    
    task.setPriority(Priority.UNDEFINED);
    
    task.setDirectObject(milvan);    
    
    Vector preps = new Vector(2);
    
    NewPrepositionalPhrase fromPrepositionalPhrase = 
      _gp.getGPFactory().newPrepositionalPhrase();
    fromPrepositionalPhrase.setPreposition(Constants.Preposition.FROM);
    fromPrepositionalPhrase.setIndirectObject(mySource);
    preps.addElement(fromPrepositionalPhrase);
    
    NewPrepositionalPhrase toPrepositionalPhrase = 
      _gp.getGPFactory().newPrepositionalPhrase();
    toPrepositionalPhrase = _gp.getGPFactory().newPrepositionalPhrase();
    toPrepositionalPhrase.setPreposition(Constants.Preposition.TO);
    toPrepositionalPhrase.setIndirectObject(myDestination);
    preps.addElement(toPrepositionalPhrase);
    
    task.setPrepositionalPhrases(preps.elements());
    
    return task;
  }
  
  /**
   * An ancillary method that creates an asset that represents a MILVAN 
   * (military container) carrying ammunition
   */
  protected Asset makeMilvan() {
    
    if (MILVAN_PROTOTYPE == null) {
      MILVAN_PROTOTYPE = _gp.getGPFactory().getPrototype(MILVAN_NSN);
      
      if (MILVAN_PROTOTYPE == null) {
        System.err.println("AmmoTransport: Error! Unable to get prototype for" +
                           " milvan NSN -" + MILVAN_NSN);
        return null;
      }
    }
    
    Container milvan = 
      (Container)_gp.getGPFactory().createInstance(MILVAN_PROTOTYPE);
    
    // AMMO Cargo Code
    NewMovabilityPG movabilityPG = 
      PropertyGroupFactory.newMovabilityPG(milvan.getMovabilityPG());
    movabilityPG.setCargoCategoryCode(AMMO_CATEGORY_CODE);
    milvan.setMovabilityPG(movabilityPG);
    
    // Milvan Contents
    NewContentsPG contentsPG = 
      PropertyGroupFactory.newContentsPG();
    milvan.setContentsPG(contentsPG);
    
    // Unique Item Identification
    NewItemIdentificationPG itemIdentificationPG = 
      (NewItemIdentificationPG)milvan.getItemIdentificationPG();
    String itemID = makeMilvanID();
    itemIdentificationPG.setItemIdentification(itemID);
    itemIdentificationPG.setNomenclature("Milvan");
    itemIdentificationPG.setAlternateItemIdentification(itemID);
    milvan.setItemIdentificationPG(itemIdentificationPG);


    // Make unique physicalPG so weight can be modified to reflect current load
    PropertyGroupSchedule physicalPGSchedule = 
      milvan.getPhysicalPGSchedule();
    PhysicalPG defaultPhysicalPG = 
      (PhysicalPG) physicalPGSchedule.getDefault();
    if (defaultPhysicalPG == null) {
      System.err.println("AmmoTransport: milvan with a null default physicalPG");
    }
    physicalPGSchedule = 
      PropertyGroupFactory.newPhysicalPGSchedule(defaultPhysicalPG);    
    milvan.setPhysicalPGSchedule(physicalPGSchedule);

    return milvan;
  }
  
  protected String makeMilvanID() {
    
    return new String(_gp.getGPClusterIdentifier() + 
                      ":Milvan" + getCounter());
  }
  
  private static synchronized long getCounter() {
    return COUNTER++;
  }
    
}







