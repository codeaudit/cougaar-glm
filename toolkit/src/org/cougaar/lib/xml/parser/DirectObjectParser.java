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

import org.w3c.dom.Node;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.w3c.dom.NodeList;

/**
 * Parses direct objects of tasks in test input files.
 */
public class DirectObjectParser{

  public static Asset getDirectObject(LDMServesPlugIn ldm, Node node){
    Asset asset = null;
    try{
      NodeList  nlist    = node.getChildNodes();      
      int       nlength  = nlist.getLength();

      for(int i = 0; i < nlength; i++){
	Node    child       = nlist.item(i);
	String  childname   = child.getNodeName();

        if(child.getNodeType() == Node.ELEMENT_NODE){
	
	  if(childname.equals("asset")){
	    asset = AssetParser.getAsset(ldm, child);
	  }
	  else if(childname.equals("aggregateasset")){
	    asset = AggregateAssetParser.getAggregate(ldm, child);
	  }
	  else if(childname.equals("assetgroup")){
	    asset = AssetGroupParser.getAssetGroup(ldm, child);
	  }
	  else {
	    System.out.println ("DirectObjectParser - XML Syntax error : " + 
				"expecting one of <asset>, <aggregateasset>, or <assetgroup> but got <" + childname + ">");
	  }
	}
      }
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      e.printStackTrace();
    }

    return asset;
  }

  private DirectObjectParser(){}

}
