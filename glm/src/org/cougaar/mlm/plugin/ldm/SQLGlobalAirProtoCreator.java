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
import org.cougaar.core.domain.RootFactory;

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
          pr = myLDMPlugin.getPrototype(protoname);
         
      return pr;
    } catch (Exception ee) {
      System.out.println(ee.toString());
      ee.printStackTrace();
    }
    return null;
  }


  
}
