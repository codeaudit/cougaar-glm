/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
