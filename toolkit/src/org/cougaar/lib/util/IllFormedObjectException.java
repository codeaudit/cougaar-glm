/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.util;

/**
 * Exception class for error checking on all types of objects,
 * such as Assets, Workflows, Tasks,etc.
 */
public class IllFormedObjectException extends RuntimeException{

  public IllFormedObjectException(java.lang.String msg){
    super(msg);
  }

  public IllFormedObjectException(String cluster, String msg){
    this(cluster + " " + msg);
  }
}
