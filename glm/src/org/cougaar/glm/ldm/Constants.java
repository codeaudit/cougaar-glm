/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.glm.ldm;

import org.cougaar.planning.ldm.plan.AspectType;

public class Constants implements org.cougaar.planning.Constants {
  private Constants() {}

  public static final String DSMAINTENANCE = "DSMaintenance";
  public static final String DURING = "During";
  public static final String FOOD = "Food";
  public static final String GSMAINTENANCE = "GSMaintenance";
  public static final String WATER = "Water";

  public interface Verb extends org.cougaar.planning.Constants.Verb {
    // ALPINE defined verb types
    // Keep in alphabetical order
    String ACCOUNTFORSUPPLY = "AccountForSupply";
    String ARM = "Arm";
    String ASSESSREADINESS = "AssessReadiness";
    String BULKESTIMATE = "BulkEstimate";
    String DETERMINEREQUIREMENTS = "DetermineRequirements";
    String ESTABLISHMISSIONREQUIREMENTS = "EstablishMissionRequirements";
    String EXECUTE = "Execute";
    String FUEL = "Fuel";
    String GENERATEAEF = "GenerateAEF";
    String GENERATEPROJECTIONS = "GenerateProjections";
    String GETLOGSUPPORT = "GetLogSupport";
    String IDLE = "Idle";
    String INFORM = "Inform";
    String LOAD = "Load";
    String MAINTAIN = "Maintain";
    String MAINTAININVENTORY = "MaintainInventory";
    String MANAGE = "Manage";
    String PREPAREFORTRANSPORT = "PrepareForTransport";
    String PRODUCEWATER = "ProduceWater";
    String PROJECTWITHDRAW = "ProjectWithdraw";
    String PROVIDEFOOD = "ProvideFood";
    String PROVIDEWATER = "ProvideWater";
    String QUARTERMASTERHANDLING = "QuartermasterHandling";
    String REPORTFORDUTY = "ReportForDuty";
    String REPORTFORSERVICE = "ReportForService";
    String REPORTREADINESS = "ReportReadiness";
    String REQUESTMAINTENANCESUPPORT =
      "RequestMaintenanceSupport";
    String SUPPLY = "Supply";
    String PROJECTSUPPLY = "ProjectSupply";
    String SUPPLYAIRCRAFT = "SupplyAircraft";
    String SUPPLYFOOD = "SupplyFood";
    String SUPPLYSTATUSINQUIRY = "SupplyStatusInquiry";
    String SUPPLYWATER = "SupplyWater";
    String SUPPORTREQUEST = "SupportRequest";
    String TRANSIT = "Transit"; // move yourself
    String TRANSPORT = "Transport";
    String TRANSPORTALERT = "TransportAlert";
    String TRANSPORTATIONMISSION = "TransportationMission";
    String UNLOAD = "Unload";
    String USERINPUT = "UserInput";
    String WITHDRAW = "Withdraw";

