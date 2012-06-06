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
import org.cougaar.planning.ldm.asset.EssentialAssetFactory;

public class AssetFactory extends EssentialAssetFactory {
  public static String[] assets = {
    "org.cougaar.glm.ldm.asset.Inventory",
    "org.cougaar.glm.ldm.asset.VolumetricInventory",
    "org.cougaar.glm.ldm.asset.Capacity",
    "org.cougaar.glm.ldm.asset.Ammunition",
    "org.cougaar.glm.ldm.asset.Barge",
    "org.cougaar.glm.ldm.asset.BulkPOL",
    "org.cougaar.glm.ldm.asset.BulkWater",
    "org.cougaar.glm.ldm.asset.CargoFixedWingAircraft",
    "org.cougaar.glm.ldm.asset.CargoLoad",
    "org.cougaar.glm.ldm.asset.CargoRotaryWingAircraft",
    "org.cougaar.glm.ldm.asset.CargoShip",
    "org.cougaar.glm.ldm.asset.CargoVehicle",
    "org.cougaar.glm.ldm.asset.ClassIIClothingAndEquipment",
    "org.cougaar.glm.ldm.asset.ClassIIIPOL",
    "org.cougaar.glm.ldm.asset.ClassISubsistence",
    "org.cougaar.glm.ldm.asset.ClassIVConstructionMaterial",
    "org.cougaar.glm.ldm.asset.ClassIXRepairPart",
    "org.cougaar.glm.ldm.asset.ClassVAmmunition",
    "org.cougaar.glm.ldm.asset.ClassVIIIMedical",
    "org.cougaar.glm.ldm.asset.ClassVIIMajorEndItem",
    "org.cougaar.glm.ldm.asset.ClassVIPersonalDemandItem",
    "org.cougaar.glm.ldm.asset.ClassXNonMilitaryItem",
    "org.cougaar.glm.ldm.asset.Clothing",
    "org.cougaar.glm.ldm.asset.Consumable",
    "org.cougaar.glm.ldm.asset.Container",
    "org.cougaar.glm.ldm.asset.Package",
    "org.cougaar.glm.ldm.asset.Convoy",
    "org.cougaar.glm.ldm.asset.Deck",
    "org.cougaar.glm.ldm.asset.Explosive",
    "org.cougaar.glm.ldm.asset.Facility",
    "org.cougaar.glm.ldm.asset.FightingShip",
    "org.cougaar.glm.ldm.asset.FixedWingAircraftWeapon",
    "org.cougaar.glm.ldm.asset.Food",
    "org.cougaar.glm.ldm.asset.LoadPlan",
    "org.cougaar.glm.ldm.asset.Manifest",
    "org.cougaar.glm.ldm.asset.LightweightManifest",
    "org.cougaar.glm.ldm.asset.MilitaryPerson",
    "org.cougaar.glm.ldm.asset.Missile",
    "org.cougaar.glm.ldm.asset.MissileLauncher",
    "org.cougaar.glm.ldm.asset.ClientOrganization",
    "org.cougaar.glm.ldm.asset.Organization",
    "org.cougaar.glm.ldm.asset.MilitaryOrganization",
    "org.cougaar.glm.ldm.asset.OtherIndividualEquipment",
    "org.cougaar.glm.ldm.asset.OtherMajorEndItem",
    "org.cougaar.glm.ldm.asset.OtherWeapon",
    "org.cougaar.glm.ldm.asset.PackagedPOL",
    "org.cougaar.glm.ldm.asset.Person",
    "org.cougaar.glm.ldm.asset.PhysicalAsset",
    "org.cougaar.glm.ldm.asset.RailCar",
    "org.cougaar.glm.ldm.asset.Repairable",
    "org.cougaar.glm.ldm.asset.RotaryWingAircraftWeapon",
    "org.cougaar.glm.ldm.asset.SelfPropelledGroundWeapon",
    "org.cougaar.glm.ldm.asset.SmallArms",
    "org.cougaar.glm.ldm.asset.TowedGroundWeapon",
    "org.cougaar.glm.ldm.asset.Trailer",
    "org.cougaar.glm.ldm.asset.Train",
    "org.cougaar.glm.ldm.asset.TrainEngine",
    "org.cougaar.glm.ldm.asset.TransportationAirLink",
    "org.cougaar.glm.ldm.asset.TransportationLinkPoint",
    "org.cougaar.glm.ldm.asset.TransportationRoadLink",
    "org.cougaar.glm.ldm.asset.TransportationRailLink",
    "org.cougaar.glm.ldm.asset.TransportationSeaLink",
    "org.cougaar.glm.ldm.asset.Truck",
    "org.cougaar.glm.ldm.asset.Warhead",
    "org.cougaar.glm.ldm.asset.Weapon",
    "org.cougaar.glm.ldm.asset.RadarSystem",
    "org.cougaar.glm.ldm.asset.NavyRadarSystem",
    "org.cougaar.glm.ldm.asset.VerticalLaunchSystem",
    "org.cougaar.glm.ldm.asset.DigitalNetworkSystem",
    "org.cougaar.glm.ldm.asset.CommSystem",
    "org.cougaar.glm.ldm.asset.FuelSystem",
    "org.cougaar.glm.ldm.asset.ElectronicBoard",
    "org.cougaar.glm.ldm.asset.HumanitarianDailyRation",
    "org.cougaar.glm.ldm.asset.BottledWater",
  };
}
