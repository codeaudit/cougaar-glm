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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import org.cougaar.core.util.OwnedUniqueObject;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.planning.ldm.plan.Transferable;

/**
 * OrgRelation
 * The OrgRelation method is a LDM Object that contains organizational data.
 * The OrgRelation object includes information such as role, timespan, relationType,
 * and the organizations related to each other.
 * Subordinate clusters can subscribe to changes in the ForcePackage in order to 
 * react to changes accordingly.  The OrgRelations are initially created in the J3 cluster 
 * and then is transferred to other clusters by the Propagation Plugin.  
 * Subordinate clusters should not modify (set) OrgRelation information.
 **/
public class OrgRelation extends OwnedUniqueObject
  implements OplanContributor, Transferable, UniqueObject, Serializable, Cloneable
{
  private String role;
  private String relationType;
  private String orgID = "";
  private String otherOrgID = "";
  private String forcePackageID = "";
  private UID oplanUID = null;
  private TimeSpan timeSpan;

  public OrgRelation() 
  {
	
  }//OrgRelation
	
  public OrgRelation(String orgID, UID oplanUID) 
  {
    this.orgID = unique(orgID);
    this.oplanUID = oplanUID;
		
  }// OrgRelation

//    public void setUID(UID uid) {
//      orgRelationId = uid;
//    }
//    public UID getUID() {
//      return orgRelationId;
//    }
  public void setOrgRelationId(UID uid)
  {
    setUID(uid);
  }

  public UID getOrgRelationId()
  {
    return getUID();
  }
	
  public void setOrgID(String orgID)
  {
    this.orgID = unique(orgID);
  }

	
  public String getOrgID()
  {
    return orgID;
  }	

  public void setForcePackageId(String forcePackageID)
  {
    this.forcePackageID = unique(forcePackageID);
  }

	
  public String getForcePackageId()
  {
    return forcePackageID;
  }	

  /** @deprecated Use getOplanUID */
  public UID getOplanID()
  {
    return oplanUID;
  }
	
  public UID getOplanUID()
  {
    return oplanUID;
  }
	
	
  public void setOtherOrgId(String otherOrgID) 
  {
    this.otherOrgID = unique(otherOrgID);
  }//setOtherOrgID
	
	
  public void setRelationType(String relationType) 
  {
    this.relationType = unique(relationType);	
  }//setRelationType

  public void setAssignedRole(String role) 
  {
    this.role = unique(role);	
  }//setAssignedRole

  public void setTimeSpan(TimeSpan ts) {
    timeSpan = ts;
  }//setTimeSpan

  public TimeSpan getTimeSpan() 
  {
    return timeSpan;
  }//getTimeSpan

  public String getOtherOrgId() 
  {
    return otherOrgID;
  }//getOtherOrgID
	
  public String getRelationType() 
  {
    return relationType;
  }//relationType
	
  public String getAssignedRole() 
  {
    return role;
  }//getAssignedRole

  public Object clone() {
    OrgRelation or = new OrgRelation(orgID, oplanUID);
    or.setForcePackageId(forcePackageID);
    or.setRelationType(relationType);
    or.setAssignedRole(role);
    or.setOtherOrgId(otherOrgID);
    or.setUID(getUID());
    or.setOwner(getOwner());
	
    if (timeSpan != null)
      or.setTimeSpan((TimeSpan)timeSpan.clone());
    return or;
  }//clone
  
  public void setAll(Transferable other) {
	
    if (!(other instanceof OrgRelation)) {
      throw new IllegalArgumentException("Parameter is not an OrgRelation.");
    }
    else 
      {		   
        OrgRelation or = (OrgRelation)other;
        relationType = or.getRelationType();
        orgID = or.getOrgID();
        forcePackageID = or.getForcePackageId();
        oplanUID = or.getOplanUID();
        timeSpan = or.getTimeSpan();
        otherOrgID = or.getOtherOrgId();
        role = or.getAssignedRole();
        setUID(or.getUID());
        setOwner(or.getOwner());

      }// else set everything
  }// setAll

  public boolean same(Transferable other) 
  {   
    if (other instanceof OrgRelation) {
      OrgRelation or = (OrgRelation) other;
      return (getUID().equals(or.getUID()));
    }
    return false;
  }

  private boolean matches(Object a, Object b) {
    return (a==null)?(b==null):(a.equals(b));
  }

  public boolean equals(Object o) {
    if (o instanceof OrgRelation) {
      OrgRelation or = (OrgRelation) o;
      
      return
        matches(getAssignedRole(), or.getAssignedRole()) &&
        matches(getRelationType(), or.getRelationType()) &&
        matches(getUID(), or.getUID()) &&
        matches(getOrgID(), or.getOrgID()) &&
        matches(getOtherOrgId(), or.getOtherOrgId()) &&
        matches(getForcePackageId(), or.getForcePackageId()) &&
        matches(getOplanUID(), or.getOplanUID()) &&
        matches(getTimeSpan(), or.getTimeSpan());
    } else
      return false;
  }

  //dummy PropertyChangeSupport for the Jess Interpreter.
  protected transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    pcs.addPropertyChangeListener(pcl);
  }

  public void removePropertyChangeListener(PropertyChangeListener pcl)   {
    pcs.removePropertyChangeListener(pcl);
  }
  public static final String unique(String s) {
    return (s==null)?null:(s.intern());
  }

}
