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

import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;

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
    PlanningFactory ldmfactory = getLDM().getFactory();
           
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
