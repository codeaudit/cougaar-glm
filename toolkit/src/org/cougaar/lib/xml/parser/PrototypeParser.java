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

import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.util.log.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <pre>
 * Parses prototype tags in an ldm.xml file or other prototype file.  
 * Caches the prototype in the ldm.
 * </pre>
 */
public class PrototypeParser {
  /** creates an object parser for parsing the contents of the prototype file */
  public PrototypeParser (Logger log) { 
    logger = log; 
    objectParser = new ObjectParser(log);
  }

  /** 
   * <pre>
   * Cache the prototype with the <tt>ldm</tt>, give the DOM
   * document at <tt>node</tt>.
   *
   * Calls other cachePrototype with return_first set to false.
   * </pre>
   * @param ldm to register the prototype with
   * @param node that defines the prototype
   */ 
  public void cachePrototype(LDMServesPlugin ldm, Node node) {
    cachePrototype(ldm, node, false); 
  }

  /** 
   * <pre>
   * Cache the prototype with the <tt>ldm</tt>, give the DOM
   * document at <tt>node</tt>.
   *
   * Called from XMLPrototypeProviderPlugin. 
   *
   * Most of the real work is done in ObjectParser.
   *
   * </pre>
   * @param ldm to register the prototype with
   * @param node that defines the prototype
   * @param return_first says whether to return the prototype before cacheing it
   * @return prototype defined by node
   * @see org.cougaar.lib.xml.parser.ObjectParser#getObject
   */ 
  public Asset cachePrototype(LDMServesPlugin ldm, Node node,
			      boolean return_first) {
   
    Asset prototype = null;

    if(node.getNodeName().equals("prototype")){
      NodeList  nlist    = node.getChildNodes();      
      int       nlength  = nlist.getLength();

      String prototypeName = node.getAttributes().getNamedItem("name").getNodeValue();
      
      // NOTE: we can't do this check, since the DefaultPrototypeProvider
      // caches some odd (AbstractAsset) values for otherwise normal 
      // (Consumables, Repairables, etc.) assets.
      //:: Check and see if this prototype is already cached (do nothing).
      //:: If prototype is not cached then go ahead and create then cache prototype.
      //      if(!myLDMServesPlugin.isPrototypeCached(prototypeName)) {
      //
      for(int i = 0; i < nlength; i++) {
	Node    child       = nlist.item(i);
	String  childname   = child.getNodeName();

	if (logger.isDebugEnabled())
	  logger.debug ("PrototypeParser.cachePrototype - child " + childname);
	
	if(child.getNodeType() == Node.ELEMENT_NODE) {
	  if (childname.equals("object")) { 
	    prototype = (Asset)objectParser.getObject(ldm, child);
	    if (return_first) return prototype; // return prototype without cacheing
	    ldm.cachePrototype(prototypeName,prototype);
	    if (logger.isDebugEnabled())
	      logger.debug ("Caching " + prototypeName + " -> " + prototype + " UID " + prototype.getUID() + " in " + ldm);
	    if (!ldm.isPrototypeCached (prototypeName))
	      logger.error ("HUH? " + prototypeName + " not cached?");
	    
	  }
	}
      } 
    }
    return prototype;
  }

  protected Logger logger;
  protected ObjectParser objectParser;
}
