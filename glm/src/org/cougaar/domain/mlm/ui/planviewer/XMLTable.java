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

import java.io.ByteArrayInputStream;
import java.util.StringTokenizer;

import javax.swing.table.AbstractTableModel;

import com.ibm.xml.parser.Parser;
import com.ibm.xml.parser.TXDocument;
import com.ibm.xml.parser.TXElement;

public class XMLTable extends AbstractTableModel {

  String[] headers = {"source", "destination", "verb", "ID", "parentTask", "priority", "DirectObject Type", "DirectObject ID", "To", "From", "For", "OfType" };
  // paths correspond to headers, except that prepositions are special cased
  String[] paths = { "source.address", "destination.address", "verb", "ID", "parentTask", "priority", "directObject.TypeIdentificationPG_type_identification", "directObject.ItemIdentificationPG_item_identification" };
  String[] prepositions = { "To", "From", "For", "OfType" };
  TXElement[] children;

  public XMLTable(byte[] buffer) {
    TXDocument doc = null;

    try {
      Parser p = new Parser("Log Plan");
      doc = p.readStream(new ByteArrayInputStream(buffer));
    } catch (Exception e) {
      e.printStackTrace();
    }

    TXElement root = (TXElement)doc.getDocumentElement();
    children = root.searchChildrenAll("Task");
  }

  public int getColumnCount() {
    return headers.length;
  }

  public int getRowCount() {
    return children.length;
  }

  private String getValueOfNode(TXElement node, String[] nodeNames, int index) {
    TXElement[] elements = node.searchChildrenAll(nodeNames[index]);
    if (elements.length != 0) {
      if (index == nodeNames.length-1)
        return elements[0].getFirstChild().getNodeValue();
      else
        for (int i = 0; i < elements.length; i++)
          return getValueOfNode(elements[i], nodeNames, index+1);
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

  private String getPreposition(TXElement child, String s) {
    TXElement[] prepPhrases = child.searchChildrenAll("prepositionalPhrases");
    for (int i = 0; i < prepPhrases.length; i++) {
      TXElement[] prepNode = prepPhrases[i].searchChildrenAll("preposition");
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

  public String getColumnName(int column) {
    return headers[column];
  }

}






