/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.plan;

public class UIOrgRelationship implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

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
   */
  public String getTargetOrganizationUID() {return targetOrganizationUID;}
  public void setTargetOrganizationUID(String targetOrganizationUID) {
    this.targetOrganizationUID = targetOrganizationUID;
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

  /**
   * get and set access methods for <code>Relationship</code>
   * <p>
   * from Relationship.getRole()
   */
  public String getRelationship() {return relationship;}
  public void setRelationship(String relationship) {
    this.relationship = relationship;
  }

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    pr.print(targetOrganizationCID, "TargetOrganizationCID");
    pr.print(providerOrganizationCID, "ProviderOrganizationCID");
    pr.print(targetOrganizationUID, "TargetOrganizationUID");
    pr.print(providerOrganizationUID, "ProviderOrganizationUID");
    pr.print(relationship, "Relationship");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 */
  public static final long serialVersionUID = 61998127348911259L;

}
