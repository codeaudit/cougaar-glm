/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.NewSchedule;
import org.cougaar.planning.ldm.plan.NewRoleSchedule;
import org.cougaar.glm.ldm.asset.ClassVIIMajorEndItem;
import org.cougaar.glm.ldm.asset.NewMovabilityPG;
import org.cougaar.glm.ldm.asset.NewPhysicalPG;
import org.cougaar.planning.ldm.measure.*;
import java.math.BigDecimal;

import java.util.Calendar;
import java.util.Date;


/** AggregateAssetCreator implements QueryHandler to create sql queries 
 * and process the query results by creating aggregateassets.
 */

public class SQLFDMAggregateAssetCreator extends PeriodicQuery 
						 //implements org.cougaar.mlm.plugin.ldm.PropertyProvider
{
    public SQLFDMAggregateAssetCreator() {}

    public String getQuery() { return (String) getParameter("query");  }

    public static int UIC=0;     // 0      fue.unit_identifier uic,
    public static int QTY=1;     // 1     fue.unit_equipment_qty qty,
    public static int NSN=2;     // 2     fted.materiel_item_identifier nsn,
    public static int TI_ID=3;     // 3     fue.ti_id ti_id,
    public static int TID_ID=4;     // 4     fted.tid_id tid_id,
    public static int SHPPNG_CNFGRTN_CD=5;     // 5     fted.shppng_cnfgrtn_cd,
    public static int TI_NM=6;     // 6     substr(fte.ti_nm,1,30) ti_nm,
    public static int LENGTH=7;     // 7     tid_lg_dm length,
    public static int WIDTH=8;     // 8     tid_wdth_dm width,
    public static int HEIGHT=9;     // 9     tid_ht_dm height,
    public static int WEIGHT=10;     // 10     tid_wt weight,
    public static int VOLUME=11;     // 11     tid_vl volume,
    public static int CGO_TP_CD=12;     // 12     fted.cgo_tp_cd,
    public static int CGO_XTNT_CD=13;     // 13     fted.cgo_xtnt_cd,
    public static int CGO_CNTZN_CD=14;     // 14     fted.cgo_cntzn_cd,
    public static int MATERIEL_ITEM_IDENTIFIER=15;     // 15     fted.materiel_item_identifier,
    public static int TYPE_PACK_CODE=16;     // 16     fted.type_pack_code,
    public static int TID_EQ_TY_CD=17;     // 17     fted.tid_eq_ty_cd
    public static int FOOTPRINT=18; //    tid_ftprnt_ar area footprint
    public void processRow(Object[] data) {
      //System.out.println("I see "+data[1]+" of "+data[0]);

      String nsn = (String)data[NSN];

      // Non-vehicle agents must use the globalArguments (are they available?)
      // and skip rows where the nsn matches something in the args
      // -- use getParameter(String key) method to get at args
      // Use param exclude_types=type1&type2&type3
      String excludes = getParameter("exclude_types");

      // IF the exclude parameter list includes this vehicle
      // type then dont use it.
      if (excludes != null && (excludes.indexOf(nsn+'&') != -1 || excludes.endsWith(nsn))) {
	  // FIXME: No logging service available? Sure wish I had one...
	  // Log as debug that we're excluding this
	  System.out.println(myMessageAddress.getAddress() + ".SQLFDMAggAssetCreator: excluding vehicle of type " + nsn);
	  return;
      }

	Number count = (Number)data[QTY];
	String nomenclature = (String) data[TI_NM];

	String tid = "NSN/" + nsn;
//  	System.out.println(myMessageAddress.getAddress() + ": " + "Creating aggregate asset : " + tid + " " +count  + " " + nomenclature);
	ClassVIIMajorEndItem proto = findPrototype(data,tid, nomenclature);
	Asset newaggasset = ldmf.createAggregate((Asset)proto,  count.intValue());
	setupAvailableSchedule(newaggasset);
	publishAdd(newaggasset);
    }
  
    /** find or create a prototype asset suitable for using in an
     * aggregate asset.
     **/
    private  ClassVIIMajorEndItem findPrototype(Object[] data, String tid, String nomenclature) {

	ClassVIIMajorEndItem proto = 
	    (ClassVIIMajorEndItem)ldmf.createPrototype("org.cougaar.glm.ldm.asset.ClassVIIMajorEndItem", tid);

	// set the nomenclature if needed (the prototypeProvider should be doing this)
	NewTypeIdentificationPG tip = (NewTypeIdentificationPG)proto.getTypeIdentificationPG();
	proto.setTypeIdentificationPG(tip);
	tip.setTypeIdentification("NSN/"+(String)data[NSN]);
	tip.setNomenclature((String)data[TI_NM]);
	tip.setAlternateTypeIdentification((String)data[TI_ID]);

	NewMovabilityPG movabilityPG = (NewMovabilityPG)ldmf.createPropertyGroup("MovabilityPG");
	proto.setMovabilityPG(movabilityPG);
	movabilityPG.
	    setCargoCategoryCode((String)data[CGO_TP_CD]+(String)data[CGO_XTNT_CD]+(String)data[CGO_CNTZN_CD]);

	NewPhysicalPG pp = (NewPhysicalPG)ldmf.createPropertyGroup("PhysicalPG");
	proto.setPhysicalPG(pp);
	pp.setLength(Distance.newInches(((Number)data[LENGTH]).doubleValue()));
	pp.setWidth(Distance.newInches(((Number)data[WIDTH]).doubleValue()));
	pp.setHeight(Distance.newInches(((Number)data[HEIGHT]).doubleValue()));
	pp.setFootprintArea(Area.newSquareFeet(((Number)data[FOOTPRINT]).doubleValue()));
	pp.setVolume(Volume.newCubicFeet(((Number)data[VOLUME]).doubleValue()));
	pp.setMass(Mass.newPounds(((Number)data[WEIGHT]).doubleValue()));
	ldm.fillProperties(proto);
	ldm.cachePrototype(tid, proto);
//  	System.out.println
//  	    ("SQLFDMAggregateAssetCreator, findPrototype returns: "+proto +" with id: "+tip.getTypeIdentification());
	return proto;
    }

  
    private void setupAvailableSchedule(Asset asset) {
	Calendar mycalendar = Calendar.getInstance();
	// set the start date of the available schedule to 01/01/1990
	mycalendar.set(1990, 0, 1, 0, 0, 0);
	Date start = mycalendar.getTime();
	// set the end date of the available schedule to 01/01/2010
	mycalendar.set(2010, 0, 1, 0, 0, 0);
	Date end = mycalendar.getTime();
	NewSchedule availsched = ldmf.newSimpleSchedule(start, end);
	// set the available schedule
	((NewRoleSchedule)asset.getRoleSchedule()).setAvailableSchedule(availsched);
    }
//      public void fillProperties(Asset anAsset) {
//  	if (anAsset instanceof ClassVIIMajorEndItem) {
//  	    NewAssetConsumptionRatePG pg = (NewAssetConsumptionRatePG)
//  		getLDM().getFactory().createPropertyGroup(AssetConsumptionRatePG.class);
	    
//  	    AssetConsumptionRatePG.AssetConsumptionRateHandler acrHandler =
//  		new ACRHandler(anAsset, clusterId_);
//  	    pg.setAssetConsumptionRateHandler(acrHandler);
//  	    // attach it to the asset
//  // 	    ((ClassVIIMajorEndItem)anAsset).setAssetConsumptionRatePG(pg);
//  	    anAsset.setPropertyGroup(pg);
//  	}
//      }

//      private class ACRHandler implements AssetConsumptionRatePG.AssetConsumptionRateHandler {
//  	Asset myAsset_;
//  	Hashtable acrTable_;
//  	Service service_ = null;
//  	String theater_ = null;
//  	MessageAddress clusterId_ = null;

//  	ACRHandler(Asset asset, MessageAddress cid) {
//  	    myAsset_ = asset;
//  	    clusterId_ = cid;
//  	    acrTable_ = new Hashtable();
//  	}

//  	public void setAssetConsumptionRate(AssetConsumptionRate acr, String asset_type,
//  					    Service srv, String thr) {
//  	    acrTable_.put(asset_type, acr);
//  	    checkServiceTheater(srv, thr);
//  	}


//  	public Enumeration getPartTypes() {
//  	    return acrTable_.keys();
//  	}

//  	public void removeAssetConsumptionRate(String asset_type) {
//  	    acrTable_.remove(asset_type);
//  	}

//  	public AssetConsumptionRate getAssetConsumptionRate(String asset_type, Service srv, 
//  							    String thr) {
//  	    checkServiceTheater(srv, thr);
//  	    AssetConsumptionRate acr = (AssetConsumptionRate) acrTable_.get(asset_type);
//  	    if (acr == null) {
//  		// call method in outer class 
//  		acr = lookupAssetConsumptionRate(myAsset_, asset_type, service_, theater_);
//  	    }
//  	    if (acr != null) {
//  		setAssetConsumptionRate(acr, asset_type, service_, theater_);
//  	    }
//  	    else {
//  		BlackjackDebug.DEBUG(this.getClass().getName(), clusterId_, 
//  				     "getAssetConsumptionRate(), No consumption rate Information for "+
//  				     AssetUtils.assetDesc(myAsset_));
//  	    }
//  	    return acr;
//  	}
	
//  	// checks the service and the theater remain constant - should not change 
//  	// for a single cluster
//  	private void checkServiceTheater(Service srv, String thr) {
//  	    if (service_ == null) { 
//  		service_ = srv; 
//  	    } else if (!service_.equals(srv)) {
//  		BlackjackDebug.ERROR(this.getClass().getName(), clusterId_,
//  				"checkServiceTheater(), expecting "+service_+" but found "+srv);
//  	    }
//  	    if (theater_ == null) { 
//  		theater_ = thr; 
//  	    } else if (!theater_.equals(thr)) {
//  		BlackjackDebug.ERROR(this.getClass().getName(), clusterId_,
//  				"getAssetConsumptionRate(), expecting "+theater_+" but found "+thr);
//  	    }
//  	}
//      }


  
}
