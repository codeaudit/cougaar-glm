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
public class Inventory extends GLMAsset {

  public Inventory() {
    myScheduledContentPG = null;
    myInventoryLevelsPG = null;
    myReportSchedulePG = null;
    myInventoryPG = null;
  }

  public Inventory(Inventory prototype) {
    super(prototype);
    myScheduledContentPG=null;
    myInventoryLevelsPG=null;
    myReportSchedulePG=null;
    myInventoryPG=null;
  }

  /** For infrastructure only - use org.cougaar.core.domain.Factory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    Inventory _thing = (Inventory) super.clone();
    if (myScheduledContentPG!=null) _thing.setScheduledContentPG(myScheduledContentPG.lock());
    if (myInventoryLevelsPG!=null) _thing.setInventoryLevelsPG(myInventoryLevelsPG.lock());
    if (myReportSchedulePG!=null) _thing.setReportSchedulePG(myReportSchedulePG.lock());
    if (myInventoryPG!=null) _thing.setInventoryPG(myInventoryPG.lock());
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new Inventory();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new Inventory(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    { Object _tmp = getScheduledContentPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getInventoryLevelsPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getReportSchedulePG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
    { Object _tmp = getInventoryPG();
    if (_tmp != null && !(_tmp instanceof Null_PG)) {
      v.addElement(_tmp);
    } }
  }

  private transient ScheduledContentPG myScheduledContentPG;

  public ScheduledContentPG getScheduledContentPG() {
    ScheduledContentPG _tmp = (myScheduledContentPG != null) ?
      myScheduledContentPG : (ScheduledContentPG)resolvePG(ScheduledContentPG.class);
    return (_tmp == ScheduledContentPG.nullPG)?null:_tmp;
  }
  public void setScheduledContentPG(PropertyGroup arg_ScheduledContentPG) {
    if (!(arg_ScheduledContentPG instanceof ScheduledContentPG))
      throw new IllegalArgumentException("setScheduledContentPG requires a ScheduledContentPG argument.");
    myScheduledContentPG = (ScheduledContentPG) arg_ScheduledContentPG;
  }

  private transient InventoryLevelsPG myInventoryLevelsPG;

  public InventoryLevelsPG getInventoryLevelsPG() {
    InventoryLevelsPG _tmp = (myInventoryLevelsPG != null) ?
      myInventoryLevelsPG : (InventoryLevelsPG)resolvePG(InventoryLevelsPG.class);
    return (_tmp == InventoryLevelsPG.nullPG)?null:_tmp;
  }
  public void setInventoryLevelsPG(PropertyGroup arg_InventoryLevelsPG) {
    if (!(arg_InventoryLevelsPG instanceof InventoryLevelsPG))
      throw new IllegalArgumentException("setInventoryLevelsPG requires a InventoryLevelsPG argument.");
    myInventoryLevelsPG = (InventoryLevelsPG) arg_InventoryLevelsPG;
  }

  private transient ReportSchedulePG myReportSchedulePG;

  public ReportSchedulePG getReportSchedulePG() {
    ReportSchedulePG _tmp = (myReportSchedulePG != null) ?
      myReportSchedulePG : (ReportSchedulePG)resolvePG(ReportSchedulePG.class);
    return (_tmp == ReportSchedulePG.nullPG)?null:_tmp;
  }
  public void setReportSchedulePG(PropertyGroup arg_ReportSchedulePG) {
    if (!(arg_ReportSchedulePG instanceof ReportSchedulePG))
      throw new IllegalArgumentException("setReportSchedulePG requires a ReportSchedulePG argument.");
    myReportSchedulePG = (ReportSchedulePG) arg_ReportSchedulePG;
  }

  private transient InventoryPG myInventoryPG;

  public InventoryPG getInventoryPG() {
    InventoryPG _tmp = (myInventoryPG != null) ?
      myInventoryPG : (InventoryPG)resolvePG(InventoryPG.class);
    return (_tmp == InventoryPG.nullPG)?null:_tmp;
  }
  public void setInventoryPG(PropertyGroup arg_InventoryPG) {
    if (!(arg_InventoryPG instanceof InventoryPG))
      throw new IllegalArgumentException("setInventoryPG requires a InventoryPG argument.");
    myInventoryPG = (InventoryPG) arg_InventoryPG;
  }

  // generic search methods
  public PropertyGroup getLocalPG(Class c, long t) {
    if (ScheduledContentPG.class.equals(c)) {
      return (myScheduledContentPG==ScheduledContentPG.nullPG)?null:myScheduledContentPG;
    }
    if (InventoryLevelsPG.class.equals(c)) {
      return (myInventoryLevelsPG==InventoryLevelsPG.nullPG)?null:myInventoryLevelsPG;
    }
    if (ReportSchedulePG.class.equals(c)) {
      return (myReportSchedulePG==ReportSchedulePG.nullPG)?null:myReportSchedulePG;
    }
    if (InventoryPG.class.equals(c)) {
      return (myInventoryPG==InventoryPG.nullPG)?null:myInventoryPG;
    }
    return super.getLocalPG(c,t);
  }

  public PropertyGroupSchedule getLocalPGSchedule(Class c) {
    return super.getLocalPGSchedule(c);
  }

  public void setLocalPG(Class c, PropertyGroup pg) {
    if (ScheduledContentPG.class.equals(c)) {
      myScheduledContentPG=(ScheduledContentPG)pg;
    } else
    if (InventoryLevelsPG.class.equals(c)) {
      myInventoryLevelsPG=(InventoryLevelsPG)pg;
    } else
    if (ReportSchedulePG.class.equals(c)) {
      myReportSchedulePG=(ReportSchedulePG)pg;
    } else
    if (InventoryPG.class.equals(c)) {
      myInventoryPG=(InventoryPG)pg;
    } else
      super.setLocalPG(c,pg);
  }

  public void setLocalPGSchedule(PropertyGroupSchedule pgSchedule) {
      super.setLocalPGSchedule(pgSchedule);
  }

  public PropertyGroup removeLocalPG(Class c) {
    PropertyGroup removed = null;
    if (ScheduledContentPG.class.equals(c)) {
      removed=myScheduledContentPG;
      myScheduledContentPG=null;
    } else if (InventoryLevelsPG.class.equals(c)) {
      removed=myInventoryLevelsPG;
      myInventoryLevelsPG=null;
    } else if (ReportSchedulePG.class.equals(c)) {
      removed=myReportSchedulePG;
      myReportSchedulePG=null;
    } else if (InventoryPG.class.equals(c)) {
      removed=myInventoryPG;
      myInventoryPG=null;
    } else {
      removed=super.removeLocalPG(c);
    }
    return removed;
  }

  public PropertyGroup removeLocalPG(PropertyGroup pg) {
    Class pgc = pg.getPrimaryClass();
    if (ScheduledContentPG.class.equals(pgc)) {
      PropertyGroup removed=myScheduledContentPG;
      myScheduledContentPG=null;
      return removed;
    } else if (InventoryLevelsPG.class.equals(pgc)) {
      PropertyGroup removed=myInventoryLevelsPG;
      myInventoryLevelsPG=null;
      return removed;
    } else if (ReportSchedulePG.class.equals(pgc)) {
      PropertyGroup removed=myReportSchedulePG;
      myReportSchedulePG=null;
      return removed;
    } else if (InventoryPG.class.equals(pgc)) {
      PropertyGroup removed=myInventoryPG;
      myInventoryPG=null;
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
    if (ScheduledContentPG.class.equals(c)) {
      return (myScheduledContentPG= new ScheduledContentPGImpl());
    } else
    if (InventoryLevelsPG.class.equals(c)) {
      return (myInventoryLevelsPG= new InventoryLevelsPGImpl());
    } else
    if (ReportSchedulePG.class.equals(c)) {
      return (myReportSchedulePG= new ReportSchedulePGImpl());
    } else
    if (InventoryPG.class.equals(c)) {
      return (myInventoryPG= new InventoryPGImpl());
    } else
      return super.generateDefaultPG(c);
  }

  // dumb serialization methods

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
      if (myScheduledContentPG instanceof Null_PG || myScheduledContentPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myScheduledContentPG);
      }
      if (myInventoryLevelsPG instanceof Null_PG || myInventoryLevelsPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myInventoryLevelsPG);
      }
      if (myReportSchedulePG instanceof Null_PG || myReportSchedulePG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myReportSchedulePG);
      }
      if (myInventoryPG instanceof Null_PG || myInventoryPG instanceof Future_PG) {
        out.writeObject(null);
      } else {
        out.writeObject(myInventoryPG);
      }
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
      myScheduledContentPG=(ScheduledContentPG)in.readObject();
      myInventoryLevelsPG=(InventoryLevelsPG)in.readObject();
      myReportSchedulePG=(ReportSchedulePG)in.readObject();
      myInventoryPG=(InventoryPG)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[4];
      properties[0] = new PropertyDescriptor("ScheduledContentPG", Inventory.class, "getScheduledContentPG", null);
      properties[1] = new PropertyDescriptor("InventoryLevelsPG", Inventory.class, "getInventoryLevelsPG", null);
      properties[2] = new PropertyDescriptor("ReportSchedulePG", Inventory.class, "getReportSchedulePG", null);
      properties[3] = new PropertyDescriptor("InventoryPG", Inventory.class, "getInventoryPG", null);
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
