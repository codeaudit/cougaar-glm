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
/** AbstractFactory implementation for Properties.
 * Prevents clients from needing to know the implementation
 * class(es) of any of the properties.
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


public class PropertyGroupFactory extends org.cougaar.planning.ldm.asset.PropertyGroupFactory {
  // brand-new instance factory
  public static NewCargoFacilityPG newCargoFacilityPG() {
    return new CargoFacilityPGImpl();
  }
  // instance from prototype factory
  public static NewCargoFacilityPG newCargoFacilityPG(CargoFacilityPG prototype) {
    return new CargoFacilityPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewSeaLinkPG newSeaLinkPG() {
    return new SeaLinkPGImpl();
  }
  // instance from prototype factory
  public static NewSeaLinkPG newSeaLinkPG(SeaLinkPG prototype) {
    return new SeaLinkPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewAirTransportationPG newAirTransportationPG() {
    return new AirTransportationPGImpl();
  }
  // instance from prototype factory
  public static NewAirTransportationPG newAirTransportationPG(AirTransportationPG prototype) {
    return new AirTransportationPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewSupplyDepotPG newSupplyDepotPG() {
    return new SupplyDepotPGImpl();
  }
  // instance from prototype factory
  public static NewSupplyDepotPG newSupplyDepotPG(SupplyDepotPG prototype) {
    return new SupplyDepotPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewTruckTerminalPG newTruckTerminalPG() {
    return new TruckTerminalPGImpl();
  }
  // instance from prototype factory
  public static NewTruckTerminalPG newTruckTerminalPG(TruckTerminalPG prototype) {
    return new TruckTerminalPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewMilitaryOrgPG newMilitaryOrgPG() {
    return new MilitaryOrgPGImpl();
  }
  // instance from prototype factory
  public static NewMilitaryOrgPG newMilitaryOrgPG(MilitaryOrgPG prototype) {
    return new MilitaryOrgPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewRailTerminalPG newRailTerminalPG() {
    return new RailTerminalPGImpl();
  }
  // instance from prototype factory
  public static NewRailTerminalPG newRailTerminalPG(RailTerminalPG prototype) {
    return new RailTerminalPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewFuelPG newFuelPG() {
    return new FuelPGImpl();
  }
  // instance from prototype factory
  public static NewFuelPG newFuelPG(FuelPG prototype) {
    return new FuelPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewAssignedPG newAssignedPG() {
    return new AssignedPGImpl();
  }
  // instance from prototype factory
  public static NewAssignedPG newAssignedPG(AssignedPG prototype) {
    return new AssignedPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewFuelSupplyPG newFuelSupplyPG() {
    return new FuelSupplyPGImpl();
  }
  // instance from prototype factory
  public static NewFuelSupplyPG newFuelSupplyPG(FuelSupplyPG prototype) {
    return new FuelSupplyPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewForUnitPG newForUnitPG() {
    return new ForUnitPGImpl();
  }
  // instance from prototype factory
  public static NewForUnitPG newForUnitPG(ForUnitPG prototype) {
    return new ForUnitPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewFromBasePG newFromBasePG() {
    return new FromBasePGImpl();
  }
  // instance from prototype factory
  public static NewFromBasePG newFromBasePG(FromBasePG prototype) {
    return new FromBasePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewTransportationPG newTransportationPG() {
    return new TransportationPGImpl();
  }
  // instance from prototype factory
  public static NewTransportationPG newTransportationPG(TransportationPG prototype) {
    return new TransportationPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewCostPG newCostPG() {
    return new CostPGImpl();
  }
  // instance from prototype factory
  public static NewCostPG newCostPG(CostPG prototype) {
    return new CostPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewWaterPG newWaterPG() {
    return new WaterPGImpl();
  }
  // instance from prototype factory
  public static NewWaterPG newWaterPG(WaterPG prototype) {
    return new WaterPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewAirSelfPropulsionPG newAirSelfPropulsionPG() {
    return new AirSelfPropulsionPGImpl();
  }
  // instance from prototype factory
  public static NewAirSelfPropulsionPG newAirSelfPropulsionPG(AirSelfPropulsionPG prototype) {
    return new AirSelfPropulsionPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewMilitaryPersonPG newMilitaryPersonPG() {
    return new MilitaryPersonPGImpl();
  }
  // instance from prototype factory
  public static NewMilitaryPersonPG newMilitaryPersonPG(MilitaryPersonPG prototype) {
    return new MilitaryPersonPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewAirportPG newAirportPG() {
    return new AirportPGImpl();
  }
  // instance from prototype factory
  public static NewAirportPG newAirportPG(AirportPG prototype) {
    return new AirportPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewSeaTransportationPG newSeaTransportationPG() {
    return new SeaTransportationPGImpl();
  }
  // instance from prototype factory
  public static NewSeaTransportationPG newSeaTransportationPG(SeaTransportationPG prototype) {
    return new SeaTransportationPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewWaterSelfPropulsionPG newWaterSelfPropulsionPG() {
    return new WaterSelfPropulsionPGImpl();
  }
  // instance from prototype factory
  public static NewWaterSelfPropulsionPG newWaterSelfPropulsionPG(WaterSelfPropulsionPG prototype) {
    return new WaterSelfPropulsionPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewPersonnelReadinessPG newPersonnelReadinessPG() {
    return new PersonnelReadinessPGImpl();
  }
  // instance from prototype factory
  public static NewPersonnelReadinessPG newPersonnelReadinessPG(PersonnelReadinessPG prototype) {
    return new PersonnelReadinessPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewConditionPG newConditionPG() {
    return new ConditionPGImpl();
  }
  // instance from prototype factory
  public static NewConditionPG newConditionPG(ConditionPG prototype) {
    return new ConditionPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewWaterSupplyPG newWaterSupplyPG() {
    return new WaterSupplyPGImpl();
  }
  // instance from prototype factory
  public static NewWaterSupplyPG newWaterSupplyPG(WaterSupplyPG prototype) {
    return new WaterSupplyPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewScheduledContentPG newScheduledContentPG() {
    return new ScheduledContentPGImpl();
  }
  // instance from prototype factory
  public static NewScheduledContentPG newScheduledContentPG(ScheduledContentPG prototype) {
    return new ScheduledContentPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewAirLiftPG newAirLiftPG() {
    return new AirLiftPGImpl();
  }
  // instance from prototype factory
  public static NewAirLiftPG newAirLiftPG(AirLiftPG prototype) {
    return new AirLiftPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewRailTransportationPG newRailTransportationPG() {
    return new RailTransportationPGImpl();
  }
  // instance from prototype factory
  public static NewRailTransportationPG newRailTransportationPG(RailTransportationPG prototype) {
    return new RailTransportationPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewSupplyClassPG newSupplyClassPG() {
    return new SupplyClassPGImpl();
  }
  // instance from prototype factory
  public static NewSupplyClassPG newSupplyClassPG(SupplyClassPG prototype) {
    return new SupplyClassPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewContainPG newContainPG() {
    return new ContainPGImpl();
  }
  // instance from prototype factory
  public static NewContainPG newContainPG(ContainPG prototype) {
    return new ContainPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewOnRoadTransportationPG newOnRoadTransportationPG() {
    return new OnRoadTransportationPGImpl();
  }
  // instance from prototype factory
  public static NewOnRoadTransportationPG newOnRoadTransportationPG(OnRoadTransportationPG prototype) {
    return new OnRoadTransportationPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewPositionPG newPositionPG() {
    return new PositionPGImpl();
  }
  // instance from prototype factory
  public static NewPositionPG newPositionPG(PositionPG prototype) {
    return new PositionPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewMovabilityPG newMovabilityPG() {
    return new MovabilityPGImpl();
  }
  // instance from prototype factory
  public static NewMovabilityPG newMovabilityPG(MovabilityPG prototype) {
    return new MovabilityPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewGroundVehiclePG newGroundVehiclePG() {
    return new GroundVehiclePGImpl();
  }
  // instance from prototype factory
  public static NewGroundVehiclePG newGroundVehiclePG(GroundVehiclePG prototype) {
    return new GroundVehiclePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewAmmunitionPG newAmmunitionPG() {
    return new AmmunitionPGImpl();
  }
  // instance from prototype factory
  public static NewAmmunitionPG newAmmunitionPG(AmmunitionPG prototype) {
    return new AmmunitionPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewAssignmentPG newAssignmentPG() {
    return new AssignmentPGImpl();
  }
  // instance from prototype factory
  public static NewAssignmentPG newAssignmentPG(AssignmentPG prototype) {
    return new AssignmentPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewBulkSolidPG newBulkSolidPG() {
    return new BulkSolidPGImpl();
  }
  // instance from prototype factory
  public static NewBulkSolidPG newBulkSolidPG(BulkSolidPG prototype) {
    return new BulkSolidPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewAirLinkPG newAirLinkPG() {
    return new AirLinkPGImpl();
  }
  // instance from prototype factory
  public static NewAirLinkPG newAirLinkPG(AirLinkPG prototype) {
    return new AirLinkPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewAirConditionPG newAirConditionPG() {
    return new AirConditionPGImpl();
  }
  // instance from prototype factory
  public static NewAirConditionPG newAirConditionPG(AirConditionPG prototype) {
    return new AirConditionPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewPhysicalPG newPhysicalPG() {
    return new PhysicalPGImpl();
  }
  // instance from prototype factory
  public static NewPhysicalPG newPhysicalPG(PhysicalPG prototype) {
    return new PhysicalPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewSupplyPG newSupplyPG() {
    return new SupplyPGImpl();
  }
  // instance from prototype factory
  public static NewSupplyPG newSupplyPG(SupplyPG prototype) {
    return new SupplyPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewShipConfigurationPG newShipConfigurationPG() {
    return new ShipConfigurationPGImpl();
  }
  // instance from prototype factory
  public static NewShipConfigurationPG newShipConfigurationPG(ShipConfigurationPG prototype) {
    return new ShipConfigurationPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewMaintenancePG newMaintenancePG() {
    return new MaintenancePGImpl();
  }
  // instance from prototype factory
  public static NewMaintenancePG newMaintenancePG(MaintenancePG prototype) {
    return new MaintenancePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewPersonSustainmentPG newPersonSustainmentPG() {
    return new PersonSustainmentPGImpl();
  }
  // instance from prototype factory
  public static NewPersonSustainmentPG newPersonSustainmentPG(PersonSustainmentPG prototype) {
    return new PersonSustainmentPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewRepairabilityPG newRepairabilityPG() {
    return new RepairabilityPGImpl();
  }
  // instance from prototype factory
  public static NewRepairabilityPG newRepairabilityPG(RepairabilityPG prototype) {
    return new RepairabilityPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewRepairablePG newRepairablePG() {
    return new RepairablePGImpl();
  }
  // instance from prototype factory
  public static NewRepairablePG newRepairablePG(RepairablePG prototype) {
    return new RepairablePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewExplosivePG newExplosivePG() {
    return new ExplosivePGImpl();
  }
  // instance from prototype factory
  public static NewExplosivePG newExplosivePG(ExplosivePG prototype) {
    return new ExplosivePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewGroundSelfPropulsionPG newGroundSelfPropulsionPG() {
    return new GroundSelfPropulsionPGImpl();
  }
  // instance from prototype factory
  public static NewGroundSelfPropulsionPG newGroundSelfPropulsionPG(GroundSelfPropulsionPG prototype) {
    return new GroundSelfPropulsionPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewPersonPG newPersonPG() {
    return new PersonPGImpl();
  }
  // instance from prototype factory
  public static NewPersonPG newPersonPG(PersonPG prototype) {
    return new PersonPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewMEIPG newMEIPG() {
    return new MEIPGImpl();
  }
  // instance from prototype factory
  public static NewMEIPG newMEIPG(MEIPG prototype) {
    return new MEIPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewContentsPG newContentsPG() {
    return new ContentsPGImpl();
  }
  // instance from prototype factory
  public static NewContentsPG newContentsPG(ContentsPG prototype) {
    return new ContentsPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewManagedAssetPG newManagedAssetPG() {
    return new ManagedAssetPGImpl();
  }
  // instance from prototype factory
  public static NewManagedAssetPG newManagedAssetPG(ManagedAssetPG prototype) {
    return new ManagedAssetPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewEquipmentOHReadinessPG newEquipmentOHReadinessPG() {
    return new EquipmentOHReadinessPGImpl();
  }
  // instance from prototype factory
  public static NewEquipmentOHReadinessPG newEquipmentOHReadinessPG(EquipmentOHReadinessPG prototype) {
    return new EquipmentOHReadinessPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewLiquidPG newLiquidPG() {
    return new LiquidPGImpl();
  }
  // instance from prototype factory
  public static NewLiquidPG newLiquidPG(LiquidPG prototype) {
    return new LiquidPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewVolumetricStockagePG newVolumetricStockagePG() {
    return new VolumetricStockagePGImpl();
  }
  // instance from prototype factory
  public static NewVolumetricStockagePG newVolumetricStockagePG(VolumetricStockagePG prototype) {
    return new VolumetricStockagePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewOrganizationPG newOrganizationPG() {
    return new OrganizationPGImpl();
  }
  // instance from prototype factory
  public static NewOrganizationPG newOrganizationPG(OrganizationPG prototype) {
    return new OrganizationPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewWeaponPG newWeaponPG() {
    return new WeaponPGImpl();
  }
  // instance from prototype factory
  public static NewWeaponPG newWeaponPG(WeaponPG prototype) {
    return new WeaponPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewTowPG newTowPG() {
    return new TowPGImpl();
  }
  // instance from prototype factory
  public static NewTowPG newTowPG(TowPG prototype) {
    return new TowPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewLiftPG newLiftPG() {
    return new LiftPGImpl();
  }
  // instance from prototype factory
  public static NewLiftPG newLiftPG(LiftPG prototype) {
    return new LiftPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewRoadLinkPG newRoadLinkPG() {
    return new RoadLinkPGImpl();
  }
  // instance from prototype factory
  public static NewRoadLinkPG newRoadLinkPG(RoadLinkPG prototype) {
    return new RoadLinkPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewSeaportPG newSeaportPG() {
    return new SeaportPGImpl();
  }
  // instance from prototype factory
  public static NewSeaportPG newSeaportPG(SeaportPG prototype) {
    return new SeaportPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewReportSchedulePG newReportSchedulePG() {
    return new ReportSchedulePGImpl();
  }
  // instance from prototype factory
  public static NewReportSchedulePG newReportSchedulePG(ReportSchedulePG prototype) {
    return new ReportSchedulePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewSupportPG newSupportPG() {
    return new SupportPGImpl();
  }
  // instance from prototype factory
  public static NewSupportPG newSupportPG(SupportPG prototype) {
    return new SupportPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewDetailKeyPG newDetailKeyPG() {
    return new DetailKeyPGImpl();
  }
  // instance from prototype factory
  public static NewDetailKeyPG newDetailKeyPG(DetailKeyPG prototype) {
    return new DetailKeyPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewLandConditionPG newLandConditionPG() {
    return new LandConditionPGImpl();
  }
  // instance from prototype factory
  public static NewLandConditionPG newLandConditionPG(LandConditionPG prototype) {
    return new LandConditionPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewRepairDepotPG newRepairDepotPG() {
    return new RepairDepotPGImpl();
  }
  // instance from prototype factory
  public static NewRepairDepotPG newRepairDepotPG(RepairDepotPG prototype) {
    return new RepairDepotPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewInventoryPG newInventoryPG() {
    return new InventoryPGImpl();
  }
  // instance from prototype factory
  public static NewInventoryPG newInventoryPG(InventoryPG prototype) {
    return new InventoryPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewSeaConditionPG newSeaConditionPG() {
    return new SeaConditionPGImpl();
  }
  // instance from prototype factory
  public static NewSeaConditionPG newSeaConditionPG(SeaConditionPG prototype) {
    return new SeaConditionPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewRailSelfPropulsionPG newRailSelfPropulsionPG() {
    return new RailSelfPropulsionPGImpl();
  }
  // instance from prototype factory
  public static NewRailSelfPropulsionPG newRailSelfPropulsionPG(RailSelfPropulsionPG prototype) {
    return new RailSelfPropulsionPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewCSSCapabilityPG newCSSCapabilityPG() {
    return new CSSCapabilityPGImpl();
  }
  // instance from prototype factory
  public static NewCSSCapabilityPG newCSSCapabilityPG(CSSCapabilityPG prototype) {
    return new CSSCapabilityPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewMidAirRefuelPG newMidAirRefuelPG() {
    return new MidAirRefuelPGImpl();
  }
  // instance from prototype factory
  public static NewMidAirRefuelPG newMidAirRefuelPG(MidAirRefuelPG prototype) {
    return new MidAirRefuelPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewInventoryLevelsPG newInventoryLevelsPG() {
    return new InventoryLevelsPGImpl();
  }
  // instance from prototype factory
  public static NewInventoryLevelsPG newInventoryLevelsPG(InventoryLevelsPG prototype) {
    return new InventoryLevelsPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewVehiclePropertyGroups newVehiclePropertyGroups() {
    return new VehiclePropertyGroupsImpl();
  }
  // instance from prototype factory
  public static NewVehiclePropertyGroups newVehiclePropertyGroups(VehiclePropertyGroups prototype) {
    return new VehiclePropertyGroupsImpl(prototype);
  }

  // brand-new instance factory
  public static NewTrainingReadinessPG newTrainingReadinessPG() {
    return new TrainingReadinessPGImpl();
  }
  // instance from prototype factory
  public static NewTrainingReadinessPG newTrainingReadinessPG(TrainingReadinessPG prototype) {
    return new TrainingReadinessPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewRailLinkPG newRailLinkPG() {
    return new RailLinkPGImpl();
  }
  // instance from prototype factory
  public static NewRailLinkPG newRailLinkPG(RailLinkPG prototype) {
    return new RailLinkPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewLiquidSupplyPG newLiquidSupplyPG() {
    return new LiquidSupplyPGImpl();
  }
  // instance from prototype factory
  public static NewLiquidSupplyPG newLiquidSupplyPG(LiquidSupplyPG prototype) {
    return new LiquidSupplyPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewDetailedScheduledContentPG newDetailedScheduledContentPG() {
    return new DetailedScheduledContentPGImpl();
  }
  // instance from prototype factory
  public static NewDetailedScheduledContentPG newDetailedScheduledContentPG(DetailedScheduledContentPG prototype) {
    return new DetailedScheduledContentPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewOffRoadTransportationPG newOffRoadTransportationPG() {
    return new OffRoadTransportationPGImpl();
  }
  // instance from prototype factory
  public static NewOffRoadTransportationPG newOffRoadTransportationPG(OffRoadTransportationPG prototype) {
    return new OffRoadTransportationPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewAssetConsumptionRatePG newAssetConsumptionRatePG() {
    return new AssetConsumptionRatePGImpl();
  }
  // instance from prototype factory
  public static NewAssetConsumptionRatePG newAssetConsumptionRatePG(AssetConsumptionRatePG prototype) {
    return new AssetConsumptionRatePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewRailVehiclePG newRailVehiclePG() {
    return new RailVehiclePGImpl();
  }
  // instance from prototype factory
  public static NewRailVehiclePG newRailVehiclePG(RailVehiclePG prototype) {
    return new RailVehiclePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewFacilityPG newFacilityPG() {
    return new FacilityPGImpl();
  }
  // instance from prototype factory
  public static NewFacilityPG newFacilityPG(FacilityPG prototype) {
    return new FacilityPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewMissileLauncherPG newMissileLauncherPG() {
    return new MissileLauncherPGImpl();
  }
  // instance from prototype factory
  public static NewMissileLauncherPG newMissileLauncherPG(MissileLauncherPG prototype) {
    return new MissileLauncherPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewAirVehiclePG newAirVehiclePG() {
    return new AirVehiclePGImpl();
  }
  // instance from prototype factory
  public static NewAirVehiclePG newAirVehiclePG(AirVehiclePG prototype) {
    return new AirVehiclePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewDeckPG newDeckPG() {
    return new DeckPGImpl();
  }
  // instance from prototype factory
  public static NewDeckPG newDeckPG(DeckPG prototype) {
    return new DeckPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewEquipmentStatusReadinessPG newEquipmentStatusReadinessPG() {
    return new EquipmentStatusReadinessPGImpl();
  }
  // instance from prototype factory
  public static NewEquipmentStatusReadinessPG newEquipmentStatusReadinessPG(EquipmentStatusReadinessPG prototype) {
    return new EquipmentStatusReadinessPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewWaterVehiclePG newWaterVehiclePG() {
    return new WaterVehiclePGImpl();
  }
  // instance from prototype factory
  public static NewWaterVehiclePG newWaterVehiclePG(WaterVehiclePG prototype) {
    return new WaterVehiclePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewWarheadPG newWarheadPG() {
    return new WarheadPGImpl();
  }
  // instance from prototype factory
  public static NewWarheadPG newWarheadPG(WarheadPG prototype) {
    return new WarheadPGImpl(prototype);
  }

  // brand-new instance factory
  public static NewPackagePG newPackagePG() {
    return new PackagePGImpl();
  }
  // instance from prototype factory
  public static NewPackagePG newPackagePG(PackagePG prototype) {
    return new PackagePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewConsumablePG newConsumablePG() {
    return new ConsumablePGImpl();
  }
  // instance from prototype factory
  public static NewConsumablePG newConsumablePG(ConsumablePG prototype) {
    return new ConsumablePGImpl(prototype);
  }

  // brand-new instance factory
  public static NewFoodPG newFoodPG() {
    return new FoodPGImpl();
  }
  // instance from prototype factory
  public static NewFoodPG newFoodPG(FoodPG prototype) {
    return new FoodPGImpl(prototype);
  }

  /** Abstract introspection information.
   * Tuples are {<classname>, <factorymethodname>}
   * return value of <factorymethodname> is <classname>.
   * <factorymethodname> takes zero or one (prototype) argument.
   **/
  public static String properties[][]={
    {"org.cougaar.glm.ldm.asset.CargoFacilityPG", "newCargoFacilityPG"},
    {"org.cougaar.glm.ldm.asset.SeaLinkPG", "newSeaLinkPG"},
    {"org.cougaar.glm.ldm.asset.AirTransportationPG", "newAirTransportationPG"},
    {"org.cougaar.glm.ldm.asset.SupplyDepotPG", "newSupplyDepotPG"},
    {"org.cougaar.glm.ldm.asset.TruckTerminalPG", "newTruckTerminalPG"},
    {"org.cougaar.glm.ldm.asset.MilitaryOrgPG", "newMilitaryOrgPG"},
    {"org.cougaar.glm.ldm.asset.RailTerminalPG", "newRailTerminalPG"},
    {"org.cougaar.glm.ldm.asset.FuelPG", "newFuelPG"},
    {"org.cougaar.glm.ldm.asset.AssignedPG", "newAssignedPG"},
    {"org.cougaar.glm.ldm.asset.FuelSupplyPG", "newFuelSupplyPG"},
    {"org.cougaar.glm.ldm.asset.ForUnitPG", "newForUnitPG"},
    {"org.cougaar.glm.ldm.asset.FromBasePG", "newFromBasePG"},
    {"org.cougaar.glm.ldm.asset.TransportationPG", "newTransportationPG"},
    {"org.cougaar.glm.ldm.asset.CostPG", "newCostPG"},
    {"org.cougaar.glm.ldm.asset.WaterPG", "newWaterPG"},
    {"org.cougaar.glm.ldm.asset.AirSelfPropulsionPG", "newAirSelfPropulsionPG"},
    {"org.cougaar.glm.ldm.asset.MilitaryPersonPG", "newMilitaryPersonPG"},
    {"org.cougaar.glm.ldm.asset.AirportPG", "newAirportPG"},
    {"org.cougaar.glm.ldm.asset.SeaTransportationPG", "newSeaTransportationPG"},
    {"org.cougaar.glm.ldm.asset.WaterSelfPropulsionPG", "newWaterSelfPropulsionPG"},
    {"org.cougaar.glm.ldm.asset.PersonnelReadinessPG", "newPersonnelReadinessPG"},
    {"org.cougaar.glm.ldm.asset.ConditionPG", "newConditionPG"},
    {"org.cougaar.glm.ldm.asset.WaterSupplyPG", "newWaterSupplyPG"},
    {"org.cougaar.glm.ldm.asset.ScheduledContentPG", "newScheduledContentPG"},
    {"org.cougaar.glm.ldm.asset.AirLiftPG", "newAirLiftPG"},
    {"org.cougaar.glm.ldm.asset.RailTransportationPG", "newRailTransportationPG"},
    {"org.cougaar.glm.ldm.asset.SupplyClassPG", "newSupplyClassPG"},
    {"org.cougaar.glm.ldm.asset.ContainPG", "newContainPG"},
    {"org.cougaar.glm.ldm.asset.OnRoadTransportationPG", "newOnRoadTransportationPG"},
    {"org.cougaar.glm.ldm.asset.PositionPG", "newPositionPG"},
    {"org.cougaar.glm.ldm.asset.MovabilityPG", "newMovabilityPG"},
    {"org.cougaar.glm.ldm.asset.GroundVehiclePG", "newGroundVehiclePG"},
    {"org.cougaar.glm.ldm.asset.AmmunitionPG", "newAmmunitionPG"},
    {"org.cougaar.glm.ldm.asset.AssignmentPG", "newAssignmentPG"},
    {"org.cougaar.glm.ldm.asset.BulkSolidPG", "newBulkSolidPG"},
    {"org.cougaar.glm.ldm.asset.AirLinkPG", "newAirLinkPG"},
    {"org.cougaar.glm.ldm.asset.AirConditionPG", "newAirConditionPG"},
    {"org.cougaar.glm.ldm.asset.PhysicalPG", "newPhysicalPG"},
    {"org.cougaar.glm.ldm.asset.SupplyPG", "newSupplyPG"},
    {"org.cougaar.glm.ldm.asset.ShipConfigurationPG", "newShipConfigurationPG"},
    {"org.cougaar.glm.ldm.asset.MaintenancePG", "newMaintenancePG"},
    {"org.cougaar.glm.ldm.asset.PersonSustainmentPG", "newPersonSustainmentPG"},
    {"org.cougaar.glm.ldm.asset.RepairabilityPG", "newRepairabilityPG"},
    {"org.cougaar.glm.ldm.asset.RepairablePG", "newRepairablePG"},
    {"org.cougaar.glm.ldm.asset.ExplosivePG", "newExplosivePG"},
    {"org.cougaar.glm.ldm.asset.GroundSelfPropulsionPG", "newGroundSelfPropulsionPG"},
    {"org.cougaar.glm.ldm.asset.PersonPG", "newPersonPG"},
    {"org.cougaar.glm.ldm.asset.MEIPG", "newMEIPG"},
    {"org.cougaar.glm.ldm.asset.ContentsPG", "newContentsPG"},
    {"org.cougaar.glm.ldm.asset.ManagedAssetPG", "newManagedAssetPG"},
    {"org.cougaar.glm.ldm.asset.EquipmentOHReadinessPG", "newEquipmentOHReadinessPG"},
    {"org.cougaar.glm.ldm.asset.LiquidPG", "newLiquidPG"},
    {"org.cougaar.glm.ldm.asset.VolumetricStockagePG", "newVolumetricStockagePG"},
    {"org.cougaar.glm.ldm.asset.OrganizationPG", "newOrganizationPG"},
    {"org.cougaar.glm.ldm.asset.WeaponPG", "newWeaponPG"},
    {"org.cougaar.glm.ldm.asset.TowPG", "newTowPG"},
    {"org.cougaar.glm.ldm.asset.LiftPG", "newLiftPG"},
    {"org.cougaar.glm.ldm.asset.RoadLinkPG", "newRoadLinkPG"},
    {"org.cougaar.glm.ldm.asset.SeaportPG", "newSeaportPG"},
    {"org.cougaar.glm.ldm.asset.ReportSchedulePG", "newReportSchedulePG"},
    {"org.cougaar.glm.ldm.asset.SupportPG", "newSupportPG"},
    {"org.cougaar.glm.ldm.asset.DetailKeyPG", "newDetailKeyPG"},
    {"org.cougaar.glm.ldm.asset.LandConditionPG", "newLandConditionPG"},
    {"org.cougaar.glm.ldm.asset.RepairDepotPG", "newRepairDepotPG"},
    {"org.cougaar.glm.ldm.asset.InventoryPG", "newInventoryPG"},
    {"org.cougaar.glm.ldm.asset.SeaConditionPG", "newSeaConditionPG"},
    {"org.cougaar.glm.ldm.asset.RailSelfPropulsionPG", "newRailSelfPropulsionPG"},
    {"org.cougaar.glm.ldm.asset.CSSCapabilityPG", "newCSSCapabilityPG"},
    {"org.cougaar.glm.ldm.asset.MidAirRefuelPG", "newMidAirRefuelPG"},
    {"org.cougaar.glm.ldm.asset.InventoryLevelsPG", "newInventoryLevelsPG"},
    {"org.cougaar.glm.ldm.asset.VehiclePropertyGroups", "newVehiclePropertyGroups"},
    {"org.cougaar.glm.ldm.asset.TrainingReadinessPG", "newTrainingReadinessPG"},
    {"org.cougaar.glm.ldm.asset.RailLinkPG", "newRailLinkPG"},
    {"org.cougaar.glm.ldm.asset.LiquidSupplyPG", "newLiquidSupplyPG"},
    {"org.cougaar.glm.ldm.asset.DetailedScheduledContentPG", "newDetailedScheduledContentPG"},
    {"org.cougaar.glm.ldm.asset.OffRoadTransportationPG", "newOffRoadTransportationPG"},
    {"org.cougaar.glm.ldm.asset.AssetConsumptionRatePG", "newAssetConsumptionRatePG"},
    {"org.cougaar.glm.ldm.asset.RailVehiclePG", "newRailVehiclePG"},
    {"org.cougaar.glm.ldm.asset.FacilityPG", "newFacilityPG"},
    {"org.cougaar.glm.ldm.asset.MissileLauncherPG", "newMissileLauncherPG"},
    {"org.cougaar.glm.ldm.asset.AirVehiclePG", "newAirVehiclePG"},
    {"org.cougaar.glm.ldm.asset.DeckPG", "newDeckPG"},
    {"org.cougaar.glm.ldm.asset.EquipmentStatusReadinessPG", "newEquipmentStatusReadinessPG"},
    {"org.cougaar.glm.ldm.asset.WaterVehiclePG", "newWaterVehiclePG"},
    {"org.cougaar.glm.ldm.asset.WarheadPG", "newWarheadPG"},
    {"org.cougaar.glm.ldm.asset.PackagePG", "newPackagePG"},
    {"org.cougaar.glm.ldm.asset.ConsumablePG", "newConsumablePG"},
    {"org.cougaar.glm.ldm.asset.FoodPG", "newFoodPG"},
  };
}