    org.cougaar.planning.ldm.plan.Verb AccountForSupply = org.cougaar.planning.ldm.plan.Verb.getVerb("AccountForSupply");
    org.cougaar.planning.ldm.plan.Verb Arm = org.cougaar.planning.ldm.plan.Verb.getVerb("Arm");
    org.cougaar.planning.ldm.plan.Verb AssessReadiness = org.cougaar.planning.ldm.plan.Verb.getVerb("AssessReadiness");
    org.cougaar.planning.ldm.plan.Verb BulkEstimate = org.cougaar.planning.ldm.plan.Verb.getVerb("BulkEstimate");
    org.cougaar.planning.ldm.plan.Verb DetermineRequirements = org.cougaar.planning.ldm.plan.Verb.getVerb("DetermineRequirements");
    org.cougaar.planning.ldm.plan.Verb EstablishMissionRequirements = org.cougaar.planning.ldm.plan.Verb.getVerb("EstablishMissionRequirements");
    org.cougaar.planning.ldm.plan.Verb Execute = org.cougaar.planning.ldm.plan.Verb.getVerb("Execute");
    org.cougaar.planning.ldm.plan.Verb Fuel = org.cougaar.planning.ldm.plan.Verb.getVerb("Fuel");
    org.cougaar.planning.ldm.plan.Verb GetLogSupport = org.cougaar.planning.ldm.plan.Verb.getVerb("GetLogSupport");
    org.cougaar.planning.ldm.plan.Verb GenerateAEF = org.cougaar.planning.ldm.plan.Verb.getVerb("GenerateAEF");
    org.cougaar.planning.ldm.plan.Verb GenerateProjections = org.cougaar.planning.ldm.plan.Verb.getVerb("GenerateProjections");
    org.cougaar.planning.ldm.plan.Verb Idle = org.cougaar.planning.ldm.plan.Verb.getVerb("Idle");
    org.cougaar.planning.ldm.plan.Verb Inform = org.cougaar.planning.ldm.plan.Verb.getVerb("Inform");
    org.cougaar.planning.ldm.plan.Verb Load = org.cougaar.planning.ldm.plan.Verb.getVerb("Load");
    org.cougaar.planning.ldm.plan.Verb Maintain = org.cougaar.planning.ldm.plan.Verb.getVerb("Maintain");
    org.cougaar.planning.ldm.plan.Verb MaintainInventory = org.cougaar.planning.ldm.plan.Verb.getVerb("MaintainInventory");
    org.cougaar.planning.ldm.plan.Verb Manage = org.cougaar.planning.ldm.plan.Verb.getVerb("Manage");
    org.cougaar.planning.ldm.plan.Verb PrepareForTransport = org.cougaar.planning.ldm.plan.Verb.getVerb("PrepareForTransport");
    org.cougaar.planning.ldm.plan.Verb ProduceWater = org.cougaar.planning.ldm.plan.Verb.getVerb("ProduceWater");
    org.cougaar.planning.ldm.plan.Verb ProjectWithdraw = org.cougaar.planning.ldm.plan.Verb.getVerb("ProjectWithdraw");
    org.cougaar.planning.ldm.plan.Verb ProvideFood = org.cougaar.planning.ldm.plan.Verb.getVerb("ProvideFood");
    org.cougaar.planning.ldm.plan.Verb ProvideWater = org.cougaar.planning.ldm.plan.Verb.getVerb("ProvideWater");
    org.cougaar.planning.ldm.plan.Verb QuartermasterHandling = org.cougaar.planning.ldm.plan.Verb.getVerb("QuartermasterHandling");
    org.cougaar.planning.ldm.plan.Verb ReportForDuty = org.cougaar.planning.ldm.plan.Verb.getVerb("ReportForDuty");
    org.cougaar.planning.ldm.plan.Verb ReportForService = org.cougaar.planning.ldm.plan.Verb.getVerb("ReportForService");
    org.cougaar.planning.ldm.plan.Verb ReportReadiness = org.cougaar.planning.ldm.plan.Verb.getVerb("ReportReadiness");
    org.cougaar.planning.ldm.plan.Verb RequestMaintenanceSupport = org.cougaar.planning.ldm.plan.Verb.getVerb("RequestMaintenanceSupport");
    org.cougaar.planning.ldm.plan.Verb Supply = org.cougaar.planning.ldm.plan.Verb.getVerb("Supply");
    org.cougaar.planning.ldm.plan.Verb ProjectSupply = org.cougaar.planning.ldm.plan.Verb.getVerb("ProjectSupply");
    org.cougaar.planning.ldm.plan.Verb SupplyAircraft = org.cougaar.planning.ldm.plan.Verb.getVerb("SupplyAircraft");
    org.cougaar.planning.ldm.plan.Verb SupplyFood = org.cougaar.planning.ldm.plan.Verb.getVerb("SupplyFood");
    org.cougaar.planning.ldm.plan.Verb SupplyStatusInquiry = org.cougaar.planning.ldm.plan.Verb.getVerb("SupplyStatusInquiry");
    org.cougaar.planning.ldm.plan.Verb SupplyWater = org.cougaar.planning.ldm.plan.Verb.getVerb("SupplyWater");
    org.cougaar.planning.ldm.plan.Verb SupportRequest = org.cougaar.planning.ldm.plan.Verb.getVerb("SupportRequest");
    org.cougaar.planning.ldm.plan.Verb Transit = org.cougaar.planning.ldm.plan.Verb.getVerb("Transit");
    org.cougaar.planning.ldm.plan.Verb Transport = org.cougaar.planning.ldm.plan.Verb.getVerb("Transport");
    org.cougaar.planning.ldm.plan.Verb TransportAlert = org.cougaar.planning.ldm.plan.Verb.getVerb("TransportAlert");
    org.cougaar.planning.ldm.plan.Verb TransportationMission = org.cougaar.planning.ldm.plan.Verb.getVerb("TransportationMission");
    org.cougaar.planning.ldm.plan.Verb Unload = org.cougaar.planning.ldm.plan.Verb.getVerb("Unload");
    org.cougaar.planning.ldm.plan.Verb UserInput = org.cougaar.planning.ldm.plan.Verb.getVerb("UserInput");
    org.cougaar.planning.ldm.plan.Verb Withdraw = org.cougaar.planning.ldm.plan.Verb.getVerb("Withdraw");
  }

