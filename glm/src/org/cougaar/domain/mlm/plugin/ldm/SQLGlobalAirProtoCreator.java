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

/** GlobalAirProtoCreator is a subclass of ComAirCreator which creates sql queries
 * (through its superclass) and processes the query results by creating prototype assets
 * for the Planes with hand code generated (fake) tail numbers.
 */

public class SQLGlobalAirProtoCreator extends SQLAggregateAssetCreator {
  public SQLGlobalAirProtoCreator() {
    super();
  }


  public void processRow(Object[] data) {
    //System.out.println("I see "+data[0]+" of "+data[1]);
      
    Number qty = (Number)data[0];
    String actype = (String)data[1];
    
    Asset newasset = createAsset(actype);
    Asset newaggasset = createAggregateAsset(newasset,  qty.intValue());
    publishAdd(newaggasset);    
  }
  
  
  private Asset createAsset(String protoname) {
    RootFactory ldmfactory = getLDM().getFactory();
           
    try {
      	  Asset pr;
	  	  if (myLDMPlugIn != null)
	  	     pr = myLDMPlugIn.getPrototype(protoname);
	  	  else
	     pr = myQueryLDMPlugIn.getPrototype(protoname);

         
      return pr;
    } catch (Exception ee) {
      System.out.println(ee.toString());
      ee.printStackTrace();
    }
    return null;
  }


  
}
