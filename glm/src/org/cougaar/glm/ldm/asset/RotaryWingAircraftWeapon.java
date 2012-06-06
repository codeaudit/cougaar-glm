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
public class RotaryWingAircraftWeapon extends Weapon {

  public RotaryWingAircraftWeapon() {
    myAirVehiclePG = null;
    myAirSelfPropulsionPG = null;
    myContainPG = null;
    myLiftPG = null;
  }

  public RotaryWingAircraftWeapon(RotaryWingAircraftWeapon prototype) {
    super(prototype);
    myAirVehiclePG=null;
    myAirSelfPropulsionPG=null;
    myContainPG=null;
    myLiftPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    RotaryWingAircraftWeapon _thing = (RotaryWingAircraftWeapon) super.clone();
    if (myAirVehiclePG!=null) _thing.setAirVehiclePG(myAirVehiclePG.lock());
    if (myAirSelfPropulsionPG!=null) _thing.setAirSelfPropulsionPG(myAirSelfPropulsionPG.lock());
    if (myContainPG!=null) _thing.setContainPG(myContainPG.lock());
    if (myLiftPG!=null) _thing.setLiftPG(myLiftPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new RotaryWingAircraftWeapon();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new RotaryWingAircraftWeapon(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getAirVehiclePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getAirSelfPropulsionPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getContainPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getLiftPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient AirVehiclePG myAirVehiclePG;

  public AirVehiclePG getAirVehiclePG() {
    AirVehiclePG _tmp = (myAirVehiclePG != null) ?
      myAirVehiclePG : (AirVehiclePG)resolvePG(AirVehiclePG.class);
    return (_tmp == AirVehiclePG.nullPG)?null:_tmp;
  }
  public void setAirVehiclePG(PropertyGroup arg_AirVehiclePG) {
    if (!(arg_AirVehiclePG instanceof AirVehiclePG))
      throw new IllegalArgumentException("setAirVehiclePG requires a AirVehiclePG argument.");
    myAirVehiclePG = (AirVehiclePG) arg_AirVehiclePG;
  }

  private transient AirSelfPropulsionPG myAirSelfPropulsionPG;

  public AirSelfPropulsionPG getAirSelfPropulsionPG() {
    AirSelfPropulsionPG _tmp = (myAirSelfPropulsionPG != null) ?
      myAirSelfPropulsionPG : (AirSelfPropulsionPG)resolvePG(AirSelfPropulsionPG.class);
    return (_tmp == AirSelfPropulsionPG.nullPG)?null:_tmp;
  }
  public void setAirSelfPropulsionPG(PropertyGroup arg_AirSelfPropulsionPG) {
    if (!(arg_AirSelfPropulsionPG instanceof AirSelfPropulsionPG))
      throw new IllegalArgumentException("setAirSelfPropulsionPG requires a AirSelfPropulsionPG argument.");
    myAirSelfPropulsionPG = (AirSelfPropulsionPG) arg_AirSelfPropulsionPG;
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

  private transient LiftPG myLiftPG;

  public LiftPG getLiftPG() {
    LiftPG _tmp = (myLiftPG != null) ?
      myLiftPG : (LiftPG)resolvePG(LiftPG.class);
    return (_tmp == LiftPG.nullPG)?null:_tmp;
  }
  public void setLiftPG(PropertyGroup arg_LiftPG) {
    if (!(arg_LiftPG instanceof LiftPG))
      throw new IllegalArgumentException("setLiftPG requires a LiftPG argument.");
    myLiftPG = (LiftPG) arg_LiftPG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (AirVehiclePG.class.equals(c)) {
      return (myAirVehiclePG==AirVehiclePG.nullPG)?null:myAirVehiclePG;
    }
    if (AirSelfPropulsionPG.class.equals(c)) {
      return (myAirSelfPropulsionPG==AirSelfPropulsionPG.nullPG)?null:myAirSelfPropulsionPG;
    }
    if (ContainPG.class.equals(c)) {
      return (myContainPG==ContainPG.nullPG)?null:myContainPG;
    }
    if (LiftPG.class.equals(c)) {
      return (myLiftPG==LiftPG.nullPG)?null:myLiftPG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (AirVehiclePG.class.equals(c)) {
      myAirVehiclePG=(AirVehiclePG)pg;
    } else
    if (AirSelfPropulsionPG.class.equals(c)) {
      myAirSelfPropulsionPG=(AirSelfPropulsionPG)pg;
    } else
    if (ContainPG.class.equals(c)) {
      myContainPG=(ContainPG)pg;
    } else
    if (LiftPG.class.equals(c)) {
      myLiftPG=(LiftPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (AirVehiclePG.class.equals(c)) {
      removed=myAirVehiclePG;
      myAirVehiclePG=null;
    } else if (AirSelfPropulsionPG.class.equals(c)) {
      removed=myAirSelfPropulsionPG;
      myAirSelfPropulsionPG=null;
    } else if (ContainPG.class.equals(c)) {
      removed=myContainPG;
      myContainPG=null;
    } else if (LiftPG.class.equals(c)) {
      removed=myLiftPG;
      myLiftPG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (AirVehiclePG.class.equals(pgc)) {
      PropertyGroup removed=myAirVehiclePG;
      myAirVehiclePG=null;
      return removed;
    } else if (AirSelfPropulsionPG.class.equals(pgc)) {
      PropertyGroup removed=myAirSelfPropulsionPG;
      myAirSelfPropulsionPG=null;
      return removed;
    } else if (ContainPG.class.equals(pgc)) {
      PropertyGroup removed=myContainPG;
      myContainPG=null;
      return removed;
    } else if (LiftPG.class.equals(pgc)) {
      PropertyGroup removed=myLiftPG;
      myLiftPG=null;
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
    if (AirVehiclePG.class.equals(c)) {
      return (myAirVehiclePG= new AirVehiclePGImpl());
    } else
    if (AirSelfPropulsionPG.class.equals(c)) {
      return (myAirSelfPropulsionPG= new AirSelfPropulsionPGImpl());
    } else
    if (ContainPG.class.equals(c)) {
      return (myContainPG= new ContainPGImpl());
    } else
    if (LiftPG.class.equals(c)) {
      return (myLiftPG= new LiftPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myAirVehiclePG instanceof Null_PG || myAirVehiclePG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myAirVehiclePG);
      }
      if (myAirSelfPropulsionPG instanceof Null_PG || myAirSelfPropulsionPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myAirSelfPropulsionPG);
      }
      if (myContainPG instanceof Null_PG || myContainPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myContainPG);
      }
      if (myLiftPG instanceof Null_PG || myLiftPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myLiftPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myAirVehiclePG=(AirVehiclePG)in.readObject();
      myAirSelfPropulsionPG=(AirSelfPropulsionPG)in.readObject();
      myContainPG=(ContainPG)in.readObject();
      myLiftPG=(LiftPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[4];
      properties[0] = new PropertyDescriptor("AirVehiclePG", RotaryWingAircraftWeapon.class, "getAirVehiclePG", null);
      properties[1] = new PropertyDescriptor("AirSelfPropulsionPG", RotaryWingAircraftWeapon.class, "getAirSelfPropulsionPG", null);
      properties[2] = new PropertyDescriptor("ContainPG", RotaryWingAircraftWeapon.class, "getContainPG", null);
      properties[3] = new PropertyDescriptor("LiftPG", RotaryWingAircraftWeapon.class, "getLiftPG", null);
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
