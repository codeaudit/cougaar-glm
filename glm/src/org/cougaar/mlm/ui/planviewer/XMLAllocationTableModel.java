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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

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

  private Object getTime(Element node, String s) {
    NodeList elements = node.getElementsByTagName("estimatedResult");
    if (elements.getLength() == 0)
      return null;
    node = (Element)elements.item(0);
    NodeList aspectTypes = node.getElementsByTagName("aspectTypes");
    NodeList results = node.getElementsByTagName("results");
    SimpleDateFormat formatter = 
      new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
    ParsePosition pos = new ParsePosition(0);
    for (int i = 0; i < aspectTypes.getLength(); i++) {
      if (s.equals(aspectTypes.item(i).getFirstChild().getNodeValue()))
        return formatter.parse(results.item(i).getFirstChild().getNodeValue(), pos);
    }
    return null;
  }

  public Object getValueAt(int row, int col) {
    Element child = (Element)children.item(row);
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
