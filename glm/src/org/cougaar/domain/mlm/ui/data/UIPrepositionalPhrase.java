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

/*
  Define a prepositional phrase object that is created from the XML returned by
  a PlanServiceProvider.  A prepositional phrase is defined as a preposition
  and an indirect object.  The indirect object is one of:
  org.cougaar.domain.planning.ldm.asset.Asset
  org.cougaar.domain.planning.ldm.plan.Location
  org.cougaar.domain.planning.ldm.plan.Schedule
  mil.darpa.log.fgi.domain.Requisition
  org.cougaar.domain.planning.ldm.OPlan
  Only one of the corresponding getters will return a non-null value.
  For example, if the indirect object is a Location, getLocation will return
  a non-null, value, and getAsset, getSchedule, getRequisition, and getOPlan
  will return null.  The method, getIndirectObjectType, will return the
  class (NOT fully qualified) of the indirect object.
*/

public interface UIPrepositionalPhrase {

  /** @return String - the preposition as defined in org.cougaar.domain.planning.ldm.plan.Constants.Preposition. */

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






