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
 
package org.cougaar.domain.mlm.ui.psp.xmlservice;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.*;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.mlm.ui.data.UISupplyStatus;
import org.cougaar.domain.mlm.ui.data.UIUnitStatus;

public class PSP_EquipmentTransport extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {
  private String myID;
  public String desiredAssetName;
  final static int CRITICAL_THRESHHOLD = 100;
  final static long MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
  String postData;

  public PSP_EquipmentTransport() throws RuntimePSPException {
    super();
  }

  public PSP_EquipmentTransport(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  /* This PSP is referenced directly (in the URL from the client)
     and hence this shouldn't be called.
     */

  public boolean test(HttpInput query_parameters, PlanServiceContext psc) {
    super.initializeTest();
    return false; 
  }

  private Date getPreferredDate(Task task, int aspectType) {
    Preference preference = task.getPreference(aspectType);
    if (preference == null)
      return null;
    ScoringFunction scoringFunction = preference.getScoringFunction();
    AspectScorePoint pt = scoringFunction.getBest();
    AspectValue aspectValue = pt.getAspectValue();
    Date date = new Date(Math.round(aspectValue.getValue()));
    //    System.out.println("Task preference date is: " + date.toString());
    return date;
  }

  /** Called after submitting a subscription; compute the
    days late for tasks vs. allocation results.  Compose results
    which are tuples of unit, supply, status.
    */

  private UISupplyStatus getEquipmentStatus(Collection container) {
    EquipmentStatus equipmentStatus = new EquipmentStatus();
    UISupplyStatus results = new UISupplyStatus();
    for (Iterator i = container.iterator(); i.hasNext(); ) {
      String unitName = null;
      Task task = (Task)i.next();
      //      System.out.println("Examining task: " + task.getUID());
      PrepositionalPhrase pp = task.getPrepositionalPhrase(Constants.Preposition.FOR);
      // temporary fix to work with TOPSClient
      // which generates tasks with no "FOR" prepositional phrase
      if (pp == null) {
	System.out.println("WARNING: No FOR prepositional phrase; using Task.source.address as unit name");
	unitName = task.getSource().getAddress();
	if (unitName.equals("MCCGlobalMode"))
	  continue; // ignore tasks from MCCGlobalMode
      } else {
	Object indirectObject = pp.getIndirectObject();
	if (indirectObject instanceof Organization)
	  unitName = ((Asset)indirectObject).getItemIdentificationPG().getItemIdentification();
	else if (indirectObject instanceof String)
	  unitName = (String)indirectObject;
	if (unitName == null) {
	  if (indirectObject == null)
	    System.out.println("WARNING: indirect object is null");
	  else
	    System.out.println("WARNING: indirect object of FOR is neither Organization or String: " + indirectObject.getClass().toString());
	  continue;
	}
      }
      Asset equipment = (Asset)task.getDirectObject();
      if (equipment instanceof AggregateAsset)
	equipment = ((AggregateAsset)equipment).getAsset();
      TypeIdentificationPG typeIdPG = equipment.getTypeIdentificationPG();
      if (typeIdPG == null) {
	System.out.println("WARNING: null type identification property group");
	continue;
      }
      String equipmentName = typeIdPG.getTypeIdentification();
      if (equipmentName == null) {
	System.out.println("WARNING: typeIDPG type identification is null");
	continue;
      }
      String equipmentNomenclature = typeIdPG.getNomenclature();
      if (equipmentNomenclature == null)
	equipmentNomenclature = "";
      double preferredQuantity = task.getPreferredValue(AspectType.QUANTITY);
      Date preferredEndDate = getPreferredDate(task, AspectType.END_TIME);
      if (preferredEndDate == null) {
	System.out.println("WARNING: using start date because end date is null");
	preferredEndDate = getPreferredDate(task, AspectType.START_TIME);
      }
      if (preferredEndDate == null) {
	System.out.println("WARNING: both start and end date are null");
	continue;
      }
      PlanElement pe = task.getPlanElement();
      if (pe == null) {
	System.out.println("WARNING: plan element is null");
	continue;
      }
      AllocationResult reportedResult = pe.getReportedResult();
      if (reportedResult == null) {
	System.out.println("WARNING: Reported result is null, using estimated result");
	reportedResult = pe.getEstimatedResult();
	if (reportedResult == null) {
	  System.out.println("WARNING: Estimated result is null");
	  continue;
	}
      }
      if (reportedResult.isPhased()) {
	System.out.println("WARNING: Ignoring phased reported result");
	continue;
      }
      String status = "";
      if (reportedResult.isSuccess()) {
	double actualQuantity = 0;
	if (reportedResult.isDefined(AspectType.QUANTITY))
	  actualQuantity = reportedResult.getValue(AspectType.QUANTITY);
	else {
	  System.out.println("WARNING: no actual quantity; using preferred quantity");
	  actualQuantity = preferredQuantity;
	}
	Date actualEndDate = null;
	if (reportedResult.isDefined(AspectType.END_TIME)) {
	  actualEndDate = new Date(Math.round(reportedResult.getValue(AspectType.END_TIME)));
	  //	  System.out.println("Actual date is: " + actualEndDate);
	  if (actualEndDate == null) {
	    if (reportedResult.isDefined(AspectType.START_TIME)) {
	      System.out.println("WARNING: using start date in allocation reported result, because end date is null");
	      actualEndDate = new Date(Math.round(reportedResult.getValue(AspectType.START_TIME)));
	    }
	  }
	  if (actualEndDate == null) {
	    System.out.println("WARNING: both start and end date are null in allocation reported result");
	    continue;
	  }
	}
	//	status = computeStatus(preferredQuantity, preferredEndDate,
	//			       actualQuantity, actualEndDate);
	computeStatus(equipmentStatus,
		      unitName, equipmentName, equipmentNomenclature,
		      preferredQuantity, preferredEndDate,
		      actualQuantity, actualEndDate);
      } else
	equipmentStatus.set(unitName, equipmentName, equipmentNomenclature,
			    UIUnitStatus.RED);
      //      System.out.println("Adding status for: " + unitName +
      //			 " " + equipmentName + " " + status);
      // results.addUnitStatus(new UIUnitStatus(unitName, equipmentName, status));
    }
    Vector individualEquipmentStatuses = equipmentStatus.getAllStatus();
    for (int i = 0; i < individualEquipmentStatuses.size(); i++) {
      IndividualEquipmentStatus ies = 
	(IndividualEquipmentStatus)individualEquipmentStatuses.elementAt(i);
      String unitName = ies.getUnitName();
      String equipmentName = ies.getEquipmentName();
      String equipmentNomenclature = ies.getEquipmentNomenclature();
      String status = ies.getStatus();
      results.addUnitStatus(new UIUnitStatus(unitName, equipmentName, 
					     equipmentNomenclature, status));
    }
    return results;
  }

  /** Compute the status of the transport for a unit and equipment.
    If dates are equal and quantity is equal, return green, else
    convert dates to day of year and comput
    (actualEnd - preferredEnd) * (preferredQuantity - actualQuantity)
    If results are over threshhold, return red, else return yellow.
    */

  //  private String computeStatus(double preferredQuantity, Date preferredEndDate,
  //			       double actualQuantity, Date actualEndDate) {
  //    long preferredEndTime = preferredEndDate.getTime();
  //    long actualEndTime = actualEndDate.getTime();
  //    int nDaysLate = (int)((actualEndTime - preferredEndTime) / MILLIS_IN_DAY);
  //    double quantityDaysLate = 0;
  //    if (preferredQuantity == -1) // no real quantity, so ignore it
  //      quantityDaysLate = nDaysLate;
  //    else
  //      quantityDaysLate = nDaysLate * (preferredQuantity - actualQuantity);
  //    if (quantityDaysLate <= 0)
  //      return UIUnitStatus.GREEN;
  //    else if (quantityDaysLate > CRITICAL_THRESHHOLD)
  //      return UIUnitStatus.RED;
  //    return UIUnitStatus.YELLOW;
  //  }


  /** If the actualQuantity is less than the preferredQuantity,
      then we treat the difference as being more than the
      threshhold_date late.
  */

  private void computeStatus(EquipmentStatus equipmentStatus,
			     String unitName, String equipmentName,
			     String equipmentNomenclature,
			     double preferredQuantity, Date preferredEndDate,
			     double actualQuantity, Date actualEndDate) {
    long preferredEndTime = preferredEndDate.getTime();
    long actualEndTime = actualEndDate.getTime();
    int nDaysLate = (int)((actualEndTime - preferredEndTime) / MILLIS_IN_DAY);
    if (preferredQuantity == actualQuantity)
      equipmentStatus.add(unitName, equipmentName, equipmentNomenclature,
			  actualQuantity, nDaysLate);
    else {
      equipmentStatus.add(unitName, equipmentName, equipmentNomenclature,
			  actualQuantity, nDaysLate);
      equipmentStatus.add(unitName, equipmentName, equipmentNomenclature,
			  (preferredQuantity - actualQuantity),
			  IndividualEquipmentStatus.THRESHHOLD_DATE+1);
    }
  }


  /*
    Called when a request is received from a client.
  */

  public void execute( PrintStream out,
		       HttpInput query_parameters,
		       PlanServiceContext psc,
		       PlanServiceUtilities psu) throws Exception {
   try {
     myExecute(out, query_parameters, psc, psu);
   } catch (Exception e) {
     e.printStackTrace();
   };
  }

 
  private void myExecute( PrintStream out,
		       HttpInput query_parameters,
		       PlanServiceContext psc,
		       PlanServiceUtilities psu) throws Exception {
    System.out.println("EquipmentTransport PSP invoked");
    postData = null;

    if (query_parameters.hasBody()) {
      postData = new String(query_parameters.getBodyAsCharArray());
      postData = postData.trim();
      System.out.println("POST DATA:" + postData);
    }
    

    // get tasks we need to determine status
    UnaryPredicate myPredicate = new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Task) {
	  if (((Task)o).getVerb().toString().equals(postData))
	    return true;
	}
	return false;
      }
    };

    Subscription subscription = 
      psc.getServerPlugInSupport().subscribe(this, myPredicate);
    Collection container = 
      ((CollectionSubscription)subscription).getCollection();

    // create status data object from the log plan objects
    UISupplyStatus status = getEquipmentStatus(container);

    // unsubscribe, don't need this subscription any more
    psc.getServerPlugInSupport().unsubscribeForSubscriber(subscription);

    // send the status
    if (status != null) {
      ObjectOutputStream p = new ObjectOutputStream(out);
      p.writeObject(status);
      System.out.println("Sent XML document");
    }
  }

  public boolean returnsXML() {
    return true;
  }

  public boolean returnsHTML() {
    return false;
  }

  public String getDTD() {
    return "myDTD";
  }

  /* The UISubscriber interface.
     This PSP doesn't care if subscriptions change
     because it treats each request as a new request.
  */

  public void subscriptionChanged(Subscription subscription) {
  }

}

