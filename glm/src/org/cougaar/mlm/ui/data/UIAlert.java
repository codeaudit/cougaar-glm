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

package org.cougaar.mlm.ui.data;

public interface UIAlert {
   
  /**
   * Text to be displayed by UI to explain the Alert to the user.
   **/
  String getAlertText();

  /**
   * Paramters that would be meaningful to the user.
   **/
  String[] getAlertParameters();

  /**
   * User meaningful descriptions of the alert parameters.
   **/
  String[] getAlertDescriptions();

  /**
   * The user's answers to the questions posed by the AlertParameters.
   **/
  String[] getAlertResponses();

  /**
   * Indicates whether the Alert has been acted upon
   **/
  boolean isAcknowledged();

  /**
   * Indicates severity of the alert.
   **/
  int getSeverity();

  /**
   * Indicates Alert type. 
   * BOZO - I presume this means the type of activity which generated the 
   * alert - transportation, ... Valid types should be defined within the 
   * interface ala severities but I haven't a clue what they might be.
   */
  int getType();

  /**
   * Indicates whether UI user is required to take action on this alert
   **/
  boolean isOperatorResponseRequired();


  /**
   * The answer to the Alert. The AlertParameters can also have responses
   **/
  String  getOperatorResponse();
}
  


