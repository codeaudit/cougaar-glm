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

import org.w3c.dom.Node;
import org.cougaar.core.domain.LDMServesPlugin;
import org.cougaar.planning.ldm.asset.Asset;
import org.w3c.dom.NodeList;

import org.cougaar.util.log.*;

/**
 * Parses direct objects of tasks in test input files.
 */
public class DirectObjectParser{

  public static Asset getDirectObject(LDMServesPlugin ldm, Node node){
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
	    logger.debug ("DirectObjectParser - XML Syntax error : " + 
				"expecting one of <asset>, <aggregateasset>, or <assetgroup> but got <" + childname + ">");
	  }
	}
      }
    }
    catch(Exception e){
      logger.error("exception " + e.getMessage(), e);
    }

    return asset;
  }

  private DirectObjectParser(){}

  private static Logger logger=LoggerFactory.getInstance().createLogger("DirectObjectParser");
}
