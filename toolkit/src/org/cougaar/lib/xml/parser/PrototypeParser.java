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
import org.w3c.dom.NodeList;
import org.cougaar.core.agent.ClusterServesPlugin;
import org.cougaar.core.domain.LDMServesPlugin;
import org.cougaar.planning.ldm.asset.Asset;

/**
 * Parses prototype tags in an ldm.xml file.  Caches the prototype
 * in the ldm.
 */
public class PrototypeParser {
  public static boolean debug = false;

  public static void setDebug (boolean d) { 
    debug = d; 
    if (debug)
      System.out.println ("PrototypeParser - debug set to true");
  }

    public static void cachePrototype(LDMServesPlugin ldm, Node node) {
      cachePrototype(ldm, node, false); 
    }
    public static Asset cachePrototype(LDMServesPlugin ldm, Node node,
					boolean return_first) {
   
    LDMServesPlugin myLDMServesPlugin = ldm;
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

	if (debug)
	  System.out.println ("PrototypeParser.cachePrototype - child " + childname);
	
	if(child.getNodeType() == Node.ELEMENT_NODE) {
	  if (childname.equals("object")) { 
	    prototype = (Asset)ObjectParser.getObject(ldm, child);
	    if (return_first) return prototype;
	    ldm.cachePrototype(prototypeName,prototype);
	    if (debug)
	      System.out.println ("Caching " + prototypeName + " -> " + prototype + " UID " + prototype.getUID() + " in " + ldm);
	    if (!ldm.isPrototypeCached (prototypeName))
	      System.out.println ("HUH? " + prototypeName + " not cached?");
	    
	  }
	}
      } 
    }
  return prototype;
 }
}

