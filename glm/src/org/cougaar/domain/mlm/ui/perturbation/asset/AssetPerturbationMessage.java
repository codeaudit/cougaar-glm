/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
                         " label not found in " + data);
      return "";
    }

    start += label.length();
    int end = data.indexOf(delim, start);

    if (end == -1) {
      System.out.println("PSP_Asset.parseParameter() -  " + delim + 
                         " delimiter not found after label " + label);
      parsePosition.setErrorIndex(start);
      return "";
    }

    parsePosition.setIndex(end);

    return data.substring(start, end).trim();
  }
}








