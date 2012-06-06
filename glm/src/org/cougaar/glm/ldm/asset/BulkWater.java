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
public class BulkWater extends ClassISubsistence {

  public BulkWater() {
    myWaterPG = null;
    myLiquidPG = null;
  }

  public BulkWater(BulkWater prototype) {
    super(prototype);
    myWaterPG=null;
    myLiquidPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    BulkWater _thing = (BulkWater) super.clone();
    if (myWaterPG!=null) _thing.setWaterPG(myWaterPG.lock());
    if (myLiquidPG!=null) _thing.setLiquidPG(myLiquidPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new BulkWater();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new BulkWater(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getWaterPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getLiquidPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient WaterPG myWaterPG;

  public WaterPG getWaterPG() {
    WaterPG _tmp = (myWaterPG != null) ?
      myWaterPG : (WaterPG)resolvePG(WaterPG.class);
    return (_tmp == WaterPG.nullPG)?null:_tmp;
  }
  public void setWaterPG(PropertyGroup arg_WaterPG) {
    if (!(arg_WaterPG instanceof WaterPG))
      throw new IllegalArgumentException("setWaterPG requires a WaterPG argument.");
    myWaterPG = (WaterPG) arg_WaterPG;
  }

  private transient LiquidPG myLiquidPG;

  public LiquidPG getLiquidPG() {
    LiquidPG _tmp = (myLiquidPG != null) ?
      myLiquidPG : (LiquidPG)resolvePG(LiquidPG.class);
    return (_tmp == LiquidPG.nullPG)?null:_tmp;
  }
  public void setLiquidPG(PropertyGroup arg_LiquidPG) {
    if (!(arg_LiquidPG instanceof LiquidPG))
      throw new IllegalArgumentException("setLiquidPG requires a LiquidPG argument.");
    myLiquidPG = (LiquidPG) arg_LiquidPG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (WaterPG.class.equals(c)) {
      return (myWaterPG==WaterPG.nullPG)?null:myWaterPG;
    }
    if (LiquidPG.class.equals(c)) {
      return (myLiquidPG==LiquidPG.nullPG)?null:myLiquidPG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (WaterPG.class.equals(c)) {
      myWaterPG=(WaterPG)pg;
    } else
    if (LiquidPG.class.equals(c)) {
      myLiquidPG=(LiquidPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (WaterPG.class.equals(c)) {
      removed=myWaterPG;
      myWaterPG=null;
    } else if (LiquidPG.class.equals(c)) {
      removed=myLiquidPG;
      myLiquidPG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (WaterPG.class.equals(pgc)) {
      PropertyGroup removed=myWaterPG;
      myWaterPG=null;
      return removed;
    } else if (LiquidPG.class.equals(pgc)) {
      PropertyGroup removed=myLiquidPG;
      myLiquidPG=null;
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
    if (WaterPG.class.equals(c)) {
      return (myWaterPG= new WaterPGImpl());
    } else
    if (LiquidPG.class.equals(c)) {
      return (myLiquidPG= new LiquidPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myWaterPG instanceof Null_PG || myWaterPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myWaterPG);
      }
      if (myLiquidPG instanceof Null_PG || myLiquidPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myLiquidPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myWaterPG=(WaterPG)in.readObject();
      myLiquidPG=(LiquidPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[2];
      properties[0] = new PropertyDescriptor("WaterPG", BulkWater.class, "getWaterPG", null);
      properties[1] = new PropertyDescriptor("LiquidPG", BulkWater.class, "getLiquidPG", null);
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
