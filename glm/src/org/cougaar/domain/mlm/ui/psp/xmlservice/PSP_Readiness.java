/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.mlm.ui.data.UIReadiness;
import org.cougaar.domain.mlm.ui.data.UIUnitReadiness;

public class PSP_Readiness extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {
  private String myID;
  public String desiredAssetName;
  final static int CRITICAL_THRESHHOLD = 100;
  final static long MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
  String postData;

  public PSP_Readiness() throws RuntimePSPException {
    super();
  }

  public PSP_Readiness(String pkg, String id) throws RuntimePSPException {
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
    System.out.println("Task preference date is: " + date.toString());
    return date;
  }

  /** Called after submitting a subscription; compute the
    days late for tasks vs. allocation results.  Compose results
    which are tuples of unit, supply, status.
    */

  private UIReadiness getEquipmentReadiness(Collection container) {
    UIReadiness results = new UIReadiness();
    for (Iterator i = container.iterator(); i.hasNext(); ) {
      String unitName = null;
      Task task = (Task)i.next();
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
	  System.out.println("WARNING: indirect object of FOR is neither Organization or String: " + indirectObject.getClass().toString());
	  continue;
	}
      }
      Asset equipment = (Asset)task.getDirectObject();
      if (equipment instanceof AggregateAsset)
	equipment = ((AggregateAsset)equipment).getAsset();
      String equipmentName = 
	equipment.getTypeIdentificationPG().getNomenclature();
      double preferredQuantity = task.getPreferredValue(AspectType.QUANTITY);
      Date preferredStartDate = getPreferredDate(task, AspectType.START_TIME);
      Date preferredEndDate = getPreferredDate(task, AspectType.END_TIME);
      PlanElement pe = task.getPlanElement();
      AllocationResult reportedResult = pe.getReportedResult();
      if (reportedResult == null) {
	System.out.println("WARNING: Reported result is null");
	continue;
      }
      if (reportedResult.isPhased()) {
	System.out.println("WARNING: Ignoring phased reported result");
	continue;
      }
      boolean success = reportedResult.isSuccess();
      Date actualStartDate = null;
      Date actualEndDate = null;
      double actualQuantity = 0;
      if (success) {
	if (reportedResult.isDefined(AspectType.QUANTITY))
	  actualQuantity = reportedResult.getValue(AspectType.QUANTITY);
	else {
	  System.out.println("WARNING: no actual quantity; using preferred quantity");
	  actualQuantity = preferredQuantity;
	}
	if (reportedResult.isDefined(AspectType.START_TIME)) {
	  actualStartDate = 
	    new Date(Math.round(reportedResult.getValue(AspectType.START_TIME)));
	  System.out.println("Actual start date is: " + actualStartDate);
	  if (actualStartDate == null)
	    continue;
	}
	if (reportedResult.isDefined(AspectType.END_TIME)) {
	  actualEndDate = 
	    new Date(Math.round(reportedResult.getValue(AspectType.END_TIME)));
	  System.out.println("Actual end date is: " + actualEndDate);
	  if (actualEndDate == null)
	    continue;
	}
      } // end if allocation is success
      results.addUnitReadiness(
	 new UIUnitReadiness(unitName, equipmentName, success,
			     preferredQuantity, actualQuantity,
			     preferredStartDate, actualStartDate,
			     preferredEndDate, actualEndDate));
    }
    return results;
  }

  /*
    Called when a request is received from a client.
  */

  public void execute( PrintStream out,
		       HttpInput query_parameters,
		       PlanServiceContext psc,
		       PlanServiceUtilities psu) throws Exception {

    System.out.println("Readiness PSP invoked");
    postData = null;

    if (query_parameters.hasBody()) {
      postData = query_parameters.getBodyAsString();
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

    // create readiness data object from the log plan objects
    UIReadiness readiness = getEquipmentReadiness(container);

    // unsubscribe, don't need this subscription any more
    psc.getServerPlugInSupport().unsubscribeForSubscriber(subscription);

    // send the readiness
    if (readiness != null) {
      System.out.println("Sending readiness: " + 
			 readiness.getSocietyReadiness().size());
      ObjectOutputStream p = new ObjectOutputStream(out);
      p.writeObject(readiness);
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

