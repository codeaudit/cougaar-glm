/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer;

import com.ibm.xml.parser.TXElement;

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

  private String getPreposition(TXElement child, String s) {
    TXElement[] prepPhrases = child.searchChildrenAll("prepositionalPhrases");
    for (int i = 0; i < prepPhrases.length; i++) {
      TXElement[] prepNode = prepPhrases[i].searchChildrenAll("preposition");
      if ((prepNode == null) || (prepNode.length <= 0)) {
        continue;
      }
      TXElement node = prepNode[0];
      String preposition = node.getFirstChild().getNodeValue();
      if (preposition.equals(s)) {
        if (s.equals("To") || s.equals("From")) {
          String[] nodeNames = { "indirectObject", "name" };
          return getValueOfNode(prepPhrases[i], nodeNames, 0);
        }
        if (s.equals("For")) {
          String[] nodeNames = { "indirectObject", "ItemIdentificationPG_item_identification" };
          return getValueOfNode(prepPhrases[i], nodeNames, 0);
        }
        if (s.equals("OfType")) {
          String[] nodeNames = { "indirectObject", "TypeIdentificationPG_type_identification" };
          return getValueOfNode(prepPhrases[i], nodeNames, 0);
        }
      }
    }
    return null;
  }

  public Object getValueAt(int row, int col) {
    TXElement child = children[row];
    String header = headers[col];
    if (isPreposition(header))
      return getPreposition(child, header);
    String[] nodeNames = getNodeNamesFromPath(paths[col]);
    String s = getValueOfNode(child, nodeNames, 0);
    return s;
  }
}
