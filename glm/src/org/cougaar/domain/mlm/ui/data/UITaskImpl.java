/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import org.cougaar.domain.glm.ldm.Constants;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.Location;
import org.cougaar.domain.planning.ldm.plan.MPTask;
import org.cougaar.domain.planning.ldm.plan.PlanElement;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.Workflow;
import org.cougaar.core.society.UID;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/*
  Define a task object that is created from the XML returned by
  a PlanServiceProvider.
*/

public class UITaskImpl implements UITask, XMLUIPlanObject {
  Task task;
  UUID taskUUID;

  public UITaskImpl(Task task) {
    this.task = task;
    taskUUID = new UUID(task.getUID().toString());
  }

  /** @return String - the cluster identifier of the task source.  */

  public String getSource() {
    return task.getSource().getAddress();
  }


  /** @return String - the cluster identifier of the task destination. */

  public String getDestination() {
    return task.getDestination().getAddress();
  }

  /** @return String - the task verb; this is one of the verbs defined in the org.cougaar.domain.planning.ldm.plan.Verb interface. */

  public String getVerb() {
    return task.getVerb().toString();
  }

  public UUID getDirectObject() {
    Asset asset = task.getDirectObject();
    if (asset == null) 
      return null;
    UID uid = asset.getUID();
    if (uid == null)
      return null;
    return new UUID(uid.toString());
  }

  /** @return String - plan name. */

  public String getPlanName() {
    return task.getPlan().getPlanName();
  }

  /** @return UUID - plan element UUID */

  public UUID getPlanElement() {
    PlanElement pe = task.getPlanElement();
    if (pe != null)
      return new UUID(pe.getUID().toString());
    else
      return null;
  }

  /** @return UUID - returns the UUID of the workflow that the task is a member of
   */

  public UUID getWorkflow() {
    Workflow wf = task.getWorkflow();
    if (wf != null)
      return new UUID(wf.getUID().toString());
    else
      return null;
  }

  /** @return UUID - the UUID of the parent task
   */

  public UUID getParentTask() {
    if (task instanceof MPTask) // don't try to get parent of mp task
      return null;
    UID parent = task.getParentTaskUID();
    if (parent != null)
      return new UUID(parent.toString());
    else
      return null;
  }

  /** @return byte - the priority of the task
   */

  public byte getPriority() {
    return task.getPriority();
  }

  public Date getCommitmentDate() {
    return task.getCommitmentDate();
  }

  public UUID getUUID() {
    return taskUUID;
  }

  /** @return OPlan - the OPlan -- not yet supported in society */
  
  //  public OPlanID getOPlanID() {
  //    return null;
  //  }

  private UILocation getLocation(String preposition) {
    Enumeration prepPhrases = task.getPrepositionalPhrases();
    // if there's a "to" preposition, then get the indirect object
    // and create a UILocation element from it
    while (prepPhrases.hasMoreElements()) {
      PrepositionalPhrase prepPhrase = (PrepositionalPhrase)(prepPhrases.nextElement());
      if (prepPhrase.getPreposition().equals(preposition)) {
        Object obj = prepPhrase.getIndirectObject();
        if (Location.class.isInstance(obj))
          return new UILocationImpl((Location)obj);
        else
          return null;
      }
    }
    return null;
  }

  /** @return Location - the Location in the "To" prepositional phrase */

  public UILocation getToLocation() {
    return getLocation("To");
  }

  /** @return Location - the Location in the "From" prepositional phrase */

  public UILocation getFromLocation() {
    return getLocation("From");
  }

  private UUID getIndirectObjectUUID(String preposition) {
    Enumeration prepPhrases = task.getPrepositionalPhrases();
    while (prepPhrases.hasMoreElements()) {
      PrepositionalPhrase prepPhrase = (PrepositionalPhrase)(prepPhrases.nextElement());
      if (prepPhrase.getPreposition().equals(preposition)) {
        Object obj = prepPhrase.getIndirectObject();
        if (UIUniqueObject.class.isInstance(obj))
          return new UUID(((UIUniqueObject)obj).getUUID().toString());
        else
          return null;
      }
    }
    return null;
  }


  /** @return UUID - the UUID of the organization in the "For" prepostional phrase 
   */

