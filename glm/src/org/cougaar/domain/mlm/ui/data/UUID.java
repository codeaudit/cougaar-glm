/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

/** This is patterned after, but is separate from org.cougaar.core.society.UID
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
