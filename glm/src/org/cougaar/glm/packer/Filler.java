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
package org.cougaar.glm.packer;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.planning.ldm.asset.PropertyGroupSchedule;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;

import org.cougaar.planning.ldm.measure.Mass;

import org.cougaar.planning.ldm.plan.AllocationResultDistributor;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.NewMPTask;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;

import org.cougaar.glm.ldm.Constants;

import org.cougaar.glm.ldm.asset.GLMAsset;
import org.cougaar.glm.ldm.asset.NewContentsPG;
import org.cougaar.glm.ldm.asset.NewPhysicalPG;
import org.cougaar.glm.ldm.asset.PhysicalPG;
import org.cougaar.glm.ldm.asset.PropertyGroupFactory;

class Filler {
  private static final String UNKNOWN = "UNKNOWN";
  private Sizer _sz;
  
  private GenericPlugin _gp;

  /**
    * This is the generator for the Multi-parent tasks that
    * are the "product" of the aggregation. Currently assumes that
    * we're creating Transport tasks.
    */
  private AggregationClosure _ac;

  private PreferenceAggregator _pa;

  /**
    * The AllocationResultDistributor that should be used on
    * any Containers created by this Filler
    */
  private AllocationResultDistributor _ard;

  private static int TRANSPORT_TONS;

  Filler (Sizer sz, GenericPlugin gp, AggregationClosure ac,
	  AllocationResultDistributor ard,
	  PreferenceAggregator pa) {
    _sz = sz;
    _gp = gp;
    _ac = ac;
    _ard = ard;
    _pa = pa;
  }

  /**
    * This is the driving function in the whole packing process.
    */
  public void execute () {
    boolean finished = false;

    while (!finished) {
      // initialize the aggregation
      ArrayList agglist = new ArrayList();
      double amount = 0.0;
      while (_ac.getQuantity() - amount > 0.0) {
        Task t = _sz.provide(_ac.getQuantity() - amount);
        if (t == null ) {
          finished = true;
          break;
        }
        
        // if we reach here, t is a Task that provides
        // some amount towards our overall amount
        double provided  = t.getPreferredValue(AspectType.QUANTITY);
        
        if (!_ac.validTask(t)) {
          _gp.getLoggingService().error("Filler.execute: AggregationClosure rejected " +
                                      " task - " + t);
          continue;
        }
        
        amount += provided;
        agglist.add(t);
      }
      
      if (!agglist.isEmpty()) {
	// now we do the aggregation
	NewMPTask mpt = _ac.newTask();
        
        // Set ContentsPG on container
        addContentsInfo((GLMAsset) mpt.getDirectObject(), agglist);
        
        //BOZO
	mpt.setPreferences(new Vector(_pa.aggregatePreferences(agglist.iterator(),
                                                               _gp.getGPFactory())).
                           elements());
        double loadedQuantity = mpt.getPreferredValue(AspectType.QUANTITY);
        TRANSPORT_TONS += loadedQuantity;
	Plan plan = ((Task)agglist.get(0)).getPlan();
        
	_gp.createAggregation(agglist.iterator(), mpt, plan, _ard);
      }
    }
    
    _gp.getLoggingService().info("Packer  - current aggregated requested transport: " +
                                 TRANSPORT_TONS + " tons.");
  }

  protected void addContentsInfo(GLMAsset container, ArrayList agglist) {
    ArrayList typeIDs = new ArrayList();
    ArrayList nomenclatures = new ArrayList();
    ArrayList weights = new ArrayList();
    ArrayList receivers = new ArrayList();

    for (Iterator iterator = agglist.iterator();
         iterator.hasNext();) {
      Task task = (Task) iterator.next();
      TypeIdentificationPG typeIdentificationPG = 
        task.getDirectObject().getTypeIdentificationPG();
      String typeID;
      String nomenclature;
      if (typeIdentificationPG != null) {
        typeID = typeIdentificationPG.getTypeIdentification();
        if ((typeID == null) || (typeID.equals(""))) {
          typeID = UNKNOWN;
        } 
        
        nomenclature = typeIdentificationPG.getNomenclature();
        if ((nomenclature == null) || (nomenclature.equals(""))) {
          nomenclature = UNKNOWN;
        }
      } else {
        typeID = UNKNOWN;
        nomenclature = UNKNOWN;
      }
      typeIDs.add(typeID);
      nomenclatures.add(nomenclature);
      
      double quantity = task.getPreferredValue(AspectType.QUANTITY);
      Mass mass = Mass.newMass(quantity, Mass.SHORT_TONS); 
      weights.add(mass);
      
      Object receiver = 
        task.getPrepositionalPhrase(Constants.Preposition.FOR);
      String receiverID;
        
      // Add field with recipient
      if (receiver == null)
        receiverID = UNKNOWN;
      else if (receiver instanceof String) 
	receiverID = (String) receiver;
      else if (!(receiver instanceof Asset)) {
        receiverID = UNKNOWN;
      } else {
        ItemIdentificationPG itemIdentificationPG = 
          ((Asset) receiver).getItemIdentificationPG();
        if ((itemIdentificationPG == null) ||
            (itemIdentificationPG.getItemIdentification() == null) ||
            (itemIdentificationPG.getItemIdentification().equals(""))) {
          receiverID = UNKNOWN;
        } else {
          receiverID = itemIdentificationPG.getItemIdentification();
        }
      }
      receivers.add(receiverID);
    }
    
    // Contents
    NewContentsPG contentsPG = 
      PropertyGroupFactory.newContentsPG();
    contentsPG.setNomenclatures(nomenclatures);
    contentsPG.setTypeIdentifications(typeIDs);
    contentsPG.setWeights(weights);
    contentsPG.setReceivers(receivers);
    container.setContentsPG(contentsPG);
  }
  
}