  public UUID getForOrganization() {
    return getIndirectObjectUUID("For");
  }

  /** @return Schedule - the schedule in the "ItineraryOf" prepositional phrase */

  public UISchedule getItinerary() {
    Enumeration prepPhrases = task.getPrepositionalPhrases();
    while (prepPhrases.hasMoreElements()) {
      PrepositionalPhrase prepPhrase = (PrepositionalPhrase)(prepPhrases.nextElement());
      if (prepPhrase.getPreposition().equals("ItineraryOf")) {
        Object obj = prepPhrase.getIndirectObject();
        if (Schedule.class.isInstance(obj))
          return new UIScheduleImpl((Schedule)obj);
        else
          return null;
      }
    }
    return null;
  }

  /** @return UUID - the UUID of the organization in the "ReportingTo" prepositional phrase */

  public UUID getReportingTo() {
    return getIndirectObjectUUID("ReportingTo");
  }

  /** The following are rather specialized prepositional phrases, and
    are supported only for completeness, i.e. when displaying all tasks,
    for example.
    */

  /** @return String - the cluster id of the organization in the "For" preposition, when the indirect object is a cluster identifier */

  public String getForClusterId() {
    Enumeration prepPhrases = task.getPrepositionalPhrases();
    while (prepPhrases.hasMoreElements()) {
      PrepositionalPhrase prepPhrase = (PrepositionalPhrase)(prepPhrases.nextElement());
      if (prepPhrase.getPreposition().equals("For")) {
        Object obj = prepPhrase.getIndirectObject();
        if (ClusterIdentifier.class.isInstance(obj))
          return ((ClusterIdentifier)obj).getAddress();
        else
          return null;
      }
    }
    return null;
  }

  private String getIndirectObjectTypeId(String preposition) {
    Enumeration prepPhrases = task.getPrepositionalPhrases();
    while (prepPhrases.hasMoreElements()) {
      PrepositionalPhrase prepPhrase = (PrepositionalPhrase)(prepPhrases.nextElement());
      if (prepPhrase.getPreposition().equals(preposition)) {
        Object obj = prepPhrase.getIndirectObject();
        if (Asset.class.isInstance(obj))
          return ((Asset)obj).getTypeIdentificationPG().getTypeIdentification();
        else
          return null;
      }
    }
    return null;
  }

  /** @return String - the type identification field in the indirect object
     of the OfType preposition, used in the DetermineRequirements tasks
     */

  public String getOfRequirementsType() {
    return getIndirectObjectTypeId("OfType");
  }

  /** @return String - the type identification field in the indirect object
    of the "For" preposition in GetLogSupport tasks
    */

  public String getForWhom() {
    if (!task.getVerb().equals(Constants.Verb.GETLOGSUPPORT))
      return null;
    return getIndirectObjectTypeId("For");
  }

  private Date getDateResult(int desiredAspectType) {
    double value = task.getPreferredValue(desiredAspectType);
    if (value != -1)
      return new Date((long)value);
    else
      return null;
  }

  public Date getPreferredStartTime() {
    return getDateResult(AspectType.START_TIME);
  }

  public Date getPreferredEndTime() {
    return getDateResult(AspectType.END_TIME);
  }
  
  public double getPreferredQuantity() {
    return task.getPreferredValue(AspectType.QUANTITY);
  }

  public double getPreferredInterval() {
    return task.getPreferredValue(AspectType.INTERVAL);
  }

  public double getPreferredTotalShipments() {
    return task.getPreferredValue(AspectType.TOTAL_SHIPMENTS);
  }

  public UITypedQuantityAspectValue[] getPreferredTypedQuantities() {
    return null; // not implemented yet
  }

  public UITypedQuantityAspectValue getPreferredTypedQuantity(int i) {
    return null; // not implemented yet
  }

  public double getPreferredPOD() {
    return task.getPreferredValue(AspectType.POD);
  }

  public Date getPreferredPODDate() {
    return getDateResult(AspectType.POD_DATE);
  }

  //  XMLPlanObject method for UI
  
  public Element getXML(Document doc, Vector requestedFields) {
    return XMLUIPlanObjectConverter.getPlanObjectXML(this, doc, requestedFields);
  }
}

