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
public class ClassVAmmunition extends PhysicalAsset {

  public ClassVAmmunition() {
    myAmmunitionPG = null;
    myPackagePG = null;
  }

  public ClassVAmmunition(ClassVAmmunition prototype) {
    super(prototype);
    myAmmunitionPG=null;
    myPackagePG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    ClassVAmmunition _thing = (ClassVAmmunition) super.clone();
    if (myAmmunitionPG!=null) _thing.setAmmunitionPG(myAmmunitionPG.lock());
    if (myPackagePG!=null) _thing.setPackagePG(myPackagePG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new ClassVAmmunition();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new ClassVAmmunition(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getAmmunitionPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getPackagePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient AmmunitionPG myAmmunitionPG;

  public AmmunitionPG getAmmunitionPG() {
    AmmunitionPG _tmp = (myAmmunitionPG != null) ?
      myAmmunitionPG : (AmmunitionPG)resolvePG(AmmunitionPG.class);
    return (_tmp == AmmunitionPG.nullPG)?null:_tmp;
  }
  public void setAmmunitionPG(PropertyGroup arg_AmmunitionPG) {
    if (!(arg_AmmunitionPG instanceof AmmunitionPG))
      throw new IllegalArgumentException("setAmmunitionPG requires a AmmunitionPG argument.");
    myAmmunitionPG = (AmmunitionPG) arg_AmmunitionPG;
  }

  private transient PackagePG myPackagePG;

  public PackagePG getPackagePG() {
    PackagePG _tmp = (myPackagePG != null) ?
      myPackagePG : (PackagePG)resolvePG(PackagePG.class);
    return (_tmp == PackagePG.nullPG)?null:_tmp;
  }
  public void setPackagePG(PropertyGroup arg_PackagePG) {
    if (!(arg_PackagePG instanceof PackagePG))
      throw new IllegalArgumentException("setPackagePG requires a PackagePG argument.");
    myPackagePG = (PackagePG) arg_PackagePG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (AmmunitionPG.class.equals(c)) {
      return (myAmmunitionPG==AmmunitionPG.nullPG)?null:myAmmunitionPG;
    }
    if (PackagePG.class.equals(c)) {
      return (myPackagePG==PackagePG.nullPG)?null:myPackagePG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (AmmunitionPG.class.equals(c)) {
      myAmmunitionPG=(AmmunitionPG)pg;
    } else
    if (PackagePG.class.equals(c)) {
      myPackagePG=(PackagePG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (AmmunitionPG.class.equals(c)) {
      removed=myAmmunitionPG;
      myAmmunitionPG=null;
    } else if (PackagePG.class.equals(c)) {
      removed=myPackagePG;
      myPackagePG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (AmmunitionPG.class.equals(pgc)) {
      PropertyGroup removed=myAmmunitionPG;
      myAmmunitionPG=null;
      return removed;
    } else if (PackagePG.class.equals(pgc)) {
      PropertyGroup removed=myPackagePG;
      myPackagePG=null;
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
    if (AmmunitionPG.class.equals(c)) {
      return (myAmmunitionPG= new AmmunitionPGImpl());
    } else
    if (PackagePG.class.equals(c)) {
      return (myPackagePG= new PackagePGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myAmmunitionPG instanceof Null_PG || myAmmunitionPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myAmmunitionPG);
      }
      if (myPackagePG instanceof Null_PG || myPackagePG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myPackagePG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myAmmunitionPG=(AmmunitionPG)in.readObject();
      myPackagePG=(PackagePG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[2];
      properties[0] = new PropertyDescriptor("AmmunitionPG", ClassVAmmunition.class, "getAmmunitionPG", null);
      properties[1] = new PropertyDescriptor("PackagePG", ClassVAmmunition.class, "getPackagePG", null);
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
