/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.producers.policy;

/**
 * A UIParameterInfoIllegalValueException is an exception to be generated when
 * setting a UIPolicyParameterInfo with values illegal for that 
 * UIPolicyParameterInfo instance.
 */

public class UIParameterInfoIllegalValueException extends IllegalArgumentException {
  /**
   * Constructor - Contains message
   * @param message String  
   */
  public UIParameterInfoIllegalValueException(String message){
    super(message);
  }
}





