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

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.asset.Asset;
import java.util.Vector;

public class QueryHandlerExample extends QueryHandler {
  public QueryHandlerExample() {}

  public String getQuery() {
    return "select nsn, count from inventory where nsn in ("+
      getParameter("nsns")+
      ")";
  }

  public void processRow(Object[] data) {
    System.out.println("I see "+data[1]+" of "+data[0]);
      
    /*
    String nsn = data.elementAt(0);
    Integer count = data.elementAt(1);
    Asset newasset = createAggregateAsset(nsn, count.intValue());
    myAssets.add(newasset);    
    */
  }
}

