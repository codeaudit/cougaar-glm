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

package org.cougaar.lib.xml.parser;
import java.util.Date;
import java.util.Vector;

import org.cougaar.lib.util.UTILAsset;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AssetGroup;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
