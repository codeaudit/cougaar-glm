/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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
* --------------------------------------------------------------------------*/
package org.cougaar.mlm.construction;

import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.glm.ldm.asset.AssetConsumptionRatePG;
import org.cougaar.glm.ldm.asset.BulkPOL;
import org.cougaar.glm.ldm.plan.ObjectScheduleElement;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.glm.ldm.plan.Service;
import org.cougaar.glm.plugins.projection.ConsumerSpec;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.measure.CountRate;
import org.cougaar.planning.ldm.measure.FlowRate;
import org.cougaar.planning.ldm.measure.Rate;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;

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
