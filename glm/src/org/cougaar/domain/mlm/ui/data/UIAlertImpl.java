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

import java.util.Vector;

import org.cougaar.domain.planning.ldm.plan.Alert;
import org.cougaar.domain.planning.ldm.plan.AlertParameter;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

public class UIAlertImpl implements UIAlert, XMLUIPlanObject {
  Alert alert;

  public UIAlertImpl(Alert alert) {
    this.alert = alert;
  }
   
  /**
   * Text to be displayed by UI to explain the Alert to the user.
   **/
  public String getAlertText() {
    return alert.getAlertText();
  }

  /**
   * Paramters that would be meaningful to the user.
   **/
  public String[] getAlertParameters() {
    AlertParameter[] alertParameters = alert.getAlertParameters();
    String[] parameters = new String[alertParameters.length];
    for (int i = 0; i < alertParameters.length; i++)
      parameters[i] = alertParameters[i].getParameter().toString();
    return parameters;
  }

  public String getAlertParameter(int i) {
    String[] parameters = getAlertParameters();
    if (i < parameters.length)
      return parameters[i];
    else
      return null;
  }

  /**
   * User meaningful descriptions of the alert parameters.
   **/
  public String[] getAlertDescriptions() {
    AlertParameter[] alertParameters = alert.getAlertParameters();
    String[] descriptions = new String[alertParameters.length];
    for (int i = 0; i < alertParameters.length; i++)
      descriptions[i] = alertParameters[i].getDescription();
    return descriptions;
  }

  public String getAlertDescription(int i) {
    String[] descriptions = getAlertDescriptions();
    if (i < descriptions.length)
      return descriptions[i];
    else
      return null;
  }


  /**
   * The user's answers to the questions posed by the AlertParameters.
   **/
  public String[] getAlertResponses() {
    AlertParameter[] alertParameters = alert.getAlertParameters();
    String[] responses = new String[alertParameters.length];
    for (int i = 0; i < alertParameters.length; i++)
      responses[i] = alertParameters[i].getResponse().toString();
    return responses;
  }

  public String getAlertResponse(int i) {
    String[] responses = getAlertResponses();
    if (i < responses.length)
      return responses[i];
    else
      return null;
  }

  /**
   * Indicates whether the Alert has been acted upon
   **/
  public boolean isAcknowledged() {
    return alert.getAcknowledged();
  }

  /**
   * Indicates severity of the alert.
   **/
  public int getSeverity() {
    return alert.getSeverity();
  }

  /**
   * Indicates type of the alert.
   **/
  public int getType() {
    return alert.getType();
  }

  /**
   * Indicates whether UI user is required to take action on this alert
   **/
  public boolean isOperatorResponseRequired() {
    return alert.getOperatorResponseRequired();
  }


  /**
   * The answer to the Alert. The AlertParameters can also have responses
   **/
  public String getOperatorResponse() {
    return alert.getOperatorResponse().toString();
  }

  //  XMLPlanObject method for UI
  
  public Element getXML(Document doc, Vector requestedFields) {
    return XMLUIPlanObjectConverter.getPlanObjectXML(this, doc, requestedFields);
  }


}
  



