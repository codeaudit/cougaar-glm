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
public class ClientOrganization extends OrganizationAdapter implements HasRelationships {

  public ClientOrganization() {
    myClusterPG = null;
    myRelationshipPG = null;
  }

  public ClientOrganization(ClientOrganization prototype) {
    super(prototype);
    myClusterPG=null;
    myRelationshipPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    ClientOrganization _thing = (ClientOrganization) super.clone();
    if (myClusterPG!=null) _thing.setClusterPG(myClusterPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new ClientOrganization();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new ClientOrganization(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getClusterPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getRelationshipPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
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
    if (ClusterPG.class.equals(c)) {
      return (myClusterPG==ClusterPG.nullPG)?null:myClusterPG;
    }
    if (RelationshipPG.class.equals(c)) {
      return (myRelationshipPG==RelationshipPG.nullPG)?null:myRelationshipPG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (ClusterPG.class.equals(c)) {
      myClusterPG=(ClusterPG)pg;
    } else
    if (RelationshipPG.class.equals(c)) {
      myRelationshipPG=(RelationshipPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (ClusterPG.class.equals(c)) {
      removed=myClusterPG;
      myClusterPG=null;
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
    if (ClusterPG.class.equals(pgc)) {
      PropertyGroup removed=myClusterPG;
      myClusterPG=null;
      return removed;
    } else if (RelationshipPG.class.equals(pgc)) {
      PropertyGroup removed=myRelationshipPG;
      myRelationshipPG=null;
      return removed;
    } else {}
    return super.removeLocalPG(pg);
  }

  public PropertyGroupSchedule removeLocalPGSchedule(Class c) {
   {
      return super.removeLocalPGSchedule(c);
    }
  }

  public PropertyGroup generateDefaultPG(Class c) {
    if (ClusterPG.class.equals(c)) {
      return (myClusterPG= new ClusterPGImpl());
    } else
    if (RelationshipPG.class.equals(c)) {
      return (myRelationshipPG= new RelationshipPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myClusterPG instanceof Null_PG || myClusterPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myClusterPG);
      }
      if (myRelationshipPG instanceof Null_PG || myRelationshipPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myRelationshipPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myClusterPG=(ClusterPG)in.readObject();
      myRelationshipPG=(RelationshipPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[2];
      properties[0] = new PropertyDescriptor("ClusterPG", ClientOrganization.class, "getClusterPG", null);
      properties[1] = new PropertyDescriptor("RelationshipPG", ClientOrganization.class, "getRelationshipPG", null);
    } catch (IntrospectionException ie) {}
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pds = super.getPropertyDescriptors();
    PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+2];
    System.arraycopy(pds, 0, ps, 0, pds.length);
    System.arraycopy(properties, 0, ps, pds.length, 2);
    return ps;
  }
}
