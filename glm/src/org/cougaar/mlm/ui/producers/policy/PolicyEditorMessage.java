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
 
package org.cougaar.mlm.ui.producers.policy;

import java.io.ByteArrayOutputStream;
import java.text.ParsePosition;
import java.util.Vector;

import org.cougaar.core.util.AbstractPrinter;

public class PolicyEditorMessage {

  public static final String NAME_LABEL = "NAME=";
  public static final String UID_LABEL = "UID=";

  public static final String COMMAND_LABEL = "COMMAND=";
  public static final String MODIFY_COMMAND = "MODIFY";
  public static final String QUERY_COMMAND = "QUERY";

  public static final String FORMAT_LABEL = "FORMAT=";
  public static final String XML_FORMAT = "xml";
  public static final String HTML_FORMAT = "html";

  public static final String PARAMETERS_LABEL = "PARAMETERS=";

  public static final String DELIM = "?";

  public static String generateModifyRequest(UIPolicyInfo policyInfo) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    AbstractPrinter printer = AbstractPrinter.createPrinter(XML_FORMAT,
                                                            out);
    printer.printObject(policyInfo.getParameters());
    return DELIM + COMMAND_LABEL + MODIFY_COMMAND + DELIM +
      FORMAT_LABEL + XML_FORMAT + DELIM + 
      UID_LABEL + policyInfo.getUID() + DELIM + 
      PARAMETERS_LABEL + out.toString() + DELIM;
  }

  public static String generateQueryRequest() {
    return DELIM + COMMAND_LABEL + QUERY_COMMAND + DELIM + 
      FORMAT_LABEL + XML_FORMAT + DELIM;
  }

  public static String parseParameter(String data, String label, 
                                      String delim, ParsePosition parsePosition) {
    
    int start = data.indexOf(label, parsePosition.getIndex());
    
    if (start == -1) {
      System.out.println("PolicyEditorMessage.parseParameter() -  " + label + 
                         " label not found in " + data);
      return "";
    }

    start += label.length();
    int end = data.indexOf(delim, start);

    if (end == -1) {
      System.out.println("PolicyEditorMessage.parseParameter() -  " + delim + 
                         " delimiter not found after label " + label);
      parsePosition.setErrorIndex(start);
      return "";
    }

    parsePosition.setIndex(end);

    return data.substring(start, end).trim();
  }
}








