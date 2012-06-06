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
public class Barge extends CargoVehicle {

  public Barge() {
    myWaterVehiclePG = null;
    myTowPG = null;
  }

  public Barge(Barge prototype) {
    super(prototype);
    myWaterVehiclePG=null;
    myTowPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    Barge _thing = (Barge) super.clone();
    if (myWaterVehiclePG!=null) _thing.setWaterVehiclePG(myWaterVehiclePG.lock());
    if (myTowPG!=null) _thing.setTowPG(myTowPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new Barge();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new Barge(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getWaterVehiclePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getTowPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
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
    if (WaterVehiclePG.class.equals(c)) {
      return (myWaterVehiclePG==WaterVehiclePG.nullPG)?null:myWaterVehiclePG;
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
    if (WaterVehiclePG.class.equals(c)) {
      myWaterVehiclePG=(WaterVehiclePG)pg;
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
    if (WaterVehiclePG.class.equals(c)) {
      removed=myWaterVehiclePG;
      myWaterVehiclePG=null;
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
    if (WaterVehiclePG.class.equals(pgc)) {
      PropertyGroup removed=myWaterVehiclePG;
      myWaterVehiclePG=null;
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
    if (WaterVehiclePG.class.equals(c)) {
      return (myWaterVehiclePG= new WaterVehiclePGImpl());
    } else
    if (TowPG.class.equals(c)) {
      return (myTowPG= new TowPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myWaterVehiclePG instanceof Null_PG || myWaterVehiclePG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myWaterVehiclePG);
      }
      if (myTowPG instanceof Null_PG || myTowPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myTowPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myWaterVehiclePG=(WaterVehiclePG)in.readObject();
      myTowPG=(TowPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[2];
      properties[0] = new PropertyDescriptor("WaterVehiclePG", Barge.class, "getWaterVehiclePG", null);
      properties[1] = new PropertyDescriptor("TowPG", Barge.class, "getTowPG", null);
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
