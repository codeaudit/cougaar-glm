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
 
package org.cougaar.domain.mlm.ui.alert;

import java.applet.Applet;

import javax.swing.JApplet;
import javax.swing.JPanel;

import org.cougaar.util.ThemeFactory;

/**
 * AlertApplet  - 
 */

public class AlertApplet extends JApplet {
  //Used by PSP_Alert - generating html
  public static final String CLASSNAME = AlertApplet.class.getName();

  /**
   * init() - pulls out alert info provided by parameters and creates the
   * correct display depending on the type of alert.
   */
  public void init() {
    ThemeFactory.establishMetalTheme();

    String alertUID = getParameter(PSP_Alert.UID_PARAM);
    String alertText = getParameter(PSP_Alert.TEXT_PARAM);
    String choiceStr = getParameter(PSP_Alert.CHOICE_PARAM);
    String []alertChoices = PSP_Alert.parseChoiceParameter(choiceStr);
    
    JPanel panel;
    if (alertChoices.length == 0) {
      panel = new TextAlertDisplay(getCodeBase().toString(), 
                                   alertUID, alertText);
    } else {
      panel = new ChoiceAlertDisplay(getCodeBase().toString(), 
                                     alertUID, alertText, alertChoices);
    }

    getContentPane().add(panel);
    getRootPane().revalidate();
  }
}





