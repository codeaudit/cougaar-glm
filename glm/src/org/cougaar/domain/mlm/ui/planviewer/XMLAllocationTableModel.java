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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ibm.xml.parser.TXElement;

public class XMLAllocationTableModel extends XMLTableModel {
  private String[] allocationHeaders = {"Asset Type ID", "Asset Item ID", "ID", "Task ID", "Confidence Rating", "Success", "Start Time", "End Time" };
  // paths match headers, except start and end times treated special
  private String[] allocationPaths = { "asset.TypeIdentificationPG_type_identification", "asset.ItemIdentificationPG_item_identification", "UID.UID", "task.ID", "estimatedResult.confidenceRating", "estimatedResult.success" };

  public XMLAllocationTableModel(byte[] buffer) {
    super(buffer, "Allocation");
    this.headers = allocationHeaders;
    this.paths = allocationPaths;
  }

  // special case getting start and end times
  // if estimatedResult.aspectTypes[i] = START_TIME, 
  // then use estimatedResult.results[i]
  // if estimatedResult.aspectTypes[i] = END_TIME,
  // then use estimatedResult.results[i]

  /* Called with estimatedResult node;
     first "results" child is startDate, second is endDate.
     Dates are of the form:
     DDD mmm dd hh:mm:ss EDT yyyy
     This strips the leading DDD (day of the week).
     */

  private Object getTime(TXElement node, String s) {
    TXElement[] elements = node.searchChildrenAll("estimatedResult");
    if (elements.length == 0)
      return null;
    node = elements[0];
    TXElement[] aspectTypes = node.searchChildrenAll("aspectTypes");
    TXElement[] results = node.searchChildrenAll("results");
    SimpleDateFormat formatter = 
      new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
    ParsePosition pos = new ParsePosition(0);
    for (int i = 0; i < aspectTypes.length; i++) {
      if (s.equals(aspectTypes[i].getFirstChild().getNodeValue()))
        return formatter.parse(results[i].getFirstChild().getNodeValue(), pos);
    }
    return null;
  }

  public Object getValueAt(int row, int col) {
    TXElement child = children[row];
    String header = headers[col];
    if (header.equals("Start Time"))
      return getTime(child, "START_TIME");
    else if (header.equals("End Time"))
      return getTime(child, "END_TIME");
    String[] nodeNames = getNodeNamesFromPath(paths[col]);
    String s = getValueOfNode(child, nodeNames, 0);
    return s;
  }

  public Class getColumnClass(int column) {
    if ((column == 6) || column == 7)
      return Date.class;
    return String.class;
  }


}
