// Copyright (9/99) Honeywell Inc.
// Unpublished - All rights reserved. This software was developed with funding 
// under U.S. government contract MDA972-97-C-0800

package org.cougaar.domain.glm.packer;

// factories
import org.cougaar.domain.planning.ldm.RootFactory;

// tasks
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.NewMPTask;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.Priority;

// Assets, etc.
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.NewItemIdentificationPG;

import org.cougaar.domain.glm.ldm.asset.ALPAsset;
import org.cougaar.domain.glm.ldm.asset.Container;
import org.cougaar.domain.glm.ldm.asset.PropertyGroupFactory;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.ldm.plan.GeolocLocationImpl;


// properties
import org.cougaar.domain.glm.ldm.asset.NewMovabilityPG;

import org.cougaar.domain.glm.ldm.Constants;

// utils
import java.util.Vector;


public class AmmoTransport extends AggregationClosure {
  public static final String AMMO_CATEGORY_CODE = "MBB";
  public static final String MILVAN_NSN = "NSN/8115001682275";
  public static final double PACKING_LIMIT = 16.0; /* short tons */

  private static Asset MILVAN_PROTOTYPE = null;
  private static int ITEM_ID = 0;

  // these instance variables constitute the "context" for this
  // "closure" 
  private GeolocLocation source;
  private GeolocLocation dest;

  
  // Going to try to call the geo functions from inside to see if they are the problem...
  public AmmoTransport(GeolocLocation s, GeolocLocation d) {
    source = s;
    dest = d;
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
    * Creates a Transport task, per the interface published by TOPS.
    */
    public NewMPTask newTask() {
      if (_gp == null) {
	System.err.println("AmmoTransport:  Error!  AmmoTransport not properly initialized: setGenericPlugin not called.");
	return null;
      }

      RootFactory factory = _gp.getGPFactory();
      if ((source == null) || 
          (dest == null)) {
	System.err.println("AmmoTransport:  Error!  AmmoTransport not properly initialized: some parameter(s) are null.");
	return null;
      }

      Asset milvan = makeMilvan(factory);
      if (milvan == null) {
        return null;
      }

      NewMPTask task = factory.newMPTask();
      task.setVerb(Constants.Verb.Transport);

      task.setPriority(Priority.UNDEFINED);

      task.setDirectObject(milvan);    

      Vector preps = new Vector(2);

      NewPrepositionalPhrase fromPrepositionalPhrase = 
        factory.newPrepositionalPhrase();
      fromPrepositionalPhrase.setPreposition(Constants.Preposition.FROM);
      fromPrepositionalPhrase.setIndirectObject(source);
      preps.addElement(fromPrepositionalPhrase);

      NewPrepositionalPhrase toPrepositionalPhrase = 
        factory.newPrepositionalPhrase();
      toPrepositionalPhrase = factory.newPrepositionalPhrase();
      toPrepositionalPhrase.setPreposition(Constants.Preposition.TO);
      toPrepositionalPhrase.setIndirectObject(dest);
      preps.addElement(toPrepositionalPhrase);

      task.setPrepositionalPhrases(preps.elements());

      return task;
    }

  /**
    * An ancillary method that creates an asset that represents a MILVAN 
    * (military container) carrying ammunition
    */
  public static Asset makeMilvan(RootFactory rootFactory) {
    if (MILVAN_PROTOTYPE == null) {
      MILVAN_PROTOTYPE = rootFactory.getPrototype(MILVAN_NSN);

      if (MILVAN_PROTOTYPE == null) {
        System.err.println("AmmoTransport: Error! Unable to get prototype for" +
                             " milvan NSN -" + MILVAN_NSN);
        return null;
      }
    }
        
    Container milvan = 
      (Container)rootFactory.createInstance(MILVAN_PROTOTYPE);

    // AMMO Cargo Code
    NewMovabilityPG movabilityPG = 
      PropertyGroupFactory.newMovabilityPG(milvan.getMovabilityPG());
    movabilityPG.setCargoCategoryCode(AMMO_CATEGORY_CODE);
    milvan.setMovabilityPG(movabilityPG);

    // Unique Item Identification
    NewItemIdentificationPG itemIdentificationPG = 
      (NewItemIdentificationPG)milvan.getItemIdentificationPG();
    String itemID = new String("Milvan" + ITEM_ID++);
    itemIdentificationPG.setItemIdentification(itemID);
    itemIdentificationPG.setNomenclature("Milvan");
    itemIdentificationPG.setAlternateItemIdentification(itemID);
    milvan.setItemIdentificationPG(itemIdentificationPG);

    return milvan;
  }
}



