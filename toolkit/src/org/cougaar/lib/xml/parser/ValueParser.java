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
import org.cougaar.util.log.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses the value portion of an object in an xml file.
 */
public class ValueParser{

  public ValueParser (Logger logger, ObjectParser objectParser) { 
    this.objectParser = objectParser;
  }

  public Object getValue(LDMServesPlugin ldm, Node node){
    // Note need the LDMServesPlugin to be able to call ObjectParser.

    Object retval = null;

    if( node.getNodeName().equals("value") ) {
      NodeList  nlist    = node.getChildNodes();      
      int       nlength  = nlist.getLength();
      
      for(int i = 0; i < nlength; i++){
      	Node    child       = nlist.item(i);
      	String  childname   = child.getNodeName();

	if(child.getNodeType() == Node.ELEMENT_NODE){
	  
	  if(childname.equals("object")){
            retval = objectParser.getObject(ldm, child);
          }
	  else if(childname.equals("array")){
	    // BOZO: need to do a loop and get the 
	    // list one by one.
            getValue(ldm, child);
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

  protected ObjectParser objectParser;
}

