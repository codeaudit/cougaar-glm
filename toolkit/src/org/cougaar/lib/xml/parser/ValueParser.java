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

