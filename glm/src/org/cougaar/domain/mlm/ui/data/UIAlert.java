/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

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
  


