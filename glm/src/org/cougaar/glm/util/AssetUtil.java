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

package org.cougaar.glm.util;

import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.PlanningFactory;

import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AssetGroup;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.planning.ldm.asset.PropertyGroup;

import org.cougaar.planning.ldm.measure.Area;
import org.cougaar.planning.ldm.measure.Distance;
import org.cougaar.planning.ldm.measure.Mass;
import org.cougaar.planning.ldm.measure.Volume;


import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.RoleSchedule;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.Task;

import org.cougaar.util.TimeSpan;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.GLMAsset;
import org.cougaar.glm.ldm.asset.Ammunition;
import org.cougaar.glm.ldm.asset.CargoShip;
import org.cougaar.glm.ldm.asset.Consumable;
import org.cougaar.glm.ldm.asset.Container;
import org.cougaar.glm.ldm.asset.Convoy;
import org.cougaar.glm.ldm.asset.GroundVehiclePG;
import org.cougaar.glm.ldm.asset.MovabilityPG;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.Person;
import org.cougaar.glm.ldm.asset.PhysicalPG;
import org.cougaar.glm.ldm.asset.PositionPG;
import org.cougaar.glm.ldm.asset.Repairable;
import org.cougaar.glm.ldm.asset.TransportationNode;
import org.cougaar.glm.ldm.asset.TransportationRoute;

import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NewQuantityScheduleElement;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;

import org.cougaar.glm.GLMConst;
import org.cougaar.glm.util.GLMPreference;


import org.cougaar.lib.util.UTILAsset;
import org.cougaar.lib.util.UTILPluginException;
import org.cougaar.lib.util.UTILRuntimeException;
import org.cougaar.util.log.Logger;

import java.util.*;


/**
 * This class contains utility functions for getting
 * Assets.
 */

public class AssetUtil extends UTILAsset {
  private static String myName = "AssetUtil";
  private static final long BTWN_AVAIL_TOLERANCE = 3600000l;
  private static double MAX_CUBIC_FT = 1.0d;
  private static double MAX_SQ_FT = 1.0d;
  public static final Role CARGO_PORT_ROLE = GLMConst.GENERIC_SEA_PORT;
  public static final Role AMMO_PORT_ROLE = GLMConst.AMMUNITION_SEA_PORT;
  public static final Role AMMO_PORT_ROLE_ALT = GLMConst.AMMO_SEA_PORT;

  public AssetUtil (Logger logger) { 
    super (logger); 
    glmPrepHelper = new GLMPrepPhrase (logger);
    glmPrefHelper = new GLMPreference (logger);
    measureHelper = new GLMMeasure    (logger);
  }

  public final Convoy makeConvoy(PlanningFactory root, String uniqueID){
    Convoy convoy = null;
    try{
      NewTypeIdentificationPG p1 = null;

      convoy = (Convoy) root.createAsset(Class.forName ("org.cougaar.glm.ldm.asset.Convoy"));
      p1 = (NewTypeIdentificationPG)convoy.getTypeIdentificationPG();
      p1.setTypeIdentification("TOPS_CONVOY");
      p1.setNomenclature("Convoy");
      ((NewItemIdentificationPG)
       convoy.getItemIdentificationPG()).setItemIdentification(uniqueID);
    }
    catch(Exception e){
      throw new UTILRuntimeException(e.getMessage());
    }
    return (convoy);
  }

  /**
   * These control how big something must be before it is made into
   * distinct assets instead of remaining within an aggregate asset.
   */

  public void setMaxCubicFt (double max) { MAX_CUBIC_FT = max; }
  public void setMaxSqFt    (double max) { MAX_SQ_FT = max; }

  /** 
   * Utility method for finding organization/cluster assets. 
   * @param assets the Enumeration of assets received from the asset container
   * @param desiredRole a string describing the capable role of an organization
   *        the string is defined in the mycluster-prototype-ini.dat file in 
   *        the topsconfig directory.
   * @return Organization that has the role described in desiredRole 
   */
  public Organization getOrgAsst(Iterator assets, String desiredRole){
    while (assets.hasNext()){
      Asset resource = (Asset)assets.next();
      if (resource instanceof Organization) {
	Organization org = (Organization)resource;
	if (org.isSelf()){
	  RelationshipSchedule schedule = org.getRelationshipSchedule();

	  Collection orgCollection = 
	    schedule.getMatchingRelationships(Role.getRole(desiredRole));
	  if (!orgCollection.isEmpty()) {
	    Relationship rel = (Relationship)orgCollection.iterator().next();
	    Organization o = (Organization)schedule.getOther(rel);
	    return o;
	  }
	}
      }
    }
    return null;
  }

