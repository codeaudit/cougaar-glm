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
public class VolumetricInventory extends Inventory {

  public VolumetricInventory() {
    myVolumetricStockagePG = null;
  }

  public VolumetricInventory(VolumetricInventory prototype) {
    super(prototype);
    myVolumetricStockagePG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    VolumetricInventory _thing = (VolumetricInventory) super.clone();
    if (myVolumetricStockagePG!=null) _thing.setVolumetricStockagePG(myVolumetricStockagePG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new VolumetricInventory();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new VolumetricInventory(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getVolumetricStockagePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient VolumetricStockagePG myVolumetricStockagePG;

  public VolumetricStockagePG getVolumetricStockagePG() {
    VolumetricStockagePG _tmp = (myVolumetricStockagePG != null) ?
      myVolumetricStockagePG : (VolumetricStockagePG)resolvePG(VolumetricStockagePG.class);
    return (_tmp == VolumetricStockagePG.nullPG)?null:_tmp;
  }
  public void setVolumetricStockagePG(PropertyGroup arg_VolumetricStockagePG) {
    if (!(arg_VolumetricStockagePG instanceof VolumetricStockagePG))
      throw new IllegalArgumentException("setVolumetricStockagePG requires a VolumetricStockagePG argument.");
    myVolumetricStockagePG = (VolumetricStockagePG) arg_VolumetricStockagePG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (VolumetricStockagePG.class.equals(c)) {
      return (myVolumetricStockagePG==VolumetricStockagePG.nullPG)?null:myVolumetricStockagePG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (VolumetricStockagePG.class.equals(c)) {
      myVolumetricStockagePG=(VolumetricStockagePG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (VolumetricStockagePG.class.equals(c)) {
      removed=myVolumetricStockagePG;
      myVolumetricStockagePG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (VolumetricStockagePG.class.equals(pgc)) {
      PropertyGroup removed=myVolumetricStockagePG;
      myVolumetricStockagePG=null;
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
    if (VolumetricStockagePG.class.equals(c)) {
      return (myVolumetricStockagePG= new VolumetricStockagePGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myVolumetricStockagePG instanceof Null_PG || myVolumetricStockagePG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myVolumetricStockagePG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myVolumetricStockagePG=(VolumetricStockagePG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[1];
      properties[0] = new PropertyDescriptor("VolumetricStockagePG", VolumetricInventory.class, "getVolumetricStockagePG", null);
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
