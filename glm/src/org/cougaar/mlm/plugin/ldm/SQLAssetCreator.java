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

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.NewSchedule;
import org.cougaar.planning.ldm.plan.NewRoleSchedule;

import java.util.Calendar;
import java.util.Date;

/** AssetCreator is a subclass of AggregateAssetCreator which creates sql queries
 * (through its superclass) and processes the query results by creating assets with bumpernumbers.
 */

public class SQLAssetCreator extends PeriodicQuery {
    
  public SQLAssetCreator() { }

  public void processRow(Object[] data) {
    //System.out.println("I see "+data[1]+" of "+data[2]);
      
    String nsn = (String)data[0];
    Number count = (Number)data[1];
    String nomenclature = (String) data[2];

    //System.out.println(myMessageAddress.getAddress() + ": " +
		//       "Creating " + count + " instances of NSN/" + nsn + " " + nomenclature);

    for (int i = 1; i <= count.intValue() ; i++) {
      String bumper = createUniqueID(myMessageAddress.getAddress(), nsn, i);
      Asset newasset = createAsset("NSN/" + nsn, bumper, nomenclature);
      setupAvailableSchedule(newasset);
      
      publishAdd(newasset);
    }
    
  }

  public String getQuery() { return (String) getParameter("query");  }

  private String createUniqueID(String cluster, String nsn, int id)
  {
    return cluster + "-" + nsn + "-" + id;
  }
  
  protected Asset createAsset(String prototype, 
			      String uniqueid, String nomenclature) 
  {
    PlanningFactory ldmfactory = getLDM().getFactory();
        
    //    System.out.println("Creating asset : " + prototype + " " + 
    //		       uniqueid + " " + nomenclature);
        
    return ldmfactory.createInstance(prototype, uniqueid);

  }
  
  private void setupAvailableSchedule(Asset asset) {
    PlanningFactory ldmfactory = getLDM().getFactory();
    Calendar mycalendar = Calendar.getInstance();
    // set the start date of the available schedule to 01/01/1990
    mycalendar.set(1990, 0, 1, 0, 0, 0);
    Date start = mycalendar.getTime();
    // set the end date of the available schedule to 01/01/2010
    mycalendar.set(2010, 0, 1, 0, 0, 0);
    Date end = mycalendar.getTime();
    NewSchedule availsched = ldmfactory.newSimpleSchedule(start, end);
    // set the available schedule
    ((NewRoleSchedule)asset.getRoleSchedule()).setAvailableSchedule(availsched);
  }
    
  
        
}
