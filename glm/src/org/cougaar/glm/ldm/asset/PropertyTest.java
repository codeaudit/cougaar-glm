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
package org.cougaar.glm.ldm.asset;

import org.cougaar.planning.ldm.measure.Area;
import org.cougaar.planning.ldm.measure.Distance;
import org.cougaar.planning.ldm.measure.Mass;
import org.cougaar.planning.ldm.measure.Volume;

public class PropertyTest {
  public static void main(String args[]) {
    NewPhysicalPG p = PropertyGroupFactory.newPhysicalPG();
    p.setLength(Distance.newMeters(23.4));
    p.setWidth((Distance)Distance.newMeasure("12",Distance.INCHES));
    p.setHeight(Distance.newDistance("0.001", Distance.MILES));
    p.setFootprintArea(Area.newSquareMeters(1.0));
    p.setVolume(Volume.newCubicCentimeters(123.4));
    p.setMass(Mass.newTons("3"));
    System.out.println("Original = "+p.toString());
    
    System.out.println("  length = "+p.getLength());
    System.out.println("  Width = "+p.getWidth());
    System.out.println("  Height = "+p.getHeight());
    System.out.println("  FootprintArea = "+p.getFootprintArea());
    System.out.println("  Volume = "+p.getVolume());
    System.out.println("  Mass = "+p.getMass());

    System.out.println();
    System.out.println("  length (in m) = "+p.getLength().getMeters());
    System.out.println("  length (in mi) = "+p.getLength().getMiles());
    System.out.println("  length (in nmi) = "+p.getLength().getNauticalMiles());
    System.out.println("  length (in yds) = "+p.getLength().getYards());
    System.out.println("  length (in ft) = "+p.getLength().getFeet());
    System.out.println("  length (in inches) = "+p.getLength().getInches());
    System.out.println("  length (in km) = "+p.getLength().getKilometers());
    System.out.println("  length (in cm) = "+p.getLength().getCentimeters());
    System.out.println("  length (in mm) = "+p.getLength().getMillimeters());
    System.out.println("  length (in furlongs) = "+
                       p.getLength().getValue(Distance.FURLONGS));
    System.out.println();

    Object key = new Object();
    Object wrong = new Object();
    PhysicalPG p1 = (PhysicalPG) p.lock(key);
    System.out.println("Locked = "+p1.toString());
    NewPhysicalPG p2=null;
    try {
      p2 = (NewPhysicalPG) p1.unlock(key);
    } catch (Exception e) {
      System.out.println("Couldn't unlock with rightkey.");
    }

    System.out.println("Unlocked = "+p2.toString());
    try {
      System.out.println("Wrong Key = "+((NewPhysicalPG) p1.unlock(wrong)).toString());
    } catch (Exception e) {
      System.out.println("Couldn't unlock with wrong key.");
    }
    System.out.println("Original length = "+p.getLength());
    System.out.println("Locked length = "+p1.getLength());
    System.out.println("Unlocked length = "+p2.getLength());
    p.setLength(Distance.newMeters(10.4));
    System.out.println("(new) Original length = "+p.getLength());
    System.out.println("(new) Locked length = "+p1.getLength());
    System.out.println("(new) Unlocked length = "+p2.getLength());

    NewPhysicalPG p3 = PropertyGroupFactory.newPhysicalPG(p1);
    System.out.println("new (from prototype) = "+p3);
    p3.setWidth(Distance.newInches("18"));
    System.out.println("new width = "+p3.getWidth());    
    //System.out.println("original width = "+p.getWidth());    
    //  PropertyGroup Prototypes not supported - use Asset prototypes instead
    /*
    System.out.println("original width (via getPrototype) = "+
                       ((PhysicalPG)(p3.getPrototype())).getWidth());
    
    */
  }
}
