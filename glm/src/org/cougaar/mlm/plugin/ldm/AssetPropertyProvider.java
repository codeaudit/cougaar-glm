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

package org.cougaar.mlm.plugin.ldm;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.ldm.plan.NewScheduleElement;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.glm.ldm.plan.NewQuantityScheduleElement;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.ItemIdentificationPGImpl;
import org.cougaar.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.core.domain.RootFactory;

import org.cougaar.util.UnaryPredicate;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/** A QueryHandler which can convert a TypeIdentification code
 * into an Asset Prototype.
 **/

public class AssetPropertyProvider 
	extends PropertyProvider 
{
  public AssetPropertyProvider() {}

  public void provideProperties(Asset asset) {
    try {
      NewTypeIdentificationPG p1 = (NewTypeIdentificationPG)asset.getTypeIdentificationPG();
      String nomen = NSNtoNomenclature(p1.getTypeIdentification());
      p1.setNomenclature(nomen);
    } catch (Exception ee) {
      System.out.println(ee.toString());
      ee.printStackTrace();
    }
  }

  private String NSNtoNomenclature(String nsn) {
    // hel
    if (nsn.equals("1520011069519"))// hel
      return "Helicopter";

    if (nsn.equals("2350010871095")) // tank
      return "M1A1 Tank";

    if (nsn.equals("2350013050028")) // hwtz
      return " Howitzer";
    
    return "Recovery Vehicle";
  }

  // fake query
  public String getQuery() { return null; }
  public void processRow(Object[] row) { return; }

}
