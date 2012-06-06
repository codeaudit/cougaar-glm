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
/** Primary client interface for InventoryPG.
 *  @see NewInventoryPG
 *  @see InventoryPGImpl
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


public interface InventoryPG extends PropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  /** maximum amount that can be contained or accommodated. **/
  Scalar getCapacity();
  Scalar getInitialLevel();
  Scalar getReorderLevel();
  Scalar getMinReorder();
  /** Quantity of materiel which cannot be used. E.g. fuel which cannot be pumped out of a tank. **/
  Scalar getUnobtainable();
  boolean getFillToCapacity();
  boolean getMaintainAtCapacity();
  Asset getResource();

  int resetInventory(Inventory inventory, long today);
  int getToday();
  int getFirstPlanningDay();
  Scalar getLevel(long day);
  Scalar getLevel(int day);
  int withdrawFromInventory(Inventory inventory, MessageAddress cluster);
  double getNDaysDemand(int day);
  double getReorderLevel(int day);
  double getGoalLevel(int day);
  int addPreviousRefillsToInventory(Task maintainInv);
  int addDueIn(Task refillTask);
  int removeDueIn(Task refillTask);
  int getPlanningDays();
  void computeThresholdSchedule(int daysOfDemand, int daysForward, int daysBackward, double minReorderLevel, double maxReorderLevel, double goalLevelMultiplier);
  int determineInventoryLevels();
  Task refillAlreadyFailedOnDay(int day);
  Task getRefillOnDay(int day);
  void removeRefillProjection(int day);
  Scalar getProjected(int day);
  Scalar getProjectedRefill(int day);
  Date lastDemandTaskEnd(Inventory inventory);
  Integer getFirstOverflow(int day, MessageAddress cluster);
  DueOut getLowestPriorityDueOutBeforeDay(int end);
  List updateDueOutAllocations();
  int updateContentSchedule(Inventory inventory);
  int updateDetailedContentSchedule(Inventory inventory);
  int clearContentSchedule(Inventory inventory);
  int updateInventoryLevelsSchedule(Inventory inventory);
  int clearInventoryLevelsSchedule(Inventory inventory);
  int printQuantityScheduleTimes(Schedule sched);
  int printInventoryLevels(Inventory inventory, MessageAddress clusterID);
  PGDelegate copy(PropertyGroup pg);
  Enumeration getAllDueIns();
  ProjectionWeight getProjectionWeight();
  void setProjectionWeight(ProjectionWeight newWeight);
  void addInventoryReport(InventoryReport anInventoryReport);
  InventoryReport getLatestInventoryReport();
  InventoryReport getOldestInventoryReport();
  void pruneOldInventoryReports(long pruneTime);
  long convertDayToTime(int day);
  long getStartOfDay(int day);
  int convertTimeToDay(long time);
  int getImputedDayOfTime(long time);
  int getImputedDay(int day);
  void setDueOutFilled(DueOut dueOut, boolean newFilled);
  // introspection and construction
  /** the method of factoryClass that creates this type **/
  String factoryMethod = "newInventoryPG";
  /** the (mutable) class type returned by factoryMethod **/
  String mutableClass = "org.cougaar.glm.ldm.asset.NewInventoryPG";
  /** the factory class **/
  Class factoryClass = org.cougaar.glm.ldm.asset.PropertyGroupFactory.class;
  /** the (immutable) class type returned by domain factory **/
   Class primaryClass = org.cougaar.glm.ldm.asset.InventoryPG.class;
  String assetSetter = "setInventoryPG";
  String assetGetter = "getInventoryPG";
  /** The Null instance for indicating that the PG definitely has no value **/
  InventoryPG nullPG = new Null_InventoryPG();

