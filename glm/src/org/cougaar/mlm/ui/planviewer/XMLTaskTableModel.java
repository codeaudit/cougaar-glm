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
 
package org.cougaar.mlm.ui.planviewer;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLTaskTableModel extends XMLTableModel {
  private String[] taskHeaders = {"source", "destination", "verb", "ID", "parentTask", "priority", "DirectObject Type", "DirectObject ID", "To", "From", "For", "OfType" };
  // paths correspond to headers, except that prepositions are special cased
  private String[] taskPaths = { "source.address", "destination.address", "verb", "ID", "parentTask", "priority", "directObject.TypeIdentificationPG_type_identification", "directObject.ItemIdentificationPG_item_identification" };
  private String[] prepositions = { "To", "From", "For", "OfType" };

  public XMLTaskTableModel(byte[] buffer) {
    super(buffer, "Task");
    this.headers = taskHeaders;
    this.paths = taskPaths;
  }

  private boolean isPreposition(String s) {
    for (int i = 0; i < prepositions.length; i++)
      if (s.equals(prepositions[i]))
        return true;
    return false;
  }

  private String getPreposition(Element child, String s) {
    NodeList prepPhrases = child.getElementsByTagName("prepositionalPhrases");
    for (int i = 0; i < prepPhrases.getLength(); i++) {
      NodeList prepNode = ((Element)prepPhrases.item(i)).getElementsByTagName("preposition");
      if ((prepNode == null) || (prepNode.getLength() <= 0)) {
        continue;
      }
      Element node = (Element) prepNode.item(0);
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
}