  /** 
   * Utility method for finding organization/cluster assets. 
   * @param assets the Enumeration of assets received from the asset container
   * @param desiredRole a string describing the capable role of an organization
   *        the string is defined in the mycluster-prototype-ini.dat file in 
   *        the topsconfig directory.
   * @return Organization that has the role described in desiredRole 
   */
  public List getOrgAssts(Iterator assets, String desiredRole){
    ArrayList orgs = new ArrayList();
    while (assets.hasNext()){
      Asset resource = (Asset)assets.next();
      if (resource instanceof Organization) {
	Organization org = (Organization)resource;
	if (org.isSelf()){
	  RelationshipSchedule schedule = org.getRelationshipSchedule();

	  Collection orgCollection = 
	    schedule.getMatchingRelationships(Role.getRole(desiredRole));
	  if (!orgCollection.isEmpty()) {
	    for (Iterator i = orgCollection.iterator(); i.hasNext();){
	      Relationship rel = (Relationship)i.next();
	      Organization o = (Organization)schedule.getOther(rel);
	      orgs.add(o);

	    }
	    return orgs;
	  }
	}
      }
    }
    return null;
  }


  /**
   * get the GeolocoLocation of an organization
   * @param o the organization
   */
  public GeolocLocation getOrgLocation(Organization o) {
    GeolocLocation geoloc;

    try {
      geoloc = (GeolocLocation) o.getMilitaryOrgPG().getHomeLocation();
    } catch (NullPointerException e) {
      throw new UTILPluginException("no military org property for organization"
				    + " (" + o + ")");
    }

    return geoloc;
  }

  /**
   * creates a HashMap of organizations with their geolocCodes as keys.
   * @param organizations the enum of organizations to be put in the hashmap
   * @return HashMap the newly constructed table with geolocCodes as keys,
   *         and the corresponding organization as object.
   */

  public HashMap orgAssetLocation (List organizations) {
    HashMap orgAssetTable = new HashMap();
    //    while (organizations.hasMoreElements()){
    for (Iterator iterator = organizations.iterator(); iterator.hasNext();){
      Organization org = (Organization)iterator.next();
      String geolocCode = getOrgLocation(org).getGeolocCode();
      List orgsAtGeoLoc;
      if ((orgsAtGeoLoc = (List) orgAssetTable.get(geolocCode)) == null) {
	orgsAtGeoLoc = new ArrayList ();
	orgAssetTable.put(geolocCode, orgsAtGeoLoc);
      }
      orgsAtGeoLoc.add (org);
    }
    return orgAssetTable;
  }

  /**
   * Finds the organization with the given geolocCode in the given table.
   * @param table the table to search
   * @param geolocCode the string to search for
   * @return Organization the organization with the geolocCode.
   */

  public Organization getOrg (HashMap table, String geolocCode) {
    return ((Organization)((List)table.get(geolocCode)).get(0));
  }

  public List getOrgList (HashMap table, String geolocCode) {
    return ((List)table.get(geolocCode));
  }

  public boolean hasOrg (HashMap table, String geolocCode) {
    return (getOrgList (table, geolocCode) != null);
  }

  /**
   * This call gets oneself as an organization asset.
   * @param clusterAssets the Enum of assets received from the asset container
   * @return Organization which represents current cluster
   */
  public Organization getSelf(Enumeration clusterAssets){
    Organization myself = null;
    while(clusterAssets.hasMoreElements()){
      Asset a = (Asset)clusterAssets.nextElement();
      if ((a instanceof Organization) &&
          (((Organization)a).isSelf())) {
        myself = (Organization)a;
        break;
      }
    }
    if(myself == null){
      throw new UTILPluginException("can't find myself as clusterAsset");
    }
    return myself;
  }


  /**
   * check to see if an asset represents a consumable
   * @param asset the asset to check
   * @return boolean
   */
  public boolean isConsumable(Asset asset){
    if(asset instanceof AggregateAsset){
      return isConsumable(((AggregateAsset)asset).getAsset());
    }
    return (asset instanceof Consumable);
  }
  
  /**
   * check to see if an asset represents a repairable
   * @param asset the asset to check
   * @return boolean
   */
  public boolean isRepairable(Asset asset){
    if(asset instanceof AggregateAsset){
      return isRepairable(((AggregateAsset)asset).getAsset());
    }
    return (asset instanceof Repairable);
  }

  /**
   * check to see if an asset represents a passenger
   * @param asset the asset to check
   * @return boolean
   */ 
  public boolean isPassenger(Asset asset) {
    if (asset instanceof AggregateAsset) {
      return isPassenger(((AggregateAsset)asset).getAsset());
    }
    else if (asset instanceof Person) {
      return true;
    }

    TypeIdentificationPG typeofasset = null;
    typeofasset = asset.getTypeIdentificationPG();
    if (typeofasset == null) {
      throw new UTILPluginException("bad type identification property: \"" +
				    asset + "\"");
    }
    String nom = typeofasset.getTypeIdentification();
    if (nom == null) {
      throw new UTILPluginException("bad type identification: \"" +
				    asset + "\"");
    }
    return(nom.equals("OTHER/Passenger") ||
	   nom.equals("OTHER/People")||
	   nom.equals("OTHER/person"));
  }

