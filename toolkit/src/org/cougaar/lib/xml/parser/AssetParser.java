/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.xml.parser;

import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.asset.Asset;

import org.w3c.dom.Node;

import org.cougaar.lib.util.UTILAsset;

/**
 * Creates an asset instance.
 */
public class AssetParser{

  public static Asset getAsset(LDMServesPlugIn ldm, Node node){
    Asset asset = null;
    String data = null;
    try{
      String bumperno = node.getAttributes().getNamedItem("id").getNodeValue();
      data            = node.getFirstChild().getNodeValue();
      asset = UTILAsset.createInstance(ldm, data, bumperno);
    }
    catch(Exception e){
      System.err.println("\nGot exception processing Node <" + 
			 node.getNodeName() + ">. value = " + data);
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
    return asset; 
  }

  private AssetParser(){}

}