/** Null_PG implementation for InventoryPG **/
final class Null_InventoryPG
  implements InventoryPG, Null_PG
{
  public Scalar getCapacity() { throw new UndefinedValueException(); }
  public Scalar getInitialLevel() { throw new UndefinedValueException(); }
  public Scalar getReorderLevel() { throw new UndefinedValueException(); }
  public Scalar getMinReorder() { throw new UndefinedValueException(); }
  public Scalar getUnobtainable() { throw new UndefinedValueException(); }
  public boolean getFillToCapacity() { throw new UndefinedValueException(); }
  public boolean getMaintainAtCapacity() { throw new UndefinedValueException(); }
  public Asset getResource() { throw new UndefinedValueException(); }
  public InventoryBG getInvBG() {
    throw new UndefinedValueException();
  }
  public void setInvBG(InventoryBG _invBG) {
    throw new UndefinedValueException();
  }
  public int resetInventory(Inventory inventory, long today) { throw new UndefinedValueException(); }
  public int getToday() { throw new UndefinedValueException(); }
  public int getFirstPlanningDay() { throw new UndefinedValueException(); }
  public Scalar getLevel(long day) { throw new UndefinedValueException(); }
  public Scalar getLevel(int day) { throw new UndefinedValueException(); }
  public int withdrawFromInventory(Inventory inventory, MessageAddress cluster) { throw new UndefinedValueException(); }
  public double getNDaysDemand(int day) { throw new UndefinedValueException(); }
  public double getReorderLevel(int day) { throw new UndefinedValueException(); }
  public double getGoalLevel(int day) { throw new UndefinedValueException(); }
  public int addPreviousRefillsToInventory(Task maintainInv) { throw new UndefinedValueException(); }
  public int addDueIn(Task refillTask) { throw new UndefinedValueException(); }
  public int removeDueIn(Task refillTask) { throw new UndefinedValueException(); }
  public int getPlanningDays() { throw new UndefinedValueException(); }
  public void computeThresholdSchedule(int daysOfDemand, int daysForward, int daysBackward, double minReorderLevel, double maxReorderLevel, double goalLevelMultiplier) { throw new UndefinedValueException(); }
  public int determineInventoryLevels() { throw new UndefinedValueException(); }
  public Task refillAlreadyFailedOnDay(int day) { throw new UndefinedValueException(); }
  public Task getRefillOnDay(int day) { throw new UndefinedValueException(); }
  public void removeRefillProjection(int day) { throw new UndefinedValueException(); }
  public Scalar getProjected(int day) { throw new UndefinedValueException(); }
  public Scalar getProjectedRefill(int day) { throw new UndefinedValueException(); }
  public Date lastDemandTaskEnd(Inventory inventory) { throw new UndefinedValueException(); }
  public Integer getFirstOverflow(int day, MessageAddress cluster) { throw new UndefinedValueException(); }
  public DueOut getLowestPriorityDueOutBeforeDay(int end) { throw new UndefinedValueException(); }
  public List updateDueOutAllocations() { throw new UndefinedValueException(); }
  public int updateContentSchedule(Inventory inventory) { throw new UndefinedValueException(); }
  public int updateDetailedContentSchedule(Inventory inventory) { throw new UndefinedValueException(); }
  public int clearContentSchedule(Inventory inventory) { throw new UndefinedValueException(); }
  public int updateInventoryLevelsSchedule(Inventory inventory) { throw new UndefinedValueException(); }
  public int clearInventoryLevelsSchedule(Inventory inventory) { throw new UndefinedValueException(); }
  public int printQuantityScheduleTimes(Schedule sched) { throw new UndefinedValueException(); }
  public int printInventoryLevels(Inventory inventory, MessageAddress clusterID) { throw new UndefinedValueException(); }
  public PGDelegate copy(PropertyGroup pg) { throw new UndefinedValueException(); }
  public Enumeration getAllDueIns() { throw new UndefinedValueException(); }
  public ProjectionWeight getProjectionWeight() { throw new UndefinedValueException(); }
  public void setProjectionWeight(ProjectionWeight newWeight) { throw new UndefinedValueException(); }
  public void addInventoryReport(InventoryReport anInventoryReport) { throw new UndefinedValueException(); }
  public InventoryReport getLatestInventoryReport() { throw new UndefinedValueException(); }
  public InventoryReport getOldestInventoryReport() { throw new UndefinedValueException(); }
  public void pruneOldInventoryReports(long pruneTime) { throw new UndefinedValueException(); }
  public long convertDayToTime(int day) { throw new UndefinedValueException(); }
  public long getStartOfDay(int day) { throw new UndefinedValueException(); }
  public int convertTimeToDay(long time) { throw new UndefinedValueException(); }
  public int getImputedDayOfTime(long time) { throw new UndefinedValueException(); }
  public int getImputedDay(int day) { throw new UndefinedValueException(); }
  public void setDueOutFilled(DueOut dueOut, boolean newFilled) { throw new UndefinedValueException(); }
  public boolean equals(Object object) { throw new UndefinedValueException(); }
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
  public NewPropertyGroup unlock(Object key) { return null; }
  public PropertyGroup lock(Object key) { return null; }
  public PropertyGroup lock() { return null; }
  public PropertyGroup copy() { return null; }
  public Class getPrimaryClass(){return primaryClass;}
  public String getAssetGetMethod() {return assetGetter;}
  public String getAssetSetMethod() {return assetSetter;}
  public Class getIntrospectionClass() {
    return InventoryPGImpl.class;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }
}

