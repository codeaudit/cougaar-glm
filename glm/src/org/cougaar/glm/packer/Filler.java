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
package org.cougaar.glm.packer;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.GLMAsset;
import org.cougaar.glm.ldm.asset.NewContentsPG;
import org.cougaar.glm.ldm.asset.PropertyGroupFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.planning.ldm.measure.Mass;
import org.cougaar.planning.ldm.plan.AllocationResultDistributor;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.ContextOfOplanIds;
import org.cougaar.planning.ldm.plan.NewMPTask;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;


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

  public static double TRANSPORT_TONS;

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
  public double execute () {
    boolean finished = false;
    double tonsPacked = 0;
    
    if (_gp.getLoggingService().isInfoEnabled()) 
      _gp.getLoggingService().info("Filler.execute - entered.");
    int numTasks = 0;
    int numParents = 0;
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
	numTasks++;
        
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
	double loadedQuantity = createMPTask(agglist);
	numParents += agglist.size();
        TRANSPORT_TONS += loadedQuantity;
	tonsPacked += loadedQuantity;
      }
    }

    if (numTasks != numParents)
      _gp.getLoggingService().error("Filler.execute - num tasks created " + numTasks + 
				    " != parents of MPTask " + numParents);

    if (numParents != _sz.sizedMade)
      _gp.getLoggingService().error("Filler.execute - sizer num tasks made " + _sz.sizedMade + 
				    " != total parents of MPTask " + numParents);

    if(_gp.getLoggingService().isInfoEnabled())
      _gp.getLoggingService().info("Packer  - current aggregated requested transport: " +
				   TRANSPORT_TONS + " tons.");

    if (_gp.getLoggingService().isInfoEnabled()) 
      _gp.getLoggingService().info("Filler.execute - exited.");

    return tonsPacked;
  }

  public double handleUnplanned (Task unplanned) {
    if (_gp.getLoggingService().isDebugEnabled()) 
      _gp.getLoggingService().debug("Filler.handleUnplanned - replanning " + unplanned.getUID());
    List agglist = new ArrayList ();
    agglist.add (unplanned);
    double loadedQuantity = createMPTask(agglist);
    return loadedQuantity;
  }

  /** agglist is the list of parent tasks */
  protected double createMPTask (List agglist) {
    // now we do the aggregation
    NewMPTask mpt = _ac.newTask();

    HashSet set = new HashSet();
    Iterator taskIt = agglist.iterator();
    Task parentTask;
    while (taskIt.hasNext()) {
      parentTask = (Task)taskIt.next();
      if (parentTask.getContext() != null) {
        set.addAll((ContextOfOplanIds)parentTask.getContext());
      }
    }
    mpt.setContext(new ContextOfOplanIds(set));

    // Set ContentsPG on container
    addContentsInfo((GLMAsset) mpt.getDirectObject(), agglist);
        
    //BOZO
    mpt.setPreferences(new Vector(_pa.aggregatePreferences(agglist.iterator(),
							   _gp.getGPFactory())).elements());
    double loadedQuantity = mpt.getPreferredValue(AspectType.QUANTITY);
    Plan plan = ((Task)agglist.get(0)).getPlan();
        
    _gp.createAggregation(agglist.iterator(), mpt, plan, _ard);

    if (mpt.getComposition().getParentTasks().size () != agglist.size())
      _gp.getLoggingService().error("Filler.createMPTask - received " + agglist.size() + 
				    " tasks to be agggregated, but only "+
				    mpt.getComposition().getParentTasks().size ()  + 
				    " tasks as parents of " + mpt.getUID());
    return loadedQuantity;
  } 

  protected void addContentsInfo(GLMAsset container, List agglist) {
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

      if (receiver != null)
	receiver = ((PrepositionalPhrase) receiver).getIndirectObject();

      String receiverID;
        
      // Add field with recipient
      if (receiver == null) {
        receiverID = UNKNOWN;
	_gp.getLoggingService().error("Filler.addContentsInfo - Task " + task.getUID() + " had no FOR prep.");
      }
      else if (receiver instanceof String) {
	receiverID = (String) receiver;
      }
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


