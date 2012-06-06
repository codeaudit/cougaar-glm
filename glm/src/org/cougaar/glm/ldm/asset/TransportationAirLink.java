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
public class TransportationAirLink extends TransportationLink {

  public TransportationAirLink() {
    myAirLinkPG = null;
  }

  public TransportationAirLink(TransportationAirLink prototype) {
    super(prototype);
    myAirLinkPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    TransportationAirLink _thing = (TransportationAirLink) super.clone();
    if (myAirLinkPG!=null) _thing.setAirLinkPG(myAirLinkPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new TransportationAirLink();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new TransportationAirLink(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getAirLinkPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient AirLinkPG myAirLinkPG;

  public AirLinkPG getAirLinkPG() {
    AirLinkPG _tmp = (myAirLinkPG != null) ?
      myAirLinkPG : (AirLinkPG)resolvePG(AirLinkPG.class);
    return (_tmp == AirLinkPG.nullPG)?null:_tmp;
  }
  public void setAirLinkPG(PropertyGroup arg_AirLinkPG) {
    if (!(arg_AirLinkPG instanceof AirLinkPG))
      throw new IllegalArgumentException("setAirLinkPG requires a AirLinkPG argument.");
    myAirLinkPG = (AirLinkPG) arg_AirLinkPG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (AirLinkPG.class.equals(c)) {
      return (myAirLinkPG==AirLinkPG.nullPG)?null:myAirLinkPG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (AirLinkPG.class.equals(c)) {
      myAirLinkPG=(AirLinkPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (AirLinkPG.class.equals(c)) {
      removed=myAirLinkPG;
      myAirLinkPG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (AirLinkPG.class.equals(pgc)) {
      PropertyGroup removed=myAirLinkPG;
      myAirLinkPG=null;
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
    if (AirLinkPG.class.equals(c)) {
      return (myAirLinkPG= new AirLinkPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myAirLinkPG instanceof Null_PG || myAirLinkPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myAirLinkPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myAirLinkPG=(AirLinkPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[1];
      properties[0] = new PropertyDescriptor("AirLinkPG", TransportationAirLink.class, "getAirLinkPG", null);
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