  /**
   * check to see if an asset represents a Pallet
   * @param asset the asset to check
   * @return boolean
   */ 
  public boolean isPallet(Asset asset) {
    if (asset instanceof AggregateAsset) {
      return isPallet(((AggregateAsset)asset).getAsset());
    } 
    
    if ((asset instanceof Container) || (asset instanceof org.cougaar.glm.ldm.asset.Package)){
      TypeIdentificationPG typeofasset = null;
      typeofasset = asset.getTypeIdentificationPG();
      if (typeofasset == null) {
	throw new UTILPluginException("bad type identification property: \"" +
				      asset + "\"");
      }
      String nom = typeofasset.getTypeIdentification();
      if (nom == null) {
	throw new UTILPluginException("bad type identification: \"" +
				      asset + "\"");
      }
      if (nom.equals ("TOPS_PALLET"))
	throw new UTILPluginException("AssetUtil.isPallet - found TOPS_PALLET.  " + 
				      "Please use PALLET from Container protos instead!");
      //!!!Hack for AEF...until we figure out the TypeIdentification/Nomenclature problem on AEF's end.
      if ( (nom.indexOf("463L") != -1))
	return true;
      return(nom.equals("OTHER/Air_Pallet") ||
	     nom.equals("PALLET") ||
	     nom.equals("463L PLT") );
    }
    return false;
  }
  /**
   * check to see if an asset represents ammo
   *
   * works with an aggregate asset too.
   *
   * Something is ammo it's an instance of org.cougaar.glm.ldm.asset.Ammunition OR
   * The type identification PG's nomenclature is Ammunition
   *
   * @see org.cougaar.glm.ldm.asset.Ammunition
   * @param asset the asset to check
   * @return boolean
   */
  public boolean isAmmo(Asset asset) {

    if (asset instanceof AssetGroup) {
      Vector assetVector = ((AssetGroup)asset).getAssets();
      boolean ammo = false;
      for(int i = 0; i < assetVector.size(); i++) {
	boolean currentAsset = isAmmo((Asset)assetVector.elementAt(i));
	if (((ammo == true) && (currentAsset == false)) ||
	    ((ammo == false && i > 0) && (currentAsset == true))) {
	  throw new UTILRuntimeException("Couldn't handle AssetGroup containing Ammo and non-Ammo!");
        }
	ammo = currentAsset;
      }
    }
    boolean ammo = false;

    if (asset instanceof AggregateAsset)
      return isAmmo(((AggregateAsset)asset).getAsset());

    if (asset instanceof Ammunition)
      return true;

    if (asset instanceof AssetGroup) {
      Vector vector = (Vector)((AssetGroup)asset).getAssets();
      return isAmmo((Asset)vector.elementAt(0));
    }

    MovabilityPG mpg = 
      (MovabilityPG) ((GLMAsset)asset).getMovabilityPG();
    if(mpg != null){
      String code = mpg.getCargoCategoryCode();
      if(code != null && CargoCategoryDecoder.isAmmo(code))
	return true;
    }

    TypeIdentificationPG typeofasset = null;
    typeofasset = asset.getTypeIdentificationPG();
    if (typeofasset == null) {
      throw new UTILPluginException("bad type identification property: \"" +
				    asset + "\"");
    }
    String nom = typeofasset.getTypeIdentification();
    if (nom == null) {
      throw new UTILPluginException("bad type identification: \"" + asset + "\"");
    }
    return (nom.equals("Ammunition"));
  }

  /**
   * <pre>
   * See if a task is a prepo task (i.e. 
   * already packed in a ship hanging arround
   * in the middle of the ocean somewhere).
   *
   * The check is made to see if the task has a WITH
   * preposition.
   * We look up the ship referred to by the String (Ship ID) 
   * in GlobalSea, where the ships are owned.
   *
   * </pre>
   * @param t task to check
   * @return boolean
   */
  public boolean isPrepoTask(Task t){
    if(glmPrepHelper.hasPrepNamed(t, Constants.Preposition.WITH)){
      Object o = glmPrepHelper.getIndirectObject(t, Constants.Preposition.WITH);
      return o instanceof CargoShip || o instanceof String;
    }
    return false;
  }

