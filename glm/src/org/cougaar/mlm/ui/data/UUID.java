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

package org.cougaar.mlm.ui.data;

/** This is patterned after, but is separate from org.cougaar.core.util.UID
to give us flexibility in expanding the definition for UI purposes, 
independent of the society.
*/

public class UUID {
  private String uuid;

  public UUID(String uuid) {
    this.uuid = uuid;
  }

  /** Returns a string of the form hostname:port:uniqueID.  
      Hostname is the URL for the machine on which the cluster that 
      created the object is running.
      Port is the cluster's LogPlanServer port.
      UniqueID is either the alp generated unique ID or a cluster-unique id.
      For example: www.alpine.bbn.com:5555:MCCGlobalMode54
      @return String - the object ID
  */

  public String getUUID() {
    return uuid;
  }

  public String toString() {
    return uuid;
  }
}
