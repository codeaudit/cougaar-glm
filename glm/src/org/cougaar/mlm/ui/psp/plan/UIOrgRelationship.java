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
 
package org.cougaar.mlm.ui.psp.plan;

import org.cougaar.core.util.UID;

public class UIOrgRelationship implements org.cougaar.mlm.ui.util.SelfPrinter, java.io.Serializable {

  private String targetOrganizationCID;     // TARGET: the "TO" Cluster
  private String providerOrganizationCID;   // PROVIDER: the "FROM" Cluster
  private String targetOrganizationUID;
  private String providerOrganizationUID;
  private String relationship;

  public UIOrgRelationship() {}

  /**
   * get and set access methods for <code>TargetOrganizationCID</code>
   * <p>
   * String version of Cluster Identifier representing this organization
   */
  public String getTargetOrganizationCID() {return targetOrganizationCID;}
  public void setTargetOrganizationCID(String targetOrganizationCID) {
    this.targetOrganizationCID = targetOrganizationCID;
  }

  /**
   * get and set access methods for <code>providerOrganizationCID</code>
   * <p>
   * String version of Cluster Identifier representing this organization
   */
  public String getProviderOrganizationCID() {return providerOrganizationCID;}
  public void setProviderOrganizationCID(String providerOrganizationCID) {
    this.providerOrganizationCID = providerOrganizationCID;
  }

  /**
   * get and set access methods for <code>TargetOrganizationUID</code>
   * <p>
   * String version of Organization Asset UID
   *
   */
  public String getTargetOrganizationUID() {return targetOrganizationUID;}
  public void setTargetOrganizationUID(String targetOrganizationUID) {
    this.targetOrganizationUID = targetOrganizationUID;
  }
  public void setTargetOrganizationUID(String targetOrgOwner,
				       long targetOrgId) {
    this.targetOrganizationUID = targetOrgOwner + "/" + targetOrgId;
  }
  public void setTargetOrganizationUID(UID targetOrganizationUID) {
    this.setTargetOrganizationUID(targetOrganizationUID.getOwner(),
				  targetOrganizationUID.getId());
  }


  /**
   * get and set access methods for <code>providerOrganizationUID</code>
   * <p>
   * String version of Organization Asset UID
   */
  public String getProviderOrganizationUID() {return providerOrganizationUID;}
  public void setProviderOrganizationUID(String providerOrganizationUID) {
    this.providerOrganizationUID = providerOrganizationUID;
  }
  public void setProviderOrganizationUID(String providerOrgOwner,
				         long providerOrgId) {
    this.providerOrganizationUID = providerOrgOwner + "/" + providerOrgId;
  }
  public void setProviderOrganizationUID(UID providerOrganizationUID) {
    this.setProviderOrganizationUID(providerOrganizationUID.getOwner(),
				  providerOrganizationUID.getId());
  }

  /**
   * get and set access methods for <code>Relationship</code>
   * <p>
   * from Relationship.getRole()
   */
  public String getRelationship() {return relationship;}
  public void setRelationship(String relationship) {
    this.relationship = relationship;
  }

  public void printContent(org.cougaar.mlm.ui.util.AsciiPrinter pr) {
    pr.print(targetOrganizationCID, "TargetOrganizationCID");
    pr.print(providerOrganizationCID, "ProviderOrganizationCID");
    pr.print(targetOrganizationUID, "TargetOrganizationUID");
    pr.print(providerOrganizationUID, "ProviderOrganizationUID");
    pr.print(relationship, "Relationship");
  }

  public String toString() {
    return org.cougaar.mlm.ui.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 */
  public static final long serialVersionUID = 61998127348911259L;

}
