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
 
package org.cougaar.domain.mlm.ui.perturbation.asset;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

public class AssetPerturbationMessage {

  public static final String NAME_LABEL = "NAME=";
  public static final String UID_LABEL = "UID=";

  public static final String COMMAND_LABEL = "COMMAND=";
  public static final String MODIFY_COMMAND = "MODIFY";
  public static final String QUERY_COMMAND = "QUERY";

  public static final String MODIFY_TYPE_LABEL = "MODIFICATION=";
  public static final String UNAVAILABLE = "UNAVAILABLE";

  public static final String START_LABEL = "START=";
  public static final String END_LABEL = "END=";
  
  public static final String DELIM = "?";

  private static final DateFormat myDateFormatter = 
    DateFormat.getDateTimeInstance();
    
  public static DateFormat getDateFormat() {
    return myDateFormatter;
  }

  public static String generateMakeUnavailableRequest(String uidStr, 
                                                      Date start, Date end) {
    return DELIM + COMMAND_LABEL + MODIFY_COMMAND + DELIM +
      MODIFY_TYPE_LABEL + UNAVAILABLE + DELIM +
      UID_LABEL + uidStr + DELIM + 
      START_LABEL + myDateFormatter.format(start) + DELIM +
      END_LABEL + myDateFormatter.format(end) + DELIM;
  }

  public static String generateQueryRequest() {
    return DELIM + COMMAND_LABEL + QUERY_COMMAND + DELIM;
  }

  public static String parseParameter(String data, String label, 
                                      String delim, ParsePosition parsePosition) {
    
    int start = data.indexOf(label, parsePosition.getIndex());
    
    if (start == -1) {
      System.out.println("PSP_Asset.parseParameter() -  " + label + 
                         " label not found in " + data.substring(parsePosition.getIndex()));
      return "";
    }

    start += label.length();
    int end = data.indexOf(delim, start);

    if (end == -1) {
      System.out.println("PSP_Asset.parseParameter() -  " + delim + 
                         " delimiter not found after label " + label + 
                         " in " + data.substring(parsePosition.getIndex()));
      parsePosition.setErrorIndex(start);
      return "";
    }

    parsePosition.setIndex(end);

    return data.substring(start, end).trim();
  }
}








