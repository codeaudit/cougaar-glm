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
 
package org.cougaar.domain.mlm.ui.planviewer;

import java.io.ByteArrayInputStream;
import java.util.StringTokenizer;

import javax.swing.table.AbstractTableModel;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLTable extends AbstractTableModel {

  String[] headers = {"source", "destination", "verb", "ID", "parentTask", "priority", "DirectObject Type", "DirectObject ID", "To", "From", "For", "OfType" };
  // paths correspond to headers, except that prepositions are special cased
  String[] paths = { "source.address", "destination.address", "verb", "ID", "parentTask", "priority", "directObject.TypeIdentificationPG_type_identification", "directObject.ItemIdentificationPG_item_identification" };
  String[] prepositions = { "To", "From", "For", "OfType" };
  NodeList children;

  public XMLTable(byte[] buffer) {
    Document doc = null;

    try {
      DOMParser p = new DOMParser();
      p.parse(new InputSource(new ByteArrayInputStream(buffer)));
      doc = p.getDocument();
    } catch (Exception e) {
      e.printStackTrace();
    }

    Element root = doc.getDocumentElement();
    children = root.getElementsByTagName("Task");
  }

  public int getColumnCount() {
    return headers.length;
  }

  public int getRowCount() {
    return children.getLength();
  }

  private String getValueOfNode(Element node, String[] nodeNames, int index) {
    NodeList list = node.getElementsByTagName(nodeNames[index]);

    if( list.getLength() != 0 ) {
      if (index == nodeNames.length-1)
	return list.item(0).getFirstChild().getNodeValue();
      else
	for(int i=0; i < list.getLength(); i++) 
	  return getValueOfNode((Element)list.item(i), nodeNames, index+1);
    }
    return null;
  }

  private String[] getNodeNamesFromPath(String s) {
    StringTokenizer st = new StringTokenizer(s, ".");
    String[] nodeNames = new String[st.countTokens()];
    for (int i = 0; i < nodeNames.length; i++)
      nodeNames[i] = st.nextToken();
    return nodeNames;
  }

  private boolean isPreposition(String s) {
    for (int i = 0; i < prepositions.length; i++)
      if (s.equals(prepositions[i]))
        return true;
    return false;
  }

  private String getPreposition(Element child, String s) {
    NodeList prepPhrases = child.getElementsByTagName("prepositionalPhrases");
    
    for( int i=0; i < prepPhrases.getLength(); i++) {
      NodeList prepNode = 
           ((Element)prepPhrases.item(i)).getElementsByTagName("preposition");
      Element node = (Element)prepNode.item(0);
      String preposition = node.getFirstChild().getNodeValue();
      if (preposition.equals(s)) {
        if (s.equals("To") || s.equals("From")) {
          String[] nodeNames = { "indirectObject", "name" };
          return getValueOfNode((Element)prepPhrases.item(i), nodeNames, 0);
        }
        if (s.equals("For")) {
          String[] nodeNames = { "indirectObject", "ItemIdentificationPG_item_identification" };
          return getValueOfNode((Element)prepPhrases.item(i), nodeNames, 0);
        }
        if (s.equals("OfType")) {
          String[] nodeNames = { "indirectObject", "TypeIdentificationPG_type_identification" };
          return getValueOfNode((Element)prepPhrases.item(i), nodeNames, 0);
        }
      }
    }
    return null;
  }

  public Object getValueAt(int row, int col) {
    Element child = (Element)children.item(row);
    String header = headers[col];
    if (isPreposition(header))
      return getPreposition(child, header);
    String[] nodeNames = getNodeNamesFromPath(paths[col]);
    String s = getValueOfNode(child, nodeNames, 0);
    return s;
  }

  public String getColumnName(int column) {
    return headers[column];
  }

}






