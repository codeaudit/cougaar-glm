/*--------------------------------------------------------------------------
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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
* --------------------------------------------------------------------------*/
package org.cougaar.mlm.construction;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.NewSchedule;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ScheduleType;
import org.cougaar.planning.ldm.plan.ScheduleUtilities;
import org.cougaar.util.TimeSpan;

import org.cougaar.planning.ldm.measure.CountRate;
import org.cougaar.planning.ldm.measure.FlowRate;
import org.cougaar.planning.ldm.measure.Rate;

import org.cougaar.glm.ldm.asset.ClassIVConstructionMaterial;
import org.cougaar.planning.ldm.PlanningFactory;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map;

import org.cougaar.glm.ldm.asset.AssetConsumptionRatePG;
import org.cougaar.glm.ldm.asset.BulkPOL;
import org.cougaar.glm.ldm.oplan.OpTempo;
import org.cougaar.glm.ldm.oplan.OrgActivity;
import org.cougaar.glm.ldm.plan.ObjectScheduleElement;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.glm.ldm.plan.Service;
import org.cougaar.glm.plugins.AssetUtils;
//import org.cougaar.glm.plugins.ClusterOPlan;
import org.cougaar.glm.plugins.projection.ConsumerSpec;

/*
 * Construction (ClassIV) Demand Spec for producing inventory demand 
 * @author  ALPINE <alpine-software@bbn.com>
 *
 **/
public class ConstructionDemandSpec extends ConsumerSpec {

	private String className = "ConstructionDemandSpec";
	Service service_;
	String theater_;
	Schedule quantitySchedule_;
	transient Vector paramSchedules_ = null;

        Vector assetList;
        Vector acrList;

        private Schedule consumer_sched;
        private Schedule materiel_sched;

        private transient PlanningFactory theLDMFactory;

	// 86400000 msec/day = 1000msec/sec * 60sec/min *60min/hr * 24 hr/day
	protected static final long              MSEC_PER_DAY =  86400000;

    public ConstructionDemandSpec (Asset c, Schedule consumer_sched, 
        Schedule materiel_sched, PlanningFactory theLDMF) {


		super (c, "ClassIVConstructionMaterial");
		this.theLDMFactory = theLDMF;
    this.materiel_sched = materiel_sched;

		// convert QuantitySchedule (needed to use ScheduleUtils in core)
		// to ObjectSchedule (so all param schedules are same type)
		quantitySchedule_ = convertQuantitySchedule(consumer_sched);
	
		AssetConsumptionRatePG acrpg = (AssetConsumptionRatePG)
	    	((Asset)consumer_).searchForPropertyGroup
			(AssetConsumptionRatePG.class);

   assetList = new Vector();
   acrList = new Vector();

		//debug (" CONSTRUCTED"+ new Date());
	} // constructor

    public void addAsset(Asset asset, Rate rate) {
        //System.out.println("Asset class = " + asset.getClass());
        //System.out.println("Asset typeID = " + asset.getTypeIdentificationPG().getTypeIdentification());
        assetList.add(asset);
        acrList.add(rate);
    }

    public static String getAssetIdentifier(Asset asset) {
        return (asset.getClass() + asset.getTypeIdentificationPG().getTypeIdentification());
    }

	public Enumeration getConsumed() {	
    return assetList.elements();

	} // getConsumed

	public Vector getParameterSchedules() {	
		if (paramSchedules_ == null) {
	    	paramSchedules_ = new Vector();
	    	paramSchedules_.add(quantitySchedule_);
       paramSchedules_.add(convertQuantitySchedule(materiel_sched));  
		}
		return paramSchedules_;
	} // getParameterSchedules

	// checks all elements are ObjectScheduleElements    
	protected void checkSchedule(Schedule sched) {
		Enumeration elements = sched.getAllScheduleElements();
		ScheduleElement el;
		while (elements.hasMoreElements()) {
	    	el = (ScheduleElement)elements.nextElement();
	    	if (!(el.getClass().getName().equals
				("org.cougaar.glm.ldm.plan.ObjectScheduleElement"))) {
	
				debug ("checkSchedule wrong class "+ el+" of type "+el.getClass().getName());
	    	} // if
		} // while
	} // checkSchedule
	
	public Schedule convertQuantitySchedule(Schedule qty_sched) {
		ObjectScheduleElement element;
		QuantityScheduleElement qty_el;
		Vector sched_els = new Vector();
		Enumeration qty_els = qty_sched.getAllScheduleElements();
		while (qty_els.hasMoreElements()) {
	    	qty_el = (QuantityScheduleElement) qty_els.nextElement();
	    	element = new ObjectScheduleElement
				(qty_el.getStartDate(), qty_el.getEndDate(),
				new Double(qty_el.getQuantity()));
	    	sched_els.addElement(element);
		} // while
		Schedule result_sched = newObjectSchedule(sched_els.elements());
		// this is a paranoid check - can be removed
		// checks all elements are ObjectScheduleElements
		checkSchedule(result_sched);
		return result_sched;
	} // convertQuantity


	// The asset in this case is the Major End Item
	// The params are:
	//   Element 0 : Number of consumers
	//   Element 1 : Org Activity (for Optempo)
	public Rate getRate(Asset resource, Vector params) {
		double quantity = 0;
		Object obj = params.get(0);
		if (obj instanceof Double) {
			quantity = ((Double)obj).doubleValue();
		} else {
	   	if (obj != null) {
				debug ( "Bad param - expected quantity got "+obj);
			} // if
			return null;
		} // if

    obj = params.get(1);
    if (obj == null) return null;

		Rate result = null;
                Rate rate = null;
                //int index = assetList.indexOf(resource);
                //int index = assetList.indexOf(getAssetIdentifier(resource));
                for (int i = 0; i < assetList.size(); i++) {
                    Asset asset = (Asset)assetList.elementAt(i);
                    if (getAssetIdentifier(resource).equals(getAssetIdentifier(asset))) {
                        //System.out.println("Matched asset with stored list, returning rate....");
                        rate = (Rate)acrList.elementAt(i);                    
                    }
	        }
		if (rate == null) System.err.println("ERROR - could not find acr for " + resource);
                /*
                if (index > 0) {
                    rate = (Rate)acrList.elementAt(index);
                }
                else {
                    System.err.println("ERROR - could not find acr for " + resource);
		    } */
		//acr_.getRate(resource, act);
		//Deans' "fix" ==> Rate rate = null;
		if (rate != null) {
			double multiplier =
			getMultiplier
				(resource.getTypeIdentificationPG().getTypeIdentification())
				.doubleValue();
			if (resource instanceof BulkPOL) {
				double flow =
					((FlowRate)rate).getGallonsPerDay()*quantity*multiplier;
				if (flow > 0.0) {
					result = FlowRate.newGallonsPerDay(flow);
				} // if

			} else {
				double count =
					((CountRate)rate).getEachesPerDay()*quantity*multiplier;
				if (count > 0.0) {
		    		// we CAN get fractional EACH demand
					result = CountRate.newEachesPerDay(count);
				} // if
			} // if
		} // if
		return result;
	} // getRate

	public String describe() {
		return "ConstructionDemandSpec for "+consumer_+" oftype "+resourceType_;
	}


	private void debug ( String msg ) {
		System.out.println ( "[ConstructionDemandSpec]  " + msg );
	} // debug

} // ConstructionDemandSpec
