/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm;

import org.cougaar.domain.planning.ldm.plan.AspectType;

public class Constants implements org.cougaar.domain.planning.Constants {
  private Constants() {}

  public static final String DSMAINTENANCE = "DSMaintenance";
  public static final String DURING = "During";
  public static final String FOOD = "Food";
  public static final String GSMAINTENANCE = "GSMaintenance";
  public static final String WATER = "Water";

  public static interface Verb extends org.cougaar.domain.planning.Constants.Verb {
    // ALPINE defined verb types
    // Keep in alphabetical order
    public static final String ACCOUNTFORSUPPLY = "AccountForSupply";
    public static final String ARM = "Arm";
    public static final String BULKESTIMATE = "BulkEstimate";
    public static final String DETERMINEREQUIREMENTS = "DetermineRequirements";
    public static final String ESTABLISHMISSIONREQUIREMENTS = "EstablishMissionRequirements";
    public static final String EXECUTE = "Execute";
    public static final String FUEL = "Fuel";
    public static final String GENERATEAEF = "GenerateAEF";
    public static final String GENERATEPROJECTIONS = "GenerateProjections";
    public static final String GETLOGSUPPORT = "GetLogSupport";
    public static final String IDLE = "Idle";
    public static final String INFORM = "Inform";
    public static final String LOAD = "Load";
    public static final String MAINTAIN = "Maintain";
    public static final String MAINTAININVENTORY = "MaintainInventory";
    public static final String MANAGE = "Manage";
    public static final String PREPAREFORTRANSPORT = "PrepareForTransport";
    public static final String PRODUCEWATER = "ProduceWater";
    public static final String PROJECTWITHDRAW = "ProjectWithdraw";
    public static final String PROVIDEFOOD = "ProvideFood";
    public static final String PROVIDEWATER = "ProvideWater";
    public static final String QUARTERMASTERHANDLING = "QuartermasterHandling";
    public static final String REPORTFORDUTY = "ReportForDuty";
    public static final String REPORTFORSERVICE = "ReportForService";
    public static final String REPORTREADINESS = "ReportReadiness";
    public static final String REQUESTMAINTENANCESUPPORT =
      "RequestMaintenanceSupport";
    public static final String SUPPLY = "Supply";
    public static final String PROJECTSUPPLY = "ProjectSupply";
    public static final String SUPPLYAIRCRAFT = "SupplyAircraft";
    public static final String SUPPLYFOOD = "SupplyFood";
    public static final String SUPPLYSTATUSINQUIRY = "SupplyStatusInquiry";
    public static final String SUPPLYWATER = "SupplyWater";
    public static final String SUPPORTREQUEST = "SupportRequest";
    public static final String TRANSIT = "Transit"; // move yourself
    public static final String TRANSPORT = "Transport";
    public static final String TRANSPORTALERT = "TransportAlert";
    public static final String TRANSPORTATIONMISSION = "TransportationMission";
    public static final String UNLOAD = "Unload";
    public static final String USERINPUT = "UserInput";
    public static final String WITHDRAW = "Withdraw";

