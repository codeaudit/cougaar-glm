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

package org.cougaar.mlm.plugin.ldm;

import org.cougaar.planning.ldm.PrototypeProvider;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.asset.AssetFactory;
import org.cougaar.planning.ldm.plan.NewScheduleElement;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.glm.ldm.plan.NewQuantityScheduleElement;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.ItemIdentificationPGImpl;
import org.cougaar.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.ldm.PlanningFactory;

import org.cougaar.util.UnaryPredicate;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/** A QueryHandler which can convert a TypeIdentification code
 * into an Asset Prototype.
 **/

public class SQLAssetPrototypeProvider 
  //extends QueryHandler
  extends org.cougaar.mlm.plugin.ldm.PrototypeProvider 
{

    private AssetFactory theAssetFactory = null;
	
  public SQLAssetPrototypeProvider() {
      theAssetFactory = new AssetFactory();
  }

  /** Called to decide if this QueryHandler can provide
   * type resolution for this type id code.
   **/
  public boolean canHandle(String typeid) { 
		if ( !(NSNtoAssetClass( typeid ).equals( "ClassVIIMajorEndItem" )) )
			 return true; 
		return false;
  }

  /** Turn a typeid into an Asset Prototype, presumably
   * by looking it up in a database.
   **/
  public Asset getAssetPrototype(String typeid) {
    //public Asset getPrototype( String typeid ) {
    PlanningFactory ldmfactory = getLDM().getFactory();
        
    String protoname = typeid;
        
    try {
      //try {
	//if (!ldmfactory.isRegistered(protoname)) {
	//if (!getLDM().isPrototypeCached(protoname)) {
	String assetClass = NSNtoAssetClass(typeid);
	//System.err.println( "Trying to register prototype " + assetClass );
	Asset tmp_asset = (Asset)theAssetFactory.create( Class.forName( "org.cougaar.planning.ldm.asset." + assetClass ), typeid );
	//System.err.println( "\ncachePrototype( " + protoname + "," + tmp_asset + ")" );
	getLDM().cachePrototype( protoname, tmp_asset );
	//System.err.println("Registered proto for "+assetClass);
	//}
	//} catch (org.cougaar.data.RegistryException ex) {
	//System.out.println("warning: prototype name already in registry \n");
	//}
          
	//System.err.println( "\nGetting prototype " + protoname );
      Asset pr = (Asset) getLDM().getPrototype( protoname );
      NewTypeIdentificationPG p1 = (NewTypeIdentificationPG)pr.getTypeIdentificationPG();
      String tmptypeid = p1.getTypeIdentification();
      //System.out.println("\n *****AssetPrototypeProvider typeid is: " + tmptypeid);
      p1.setTypeIdentification( typeid );
      p1.setNomenclature( typeid );
      //System.err.println( "\nNOW, typeid = " + pr.getTypeIdentificationPG().getTypeIdentification() );
      p1.setAlternateTypeIdentification( typeid );
      
      System.err.println("Created PrototypeAsset for "+typeid+" = "+pr);

      myLDMPlugin.fillProperties(pr);

      return pr;
    } catch (Exception ee) {
      System.out.println(ee.toString());
      ee.printStackTrace();
    }
    return null;
  }

  private String NSNtoAssetClass(String nsn) {
    // hel
    if (nsn.equals("NSN/1520011069519") || // Helicopter: AH-64A
	nsn.equals("NSN/1520010820686") || // Helicopter: EH-60A
	nsn.equals("NSN/1520011255476"))   // Helicopter: OH-58D
      return "RotaryWingAircraftWeapon";

    if (nsn.equals("NSN/1520010350266"))   // Helicopter: UH-60A
      return "CargoRotaryWingAircraft";

    if (nsn.equals("NSN/2350010871095") || // Tank: M1A1
	nsn.equals("NSN/2350001226826") || // Recovery Vehicle: M88A1
	nsn.equals("NSN/2350013050028") || // Howitzer: M109A6
	nsn.equals("NSN/2350014059886") || // Fighting Veh: M2A2 W/ODS
	nsn.equals("NSN/2350012487620") || // Fighting Veh: M3A2
	nsn.equals("NSN/2350010684089") || // CommandPostCarrier: M57782
	nsn.equals("NSN/2350012197577") || // PersonnelCarrier: M113A3
	nsn.equals("NSN/2350010853792"))   // TrackedPersonnelCarrier: M981
      return "SelfPropelledGroundWeapon";
      
    if (nsn.equals("NSN/2320011077155") || // HMMWV M998
	nsn.equals("NSN/2320011007673") || // HMMWV M985
	nsn.equals("NSN/2320011007672") || // HEMMT M978 WOWN (Tank)
	nsn.equals("NSN/2320013042278") || // PLS Truck: M1075
	nsn.equals("NSN/2320013189902"))   // HET Tractor: M1070
      return "Truck";

    if (nsn.equals("NSN/2330013035197") || // PLS Trailer: M1076
	nsn.equals("NSN/2330013038832"))   // HET Trailer: M1000
      return "Trailer";

    if (nsn.equals("NSN/8115001519953"))   // 20 Ft Container
      return "Container";

    if (nsn.equals("NSN/3990013077676"))   // PLS Flatrack: M1077
      return "Container";

    if (nsn.equals("C005A") ||
	nsn.equals("C005B") ||
	nsn.equals("C017A") ||
	nsn.equals("C141B") ||
	nsn.equals("DC10"))
      return "CargoFixedWingAircraft";

    return "ClassVIIMajorEndItem";
  }

  public String getQuery() { return null; }
  public void processRow(Object[] row) { return; }

}
