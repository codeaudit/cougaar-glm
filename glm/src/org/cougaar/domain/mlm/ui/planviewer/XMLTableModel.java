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

public class XMLTableModel extends AbstractTableModel {
  public String[] headers;
  // paths correspond to headers, except that prepositions are special cased
  public String[] paths;
  public TXElement[] children;

  public XMLTableModel(byte[] buffer, String logPlanObjectName) {
    TXDocument doc = null;

    try {
      Parser p = new Parser("Log Plan");
      doc = p.readStream(new ByteArrayInputStream(buffer));
    } catch (Exception e) {
      e.printStackTrace();
    }
    TXElement root = (TXElement)doc.getDocumentElement();
    children = root.searchChildrenAll(logPlanObjectName);
  }

  public int getColumnCount() {
    return headers.length;
  }

  public int getRowCount() {
    return children.length;
  }

  public String getValueOfNode(TXElement node, String[] nodeNames, int index) {
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

  public String[] getNodeNamesFromPath(String s) {
    StringTokenizer st = new StringTokenizer(s, ".");
    String[] nodeNames = new String[st.countTokens()];
    for (int i = 0; i < nodeNames.length; i++)
      nodeNames[i] = st.nextToken();
    return nodeNames;
  }

  public Object getValueAt(int row, int col) {
    TXElement child = children[row];
    String header = headers[col];
    String[] nodeNames = getNodeNamesFromPath(paths[col]);
    String s = getValueOfNode(child, nodeNames, 0);
    return s;
  }

  public String getColumnName(int column) {
    return headers[column];
  }

}






