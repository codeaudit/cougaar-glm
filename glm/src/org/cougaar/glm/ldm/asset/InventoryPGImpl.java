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

/* @generated Wed Jun 06 08:28:58 EDT 2012 from alpprops.def - DO NOT HAND EDIT */
/** Implementation of InventoryPG.
 *  @see InventoryPG
 *  @see NewInventoryPG
 **/

package org.cougaar.glm.ldm.asset;

import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import java.util.*;

import  org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.policy.*;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.execution.common.InventoryReport;


import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;

public class InventoryPGImpl extends java.beans.SimpleBeanInfo
  implements NewInventoryPG, Cloneable
{
  public InventoryPGImpl() {
  }

  // Slots

  private Scalar theCapacity;
  public Scalar getCapacity(){ return theCapacity; }
  public void setCapacity(Scalar capacity) {
    theCapacity=capacity;
  }
  private Scalar theInitialLevel;
  public Scalar getInitialLevel(){ return theInitialLevel; }
  public void setInitialLevel(Scalar initialLevel) {
    theInitialLevel=initialLevel;
  }
  private Scalar theReorderLevel;
  public Scalar getReorderLevel(){ return theReorderLevel; }
  public void setReorderLevel(Scalar reorderLevel) {
    theReorderLevel=reorderLevel;
  }
  private Scalar theMinReorder;
  public Scalar getMinReorder(){ return theMinReorder; }
  public void setMinReorder(Scalar minReorder) {
    theMinReorder=minReorder;
  }
  private Scalar theUnobtainable;
  public Scalar getUnobtainable(){ return theUnobtainable; }
  public void setUnobtainable(Scalar unobtainable) {
    theUnobtainable=unobtainable;
  }
  private boolean theFillToCapacity;
  public boolean getFillToCapacity(){ return theFillToCapacity; }
  public void setFillToCapacity(boolean fillToCapacity) {
    theFillToCapacity=fillToCapacity;
  }
  private boolean theMaintainAtCapacity;
  public boolean getMaintainAtCapacity(){ return theMaintainAtCapacity; }
  public void setMaintainAtCapacity(boolean maintainAtCapacity) {
    theMaintainAtCapacity=maintainAtCapacity;
  }
  private Asset theResource;
  public Asset getResource(){ return theResource; }
  public void setResource(Asset resource) {
    theResource=resource;
  }

  private InventoryBG invBG = null;
  public InventoryBG getInvBG() {
    return invBG;
  }
  public void setInvBG(InventoryBG _invBG) {
    if (invBG != null) throw new IllegalArgumentException("invBG already set");
    invBG = _invBG;
  }
  public int resetInventory(Inventory inventory, long today) { return invBG.resetInventory(inventory, today);  }
  public int getToday() { return invBG.getToday();  }
  public int getFirstPlanningDay() { return invBG.getFirstPlanningDay();  }
  public Scalar getLevel(long day) { return invBG.getLevel(day);  }
  public Scalar getLevel(int day) { return invBG.getLevel(day);  }
  public int withdrawFromInventory(Inventory inventory, MessageAddress cluster) { return invBG.withdrawFromInventory(inventory, cluster);  }
  public double getNDaysDemand(int day) { return invBG.getNDaysDemand(day);  }
  public double getReorderLevel(int day) { return invBG.getReorderLevel(day);  }
  public double getGoalLevel(int day) { return invBG.getGoalLevel(day);  }
  public int addPreviousRefillsToInventory(Task maintainInv) { return invBG.addPreviousRefillsToInventory(maintainInv);  }
  public int addDueIn(Task refillTask) { return invBG.addDueIn(refillTask);  }
  public int removeDueIn(Task refillTask) { return invBG.removeDueIn(refillTask);  }
  public int getPlanningDays() { return invBG.getPlanningDays();  }
  public void computeThresholdSchedule(int daysOfDemand, int daysForward, int daysBackward, double minReorderLevel, double maxReorderLevel, double goalLevelMultiplier) { invBG.computeThresholdSchedule(daysOfDemand, daysForward, daysBackward, minReorderLevel, maxReorderLevel, goalLevelMultiplier);  }
  public int determineInventoryLevels() { return invBG.determineInventoryLevels();  }
  public Task refillAlreadyFailedOnDay(int day) { return invBG.refillAlreadyFailedOnDay(day);  }
  public Task getRefillOnDay(int day) { return invBG.getRefillOnDay(day);  }
  public void removeRefillProjection(int day) { invBG.removeRefillProjection(day);  }
  public Scalar getProjected(int day) { return invBG.getProjected(day);  }
  public Scalar getProjectedRefill(int day) { return invBG.getProjectedRefill(day);  }
  public Date lastDemandTaskEnd(Inventory inventory) { return invBG.lastDemandTaskEnd(inventory);  }
  public Integer getFirstOverflow(int day, MessageAddress cluster) { return invBG.getFirstOverflow(day, cluster);  }
  public DueOut getLowestPriorityDueOutBeforeDay(int end) { return invBG.getLowestPriorityDueOutBeforeDay(end);  }
  public List updateDueOutAllocations() { return invBG.updateDueOutAllocations();  }
  public int updateContentSchedule(Inventory inventory) { return invBG.updateContentSchedule(inventory);  }
  public int updateDetailedContentSchedule(Inventory inventory) { return invBG.updateDetailedContentSchedule(inventory);  }
  public int clearContentSchedule(Inventory inventory) { return invBG.clearContentSchedule(inventory);  }
  public int updateInventoryLevelsSchedule(Inventory inventory) { return invBG.updateInventoryLevelsSchedule(inventory);  }
  public int clearInventoryLevelsSchedule(Inventory inventory) { return invBG.clearInventoryLevelsSchedule(inventory);  }
  public int printQuantityScheduleTimes(Schedule sched) { return invBG.printQuantityScheduleTimes(sched);  }
  public int printInventoryLevels(Inventory inventory, MessageAddress clusterID) { return invBG.printInventoryLevels(inventory, clusterID);  }
  public PGDelegate copy(PropertyGroup pg) { return invBG.copy(pg);  }
  public Enumeration getAllDueIns() { return invBG.getAllDueIns();  }
  public ProjectionWeight getProjectionWeight() { return invBG.getProjectionWeight();  }
  public void setProjectionWeight(ProjectionWeight newWeight) { invBG.setProjectionWeight(newWeight);  }
  public void addInventoryReport(InventoryReport anInventoryReport) { invBG.addInventoryReport(anInventoryReport);  }
  public InventoryReport getLatestInventoryReport() { return invBG.getLatestInventoryReport();  }
  public InventoryReport getOldestInventoryReport() { return invBG.getOldestInventoryReport();  }
  public void pruneOldInventoryReports(long pruneTime) { invBG.pruneOldInventoryReports(pruneTime);  }
  public long convertDayToTime(int day) { return invBG.convertDayToTime(day);  }
  public long getStartOfDay(int day) { return invBG.getStartOfDay(day);  }
  public int convertTimeToDay(long time) { return invBG.convertTimeToDay(time);  }
  public int getImputedDayOfTime(long time) { return invBG.getImputedDayOfTime(time);  }
  public int getImputedDay(int day) { return invBG.getImputedDay(day);  }
  public void setDueOutFilled(DueOut dueOut, boolean newFilled) { invBG.setDueOutFilled(dueOut, newFilled);  }

  public InventoryPGImpl(InventoryPG original) {
    theCapacity = original.getCapacity();
    theInitialLevel = original.getInitialLevel();
    theReorderLevel = original.getReorderLevel();
    theMinReorder = original.getMinReorder();
    theUnobtainable = original.getUnobtainable();
    theFillToCapacity = original.getFillToCapacity();
    theMaintainAtCapacity = original.getMaintainAtCapacity();
    theResource = original.getResource();
  }

  public boolean equals(Object other) {

    if (!(other instanceof InventoryPG)) {
      return false;
    }

    InventoryPG otherInventoryPG = (InventoryPG) other;

    if (getCapacity() == null) {
      if (otherInventoryPG.getCapacity() != null) {
        return false;
      }
    } else if (!(getCapacity().equals(otherInventoryPG.getCapacity()))) {
      return false;
    }

    if (getInitialLevel() == null) {
      if (otherInventoryPG.getInitialLevel() != null) {
        return false;
      }
    } else if (!(getInitialLevel().equals(otherInventoryPG.getInitialLevel()))) {
      return false;
    }

    if (getReorderLevel() == null) {
      if (otherInventoryPG.getReorderLevel() != null) {
        return false;
      }
    } else if (!(getReorderLevel().equals(otherInventoryPG.getReorderLevel()))) {
      return false;
    }

    if (getMinReorder() == null) {
      if (otherInventoryPG.getMinReorder() != null) {
        return false;
      }
    } else if (!(getMinReorder().equals(otherInventoryPG.getMinReorder()))) {
      return false;
    }

    if (getUnobtainable() == null) {
      if (otherInventoryPG.getUnobtainable() != null) {
        return false;
      }
    } else if (!(getUnobtainable().equals(otherInventoryPG.getUnobtainable()))) {
      return false;
    }

    if (!(getFillToCapacity() == otherInventoryPG.getFillToCapacity())) {
      return false;
    }

    if (!(getMaintainAtCapacity() == otherInventoryPG.getMaintainAtCapacity())) {
      return false;
    }

    if (getResource() == null) {
      if (otherInventoryPG.getResource() != null) {
        return false;
      }
    } else if (!(getResource().equals(otherInventoryPG.getResource()))) {
      return false;
    }

    if (other instanceof InventoryPGImpl) {
      if (getInvBG() == null) {
        if (((InventoryPGImpl) otherInventoryPG).getInvBG() != null) {
          return false;
        }
      } else if (!(getInvBG().equals(((InventoryPGImpl) otherInventoryPG).getInvBG()))) {
        return false;
      }

    }
    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends InventoryPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(InventoryPG original) {
    super(original);
   }
   public Object clone() { return new DQ(this); }
   private transient org.cougaar.planning.ldm.dq.DataQuality _dq = null;
   public boolean hasDataQuality() { return (_dq!=null); }
   public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return _dq; }
   public void setDataQuality(org.cougaar.planning.ldm.dq.DataQuality dq) { _dq=dq; }
   private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    if (out instanceof org.cougaar.core.persist.PersistenceOutputStream) out.writeObject(_dq);
   }
   private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    if (in instanceof org.cougaar.core.persist.PersistenceInputStream) _dq=(org.cougaar.planning.ldm.dq.DataQuality)in.readObject();
   }
    
    private final static PropertyDescriptor properties[]=new PropertyDescriptor[1];
    static {
      try {
        properties[0]= new PropertyDescriptor("dataQuality", DQ.class, "getDataQuality", null);
      } catch (Exception e) { e.printStackTrace(); }
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
      PropertyDescriptor[] pds = super.properties;
      PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+properties.length];
      System.arraycopy(pds, 0, ps, 0, pds.length);
      System.arraycopy(properties, 0, ps, pds.length, properties.length);
      return ps;
    }
  }


  private transient InventoryPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    InventoryPGImpl _tmp = new InventoryPGImpl(this);
    if (invBG != null) {
      _tmp.invBG = (InventoryBG) invBG.copy(_tmp);
    }
    return _tmp;
  }

  public PropertyGroup copy() {
    try {
      return (PropertyGroup) clone();
    } catch (CloneNotSupportedException cnse) { return null;}
  }

  public Class getPrimaryClass() {
    return primaryClass;
  }
  public String getAssetGetMethod() {
    return assetGetter;
  }
  public String getAssetSetMethod() {
    return assetSetter;
  }

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[8];
  static {
    try {
      properties[0]= new PropertyDescriptor("capacity", InventoryPG.class, "getCapacity", null);
      properties[1]= new PropertyDescriptor("initialLevel", InventoryPG.class, "getInitialLevel", null);
      properties[2]= new PropertyDescriptor("reorderLevel", InventoryPG.class, "getReorderLevel", null);
      properties[3]= new PropertyDescriptor("minReorder", InventoryPG.class, "getMinReorder", null);
      properties[4]= new PropertyDescriptor("unobtainable", InventoryPG.class, "getUnobtainable", null);
      properties[5]= new PropertyDescriptor("fillToCapacity", InventoryPG.class, "getFillToCapacity", null);
      properties[6]= new PropertyDescriptor("maintainAtCapacity", InventoryPG.class, "getMaintainAtCapacity", null);
      properties[7]= new PropertyDescriptor("resource", InventoryPG.class, "getResource", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(InventoryPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements InventoryPG, Cloneable, LockedPG
  {
    private transient Object theKey = null;
    _Locked(Object key) { 
      if (this.theKey == null) this.theKey = key;
    }  

    public _Locked() {}

    public PropertyGroup lock() { return this; }
    public PropertyGroup lock(Object o) { return this; }

    public NewPropertyGroup unlock(Object key) throws IllegalAccessException {
       if( theKey.equals(key) ) {
         return InventoryPGImpl.this;
       } else {
         throw new IllegalAccessException("unlock: mismatched internal and provided keys!");
       }
    }

    public PropertyGroup copy() {
      try {
        return (PropertyGroup) clone();
      } catch (CloneNotSupportedException cnse) { return null;}
    }


    public Object clone() throws CloneNotSupportedException {
      InventoryPGImpl _tmp = new InventoryPGImpl(this);
      if (invBG != null) {
        _tmp.invBG = (InventoryBG) invBG.copy(_tmp);
      }
      return _tmp;
    }

    public boolean equals(Object object) { return InventoryPGImpl.this.equals(object); }
    public Scalar getCapacity() { return InventoryPGImpl.this.getCapacity(); }
    public Scalar getInitialLevel() { return InventoryPGImpl.this.getInitialLevel(); }
    public Scalar getReorderLevel() { return InventoryPGImpl.this.getReorderLevel(); }
    public Scalar getMinReorder() { return InventoryPGImpl.this.getMinReorder(); }
    public Scalar getUnobtainable() { return InventoryPGImpl.this.getUnobtainable(); }
    public boolean getFillToCapacity() { return InventoryPGImpl.this.getFillToCapacity(); }
    public boolean getMaintainAtCapacity() { return InventoryPGImpl.this.getMaintainAtCapacity(); }
    public Asset getResource() { return InventoryPGImpl.this.getResource(); }
  public int resetInventory(Inventory inventory, long today) {
    return InventoryPGImpl.this.resetInventory(inventory, today);
  }
  public int getToday() {
    return InventoryPGImpl.this.getToday();
  }
  public int getFirstPlanningDay() {
    return InventoryPGImpl.this.getFirstPlanningDay();
  }
  public Scalar getLevel(long day) {
    return InventoryPGImpl.this.getLevel(day);
  }
  public Scalar getLevel(int day) {
    return InventoryPGImpl.this.getLevel(day);
  }
  public int withdrawFromInventory(Inventory inventory, MessageAddress cluster) {
    return InventoryPGImpl.this.withdrawFromInventory(inventory, cluster);
  }
  public double getNDaysDemand(int day) {
    return InventoryPGImpl.this.getNDaysDemand(day);
  }
  public double getReorderLevel(int day) {
    return InventoryPGImpl.this.getReorderLevel(day);
  }
  public double getGoalLevel(int day) {
    return InventoryPGImpl.this.getGoalLevel(day);
  }
  public int addPreviousRefillsToInventory(Task maintainInv) {
    return InventoryPGImpl.this.addPreviousRefillsToInventory(maintainInv);
  }
  public int addDueIn(Task refillTask) {
    return InventoryPGImpl.this.addDueIn(refillTask);
  }
  public int removeDueIn(Task refillTask) {
    return InventoryPGImpl.this.removeDueIn(refillTask);
  }
  public int getPlanningDays() {
    return InventoryPGImpl.this.getPlanningDays();
  }
  public void computeThresholdSchedule(int daysOfDemand, int daysForward, int daysBackward, double minReorderLevel, double maxReorderLevel, double goalLevelMultiplier) {
    InventoryPGImpl.this.computeThresholdSchedule(daysOfDemand, daysForward, daysBackward, minReorderLevel, maxReorderLevel, goalLevelMultiplier);
  }
  public int determineInventoryLevels() {
    return InventoryPGImpl.this.determineInventoryLevels();
  }
  public Task refillAlreadyFailedOnDay(int day) {
    return InventoryPGImpl.this.refillAlreadyFailedOnDay(day);
  }
  public Task getRefillOnDay(int day) {
    return InventoryPGImpl.this.getRefillOnDay(day);
  }
  public void removeRefillProjection(int day) {
    InventoryPGImpl.this.removeRefillProjection(day);
  }
  public Scalar getProjected(int day) {
    return InventoryPGImpl.this.getProjected(day);
  }
  public Scalar getProjectedRefill(int day) {
    return InventoryPGImpl.this.getProjectedRefill(day);
  }
  public Date lastDemandTaskEnd(Inventory inventory) {
    return InventoryPGImpl.this.lastDemandTaskEnd(inventory);
  }
  public Integer getFirstOverflow(int day, MessageAddress cluster) {
    return InventoryPGImpl.this.getFirstOverflow(day, cluster);
  }
  public DueOut getLowestPriorityDueOutBeforeDay(int end) {
    return InventoryPGImpl.this.getLowestPriorityDueOutBeforeDay(end);
  }
  public List updateDueOutAllocations() {
    return InventoryPGImpl.this.updateDueOutAllocations();
  }
  public int updateContentSchedule(Inventory inventory) {
    return InventoryPGImpl.this.updateContentSchedule(inventory);
  }
  public int updateDetailedContentSchedule(Inventory inventory) {
    return InventoryPGImpl.this.updateDetailedContentSchedule(inventory);
  }
  public int clearContentSchedule(Inventory inventory) {
    return InventoryPGImpl.this.clearContentSchedule(inventory);
  }
  public int updateInventoryLevelsSchedule(Inventory inventory) {
    return InventoryPGImpl.this.updateInventoryLevelsSchedule(inventory);
  }
  public int clearInventoryLevelsSchedule(Inventory inventory) {
    return InventoryPGImpl.this.clearInventoryLevelsSchedule(inventory);
  }
  public int printQuantityScheduleTimes(Schedule sched) {
    return InventoryPGImpl.this.printQuantityScheduleTimes(sched);
  }
  public int printInventoryLevels(Inventory inventory, MessageAddress clusterID) {
    return InventoryPGImpl.this.printInventoryLevels(inventory, clusterID);
  }
  public PGDelegate copy(PropertyGroup pg) {
    return InventoryPGImpl.this.copy(pg);
  }
  public Enumeration getAllDueIns() {
    return InventoryPGImpl.this.getAllDueIns();
  }
  public ProjectionWeight getProjectionWeight() {
    return InventoryPGImpl.this.getProjectionWeight();
  }
  public void setProjectionWeight(ProjectionWeight newWeight) {
    InventoryPGImpl.this.setProjectionWeight(newWeight);
  }
  public void addInventoryReport(InventoryReport anInventoryReport) {
    InventoryPGImpl.this.addInventoryReport(anInventoryReport);
  }
  public InventoryReport getLatestInventoryReport() {
    return InventoryPGImpl.this.getLatestInventoryReport();
  }
  public InventoryReport getOldestInventoryReport() {
    return InventoryPGImpl.this.getOldestInventoryReport();
  }
  public void pruneOldInventoryReports(long pruneTime) {
    InventoryPGImpl.this.pruneOldInventoryReports(pruneTime);
  }
  public long convertDayToTime(int day) {
    return InventoryPGImpl.this.convertDayToTime(day);
  }
  public long getStartOfDay(int day) {
    return InventoryPGImpl.this.getStartOfDay(day);
  }
  public int convertTimeToDay(long time) {
    return InventoryPGImpl.this.convertTimeToDay(time);
  }
  public int getImputedDayOfTime(long time) {
    return InventoryPGImpl.this.getImputedDayOfTime(time);
  }
  public int getImputedDay(int day) {
    return InventoryPGImpl.this.getImputedDay(day);
  }
  public void setDueOutFilled(DueOut dueOut, boolean newFilled) {
    InventoryPGImpl.this.setDueOutFilled(dueOut, newFilled);
  }
  public final boolean hasDataQuality() { return InventoryPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return InventoryPGImpl.this.getDataQuality(); }
    public Class getPrimaryClass() {
      return primaryClass;
    }
    public String getAssetGetMethod() {
      return assetGetter;
    }
    public String getAssetSetMethod() {
      return assetSetter;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
      return properties;
    }

    public Class getIntrospectionClass() {
      return InventoryPGImpl.class;
    }

  }

}
