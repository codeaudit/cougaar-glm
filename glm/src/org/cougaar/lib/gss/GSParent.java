/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss;

/**
 * Interface satisfied by all objects that can have child XML objects
 *
 */

public interface GSParent {

  /** Handle a new child during parsing */
  void addChild (Object obj);

}
