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

package org.cougaar.mlm.ui.data;

/*
  Define a prepositional phrase object that is created from the XML returned by
  a PlanServiceProvider.  A prepositional phrase is defined as a preposition
  and an indirect object.  The indirect object is one of:
  org.cougaar.planning.ldm.asset.Asset
  org.cougaar.planning.ldm.plan.Location
  org.cougaar.planning.ldm.plan.Schedule
  mil.darpa.log.fgi.domain.Requisition
  org.cougaar.planning.ldm.OPlan
  Only one of the corresponding getters will return a non-null value.
  For example, if the indirect object is a Location, getLocation will return
  a non-null, value, and getAsset, getSchedule, getRequisition, and getOPlan
  will return null.  The method, getIndirectObjectType, will return the
  class (NOT fully qualified) of the indirect object.
*/

public interface UIPrepositionalPhrase {

  /** @return String - the preposition as defined in org.cougaar.planning.ldm.plan.Constants.Preposition. */

  public String getPreposition();

  /** @return String - the class of the indirect object.
    Returns one of: "Asset", "Location", "Schedule", "Requisition", "OPlan".
  */

  public String getIndirectObjectType();

  /** @return String - the UID of the asset indirect object. */

  public String getUIAssetUUID();

  /** @return UILocation - the UI representation of the location indirect object. */
  public UILocation getLocation();

  /** @return UISchedule - the UI representation of the schedule indirect object. */

  public UISchedule getSchedule();

  /** @return UIRequisition - the UI representation of the requisition indirect object. */

  public UIRequisition getRequisition();

  /** @return UIOPlan - the UI representation of the OPlan indirect object. */

  public UIOPlan getOPlan();

}






