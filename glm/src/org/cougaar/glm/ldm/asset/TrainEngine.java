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
public class TrainEngine extends CargoVehicle {

  public TrainEngine() {
    myRailVehiclePG = null;
    myRailSelfPropulsionPG = null;
    myTowPG = null;
  }

  public TrainEngine(TrainEngine prototype) {
    super(prototype);
    myRailVehiclePG=null;
    myRailSelfPropulsionPG=null;
    myTowPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    TrainEngine _thing = (TrainEngine) super.clone();
    if (myRailVehiclePG!=null) _thing.setRailVehiclePG(myRailVehiclePG.lock());
    if (myRailSelfPropulsionPG!=null) _thing.setRailSelfPropulsionPG(myRailSelfPropulsionPG.lock());
    if (myTowPG!=null) _thing.setTowPG(myTowPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new TrainEngine();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new TrainEngine(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getRailVehiclePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getRailSelfPropulsionPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getTowPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient RailVehiclePG myRailVehiclePG;

  public RailVehiclePG getRailVehiclePG() {
    RailVehiclePG _tmp = (myRailVehiclePG != null) ?
      myRailVehiclePG : (RailVehiclePG)resolvePG(RailVehiclePG.class);
    return (_tmp == RailVehiclePG.nullPG)?null:_tmp;
  }
  public void setRailVehiclePG(PropertyGroup arg_RailVehiclePG) {
    if (!(arg_RailVehiclePG instanceof RailVehiclePG))
      throw new IllegalArgumentException("setRailVehiclePG requires a RailVehiclePG argument.");
    myRailVehiclePG = (RailVehiclePG) arg_RailVehiclePG;
  }

  private transient RailSelfPropulsionPG myRailSelfPropulsionPG;

  public RailSelfPropulsionPG getRailSelfPropulsionPG() {
    RailSelfPropulsionPG _tmp = (myRailSelfPropulsionPG != null) ?
      myRailSelfPropulsionPG : (RailSelfPropulsionPG)resolvePG(RailSelfPropulsionPG.class);
    return (_tmp == RailSelfPropulsionPG.nullPG)?null:_tmp;
  }
  public void setRailSelfPropulsionPG(PropertyGroup arg_RailSelfPropulsionPG) {
    if (!(arg_RailSelfPropulsionPG instanceof RailSelfPropulsionPG))
      throw new IllegalArgumentException("setRailSelfPropulsionPG requires a RailSelfPropulsionPG argument.");
    myRailSelfPropulsionPG = (RailSelfPropulsionPG) arg_RailSelfPropulsionPG;
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
    if (RailVehiclePG.class.equals(c)) {
      return (myRailVehiclePG==RailVehiclePG.nullPG)?null:myRailVehiclePG;
    }
    if (RailSelfPropulsionPG.class.equals(c)) {
      return (myRailSelfPropulsionPG==RailSelfPropulsionPG.nullPG)?null:myRailSelfPropulsionPG;
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
    if (RailVehiclePG.class.equals(c)) {
      myRailVehiclePG=(RailVehiclePG)pg;
    } else
    if (RailSelfPropulsionPG.class.equals(c)) {
      myRailSelfPropulsionPG=(RailSelfPropulsionPG)pg;
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
    if (RailVehiclePG.class.equals(c)) {
      removed=myRailVehiclePG;
      myRailVehiclePG=null;
    } else if (RailSelfPropulsionPG.class.equals(c)) {
      removed=myRailSelfPropulsionPG;
      myRailSelfPropulsionPG=null;
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
    if (RailVehiclePG.class.equals(pgc)) {
      PropertyGroup removed=myRailVehiclePG;
      myRailVehiclePG=null;
      return removed;
    } else if (RailSelfPropulsionPG.class.equals(pgc)) {
      PropertyGroup removed=myRailSelfPropulsionPG;
      myRailSelfPropulsionPG=null;
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
    if (RailVehiclePG.class.equals(c)) {
      return (myRailVehiclePG= new RailVehiclePGImpl());
    } else
    if (RailSelfPropulsionPG.class.equals(c)) {
      return (myRailSelfPropulsionPG= new RailSelfPropulsionPGImpl());
    } else
    if (TowPG.class.equals(c)) {
      return (myTowPG= new TowPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myRailVehiclePG instanceof Null_PG || myRailVehiclePG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myRailVehiclePG);
      }
      if (myRailSelfPropulsionPG instanceof Null_PG || myRailSelfPropulsionPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myRailSelfPropulsionPG);
      }
      if (myTowPG instanceof Null_PG || myTowPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myTowPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myRailVehiclePG=(RailVehiclePG)in.readObject();
      myRailSelfPropulsionPG=(RailSelfPropulsionPG)in.readObject();
      myTowPG=(TowPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[3];
      properties[0] = new PropertyDescriptor("RailVehiclePG", TrainEngine.class, "getRailVehiclePG", null);
      properties[1] = new PropertyDescriptor("RailSelfPropulsionPG", TrainEngine.class, "getRailSelfPropulsionPG", null);
      properties[2] = new PropertyDescriptor("TowPG", TrainEngine.class, "getTowPG", null);
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
