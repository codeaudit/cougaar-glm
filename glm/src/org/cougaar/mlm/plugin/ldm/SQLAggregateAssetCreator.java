/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.mlm.plugin.ldm;

import java.util.Calendar;
import java.util.Date;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.planning.ldm.plan.NewRoleSchedule;
import org.cougaar.planning.ldm.plan.NewSchedule;


/** AggregateAssetCreator implements QueryHandler to create sql queries 
 * and process the query results by creating aggregateassets.
 */

public class SQLAggregateAssetCreator extends PeriodicQuery {
  public SQLAggregateAssetCreator() {}

  public String getQuery() { return (String) getParameter("query");  }

  public void processRow(Object[] data) {
    //System.out.println("I see "+data[1]+" of "+data[0]);
        
    String nsn = (String)data[0];
    Number count = (Number)data[1];
    String nomenclature = (String) data[2];

    String tid = "NSN/" + nsn;
    // System.out.println(myMessageAddress.getAddress() + ": " +
    //                   "Creating aggregate asset : " + tid + " " +
    //                   count  + " " + nomenclature);

    Asset proto = findPrototype(tid, nomenclature);
    Asset newaggasset = createAggregateAsset(proto,  count.intValue());
    setupAvailableSchedule(newaggasset);
    publishAdd(newaggasset);
  }
  
  /** find or create a prototype asset suitable for using in an
   * aggregate asset.
   **/
  private Asset findPrototype(String tid, String nomenclature) {
    // find it if it already exists
    Asset proto = ldm.getPrototype(tid);

    // if it wasn't found, make one and cache it
    if (proto == null) {
      proto = ldmf.createPrototype("Asset", tid);
      ldm.cachePrototype(tid, proto);
    }

    // set the nomenclature if needed (the prototypeProvider should be doing this)
    TypeIdentificationPG tip = proto.getTypeIdentificationPG();
    if (tip.getNomenclature() == null && tip instanceof NewTypeIdentificationPG) {
      ((NewTypeIdentificationPG) tip).setNomenclature(nomenclature);
    }

    return proto;
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