  public interface Preposition extends org.cougaar.planning.Constants.Preposition {
    // ALPINE defined prepositions
    String VIA         = "Via"; 	// typically used with transportation routes
    String READYAT     = "ReadyAt"; 	// typically used for origin and available date
    String ITINERARYOF = "ItineraryOf"; 	// typically used for detailed schedules
    String REPORTINGTO = "ReportingTo"; 	// typically used for specifying a new superior
    String MAINTAINING = "Maintaining"; 	// indicates consumer, e.g., MEI

    // FGI Prepositions
    String REFILL             = "Refill";           // For ANTS
    String DEMANDSPEC         = "DemandSpec";       // For ANTS/GLM
//      String DEMANDRATE         = "DemandRate";       // For ANTS/GLM
//      String WITHMULTIPLIER     = "WithMultiplier";   // For ANTS/GLM
    String USINGREQUISITION   = "UsingRequisition"; 	// For A0s
    String USINGSUPPLYSOURCE  = "UsingSupplySource"; 	// For FGI
    String USINGPARTNETEPORTS = "UsingPartNetEPorts"; 	// For FGI
    String RESPONDTO          = "RespondTo"; 	// For FGI
    String FROMTASK           = "FromTask"; 	// For FGI
    String USINGCALLNUMBER    = "UsingCallNumber"; 	//For FGI
  }


  public interface RelationshipType {
    String SUPERIOR_SUFFIX = "Superior";
    String SUBORDINATE_SUFFIX = "Subordinate";
    org.cougaar.planning.ldm.plan.RelationshipType SUPERIOR = 
      org.cougaar.planning.ldm.plan.RelationshipType.create(SUPERIOR_SUFFIX, SUBORDINATE_SUFFIX);

    String PROVIDER_SUFFIX = "Provider";
    String CUSTOMER_SUFFIX = "Customer";
    org.cougaar.planning.ldm.plan.RelationshipType PROVIDER = 
      org.cougaar.planning.ldm.plan.RelationshipType.create(PROVIDER_SUFFIX, CUSTOMER_SUFFIX);
  }

  public static class Role {
    /**
     * Insure that Role constants are initialized. Actually does
     * nothing, but the classloader insures that all static
     * initializers have been run before executing any code in this
     * class.
     **/
    public static void init() {
    }