  /**
   * <pre>
   * convert an AssetGroup/AggregateAsset into it's component assets,
   * recursing through any contained AssetGroup/AggregateAssets as
   * needed.
   *
   * Note : Convoys are AssetGroups, so the contents of the convoy
   * will also appear on the result list.
   *
   * Will take an aggregate asset and create new instances that are
   * copies of the asset of the aggregation.  These will have
   * item id's like "xxx-7_of_9_from_yyy", where xxx is the original id, 
   * the quantity of the aggregate asset is 9, and yyy is the number of times
   * this method has been called.  Yes, you might ask, why not use the
   * aggregate's UID?  Well this seems to be null.  Why not the agg's asset? 
   * Well, this is just a prototype, so two different aggregations will
   * have the same asset, giving us the same UID.
   *
   * This allows the receipt of two aggregates of the same type of object
   * creating distinct subobjects.  Otherwise a M1A1-7_of_9 from one aggregate
   * would be equals() and hashcode() equal to a M1A1-7_of_9 from another.  
   * This is needed since now Assets are equal on the basis of type and item
   * PG equality.  (01/23/99 GWFV)
   * 
   * Will not break up aggregate
   * assets where the items are smaller than one cubic foot.
   *
   * </pre>
   * @param asset AssetGroup/AggreateAsset to divide
   * @return Vector of sub-objects
   */
  public Vector ExpandAsset(PlanningFactory theFactory, Asset asset) {
    Vector retval = new Vector();

    if (asset instanceof AssetGroup) {
      AssetGroup group = (AssetGroup)asset;
      Vector subobjects = group.getAssets();

      for (int i=0; i<subobjects.size(); i++) {
	Object subobject = subobjects.elementAt(i);
	if (subobject instanceof AssetGroup ||
	    subobject instanceof AggregateAsset) {
	  Vector moreboxes = ExpandAsset(theFactory, (Asset)subobject);
	  for (int j = 0; j < moreboxes.size(); j++)
	    retval.addElement(moreboxes.elementAt(j));
	} 
	else{
	  retval.addElement(subobject);
	}
      }
    } 
    else if (asset instanceof AggregateAsset) {
      AggregateAsset aggregate = (AggregateAsset) asset;

      long count = aggregate.getQuantity();
      Asset subobject = (Asset)aggregate.getAsset();
      if (subobject instanceof AggregateAsset ||
	  subobject instanceof AssetGroup) {
	for (long i=0; i<count; i++) {
	  Vector evenmoreboxes = ExpandAsset(theFactory, (Asset)subobject);
	  for (int j = 0; j < evenmoreboxes.size(); j++)
	    retval.addElement(evenmoreboxes.elementAt(j));
	} 
      }
      else if (subobject instanceof Asset) {
	boolean tooBig = false;

        PhysicalPG ppg = 
          (PhysicalPG) ((GLMAsset)subobject).getPhysicalPG();
	if (ppg != null) {
	  try {
	    Volume vol  = ppg.getVolume();
	    Area   area = ppg.getFootprintArea();
	  
	    if ((vol.getCubicFeet   () > MAX_CUBIC_FT) || 
		(area.getSquareFeet () > MAX_SQ_FT))
	      tooBig = true;
	  } catch (Exception e) {
	    logger.warn ("AssetUtil.ExpandAsset - ERROR : " +
			 "aggregate asset\n\t" + aggregate + 
			 "\n\thas asset\n\t" +
			 subobject + 
			 "\n\tthat has no physicalPG.");
	  }
	}
	if (tooBig) {
	  synchronized (this) {
	    for (long i=0; i<count; i++) {
	      String name = getTypeName (subobject);
	      if (name.length () > 10)
		name = name.substring (0, 9) + "...";

	      name = name + "-" + 
		(i+1) + "_of_" + 
		count + "(" + 
		getLatestUID () + ")";
	      Asset particle = theFactory.createInstance (subobject, name);
	      Enumeration otherProps = asset.getOtherProperties();
	      while (otherProps.hasMoreElements()) {
		particle.addOtherPropertyGroup((PropertyGroup)otherProps.nextElement());
	      }
	      retval.addElement(particle);
	    }
	  }
	}
	else
	  retval.addElement (asset);
      }
      else {
	logger.error ("AssetUtil.ExpandAsset - ERROR : " +
		      "aggregate asset\n\t" + aggregate + 
		      "\n\thas asset\n\t" +
		      subobject + 
		      "\n\tthat is not an Asset.");
	retval.addElement (asset);
      }
    } 
    else{
      retval.addElement(asset);
    }
    
    return retval;
  }

  protected int latestUID = 0;
  protected synchronized int getLatestUID () {
    if (latestUID == Integer.MAX_VALUE)
      latestUID = 0;
    return ++latestUID;
  }

  /**
   * get the total Area of an asset regardless of whether it's
   * an Asset, AssetGroup, AggregateAsset, or whatever
   * @param asset the asset
   * @return Area the total area of the asset
   */
  public Area totalArea(Asset asset) {
    return totalArea (asset, false);
  }

  /**
   * get the total Area of an asset regardless of whether it's
   * an Asset, AssetGroup, AggregateAsset, or whatever
   * BUT adjusted for the peculiar semantics of asset handling in GlobalSea
   * >Vehicles only have footprint area
   * >Containers only have volume
   * 
   * @param asset the asset
   * @return Area the total area of the asset
   */
  public Area totalAdjustedArea(Asset asset) {
    return totalArea (asset, true);
  }

  public Area totalArea(Asset asset, boolean ignoreContainers) {
    if (asset instanceof AggregateAsset) {
      AggregateAsset aggAsset = (AggregateAsset)asset;
      double qty = aggAsset.getQuantity();

      if (qty <= 0)
	throw new UTILPluginException("got bad qty for Aggregate Asset: \"" + 
				      asset + "\"");

      asset = aggAsset.getAsset();

      if(asset == null)
	throw new UTILPluginException("Got null asset in Aggregate Asset");

      double area = getArea(asset).getSquareFeet();
      return Area.newSquareFeet(qty * area);
    }
    else if (asset instanceof AssetGroup) {
      double d = 0.0d;
      Vector subassets = ((AssetGroup)asset).getAssets();
      for (int i = 0; i < subassets.size (); i++)
	d += totalArea((Asset)subassets.elementAt(i)).getSquareFeet();

      return Area.newSquareFeet(d);
    }

    if (ignoreContainers)
      return isVehicleOrAircraft(asset) ? getArea (asset) : Area.newSquareFeet(0.0d);

    return getArea (asset);
  }

