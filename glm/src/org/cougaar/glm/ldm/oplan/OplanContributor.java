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
 
package org.cougaar.glm.ldm.oplan;
import java.util.Vector;
import java.util.Enumeration;
import org.cougaar.planning.ldm.plan.Transferable;
import org.cougaar.core.util.UID;

/**
 * Unify a bunch of getters that oplan sub-objects (hence "contributor"s)
 * support.
 * 
 * @author  ALPINE <alpine-software@bbn.com>
 *
 */

public interface OplanContributor extends Transferable {
  /** Gets the time span in force. **/
  TimeSpan getTimeSpan();

  /** Gets the time span in force. **/
  void setTimeSpan(TimeSpan span);
  
  /** @deprecated Use getOplanUID*/
  UID getOplanID();

  /** Which oplan are we connected to? **/
  UID getOplanUID();
}

