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

import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AssetGroup;

import org.cougaar.lib.util.UTILAsset;

import java.util.Date;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Creates an asset group -- primarily used in parsing test input files.
 */
public class AssetGroupParser{

  public static AssetGroup getAssetGroup(LDMServesPlugin ldm, Node node){
    AssetGroup ag = null;
    try{
      NodeList  nlist    = node.getChildNodes();      
      int       nlength  = nlist.getLength();
      Node      idNode   = node.getAttributes().getNamedItem("id");
	  String id = null;
	  
	  if (idNode == null)
		id = "" + new Date ().getTime ();
	  else
        id = idNode.getNodeValue();

      Vector    assets   = new Vector();

      for(int i = 0; i < nlength; i++){
	Node    child       = nlist.item(i);
	String  childname   = child.getNodeName();

	if(childname.equals("asset")){
	  Asset asset = AssetParser.getAsset(ldm, child);
	  assets.addElement(asset);
	}
	else if(childname.equals("aggregateasset")){
	  AggregateAsset asset = AggregateAssetParser.getAggregate(ldm, child);
	  assets.addElement(asset);
	}
	else if(childname.equals("assetgroup")){
	  AssetGroup asset = AssetGroupParser.getAssetGroup(ldm, child);
	  assets.addElement(asset);
	}
	else if(!childname.equals("#text")){
	  System.out.println ("AssetGroupParser - XML Syntax error : " + 
			      "expecting one of <asset>, <aggregateasset>, or <assetgroup> but got <" + childname + ">");
	}
      }
      if (ldm.getFactory() == null) 
	System.err.println("WARNING: LDM Factory is null in xmlparser!");
      ag = UTILAsset.makeAssetGroup(ldm.getFactory(), id);
      ag.setAssets(assets);
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      e.printStackTrace();
    }

    return ag; 
  }

  private AssetGroupParser(){}

}
