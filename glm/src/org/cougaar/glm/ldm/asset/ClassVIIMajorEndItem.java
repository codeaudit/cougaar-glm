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
public class ClassVIIMajorEndItem extends PhysicalAsset {

  public ClassVIIMajorEndItem() {
    myMaintenancePG = null;
    myMovabilityPG = null;
    myAssetConsumptionRatePG = null;
    myMEIPG = null;
  }

  public ClassVIIMajorEndItem(ClassVIIMajorEndItem prototype) {
    super(prototype);
    myMaintenancePG=null;
    myMovabilityPG=null;
    myAssetConsumptionRatePG=null;
    myMEIPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    ClassVIIMajorEndItem _thing = (ClassVIIMajorEndItem) super.clone();
    if (myMaintenancePG!=null) _thing.setMaintenancePG(myMaintenancePG.lock());
    if (myMovabilityPG!=null) _thing.setMovabilityPG(myMovabilityPG.lock());
    if (myAssetConsumptionRatePG!=null) _thing.setAssetConsumptionRatePG(myAssetConsumptionRatePG.lock());
    if (myMEIPG!=null) _thing.setMEIPG(myMEIPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new ClassVIIMajorEndItem();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new ClassVIIMajorEndItem(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getMaintenancePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getMovabilityPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getAssetConsumptionRatePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getMEIPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient MaintenancePG myMaintenancePG;

  public MaintenancePG getMaintenancePG() {
    MaintenancePG _tmp = (myMaintenancePG != null) ?
      myMaintenancePG : (MaintenancePG)resolvePG(MaintenancePG.class);
    return (_tmp == MaintenancePG.nullPG)?null:_tmp;
  }
  public void setMaintenancePG(PropertyGroup arg_MaintenancePG) {
    if (!(arg_MaintenancePG instanceof MaintenancePG))
      throw new IllegalArgumentException("setMaintenancePG requires a MaintenancePG argument.");
    myMaintenancePG = (MaintenancePG) arg_MaintenancePG;
  }

  private transient MovabilityPG myMovabilityPG;

  public MovabilityPG getMovabilityPG() {
    MovabilityPG _tmp = (myMovabilityPG != null) ?
      myMovabilityPG : (MovabilityPG)resolvePG(MovabilityPG.class);
    return (_tmp == MovabilityPG.nullPG)?null:_tmp;
  }
  public void setMovabilityPG(PropertyGroup arg_MovabilityPG) {
    if (!(arg_MovabilityPG instanceof MovabilityPG))
      throw new IllegalArgumentException("setMovabilityPG requires a MovabilityPG argument.");
    myMovabilityPG = (MovabilityPG) arg_MovabilityPG;
  }

  private transient AssetConsumptionRatePG myAssetConsumptionRatePG;

  public AssetConsumptionRatePG getAssetConsumptionRatePG() {
    AssetConsumptionRatePG _tmp = (myAssetConsumptionRatePG != null) ?
      myAssetConsumptionRatePG : (AssetConsumptionRatePG)resolvePG(AssetConsumptionRatePG.class);
    return (_tmp == AssetConsumptionRatePG.nullPG)?null:_tmp;
  }
  public void setAssetConsumptionRatePG(PropertyGroup arg_AssetConsumptionRatePG) {
    if (!(arg_AssetConsumptionRatePG instanceof AssetConsumptionRatePG))
      throw new IllegalArgumentException("setAssetConsumptionRatePG requires a AssetConsumptionRatePG argument.");
    myAssetConsumptionRatePG = (AssetConsumptionRatePG) arg_AssetConsumptionRatePG;
  }

  private transient MEIPG myMEIPG;

  public MEIPG getMEIPG() {
    MEIPG _tmp = (myMEIPG != null) ?
      myMEIPG : (MEIPG)resolvePG(MEIPG.class);
    return (_tmp == MEIPG.nullPG)?null:_tmp;
  }
  public void setMEIPG(PropertyGroup arg_MEIPG) {
    if (!(arg_MEIPG instanceof MEIPG))
      throw new IllegalArgumentException("setMEIPG requires a MEIPG argument.");
    myMEIPG = (MEIPG) arg_MEIPG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (MaintenancePG.class.equals(c)) {
      return (myMaintenancePG==MaintenancePG.nullPG)?null:myMaintenancePG;
    }
    if (MovabilityPG.class.equals(c)) {
      return (myMovabilityPG==MovabilityPG.nullPG)?null:myMovabilityPG;
    }
    if (AssetConsumptionRatePG.class.equals(c)) {
      return (myAssetConsumptionRatePG==AssetConsumptionRatePG.nullPG)?null:myAssetConsumptionRatePG;
    }
    if (MEIPG.class.equals(c)) {
      return (myMEIPG==MEIPG.nullPG)?null:myMEIPG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (MaintenancePG.class.equals(c)) {
      myMaintenancePG=(MaintenancePG)pg;
    } else
    if (MovabilityPG.class.equals(c)) {
      myMovabilityPG=(MovabilityPG)pg;
    } else
    if (AssetConsumptionRatePG.class.equals(c)) {
      myAssetConsumptionRatePG=(AssetConsumptionRatePG)pg;
    } else
    if (MEIPG.class.equals(c)) {
      myMEIPG=(MEIPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (MaintenancePG.class.equals(c)) {
      removed=myMaintenancePG;
      myMaintenancePG=null;
    } else if (MovabilityPG.class.equals(c)) {
      removed=myMovabilityPG;
      myMovabilityPG=null;
    } else if (AssetConsumptionRatePG.class.equals(c)) {
      removed=myAssetConsumptionRatePG;
      myAssetConsumptionRatePG=null;
    } else if (MEIPG.class.equals(c)) {
      removed=myMEIPG;
      myMEIPG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (MaintenancePG.class.equals(pgc)) {
      PropertyGroup removed=myMaintenancePG;
      myMaintenancePG=null;
      return removed;
    } else if (MovabilityPG.class.equals(pgc)) {
      PropertyGroup removed=myMovabilityPG;
      myMovabilityPG=null;
      return removed;
    } else if (AssetConsumptionRatePG.class.equals(pgc)) {
      PropertyGroup removed=myAssetConsumptionRatePG;
      myAssetConsumptionRatePG=null;
      return removed;
    } else if (MEIPG.class.equals(pgc)) {
      PropertyGroup removed=myMEIPG;
      myMEIPG=null;
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
    if (MaintenancePG.class.equals(c)) {
      return (myMaintenancePG= new MaintenancePGImpl());
    } else
    if (MovabilityPG.class.equals(c)) {
      return (myMovabilityPG= new MovabilityPGImpl());
    } else
    if (AssetConsumptionRatePG.class.equals(c)) {
      return (myAssetConsumptionRatePG= new AssetConsumptionRatePGImpl());
    } else
    if (MEIPG.class.equals(c)) {
      return (myMEIPG= new MEIPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myMaintenancePG instanceof Null_PG || myMaintenancePG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myMaintenancePG);
      }
      if (myMovabilityPG instanceof Null_PG || myMovabilityPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myMovabilityPG);
      }
      if (myAssetConsumptionRatePG instanceof Null_PG || myAssetConsumptionRatePG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myAssetConsumptionRatePG);
      }
      if (myMEIPG instanceof Null_PG || myMEIPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myMEIPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myMaintenancePG=(MaintenancePG)in.readObject();
      myMovabilityPG=(MovabilityPG)in.readObject();
      myAssetConsumptionRatePG=(AssetConsumptionRatePG)in.readObject();
      myMEIPG=(MEIPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[4];
      properties[0] = new PropertyDescriptor("MaintenancePG", ClassVIIMajorEndItem.class, "getMaintenancePG", null);
      properties[1] = new PropertyDescriptor("MovabilityPG", ClassVIIMajorEndItem.class, "getMovabilityPG", null);
      properties[2] = new PropertyDescriptor("AssetConsumptionRatePG", ClassVIIMajorEndItem.class, "getAssetConsumptionRatePG", null);
      properties[3] = new PropertyDescriptor("MEIPG", ClassVIIMajorEndItem.class, "getMEIPG", null);
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
