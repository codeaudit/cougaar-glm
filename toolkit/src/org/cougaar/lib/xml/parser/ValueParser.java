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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;

/**
 * Parses the value portion of an object in an xml file.
 */
public class ValueParser{
  
  public static Object getValue(LDMServesPlugIn ldm, Node node){
    // Note need the LDMServesPlugIn to be able to call ObjectParser.

    Object retval = null;

    if( node.getNodeName().equals("value") ) {
      NodeList  nlist    = node.getChildNodes();      
      int       nlength  = nlist.getLength();
      
      for(int i = 0; i < nlength; i++){
      	Node    child       = nlist.item(i);
      	String  childname   = child.getNodeName();

	if(child.getNodeType() == Node.ELEMENT_NODE){
	  
	  if(childname.equals("object")){
            retval = ObjectParser.getObject(ldm, child);
          }
	  else if(childname.equals("array")){
	    // BOZO: need to do a loop and get the 
	    // list one by one.
            ValueParser.getValue(ldm, child);
          }
	}
	else if(child.getNodeType() == Node.TEXT_NODE ){
	  // PCData
	  String data = child.getNodeValue().trim();
	  if(data.length() != 0){
	    retval = data;
	  }
	}
      }
    }

    return retval;
  }
}