    static {
      org.cougaar.planning.ldm.plan.Role.create("Self", "Self");
      org.cougaar.planning.ldm.plan.Role.create("", RelationshipType.SUPERIOR);
      org.cougaar.planning.ldm.plan.Role.create("Administrative", RelationshipType.SUPERIOR);


      org.cougaar.planning.ldm.plan.Role.create("Ammunition", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("AmmunitionHandling", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("AmmunitionTransport", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("CONUSForce", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("DSMaintenance", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("Food", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("FuelHandling", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("FuelSupply", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("FuelTransport", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("PackagedPOLSupply", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("GSMaintenance", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("HETTransportation", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("Level1HealthCare", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("Level2HealthCare", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("Level3HealthCare", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("MaterielHandling", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("MaterielTransport", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("MedicalSupply", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("NonS9CSpareParts", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("S9CSpareParts", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("SpareParts", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("SubsistenceSupply", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("StrategicTransportation", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("Supply", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("SupportForce", RelationshipType.PROVIDER);
      org.cougaar.planning.ldm.plan.Role.create("Water", RelationshipType.PROVIDER);
    }


    // duplicate these from the main Role implementation
    public static final org.cougaar.planning.ldm.plan.Role ASSIGNED = org.cougaar.planning.ldm.plan.Role.ASSIGNED;
    public static final org.cougaar.planning.ldm.plan.Role AVAILABLE = org.cougaar.planning.ldm.plan.Role.AVAILABLE;
    public static final org.cougaar.planning.ldm.plan.Role BOGUS = org.cougaar.planning.ldm.plan.Role.BOGUS;

    // asset roles
    public static final org.cougaar.planning.ldm.plan.Role TRANSPORTER = org.cougaar.planning.ldm.plan.Role.getRole("Transporter");
    public static final org.cougaar.planning.ldm.plan.Role MAINTAINER = org.cougaar.planning.ldm.plan.Role.getRole("Maintainer");
    public static final org.cougaar.planning.ldm.plan.Role HANDLER = org.cougaar.planning.ldm.plan.Role.getRole("Handler");
    public static final org.cougaar.planning.ldm.plan.Role CARGO = org.cougaar.planning.ldm.plan.Role.getRole("Cargo");
    public static final org.cougaar.planning.ldm.plan.Role OUTOFSERVICE = org.cougaar.planning.ldm.plan.Role.getRole("OutOfService");

    // organization roles
    public static final org.cougaar.planning.ldm.plan.Role SELF = 
      org.cougaar.planning.ldm.plan.Role.getRole("Self");

    public static final org.cougaar.planning.ldm.plan.Role SUPERIOR = 
      org.cougaar.planning.ldm.plan.Role.getRole(RelationshipType.SUPERIOR_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role SUBORDINATE =
      org.cougaar.planning.ldm.plan.Role.getRole(RelationshipType.SUBORDINATE_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role ADMINISTRATIVESUPERIOR = 
      org.cougaar.planning.ldm.plan.Role.getRole("Administrative" + 
                                RelationshipType.SUPERIOR_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role ADMINISTRATIVESUBORDINATE =
      org.cougaar.planning.ldm.plan.Role.getRole("Administrative" +
                                RelationshipType.SUBORDINATE_SUFFIX);

    public static final org.cougaar.planning.ldm.plan.Role COMBAT = org.cougaar.planning.ldm.plan.Role.getRole("Combat");
    public static final org.cougaar.planning.ldm.plan.Role HEADQUARTERS = org.cougaar.planning.ldm.plan.Role.getRole("Headquarters");

    public static final org.cougaar.planning.ldm.plan.Role AMMUNITIONPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Ammunition" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role AMMUNITIONCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Ammunition" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role AMMUNITIONHANDLINGPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("AmmunitionHandling" +
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role AMMUNITIONHANDLINGCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("AmmunitionHandling" +
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role AMMUNITIONTRANSPORTPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("AmmunitionTransport" +  
      RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role AMMUNITIONTRANSPORTCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("AmmunitionTransport" +
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role CONUSFORCEPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("CONUSForce" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role CONUSFORCECUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("CONUSForce" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role DSMAINTENANCEPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("DSMaintenance" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role DSMAINTENANCECUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("DSMaintenance" + 
                                RelationshipType.CUSTOMER_SUFFIX);
   public static final org.cougaar.planning.ldm.plan.Role FOODPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Food" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role FOODCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Food" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role FUELHANDLINGPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("FuelHandling" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role FUELSUPPLYPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("FuelSupply" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role FUELSUPPLYCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("FuelSupply" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role PACKAGEDPOLSUPPLYPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("PackagedPOLSupply" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role PACKAGEDPOLSUPPLYCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("PackagedPOLSupply" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role FUELHANDLINGCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("FuelHandling" +
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role FUELTRANSPORTPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("FuelTransport" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role FUELTRANSPORTCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("FuelTransport" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role GSMAINTENANCEPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("GSMaintenance" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role GSMAINTENANCECUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("GSMaintenance" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role HETTRANSPORTATIONPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("HETTransportation" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role HETTRANSPORTATIONCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("HETTransportation" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role LEVEL1HEALTHCAREPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Level1HealthCare" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role LEVEL1HEALTHCARECUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Level1HealthCare" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role LEVEL2HEALTHCAREPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Level2HealthCare" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role LEVEL2HEALTHCARECUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Level2HealthCare" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role LEVEL3HEALTHCAREPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Level3HealthCare" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role LEVEL3HEALTHCARECUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Level3HealthCare" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role MATERIELHANDLINGPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("MaterielHandling" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role MATERIELHANDLINGCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("MaterielHandling" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role MATERIELTRANSPORTPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("MaterielTransport" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role MATERIELTRANSPORTCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("MaterielTransport" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role MEDICALSUPPLYPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("MedicalSupply" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role MEDICALSUPPLYCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("MedicalSupply" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role NONS9CSPAREPARTSPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("NonS9CSpareParts" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role NONS9CSPAREPARTSCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("NonS9CSpareParts" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role S9CSPAREPARTSPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("S9CSpareParts" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role S9CSPAREPARTSCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("S9CSpareParts" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role SPAREPARTSPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("SpareParts" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role SPAREPARTSCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("SpareParts" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role SUPPLYPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Supply" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role SUPPLYCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Supply" + 
                                RelationshipType.CUSTOMER_SUFFIX);


    public static final org.cougaar.planning.ldm.plan.Role STRATEGICTRANSPORTATIONCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("StrategicTransportation" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role STRATEGICTRANSPORTATIONPROVIDER =
      org.cougaar.planning.ldm.plan.Role.getRole("StrategicTransportation" + 
                                RelationshipType.PROVIDER_SUFFIX);
   public static final org.cougaar.planning.ldm.plan.Role SUBSISTENCESUPPLYPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("SubsistenceSupply" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role SUBSISTENCESUPPLYCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("SubsistenceSupply" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role SUPPORTFORCEPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("SupportForce" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role SUPPORTFORCECUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("SupportForce" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role WATERCUSTOMER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Water" + RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.planning.ldm.plan.Role WATERPROVIDER = 
      org.cougaar.planning.ldm.plan.Role.getRole("Water" + RelationshipType.PROVIDER_SUFFIX);
  }
}









