/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
import org.cougaar.planning.ldm.LDMServesPlugin;

import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AssetGroup;

import org.cougaar.lib.util.UTILAsset;

import java.util.Date;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.util.log.*;

/**
 * Creates an asset group -- primarily used in parsing test input files.
 */
public class AssetGroupParser{
  public AssetGroupParser (Logger log) { 
    logger = log; 
    assetHelper = new UTILAsset (log);
    assetParser = new AssetParser (log);
    aggregateAssetParser = new AggregateAssetParser (log);
  }

  public AssetGroup getAssetGroup(LDMServesPlugin ldm, Node node){
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
	  Asset asset = assetParser.getAsset(ldm, child);
	  assets.addElement(asset);
	}
	else if(childname.equals("aggregateasset")){
	  AggregateAsset asset = aggregateAssetParser.getAggregate(ldm, child);
	  assets.addElement(asset);
	}
	else if(childname.equals("assetgroup")){
	  AssetGroup asset = getAssetGroup(ldm, child); // RECURSE
	  assets.addElement(asset);
	}
	else if(!childname.equals("#text")){
	  logger.error ("AssetGroupParser - XML Syntax error : " + 
			"expecting one of <asset>, <aggregateasset>, or <assetgroup> but got <" + childname + ">");
	}
      }
      if (ldm.getFactory() == null) 
	logger.error("WARNING: LDM Factory is null in xmlparser!");
      ag = assetHelper.makeAssetGroup(ldm.getFactory(), id);
      ag.setAssets(assets);
    }
    catch(Exception e){
      logger.error(e.getMessage(), e);
    }

    return ag; 
  }

  protected Logger logger;
  protected UTILAsset assetHelper;
  protected AssetParser assetParser;
  protected AggregateAssetParser aggregateAssetParser;
}
