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
public class MilitaryPerson extends Person {

  public MilitaryPerson() {
    myMilitaryPersonPG = null;
    myAssignmentPG = null;
  }

  public MilitaryPerson(MilitaryPerson prototype) {
    super(prototype);
    myMilitaryPersonPG=null;
    myAssignmentPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    MilitaryPerson _thing = (MilitaryPerson) super.clone();
    if (myMilitaryPersonPG!=null) _thing.setMilitaryPersonPG(myMilitaryPersonPG.lock());
    if (myAssignmentPG!=null) _thing.setAssignmentPG(myAssignmentPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new MilitaryPerson();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new MilitaryPerson(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getMilitaryPersonPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getAssignmentPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient MilitaryPersonPG myMilitaryPersonPG;

  public MilitaryPersonPG getMilitaryPersonPG() {
    MilitaryPersonPG _tmp = (myMilitaryPersonPG != null) ?
      myMilitaryPersonPG : (MilitaryPersonPG)resolvePG(MilitaryPersonPG.class);
    return (_tmp == MilitaryPersonPG.nullPG)?null:_tmp;
  }
  public void setMilitaryPersonPG(PropertyGroup arg_MilitaryPersonPG) {
    if (!(arg_MilitaryPersonPG instanceof MilitaryPersonPG))
      throw new IllegalArgumentException("setMilitaryPersonPG requires a MilitaryPersonPG argument.");
    myMilitaryPersonPG = (MilitaryPersonPG) arg_MilitaryPersonPG;
  }

  private transient AssignmentPG myAssignmentPG;

  public AssignmentPG getAssignmentPG() {
    AssignmentPG _tmp = (myAssignmentPG != null) ?
      myAssignmentPG : (AssignmentPG)resolvePG(AssignmentPG.class);
    return (_tmp == AssignmentPG.nullPG)?null:_tmp;
  }
  public void setAssignmentPG(PropertyGroup arg_AssignmentPG) {
    if (!(arg_AssignmentPG instanceof AssignmentPG))
      throw new IllegalArgumentException("setAssignmentPG requires a AssignmentPG argument.");
    myAssignmentPG = (AssignmentPG) arg_AssignmentPG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (MilitaryPersonPG.class.equals(c)) {
      return (myMilitaryPersonPG==MilitaryPersonPG.nullPG)?null:myMilitaryPersonPG;
    }
    if (AssignmentPG.class.equals(c)) {
      return (myAssignmentPG==AssignmentPG.nullPG)?null:myAssignmentPG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (MilitaryPersonPG.class.equals(c)) {
      myMilitaryPersonPG=(MilitaryPersonPG)pg;
    } else
    if (AssignmentPG.class.equals(c)) {
      myAssignmentPG=(AssignmentPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (MilitaryPersonPG.class.equals(c)) {
      removed=myMilitaryPersonPG;
      myMilitaryPersonPG=null;
    } else if (AssignmentPG.class.equals(c)) {
      removed=myAssignmentPG;
      myAssignmentPG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (MilitaryPersonPG.class.equals(pgc)) {
      PropertyGroup removed=myMilitaryPersonPG;
      myMilitaryPersonPG=null;
      return removed;
    } else if (AssignmentPG.class.equals(pgc)) {
      PropertyGroup removed=myAssignmentPG;
      myAssignmentPG=null;
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
    if (MilitaryPersonPG.class.equals(c)) {
      return (myMilitaryPersonPG= new MilitaryPersonPGImpl());
    } else
    if (AssignmentPG.class.equals(c)) {
      return (myAssignmentPG= new AssignmentPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myMilitaryPersonPG instanceof Null_PG || myMilitaryPersonPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myMilitaryPersonPG);
      }
      if (myAssignmentPG instanceof Null_PG || myAssignmentPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myAssignmentPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myMilitaryPersonPG=(MilitaryPersonPG)in.readObject();
      myAssignmentPG=(AssignmentPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[2];
      properties[0] = new PropertyDescriptor("MilitaryPersonPG", MilitaryPerson.class, "getMilitaryPersonPG", null);
      properties[1] = new PropertyDescriptor("AssignmentPG", MilitaryPerson.class, "getAssignmentPG", null);
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
