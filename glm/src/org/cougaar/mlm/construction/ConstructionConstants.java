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

package org.cougaar.mlm.construction;

import org.cougaar.planning.ldm.plan.Role;

//import org.cougaar.planning.ldm.plan.AspectType;

public final class ConstructionConstants {
  private ConstructionConstants() {}

  public interface Verb {
    String EXPANDRUNWAYAPRON = "Expand_Runway_Apron";
    String CLEARAREA = "Clear_Area";
    String GRADEAREA = "Grade_Area";
    String PACKFOUNDATION = "Pack_Foundation";
    String PACKANDSEAL = "Pack_and_Seal";
    String BUILDSHORTRUNWAY = "Build_Short_Runway";
    String BUILDSMALLTENTCITY = "Build_Small_Tent_City";
    String CUTTREES = "Cut_Trees";
    String REMOVESTUMPS = "Remove_Stumps";
    String DOZEAREA = "Doze_Area";
    String SCRAPE_AREA = "Scrape_Area";
    String STEAMROLLAREA = "Steam_Roll_Area";
    String COMPACTAREA = "Compact_Area";
    String SPRAYSEALER = "Spray_Sealer";
    String BUILDFRAMES = "Build_Frames";
    String BUILDWALLS = "Build_Walls";
    String CONSTRUCTROOFING = "Construct_Roofing";
    String ESTABLISHSANITATION = "Establish_Sanitation";
    String ESTABLISHPOWER = "Establish_Power";
    String LAYGRAVEL = "Lay_Gravel";
    String LAYMESH = "Lay_Mesh";
    String POURCONCRETE = "Pour_Concrete";

    String GENERATE_CONSTRUCTION_DEMAND =
		"GenerateConstructionDemand";

    org.cougaar.planning.ldm.plan.Verb Expand_Runway_Apron = org.cougaar.planning.ldm.plan.Verb.getVerb("Expand_Runway_Apron");
    org.cougaar.planning.ldm.plan.Verb Clear_Area = org.cougaar.planning.ldm.plan.Verb.getVerb("Clear_Area");
    org.cougaar.planning.ldm.plan.Verb Grade_Area = org.cougaar.planning.ldm.plan.Verb.getVerb("Grade_Area");
    org.cougaar.planning.ldm.plan.Verb Pack_Foundation = org.cougaar.planning.ldm.plan.Verb.getVerb("Pack_Foundation");
    org.cougaar.planning.ldm.plan.Verb Pack_and_Seal = org.cougaar.planning.ldm.plan.Verb.getVerb("Pack_and_Seal");
    org.cougaar.planning.ldm.plan.Verb Build_Short_Runway = org.cougaar.planning.ldm.plan.Verb.getVerb("Build_Short_Runway");
    org.cougaar.planning.ldm.plan.Verb Build_Small_Tent_City = org.cougaar.planning.ldm.plan.Verb.getVerb("Build_Small_Tent_City");
    org.cougaar.planning.ldm.plan.Verb Cut_Trees  = org.cougaar.planning.ldm.plan.Verb.getVerb("Cut_Trees"); 
    org.cougaar.planning.ldm.plan.Verb Remove_Stumps  = org.cougaar.planning.ldm.plan.Verb.getVerb("Remove_Stumps");
    org.cougaar.planning.ldm.plan.Verb Doze_Area = org.cougaar.planning.ldm.plan.Verb.getVerb("Doze_Area");
    org.cougaar.planning.ldm.plan.Verb Scrape_Area = org.cougaar.planning.ldm.plan.Verb.getVerb("Scrape_Area");
    org.cougaar.planning.ldm.plan.Verb Steam_Roll_Area = org.cougaar.planning.ldm.plan.Verb.getVerb("Steam_Roll_Area");
    org.cougaar.planning.ldm.plan.Verb Compact_Area = org.cougaar.planning.ldm.plan.Verb.getVerb("Compact_Area");
    org.cougaar.planning.ldm.plan.Verb Spray_Sealer = org.cougaar.planning.ldm.plan.Verb.getVerb("Spray_Sealer");
    org.cougaar.planning.ldm.plan.Verb Build_Frames = org.cougaar.planning.ldm.plan.Verb.getVerb("Build_Frames");
    org.cougaar.planning.ldm.plan.Verb Build_Walls  = org.cougaar.planning.ldm.plan.Verb.getVerb("Build_Walls");
    org.cougaar.planning.ldm.plan.Verb Construct_Roofing = org.cougaar.planning.ldm.plan.Verb.getVerb("Construct_Roofing");
    org.cougaar.planning.ldm.plan.Verb Establish_Sanitation = org.cougaar.planning.ldm.plan.Verb.getVerb("Establish_Sanitation");
    org.cougaar.planning.ldm.plan.Verb Establish_Power = org.cougaar.planning.ldm.plan.Verb.getVerb("Establish_Power");
    org.cougaar.planning.ldm.plan.Verb Lay_Gravel = org.cougaar.planning.ldm.plan.Verb.getVerb("Lay_Gravel");
    org.cougaar.planning.ldm.plan.Verb Lay_Mesh = org.cougaar.planning.ldm.plan.Verb.getVerb("Lay_Mesh");
    org.cougaar.planning.ldm.plan.Verb Pour_Concrete = org.cougaar.planning.ldm.plan.Verb.getVerb("Pour_Concrete");
  }

  public static final Role CONSTRUCTION_PROVIDER = Role.getRole("ConstructionProvider");
  public static final Role CONSTRUCTIONSUPPLYPROVIDER = Role.getRole("ConstructionSupplyProvider");


}