/** Future PG implementation for InventoryPG **/
final class Future
  implements InventoryPG, Future_PG
{
  public Scalar getCapacity() {
    waitForFinalize();
    return _real.getCapacity();
  }
  public Scalar getInitialLevel() {
    waitForFinalize();
    return _real.getInitialLevel();
  }
  public Scalar getReorderLevel() {
    waitForFinalize();
    return _real.getReorderLevel();
  }
  public Scalar getMinReorder() {
    waitForFinalize();
    return _real.getMinReorder();
  }
  public Scalar getUnobtainable() {
    waitForFinalize();
    return _real.getUnobtainable();
  }
  public boolean getFillToCapacity() {
    waitForFinalize();
    return _real.getFillToCapacity();
  }
  public boolean getMaintainAtCapacity() {
    waitForFinalize();
    return _real.getMaintainAtCapacity();
  }
  public Asset getResource() {
    waitForFinalize();
    return _real.getResource();
  }
  public boolean equals(Object object) {
    waitForFinalize();
    return _real.equals(object);
  }
  public int resetInventory(Inventory inventory, long today) {
    waitForFinalize();
    return _real.resetInventory(inventory, today);
  }
  public int getToday() {
    waitForFinalize();
    return _real.getToday();
  }
  public int getFirstPlanningDay() {
    waitForFinalize();
    return _real.getFirstPlanningDay();
  }
  public Scalar getLevel(long day) {
    waitForFinalize();
    return _real.getLevel(day);
  }
  public Scalar getLevel(int day) {
    waitForFinalize();
    return _real.getLevel(day);
  }
  public int withdrawFromInventory(Inventory inventory, MessageAddress cluster) {
    waitForFinalize();
    return _real.withdrawFromInventory(inventory, cluster);
  }
  public double getNDaysDemand(int day) {
    waitForFinalize();
    return _real.getNDaysDemand(day);
  }
  public double getReorderLevel(int day) {
    waitForFinalize();
    return _real.getReorderLevel(day);
  }
  public double getGoalLevel(int day) {
    waitForFinalize();
    return _real.getGoalLevel(day);
  }
  public int addPreviousRefillsToInventory(Task maintainInv) {
    waitForFinalize();
    return _real.addPreviousRefillsToInventory(maintainInv);
  }
  public int addDueIn(Task refillTask) {
    waitForFinalize();
    return _real.addDueIn(refillTask);
  }
  public int removeDueIn(Task refillTask) {
    waitForFinalize();
    return _real.removeDueIn(refillTask);
  }
  public int getPlanningDays() {
    waitForFinalize();
    return _real.getPlanningDays();
  }
  public void computeThresholdSchedule(int daysOfDemand, int daysForward, int daysBackward, double minReorderLevel, double maxReorderLevel, double goalLevelMultiplier) {
    waitForFinalize();
    _real.computeThresholdSchedule(daysOfDemand, daysForward, daysBackward, minReorderLevel, maxReorderLevel, goalLevelMultiplier);
  }
  public int determineInventoryLevels() {
    waitForFinalize();
    return _real.determineInventoryLevels();
  }
  public Task refillAlreadyFailedOnDay(int day) {
    waitForFinalize();
    return _real.refillAlreadyFailedOnDay(day);
  }
  public Task getRefillOnDay(int day) {
    waitForFinalize();
    return _real.getRefillOnDay(day);
  }
  public void removeRefillProjection(int day) {
    waitForFinalize();
    _real.removeRefillProjection(day);
  }
  public Scalar getProjected(int day) {
    waitForFinalize();
    return _real.getProjected(day);
  }
  public Scalar getProjectedRefill(int day) {
    waitForFinalize();
    return _real.getProjectedRefill(day);
  }
  public Date lastDemandTaskEnd(Inventory inventory) {
    waitForFinalize();
    return _real.lastDemandTaskEnd(inventory);
  }
  public Integer getFirstOverflow(int day, MessageAddress cluster) {
    waitForFinalize();
    return _real.getFirstOverflow(day, cluster);
  }
  public DueOut getLowestPriorityDueOutBeforeDay(int end) {
    waitForFinalize();
    return _real.getLowestPriorityDueOutBeforeDay(end);
  }
  public List updateDueOutAllocations() {
    waitForFinalize();
    return _real.updateDueOutAllocations();
  }
  public int updateContentSchedule(Inventory inventory) {
    waitForFinalize();
    return _real.updateContentSchedule(inventory);
  }
  public int updateDetailedContentSchedule(Inventory inventory) {
    waitForFinalize();
    return _real.updateDetailedContentSchedule(inventory);
  }
  public int clearContentSchedule(Inventory inventory) {
    waitForFinalize();
    return _real.clearContentSchedule(inventory);
  }
  public int updateInventoryLevelsSchedule(Inventory inventory) {
    waitForFinalize();
    return _real.updateInventoryLevelsSchedule(inventory);
  }
  public int clearInventoryLevelsSchedule(Inventory inventory) {
    waitForFinalize();
    return _real.clearInventoryLevelsSchedule(inventory);
  }
  public int printQuantityScheduleTimes(Schedule sched) {
    waitForFinalize();
    return _real.printQuantityScheduleTimes(sched);
  }
  public int printInventoryLevels(Inventory inventory, MessageAddress clusterID) {
    waitForFinalize();
    return _real.printInventoryLevels(inventory, clusterID);
  }
  public PGDelegate copy(PropertyGroup pg) {
    waitForFinalize();
    return _real.copy(pg);
  }
  public Enumeration getAllDueIns() {
    waitForFinalize();
    return _real.getAllDueIns();
  }
  public ProjectionWeight getProjectionWeight() {
    waitForFinalize();
    return _real.getProjectionWeight();
  }
  public void setProjectionWeight(ProjectionWeight newWeight) {
    waitForFinalize();
    _real.setProjectionWeight(newWeight);
  }
  public void addInventoryReport(InventoryReport anInventoryReport) {
    waitForFinalize();
    _real.addInventoryReport(anInventoryReport);
  }
  public InventoryReport getLatestInventoryReport() {
    waitForFinalize();
    return _real.getLatestInventoryReport();
  }
  public InventoryReport getOldestInventoryReport() {
    waitForFinalize();
    return _real.getOldestInventoryReport();
  }
  public void pruneOldInventoryReports(long pruneTime) {
    waitForFinalize();
    _real.pruneOldInventoryReports(pruneTime);
  }
  public long convertDayToTime(int day) {
    waitForFinalize();
    return _real.convertDayToTime(day);
  }
  public long getStartOfDay(int day) {
    waitForFinalize();
    return _real.getStartOfDay(day);
  }
  public int convertTimeToDay(long time) {
    waitForFinalize();
    return _real.convertTimeToDay(time);
  }
  public int getImputedDayOfTime(long time) {
    waitForFinalize();
    return _real.getImputedDayOfTime(time);
  }
  public int getImputedDay(int day) {
    waitForFinalize();
    return _real.getImputedDay(day);
  }
  public void setDueOutFilled(DueOut dueOut, boolean newFilled) {
    waitForFinalize();
    _real.setDueOutFilled(dueOut, newFilled);
  }
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }
  public NewPropertyGroup unlock(Object key) { return null; }
  public PropertyGroup lock(Object key) { return null; }
  public PropertyGroup lock() { return null; }
  public PropertyGroup copy() { return null; }
  public Class getPrimaryClass(){return primaryClass;}
  public String getAssetGetMethod() {return assetGetter;}
  public String getAssetSetMethod() {return assetSetter;}
  public Class getIntrospectionClass() {
    return InventoryPGImpl.class;
  }
  public synchronized boolean hasDataQuality() {
    return (_real!=null) && _real.hasDataQuality();
  }
  public synchronized org.cougaar.planning.ldm.dq.DataQuality getDataQuality() {
    return (_real==null)?null:(_real.getDataQuality());
  }

  // Finalization support
  private InventoryPG _real = null;
  public synchronized void finalize(PropertyGroup real) {
    if (real instanceof InventoryPG) {
      _real=(InventoryPG) real;
      notifyAll();
    } else {
      throw new IllegalArgumentException("Finalization with wrong class: "+real);
    }
  }
  private synchronized void waitForFinalize() {
    while (_real == null) {
      try {
        wait();
      } catch (InterruptedException _ie) {
        // We should really let waitForFinalize throw InterruptedException
        Thread.interrupted();
      }
    }
  }
}
}
