/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */


package org.cougaar.domain.mlm.debug.ui;

/** Base exception class for all exceptions in User Interface PlugIn.
 */

public class UIPlugInException extends RuntimeException {

  UIPlugInException() { 
    super(); 
  }

  UIPlugInException(String s) { 
    super(s);
  }
}