class IndividualEquipmentStatus {
  final static double SOMEWHAT_LATE_YELLOW_PERCENT = .1;
  final static double VERY_LATE_YELLOW_PERCENT = .05;
  final static double SOMEWHAT_LATE_RED_PERCENT = .2;
  final static double VERY_LATE_RED_PERCENT = .1;
  public final static int THRESHHOLD_DATE = 6;
  String unitName;
  String equipmentName;
  String equipmentNomenclature;
  double quantity;
  double somewhatLate;
  double veryLate;
  String status;

  public IndividualEquipmentStatus(String unitName, String equipmentName,
				   String equipmentNomenclature) {
    this.unitName = unitName;
    this.equipmentName = equipmentName;
    this.equipmentNomenclature = equipmentNomenclature;
    this.quantity = 0;
    this.somewhatLate = 0;
    this.veryLate = 0;
    this.status = null;  // red if allocation reported result is not success
  }

  public void add(double n, int daysLate) {
    quantity = quantity + n;
    if (daysLate > 0)
      somewhatLate = somewhatLate + n;
    if (daysLate >= THRESHHOLD_DATE)
      veryLate = veryLate + n;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    System.out.println("Getting status for: " + unitName + " " +
		       equipmentName + " " + equipmentNomenclature +
		       " quantity: " + quantity +
		       " somewhatLate: " + somewhatLate +
		       " veryLate: " + veryLate);
    if (status != null) {
      System.out.println("Returning status: " + status);
      return status;
    }
    if (quantity == 0) // to prevent divide by 0
      return UIUnitStatus.GREEN;
    if (((somewhatLate / quantity) > SOMEWHAT_LATE_RED_PERCENT) ||
	(veryLate / quantity) > VERY_LATE_RED_PERCENT) {
      System.out.println("Returning red");
      return UIUnitStatus.RED;
    }
    if (((somewhatLate / quantity) > SOMEWHAT_LATE_YELLOW_PERCENT) ||
	(veryLate / quantity) > VERY_LATE_RED_PERCENT) {
      System.out.println("Returning yellow");
      return UIUnitStatus.YELLOW;
    }
    System.out.println("Returning green");
    return UIUnitStatus.GREEN;
  }

