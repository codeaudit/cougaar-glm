/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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


package org.cougaar.mlm.debug.ui;

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
