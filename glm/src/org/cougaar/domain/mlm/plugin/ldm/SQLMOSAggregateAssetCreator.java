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

package org.cougaar.domain.mlm.plugin.ldm;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.plan.NewSchedule;
import org.cougaar.domain.planning.ldm.plan.NewRoleSchedule;
import org.cougaar.domain.glm.ldm.asset.NewPersonPG;
import org.cougaar.domain.glm.ldm.plan.Skill;
import org.cougaar.domain.glm.ldm.asset.MilitaryPerson;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;


/** AggregateAssetCreator implements QueryHandler to create sql queries 
 * and process the query results by creating aggregateassets.
 */

public class SQLMOSAggregateAssetCreator extends PeriodicQuery {
  public SQLMOSAggregateAssetCreator() {}

  public String getQuery() { return (String) getParameter("query");  }

  public static int MOS=0;     // 0     billet.unfrmd_srvc_occptn_cd 
  public static int QTY=1;     // 1     sum(to_strength)
  public static int OCC=2;     // 2     unfrmd_srvc_occptn_tx 

  public void processRow(Object[] data) {
    //System.out.println("I see "+data[1]+" of "+data[0]);
        
    String nsn = (String)data[0];
    Number count = (Number)data[1];
    String nomenclature = (String) data[2];

    String tid = "NSN/" + nsn;
    // System.out.println(myClusterIdentifier.getAddress() + ": " +
    //                   "Creating aggregate asset : " + tid + " " +
    //                   count  + " " + nomenclature);Skill

    Asset proto = findPrototype(data, tid, nomenclature);
    Asset newaggasset = createAggregateAsset(proto,  count.intValue());
    setupAvailableSchedule(newaggasset);
    publishAdd(newaggasset);
  }
  
  /** find or create a prototype asset suitable for using in an
   * aggregate asset.
   **/
    private Asset findPrototype(Object[] data, String tid, String nomenclature) {
	MilitaryPerson proto = (MilitaryPerson)ldmf.createPrototype("org.cougaar.domain.glm.ldm.asset.MilitaryPerson", tid);

	NewTypeIdentificationPG tip = (NewTypeIdentificationPG)proto.getTypeIdentificationPG();
	proto.setTypeIdentificationPG(tip);
	tip.setTypeIdentification("MOS/"+(String)data[MOS]);
	tip.setNomenclature((String)data[OCC]);

	NewPersonPG pp = (NewPersonPG)ldmf.createPropertyGroup("PersonPG");
	proto.setPersonPG(pp);
	ArrayList al = new ArrayList();
	al.add(new Skill("MOS", (String)data[MOS], (String)data[OCC]));
	pp.setSkills(al);

	ldm.fillProperties(proto);
	ldm.cachePrototype(tid, proto);
//  	System.out.println
//  	    ("SQLMOSAggregateAssetCreator, findPrototype returns: "+proto +" with id: "+tip.getTypeIdentification());
	return (Asset)proto;
    }

  private Asset createAsset(String prototype, String nomenclature) {
    return ldmf.createInstance(prototype, nomenclature);
  }
  
  public Asset createAggregateAsset(Asset asset, int qty) 
  {
    // wrong:
    // return asset.createAggregate(qty);
    // right:
    return ldmf.createAggregate(asset, qty);
  }
  
  private void setupAvailableSchedule(Asset asset) {
    Calendar mycalendar = Calendar.getInstance();
    // set the start date of the available schedule to 01/01/1990
    mycalendar.set(1990, 0, 1, 0, 0, 0);
    Date start = mycalendar.getTime();
    // set the end date of the available schedule to 01/01/2010
    mycalendar.set(2010, 0, 1, 0, 0, 0);
    Date end = mycalendar.getTime();
    NewSchedule availsched = ldmf.newSimpleSchedule(start, end);
    // set the available schedule
    ((NewRoleSchedule)asset.getRoleSchedule()).setAvailableSchedule(availsched);
  }
  
}
