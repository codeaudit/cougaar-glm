/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.ldm;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.plan.NewSchedule;
import org.cougaar.domain.planning.ldm.plan.NewRoleSchedule;

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

    //System.out.println(myClusterIdentifier.getAddress() + ": " +
		//       "Creating " + count + " instances of NSN/" + nsn + " " + nomenclature);

    for (int i = 1; i <= count.intValue() ; i++) {
      String bumper = createUniqueID(myClusterIdentifier.getAddress(), nsn, i);
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
    RootFactory ldmfactory = getLDM().getFactory();
        
    //    System.out.println("Creating asset : " + prototype + " " + 
    //		       uniqueid + " " + nomenclature);
        
    return ldmfactory.createInstance(prototype, uniqueid);

  }
  
  private void setupAvailableSchedule(Asset asset) {
    RootFactory ldmfactory = getLDM().getFactory();
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
