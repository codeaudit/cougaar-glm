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
public class Truck extends CargoVehicle {

  public Truck() {
    myGroundSelfPropulsionPG = null;
    myGroundVehiclePG = null;
    myTowPG = null;
  }

  public Truck(Truck prototype) {
    super(prototype);
    myGroundSelfPropulsionPG=null;
    myGroundVehiclePG=null;
    myTowPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    Truck _thing = (Truck) super.clone();
    if (myGroundSelfPropulsionPG!=null) _thing.setGroundSelfPropulsionPG(myGroundSelfPropulsionPG.lock());
    if (myGroundVehiclePG!=null) _thing.setGroundVehiclePG(myGroundVehiclePG.lock());
    if (myTowPG!=null) _thing.setTowPG(myTowPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new Truck();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new Truck(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getGroundSelfPropulsionPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getGroundVehiclePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getTowPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient GroundSelfPropulsionPG myGroundSelfPropulsionPG;

  public GroundSelfPropulsionPG getGroundSelfPropulsionPG() {
    GroundSelfPropulsionPG _tmp = (myGroundSelfPropulsionPG != null) ?
      myGroundSelfPropulsionPG : (GroundSelfPropulsionPG)resolvePG(GroundSelfPropulsionPG.class);
    return (_tmp == GroundSelfPropulsionPG.nullPG)?null:_tmp;
  }
  public void setGroundSelfPropulsionPG(PropertyGroup arg_GroundSelfPropulsionPG) {
    if (!(arg_GroundSelfPropulsionPG instanceof GroundSelfPropulsionPG))
      throw new IllegalArgumentException("setGroundSelfPropulsionPG requires a GroundSelfPropulsionPG argument.");
    myGroundSelfPropulsionPG = (GroundSelfPropulsionPG) arg_GroundSelfPropulsionPG;
  }

  private transient GroundVehiclePG myGroundVehiclePG;

  public GroundVehiclePG getGroundVehiclePG() {
    GroundVehiclePG _tmp = (myGroundVehiclePG != null) ?
      myGroundVehiclePG : (GroundVehiclePG)resolvePG(GroundVehiclePG.class);
    return (_tmp == GroundVehiclePG.nullPG)?null:_tmp;
  }
  public void setGroundVehiclePG(PropertyGroup arg_GroundVehiclePG) {
    if (!(arg_GroundVehiclePG instanceof GroundVehiclePG))
      throw new IllegalArgumentException("setGroundVehiclePG requires a GroundVehiclePG argument.");
    myGroundVehiclePG = (GroundVehiclePG) arg_GroundVehiclePG;
  }

  private transient TowPG myTowPG;

  public TowPG getTowPG() {
    TowPG _tmp = (myTowPG != null) ?
      myTowPG : (TowPG)resolvePG(TowPG.class);
    return (_tmp == TowPG.nullPG)?null:_tmp;
  }
  public void setTowPG(PropertyGroup arg_TowPG) {
    if (!(arg_TowPG instanceof TowPG))
      throw new IllegalArgumentException("setTowPG requires a TowPG argument.");
    myTowPG = (TowPG) arg_TowPG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (GroundSelfPropulsionPG.class.equals(c)) {
      return (myGroundSelfPropulsionPG==GroundSelfPropulsionPG.nullPG)?null:myGroundSelfPropulsionPG;
    }
    if (GroundVehiclePG.class.equals(c)) {
      return (myGroundVehiclePG==GroundVehiclePG.nullPG)?null:myGroundVehiclePG;
    }
    if (TowPG.class.equals(c)) {
      return (myTowPG==TowPG.nullPG)?null:myTowPG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (GroundSelfPropulsionPG.class.equals(c)) {
      myGroundSelfPropulsionPG=(GroundSelfPropulsionPG)pg;
    } else
    if (GroundVehiclePG.class.equals(c)) {
      myGroundVehiclePG=(GroundVehiclePG)pg;
    } else
    if (TowPG.class.equals(c)) {
      myTowPG=(TowPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (GroundSelfPropulsionPG.class.equals(c)) {
      removed=myGroundSelfPropulsionPG;
      myGroundSelfPropulsionPG=null;
    } else if (GroundVehiclePG.class.equals(c)) {
      removed=myGroundVehiclePG;
      myGroundVehiclePG=null;
    } else if (TowPG.class.equals(c)) {
      removed=myTowPG;
      myTowPG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (GroundSelfPropulsionPG.class.equals(pgc)) {
      PropertyGroup removed=myGroundSelfPropulsionPG;
      myGroundSelfPropulsionPG=null;
      return removed;
    } else if (GroundVehiclePG.class.equals(pgc)) {
      PropertyGroup removed=myGroundVehiclePG;
      myGroundVehiclePG=null;
      return removed;
    } else if (TowPG.class.equals(pgc)) {
      PropertyGroup removed=myTowPG;
      myTowPG=null;
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
    if (GroundSelfPropulsionPG.class.equals(c)) {
      return (myGroundSelfPropulsionPG= new GroundSelfPropulsionPGImpl());
    } else
    if (GroundVehiclePG.class.equals(c)) {
      return (myGroundVehiclePG= new GroundVehiclePGImpl());
    } else
    if (TowPG.class.equals(c)) {
      return (myTowPG= new TowPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myGroundSelfPropulsionPG instanceof Null_PG || myGroundSelfPropulsionPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myGroundSelfPropulsionPG);
      }
      if (myGroundVehiclePG instanceof Null_PG || myGroundVehiclePG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myGroundVehiclePG);
      }
      if (myTowPG instanceof Null_PG || myTowPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myTowPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myGroundSelfPropulsionPG=(GroundSelfPropulsionPG)in.readObject();
      myGroundVehiclePG=(GroundVehiclePG)in.readObject();
      myTowPG=(TowPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[3];
      properties[0] = new PropertyDescriptor("GroundSelfPropulsionPG", Truck.class, "getGroundSelfPropulsionPG", null);
      properties[1] = new PropertyDescriptor("GroundVehiclePG", Truck.class, "getGroundVehiclePG", null);
      properties[2] = new PropertyDescriptor("TowPG", Truck.class, "getTowPG", null);
    } catch (IntrospectionException ie) {}
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pds = super.getPropertyDescriptors();
    PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+3];
    System.arraycopy(pds, 0, ps, 0, pds.length);
    System.arraycopy(properties, 0, ps, pds.length, 3);
    return ps;
  }
}
