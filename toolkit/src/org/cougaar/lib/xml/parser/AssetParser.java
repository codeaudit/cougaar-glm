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

import org.cougaar.core.domain.LDMServesPlugin;
import org.cougaar.planning.ldm.asset.Asset;

import org.w3c.dom.Node;

import org.cougaar.lib.util.UTILAsset;

/**
 * Creates an asset instance.
 */
public class AssetParser{

  public static Asset getAsset(LDMServesPlugin ldm, Node node){
    Asset asset = null;
    String data = null;
    String bumperno = null;
    try {
      bumperno = node.getAttributes().getNamedItem("id").getNodeValue();
    } catch (Exception e) {
      System.err.println("\nGot exception processing Node <" + 
			 node.getNodeName() + ">.  Missing id attribute.  It gives the asset a unique item id.");
    }
    try {
      data  = node.getFirstChild().getNodeValue();
    } catch(Exception e){
      System.err.println("\nGot exception processing Node <" + 
			 node.getNodeName() + ">.  Expecting prototype name to be in body of tag.");
    }
    try {
      asset = UTILAsset.createInstance(ldm, data, bumperno);
    } catch(Exception e){
      System.err.println("\nGot exception processing Node <" + 
			 node.getNodeName() + ">.  Could not create instance of " + data + " with unique id " + bumperno);
    }
    return asset; 
  }

  private AssetParser(){}

}
