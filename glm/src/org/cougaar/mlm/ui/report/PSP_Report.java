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
 
package org.cougaar.mlm.ui.report;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.planning.ldm.plan.Report;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.UnaryPredicate;

/**
 * PSP_Report - PSP for retrieving reports
 * Optionally allows caller to specify a since date for the reports
 * Currently returns messages as HTML
 */

public class PSP_Report extends PSP_BaseAdapter 
  implements PlanServiceProvider, UISubscriber {

  public static final String DATE_LABEL = "DATE=";
  public static final String TEXT_LABEL = "TEXT=";
  public static final String DELIM = "?";

  private static final DateFormat myDateFormatter = 
    DateFormat.getDateTimeInstance();
    
  public static DateFormat getDateFormat() {
    return myDateFormatter;
  }

  private Date myScreenDate;

  /** 
   * Constructor -  A zero-argument constructor is required for dynamically 
   * loaded PSPs by Class.newInstance()
   **/
  public PSP_Report() {
    super();
  }
    
  /**
   * Constructor -
   *
   * @param pkg String specifying package id
   * @param id String specifying PSP name
   * @throw org.cougaar.lib.planserver.RuntimePSPException
   */
  public PSP_Report(String pkg, String id ) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }
  
  /**
   * reportPred - subscribes for all Reports since myScreenDate
   */
  private UnaryPredicate reportPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if ((o instanceof Report) &&
          ((Report)o).getDate().compareTo(myScreenDate) >= 0) {
        return true;
      }
      return false;
    }
  };            
  
  /**
   * test - Always returns false as currently implemented.
   * See doc in org.cougaar.lib.planserver.PlanServiceProvider
   *
   * @param queryParamaters HttpInput
   * @param sc PlanServiceContext
   */
  public boolean test(HttpInput queryParameters, PlanServiceContext sc) {
    super.initializeTest();
    return false;  // This PSP is only accessed by direct reference.
  }
  
  
  /**
   * execute - creates HTML with the relevant Reports
   * See doc in org.cougaar.lib.planserver.PlanServiceProvider
   *
   * @param out PrintStream to which output will be written
   * @param queryParameters HttpInput with screen date parameter. Parameter
   * is optional.
   * @param psc PlanServiceContext
   * @param psu PlanServiceUtilities
   **/
  public void execute(PrintStream out,
                      HttpInput queryParameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception {
    String postData = queryParameters.getBodyAsString();
    double millis = 0; 

    if ((postData != null) &&
        !(postData.equals(""))) {
      String milliString = parseParameter(postData, DATE_LABEL, DELIM);
      try {
        millis = Double.parseDouble(milliString);
      } catch (Exception e) {
        System.out.println(e);
        throw new RuntimePSPException("Unrecognized Date parameter encountered: " + 
                                      milliString);
      }
    }

    myScreenDate = new Date((long)millis);

    Subscription subscription = 
      psc.getServerPlugInSupport().subscribe(this, reportPred);
    Collection collection = 
      ((CollectionSubscription)subscription).getCollection();
    Iterator reportIterator = collection.iterator();

    while (reportIterator.hasNext()) {
      Report report = (Report)reportIterator.next();
      String text = report.getText();
      Date date = report.getDate();
      String info = DATE_LABEL + getDateFormat().format(date) + DELIM + 
        TEXT_LABEL + text + DELIM;

      System.out.println(info);
      out.println("<HTML><BODY> <FONT color=#CC0000>" + 
                  info + 
                  "</FONT></BODY></HTML><p>");
    }
    out.close();
  }
  
  
  /**
   * returnsXML - returns true if PSP can output XML.  Currently always false.
   * 
   * @return boolean 
   **/
  public boolean returnsXML() {
    return false;
  }
  
  /**
   * returnsHTML - returns true if PSP can output HTML.  Currently always true.
   * 
   * @return boolean 
   **/
  public boolean returnsHTML() {
    return true;
  }
  
  /** 
   * getDTD - returns null. PSP does not return XML.
   * Any PlanServiceProvider must be able to provide DTD of its
   * output IFF it is an XML PSP... ie.  returnsXML() == true;
   *
   * @return String
   **/
  public String getDTD() {
    return null;
  }

  /**
   * subscriptionChanged - doesn't do anything. All the data currently returned
   * by execute.
   *
   * @param subscription Subscription
   */
  public void subscriptionChanged(Subscription subscription) {
  }

  /**
   * generateQuery - returns string in the correct format for getting all reports since
   * the specified date from the PSP
   *
   * @param screenDate Starting date for returned reports
   */
  public static String generateQuery(Date screenDate) {
    return DELIM + DATE_LABEL + screenDate.getTime() + DELIM;
  }

  /**
   * parseParameter - returns string associated with the specified parameter - 
   * should be bounded by specified label and delimiter.
   *
   * @param data String to be parsed
   * @param label String label
   * @param delim String delimiter
   *
   * @return String bounded by label and delimiter. "" if not found.
   */
  public static String parseParameter(String data, String label, 
                                      String delim) {
    return parseParameter(data, label, delim, false);
  }

  /**
   * parseParameter - returns string associated with the specified parameter - 
   * should be bounded by specified label and delimiter.
   *
   * @param data String to be parsed
   * @param label String label
   * @param delim String delimiter
   * @param quiet boolean controls whether error messages are displayed
   *
   * @return String bounded by label and delimiter. "" if not found.
   */
  public static String parseParameter(String data, String label, 
                                      String delim, boolean quiet) {
    int start = data.indexOf(label);
    
    if  (!(quiet) &&
         (start == -1)) {
      System.out.println("PSP_Report.parseParameter() -  " + label + 
                         " label not found in " + data);
      return "";
    }

    start += label.length();
    int end = data.indexOf(delim, start);

    if (!(quiet) &&
        (end < start)) {
      System.out.println("PSP_Report.parseParameter() -  " + delim + 
                         " delimiter not found after label " + label);
      return "";
    }

    return data.substring(start, end).trim();
  }
}