  private Area getArea (Asset asset) {
    PhysicalPG prop = null;
    try{
      prop = (PhysicalPG) ((GLMAsset)asset).getPhysicalPG();
    }
    catch(Exception e){
      throw new UTILPluginException("error getting physical property for asset :\"" +
				    asset + "\"");
    }
    Area area;
    try { area = prop.getFootprintArea(); }
    catch (Exception e) {
      throw new UTILPluginException("No Physical property set for asset: \"" +
				    asset + "\"");
    }
    if (area == null) {
      throw new UTILPluginException("Got null footprint area in asset: \"" +
				    asset + "\"");
    }
    return area;
  }

  /**
   * get the total Volume of an asset
   * ( regardless of whether it's
   * an Asset, AssetGroup, AggregateAsset, or whatever)
   * @param asset the asset
   * @return Volume the total volume of the asset
   */
  public Volume totalVolume(Asset asset) {
    return totalVolume(asset, false);
  }

  /**
   * get the total Volume of an asset
   * ( regardless of whether it's
   * an Asset, AssetGroup, AggregateAsset, or whatever)
   * @param asset the asset
   * @return Volume the total volume of the asset
   */
  public Volume totalAdjustedVolume(Asset asset) {
    return totalVolume(asset, true);
  }

  public Volume totalVolume(Asset asset, boolean ignoreVehiclesAndContainers) {
    if (asset instanceof AggregateAsset) {
      AggregateAsset aggAsset = (AggregateAsset)asset;
      double qty = aggAsset.getQuantity();

      if (qty <= 0)
 	throw new UTILPluginException("got bad qty for Aggregate Asset: \"" + 
 				      asset + "\"");

      asset = aggAsset.getAsset();

      if(asset == null)
 	throw new UTILPluginException("Got null asset in Aggregate Asset");

      double m3perasset = totalVolume(asset,ignoreVehiclesAndContainers).getCubicMeters();
      return Volume.newCubicMeters(qty * m3perasset);
    }
    else if (asset instanceof AssetGroup) {
      double d = 0.0d;
      Vector subassets = ((AssetGroup)asset).getAssets();
      for (int i = 0; i < subassets.size (); i++)
	d += totalVolume((Asset)subassets.elementAt(i),ignoreVehiclesAndContainers).getCubicMeters();

      return Volume.newCubicMeters(d);
    }

    if (ignoreVehiclesAndContainers)
      return (!isVehicleOrAircraft(asset) && !isStandardContainer(asset)) 
	? getVolume (asset) : Volume.newCubicMeters(0.0d);

    return getVolume (asset);
  }

  private Volume getVolume (Asset asset) {
    PhysicalPG prop = null;
    try{ prop = (PhysicalPG) ((GLMAsset)asset).getPhysicalPG(); }
    catch(Exception e){
      throw new UTILPluginException("error getting physical property for asset :\"" +
				    asset + "\"");
    }

    Volume vol;
    try { vol = prop.getVolume(); }
    catch (Exception e) {
      throw new UTILPluginException("No Physical property set for asset: \"" +
				    asset + "\"");
    }
    if (vol == null) {
      throw new UTILPluginException("Got null volume in asset: \"" +
				    asset + "\"");
    }
    return vol;
  }

  /**
   * get the total Mass of an asset
   * ( regardless of whether it's
   * an Asset, AssetGroup, AggregateAsset, or whatever)
   * @param asset the asset
   * @return Mass the total mass of the asset
   */

  public Mass totalMass(Asset asset) {
    if (asset instanceof AggregateAsset) {
      AggregateAsset aggAsset = (AggregateAsset)asset;
      double qty = aggAsset.getQuantity();

      if (qty <= 0)
 	throw new UTILPluginException("got bad qty for Aggregate Asset: \"" + 
 				      asset + "\"");

      asset = aggAsset.getAsset();

      if(asset == null)
 	throw new UTILPluginException("Got null asset in Aggregate Asset");

      double tonsperasset = totalMass(asset).getTons();
      return Mass.newTons(qty * tonsperasset);
    }
    else if (asset instanceof AssetGroup) {
      double d = 0.0d;
      Vector subassets = ((AssetGroup)asset).getAssets();
      for (int i = 0; i < subassets.size (); i++)
	d += totalMass((Asset)subassets.elementAt(i)).getTons();

      return Mass.newTons(d);
    }

    return getMass (asset);
  }

  private Mass getMass (Asset asset) {
    PhysicalPG prop = null;
    try{ prop = (PhysicalPG) ((GLMAsset)asset).getPhysicalPG(); }
    catch(Exception e){
      throw new UTILPluginException("error getting physical property for asset :\"" +
				    asset + "\"");
    }

    Mass mass;
    try { mass = prop.getMass(); }
    catch (Exception e) {
      throw new UTILPluginException("No Physical property set for asset: \"" +
				    asset + "\"");
    }
    if (mass == null) {
      throw new UTILPluginException("Got null mass in asset: \"" +
				    asset + "\"");
    }
    return mass;
  }

