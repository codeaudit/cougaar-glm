/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.xml.parser;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;

import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AssetGroup;

import org.cougaar.lib.util.UTILAsset;

import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Creates an asset group -- primarily used in parsing test input files.
 */
public class AssetGroupParser{

  public static AssetGroup getAssetGroup(LDMServesPlugIn ldm, Node node){
    AssetGroup ag = null;
    try{
      NodeList  nlist    = node.getChildNodes();      
      int       nlength  = nlist.getLength();
      String    id       = node.getAttributes().getNamedItem("id").getNodeValue();
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
