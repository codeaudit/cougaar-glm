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

package org.cougaar.domain.mlm.plugin.assessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.io.Serializable;

import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.society.UID;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.plugin.LDMService;
import org.cougaar.core.plugin.util.PlugInHelper;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPG;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.plan.Allocation;
import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AllocationResultAggregator;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.HasRelationships;
import org.cougaar.domain.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.Relationship;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.TaskScoreTable;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.NewWorkflow;
import org.cougaar.domain.planning.ldm.plan.Workflow;

import org.cougaar.domain.glm.ldm.Constants;

import org.cougaar.domain.glm.ldm.plan.AlpineAspectType;

import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.asset.BulkPOL;
import org.cougaar.domain.glm.plugins.MaintainedItem;

import org.cougaar.domain.glm.plugins.TaskUtils;

public class ReadinessAssessorPSPPlugIn extends ReadinessAssessorPlugin {

  public static final Hashtable pspData = new Hashtable();

  /**
   * @return an collection of AspectValue[] - part of a phased AllocationResult
   */
  protected ArrayList calcResults(Task parentTask, HashMap tree, long start, long end) {
    ArrayList in = new ArrayList(13);

    // Collection of AspectValue[] for entire Agent
    ArrayList overallResults = new ArrayList(13);
    // Collection of AspectValue[] for a pacing item
    ArrayList pacingResults = new ArrayList(13);
    // Collection of AspectValue[] for a pacing item and supply type
    ArrayList pacingAndSupplyResults = new ArrayList(13);

    int mergeCount = 0;

    for (Iterator itemIt = tree.keySet().iterator(); itemIt.hasNext();) {
      MaintainedItem pacingItem = (MaintainedItem)itemIt.next();
      // make new subtask of toplevel task
      Task pacingTask = createSubTask(parentTask, pacingItem, null);
      publishAddToExpansion(parentTask, pacingTask);

      HashMap itemBuckets = (HashMap)tree.get(pacingItem);
      for (Iterator bucketsIt = itemBuckets.keySet().iterator(); bucketsIt.hasNext();) {
       Asset suppliedItem = (Asset) bucketsIt.next();
       ArrayList bucket = (ArrayList) itemBuckets.get(suppliedItem);

       long lastEnd = start;
       while (lastEnd < end) {
         long day1 = lastEnd;
         long dayn = lastEnd + (MILLISPERDAY * rollupSpan);
         lastEnd = dayn;
         in.clear();
           for ( Iterator rseIt = bucket.iterator(); rseIt.hasNext(); ) {
             RateScheduleElement rse = (RateScheduleElement) rseIt.next();
             if (inRange(day1, dayn, rse)) {
               in.add(rse);
               rseIt.remove(); // counted it once. won't need it again
             } 
           }
           double avg = average(in);
           AspectValue[] avs = null;
           if ( Double.isNaN(avg) )
           {
             System.err.println(debugStart + "invalid aspect value " + in);
           }
           else
           {
//             System.out.println ("calling newReadiness with avg of: " + avg);
             avs = newReadinessAspectArray(day1, dayn, avg);
//             avs = newReadinessAspectArray(day1, dayn, average(in));
//           if (Double.isNaN(avs[2].getValue())) {
//           System.err.println(debugStart + "invalid aspect value " + avs);
           }

           if (avs != null)
             pacingAndSupplyResults.add(avs);

       }

       // At this point we have the phased allocation result collection for the lowest level task (pacing item, supply type)
       // make new subtask of pacing item task and fill in its allocation result
       Task pacingAndSupplyTask = createSubTask(pacingTask, pacingItem, suppliedItem);
       if (pacingResults.isEmpty()) {
         pacingResults.addAll(pacingAndSupplyResults);
         mergeCount = 1;
       } else {
         mergeAdd(pacingResults, pacingAndSupplyResults);
         mergeCount++;
       }
       publishAddToExpansion(pacingTask, pacingAndSupplyTask);
       publishAllocationResult(pacingAndSupplyTask, pacingAndSupplyResults);

//       System.out.println();
//       System.out.println(pacingItem + " " + suppliedItem);
//       printResults(pacingAndSupplyResults);

        // before we get rid of it we want a copy for out psp hook-up
        storeCopyForPSP ( new String (pacingItem.getNomenclature()), new String (suppliedItem.getTypeIdentificationPG().getNomenclature()), pacingAndSupplyResults);

        pacingAndSupplyResults.clear();
      }

      averageResults(pacingResults, mergeCount);

      // At this point we have enough info to fill in the allocation result collection for the the pacing item task
      // fill in allocation result of pacing item subtask

      if (overallResults.isEmpty()) {
       overallResults.addAll(pacingResults);
      } else {
       merge(overallResults, pacingResults);
      }

      publishAddToExpansion(parentTask, pacingTask);
      publishAllocationResult(pacingTask, pacingResults);

//        System.out.println();  System.out.println();
//        System.out.println(debugStart);
//        System.out.println(pacingItem);
//        printResults(pacingResults);

      storeCopyForPSP ( new String (pacingItem.getNomenclature()), new String ("All Assets"), pacingResults);

      pacingResults.clear();
    }

    publishAllocationResult(parentTask, overallResults );

    storeCopyForPSP (new String ("Overall"), new String ("All Assets"), overallResults);

    return overallResults;
  }

  private void storeCopyForPSP (String item, String invAsset, ArrayList arList)
  {

//    System.out.println ("storeCopyForPSP: item: " + item + "asset: " + invAsset + "arlist size: " + arList.size() );

    synchronized (pspData)
    {

      String cId = new String (getClusterIdentifier().cleanToString() );
//      System.out.println ("ReadinessAssessorPSPPlugIn: storing cluster data with key " + cId);

      HashMap clusterData = (HashMap) pspData.get(cId);
      if (clusterData == null)
      {
        clusterData = new HashMap();
        pspData.put (cId, clusterData);
      }

      HashMap assetBucket = (HashMap) clusterData.get(item);
      if (assetBucket == null)
      {
        assetBucket = new HashMap();
        clusterData.put(item, assetBucket);
      }

      ArrayList ar = (ArrayList) assetBucket.get(invAsset);
      if (ar == null)
      {

        ar = new ArrayList(arList.size());

          for (int kk = 0; kk < arList.size(); kk ++)
          {
            AspectValue[] avArray = new AspectValue[3];

            AspectValue[] avs = (AspectValue[]) arList.get(kk);

            avArray[0] = new AspectValue (avs[0].getAspectType(), avs[0].getValue());
            avArray[1] = new AspectValue (avs[1].getAspectType(), avs[1].getValue());
            avArray[2] = new AspectValue (avs[2].getAspectType(), avs[2].getValue());

            ar.add(avArray);
           }

        assetBucket.put(invAsset, ar);
      }
      else
      {
//        System.out.println ("ReadinessAssessorPSPPlugIn: array list " + item + "->" + invAsset + " has data, replacing");
        ar.clear();
        ar.addAll(arList);
      }

    }

  }


}
