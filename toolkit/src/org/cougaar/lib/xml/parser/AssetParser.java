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
