/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
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

    org.cougaar.planning.ldm.plan.Verb Expand_Runway_Apron = org.cougaar.planning.ldm.plan.Verb.get("Expand_Runway_Apron");
    org.cougaar.planning.ldm.plan.Verb Clear_Area = org.cougaar.planning.ldm.plan.Verb.get("Clear_Area");
    org.cougaar.planning.ldm.plan.Verb Grade_Area = org.cougaar.planning.ldm.plan.Verb.get("Grade_Area");
    org.cougaar.planning.ldm.plan.Verb Pack_Foundation = org.cougaar.planning.ldm.plan.Verb.get("Pack_Foundation");
    org.cougaar.planning.ldm.plan.Verb Pack_and_Seal = org.cougaar.planning.ldm.plan.Verb.get("Pack_and_Seal");
    org.cougaar.planning.ldm.plan.Verb Build_Short_Runway = org.cougaar.planning.ldm.plan.Verb.get("Build_Short_Runway");
    org.cougaar.planning.ldm.plan.Verb Build_Small_Tent_City = org.cougaar.planning.ldm.plan.Verb.get("Build_Small_Tent_City");
    org.cougaar.planning.ldm.plan.Verb Cut_Trees  = org.cougaar.planning.ldm.plan.Verb.get("Cut_Trees"); 
    org.cougaar.planning.ldm.plan.Verb Remove_Stumps  = org.cougaar.planning.ldm.plan.Verb.get("Remove_Stumps");
    org.cougaar.planning.ldm.plan.Verb Doze_Area = org.cougaar.planning.ldm.plan.Verb.get("Doze_Area");
    org.cougaar.planning.ldm.plan.Verb Scrape_Area = org.cougaar.planning.ldm.plan.Verb.get("Scrape_Area");
    org.cougaar.planning.ldm.plan.Verb Steam_Roll_Area = org.cougaar.planning.ldm.plan.Verb.get("Steam_Roll_Area");
    org.cougaar.planning.ldm.plan.Verb Compact_Area = org.cougaar.planning.ldm.plan.Verb.get("Compact_Area");
    org.cougaar.planning.ldm.plan.Verb Spray_Sealer = org.cougaar.planning.ldm.plan.Verb.get("Spray_Sealer");
    org.cougaar.planning.ldm.plan.Verb Build_Frames = org.cougaar.planning.ldm.plan.Verb.get("Build_Frames");
    org.cougaar.planning.ldm.plan.Verb Build_Walls  = org.cougaar.planning.ldm.plan.Verb.get("Build_Walls");
    org.cougaar.planning.ldm.plan.Verb Construct_Roofing = org.cougaar.planning.ldm.plan.Verb.get("Construct_Roofing");
    org.cougaar.planning.ldm.plan.Verb Establish_Sanitation = org.cougaar.planning.ldm.plan.Verb.get("Establish_Sanitation");
    org.cougaar.planning.ldm.plan.Verb Establish_Power = org.cougaar.planning.ldm.plan.Verb.get("Establish_Power");
    org.cougaar.planning.ldm.plan.Verb Lay_Gravel = org.cougaar.planning.ldm.plan.Verb.get("Lay_Gravel");
    org.cougaar.planning.ldm.plan.Verb Lay_Mesh = org.cougaar.planning.ldm.plan.Verb.get("Lay_Mesh");
    org.cougaar.planning.ldm.plan.Verb Pour_Concrete = org.cougaar.planning.ldm.plan.Verb.get("Pour_Concrete");
  }

  public static final Role CONSTRUCTION_PROVIDER = Role.getRole("ConstructionProvider");
  public static final Role CONSTRUCTIONSUPPLYPROVIDER = Role.getRole("ConstructionSupplyProvider");


}







