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
public class Repairable extends ClassIXRepairPart {

  public Repairable() {
    myRepairabilityPG = null;
    myRepairablePG = null;
  }

  public Repairable(Repairable prototype) {
    super(prototype);
    myRepairabilityPG=null;
    myRepairablePG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    Repairable _thing = (Repairable) super.clone();
    if (myRepairabilityPG!=null) _thing.setRepairabilityPG(myRepairabilityPG.lock());
    if (myRepairablePG!=null) _thing.setRepairablePG(myRepairablePG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new Repairable();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new Repairable(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getRepairabilityPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getRepairablePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient RepairabilityPG myRepairabilityPG;

  public RepairabilityPG getRepairabilityPG() {
    RepairabilityPG _tmp = (myRepairabilityPG != null) ?
      myRepairabilityPG : (RepairabilityPG)resolvePG(RepairabilityPG.class);
    return (_tmp == RepairabilityPG.nullPG)?null:_tmp;
  }
  public void setRepairabilityPG(PropertyGroup arg_RepairabilityPG) {
    if (!(arg_RepairabilityPG instanceof RepairabilityPG))
      throw new IllegalArgumentException("setRepairabilityPG requires a RepairabilityPG argument.");
    myRepairabilityPG = (RepairabilityPG) arg_RepairabilityPG;
  }

  private transient RepairablePG myRepairablePG;

  public RepairablePG getRepairablePG() {
    RepairablePG _tmp = (myRepairablePG != null) ?
      myRepairablePG : (RepairablePG)resolvePG(RepairablePG.class);
    return (_tmp == RepairablePG.nullPG)?null:_tmp;
  }
  public void setRepairablePG(PropertyGroup arg_RepairablePG) {
    if (!(arg_RepairablePG instanceof RepairablePG))
      throw new IllegalArgumentException("setRepairablePG requires a RepairablePG argument.");
    myRepairablePG = (RepairablePG) arg_RepairablePG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (RepairabilityPG.class.equals(c)) {
      return (myRepairabilityPG==RepairabilityPG.nullPG)?null:myRepairabilityPG;
    }
    if (RepairablePG.class.equals(c)) {
      return (myRepairablePG==RepairablePG.nullPG)?null:myRepairablePG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (RepairabilityPG.class.equals(c)) {
      myRepairabilityPG=(RepairabilityPG)pg;
    } else
    if (RepairablePG.class.equals(c)) {
      myRepairablePG=(RepairablePG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (RepairabilityPG.class.equals(c)) {
      removed=myRepairabilityPG;
      myRepairabilityPG=null;
    } else if (RepairablePG.class.equals(c)) {
      removed=myRepairablePG;
      myRepairablePG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (RepairabilityPG.class.equals(pgc)) {
      PropertyGroup removed=myRepairabilityPG;
      myRepairabilityPG=null;
      return removed;
    } else if (RepairablePG.class.equals(pgc)) {
      PropertyGroup removed=myRepairablePG;
      myRepairablePG=null;
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
    if (RepairabilityPG.class.equals(c)) {
      return (myRepairabilityPG= new RepairabilityPGImpl());
    } else
    if (RepairablePG.class.equals(c)) {
      return (myRepairablePG= new RepairablePGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myRepairabilityPG instanceof Null_PG || myRepairabilityPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myRepairabilityPG);
      }
      if (myRepairablePG instanceof Null_PG || myRepairablePG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myRepairablePG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myRepairabilityPG=(RepairabilityPG)in.readObject();
      myRepairablePG=(RepairablePG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[2];
      properties[0] = new PropertyDescriptor("RepairabilityPG", Repairable.class, "getRepairabilityPG", null);
      properties[1] = new PropertyDescriptor("RepairablePG", Repairable.class, "getRepairablePG", null);
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
