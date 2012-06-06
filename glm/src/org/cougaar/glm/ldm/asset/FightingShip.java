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
public class FightingShip extends Weapon {

  public FightingShip() {
    myContainPG = null;
    myTowPG = null;
    myWaterVehiclePG = null;
    myWaterSelfPropulsionPG = null;
  }

  public FightingShip(FightingShip prototype) {
    super(prototype);
    myContainPG=null;
    myTowPG=null;
    myWaterVehiclePG=null;
    myWaterSelfPropulsionPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    FightingShip _thing = (FightingShip) super.clone();
    if (myContainPG!=null) _thing.setContainPG(myContainPG.lock());
    if (myTowPG!=null) _thing.setTowPG(myTowPG.lock());
    if (myWaterVehiclePG!=null) _thing.setWaterVehiclePG(myWaterVehiclePG.lock());
    if (myWaterSelfPropulsionPG!=null) _thing.setWaterSelfPropulsionPG(myWaterSelfPropulsionPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new FightingShip();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new FightingShip(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getContainPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getTowPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getWaterVehiclePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getWaterSelfPropulsionPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
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

  private transient WaterVehiclePG myWaterVehiclePG;

  public WaterVehiclePG getWaterVehiclePG() {
    WaterVehiclePG _tmp = (myWaterVehiclePG != null) ?
      myWaterVehiclePG : (WaterVehiclePG)resolvePG(WaterVehiclePG.class);
    return (_tmp == WaterVehiclePG.nullPG)?null:_tmp;
  }
  public void setWaterVehiclePG(PropertyGroup arg_WaterVehiclePG) {
    if (!(arg_WaterVehiclePG instanceof WaterVehiclePG))
      throw new IllegalArgumentException("setWaterVehiclePG requires a WaterVehiclePG argument.");
    myWaterVehiclePG = (WaterVehiclePG) arg_WaterVehiclePG;
  }

  private transient WaterSelfPropulsionPG myWaterSelfPropulsionPG;

  public WaterSelfPropulsionPG getWaterSelfPropulsionPG() {
    WaterSelfPropulsionPG _tmp = (myWaterSelfPropulsionPG != null) ?
      myWaterSelfPropulsionPG : (WaterSelfPropulsionPG)resolvePG(WaterSelfPropulsionPG.class);
    return (_tmp == WaterSelfPropulsionPG.nullPG)?null:_tmp;
  }
  public void setWaterSelfPropulsionPG(PropertyGroup arg_WaterSelfPropulsionPG) {
    if (!(arg_WaterSelfPropulsionPG instanceof WaterSelfPropulsionPG))
      throw new IllegalArgumentException("setWaterSelfPropulsionPG requires a WaterSelfPropulsionPG argument.");
    myWaterSelfPropulsionPG = (WaterSelfPropulsionPG) arg_WaterSelfPropulsionPG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (ContainPG.class.equals(c)) {
      return (myContainPG==ContainPG.nullPG)?null:myContainPG;
    }
    if (TowPG.class.equals(c)) {
      return (myTowPG==TowPG.nullPG)?null:myTowPG;
    }
    if (WaterVehiclePG.class.equals(c)) {
      return (myWaterVehiclePG==WaterVehiclePG.nullPG)?null:myWaterVehiclePG;
    }
    if (WaterSelfPropulsionPG.class.equals(c)) {
      return (myWaterSelfPropulsionPG==WaterSelfPropulsionPG.nullPG)?null:myWaterSelfPropulsionPG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (ContainPG.class.equals(c)) {
      myContainPG=(ContainPG)pg;
    } else
    if (TowPG.class.equals(c)) {
      myTowPG=(TowPG)pg;
    } else
    if (WaterVehiclePG.class.equals(c)) {
      myWaterVehiclePG=(WaterVehiclePG)pg;
    } else
    if (WaterSelfPropulsionPG.class.equals(c)) {
      myWaterSelfPropulsionPG=(WaterSelfPropulsionPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (ContainPG.class.equals(c)) {
      removed=myContainPG;
      myContainPG=null;
    } else if (TowPG.class.equals(c)) {
      removed=myTowPG;
      myTowPG=null;
    } else if (WaterVehiclePG.class.equals(c)) {
      removed=myWaterVehiclePG;
      myWaterVehiclePG=null;
    } else if (WaterSelfPropulsionPG.class.equals(c)) {
      removed=myWaterSelfPropulsionPG;
      myWaterSelfPropulsionPG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (ContainPG.class.equals(pgc)) {
      PropertyGroup removed=myContainPG;
      myContainPG=null;
      return removed;
    } else if (TowPG.class.equals(pgc)) {
      PropertyGroup removed=myTowPG;
      myTowPG=null;
      return removed;
    } else if (WaterVehiclePG.class.equals(pgc)) {
      PropertyGroup removed=myWaterVehiclePG;
      myWaterVehiclePG=null;
      return removed;
    } else if (WaterSelfPropulsionPG.class.equals(pgc)) {
      PropertyGroup removed=myWaterSelfPropulsionPG;
      myWaterSelfPropulsionPG=null;
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
    if (ContainPG.class.equals(c)) {
      return (myContainPG= new ContainPGImpl());
    } else
    if (TowPG.class.equals(c)) {
      return (myTowPG= new TowPGImpl());
    } else
    if (WaterVehiclePG.class.equals(c)) {
      return (myWaterVehiclePG= new WaterVehiclePGImpl());
    } else
    if (WaterSelfPropulsionPG.class.equals(c)) {
      return (myWaterSelfPropulsionPG= new WaterSelfPropulsionPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myContainPG instanceof Null_PG || myContainPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myContainPG);
      }
      if (myTowPG instanceof Null_PG || myTowPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myTowPG);
      }
      if (myWaterVehiclePG instanceof Null_PG || myWaterVehiclePG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myWaterVehiclePG);
      }
      if (myWaterSelfPropulsionPG instanceof Null_PG || myWaterSelfPropulsionPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myWaterSelfPropulsionPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myContainPG=(ContainPG)in.readObject();
      myTowPG=(TowPG)in.readObject();
      myWaterVehiclePG=(WaterVehiclePG)in.readObject();
      myWaterSelfPropulsionPG=(WaterSelfPropulsionPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[4];
      properties[0] = new PropertyDescriptor("ContainPG", FightingShip.class, "getContainPG", null);
      properties[1] = new PropertyDescriptor("TowPG", FightingShip.class, "getTowPG", null);
      properties[2] = new PropertyDescriptor("WaterVehiclePG", FightingShip.class, "getWaterVehiclePG", null);
      properties[3] = new PropertyDescriptor("WaterSelfPropulsionPG", FightingShip.class, "getWaterSelfPropulsionPG", null);
    } catch (IntrospectionException ie) {}
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pds = super.getPropertyDescriptors();
    PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+4];
    System.arraycopy(pds, 0, ps, 0, pds.length);
    System.arraycopy(properties, 0, ps, pds.length, 4);
    return ps;
  }
}