  public String getUnitName() {
    return unitName;
  }

  public String getEquipmentName() {
    return equipmentName;
  }

  public String getEquipmentNomenclature() {
    return equipmentNomenclature;
  }
}

class EquipmentStatus {
  Hashtable equipmentStatusHT;

  public EquipmentStatus() {
    equipmentStatusHT = new Hashtable();
  }

  public void add(String unitName, String equipmentName, 
		  String equipmentNomenclature,
		  double quantity, int daysLate) {
    IndividualEquipmentStatus ies = 
      (IndividualEquipmentStatus)equipmentStatusHT.get(unitName+equipmentName);
    if (ies == null) {
      System.out.println("Creating entry for: " + unitName + " " +
			 equipmentName);
      ies = new IndividualEquipmentStatus(unitName, equipmentName,
					  equipmentNomenclature);
    }
    ies.add(quantity, daysLate);
    equipmentStatusHT.put(unitName+equipmentName, ies);
  }

  public void set(String unitName, String equipmentName, 
		  String equipmentNomenclature, String status) {
    IndividualEquipmentStatus ies = 
      (IndividualEquipmentStatus)equipmentStatusHT.get(unitName+equipmentName);
    if (ies == null)
      ies = new IndividualEquipmentStatus(unitName, equipmentName,
					  equipmentNomenclature);
    ies.setStatus(status);
    equipmentStatusHT.put(unitName+equipmentName, ies);
  }

  public Vector getAllStatus() {
    return new Vector(equipmentStatusHT.values());
  }
}
    

      
    
      