  public long totalContainers(Asset asset) {
    if (asset instanceof AggregateAsset) {
      AggregateAsset aggAsset = (AggregateAsset)asset;
      double qty = aggAsset.getQuantity();

      if (qty <= 0)
 	throw new UTILPluginException("got bad qty for Aggregate Asset: \"" + 
 				      asset + "\"");

      asset = aggAsset.getAsset();

      if(asset == null)
 	throw new UTILPluginException("Got null asset in Aggregate Asset");

      if (isStandardContainer(asset)) return (long)qty;
      else return 0l;
    } else if (asset instanceof AssetGroup) {
      long d = 0l;
      Vector subassets = ((AssetGroup)asset).getAssets();
      for (int i = 0; i < subassets.size (); i++)
	if (isStandardContainer((Asset)subassets.elementAt(i))) d++;
      return d;
    }
    return isStandardContainer(asset) ? 1 : 0;
  }  

  /**
   * helper function, gets the total square feet footprint of an asset
   * ( regardless of whether it's
   * an Asset, AssetGroup, AggregateAsset, or whatever)
   * @param a the asset
   * @return double the area of the asset in sqr. ft.
   */
  public double totalSquareFeet(Asset asset) {
    double area = totalArea(asset).getSquareFeet();
    if(area < 0){
      throw new UTILPluginException("Got bad area in asset: \"" +
 				    asset + "\"");
    }
    return area;
  }

  /**
   * helper function, gets the total volume of an asset
   * ( regardless of whether it's
   * an Asset, AssetGroup, AggregateAsset, or whatever)
   * @param a the asset
   * @return double the volume of the asset in cubic meters
   */
  public double totalCubicMeters(Asset asset) {
    double vol = totalVolume(asset).getCubicMeters();
    if(vol < 0){
      throw new UTILPluginException("Got bad volume in asset: \"" +
				    asset + "\"");
    }
    return vol;
  }

  /**
   * helper function, gets the total mass of an asset
   * ( regardless of whether it's
   * an Asset, AssetGroup, AggregateAsset, or whatever)
   * @param a the asset
   * @return double the mass of the asset in cubic meters
   */
  public double totalTons(Asset asset) {
    double mass = totalMass(asset).getTons();
    if(mass < 0){
      throw new UTILPluginException("Got bad weight in asset: \"" +
				    asset + "\"");
    }
    return mass;
  }

  /** 
   * Necessary for accurate ship location computation.
   * NOTE that travel isn't taken into account in that if the time specified
   * is in the middle of the time when the asset is traveling between tasks, 
   * the position reported will be the end of the last task.  This is as intended.
   *
   * If the time specified is in the middle of a task, we interpolate. (!!FIXIT!!)
   * 
   * @return GeolocLocation representing the current position of the asset
   *    at the time specified.  returns null if none found
   */
  public GeolocLocation getMostRecentKnownPosition(Asset a, Date time) {
    RoleSchedule rs = a.getRoleSchedule();

    // The task immediately previous (possibly including) now
    Task most_recent_task = findClosestRoleScheduleTask(rs, time, 
							true/*previous tasks*/);

    if (most_recent_task == null) {

      //        System.err.println("AssetUtil.getCurrentPosition for asset " + 
      //  			 a.getUID() + " at time " + time + 
      //  			 " found no previous tasks");
      return null;
    }
        
    if (prefHelper.getLateDate(most_recent_task).after(time)) 
      return glmPrepHelper.getFromLocation(most_recent_task);
    /*
      {
      // If the last one overlaps the time, we need to interpolate
      GeolocLocation start = glmPrepHelper.getFromLocation(most_recent_task);
      GeolocLocation end = glmPrepHelper.getToLocation(most_recent_task);      
      }
    */

    return glmPrepHelper.getToLocation(most_recent_task);
  }

