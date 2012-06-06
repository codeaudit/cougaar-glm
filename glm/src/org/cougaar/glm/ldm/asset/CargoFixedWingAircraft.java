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
public class CargoFixedWingAircraft extends CargoVehicle {

  public CargoFixedWingAircraft() {
    myAirLiftPG = null;
    myAirVehiclePG = null;
    myAirSelfPropulsionPG = null;
  }

  public CargoFixedWingAircraft(CargoFixedWingAircraft prototype) {
    super(prototype);
    myAirLiftPG=null;
    myAirVehiclePG=null;
    myAirSelfPropulsionPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    CargoFixedWingAircraft _thing = (CargoFixedWingAircraft) super.clone();
    if (myAirLiftPG!=null) _thing.setAirLiftPG(myAirLiftPG.lock());
    if (myAirVehiclePG!=null) _thing.setAirVehiclePG(myAirVehiclePG.lock());
    if (myAirSelfPropulsionPG!=null) _thing.setAirSelfPropulsionPG(myAirSelfPropulsionPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new CargoFixedWingAircraft();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new CargoFixedWingAircraft(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getAirLiftPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getAirVehiclePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getAirSelfPropulsionPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient AirLiftPG myAirLiftPG;

  public AirLiftPG getAirLiftPG() {
    AirLiftPG _tmp = (myAirLiftPG != null) ?
      myAirLiftPG : (AirLiftPG)resolvePG(AirLiftPG.class);
    return (_tmp == AirLiftPG.nullPG)?null:_tmp;
  }
  public void setAirLiftPG(PropertyGroup arg_AirLiftPG) {
    if (!(arg_AirLiftPG instanceof AirLiftPG))
      throw new IllegalArgumentException("setAirLiftPG requires a AirLiftPG argument.");
    myAirLiftPG = (AirLiftPG) arg_AirLiftPG;
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

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (AirLiftPG.class.equals(c)) {
      return (myAirLiftPG==AirLiftPG.nullPG)?null:myAirLiftPG;
    }
    if (AirVehiclePG.class.equals(c)) {
      return (myAirVehiclePG==AirVehiclePG.nullPG)?null:myAirVehiclePG;
    }
    if (AirSelfPropulsionPG.class.equals(c)) {
      return (myAirSelfPropulsionPG==AirSelfPropulsionPG.nullPG)?null:myAirSelfPropulsionPG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (AirLiftPG.class.equals(c)) {
      myAirLiftPG=(AirLiftPG)pg;
    } else
    if (AirVehiclePG.class.equals(c)) {
      myAirVehiclePG=(AirVehiclePG)pg;
    } else
    if (AirSelfPropulsionPG.class.equals(c)) {
      myAirSelfPropulsionPG=(AirSelfPropulsionPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (AirLiftPG.class.equals(c)) {
      removed=myAirLiftPG;
      myAirLiftPG=null;
    } else if (AirVehiclePG.class.equals(c)) {
      removed=myAirVehiclePG;
      myAirVehiclePG=null;
    } else if (AirSelfPropulsionPG.class.equals(c)) {
      removed=myAirSelfPropulsionPG;
      myAirSelfPropulsionPG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (AirLiftPG.class.equals(pgc)) {
      PropertyGroup removed=myAirLiftPG;
      myAirLiftPG=null;
      return removed;
    } else if (AirVehiclePG.class.equals(pgc)) {
      PropertyGroup removed=myAirVehiclePG;
      myAirVehiclePG=null;
      return removed;
    } else if (AirSelfPropulsionPG.class.equals(pgc)) {
      PropertyGroup removed=myAirSelfPropulsionPG;
      myAirSelfPropulsionPG=null;
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
    if (AirLiftPG.class.equals(c)) {
      return (myAirLiftPG= new AirLiftPGImpl());
    } else
    if (AirVehiclePG.class.equals(c)) {
      return (myAirVehiclePG= new AirVehiclePGImpl());
    } else
    if (AirSelfPropulsionPG.class.equals(c)) {
      return (myAirSelfPropulsionPG= new AirSelfPropulsionPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myAirLiftPG instanceof Null_PG || myAirLiftPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myAirLiftPG);
      }
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
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myAirLiftPG=(AirLiftPG)in.readObject();
      myAirVehiclePG=(AirVehiclePG)in.readObject();
      myAirSelfPropulsionPG=(AirSelfPropulsionPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[3];
      properties[0] = new PropertyDescriptor("AirLiftPG", CargoFixedWingAircraft.class, "getAirLiftPG", null);
      properties[1] = new PropertyDescriptor("AirVehiclePG", CargoFixedWingAircraft.class, "getAirVehiclePG", null);
      properties[2] = new PropertyDescriptor("AirSelfPropulsionPG", CargoFixedWingAircraft.class, "getAirSelfPropulsionPG", null);
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
