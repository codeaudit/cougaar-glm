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

import org.cougaar.lib.util.UTILAsset;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Node;

/**
 * Creates an asset instance.
 */
public class AssetParser{
  public AssetParser(Logger logger){ 
    this.logger = logger;
    assetHelper = new UTILAsset (logger);
  }

  public Asset getAsset(LDMServesPlugin ldm, Node node){
    Asset asset = null;
    String data = null;
    String bumperno = null;
    try {
      bumperno = node.getAttributes().getNamedItem("id").getNodeValue();
    } catch (Exception e) {
      logger.error("\nGot exception processing Node <" + 
		   node.getNodeName() + 
		   ">.  Missing id attribute.  It gives the asset a unique item id.");
    }
    try {
      data  = node.getFirstChild().getNodeValue();
    } catch(Exception e){
      logger.error("\nGot exception processing Node <" + 
		   node.getNodeName() + ">.  Expecting prototype name to be in body of tag.");
    }
    try {
      asset = assetHelper.createInstance(ldm, data, bumperno);
    } catch(RuntimeException e){
      logger.error("\nGot exception processing Node <" + 
		   node.getNodeName() + 
		   ">.  Could not create instance of " + data + " with unique id " + bumperno);
    }
    return asset; 
  }

  protected Logger logger;
  protected UTILAsset assetHelper;
}
