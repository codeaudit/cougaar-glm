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
/** A usually temporary object which contains other objects, e.g. forshipping.  Examples are pallets, boxes, etc. **/

public class Package extends PhysicalAsset {

  public Package() {
    myScheduledContentPG = null;
    myContainPG = null;
  }

  public Package(Package prototype) {
    super(prototype);
    myScheduledContentPG=null;
    myContainPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    Package _thing = (Package) super.clone();
    if (myScheduledContentPG!=null) _thing.setScheduledContentPG(myScheduledContentPG.lock());
    if (myContainPG!=null) _thing.setContainPG(myContainPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new Package();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new Package(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getScheduledContentPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getContainPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient ScheduledContentPG myScheduledContentPG;

  public ScheduledContentPG getScheduledContentPG() {
    ScheduledContentPG _tmp = (myScheduledContentPG != null) ?
      myScheduledContentPG : (ScheduledContentPG)resolvePG(ScheduledContentPG.class);
    return (_tmp == ScheduledContentPG.nullPG)?null:_tmp;
  }
  public void setScheduledContentPG(PropertyGroup arg_ScheduledContentPG) {
    if (!(arg_ScheduledContentPG instanceof ScheduledContentPG))
      throw new IllegalArgumentException("setScheduledContentPG requires a ScheduledContentPG argument.");
    myScheduledContentPG = (ScheduledContentPG) arg_ScheduledContentPG;
  }

  private transient ContainPG myContainPG;

  public ContainPG getContainPG() {
    ContainPG _tmp = (myContainPG != null) ?
      myContainPG : (ContainPG)resolvePG(ContainPG.class);
    return (_tmp == ContainPG.nullPG)?null:_tmp;
  }
  public void setContainPG(PropertyGroup arg_ContainPG) {
    if (!(arg_ContainPG instanceof ContainPG))
      throw new IllegalArgumentException("setContainPG requires a ContainPG argument.");
    myContainPG = (ContainPG) arg_ContainPG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (ScheduledContentPG.class.equals(c)) {
      return (myScheduledContentPG==ScheduledContentPG.nullPG)?null:myScheduledContentPG;
    }
    if (ContainPG.class.equals(c)) {
      return (myContainPG==ContainPG.nullPG)?null:myContainPG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (ScheduledContentPG.class.equals(c)) {
      myScheduledContentPG=(ScheduledContentPG)pg;
    } else
    if (ContainPG.class.equals(c)) {
      myContainPG=(ContainPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (ScheduledContentPG.class.equals(c)) {
      removed=myScheduledContentPG;
      myScheduledContentPG=null;
    } else if (ContainPG.class.equals(c)) {
      removed=myContainPG;
      myContainPG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (ScheduledContentPG.class.equals(pgc)) {
      PropertyGroup removed=myScheduledContentPG;
      myScheduledContentPG=null;
      return removed;
    } else if (ContainPG.class.equals(pgc)) {
      PropertyGroup removed=myContainPG;
      myContainPG=null;
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
    if (ScheduledContentPG.class.equals(c)) {
      return (myScheduledContentPG= new ScheduledContentPGImpl());
    } else
    if (ContainPG.class.equals(c)) {
      return (myContainPG= new ContainPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myScheduledContentPG instanceof Null_PG || myScheduledContentPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myScheduledContentPG);
      }
      if (myContainPG instanceof Null_PG || myContainPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myContainPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myScheduledContentPG=(ScheduledContentPG)in.readObject();
      myContainPG=(ContainPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[2];
      properties[0] = new PropertyDescriptor("ScheduledContentPG", Package.class, "getScheduledContentPG", null);
      properties[1] = new PropertyDescriptor("ContainPG", Package.class, "getContainPG", null);
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