  /**
   * Check to see if the task fits in the role schedule of the asset.
   * DON'T perform any allocation, assignment, etc. just check for 
   * enough space.
   * FOR NOW, (!!FIXIT!!) we're assuming the ship is EMPTY unless 
   * fully allocated, i.e. you have to manage the loading yourself 
   * without actually adding something on the ship until you add 
   * everything in one big block.  You can actually get into the guts
   * of the asset and check the role schedule for yourself if you really
   * want to, but it's kind of ugly.
   * 
   * There are two possible legal cases (i.e. no nulls or other errors) in
   * which this method will return false:  the role schedule doesn't have
   * availability for a task, or the role schedule already has plan elements
   * in the time window where we want the task to fit that make the schedule
   * unfeasible.
   */
  public boolean checkTaskAgainstRoleSchedule(Asset a, Task t) {
    boolean taskOK = false;

    GeolocLocation t_start_loc = (GeolocLocation)
      t.getPrepositionalPhrase(Constants.Preposition.FROM).getIndirectObject();
    // We assume the VIA is irrelevant
    GeolocLocation t_end_loc = (GeolocLocation)
      t.getPrepositionalPhrase(Constants.Preposition.TO).getIndirectObject();
    
    Date t_start_time = prefHelper.getReadyAt(t);
    Date t_end_time = prefHelper.getLateDate(t);
    
    // We need to make sure a) the asset is available in this time window
    // b) the asset can get to the start location by the start time and
    // c) the asset can get to its next commitment after the end time 
    RoleSchedule a_rs = a.getRoleSchedule();
	
    // Note that these aren't really the start and end of the travel of the
    // asset, they're the tasks that the asset can start travel at the EARLIEST
    // and the date by which the asset must at the LATEST be somewhere else.
    Task t_start_trvl_task = 
      findClosestRoleScheduleTask(a_rs, t_start_time, true); // Check earlier
    Task t_end_trvl_task =  
      findClosestRoleScheduleTask(a_rs, t_end_time, false); // Check later

    // First check is to make sure the transport asset for this mission can
    // get where it needs to be on time (NOTE - this prefers the status quo,
    // i.e. tasks already allocated get preference.  is that ok?)
    // Should we be robust for nulls here?
    Distance start_d = 
      measureHelper.distanceBetween((GeolocLocation)
				    t_start_trvl_task.getPrepositionalPhrase(Constants.Preposition.TO).getIndirectObject(),
				    t_start_loc);
    //     Date t_start_trvl = new Date(t_start_time.getTime() -
    // 				 travelTimeUsingAsset(start_d,a));

    long t_start_trvl = (t_start_time.getTime() - travelTimeUsingAsset(start_d,a));

    //    if (t_start_trvl.before(prefHelper.getLateDate(t_start_trvl_task))) {
    if (t_start_trvl < (prefHelper.getLateDate(t_start_trvl_task).getTime())) {
      //System.err.println("Could not get to start in time using this mission");
      taskOK = false;
      return taskOK;
    }

    Distance end_d = measureHelper.distanceBetween(t_end_loc,
						   (GeolocLocation)t_end_trvl_task.getPrepositionalPhrase(Constants.Preposition.FROM).getIndirectObject());
    //     Date t_end_trvl = new Date(t_end_time.getTime() +
    // 			       travelTimeUsingAsset(end_d,a));
    long t_end_trvl = (t_end_time.getTime() + travelTimeUsingAsset(end_d,a));

    //    if (prefHelper.getReadyAt(t_end_trvl_task).before(t_end_trvl)) {
    if (prefHelper.getReadyAt(t_end_trvl_task).getTime() < (t_end_trvl)) {
      //System.err.println("Could not get to next task in time using this mission");
      taskOK = false;
      return taskOK;
    }
    
    
    // get a container of plan elements that have dates in the
    // given range
    Collection rs_avail_elts = 
      a_rs.getOverlappingRoleSchedule(t_start_trvl, t_end_trvl);
    
    // if the above query returns anything, that means that we can't insert
    // the task because it would overlap an already-existing block
    if (rs_avail_elts.size() != 0) {
      taskOK = false;
      return taskOK;
    }

    // make sure we have availability throughout the time range of the task
    Collection avail_s = 
      a_rs.getAvailableSchedule().getOverlappingScheduleElements(t_start_trvl, 
								 t_end_trvl);
    
    Iterator avail_iter = avail_s.iterator();

    //    Date last_date_checked = t_start_trvl;
    long last_date_checked = t_start_trvl;
    while (avail_iter.hasNext()) {
      ScheduleElement next_se = (ScheduleElement) avail_iter.next();
      
      if (!(next_se.included(last_date_checked))) {
	taskOK = false;
	return taskOK;
      }
      if (next_se.included(t_end_trvl)) {
	taskOK = true;
	return taskOK;
      } else {
	// 	last_date_checked = new Date(next_se.getEndDate().getTime() + 
	// 				     BTWN_AVAIL_TOLERANCE);
	last_date_checked = next_se.getEndDate().getTime() + BTWN_AVAIL_TOLERANCE;
      }
    }
    
    return taskOK;
  }

  // !!FIXIT!! Hack!  
  /**
   * Utility method to return the approximate time the given asset requires
   * to go Distance d
   */
  private long travelTimeUsingAsset(Distance d, Asset a) {
    // 100 thousand seconds, or a little over a day (27.77... hours)
    return 100000000l;
  }
  
  /**
   * Looks for Allocation (is this correct?) plan element that contains
   * a task immediately previous (check_backwards is true) or after (false)
   * the time given in the RoleSchedule.
   * Do we need to check the role in this allocation as well?
   * @return null if no task found
   */
  /** 
   * Inefficient...
   * @return string that lists the nodes in the route 
   */
  public String getNodeNames (TransportationRoute route) {
    String nodes = "";
    for (Iterator i = route.getNodes().iterator(); i.hasNext ();) {
      nodes = nodes + " " + ((TransportationNode) i.next()).getDescription();
    }
    return nodes;
  }

