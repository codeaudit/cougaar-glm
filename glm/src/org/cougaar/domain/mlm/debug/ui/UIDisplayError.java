/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */


package org.cougaar.domain.mlm.debug.ui;

import javax.swing.JFrame;
import org.cougaar.util.OptionPane;

/** Display an error dialog box with optionally specified message.
  Returns when the user has selected the "OK" button on the dialog.
  */

public class UIDisplayError {

  public UIDisplayError() {
      OptionPane.showOptionDialog(new JFrame(), 
				  "Unspecified error in user interface plugin.",
				  "Error",
                                  OptionPane.DEFAULT_OPTION,
				  OptionPane.ERROR_MESSAGE,
                                  null, null, null);
  }

  /** Display a dialog box with the specified message.
    @param s the message to display in the dialog box
    */

  public UIDisplayError(String s) {
      OptionPane.showOptionDialog(new JFrame(), s, "Error",
                                  OptionPane.DEFAULT_OPTION,
                                  OptionPane.ERROR_MESSAGE,
                                  null, null, null);
  }

}
