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

package org.cougaar.mlm.ui.data;

import java.util.Date;

/*
  Define a task object that is created from the XML returned by
  a PlanServiceProvider.
*/

public interface UITask extends UIUniqueObject {

  /** @return String - the cluster identifier of the task source.  */

  String getSource();

  /** @return String - the cluster identifier of the task destination. */

  String getDestination();

  /** @return String - the task verb; this is one of the verbs defined in the org.cougaar.planning.ldm.plan.Verb interface. */

  String getVerb();

  /** @return UUID - the UUID of the asset that is being acted upon by this task */

  UUID getDirectObject();

  /** @return UIPrepositionalPhrase - an array of prepositional phrases. */

  //  UIPrepositionalPhrase[] getPrepositionalPhrases();

  /** @return String - plan name. */

  String getPlanName();

  /** @return UUID - plan element UUID */

  UUID getPlanElement();

  /** @return UUID - returns the UUID of the workflow that the task is a member of
   */

  UUID getWorkflow();

  /** @return UUID - the UUID of the parent task
   */

  UUID getParentTask();


  /** @return byte - the priority of the task
   */

  byte getPriority();

  /** @return String[] -  array of aspect types for the preferences
   */

  //  String[] getPreferenceAspectTypes();

  /** @return Date - date after which task can't be rescinded
   */
  Date getCommitmentDate();

  /** @return OPlanID - the OPlan ID  -- not yet supported in society */
  
  //  OPlan getOPlanID();

  /** @return Location - the Location in the "To" prepositional phrase */

  UILocation getToLocation();

  /** @return Location - the Location in the "From" prepositional phrase */

  UILocation getFromLocation();

  /** @return UUID - the UUID of the organization in the "For" prepostional phrase 
   */

  UUID getForOrganization();

  /** @return Schedule - the schedule in the "ItineraryOf" prepositional phrase */

  UISchedule getItinerary();

  /** @return UUID - the UUID of the organization in the "ReportingTo" prepositional phrase */

  UUID getReportingTo();

  /** The following are rather specialized prepositional phrases, and
    are supported only for completeness, i.e. when displaying all tasks,
    for example.
    */

  /** @return String - the cluster id of the organization in the "For" preposition, when the indirect object is a cluster identifier */

  String getForClusterId();

  /** @return String - the type identification field in the indirect object
     of the OfType preposition, used in the DetermineRequirements tasks
     */

  String getOfRequirementsType();

  /** @return String - the type identification field in the indirect object
    of the "For" preposition in GetLogSupport tasks
    */

  String getForWhom();

  /** Preferences */

  Date getPreferredStartTime();

  Date getPreferredEndTime();
  
  double getPreferredQuantity();

  double getPreferredInterval();

  double getPreferredTotalShipments();

  UITypedQuantityAspectValue[] getPreferredTypedQuantities();

  UITypedQuantityAspectValue getPreferredTypedQuantity(int i);

  double getPreferredPOD();

  Date getPreferredPODDate();

}

