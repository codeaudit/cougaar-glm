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

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLTableModel extends AbstractTableModel {
  public String[] headers;
  // paths correspond to headers, except that prepositions are special cased
  public String[] paths;
  public NodeList children;

  public XMLTableModel(byte[] buffer, String logPlanObjectName) {
    Document doc = null;

    try {
      DOMParser p = new DOMParser();
      p.parse(new InputSource(new ByteArrayInputStream(buffer)));
      doc = p.getDocument();
    } catch (Exception e) {
      e.printStackTrace();
    }
    Element root = doc.getDocumentElement();
    children = root.getElementsByTagName(logPlanObjectName);
  }

  public int getColumnCount() {
    return headers.length;
  }

  public int getRowCount() {
    return children.getLength();
  }

  public String getValueOfNode(Element node, String[] nodeNames, int index) {
    NodeList elements = node.getElementsByTagName(nodeNames[index]);
    if (elements.getLength() != 0) {
      if (index == nodeNames.length-1)
        return elements.item(0).getFirstChild().getNodeValue();
      else
        for (int i = 0; i < elements.getLength(); i++)
          return getValueOfNode((Element)elements.item(i), nodeNames, index+1);
    }
    return null;
  }

  public String[] getNodeNamesFromPath(String s) {
    StringTokenizer st = new StringTokenizer(s, ".");
    String[] nodeNames = new String[st.countTokens()];
    for (int i = 0; i < nodeNames.length; i++)
      nodeNames[i] = st.nextToken();
    return nodeNames;
  }

  public Object getValueAt(int row, int col) {
    Element child = (Element)children.item(row);
    String header = headers[col];
    String[] nodeNames = getNodeNamesFromPath(paths[col]);
    String s = getValueOfNode(child, nodeNames, 0);
    return s;
  }

  public String getColumnName(int column) {
    return headers[column];
  }

}






