/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