  /**
   * Determines if the asset passed in is a standard container or not
   */
  public boolean isStandardContainer(Asset a)
  {
    return (a instanceof Container);
  }

  /**
   * Determines if the asset passed in is a vehicle (for purposes of ship 
   * loading) or not.
   **/
  public boolean isVehicle(Asset a){
    MovabilityPG mpg = 
      (MovabilityPG) ((GLMAsset)a).getMovabilityPG();
    if(mpg == null)
      return false;
    String code = mpg.getCargoCategoryCode();

    boolean retval = false;
	  
    try {
      retval = CargoCategoryDecoder.isRORO(code);
    } catch (NullPointerException npe) {
      throw new UTILRuntimeException("GMLAsset.isVehicle - asset has movability PG but " + 
				     "cargo category code is NOT set.\nMovability PG was :" + mpg + 
				     "\nAsset was " + a);
    }
	  
    return retval;
  }

  /**
   * Determines if the asset passed in is a vehicle (for purposes of ship 
   * loading) or not OR is a crated aircraft.
   * 
   * These items are packed on ship by area.
   **/
  public boolean isVehicleOrAircraft(Asset a){
    MovabilityPG mpg = 
      (MovabilityPG) ((GLMAsset)a).getMovabilityPG();
    if(mpg == null)
      return false;
    String code = mpg.getCargoCategoryCode();
    boolean isAircraft = (code.toUpperCase().charAt(0) == 'B');
    return (isAircraft || CargoCategoryDecoder.isRORO(code));
  }

  /**
   * Utility method - find the organization corresponding to the port at 
   * the given location
   * @param loc he location
   * @param ports_enum a list of ports to search: probably obtained by a call to
   * getOrganizationAssets()
   * @param myClusterName used for logger.isDebugEnabled()()ging purposes.
   * @return the port that matches the location.
   */
  public Organization findPortOrg(GeolocLocation loc, 
                                         Enumeration ports_enum,
                                         String myClusterName) {
    Set ports = new HashSet();
    while (ports_enum.hasMoreElements()) {
      Organization org = (Organization)ports_enum.nextElement();
      RelationshipSchedule sched = org.getRelationshipSchedule();
      
      Collection portRelationships;
      
      //BOZO - Should be looking at self org for providers. Don't have self org so 
      //will look to see if org has any port customers.
      if (!((portRelationships = 
	     sched.getMatchingRelationships(GLMConst.THEATER_SEA_PROVIDER.getConverse(),
					    TimeSpan.MIN_VALUE,
					    TimeSpan.MAX_VALUE)).isEmpty()) ||
          !((portRelationships = 
	     sched.getMatchingRelationships(GLMConst.GENERIC_SEA_PORT.getConverse(),
					    TimeSpan.MIN_VALUE,
					    TimeSpan.MAX_VALUE)).isEmpty()) ||
          !((portRelationships = 
	     sched.getMatchingRelationships(GLMConst.AMMUNITION_SEA_PORT.getConverse(),
					    TimeSpan.MIN_VALUE,
					    TimeSpan.MAX_VALUE)).isEmpty())) {
        for (Iterator iterator = portRelationships.iterator();
             iterator.hasNext();) {
          Relationship relationship = (Relationship) iterator.next();
          ports.add(sched.getOther(relationship));
        }
      }
    }
    
    Organization foundOrg = measureHelper.bestOrg(loc, ports, myClusterName);
    return foundOrg;
  }
  
  /** 
   * test if port is an ammo port
   * @param p Organization to be tested
   * @return true if is an ammo port
   */
  public boolean isAmmoPort(Asset p) {
    if (p instanceof Organization) {
      RelationshipSchedule schedule = 
        ((Organization)p).getRelationshipSchedule();
      //      return (!(schedule.getMatchingRelationships(AMMO_PORT_ROLE).isEmpty())) ||
      //     (!(schedule.getMatchingRelationships(AMMO_PORT_ROLE_ALT).isEmpty()));	
      return (!(schedule.getMatchingRelationships(AMMO_PORT_ROLE.getConverse()).isEmpty()) ||
	      (!(schedule.getMatchingRelationships(AMMO_PORT_ROLE_ALT.getConverse()).isEmpty())));
    } else {
      return false;
    }
  }

  /** 
   * test if organization is a non-ammo port
   * @param p Organization to be tested
   * @return true if it is a generic port
   */
  public boolean isCargoPort(Asset p) {
    if (p instanceof Organization) {
      RelationshipSchedule schedule = ((Organization)p).getRelationshipSchedule(); 
      //return (schedule.getMatchingRelationships(CARGO_PORT_ROLE).size() > 0);
      return (!schedule.getMatchingRelationships(CARGO_PORT_ROLE.getConverse()).isEmpty());
    }         
    return false;
  }

  public String getUniqueTag(Asset a) {
    return new String(a.getTypeIdentificationPG().getTypeIdentification()+
		      a.getItemIdentificationPG().getItemIdentification());
  }

  GLMPrepPhrase glmPrepHelper;
  GLMPreference glmPrefHelper;
  GLMMeasure measureHelper;
}

