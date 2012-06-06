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
public class TowedGroundWeapon extends Weapon {

  public TowedGroundWeapon() {
    myGroundVehiclePG = null;
  }

  public TowedGroundWeapon(TowedGroundWeapon prototype) {
    super(prototype);
    myGroundVehiclePG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    TowedGroundWeapon _thing = (TowedGroundWeapon) super.clone();
    if (myGroundVehiclePG!=null) _thing.setGroundVehiclePG(myGroundVehiclePG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new TowedGroundWeapon();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new TowedGroundWeapon(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getGroundVehiclePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
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

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (GroundVehiclePG.class.equals(c)) {
      return (myGroundVehiclePG==GroundVehiclePG.nullPG)?null:myGroundVehiclePG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (GroundVehiclePG.class.equals(c)) {
      myGroundVehiclePG=(GroundVehiclePG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (GroundVehiclePG.class.equals(c)) {
      removed=myGroundVehiclePG;
      myGroundVehiclePG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (GroundVehiclePG.class.equals(pgc)) {
      PropertyGroup removed=myGroundVehiclePG;
      myGroundVehiclePG=null;
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
    if (GroundVehiclePG.class.equals(c)) {
      return (myGroundVehiclePG= new GroundVehiclePGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myGroundVehiclePG instanceof Null_PG || myGroundVehiclePG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myGroundVehiclePG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myGroundVehiclePG=(GroundVehiclePG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[1];
      properties[0] = new PropertyDescriptor("GroundVehiclePG", TowedGroundWeapon.class, "getGroundVehiclePG", null);
    } catch (IntrospectionException ie) {}
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pds = super.getPropertyDescriptors();
    PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+1];
    System.arraycopy(pds, 0, ps, 0, pds.length);
    System.arraycopy(properties, 0, ps, pds.length, 1);
    return ps;
  }
}
