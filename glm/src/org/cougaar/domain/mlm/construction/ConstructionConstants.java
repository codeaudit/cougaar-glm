/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

package org.cougaar.domain.mlm.construction;

import org.cougaar.domain.planning.ldm.plan.Role;

//import org.cougaar.domain.planning.ldm.plan.AspectType;

public final class ConstructionConstants {
  private ConstructionConstants() {}

  public static interface Verb {
    public static final String EXPANDRUNWAYAPRON = "Expand_Runway_Apron";
    public static final String CLEARAREA = "Clear_Area";
    public static final String GRADEAREA = "Grade_Area";
    public static final String PACKFOUNDATION = "Pack_Foundation";
    public static final String PACKANDSEAL = "Pack_and_Seal";
    public static final String BUILDSHORTRUNWAY = "Build_Short_Runway";
    public static final String BUILDSMALLTENTCITY = "Build_Small_Tent_City";
    public static final String CUTTREES = "Cut_Trees";
    public static final String REMOVESTUMPS = "Remove_Stumps";
    public static final String DOZEAREA = "Doze_Area";
    public static final String SCRAPE_AREA = "Scrape_Area";
    public static final String STEAMROLLAREA = "Steam_Roll_Area";
    public static final String COMPACTAREA = "Compact_Area";
    public static final String SPRAYSEALER = "Spray_Sealer";
    public static final String BUILDFRAMES = "Build_Frames";
    public static final String BUILDWALLS = "Build_Walls";
    public static final String CONSTRUCTROOFING = "Construct_Roofing";
    public static final String ESTABLISHSANITATION = "Establish_Sanitation";
    public static final String ESTABLISHPOWER = "Establish_Power";
    public static final String LAYGRAVEL = "Lay_Gravel";
    public static final String LAYMESH = "Lay_Mesh";
    public static final String POURCONCRETE = "Pour_Concrete";

   

    public static final String GENERATE_CONSTRUCTION_DEMAND =
		"GenerateConstructionDemand";


    public static final org.cougaar.domain.planning.ldm.plan.Verb Expand_Runway_Apron = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Expand_Runway_Apron");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Clear_Area = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Clear_Area");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Grade_Area = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Grade_Area");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Pack_Foundation = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Pack_Foundation");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Pack_and_Seal = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Pack_and_Seal");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Build_Short_Runway = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Build_Short_Runway");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Build_Small_Tent_City = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Build_Small_Tent_City");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Cut_Trees  = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Cut_Trees"); 
    public static final org.cougaar.domain.planning.ldm.plan.Verb Remove_Stumps  = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Remove_Stumps");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Doze_Area = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Doze_Area");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Scrape_Area = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Scrape_Area");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Steam_Roll_Area = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Steam_Roll_Area");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Compact_Area = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Compact_Area");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Spray_Sealer = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Spray_Sealer");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Build_Frames = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Build_Frames");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Build_Walls  = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Build_Walls");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Construct_Roofing = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Construct_Roofing");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Establish_Sanitation = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Establish_Sanitation");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Establish_Power = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Establish_Power");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Lay_Gravel = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Lay_Gravel");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Lay_Mesh = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Lay_Mesh");
    public static final org.cougaar.domain.planning.ldm.plan.Verb Pour_Concrete = org.cougaar.domain.planning.ldm.plan.Verb.getVerb("Pour_Concrete");
   
   
  }

  public static final Role CONSTRUCTION_PROVIDER = Role.getRole("ConstructionProvider");
  public static final Role CONSTRUCTIONSUPPLYPROVIDER = Role.getRole("ConstructionSupplyProvider");


}







