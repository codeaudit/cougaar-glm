/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/UIParser.java,v 1.2 2001-04-16 20:32:36 bkrisler Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.xml;



import java.io.ByteArrayInputStream;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.util.*;
import java.io.*;

import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;

import org.cougaar.domain.mlm.ui.tpfdd.producer.UIVector;
import org.cougaar.domain.mlm.ui.tpfdd.producer.PlanElementProvider;


public class UIParser
{
  protected String tagName;
  protected NodeList nodes;
  protected Element root; // used between parse_one() and getOne/Allxxx()
  private PlanElementProvider provider;

  public UIParser(byte[] buffer, String tagName, PlanElementProvider provider) {
    try {
      Document doc = ConfigFileFinder.parseXMLConfigFile(new String(buffer));
      if(doc != null) {
	root = doc.getDocumentElement();
	nodes = getAllNodesByTag(tagName);
      }
    } catch(IOException e) {
      e.printStackTrace();
    }
    this.tagName = tagName;
    this.provider = provider;
  }
		
  /** @deprecated Changed the <code>Node[]</code> to a NodeList.  **/
  public UIParser(Node[] nodes) {
    throw new IllegalArgumentException("Deprecated, now takes a NodeList");
  }    
	
  /**Constructor **/	
  public UIParser(NodeList nodes) {
    this.nodes = nodes;
  }

  /**
   * getAllNodesByTag
   *
   * Returns all Elements with a given tag name.
   * Note:  For all Elements, use '*' as the tagname.
   *
   * @param tagName Name of tag to match on, use '*' for all tags.
   * @return NodeList of all matching tags.
   */ 
  protected NodeList getAllNodesByTag(String tagName) {
    if(tagName.equals("all")) {
      throw new IllegalArgumentException("Use '*' for all Elements");
    }
    return root.getElementsByTagName(tagName);
  }
    
  protected String[] getAllValuesByTag(String tagName)
  {
    NodeList nodes = root.getElementsByTagName(tagName);
				
    if ( nodes == null ) {
      /* OutputHandler.out("UIP:gOVBT Error: empty parent: " + 
	 element.getTagName() + " while looking for: " + tagName + "!"); */
      return null;
    }

    String[] values = new String[nodes.getLength()];

    for ( int i = 0; i < nodes.getLength(); i++ ) {
      values[i] = nodes.item(i).getFirstChild().getNodeValue();
    }

    return values;
  }
    
  protected String getOneValueByTag(String tagName)
  {
    NodeList nodes = root.getElementsByTagName(tagName);
    return nodes.item(0).getFirstChild().getNodeValue();
  }

  public UIVector parse()
  {
    UIVector objects = new UIVector();

    for ( int i = 0; i < nodes.getLength(); i++ ) {
      objects.add(UIObjectFactory.create((Element)nodes.item(i), provider));
    }

    return objects;
  }

  /** 
   * @deprecated Removed it was only called <code>UIObjectFactory.create</code>
   **/
  public Object parse_one() { 
    throw new IllegalArgumentException("Deprecated");
  }
}

