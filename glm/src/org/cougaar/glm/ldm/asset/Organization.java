/*
 * <copyright>
 *  
 *  Copyright 1997-2012 Raytheon BBN Technologies
 *  under partial sponsorship of the Defense Advanced Research Projects
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

/* @generated Wed Jun 06 08:28:40 EDT 2012 from alpassets.def - DO NOT HAND EDIT */
package org.cougaar.glm.ldm.asset;
import org.cougaar.planning.ldm.asset.*;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Vector;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.RelationshipScheduleImpl;
public class Organization extends OrganizationAdapter implements HasRelationships {

  public Organization() {
    myManagedAssetPG = null;
    myAssignmentPG = null;
    myClusterPG = null;
    myOrganizationPG = null;
    myCommunityPGSchedule = null;
    myRelationshipPG = null;
  }

  public Organization(Organization prototype) {
    super(prototype);
    myManagedAssetPG=null;
    myAssignmentPG=null;
    myClusterPG=null;
    myOrganizationPG=null;
    myCommunityPGSchedule=null;
    myRelationshipPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    Organization _thing = (Organization) super.clone();
    if (myManagedAssetPG!=null) _thing.setManagedAssetPG(myManagedAssetPG.lock());
    if (myAssignmentPG!=null) _thing.setAssignmentPG(myAssignmentPG.lock());
    if (myClusterPG!=null) _thing.setClusterPG(myClusterPG.lock());
    if (myOrganizationPG!=null) _thing.setOrganizationPG(myOrganizationPG.lock());
    if (myCommunityPGSchedule!=null) _thing.setCommunityPGSchedule((PropertyGroupSchedule) myCommunityPGSchedule.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new Organization();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new Organization(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getManagedAssetPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getAssignmentPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getClusterPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getOrganizationPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getCommunityPGSchedule();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getRelationshipPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient ManagedAssetPG myManagedAssetPG;

  public ManagedAssetPG getManagedAssetPG() {
    ManagedAssetPG _tmp = (myManagedAssetPG != null) ?
      myManagedAssetPG : (ManagedAssetPG)resolvePG(ManagedAssetPG.class);
    return (_tmp == ManagedAssetPG.nullPG)?null:_tmp;
  }
  public void setManagedAssetPG(PropertyGroup arg_ManagedAssetPG) {
    if (!(arg_ManagedAssetPG instanceof ManagedAssetPG))
      throw new IllegalArgumentException("setManagedAssetPG requires a ManagedAssetPG argument.");
    myManagedAssetPG = (ManagedAssetPG) arg_ManagedAssetPG;
  }

  private transient AssignmentPG myAssignmentPG;

  public AssignmentPG getAssignmentPG() {
    AssignmentPG _tmp = (myAssignmentPG != null) ?
      myAssignmentPG : (AssignmentPG)resolvePG(AssignmentPG.class);
    return (_tmp == AssignmentPG.nullPG)?null:_tmp;
  }
  public void setAssignmentPG(PropertyGroup arg_AssignmentPG) {
    if (!(arg_AssignmentPG instanceof AssignmentPG))
      throw new IllegalArgumentException("setAssignmentPG requires a AssignmentPG argument.");
    myAssignmentPG = (AssignmentPG) arg_AssignmentPG;
  }

  private transient ClusterPG myClusterPG;

  public ClusterPG getClusterPG() {
    ClusterPG _tmp = (myClusterPG != null) ?
      myClusterPG : (ClusterPG)resolvePG(ClusterPG.class);
    return (_tmp == ClusterPG.nullPG)?null:_tmp;
  }
  public void setClusterPG(PropertyGroup arg_ClusterPG) {
    if (!(arg_ClusterPG instanceof ClusterPG))
      throw new IllegalArgumentException("setClusterPG requires a ClusterPG argument.");
    myClusterPG = (ClusterPG) arg_ClusterPG;
  }

  private transient OrganizationPG myOrganizationPG;

  public OrganizationPG getOrganizationPG() {
    OrganizationPG _tmp = (myOrganizationPG != null) ?
      myOrganizationPG : (OrganizationPG)resolvePG(OrganizationPG.class);
    return (_tmp == OrganizationPG.nullPG)?null:_tmp;
  }
  public void setOrganizationPG(PropertyGroup arg_OrganizationPG) {
    if (!(arg_OrganizationPG instanceof OrganizationPG))
      throw new IllegalArgumentException("setOrganizationPG requires a OrganizationPG argument.");
    myOrganizationPG = (OrganizationPG) arg_OrganizationPG;
  }

  private transient PropertyGroupSchedule myCommunityPGSchedule;

  public CommunityPG getCommunityPG(long time) {
    CommunityPG _tmp = (myCommunityPGSchedule != null) ?
      (CommunityPG)myCommunityPGSchedule.intersects(time) :
      (CommunityPG)resolvePG(CommunityPG.class, time);
    return (_tmp == CommunityPG.nullPG)?null:_tmp;
  }
  public PropertyGroupSchedule getCommunityPGSchedule() {
    PropertyGroupSchedule _tmp = (myCommunityPGSchedule != null) ?
         myCommunityPGSchedule : resolvePGSchedule(CommunityPG.class);
    return _tmp;
  }

  public void setCommunityPG(PropertyGroup arg_CommunityPG) {
    if (!(arg_CommunityPG instanceof CommunityPG))
      throw new IllegalArgumentException("setCommunityPG requires a CommunityPG argument.");
    if (myCommunityPGSchedule == null) {
      myCommunityPGSchedule = PropertyGroupFactory.newCommunityPGSchedule();
    }

    myCommunityPGSchedule.add(arg_CommunityPG);
  }

  public void setCommunityPGSchedule(PropertyGroupSchedule arg_OrganizationSchedule) {
    if (!(CommunityPG.class.equals(arg_OrganizationSchedule.getPGClass())))
      throw new IllegalArgumentException("setCommunityPGSchedule requires a PropertyGroupSchedule ofCommunityPGs.");

    myCommunityPGSchedule = arg_OrganizationSchedule;
  }

  private transient RelationshipPG myRelationshipPG;

  public RelationshipSchedule getRelationshipSchedule() {
    return getRelationshipPG().getRelationshipSchedule();
  }
  public void setRelationshipSchedule(RelationshipSchedule schedule) {
    NewRelationshipPG _argRelationshipPG = (NewRelationshipPG) getRelationshipPG().copy();
    _argRelationshipPG.setRelationshipSchedule(schedule);
    setRelationshipPG(_argRelationshipPG);
  }

  public boolean isLocal() {
    return getRelationshipPG().getLocal();
  }
  public void setLocal(boolean localFlag) {
    NewRelationshipPG _argRelationshipPG = (NewRelationshipPG) getRelationshipPG().copy();
    _argRelationshipPG.setLocal(localFlag);
    setRelationshipPG(_argRelationshipPG);
  }

  public boolean isSelf() {
    return getRelationshipPG().getLocal();
  }

  public RelationshipPG getRelationshipPG() {
    RelationshipPG _tmp = (myRelationshipPG != null) ?
      myRelationshipPG : (RelationshipPG)resolvePG(RelationshipPG.class);
    return (_tmp == RelationshipPG.nullPG)?null:_tmp;
  }
  public void setRelationshipPG(PropertyGroup arg_RelationshipPG) {
    if (!(arg_RelationshipPG instanceof RelationshipPG))
      throw new IllegalArgumentException("setRelationshipPG requires a RelationshipPG argument.");
    myRelationshipPG = (RelationshipPG) arg_RelationshipPG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (ManagedAssetPG.class.equals(c)) {
      return (myManagedAssetPG==ManagedAssetPG.nullPG)?null:myManagedAssetPG;
    }
    if (AssignmentPG.class.equals(c)) {
      return (myAssignmentPG==AssignmentPG.nullPG)?null:myAssignmentPG;
    }
    if (ClusterPG.class.equals(c)) {
      return (myClusterPG==ClusterPG.nullPG)?null:myClusterPG;
    }
    if (OrganizationPG.class.equals(c)) {
      return (myOrganizationPG==OrganizationPG.nullPG)?null:myOrganizationPG;
    }
    if (CommunityPG.class.equals(c)) {
      if (myCommunityPGSchedule==null) {
        return null;
      } else {
        if (t == UNSPECIFIED_TIME) {
          return (CommunityPG)myCommunityPGSchedule.getDefault();
        } else {
          return (CommunityPG)myCommunityPGSchedule.intersects(t);
        }
      }
    }
    if (RelationshipPG.class.equals(c)) {
      return (myRelationshipPG==RelationshipPG.nullPG)?null:myRelationshipPG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    if (CommunityPG.class.equals(c)) {
      return myCommunityPGSchedule;
    }
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (ManagedAssetPG.class.equals(c)) {
      myManagedAssetPG=(ManagedAssetPG)pg;
    } else
    if (AssignmentPG.class.equals(c)) {
      myAssignmentPG=(AssignmentPG)pg;
    } else
    if (ClusterPG.class.equals(c)) {
      myClusterPG=(ClusterPG)pg;
    } else
    if (OrganizationPG.class.equals(c)) {
      myOrganizationPG=(OrganizationPG)pg;
    } else
    if (CommunityPG.class.equals(c)) {
      if (myCommunityPGSchedule==null) {
        myCommunityPGSchedule=PropertyGroupFactory.newCommunityPGSchedule();
      } else {
        myCommunityPGSchedule.removeAll(myCommunityPGSchedule.intersectingSet((TimePhasedPropertyGroup) pg));
      }
      myCommunityPGSchedule.add(pg);
    } else
    if (RelationshipPG.class.equals(c)) {
      myRelationshipPG=(RelationshipPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
    if (CommunityPG.class.equals(pgSchedule.getPGClass())) {
      myCommunityPGSchedule=pgSchedule;
    } else
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (ManagedAssetPG.class.equals(c)) {
      removed=myManagedAssetPG;
      myManagedAssetPG=null;
    } else if (AssignmentPG.class.equals(c)) {
      removed=myAssignmentPG;
      myAssignmentPG=null;
    } else if (ClusterPG.class.equals(c)) {
      removed=myClusterPG;
      myClusterPG=null;
    } else if (OrganizationPG.class.equals(c)) {
      removed=myOrganizationPG;
      myOrganizationPG=null;
    } else if (CommunityPG.class.equals(c)) {
      if (myCommunityPGSchedule!=null) {
        if (myCommunityPGSchedule.getDefault()!=null) {
          removed=myCommunityPGSchedule.getDefault();
        } else if (myCommunityPGSchedule.size() > 0) {
          removed=(PropertyGroup) myCommunityPGSchedule.get(0);
        }
        myCommunityPGSchedule=null;
      }
    } else if (RelationshipPG.class.equals(c)) {
      removed=myRelationshipPG;
      myRelationshipPG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (ManagedAssetPG.class.equals(pgc)) {
      PropertyGroup removed=myManagedAssetPG;
      myManagedAssetPG=null;
      return removed;
    } else if (AssignmentPG.class.equals(pgc)) {
      PropertyGroup removed=myAssignmentPG;
      myAssignmentPG=null;
      return removed;
    } else if (ClusterPG.class.equals(pgc)) {
      PropertyGroup removed=myClusterPG;
      myClusterPG=null;
      return removed;
    } else if (OrganizationPG.class.equals(pgc)) {
      PropertyGroup removed=myOrganizationPG;
      myOrganizationPG=null;
      return removed;
    } else if (CommunityPG.class.equals(pgc)) {
if ((myCommunityPGSchedule!=null) && 
          (myCommunityPGSchedule.remove(pg))) {
        return pg;
      }
    } else if (RelationshipPG.class.equals(pgc)) {
      PropertyGroup removed=myRelationshipPG;
      myRelationshipPG=null;
      return removed;
    } else {}
    return super.removeLocalPG(pg);
  }

  public PropertyGroupSchedule removeLocalPGSchedule(Class c) {
    if (CommunityPG.class.equals(c)) {
      PropertyGroupSchedule removed=myCommunityPGSchedule;
      myCommunityPGSchedule=null;
      return removed;
    } else 
   {
      return super.removeLocalPGSchedule(c);
    }
  }

  public PropertyGroup generateDefaultPG(Class c) {
    if (ManagedAssetPG.class.equals(c)) {
      return (myManagedAssetPG= new ManagedAssetPGImpl());
    } else
    if (AssignmentPG.class.equals(c)) {
      return (myAssignmentPG= new AssignmentPGImpl());
    } else
    if (ClusterPG.class.equals(c)) {
      return (myClusterPG= new ClusterPGImpl());
    } else
    if (OrganizationPG.class.equals(c)) {
      return (myOrganizationPG= new OrganizationPGImpl());
    } else
    if (CommunityPG.class.equals(c)) {
      return null;
    } else
    if (RelationshipPG.class.equals(c)) {
      return (myRelationshipPG= new RelationshipPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myManagedAssetPG instanceof Null_PG || myManagedAssetPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myManagedAssetPG);
      }
      if (myAssignmentPG instanceof Null_PG || myAssignmentPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myAssignmentPG);
      }
      if (myClusterPG instanceof Null_PG || myClusterPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myClusterPG);
      }
      if (myOrganizationPG instanceof Null_PG || myOrganizationPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myOrganizationPG);
      }
      if (myCommunityPGSchedule instanceof Null_PG || myCommunityPGSchedule instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myCommunityPGSchedule);
      }
      if (myRelationshipPG instanceof Null_PG || myRelationshipPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myRelationshipPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myManagedAssetPG=(ManagedAssetPG)in.readObject();
      myAssignmentPG=(AssignmentPG)in.readObject();
      myClusterPG=(ClusterPG)in.readObject();
      myOrganizationPG=(OrganizationPG)in.readObject();
      myCommunityPGSchedule=(PropertyGroupSchedule)in.readObject();
      myRelationshipPG=(RelationshipPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[6];
      properties[0] = new PropertyDescriptor("ManagedAssetPG", Organization.class, "getManagedAssetPG", null);
      properties[1] = new PropertyDescriptor("AssignmentPG", Organization.class, "getAssignmentPG", null);
      properties[2] = new PropertyDescriptor("ClusterPG", Organization.class, "getClusterPG", null);
      properties[3] = new PropertyDescriptor("OrganizationPG", Organization.class, "getOrganizationPG", null);
      properties[4] = new PropertyDescriptor("CommunityPGSchedule", Organization.class, "getCommunityPGSchedule", null);
      properties[5] = new PropertyDescriptor("RelationshipPG", Organization.class, "getRelationshipPG", null);
    } catch (IntrospectionException ie) {}
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pds = super.getPropertyDescriptors();
    PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+6];
    System.arraycopy(pds, 0, ps, 0, pds.length);
    System.arraycopy(properties, 0, ps, pds.length, 6);
    return ps;
  }
}
