/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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





