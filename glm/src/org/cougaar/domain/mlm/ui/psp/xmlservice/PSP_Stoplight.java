/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.xmlservice;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.*;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.mlm.ui.data.UILateStatus;

public class PSP_Stoplight extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {
  public String desiredAssetName;
  final static long MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
  String postData;
  Hashtable statusHashtable = null;

  public PSP_Stoplight() throws RuntimePSPException {
    super();
  }

  public PSP_Stoplight(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  /* This PSP is referenced directly (in the URL from the client)
     and hence this shouldn't be called.
     */

  public boolean test(HttpInput query_parameters, PlanServiceContext psc) {
    super.initializeTest();
    return false; 
  }

  /** Based on the scoring function in the task preference for end date,
    and the score of the reported end date, find the latest acceptable date,
    and return reported end date - latest acceptable date.
    */

  private int getDaysLate(Task task, AllocationResult reportedResult) {
    AspectScorePoint latestAcceptable;
    AspectValue reportedAspectValue = null;

    AspectValue[] aspectValues = reportedResult.getAspectValueResults();
    for (int i = 0; i < aspectValues.length; i++)
      if (aspectValues[i].getAspectType() == AspectType.END_TIME)
	reportedAspectValue = aspectValues[i];
    
    Preference preference = task.getPreference(AspectType.END_TIME);
    ScoringFunction scoringFunction = preference.getScoringFunction();
    double actualValue = scoringFunction.getScore(reportedAspectValue);
    if (actualValue < 1.0)
       return 0; // not late

    AspectScorePoint bestPoint = scoringFunction.getBest();
    AspectValue bestAspectValue = bestPoint.getAspectValue();
    double bestValue = bestAspectValue.getValue();

    if (bestValue < actualValue)
      latestAcceptable = 
	scoringFunction.getMaxInRange(bestAspectValue, reportedAspectValue);
    else
      latestAcceptable = 
	scoringFunction.getMaxInRange(reportedAspectValue, bestAspectValue);

    return (int)((reportedAspectValue.getValue() - latestAcceptable.getValue()) / MILLIS_IN_DAY);
  }

  /** Get unit name from task.
   */

  private String getUnitName(Task task) {
    String unitName = null;

    PrepositionalPhrase pp = task.getPrepositionalPhrase(Constants.Preposition.FOR);
    // temporary fix to work with TOPSClient
    // which generates tasks with no "FOR" prepositional phrase
    if (pp == null) {
      System.out.println("WARNING: No FOR prepositional phrase; using Task.source.address as unit name");
      unitName = task.getSource().getAddress();
      if (unitName.equals("MCCGlobalMode"))
	return null;
      else
	return unitName;
    }

    // unit name is indirect object of prepositional phrase
    Object indirectObject = pp.getIndirectObject();
    if (indirectObject == null) {
      System.out.println("WARNING: indirect object is null");
      return null;
    }
    if (indirectObject instanceof Organization)
      unitName = ((Asset)indirectObject).getItemIdentificationPG().getItemIdentification();
    else if (indirectObject instanceof String)
      unitName = (String)indirectObject;
    else
      System.out.println("WARNING: indirect object of FOR is neither Organization or String: " + indirectObject.getClass().toString());
    return unitName;
  }

  /** Called after submitting a subscription; compute the
    days late for tasks vs. allocation results.  Returns 
    days late and quantities for each type of equipment in each unit.
    */

  private void getEquipmentStatus(Collection container) {
    Asset equipment;
    statusHashtable = new Hashtable();

    for (Iterator i = container.iterator(); i.hasNext(); ) {
      Task task = (Task)i.next();
      //      System.out.println("Examining task: " + task.getUID());
      if (task.getPreference(AspectType.INTERVAL) != null) {
	System.out.println("WARNING: ignoring task with interval aspect type and UID: " + task.getUID());
        continue;
      }

      String unitName = getUnitName(task);
      if (unitName == null)
	continue;

      equipment = (Asset)task.getDirectObject();
      if (equipment instanceof AggregateAsset) {
	equipment = ((AggregateAsset)equipment).getAsset();
	getSingleEquipmentStatus(task, unitName, equipment);
      } else if (equipment instanceof AssetGroup) {
	Vector assets = ((AssetGroup)equipment).getAssets();
	for (int j = 0; j < assets.size(); j++) {
	  equipment = (Asset)assets.elementAt(j);
	  if (equipment instanceof AggregateAsset) {
	    equipment = ((AggregateAsset)equipment).getAsset();
	    getSingleEquipmentStatus(task, unitName, equipment);
	  } else
	    getSingleEquipmentStatus(task, unitName, equipment);
	}
      } else 
	getSingleEquipmentStatus(task, unitName, equipment);
    }
  }

  private void getSingleEquipmentStatus(Task task, String unitName,
					Asset equipment) {
    int daysLate;
    TypeIdentificationPG typeIdPG = equipment.getTypeIdentificationPG();
    if (typeIdPG == null) {
      System.out.println("WARNING: ignoring task with null type identification property group");
      return;
    }
    String equipmentName = typeIdPG.getTypeIdentification();
    if (equipmentName == null) {
      System.out.println("WARNING: ignoring task with null type identification");
      return;
    }
    if (equipmentName.equals("TOPS_ASSET_GROUP")) {
      System.out.println("WARNING: ignoring TOPS_ASSET_GROUP");
      return;
    }

    String equipmentNomenclature = typeIdPG.getNomenclature();
    if (equipmentNomenclature == null)
      equipmentNomenclature = "";
    
    // if the quantity preference doesn't exist, then this returns -1
    // and we assume quantity of 1
    double preferredQuantity = task.getPreferredValue(AspectType.QUANTITY);
    if (preferredQuantity == -1) {
      System.out.println("WARNING: no task preference for quantity, using 1");
      preferredQuantity = 1;
    }
    
    Preference preference = task.getPreference(AspectType.END_TIME);
    if (preference == null) {
      System.out.println("WARNING: ignoring task with null end time preference");
      return;
    }
    
    PlanElement pe = task.getPlanElement();
    if (pe == null) {
      System.out.println("WARNING: ignoring task with null plan element");
      return;
    }
    AllocationResult reportedResult = pe.getReportedResult();
    if (reportedResult == null) {
      System.out.println("WARNING: Reported result is null, using estimated result");
      reportedResult = pe.getEstimatedResult();
      if (reportedResult == null) {
	System.out.println("WARNING: ignoring task with null estimated result");
	return;
      }
    }
    if (reportedResult.isPhased()) {
      System.out.println("WARNING: ignoring task with phased reported result");
      return;
    }

    double actualQuantity = 0;
    if (reportedResult.isDefined(AspectType.QUANTITY))
      actualQuantity = reportedResult.getValue(AspectType.QUANTITY);
    else {
      System.out.println("WARNING: no actual quantity; using preferred quantity");
      actualQuantity = preferredQuantity;
    }

    if (!reportedResult.isDefined(AspectType.END_TIME)) {
      System.out.println("WARNING: ignoring task with reported result end date not defined");
      return;
    }

    if (reportedResult.isSuccess())
      daysLate = getDaysLate(task, reportedResult);
    else {
      System.out.println("Reported result success is false; treating as 49 days late");
      daysLate = 49; // treat unsuccessful as infinitely late
    }

    //    System.out.println("Status for: " + unitName + " " +
    //		       equipmentName + " " + equipmentNomenclature + " " +
    //		       actualQuantity + " " + preferredQuantity + " " +
    //		       daysLate + " from task: " + task.getUID());

    String key = unitName + equipmentName + equipmentNomenclature;
    UILateStatus status = 
      (UILateStatus)statusHashtable.get(key);
    if (status == null) {
      status = new UILateStatus(unitName, equipmentName, 
				equipmentNomenclature);
      statusHashtable.put(key, status);
    }
    // treat undelivered quantites as infinitely late
    if (actualQuantity >= preferredQuantity)
      status.add(actualQuantity, daysLate);
    else {
      status.add(actualQuantity, daysLate);
      status.add(preferredQuantity - actualQuantity, 49);
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
    System.out.println("Stoplight PSP invoked");
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

    // create status data object from the log plan objects
    getEquipmentStatus(container);

    // unsubscribe, don't need this subscription any more
    psc.getServerPlugInSupport().unsubscribeForSubscriber(subscription);

    // send the status
    if (statusHashtable != null) {
      ObjectOutputStream p = new ObjectOutputStream(out);
      p.writeObject(statusHashtable);
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
