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

package org.cougaar.lib.util;

import org.cougaar.core.domain.LDMServesPlugin;
import org.cougaar.core.domain.RootFactory;

import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AssetGroup;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;

import org.cougaar.planning.ldm.measure.Area;
import org.cougaar.planning.ldm.measure.Distance;
import org.cougaar.planning.ldm.measure.Mass;
import org.cougaar.planning.ldm.measure.Volume;

import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.RoleSchedule;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.Task;

import java.util.*;

import org.cougaar.lib.util.UTILPreference;

/**
 * This class contains utility functions for getting
 * Assets.
 */

public class UTILAsset {
  private static String myName = "UTILAsset";
  
  /**
   * Creates an instance of an LDM object from a prototype.
   * An LDM Prototype is like the "idea" of an object.
   *
   * @param ldm           - ldm prototype manager
   * @param prototypeName - name of the prototype, e.g. "DC-10"
   * @param bumperno      - the unique identifier assigned to the returned instance.
   * @return an LDM asset, null if no prototype prototypeName is known
   */
  public static Asset createInstance(LDMServesPlugin ldm, String prototypeName, String bumperno){
    Asset instance = ldm.getFactory().createInstance (prototypeName, bumperno);
    return instance;
  }

  /** 
   * Create empty asset group
   */
  public static final AssetGroup makeAssetGroup(RootFactory root, String id){
    AssetGroup ag = makeAssetGroup (root, new Vector ());
    ((NewItemIdentificationPG)
     ag.getItemIdentificationPG()).setItemIdentification(id);
    return ag;
  }

  /**
   * Given a vector of objects, it creates an asset group
   * containing those objects.  
   *
   * @param  ldm can be gotten from the getLDM() method inherited from PluginAdapter.
   * @param  v a Vector of assets to be grouped
   * @return AssetGroup
   */
  public static final AssetGroup makeAssetGroup(RootFactory root, Vector v){
    AssetGroup ag = null;
    try{
      NewTypeIdentificationPG p1 = null;

      ag = (AssetGroup) root.createAsset(Class.forName ("org.cougaar.planning.ldm.asset.AssetGroup"));

      p1 = (NewTypeIdentificationPG)ag.getTypeIdentificationPG();
      p1.setTypeIdentification("ASSET_GROUP");
      p1.setNomenclature("AssetGroup");
    }
    catch(Exception e){
      throw new UTILRuntimeException(e.getMessage());
    }
    ag.setAssets(v);
    return (ag);
  }

  /**
   * utility methods for accessing TypeIdentification of an asset
   * @param item an asset
   * @return String representing the TypeIdentification of the asset
   */
  
  public static String getTypeName (Asset item) {
    TypeIdentificationPG tip = 
      item.getTypeIdentificationPG ();
    
    String typeName;
    if (tip != null)
      try {
	typeName = tip.getNomenclature ();
      } catch (Exception e) {
	typeName = item.getClass ().getName ();
      }
    else
      typeName = item.getClass ().getName ();
    
    return typeName;
  }
  
  /**
   * utility methods for accessing ItemIdentification of an asset
   * @param item an asset
   * @return String representing the ItemIdentification of the asset
   */

  public static String getItemName (Asset item) {
    ItemIdentificationPG iip = 
      item.getItemIdentificationPG ();
    
    String itemID;
    if (iip != null)
      try {
	//	itemID = iip.getNomenclature ();
	itemID = iip.getItemIdentification ();
      } catch (Exception e) {
	itemID = "<unknown>";
      }
    else
      itemID = "<unknown>";
    
    return itemID;
  }

  protected static int latestUID = 0;
  protected static int getLatestUID () {
    if (latestUID == Integer.MAX_VALUE)
      latestUID = 0;
    return ++latestUID;
  }

    /**
     * Utility function to determine if an asset is or contains
     * an AssetGroup at any level of its hierarchy.
     * @param asset the asset.
     * @return true iff the asset is or contains an AssetGroup
     **/
    public static boolean containsAssetGroup(Asset asset){
	if(asset instanceof AssetGroup)
	    return true;
	else if(asset instanceof AggregateAsset){
	    AggregateAsset aggregate = (AggregateAsset) asset;
	    Asset subobject = (Asset)aggregate.getAsset();
	    return containsAssetGroup(subobject);
	}
	return false;
    }

  /**
   * Break up an asset group into individual assets. This
   * is a utility wrapper around a private function
   * to make the input/output nicer to deal with.
   *
   * NOTE : Convoys are AssetGroups, so the contents of a convoy
   * will also appear on the result list.
   *
   * @param group AssetGroup to divide
   * @return Vector of sub-objects
   */
  public static Vector expandAssetGroup(AssetGroup group) {
    Vector buffer = new Vector();
    expandAssetGroup(group, buffer);
    return buffer;
  }
  
  /**
   * Private utility function to break up an asset group
   * recursively.  Does not expand Aggregate assets.
   *
   * NOTE : Convoys are AssetGroups, so the contents of a convoy
   * will also appear on the result list.
   *
   * @param group - the asset group
   * @param buffer - the vector to put the assets into
   * @return void
   */
  private static void expandAssetGroup(AssetGroup group, Vector buffer){
    Vector subobjects = group.getAssets();
    if (subobjects.isEmpty ())
      System.out.println ("WARNING -- asset group is empty!");
    for (int i=0; i<subobjects.size(); i++) {
      Object subobject = subobjects.elementAt(i);

      if (subobject instanceof AssetGroup)
	expandAssetGroup((AssetGroup)subobject, buffer);
      else
	buffer.addElement(subobject);
    }
  }

  /**
   * Looks for Allocation (is this correct?) plan element that contains
   * a task immediately previous (check_backwards is true) or after (false)
   * the time given in the RoleSchedule.
   * Do we need to check the role in this allocation as well?
   * @returns null if no task found
   */
  public static Task findClosestRoleScheduleTask(RoleSchedule rs, Date time, 
						 boolean check_backwards) {
    // !!FIXIT!! What we really want is the entire role schedule as an
    // OrderedSet by PE start time.
    // Since we're doing forward planning, 'now' should be earlier than
    // all plan elements (i.e. start_of_time_window could just new Date())
    // but let's be safe
    //    Date start_of_time_window = new Date(0); // The beginning of time
    long start_of_time_window = 0; // The beginning of time
    // And pick an arbitrary time far in the future
    //    Date end_of_time_window = new Date((long)((new Date()).getTime() * 2));
    long end_of_time_window = ((new Date()).getTime() * 2);
    Collection pes = rs.getEncapsulatedRoleSchedule(start_of_time_window, 
						    end_of_time_window);

    PlanElement pe_to_return = null;
    
    Iterator pe_i = pes.iterator();
    // Are there any JGL tricks that would make this more efficient?
    while (pe_i.hasNext()) {
      PlanElement next_pe = (PlanElement) pe_i.next();
      // Is all we care about allocations?
      if (next_pe instanceof Allocation) {
	// Process it!  Check if it's the PE that we want- we want to make
	// sure that there's a clear space in the schedule either from
	// (if check_backwards is true) or to (false) the given time
	if (UTILPreference.getReadyAt(next_pe.getTask()).before(time))
	  pe_to_return = next_pe;
	// If we're going backwards from the time, get the one immediately previous
	else if (check_backwards) 
	  break;
	// If we're going forwards, get the one immediately subsequent
	else if (!check_backwards) {
	  pe_to_return = next_pe;
	  break;
	}
      }
    }
    
    if (pe_to_return == null)
      return null;
    
    return pe_to_return.getTask();
  }
}
