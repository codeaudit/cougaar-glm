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

package org.cougaar.mlm.examples;

import org.cougaar.glm.ldm.asset.GLMAsset;
import org.cougaar.glm.ldm.asset.NewPhysicalPG;
import org.cougaar.planning.ldm.PropertyProvider;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.measure.Area;
import org.cougaar.planning.ldm.measure.Distance;
import org.cougaar.planning.ldm.measure.Mass;
import org.cougaar.planning.ldm.measure.Volume;
import org.cougaar.planning.plugin.legacy.PluginAdapter;

/** This plugin knows how to fill Widget prototypes
 * with physical attribute data.
 **/

public class PropertyProviderPluginExample
  // Just another *very* simple plugin
  extends PluginAdapter 
  // advertize as a provider of property groups
  implements PropertyProvider 
{

  /** this method is called by the LDM in response to 
   * LDM.fillProperties() when a prototype provider wants
   * other LDM plugins to fill in additional information.
   **/
  public void fillProperties(Asset proto) {
    if (proto instanceof GLMAsset) {
      GLMAsset ap = (GLMAsset) proto;

      String aTypeName = ap.getTypeIdentificationPG().getTypeIdentification();

      // decide if this is the kind of prototype we can handle.
      if (aTypeName != null && aTypeName.startsWith("NSN/")) {
        int nsn = Integer.parseInt(aTypeName.substring(4));
        if (nsn >= 100 && nsn <= 199) {
          NewPhysicalPG pp = (NewPhysicalPG)ap.getPhysicalPG();
          // While all widget prototypes constructed by PrototypeProviderPluginExample
          // are PhysicalAssets (and so already have a PhysicalPG),
          // we'll be careful lest some joker makes and registers
          // another prototype of a different class.
          if (pp == null) {
            pp = (NewPhysicalPG)theLDMF.createPropertyGroup("PhysicalPG");
            // add it to our prototype.
            ap.setPhysicalPG(pp);
          }

          pp.setLength(Distance.newInches(6));
          pp.setWidth(Distance.newFeet(5));
          pp.setHeight(Distance.newFeet(3));
          pp.setFootprintArea(Area.newSquareFeet(2.5));
          pp.setVolume(Volume.newCubicFeet(7.5));
          // the main difference between widgets is how much they weigh...
          pp.setMass(Mass.newPounds(nsn-99));
        }
      }
    }
  }
}

