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
/** Abstract Asset Skeleton implementation
 * Implements default property getters, and additional property
 * lists.
 * Intended to be extended by org.cougaar.planning.ldm.asset.Asset
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

import java.io.Serializable;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;

public abstract class AssetSkeleton extends org.cougaar.planning.ldm.asset.Asset {

  protected AssetSkeleton() {}

  protected AssetSkeleton(AssetSkeleton prototype) {
    super(prototype);
  }

  /**                 Default PG accessors               **/

  /** Search additional properties for a CargoFacilityPG instance.
   * @return instance of CargoFacilityPG or null.
   **/
  public CargoFacilityPG getCargoFacilityPG()
  {
    CargoFacilityPG _tmp = (CargoFacilityPG) resolvePG(CargoFacilityPG.class);
    return (_tmp==CargoFacilityPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a CargoFacilityPG
   **/
  public boolean hasCargoFacilityPG() {
    return (getCargoFacilityPG() != null);
  }

  /** Set the CargoFacilityPG property.
   * The default implementation will create a new CargoFacilityPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setCargoFacilityPG(PropertyGroup aCargoFacilityPG) {
    if (aCargoFacilityPG == null) {
      removeOtherPropertyGroup(CargoFacilityPG.class);
    } else {
      addOtherPropertyGroup(aCargoFacilityPG);
    }
  }

  /** Search additional properties for a SeaLinkPG instance.
   * @return instance of SeaLinkPG or null.
   **/
  public SeaLinkPG getSeaLinkPG()
  {
    SeaLinkPG _tmp = (SeaLinkPG) resolvePG(SeaLinkPG.class);
    return (_tmp==SeaLinkPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a SeaLinkPG
   **/
  public boolean hasSeaLinkPG() {
    return (getSeaLinkPG() != null);
  }

  /** Set the SeaLinkPG property.
   * The default implementation will create a new SeaLinkPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setSeaLinkPG(PropertyGroup aSeaLinkPG) {
    if (aSeaLinkPG == null) {
      removeOtherPropertyGroup(SeaLinkPG.class);
    } else {
      addOtherPropertyGroup(aSeaLinkPG);
    }
  }

  /** Search additional properties for a AirTransportationPG instance.
   * @return instance of AirTransportationPG or null.
   **/
  public AirTransportationPG getAirTransportationPG()
  {
    AirTransportationPG _tmp = (AirTransportationPG) resolvePG(AirTransportationPG.class);
    return (_tmp==AirTransportationPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a AirTransportationPG
   **/
  public boolean hasAirTransportationPG() {
    return (getAirTransportationPG() != null);
  }

  /** Set the AirTransportationPG property.
   * The default implementation will create a new AirTransportationPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setAirTransportationPG(PropertyGroup aAirTransportationPG) {
    if (aAirTransportationPG == null) {
      removeOtherPropertyGroup(AirTransportationPG.class);
    } else {
      addOtherPropertyGroup(aAirTransportationPG);
    }
  }

  /** Search additional properties for a SupplyDepotPG instance.
   * @return instance of SupplyDepotPG or null.
   **/
  public SupplyDepotPG getSupplyDepotPG()
  {
    SupplyDepotPG _tmp = (SupplyDepotPG) resolvePG(SupplyDepotPG.class);
    return (_tmp==SupplyDepotPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a SupplyDepotPG
   **/
  public boolean hasSupplyDepotPG() {
    return (getSupplyDepotPG() != null);
  }

  /** Set the SupplyDepotPG property.
   * The default implementation will create a new SupplyDepotPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setSupplyDepotPG(PropertyGroup aSupplyDepotPG) {
    if (aSupplyDepotPG == null) {
      removeOtherPropertyGroup(SupplyDepotPG.class);
    } else {
      addOtherPropertyGroup(aSupplyDepotPG);
    }
  }

  /** Search additional properties for a TruckTerminalPG instance.
   * @return instance of TruckTerminalPG or null.
   **/
  public TruckTerminalPG getTruckTerminalPG()
  {
    TruckTerminalPG _tmp = (TruckTerminalPG) resolvePG(TruckTerminalPG.class);
    return (_tmp==TruckTerminalPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a TruckTerminalPG
   **/
  public boolean hasTruckTerminalPG() {
    return (getTruckTerminalPG() != null);
  }

  /** Set the TruckTerminalPG property.
   * The default implementation will create a new TruckTerminalPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setTruckTerminalPG(PropertyGroup aTruckTerminalPG) {
    if (aTruckTerminalPG == null) {
      removeOtherPropertyGroup(TruckTerminalPG.class);
    } else {
      addOtherPropertyGroup(aTruckTerminalPG);
    }
  }

  /** Search additional properties for a MilitaryOrgPG instance.
   * @return instance of MilitaryOrgPG or null.
   **/
  public MilitaryOrgPG getMilitaryOrgPG()
  {
    MilitaryOrgPG _tmp = (MilitaryOrgPG) resolvePG(MilitaryOrgPG.class);
    return (_tmp==MilitaryOrgPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a MilitaryOrgPG
   **/
  public boolean hasMilitaryOrgPG() {
    return (getMilitaryOrgPG() != null);
  }

  /** Set the MilitaryOrgPG property.
   * The default implementation will create a new MilitaryOrgPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setMilitaryOrgPG(PropertyGroup aMilitaryOrgPG) {
    if (aMilitaryOrgPG == null) {
      removeOtherPropertyGroup(MilitaryOrgPG.class);
    } else {
      addOtherPropertyGroup(aMilitaryOrgPG);
    }
  }

  /** Search additional properties for a RailTerminalPG instance.
   * @return instance of RailTerminalPG or null.
   **/
  public RailTerminalPG getRailTerminalPG()
  {
    RailTerminalPG _tmp = (RailTerminalPG) resolvePG(RailTerminalPG.class);
    return (_tmp==RailTerminalPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a RailTerminalPG
   **/
  public boolean hasRailTerminalPG() {
    return (getRailTerminalPG() != null);
  }

  /** Set the RailTerminalPG property.
   * The default implementation will create a new RailTerminalPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setRailTerminalPG(PropertyGroup aRailTerminalPG) {
    if (aRailTerminalPG == null) {
      removeOtherPropertyGroup(RailTerminalPG.class);
    } else {
      addOtherPropertyGroup(aRailTerminalPG);
    }
  }

  /** Search additional properties for a FuelPG instance.
   * @return instance of FuelPG or null.
   **/
  public FuelPG getFuelPG()
  {
    FuelPG _tmp = (FuelPG) resolvePG(FuelPG.class);
    return (_tmp==FuelPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a FuelPG
   **/
  public boolean hasFuelPG() {
    return (getFuelPG() != null);
  }

  /** Set the FuelPG property.
   * The default implementation will create a new FuelPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setFuelPG(PropertyGroup aFuelPG) {
    if (aFuelPG == null) {
      removeOtherPropertyGroup(FuelPG.class);
    } else {
      addOtherPropertyGroup(aFuelPG);
    }
  }

  /** Search additional properties for a AssignedPG instance.
   * @return instance of AssignedPG or null.
   **/
  public AssignedPG getAssignedPG()
  {
    AssignedPG _tmp = (AssignedPG) resolvePG(AssignedPG.class);
    return (_tmp==AssignedPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a AssignedPG
   **/
  public boolean hasAssignedPG() {
    return (getAssignedPG() != null);
  }

  /** Set the AssignedPG property.
   * The default implementation will create a new AssignedPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setAssignedPG(PropertyGroup aAssignedPG) {
    if (aAssignedPG == null) {
      removeOtherPropertyGroup(AssignedPG.class);
    } else {
      addOtherPropertyGroup(aAssignedPG);
    }
  }

  /** Search additional properties for a FuelSupplyPG instance.
   * @return instance of FuelSupplyPG or null.
   **/
  public FuelSupplyPG getFuelSupplyPG()
  {
    FuelSupplyPG _tmp = (FuelSupplyPG) resolvePG(FuelSupplyPG.class);
    return (_tmp==FuelSupplyPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a FuelSupplyPG
   **/
  public boolean hasFuelSupplyPG() {
    return (getFuelSupplyPG() != null);
  }

  /** Set the FuelSupplyPG property.
   * The default implementation will create a new FuelSupplyPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setFuelSupplyPG(PropertyGroup aFuelSupplyPG) {
    if (aFuelSupplyPG == null) {
      removeOtherPropertyGroup(FuelSupplyPG.class);
    } else {
      addOtherPropertyGroup(aFuelSupplyPG);
    }
  }

  /** Search additional properties for a ForUnitPG instance.
   * @return instance of ForUnitPG or null.
   **/
  public ForUnitPG getForUnitPG()
  {
    ForUnitPG _tmp = (ForUnitPG) resolvePG(ForUnitPG.class);
    return (_tmp==ForUnitPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a ForUnitPG
   **/
  public boolean hasForUnitPG() {
    return (getForUnitPG() != null);
  }

  /** Set the ForUnitPG property.
   * The default implementation will create a new ForUnitPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setForUnitPG(PropertyGroup aForUnitPG) {
    if (aForUnitPG == null) {
      removeOtherPropertyGroup(ForUnitPG.class);
    } else {
      addOtherPropertyGroup(aForUnitPG);
    }
  }

  /** Search additional properties for a FromBasePG instance.
   * @return instance of FromBasePG or null.
   **/
  public FromBasePG getFromBasePG()
  {
    FromBasePG _tmp = (FromBasePG) resolvePG(FromBasePG.class);
    return (_tmp==FromBasePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a FromBasePG
   **/
  public boolean hasFromBasePG() {
    return (getFromBasePG() != null);
  }

  /** Set the FromBasePG property.
   * The default implementation will create a new FromBasePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setFromBasePG(PropertyGroup aFromBasePG) {
    if (aFromBasePG == null) {
      removeOtherPropertyGroup(FromBasePG.class);
    } else {
      addOtherPropertyGroup(aFromBasePG);
    }
  }

  /** Search additional properties for a TransportationPG instance.
   * @return instance of TransportationPG or null.
   **/
  public TransportationPG getTransportationPG()
  {
    TransportationPG _tmp = (TransportationPG) resolvePG(TransportationPG.class);
    return (_tmp==TransportationPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a TransportationPG
   **/
  public boolean hasTransportationPG() {
    return (getTransportationPG() != null);
  }

  /** Set the TransportationPG property.
   * The default implementation will create a new TransportationPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setTransportationPG(PropertyGroup aTransportationPG) {
    if (aTransportationPG == null) {
      removeOtherPropertyGroup(TransportationPG.class);
    } else {
      addOtherPropertyGroup(aTransportationPG);
    }
  }

  /** Search additional properties for a CostPG instance.
   * @return instance of CostPG or null.
   **/
  public CostPG getCostPG()
  {
    CostPG _tmp = (CostPG) resolvePG(CostPG.class);
    return (_tmp==CostPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a CostPG
   **/
  public boolean hasCostPG() {
    return (getCostPG() != null);
  }

  /** Set the CostPG property.
   * The default implementation will create a new CostPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setCostPG(PropertyGroup aCostPG) {
    if (aCostPG == null) {
      removeOtherPropertyGroup(CostPG.class);
    } else {
      addOtherPropertyGroup(aCostPG);
    }
  }

  /** Search additional properties for a WaterPG instance.
   * @return instance of WaterPG or null.
   **/
  public WaterPG getWaterPG()
  {
    WaterPG _tmp = (WaterPG) resolvePG(WaterPG.class);
    return (_tmp==WaterPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a WaterPG
   **/
  public boolean hasWaterPG() {
    return (getWaterPG() != null);
  }

  /** Set the WaterPG property.
   * The default implementation will create a new WaterPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setWaterPG(PropertyGroup aWaterPG) {
    if (aWaterPG == null) {
      removeOtherPropertyGroup(WaterPG.class);
    } else {
      addOtherPropertyGroup(aWaterPG);
    }
  }

  /** Search additional properties for a AirSelfPropulsionPG instance.
   * @return instance of AirSelfPropulsionPG or null.
   **/
  public AirSelfPropulsionPG getAirSelfPropulsionPG()
  {
    AirSelfPropulsionPG _tmp = (AirSelfPropulsionPG) resolvePG(AirSelfPropulsionPG.class);
    return (_tmp==AirSelfPropulsionPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a AirSelfPropulsionPG
   **/
  public boolean hasAirSelfPropulsionPG() {
    return (getAirSelfPropulsionPG() != null);
  }

  /** Set the AirSelfPropulsionPG property.
   * The default implementation will create a new AirSelfPropulsionPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setAirSelfPropulsionPG(PropertyGroup aAirSelfPropulsionPG) {
    if (aAirSelfPropulsionPG == null) {
      removeOtherPropertyGroup(AirSelfPropulsionPG.class);
    } else {
      addOtherPropertyGroup(aAirSelfPropulsionPG);
    }
  }

  /** Search additional properties for a MilitaryPersonPG instance.
   * @return instance of MilitaryPersonPG or null.
   **/
  public MilitaryPersonPG getMilitaryPersonPG()
  {
    MilitaryPersonPG _tmp = (MilitaryPersonPG) resolvePG(MilitaryPersonPG.class);
    return (_tmp==MilitaryPersonPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a MilitaryPersonPG
   **/
  public boolean hasMilitaryPersonPG() {
    return (getMilitaryPersonPG() != null);
  }

  /** Set the MilitaryPersonPG property.
   * The default implementation will create a new MilitaryPersonPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setMilitaryPersonPG(PropertyGroup aMilitaryPersonPG) {
    if (aMilitaryPersonPG == null) {
      removeOtherPropertyGroup(MilitaryPersonPG.class);
    } else {
      addOtherPropertyGroup(aMilitaryPersonPG);
    }
  }

  /** Search additional properties for a AirportPG instance.
   * @return instance of AirportPG or null.
   **/
  public AirportPG getAirportPG()
  {
    AirportPG _tmp = (AirportPG) resolvePG(AirportPG.class);
    return (_tmp==AirportPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a AirportPG
   **/
  public boolean hasAirportPG() {
    return (getAirportPG() != null);
  }

  /** Set the AirportPG property.
   * The default implementation will create a new AirportPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setAirportPG(PropertyGroup aAirportPG) {
    if (aAirportPG == null) {
      removeOtherPropertyGroup(AirportPG.class);
    } else {
      addOtherPropertyGroup(aAirportPG);
    }
  }

  /** Search additional properties for a SeaTransportationPG instance.
   * @return instance of SeaTransportationPG or null.
   **/
  public SeaTransportationPG getSeaTransportationPG()
  {
    SeaTransportationPG _tmp = (SeaTransportationPG) resolvePG(SeaTransportationPG.class);
    return (_tmp==SeaTransportationPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a SeaTransportationPG
   **/
  public boolean hasSeaTransportationPG() {
    return (getSeaTransportationPG() != null);
  }

  /** Set the SeaTransportationPG property.
   * The default implementation will create a new SeaTransportationPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setSeaTransportationPG(PropertyGroup aSeaTransportationPG) {
    if (aSeaTransportationPG == null) {
      removeOtherPropertyGroup(SeaTransportationPG.class);
    } else {
      addOtherPropertyGroup(aSeaTransportationPG);
    }
  }

  /** Search additional properties for a WaterSelfPropulsionPG instance.
   * @return instance of WaterSelfPropulsionPG or null.
   **/
  public WaterSelfPropulsionPG getWaterSelfPropulsionPG()
  {
    WaterSelfPropulsionPG _tmp = (WaterSelfPropulsionPG) resolvePG(WaterSelfPropulsionPG.class);
    return (_tmp==WaterSelfPropulsionPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a WaterSelfPropulsionPG
   **/
  public boolean hasWaterSelfPropulsionPG() {
    return (getWaterSelfPropulsionPG() != null);
  }

  /** Set the WaterSelfPropulsionPG property.
   * The default implementation will create a new WaterSelfPropulsionPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setWaterSelfPropulsionPG(PropertyGroup aWaterSelfPropulsionPG) {
    if (aWaterSelfPropulsionPG == null) {
      removeOtherPropertyGroup(WaterSelfPropulsionPG.class);
    } else {
      addOtherPropertyGroup(aWaterSelfPropulsionPG);
    }
  }

  /** Search additional properties for a PersonnelReadinessPG instance.
   * @return instance of PersonnelReadinessPG or null.
   **/
  public PersonnelReadinessPG getPersonnelReadinessPG()
  {
    PersonnelReadinessPG _tmp = (PersonnelReadinessPG) resolvePG(PersonnelReadinessPG.class);
    return (_tmp==PersonnelReadinessPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a PersonnelReadinessPG
   **/
  public boolean hasPersonnelReadinessPG() {
    return (getPersonnelReadinessPG() != null);
  }

  /** Set the PersonnelReadinessPG property.
   * The default implementation will create a new PersonnelReadinessPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setPersonnelReadinessPG(PropertyGroup aPersonnelReadinessPG) {
    if (aPersonnelReadinessPG == null) {
      removeOtherPropertyGroup(PersonnelReadinessPG.class);
    } else {
      addOtherPropertyGroup(aPersonnelReadinessPG);
    }
  }

  /** Search additional properties for a ConditionPG instance.
   * @return instance of ConditionPG or null.
   **/
  public ConditionPG getConditionPG()
  {
    ConditionPG _tmp = (ConditionPG) resolvePG(ConditionPG.class);
    return (_tmp==ConditionPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a ConditionPG
   **/
  public boolean hasConditionPG() {
    return (getConditionPG() != null);
  }

  /** Set the ConditionPG property.
   * The default implementation will create a new ConditionPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setConditionPG(PropertyGroup aConditionPG) {
    if (aConditionPG == null) {
      removeOtherPropertyGroup(ConditionPG.class);
    } else {
      addOtherPropertyGroup(aConditionPG);
    }
  }

  /** Search additional properties for a WaterSupplyPG instance.
   * @return instance of WaterSupplyPG or null.
   **/
  public WaterSupplyPG getWaterSupplyPG()
  {
    WaterSupplyPG _tmp = (WaterSupplyPG) resolvePG(WaterSupplyPG.class);
    return (_tmp==WaterSupplyPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a WaterSupplyPG
   **/
  public boolean hasWaterSupplyPG() {
    return (getWaterSupplyPG() != null);
  }

  /** Set the WaterSupplyPG property.
   * The default implementation will create a new WaterSupplyPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setWaterSupplyPG(PropertyGroup aWaterSupplyPG) {
    if (aWaterSupplyPG == null) {
      removeOtherPropertyGroup(WaterSupplyPG.class);
    } else {
      addOtherPropertyGroup(aWaterSupplyPG);
    }
  }

  /** Search additional properties for a ScheduledContentPG instance.
   * @return instance of ScheduledContentPG or null.
   **/
  public ScheduledContentPG getScheduledContentPG()
  {
    ScheduledContentPG _tmp = (ScheduledContentPG) resolvePG(ScheduledContentPG.class);
    return (_tmp==ScheduledContentPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a ScheduledContentPG
   **/
  public boolean hasScheduledContentPG() {
    return (getScheduledContentPG() != null);
  }

  /** Set the ScheduledContentPG property.
   * The default implementation will create a new ScheduledContentPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setScheduledContentPG(PropertyGroup aScheduledContentPG) {
    if (aScheduledContentPG == null) {
      removeOtherPropertyGroup(ScheduledContentPG.class);
    } else {
      addOtherPropertyGroup(aScheduledContentPG);
    }
  }

  /** Search additional properties for a AirLiftPG instance.
   * @return instance of AirLiftPG or null.
   **/
  public AirLiftPG getAirLiftPG()
  {
    AirLiftPG _tmp = (AirLiftPG) resolvePG(AirLiftPG.class);
    return (_tmp==AirLiftPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a AirLiftPG
   **/
  public boolean hasAirLiftPG() {
    return (getAirLiftPG() != null);
  }

  /** Set the AirLiftPG property.
   * The default implementation will create a new AirLiftPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setAirLiftPG(PropertyGroup aAirLiftPG) {
    if (aAirLiftPG == null) {
      removeOtherPropertyGroup(AirLiftPG.class);
    } else {
      addOtherPropertyGroup(aAirLiftPG);
    }
  }

  /** Search additional properties for a RailTransportationPG instance.
   * @return instance of RailTransportationPG or null.
   **/
  public RailTransportationPG getRailTransportationPG()
  {
    RailTransportationPG _tmp = (RailTransportationPG) resolvePG(RailTransportationPG.class);
    return (_tmp==RailTransportationPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a RailTransportationPG
   **/
  public boolean hasRailTransportationPG() {
    return (getRailTransportationPG() != null);
  }

  /** Set the RailTransportationPG property.
   * The default implementation will create a new RailTransportationPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setRailTransportationPG(PropertyGroup aRailTransportationPG) {
    if (aRailTransportationPG == null) {
      removeOtherPropertyGroup(RailTransportationPG.class);
    } else {
      addOtherPropertyGroup(aRailTransportationPG);
    }
  }

  /** Search additional properties for a SupplyClassPG instance.
   * @return instance of SupplyClassPG or null.
   **/
  public SupplyClassPG getSupplyClassPG()
  {
    SupplyClassPG _tmp = (SupplyClassPG) resolvePG(SupplyClassPG.class);
    return (_tmp==SupplyClassPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a SupplyClassPG
   **/
  public boolean hasSupplyClassPG() {
    return (getSupplyClassPG() != null);
  }

  /** Set the SupplyClassPG property.
   * The default implementation will create a new SupplyClassPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setSupplyClassPG(PropertyGroup aSupplyClassPG) {
    if (aSupplyClassPG == null) {
      removeOtherPropertyGroup(SupplyClassPG.class);
    } else {
      addOtherPropertyGroup(aSupplyClassPG);
    }
  }

  /** Search additional properties for a ContainPG instance.
   * @return instance of ContainPG or null.
   **/
  public ContainPG getContainPG()
  {
    ContainPG _tmp = (ContainPG) resolvePG(ContainPG.class);
    return (_tmp==ContainPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a ContainPG
   **/
  public boolean hasContainPG() {
    return (getContainPG() != null);
  }

  /** Set the ContainPG property.
   * The default implementation will create a new ContainPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setContainPG(PropertyGroup aContainPG) {
    if (aContainPG == null) {
      removeOtherPropertyGroup(ContainPG.class);
    } else {
      addOtherPropertyGroup(aContainPG);
    }
  }

  /** Search additional properties for a OnRoadTransportationPG instance.
   * @return instance of OnRoadTransportationPG or null.
   **/
  public OnRoadTransportationPG getOnRoadTransportationPG()
  {
    OnRoadTransportationPG _tmp = (OnRoadTransportationPG) resolvePG(OnRoadTransportationPG.class);
    return (_tmp==OnRoadTransportationPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a OnRoadTransportationPG
   **/
  public boolean hasOnRoadTransportationPG() {
    return (getOnRoadTransportationPG() != null);
  }

  /** Set the OnRoadTransportationPG property.
   * The default implementation will create a new OnRoadTransportationPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setOnRoadTransportationPG(PropertyGroup aOnRoadTransportationPG) {
    if (aOnRoadTransportationPG == null) {
      removeOtherPropertyGroup(OnRoadTransportationPG.class);
    } else {
      addOtherPropertyGroup(aOnRoadTransportationPG);
    }
  }

  /** Search additional properties for a PositionPG instance.
   * @return instance of PositionPG or null.
   **/
  public PositionPG getPositionPG()
  {
    PositionPG _tmp = (PositionPG) resolvePG(PositionPG.class);
    return (_tmp==PositionPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a PositionPG
   **/
  public boolean hasPositionPG() {
    return (getPositionPG() != null);
  }

  /** Set the PositionPG property.
   * The default implementation will create a new PositionPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setPositionPG(PropertyGroup aPositionPG) {
    if (aPositionPG == null) {
      removeOtherPropertyGroup(PositionPG.class);
    } else {
      addOtherPropertyGroup(aPositionPG);
    }
  }

  /** Search additional properties for a MovabilityPG instance.
   * @return instance of MovabilityPG or null.
   **/
  public MovabilityPG getMovabilityPG()
  {
    MovabilityPG _tmp = (MovabilityPG) resolvePG(MovabilityPG.class);
    return (_tmp==MovabilityPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a MovabilityPG
   **/
  public boolean hasMovabilityPG() {
    return (getMovabilityPG() != null);
  }

  /** Set the MovabilityPG property.
   * The default implementation will create a new MovabilityPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setMovabilityPG(PropertyGroup aMovabilityPG) {
    if (aMovabilityPG == null) {
      removeOtherPropertyGroup(MovabilityPG.class);
    } else {
      addOtherPropertyGroup(aMovabilityPG);
    }
  }

  /** Search additional properties for a GroundVehiclePG instance.
   * @return instance of GroundVehiclePG or null.
   **/
  public GroundVehiclePG getGroundVehiclePG()
  {
    GroundVehiclePG _tmp = (GroundVehiclePG) resolvePG(GroundVehiclePG.class);
    return (_tmp==GroundVehiclePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a GroundVehiclePG
   **/
  public boolean hasGroundVehiclePG() {
    return (getGroundVehiclePG() != null);
  }

  /** Set the GroundVehiclePG property.
   * The default implementation will create a new GroundVehiclePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setGroundVehiclePG(PropertyGroup aGroundVehiclePG) {
    if (aGroundVehiclePG == null) {
      removeOtherPropertyGroup(GroundVehiclePG.class);
    } else {
      addOtherPropertyGroup(aGroundVehiclePG);
    }
  }

  /** Search additional properties for a AmmunitionPG instance.
   * @return instance of AmmunitionPG or null.
   **/
  public AmmunitionPG getAmmunitionPG()
  {
    AmmunitionPG _tmp = (AmmunitionPG) resolvePG(AmmunitionPG.class);
    return (_tmp==AmmunitionPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a AmmunitionPG
   **/
  public boolean hasAmmunitionPG() {
    return (getAmmunitionPG() != null);
  }

  /** Set the AmmunitionPG property.
   * The default implementation will create a new AmmunitionPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setAmmunitionPG(PropertyGroup aAmmunitionPG) {
    if (aAmmunitionPG == null) {
      removeOtherPropertyGroup(AmmunitionPG.class);
    } else {
      addOtherPropertyGroup(aAmmunitionPG);
    }
  }

  /** Search additional properties for a AssignmentPG instance.
   * @return instance of AssignmentPG or null.
   **/
  public AssignmentPG getAssignmentPG()
  {
    AssignmentPG _tmp = (AssignmentPG) resolvePG(AssignmentPG.class);
    return (_tmp==AssignmentPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a AssignmentPG
   **/
  public boolean hasAssignmentPG() {
    return (getAssignmentPG() != null);
  }

  /** Set the AssignmentPG property.
   * The default implementation will create a new AssignmentPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setAssignmentPG(PropertyGroup aAssignmentPG) {
    if (aAssignmentPG == null) {
      removeOtherPropertyGroup(AssignmentPG.class);
    } else {
      addOtherPropertyGroup(aAssignmentPG);
    }
  }

  /** Search additional properties for a BulkSolidPG instance.
   * @return instance of BulkSolidPG or null.
   **/
  public BulkSolidPG getBulkSolidPG()
  {
    BulkSolidPG _tmp = (BulkSolidPG) resolvePG(BulkSolidPG.class);
    return (_tmp==BulkSolidPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a BulkSolidPG
   **/
  public boolean hasBulkSolidPG() {
    return (getBulkSolidPG() != null);
  }

  /** Set the BulkSolidPG property.
   * The default implementation will create a new BulkSolidPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setBulkSolidPG(PropertyGroup aBulkSolidPG) {
    if (aBulkSolidPG == null) {
      removeOtherPropertyGroup(BulkSolidPG.class);
    } else {
      addOtherPropertyGroup(aBulkSolidPG);
    }
  }

  /** Search additional properties for a AirLinkPG instance.
   * @return instance of AirLinkPG or null.
   **/
  public AirLinkPG getAirLinkPG()
  {
    AirLinkPG _tmp = (AirLinkPG) resolvePG(AirLinkPG.class);
    return (_tmp==AirLinkPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a AirLinkPG
   **/
  public boolean hasAirLinkPG() {
    return (getAirLinkPG() != null);
  }

  /** Set the AirLinkPG property.
   * The default implementation will create a new AirLinkPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setAirLinkPG(PropertyGroup aAirLinkPG) {
    if (aAirLinkPG == null) {
      removeOtherPropertyGroup(AirLinkPG.class);
    } else {
      addOtherPropertyGroup(aAirLinkPG);
    }
  }

  /** Search additional properties for a AirConditionPG instance.
   * @return instance of AirConditionPG or null.
   **/
  public AirConditionPG getAirConditionPG()
  {
    AirConditionPG _tmp = (AirConditionPG) resolvePG(AirConditionPG.class);
    return (_tmp==AirConditionPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a AirConditionPG
   **/
  public boolean hasAirConditionPG() {
    return (getAirConditionPG() != null);
  }

  /** Set the AirConditionPG property.
   * The default implementation will create a new AirConditionPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setAirConditionPG(PropertyGroup aAirConditionPG) {
    if (aAirConditionPG == null) {
      removeOtherPropertyGroup(AirConditionPG.class);
    } else {
      addOtherPropertyGroup(aAirConditionPG);
    }
  }

  /** Search additional properties for a PhysicalPG instance.
   * @return instance of PhysicalPG or null.
   **/
  public PhysicalPG getPhysicalPG()
  {
    PhysicalPG _tmp = (PhysicalPG) resolvePG(PhysicalPG.class);
    return (_tmp==PhysicalPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a PhysicalPG
   **/
  public boolean hasPhysicalPG() {
    return (getPhysicalPG() != null);
  }

  /** Set the PhysicalPG property.
   * The default implementation will create a new PhysicalPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setPhysicalPG(PropertyGroup aPhysicalPG) {
    if (aPhysicalPG == null) {
      removeOtherPropertyGroup(PhysicalPG.class);
    } else {
      addOtherPropertyGroup(aPhysicalPG);
    }
  }

  /** Search additional properties for a SupplyPG instance.
   * @return instance of SupplyPG or null.
   **/
  public SupplyPG getSupplyPG()
  {
    SupplyPG _tmp = (SupplyPG) resolvePG(SupplyPG.class);
    return (_tmp==SupplyPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a SupplyPG
   **/
  public boolean hasSupplyPG() {
    return (getSupplyPG() != null);
  }

  /** Set the SupplyPG property.
   * The default implementation will create a new SupplyPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setSupplyPG(PropertyGroup aSupplyPG) {
    if (aSupplyPG == null) {
      removeOtherPropertyGroup(SupplyPG.class);
    } else {
      addOtherPropertyGroup(aSupplyPG);
    }
  }

  /** Search additional properties for a ShipConfigurationPG instance.
   * @return instance of ShipConfigurationPG or null.
   **/
  public ShipConfigurationPG getShipConfigurationPG()
  {
    ShipConfigurationPG _tmp = (ShipConfigurationPG) resolvePG(ShipConfigurationPG.class);
    return (_tmp==ShipConfigurationPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a ShipConfigurationPG
   **/
  public boolean hasShipConfigurationPG() {
    return (getShipConfigurationPG() != null);
  }

  /** Set the ShipConfigurationPG property.
   * The default implementation will create a new ShipConfigurationPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setShipConfigurationPG(PropertyGroup aShipConfigurationPG) {
    if (aShipConfigurationPG == null) {
      removeOtherPropertyGroup(ShipConfigurationPG.class);
    } else {
      addOtherPropertyGroup(aShipConfigurationPG);
    }
  }

  /** Search additional properties for a MaintenancePG instance.
   * @return instance of MaintenancePG or null.
   **/
  public MaintenancePG getMaintenancePG()
  {
    MaintenancePG _tmp = (MaintenancePG) resolvePG(MaintenancePG.class);
    return (_tmp==MaintenancePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a MaintenancePG
   **/
  public boolean hasMaintenancePG() {
    return (getMaintenancePG() != null);
  }

  /** Set the MaintenancePG property.
   * The default implementation will create a new MaintenancePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setMaintenancePG(PropertyGroup aMaintenancePG) {
    if (aMaintenancePG == null) {
      removeOtherPropertyGroup(MaintenancePG.class);
    } else {
      addOtherPropertyGroup(aMaintenancePG);
    }
  }

  /** Search additional properties for a PersonSustainmentPG instance.
   * @return instance of PersonSustainmentPG or null.
   **/
  public PersonSustainmentPG getPersonSustainmentPG()
  {
    PersonSustainmentPG _tmp = (PersonSustainmentPG) resolvePG(PersonSustainmentPG.class);
    return (_tmp==PersonSustainmentPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a PersonSustainmentPG
   **/
  public boolean hasPersonSustainmentPG() {
    return (getPersonSustainmentPG() != null);
  }

  /** Set the PersonSustainmentPG property.
   * The default implementation will create a new PersonSustainmentPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setPersonSustainmentPG(PropertyGroup aPersonSustainmentPG) {
    if (aPersonSustainmentPG == null) {
      removeOtherPropertyGroup(PersonSustainmentPG.class);
    } else {
      addOtherPropertyGroup(aPersonSustainmentPG);
    }
  }

  /** Search additional properties for a RepairabilityPG instance.
   * @return instance of RepairabilityPG or null.
   **/
  public RepairabilityPG getRepairabilityPG()
  {
    RepairabilityPG _tmp = (RepairabilityPG) resolvePG(RepairabilityPG.class);
    return (_tmp==RepairabilityPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a RepairabilityPG
   **/
  public boolean hasRepairabilityPG() {
    return (getRepairabilityPG() != null);
  }

  /** Set the RepairabilityPG property.
   * The default implementation will create a new RepairabilityPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setRepairabilityPG(PropertyGroup aRepairabilityPG) {
    if (aRepairabilityPG == null) {
      removeOtherPropertyGroup(RepairabilityPG.class);
    } else {
      addOtherPropertyGroup(aRepairabilityPG);
    }
  }

  /** Search additional properties for a RepairablePG instance.
   * @return instance of RepairablePG or null.
   **/
  public RepairablePG getRepairablePG()
  {
    RepairablePG _tmp = (RepairablePG) resolvePG(RepairablePG.class);
    return (_tmp==RepairablePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a RepairablePG
   **/
  public boolean hasRepairablePG() {
    return (getRepairablePG() != null);
  }

  /** Set the RepairablePG property.
   * The default implementation will create a new RepairablePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setRepairablePG(PropertyGroup aRepairablePG) {
    if (aRepairablePG == null) {
      removeOtherPropertyGroup(RepairablePG.class);
    } else {
      addOtherPropertyGroup(aRepairablePG);
    }
  }

  /** Search additional properties for a ExplosivePG instance.
   * @return instance of ExplosivePG or null.
   **/
  public ExplosivePG getExplosivePG()
  {
    ExplosivePG _tmp = (ExplosivePG) resolvePG(ExplosivePG.class);
    return (_tmp==ExplosivePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a ExplosivePG
   **/
  public boolean hasExplosivePG() {
    return (getExplosivePG() != null);
  }

  /** Set the ExplosivePG property.
   * The default implementation will create a new ExplosivePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setExplosivePG(PropertyGroup aExplosivePG) {
    if (aExplosivePG == null) {
      removeOtherPropertyGroup(ExplosivePG.class);
    } else {
      addOtherPropertyGroup(aExplosivePG);
    }
  }

  /** Search additional properties for a GroundSelfPropulsionPG instance.
   * @return instance of GroundSelfPropulsionPG or null.
   **/
  public GroundSelfPropulsionPG getGroundSelfPropulsionPG()
  {
    GroundSelfPropulsionPG _tmp = (GroundSelfPropulsionPG) resolvePG(GroundSelfPropulsionPG.class);
    return (_tmp==GroundSelfPropulsionPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a GroundSelfPropulsionPG
   **/
  public boolean hasGroundSelfPropulsionPG() {
    return (getGroundSelfPropulsionPG() != null);
  }

  /** Set the GroundSelfPropulsionPG property.
   * The default implementation will create a new GroundSelfPropulsionPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setGroundSelfPropulsionPG(PropertyGroup aGroundSelfPropulsionPG) {
    if (aGroundSelfPropulsionPG == null) {
      removeOtherPropertyGroup(GroundSelfPropulsionPG.class);
    } else {
      addOtherPropertyGroup(aGroundSelfPropulsionPG);
    }
  }

  /** Search additional properties for a PersonPG instance.
   * @return instance of PersonPG or null.
   **/
  public PersonPG getPersonPG()
  {
    PersonPG _tmp = (PersonPG) resolvePG(PersonPG.class);
    return (_tmp==PersonPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a PersonPG
   **/
  public boolean hasPersonPG() {
    return (getPersonPG() != null);
  }

  /** Set the PersonPG property.
   * The default implementation will create a new PersonPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setPersonPG(PropertyGroup aPersonPG) {
    if (aPersonPG == null) {
      removeOtherPropertyGroup(PersonPG.class);
    } else {
      addOtherPropertyGroup(aPersonPG);
    }
  }

  /** Search additional properties for a MEIPG instance.
   * @return instance of MEIPG or null.
   **/
  public MEIPG getMEIPG()
  {
    MEIPG _tmp = (MEIPG) resolvePG(MEIPG.class);
    return (_tmp==MEIPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a MEIPG
   **/
  public boolean hasMEIPG() {
    return (getMEIPG() != null);
  }

  /** Set the MEIPG property.
   * The default implementation will create a new MEIPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setMEIPG(PropertyGroup aMEIPG) {
    if (aMEIPG == null) {
      removeOtherPropertyGroup(MEIPG.class);
    } else {
      addOtherPropertyGroup(aMEIPG);
    }
  }

  /** Search additional properties for a ContentsPG instance.
   * @return instance of ContentsPG or null.
   **/
  public ContentsPG getContentsPG()
  {
    ContentsPG _tmp = (ContentsPG) resolvePG(ContentsPG.class);
    return (_tmp==ContentsPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a ContentsPG
   **/
  public boolean hasContentsPG() {
    return (getContentsPG() != null);
  }

  /** Set the ContentsPG property.
   * The default implementation will create a new ContentsPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setContentsPG(PropertyGroup aContentsPG) {
    if (aContentsPG == null) {
      removeOtherPropertyGroup(ContentsPG.class);
    } else {
      addOtherPropertyGroup(aContentsPG);
    }
  }

  /** Search additional properties for a ManagedAssetPG instance.
   * @return instance of ManagedAssetPG or null.
   **/
  public ManagedAssetPG getManagedAssetPG()
  {
    ManagedAssetPG _tmp = (ManagedAssetPG) resolvePG(ManagedAssetPG.class);
    return (_tmp==ManagedAssetPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a ManagedAssetPG
   **/
  public boolean hasManagedAssetPG() {
    return (getManagedAssetPG() != null);
  }

  /** Set the ManagedAssetPG property.
   * The default implementation will create a new ManagedAssetPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setManagedAssetPG(PropertyGroup aManagedAssetPG) {
    if (aManagedAssetPG == null) {
      removeOtherPropertyGroup(ManagedAssetPG.class);
    } else {
      addOtherPropertyGroup(aManagedAssetPG);
    }
  }

  /** Search additional properties for a EquipmentOHReadinessPG instance.
   * @return instance of EquipmentOHReadinessPG or null.
   **/
  public EquipmentOHReadinessPG getEquipmentOHReadinessPG()
  {
    EquipmentOHReadinessPG _tmp = (EquipmentOHReadinessPG) resolvePG(EquipmentOHReadinessPG.class);
    return (_tmp==EquipmentOHReadinessPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a EquipmentOHReadinessPG
   **/
  public boolean hasEquipmentOHReadinessPG() {
    return (getEquipmentOHReadinessPG() != null);
  }

  /** Set the EquipmentOHReadinessPG property.
   * The default implementation will create a new EquipmentOHReadinessPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setEquipmentOHReadinessPG(PropertyGroup aEquipmentOHReadinessPG) {
    if (aEquipmentOHReadinessPG == null) {
      removeOtherPropertyGroup(EquipmentOHReadinessPG.class);
    } else {
      addOtherPropertyGroup(aEquipmentOHReadinessPG);
    }
  }

  /** Search additional properties for a LiquidPG instance.
   * @return instance of LiquidPG or null.
   **/
  public LiquidPG getLiquidPG()
  {
    LiquidPG _tmp = (LiquidPG) resolvePG(LiquidPG.class);
    return (_tmp==LiquidPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a LiquidPG
   **/
  public boolean hasLiquidPG() {
    return (getLiquidPG() != null);
  }

  /** Set the LiquidPG property.
   * The default implementation will create a new LiquidPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setLiquidPG(PropertyGroup aLiquidPG) {
    if (aLiquidPG == null) {
      removeOtherPropertyGroup(LiquidPG.class);
    } else {
      addOtherPropertyGroup(aLiquidPG);
    }
  }

  /** Search additional properties for a VolumetricStockagePG instance.
   * @return instance of VolumetricStockagePG or null.
   **/
  public VolumetricStockagePG getVolumetricStockagePG()
  {
    VolumetricStockagePG _tmp = (VolumetricStockagePG) resolvePG(VolumetricStockagePG.class);
    return (_tmp==VolumetricStockagePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a VolumetricStockagePG
   **/
  public boolean hasVolumetricStockagePG() {
    return (getVolumetricStockagePG() != null);
  }

  /** Set the VolumetricStockagePG property.
   * The default implementation will create a new VolumetricStockagePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setVolumetricStockagePG(PropertyGroup aVolumetricStockagePG) {
    if (aVolumetricStockagePG == null) {
      removeOtherPropertyGroup(VolumetricStockagePG.class);
    } else {
      addOtherPropertyGroup(aVolumetricStockagePG);
    }
  }

  /** Search additional properties for a OrganizationPG instance.
   * @return instance of OrganizationPG or null.
   **/
  public OrganizationPG getOrganizationPG()
  {
    OrganizationPG _tmp = (OrganizationPG) resolvePG(OrganizationPG.class);
    return (_tmp==OrganizationPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a OrganizationPG
   **/
  public boolean hasOrganizationPG() {
    return (getOrganizationPG() != null);
  }

  /** Set the OrganizationPG property.
   * The default implementation will create a new OrganizationPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setOrganizationPG(PropertyGroup aOrganizationPG) {
    if (aOrganizationPG == null) {
      removeOtherPropertyGroup(OrganizationPG.class);
    } else {
      addOtherPropertyGroup(aOrganizationPG);
    }
  }

  /** Search additional properties for a WeaponPG instance.
   * @return instance of WeaponPG or null.
   **/
  public WeaponPG getWeaponPG()
  {
    WeaponPG _tmp = (WeaponPG) resolvePG(WeaponPG.class);
    return (_tmp==WeaponPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a WeaponPG
   **/
  public boolean hasWeaponPG() {
    return (getWeaponPG() != null);
  }

  /** Set the WeaponPG property.
   * The default implementation will create a new WeaponPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setWeaponPG(PropertyGroup aWeaponPG) {
    if (aWeaponPG == null) {
      removeOtherPropertyGroup(WeaponPG.class);
    } else {
      addOtherPropertyGroup(aWeaponPG);
    }
  }

  /** Search additional properties for a TowPG instance.
   * @return instance of TowPG or null.
   **/
  public TowPG getTowPG()
  {
    TowPG _tmp = (TowPG) resolvePG(TowPG.class);
    return (_tmp==TowPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a TowPG
   **/
  public boolean hasTowPG() {
    return (getTowPG() != null);
  }

  /** Set the TowPG property.
   * The default implementation will create a new TowPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setTowPG(PropertyGroup aTowPG) {
    if (aTowPG == null) {
      removeOtherPropertyGroup(TowPG.class);
    } else {
      addOtherPropertyGroup(aTowPG);
    }
  }

  /** Search additional properties for a LiftPG instance.
   * @return instance of LiftPG or null.
   **/
  public LiftPG getLiftPG()
  {
    LiftPG _tmp = (LiftPG) resolvePG(LiftPG.class);
    return (_tmp==LiftPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a LiftPG
   **/
  public boolean hasLiftPG() {
    return (getLiftPG() != null);
  }

  /** Set the LiftPG property.
   * The default implementation will create a new LiftPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setLiftPG(PropertyGroup aLiftPG) {
    if (aLiftPG == null) {
      removeOtherPropertyGroup(LiftPG.class);
    } else {
      addOtherPropertyGroup(aLiftPG);
    }
  }

  /** Search additional properties for a RoadLinkPG instance.
   * @return instance of RoadLinkPG or null.
   **/
  public RoadLinkPG getRoadLinkPG()
  {
    RoadLinkPG _tmp = (RoadLinkPG) resolvePG(RoadLinkPG.class);
    return (_tmp==RoadLinkPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a RoadLinkPG
   **/
  public boolean hasRoadLinkPG() {
    return (getRoadLinkPG() != null);
  }

  /** Set the RoadLinkPG property.
   * The default implementation will create a new RoadLinkPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setRoadLinkPG(PropertyGroup aRoadLinkPG) {
    if (aRoadLinkPG == null) {
      removeOtherPropertyGroup(RoadLinkPG.class);
    } else {
      addOtherPropertyGroup(aRoadLinkPG);
    }
  }

  /** Search additional properties for a SeaportPG instance.
   * @return instance of SeaportPG or null.
   **/
  public SeaportPG getSeaportPG()
  {
    SeaportPG _tmp = (SeaportPG) resolvePG(SeaportPG.class);
    return (_tmp==SeaportPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a SeaportPG
   **/
  public boolean hasSeaportPG() {
    return (getSeaportPG() != null);
  }

  /** Set the SeaportPG property.
   * The default implementation will create a new SeaportPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setSeaportPG(PropertyGroup aSeaportPG) {
    if (aSeaportPG == null) {
      removeOtherPropertyGroup(SeaportPG.class);
    } else {
      addOtherPropertyGroup(aSeaportPG);
    }
  }

  /** Search additional properties for a ReportSchedulePG instance.
   * @return instance of ReportSchedulePG or null.
   **/
  public ReportSchedulePG getReportSchedulePG()
  {
    ReportSchedulePG _tmp = (ReportSchedulePG) resolvePG(ReportSchedulePG.class);
    return (_tmp==ReportSchedulePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a ReportSchedulePG
   **/
  public boolean hasReportSchedulePG() {
    return (getReportSchedulePG() != null);
  }

  /** Set the ReportSchedulePG property.
   * The default implementation will create a new ReportSchedulePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setReportSchedulePG(PropertyGroup aReportSchedulePG) {
    if (aReportSchedulePG == null) {
      removeOtherPropertyGroup(ReportSchedulePG.class);
    } else {
      addOtherPropertyGroup(aReportSchedulePG);
    }
  }

  /** Search additional properties for a SupportPG instance.
   * @return instance of SupportPG or null.
   **/
  public SupportPG getSupportPG()
  {
    SupportPG _tmp = (SupportPG) resolvePG(SupportPG.class);
    return (_tmp==SupportPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a SupportPG
   **/
  public boolean hasSupportPG() {
    return (getSupportPG() != null);
  }

  /** Set the SupportPG property.
   * The default implementation will create a new SupportPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setSupportPG(PropertyGroup aSupportPG) {
    if (aSupportPG == null) {
      removeOtherPropertyGroup(SupportPG.class);
    } else {
      addOtherPropertyGroup(aSupportPG);
    }
  }

  /** Search additional properties for a DetailKeyPG instance.
   * @return instance of DetailKeyPG or null.
   **/
  public DetailKeyPG getDetailKeyPG()
  {
    DetailKeyPG _tmp = (DetailKeyPG) resolvePG(DetailKeyPG.class);
    return (_tmp==DetailKeyPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a DetailKeyPG
   **/
  public boolean hasDetailKeyPG() {
    return (getDetailKeyPG() != null);
  }

  /** Set the DetailKeyPG property.
   * The default implementation will create a new DetailKeyPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setDetailKeyPG(PropertyGroup aDetailKeyPG) {
    if (aDetailKeyPG == null) {
      removeOtherPropertyGroup(DetailKeyPG.class);
    } else {
      addOtherPropertyGroup(aDetailKeyPG);
    }
  }

  /** Search additional properties for a LandConditionPG instance.
   * @return instance of LandConditionPG or null.
   **/
  public LandConditionPG getLandConditionPG()
  {
    LandConditionPG _tmp = (LandConditionPG) resolvePG(LandConditionPG.class);
    return (_tmp==LandConditionPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a LandConditionPG
   **/
  public boolean hasLandConditionPG() {
    return (getLandConditionPG() != null);
  }

  /** Set the LandConditionPG property.
   * The default implementation will create a new LandConditionPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setLandConditionPG(PropertyGroup aLandConditionPG) {
    if (aLandConditionPG == null) {
      removeOtherPropertyGroup(LandConditionPG.class);
    } else {
      addOtherPropertyGroup(aLandConditionPG);
    }
  }

  /** Search additional properties for a RepairDepotPG instance.
   * @return instance of RepairDepotPG or null.
   **/
  public RepairDepotPG getRepairDepotPG()
  {
    RepairDepotPG _tmp = (RepairDepotPG) resolvePG(RepairDepotPG.class);
    return (_tmp==RepairDepotPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a RepairDepotPG
   **/
  public boolean hasRepairDepotPG() {
    return (getRepairDepotPG() != null);
  }

  /** Set the RepairDepotPG property.
   * The default implementation will create a new RepairDepotPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setRepairDepotPG(PropertyGroup aRepairDepotPG) {
    if (aRepairDepotPG == null) {
      removeOtherPropertyGroup(RepairDepotPG.class);
    } else {
      addOtherPropertyGroup(aRepairDepotPG);
    }
  }

  /** Search additional properties for a InventoryPG instance.
   * @return instance of InventoryPG or null.
   **/
  public InventoryPG getInventoryPG()
  {
    InventoryPG _tmp = (InventoryPG) resolvePG(InventoryPG.class);
    return (_tmp==InventoryPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a InventoryPG
   **/
  public boolean hasInventoryPG() {
    return (getInventoryPG() != null);
  }

  /** Set the InventoryPG property.
   * The default implementation will create a new InventoryPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setInventoryPG(PropertyGroup aInventoryPG) {
    if (aInventoryPG == null) {
      removeOtherPropertyGroup(InventoryPG.class);
    } else {
      addOtherPropertyGroup(aInventoryPG);
    }
  }

  /** Search additional properties for a SeaConditionPG instance.
   * @return instance of SeaConditionPG or null.
   **/
  public SeaConditionPG getSeaConditionPG()
  {
    SeaConditionPG _tmp = (SeaConditionPG) resolvePG(SeaConditionPG.class);
    return (_tmp==SeaConditionPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a SeaConditionPG
   **/
  public boolean hasSeaConditionPG() {
    return (getSeaConditionPG() != null);
  }

  /** Set the SeaConditionPG property.
   * The default implementation will create a new SeaConditionPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setSeaConditionPG(PropertyGroup aSeaConditionPG) {
    if (aSeaConditionPG == null) {
      removeOtherPropertyGroup(SeaConditionPG.class);
    } else {
      addOtherPropertyGroup(aSeaConditionPG);
    }
  }

  /** Search additional properties for a RailSelfPropulsionPG instance.
   * @return instance of RailSelfPropulsionPG or null.
   **/
  public RailSelfPropulsionPG getRailSelfPropulsionPG()
  {
    RailSelfPropulsionPG _tmp = (RailSelfPropulsionPG) resolvePG(RailSelfPropulsionPG.class);
    return (_tmp==RailSelfPropulsionPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a RailSelfPropulsionPG
   **/
  public boolean hasRailSelfPropulsionPG() {
    return (getRailSelfPropulsionPG() != null);
  }

  /** Set the RailSelfPropulsionPG property.
   * The default implementation will create a new RailSelfPropulsionPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setRailSelfPropulsionPG(PropertyGroup aRailSelfPropulsionPG) {
    if (aRailSelfPropulsionPG == null) {
      removeOtherPropertyGroup(RailSelfPropulsionPG.class);
    } else {
      addOtherPropertyGroup(aRailSelfPropulsionPG);
    }
  }

  /** Search additional properties for a CSSCapabilityPG instance.
   * @return instance of CSSCapabilityPG or null.
   **/
  public CSSCapabilityPG getCSSCapabilityPG()
  {
    CSSCapabilityPG _tmp = (CSSCapabilityPG) resolvePG(CSSCapabilityPG.class);
    return (_tmp==CSSCapabilityPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a CSSCapabilityPG
   **/
  public boolean hasCSSCapabilityPG() {
    return (getCSSCapabilityPG() != null);
  }

  /** Set the CSSCapabilityPG property.
   * The default implementation will create a new CSSCapabilityPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setCSSCapabilityPG(PropertyGroup aCSSCapabilityPG) {
    if (aCSSCapabilityPG == null) {
      removeOtherPropertyGroup(CSSCapabilityPG.class);
    } else {
      addOtherPropertyGroup(aCSSCapabilityPG);
    }
  }

  /** Search additional properties for a MidAirRefuelPG instance.
   * @return instance of MidAirRefuelPG or null.
   **/
  public MidAirRefuelPG getMidAirRefuelPG()
  {
    MidAirRefuelPG _tmp = (MidAirRefuelPG) resolvePG(MidAirRefuelPG.class);
    return (_tmp==MidAirRefuelPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a MidAirRefuelPG
   **/
  public boolean hasMidAirRefuelPG() {
    return (getMidAirRefuelPG() != null);
  }

  /** Set the MidAirRefuelPG property.
   * The default implementation will create a new MidAirRefuelPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setMidAirRefuelPG(PropertyGroup aMidAirRefuelPG) {
    if (aMidAirRefuelPG == null) {
      removeOtherPropertyGroup(MidAirRefuelPG.class);
    } else {
      addOtherPropertyGroup(aMidAirRefuelPG);
    }
  }

  /** Search additional properties for a InventoryLevelsPG instance.
   * @return instance of InventoryLevelsPG or null.
   **/
  public InventoryLevelsPG getInventoryLevelsPG()
  {
    InventoryLevelsPG _tmp = (InventoryLevelsPG) resolvePG(InventoryLevelsPG.class);
    return (_tmp==InventoryLevelsPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a InventoryLevelsPG
   **/
  public boolean hasInventoryLevelsPG() {
    return (getInventoryLevelsPG() != null);
  }

  /** Set the InventoryLevelsPG property.
   * The default implementation will create a new InventoryLevelsPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setInventoryLevelsPG(PropertyGroup aInventoryLevelsPG) {
    if (aInventoryLevelsPG == null) {
      removeOtherPropertyGroup(InventoryLevelsPG.class);
    } else {
      addOtherPropertyGroup(aInventoryLevelsPG);
    }
  }

  /** Search additional properties for a VehiclePropertyGroups instance.
   * @return instance of VehiclePropertyGroups or null.
   **/
  public VehiclePropertyGroups getVehiclePropertyGroups()
  {
    VehiclePropertyGroups _tmp = (VehiclePropertyGroups) resolvePG(VehiclePropertyGroups.class);
    return (_tmp==VehiclePropertyGroups.nullPG)?null:_tmp;
  }

  /** Test for existence of a VehiclePropertyGroups
   **/
  public boolean hasVehiclePropertyGroups() {
    return (getVehiclePropertyGroups() != null);
  }

  /** Set the VehiclePropertyGroups property.
   * The default implementation will create a new VehiclePropertyGroups
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setVehiclePropertyGroups(PropertyGroup aVehiclePropertyGroups) {
    if (aVehiclePropertyGroups == null) {
      removeOtherPropertyGroup(VehiclePropertyGroups.class);
    } else {
      addOtherPropertyGroup(aVehiclePropertyGroups);
    }
  }

  /** Search additional properties for a TrainingReadinessPG instance.
   * @return instance of TrainingReadinessPG or null.
   **/
  public TrainingReadinessPG getTrainingReadinessPG()
  {
    TrainingReadinessPG _tmp = (TrainingReadinessPG) resolvePG(TrainingReadinessPG.class);
    return (_tmp==TrainingReadinessPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a TrainingReadinessPG
   **/
  public boolean hasTrainingReadinessPG() {
    return (getTrainingReadinessPG() != null);
  }

  /** Set the TrainingReadinessPG property.
   * The default implementation will create a new TrainingReadinessPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setTrainingReadinessPG(PropertyGroup aTrainingReadinessPG) {
    if (aTrainingReadinessPG == null) {
      removeOtherPropertyGroup(TrainingReadinessPG.class);
    } else {
      addOtherPropertyGroup(aTrainingReadinessPG);
    }
  }

  /** Search additional properties for a RailLinkPG instance.
   * @return instance of RailLinkPG or null.
   **/
  public RailLinkPG getRailLinkPG()
  {
    RailLinkPG _tmp = (RailLinkPG) resolvePG(RailLinkPG.class);
    return (_tmp==RailLinkPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a RailLinkPG
   **/
  public boolean hasRailLinkPG() {
    return (getRailLinkPG() != null);
  }

  /** Set the RailLinkPG property.
   * The default implementation will create a new RailLinkPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setRailLinkPG(PropertyGroup aRailLinkPG) {
    if (aRailLinkPG == null) {
      removeOtherPropertyGroup(RailLinkPG.class);
    } else {
      addOtherPropertyGroup(aRailLinkPG);
    }
  }

  /** Search additional properties for a LiquidSupplyPG instance.
   * @return instance of LiquidSupplyPG or null.
   **/
  public LiquidSupplyPG getLiquidSupplyPG()
  {
    LiquidSupplyPG _tmp = (LiquidSupplyPG) resolvePG(LiquidSupplyPG.class);
    return (_tmp==LiquidSupplyPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a LiquidSupplyPG
   **/
  public boolean hasLiquidSupplyPG() {
    return (getLiquidSupplyPG() != null);
  }

  /** Set the LiquidSupplyPG property.
   * The default implementation will create a new LiquidSupplyPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setLiquidSupplyPG(PropertyGroup aLiquidSupplyPG) {
    if (aLiquidSupplyPG == null) {
      removeOtherPropertyGroup(LiquidSupplyPG.class);
    } else {
      addOtherPropertyGroup(aLiquidSupplyPG);
    }
  }

  /** Search additional properties for a DetailedScheduledContentPG instance.
   * @return instance of DetailedScheduledContentPG or null.
   **/
  public DetailedScheduledContentPG getDetailedScheduledContentPG()
  {
    DetailedScheduledContentPG _tmp = (DetailedScheduledContentPG) resolvePG(DetailedScheduledContentPG.class);
    return (_tmp==DetailedScheduledContentPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a DetailedScheduledContentPG
   **/
  public boolean hasDetailedScheduledContentPG() {
    return (getDetailedScheduledContentPG() != null);
  }

  /** Set the DetailedScheduledContentPG property.
   * The default implementation will create a new DetailedScheduledContentPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setDetailedScheduledContentPG(PropertyGroup aDetailedScheduledContentPG) {
    if (aDetailedScheduledContentPG == null) {
      removeOtherPropertyGroup(DetailedScheduledContentPG.class);
    } else {
      addOtherPropertyGroup(aDetailedScheduledContentPG);
    }
  }

  /** Search additional properties for a OffRoadTransportationPG instance.
   * @return instance of OffRoadTransportationPG or null.
   **/
  public OffRoadTransportationPG getOffRoadTransportationPG()
  {
    OffRoadTransportationPG _tmp = (OffRoadTransportationPG) resolvePG(OffRoadTransportationPG.class);
    return (_tmp==OffRoadTransportationPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a OffRoadTransportationPG
   **/
  public boolean hasOffRoadTransportationPG() {
    return (getOffRoadTransportationPG() != null);
  }

  /** Set the OffRoadTransportationPG property.
   * The default implementation will create a new OffRoadTransportationPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setOffRoadTransportationPG(PropertyGroup aOffRoadTransportationPG) {
    if (aOffRoadTransportationPG == null) {
      removeOtherPropertyGroup(OffRoadTransportationPG.class);
    } else {
      addOtherPropertyGroup(aOffRoadTransportationPG);
    }
  }

  /** Search additional properties for a AssetConsumptionRatePG instance.
   * @return instance of AssetConsumptionRatePG or null.
   **/
  public AssetConsumptionRatePG getAssetConsumptionRatePG()
  {
    AssetConsumptionRatePG _tmp = (AssetConsumptionRatePG) resolvePG(AssetConsumptionRatePG.class);
    return (_tmp==AssetConsumptionRatePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a AssetConsumptionRatePG
   **/
  public boolean hasAssetConsumptionRatePG() {
    return (getAssetConsumptionRatePG() != null);
  }

  /** Set the AssetConsumptionRatePG property.
   * The default implementation will create a new AssetConsumptionRatePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setAssetConsumptionRatePG(PropertyGroup aAssetConsumptionRatePG) {
    if (aAssetConsumptionRatePG == null) {
      removeOtherPropertyGroup(AssetConsumptionRatePG.class);
    } else {
      addOtherPropertyGroup(aAssetConsumptionRatePG);
    }
  }

  /** Search additional properties for a RailVehiclePG instance.
   * @return instance of RailVehiclePG or null.
   **/
  public RailVehiclePG getRailVehiclePG()
  {
    RailVehiclePG _tmp = (RailVehiclePG) resolvePG(RailVehiclePG.class);
    return (_tmp==RailVehiclePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a RailVehiclePG
   **/
  public boolean hasRailVehiclePG() {
    return (getRailVehiclePG() != null);
  }

  /** Set the RailVehiclePG property.
   * The default implementation will create a new RailVehiclePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setRailVehiclePG(PropertyGroup aRailVehiclePG) {
    if (aRailVehiclePG == null) {
      removeOtherPropertyGroup(RailVehiclePG.class);
    } else {
      addOtherPropertyGroup(aRailVehiclePG);
    }
  }

  /** Search additional properties for a FacilityPG instance.
   * @return instance of FacilityPG or null.
   **/
  public FacilityPG getFacilityPG()
  {
    FacilityPG _tmp = (FacilityPG) resolvePG(FacilityPG.class);
    return (_tmp==FacilityPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a FacilityPG
   **/
  public boolean hasFacilityPG() {
    return (getFacilityPG() != null);
  }

  /** Set the FacilityPG property.
   * The default implementation will create a new FacilityPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setFacilityPG(PropertyGroup aFacilityPG) {
    if (aFacilityPG == null) {
      removeOtherPropertyGroup(FacilityPG.class);
    } else {
      addOtherPropertyGroup(aFacilityPG);
    }
  }

  /** Search additional properties for a MissileLauncherPG instance.
   * @return instance of MissileLauncherPG or null.
   **/
  public MissileLauncherPG getMissileLauncherPG()
  {
    MissileLauncherPG _tmp = (MissileLauncherPG) resolvePG(MissileLauncherPG.class);
    return (_tmp==MissileLauncherPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a MissileLauncherPG
   **/
  public boolean hasMissileLauncherPG() {
    return (getMissileLauncherPG() != null);
  }

  /** Set the MissileLauncherPG property.
   * The default implementation will create a new MissileLauncherPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setMissileLauncherPG(PropertyGroup aMissileLauncherPG) {
    if (aMissileLauncherPG == null) {
      removeOtherPropertyGroup(MissileLauncherPG.class);
    } else {
      addOtherPropertyGroup(aMissileLauncherPG);
    }
  }

  /** Search additional properties for a AirVehiclePG instance.
   * @return instance of AirVehiclePG or null.
   **/
  public AirVehiclePG getAirVehiclePG()
  {
    AirVehiclePG _tmp = (AirVehiclePG) resolvePG(AirVehiclePG.class);
    return (_tmp==AirVehiclePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a AirVehiclePG
   **/
  public boolean hasAirVehiclePG() {
    return (getAirVehiclePG() != null);
  }

  /** Set the AirVehiclePG property.
   * The default implementation will create a new AirVehiclePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setAirVehiclePG(PropertyGroup aAirVehiclePG) {
    if (aAirVehiclePG == null) {
      removeOtherPropertyGroup(AirVehiclePG.class);
    } else {
      addOtherPropertyGroup(aAirVehiclePG);
    }
  }

  /** Search additional properties for a DeckPG instance.
   * @return instance of DeckPG or null.
   **/
  public DeckPG getDeckPG()
  {
    DeckPG _tmp = (DeckPG) resolvePG(DeckPG.class);
    return (_tmp==DeckPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a DeckPG
   **/
  public boolean hasDeckPG() {
    return (getDeckPG() != null);
  }

  /** Set the DeckPG property.
   * The default implementation will create a new DeckPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setDeckPG(PropertyGroup aDeckPG) {
    if (aDeckPG == null) {
      removeOtherPropertyGroup(DeckPG.class);
    } else {
      addOtherPropertyGroup(aDeckPG);
    }
  }

  /** Search additional properties for a EquipmentStatusReadinessPG instance.
   * @return instance of EquipmentStatusReadinessPG or null.
   **/
  public EquipmentStatusReadinessPG getEquipmentStatusReadinessPG()
  {
    EquipmentStatusReadinessPG _tmp = (EquipmentStatusReadinessPG) resolvePG(EquipmentStatusReadinessPG.class);
    return (_tmp==EquipmentStatusReadinessPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a EquipmentStatusReadinessPG
   **/
  public boolean hasEquipmentStatusReadinessPG() {
    return (getEquipmentStatusReadinessPG() != null);
  }

  /** Set the EquipmentStatusReadinessPG property.
   * The default implementation will create a new EquipmentStatusReadinessPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setEquipmentStatusReadinessPG(PropertyGroup aEquipmentStatusReadinessPG) {
    if (aEquipmentStatusReadinessPG == null) {
      removeOtherPropertyGroup(EquipmentStatusReadinessPG.class);
    } else {
      addOtherPropertyGroup(aEquipmentStatusReadinessPG);
    }
  }

  /** Search additional properties for a WaterVehiclePG instance.
   * @return instance of WaterVehiclePG or null.
   **/
  public WaterVehiclePG getWaterVehiclePG()
  {
    WaterVehiclePG _tmp = (WaterVehiclePG) resolvePG(WaterVehiclePG.class);
    return (_tmp==WaterVehiclePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a WaterVehiclePG
   **/
  public boolean hasWaterVehiclePG() {
    return (getWaterVehiclePG() != null);
  }

  /** Set the WaterVehiclePG property.
   * The default implementation will create a new WaterVehiclePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setWaterVehiclePG(PropertyGroup aWaterVehiclePG) {
    if (aWaterVehiclePG == null) {
      removeOtherPropertyGroup(WaterVehiclePG.class);
    } else {
      addOtherPropertyGroup(aWaterVehiclePG);
    }
  }

  /** Search additional properties for a WarheadPG instance.
   * @return instance of WarheadPG or null.
   **/
  public WarheadPG getWarheadPG()
  {
    WarheadPG _tmp = (WarheadPG) resolvePG(WarheadPG.class);
    return (_tmp==WarheadPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a WarheadPG
   **/
  public boolean hasWarheadPG() {
    return (getWarheadPG() != null);
  }

  /** Set the WarheadPG property.
   * The default implementation will create a new WarheadPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setWarheadPG(PropertyGroup aWarheadPG) {
    if (aWarheadPG == null) {
      removeOtherPropertyGroup(WarheadPG.class);
    } else {
      addOtherPropertyGroup(aWarheadPG);
    }
  }

  /** Search additional properties for a PackagePG instance.
   * @return instance of PackagePG or null.
   **/
  public PackagePG getPackagePG()
  {
    PackagePG _tmp = (PackagePG) resolvePG(PackagePG.class);
    return (_tmp==PackagePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a PackagePG
   **/
  public boolean hasPackagePG() {
    return (getPackagePG() != null);
  }

  /** Set the PackagePG property.
   * The default implementation will create a new PackagePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setPackagePG(PropertyGroup aPackagePG) {
    if (aPackagePG == null) {
      removeOtherPropertyGroup(PackagePG.class);
    } else {
      addOtherPropertyGroup(aPackagePG);
    }
  }

  /** Search additional properties for a ConsumablePG instance.
   * @return instance of ConsumablePG or null.
   **/
  public ConsumablePG getConsumablePG()
  {
    ConsumablePG _tmp = (ConsumablePG) resolvePG(ConsumablePG.class);
    return (_tmp==ConsumablePG.nullPG)?null:_tmp;
  }

  /** Test for existence of a ConsumablePG
   **/
  public boolean hasConsumablePG() {
    return (getConsumablePG() != null);
  }

  /** Set the ConsumablePG property.
   * The default implementation will create a new ConsumablePG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setConsumablePG(PropertyGroup aConsumablePG) {
    if (aConsumablePG == null) {
      removeOtherPropertyGroup(ConsumablePG.class);
    } else {
      addOtherPropertyGroup(aConsumablePG);
    }
  }

  /** Search additional properties for a FoodPG instance.
   * @return instance of FoodPG or null.
   **/
  public FoodPG getFoodPG()
  {
    FoodPG _tmp = (FoodPG) resolvePG(FoodPG.class);
    return (_tmp==FoodPG.nullPG)?null:_tmp;
  }

  /** Test for existence of a FoodPG
   **/
  public boolean hasFoodPG() {
    return (getFoodPG() != null);
  }

  /** Set the FoodPG property.
   * The default implementation will create a new FoodPG
   * property and add it to the otherPropertyGroup list.
   * Many subclasses override with local slots.
   **/
  public void setFoodPG(PropertyGroup aFoodPG) {
    if (aFoodPG == null) {
      removeOtherPropertyGroup(FoodPG.class);
    } else {
      addOtherPropertyGroup(aFoodPG);
    }
  }

}