    public static final org.cougaar.domain.planning.ldm.plan.Verb AccountForSupply = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("AccountForSupply");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Arm = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Arm");
    public static final org.cougaar.domain.planning.ldm.plan.Verb BulkEstimate = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("BulkEstimate");
    public static final org.cougaar.domain.planning.ldm.plan.Verb DetermineRequirements = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("DetermineRequirements");
    public static final org.cougaar.domain.planning.ldm.plan.Verb EstablishMissionRequirements = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("EstablishMissionRequirements");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Execute = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Execute");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Fuel = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Fuel");
    public static final org.cougaar.domain.planning.ldm.plan.Verb GetLogSupport = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("GetLogSupport");
    public static final org.cougaar.domain.planning.ldm.plan.Verb GenerateAEF = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("GenerateAEF");
    public static final org.cougaar.domain.planning.ldm.plan.Verb GenerateProjections = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("GenerateProjections");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Idle = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Idle");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Inform = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Inform");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Load = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Load");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Maintain = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Maintain");
    public static final org.cougaar.domain.planning.ldm.plan.Verb MaintainInventory = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("MaintainInventory");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Manage = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Manage");
    public static final org.cougaar.domain.planning.ldm.plan.Verb PrepareForTransport = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("PrepareForTransport");
    public static final org.cougaar.domain.planning.ldm.plan.Verb ProduceWater = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("ProduceWater");
    public static final org.cougaar.domain.planning.ldm.plan.Verb ProjectWithdraw = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("ProjectWithdraw");
    public static final org.cougaar.domain.planning.ldm.plan.Verb ProvideFood = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("ProvideFood");
    public static final org.cougaar.domain.planning.ldm.plan.Verb ProvideWater = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("ProvideWater");
    public static final org.cougaar.domain.planning.ldm.plan.Verb QuartermasterHandling = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("QuartermasterHandling");
    public static final org.cougaar.domain.planning.ldm.plan.Verb ReportForDuty = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("ReportForDuty");
    public static final org.cougaar.domain.planning.ldm.plan.Verb ReportForService = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("ReportForService");
    public static final org.cougaar.domain.planning.ldm.plan.Verb ReportReadiness = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("ReportReadiness");
    public static final org.cougaar.domain.planning.ldm.plan.Verb RequestMaintenanceSupport = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("RequestMaintenanceSupport");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Supply = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Supply");
    public static final org.cougaar.domain.planning.ldm.plan.Verb ProjectSupply = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("ProjectSupply");
    public static final org.cougaar.domain.planning.ldm.plan.Verb SupplyAircraft = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("SupplyAircraft");
    public static final org.cougaar.domain.planning.ldm.plan.Verb SupplyFood = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("SupplyFood");
    public static final org.cougaar.domain.planning.ldm.plan.Verb SupplyStatusInquiry = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("SupplyStatusInquiry");
    public static final org.cougaar.domain.planning.ldm.plan.Verb SupplyWater = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("SupplyWater");
    public static final org.cougaar.domain.planning.ldm.plan.Verb SupportRequest = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("SupportRequest");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Transit = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Transit");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Transport = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Transport");
    public static final org.cougaar.domain.planning.ldm.plan.Verb TransportAlert = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("TransportAlert");
    public static final org.cougaar.domain.planning.ldm.plan.Verb TransportationMission = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("TransportationMission");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Unload = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Unload");
    public static final org.cougaar.domain.planning.ldm.plan.Verb UserInput = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("UserInput");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Withdraw = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Withdraw");
  }

  public static interface Preposition extends org.cougaar.domain.planning.Constants.Preposition {
    // ALPINE defined prepositions
    public static final String VIA         = "Via"; 	// typically used with transportation routes
    public static final String READYAT     = "ReadyAt"; 	// typically used for origin and available date
    public static final String ITINERARYOF = "ItineraryOf"; 	// typically used for detailed schedules
    public static final String REPORTINGTO = "ReportingTo"; 	// typically used for specifying a new superior
    public static final String MAINTAINING = "Maintaining"; 	// indicates consumer, e.g., MEI


    // FGI Prepositions
    public static final String REFILL             = "Refill";           // For ANTS
    public static final String DEMANDSPEC         = "DemandSpec";       // For ANTS/GLM
//      public static final String DEMANDRATE         = "DemandRate";       // For ANTS/GLM
//      public static final String WITHMULTIPLIER     = "WithMultiplier";   // For ANTS/GLM
    public static final String USINGREQUISITION   = "UsingRequisition"; 	// For A0s
    public static final String USINGSUPPLYSOURCE  = "UsingSupplySource"; 	// For FGI
    public static final String USINGPARTNETEPORTS = "UsingPartNetEPorts"; 	// For FGI
    public static final String RESPONDTO          = "RespondTo"; 	// For FGI
    public static final String FROMTASK           = "FromTask"; 	// For FGI
    public static final String USINGCALLNUMBER    = "UsingCallNumber"; 	//For FGI
  }


  public static interface RelationshipType {
    public static final String SUPERIOR_SUFFIX = "Superior";
    public static final String SUBORDINATE_SUFFIX = "Subordinate";

    public static final org.cougaar.domain.planning.ldm.plan.RelationshipType SUPERIOR = 
      org.cougaar.domain.planning.ldm.plan.RelationshipType.create(SUPERIOR_SUFFIX, SUBORDINATE_SUFFIX);

    public static final String PROVIDER_SUFFIX = "Provider";
    public static final String CUSTOMER_SUFFIX = "Customer";
    public static final org.cougaar.domain.planning.ldm.plan.RelationshipType PROVIDER = 
      org.cougaar.domain.planning.ldm.plan.RelationshipType.create(PROVIDER_SUFFIX, CUSTOMER_SUFFIX);


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
      org.cougaar.domain.planning.ldm.plan.Role.create("Self", "Self");
      org.cougaar.domain.planning.ldm.plan.Role.create("", RelationshipType.SUPERIOR);
      org.cougaar.domain.planning.ldm.plan.Role.create("Administrative", RelationshipType.SUPERIOR);


      org.cougaar.domain.planning.ldm.plan.Role.create("Ammunition", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("AmmunitionHandling", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("AmmunitionTransport", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("CONUSForce", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("DSMaintenance", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("Food", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("FuelHandling", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("FuelSupply", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("FuelTransport", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("PackagedPOLSupply", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("GSMaintenance", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("HETTransportation", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("Level1HealthCare", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("Level2HealthCare", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("Level3HealthCare", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("MaterielHandling", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("MaterielTransport", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("MedicalSupply", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("NonS9CSpareParts", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("S9CSpareParts", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("SpareParts", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("SubsistenceSupply", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("StrategicTransportation", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("Supply", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("SupportForce", RelationshipType.PROVIDER);
      org.cougaar.domain.planning.ldm.plan.Role.create("Water", RelationshipType.PROVIDER);
    }


    // duplicate these from the main Role implementation
    public static final org.cougaar.domain.planning.ldm.plan.Role ASSIGNED = org.cougaar.domain.planning.ldm.plan.Role.ASSIGNED;
    public static final org.cougaar.domain.planning.ldm.plan.Role AVAILABLE = org.cougaar.domain.planning.ldm.plan.Role.AVAILABLE;
    public static final org.cougaar.domain.planning.ldm.plan.Role BOGUS = org.cougaar.domain.planning.ldm.plan.Role.BOGUS;

    // asset roles
    public static final org.cougaar.domain.planning.ldm.plan.Role TRANSPORTER = org.cougaar.domain.planning.ldm.plan.Role.getRole("Transporter");
    public static final org.cougaar.domain.planning.ldm.plan.Role MAINTAINER = org.cougaar.domain.planning.ldm.plan.Role.getRole("Maintainer");
    public static final org.cougaar.domain.planning.ldm.plan.Role HANDLER = org.cougaar.domain.planning.ldm.plan.Role.getRole("Handler");
    public static final org.cougaar.domain.planning.ldm.plan.Role CARGO = org.cougaar.domain.planning.ldm.plan.Role.getRole("Cargo");
    public static final org.cougaar.domain.planning.ldm.plan.Role OUTOFSERVICE = org.cougaar.domain.planning.ldm.plan.Role.getRole("OutOfService");

    // organization roles
    public static final org.cougaar.domain.planning.ldm.plan.Role SELF = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Self");

    public static final org.cougaar.domain.planning.ldm.plan.Role SUPERIOR = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole(RelationshipType.SUPERIOR_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SUBORDINATE =
      org.cougaar.domain.planning.ldm.plan.Role.getRole(RelationshipType.SUBORDINATE_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role ADMINISTRATIVESUPERIOR = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Administrative" + 
                                RelationshipType.SUPERIOR_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role ADMINISTRATIVESUBORDINATE =
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Administrative" +
                                RelationshipType.SUBORDINATE_SUFFIX);

    public static final org.cougaar.domain.planning.ldm.plan.Role COMBAT = org.cougaar.domain.planning.ldm.plan.Role.getRole("Combat");
    public static final org.cougaar.domain.planning.ldm.plan.Role HEADQUARTERS = org.cougaar.domain.planning.ldm.plan.Role.getRole("Headquarters");

    public static final org.cougaar.domain.planning.ldm.plan.Role AMMUNITIONPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Ammunition" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role AMMUNITIONCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Ammunition" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role AMMUNITIONHANDLINGPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("AmmunitionHandling" +
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role AMMUNITIONHANDLINGCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("AmmunitionHandling" +
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role AMMUNITIONTRANSPORTPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("AmmunitionTransport" +  
      RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role AMMUNITIONTRANSPORTCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("AmmunitionTransport" +
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role CONUSFORCEPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("CONUSForce" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role CONUSFORCECUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("CONUSForce" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role DSMAINTENANCEPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("DSMaintenance" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role DSMAINTENANCECUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("DSMaintenance" + 
                                RelationshipType.CUSTOMER_SUFFIX);
   public static final org.cougaar.domain.planning.ldm.plan.Role FOODPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Food" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role FOODCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Food" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role FUELHANDLINGPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("FuelHandling" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role FUELSUPPLYPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("FuelSupply" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role FUELSUPPLYCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("FuelSupply" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role PACKAGEDPOLSUPPLYPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("PackagedPOLSupply" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role PACKAGEDPOLSUPPLYCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("PackagedPOLSupply" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role FUELHANDLINGCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("FuelHandling" +
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role FUELTRANSPORTPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("FuelTransport" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role FUELTRANSPORTCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("FuelTransport" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role GSMAINTENANCEPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("GSMaintenance" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role GSMAINTENANCECUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("GSMaintenance" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role HETTRANSPORTATIONPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("HETTransportation" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role HETTRANSPORTATIONCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("HETTransportation" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role LEVEL1HEALTHCAREPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Level1HealthCare" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role LEVEL1HEALTHCARECUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Level1HealthCare" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role LEVEL2HEALTHCAREPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Level2HealthCare" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role LEVEL2HEALTHCARECUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Level2HealthCare" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role LEVEL3HEALTHCAREPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Level3HealthCare" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role LEVEL3HEALTHCARECUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Level3HealthCare" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role MATERIELHANDLINGPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("MaterielHandling" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role MATERIELHANDLINGCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("MaterielHandling" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role MATERIELTRANSPORTPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("MaterielTransport" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role MATERIELTRANSPORTCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("MaterielTransport" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role MEDICALSUPPLYPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("MedicalSupply" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role MEDICALSUPPLYCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("MedicalSupply" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role NONS9CSPAREPARTSPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("NonS9CSpareParts" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role NONS9CSPAREPARTSCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("NonS9CSpareParts" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role S9CSPAREPARTSPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("S9CSpareParts" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role S9CSPAREPARTSCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("S9CSpareParts" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SPAREPARTSPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("SpareParts" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SPAREPARTSCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("SpareParts" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SUPPLYPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Supply" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SUPPLYCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Supply" + 
                                RelationshipType.CUSTOMER_SUFFIX);


    public static final org.cougaar.domain.planning.ldm.plan.Role STRATEGICTRANSPORTATIONCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("StrategicTransportation" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role STRATEGICTRANSPORTATIONPROVIDER =
      org.cougaar.domain.planning.ldm.plan.Role.getRole("StrategicTransportation" + 
                                RelationshipType.PROVIDER_SUFFIX);
   public static final org.cougaar.domain.planning.ldm.plan.Role SUBSISTENCESUPPLYPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("SubsistenceSupply" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SUBSISTENCESUPPLYCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("SubsistenceSupply" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SUPPORTFORCEPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("SupportForce" + 
                                RelationshipType.PROVIDER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role SUPPORTFORCECUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("SupportForce" + 
                                RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role WATERCUSTOMER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Water" + RelationshipType.CUSTOMER_SUFFIX);
    public static final org.cougaar.domain.planning.ldm.plan.Role WATERPROVIDER = 
      org.cougaar.domain.planning.ldm.plan.Role.getRole("Water" + RelationshipType.PROVIDER_SUFFIX);
  }
}









