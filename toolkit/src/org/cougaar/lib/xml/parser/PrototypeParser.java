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
import org.w3c.dom.NodeList;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.asset.Asset;

/**
 * Parses prototype tags in an ldm.xml file.  Caches the prototype
 * in the ldm.
 */
public class PrototypeParser {
  public static boolean debug = false;

  public static void setDebug (boolean d) { debug = d; }

    public static void cachePrototype(LDMServesPlugIn ldm, Node node) {
      cachePrototype(ldm, node, false); 
    }
    public static Asset cachePrototype(LDMServesPlugIn ldm, Node node,
					boolean return_first) {
   
    LDMServesPlugIn myLDMServesPlugIn = ldm;
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
      //      if(!myLDMServesPlugIn.isPrototypeCached(prototypeName)) {
      //
      for(int i = 0; i < nlength; i++) {
	Node    child       = nlist.item(i);
	String  childname   = child.getNodeName();
	
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

