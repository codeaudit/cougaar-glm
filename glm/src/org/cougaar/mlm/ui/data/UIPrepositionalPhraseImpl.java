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

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Location;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;

/*
  Define a prepositional phrase object that is created from the XML returned by
  a PlanServiceProvider.
*/

public class UIPrepositionalPhraseImpl implements UIPrepositionalPhrase {
  PrepositionalPhrase prepPhrase;

  public UIPrepositionalPhraseImpl(PrepositionalPhrase prepPhrase) {
    this.prepPhrase = prepPhrase;
  }

  public String getPreposition() {
    return prepPhrase.getPreposition();
  }

  /** @return String - the class of the indirect object.
    Returns one of: "Asset", "Location", "Schedule", "Requisition", "OPlan".
  */

  public String getIndirectObjectType() {
    String className = prepPhrase.getIndirectObject().getClass().toString();
    int i = className.lastIndexOf(".");
    return (className.substring(i+1));
  }

  public String getUIAssetUUID() {
    Object obj = prepPhrase.getIndirectObject();
    Class assetClass = null;
    try {
      assetClass = Class.forName("org.cougaar.planning.ldm.asset.Asset");
    } catch (Exception e) {
      System.out.println(e);
    }
    if (assetClass.isInstance(obj)) {
      UID uid = ((Asset)obj).getUID();
      if (uid == null)
        return null;
      return uid.toString();
    } else
      return null;
  }


  /** @return UILocation - the UI representation of the location indirect object. */
  public UILocation getLocation() {
    Object obj = prepPhrase.getIndirectObject();
    Class locationClass = null;
    try {
      locationClass = Class.forName("org.cougaar.planning.ldm.plan.Location");
    } catch (Exception e) {
      System.out.println(e);
    }
    if (locationClass.isInstance(obj))
      return new UILocationImpl((Location)obj);
    else
      return null;
  }

  /** @return UISchedule - the UI representation of the schedule indirect object. */

  public UISchedule getSchedule() {
    Object obj = prepPhrase.getIndirectObject();
    Class scheduleClass = null;
    try {
      scheduleClass = Class.forName("org.cougaar.planning.ldm.plan.Schedule");
    } catch (Exception e) {
      System.out.println(e);
    }
    if (scheduleClass.isInstance(obj))
      return new UIScheduleImpl((Schedule)obj);
    else
      return null;
  }

  /** @return UIRequisition - the UI representation of the requisition indirect object. */
  // NOT IMPLEMENTED YET
  public UIRequisition getRequisition() {
    return null;
  }

  /** @return UIOPlan - the UI representation of the OPlan indirect object. */
  // NOT IMPLEMENTED YET
  public UIOPlan getOPlan() {
    return null;
  }

}

