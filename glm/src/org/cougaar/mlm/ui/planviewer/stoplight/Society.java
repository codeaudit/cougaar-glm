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
 
package org.cougaar.mlm.ui.planviewer.stoplight;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Society {
  Hashtable units;
  Vector subordinates;
  public static final String TOPUNIT = "XVIII Corps";
  static final String[][] superiors = 
      { { "Society", "HigherAuthority" },
        { "III Corps", "Society" },
        { "XVIII Corps", "Society" },
        { "III Corps-HQ", "III Corps" },
        { "XVIII Corps-HQ", "XVIII Corps" },

        { "1CAVDIV", "III Corps" },
        { "4ID", "III Corps" },
        { "1CAVDIV-HQ", "1CAVDIV" },
        { "4ID-HQ", "4ID" },

        { "3ID", "XVIII Corps", },
        { "3ID-HQ", "3ID" },

        { "1BDE-3ID", "3ID" },
        { "2BDE-3ID", "3ID" },
        { "3BDE-3ID", "3ID" },
        { "AVNBDE-3ID", "3ID" },
        { "DIVARTY-3ID", "3ID" },
        { "ENGBDE-3ID", "3ID" },
        { "DISCOM-3ID", "3ID" }, // add discom and its subordinates
        { "3-FSB", "DISCOM-3ID" },
        { "203-FSB", "DISCOM-3ID" },
        { "26-FSB", "DISCOM-3ID" },
        { "603-DASB", "DISCOM-3ID" },
        { "703-MSB", "DISCOM-3ID" },
        { "1BDE-3ID-HQ", "1BDE-3ID" },
        { "2BDE-3ID-HQ", "2BDE-3ID" },
        { "3BDE-3ID-HQ", "3BDE-3ID" },
        { "AVNBDE-3ID-HQ", "AVNBDE-3ID" },
        { "DIVARTY-3ID-HQ", "DIVARTY-3ID" },
        { "ENGBDE-3ID-HQ", "ENGBDE-3ID" },
        { "DISCOM-3ID-HQ", "DISCOM-3ID" },
        { "3-7-INFBN", "1BDE-3ID" },
        { "3-69-ARBN", "1BDE-3ID" },
        { "2-7-INFBN", "1BDE-3ID" },
        { "3-15-INFBN", "2BDE-3ID" },
        { "4-64-ARBN", "2BDE-3ID" },
        { "1-64-ARBN" , "2BDE-3ID" },
        { "2-69-ARBN", "3BDE-3ID" },
        { "1-30-INFBN", "3BDE-3ID" },
        { "1-15-INFBN", "3BDE-3ID" },
        { "3-7-CAVSQDN", "AVNBDE-3ID" },
        { "1-3-AVNBN", "AVNBDE-3ID" },
        { "2-3-AVNBN", "AVNBDE-3ID" },
        { "1-9-FABN", "DIVARTY-3ID" },
        { "1-41-FABN", "DIVARTY-3ID" },
        { "1-10-FABN", "DIVARTY-3ID" },
        { "10-ENGBN", "ENGBDE-3ID" },
        { "317-ENGBN", "ENGBDE-3ID" },
        { "11-ENGBN", "ENGBDE-3ID" },

        { "1BDE-4ID", "4ID" },
        { "2BDE-4ID", "4ID" },
        { "3BDE-4ID", "4ID" },
        { "AVNBDE-4ID", "4ID" },
        { "DIVARTY-4ID", "4ID" },
        { "ENGBDE-4ID", "4ID" },
        { "1BDE-4ID-HQ", "1BDE-4ID" },
        { "2BDE-4ID-HQ", "2BDE-4ID" },
        { "3BDE-4ID-HQ", "3BDE-4ID" },
        { "AVNBDE-4ID-HQ", "AVNBDE-4ID" },
        { "DIVARTY-4ID-HQ", "DIVARTY-4ID" },
        { "ENGBDE-4ID-HQ", "ENGBDE-4ID" },
        { "1-22-INFBN", "1BDE-4ID" },
        { "1-66-ARBN", "1BDE-4ID" },
        { "3-66-ARBN", "1BDE-4ID" },
        { "1-67-ARBN", "2BDE-4ID" },
        { "2-8-INFBN", "2BDE-4ID" },
        { "3-67-ARBN" , "2BDE-4ID" },
        { "1-12-INFBN", "3BDE-4ID" },
        { "1-68-ARBN", "3BDE-4ID" },
        { "1-8-INFBN", "3BDE-4ID" },
        { "1-10-CAVSQDN", "AVNBDE-4ID" },
        { "1-4-AVNBN", "AVNBDE-4ID" },
        { "2-4-AVNBN", "AVNBDE-4ID" },
        { "2-20-FABN", "DIVARTY-4ID" },
        { "3-16-FABN", "DIVARTY-4ID" },
        { "3-29-FABN", "DIVARTY-4ID" },
        { "4-42-FABN", "DIVARTY-4ID" },
        { "299-ENGBN", "ENGBDE-4ID" },
        { "4-ENGBN", "ENGBDE-4ID" },
        { "588-ENGBN", "ENGBDE-4ID" },

        { "1BDE-1CAVDIV", "1CAVDIV" },
        { "2BDE-1CAVDIV", "1CAVDIV" },
        { "3BDE-1CAVDIV", "1CAVDIV" },
        { "AVNBDE-1CAVDIV", "1CAVDIV" },
        { "DIVARTY-1CAVDIV", "1CAVDIV" },
        { "ENGBDE-1CAVDIV", "1CAVDIV" },
        { "1BDE-1CAVDIV-HQ", "1BDE-1CAVDIV" },
        { "2BDE-1CAVDIV-HQ", "2BDE-1CAVDIV" },
        { "3BDE-1CAVDIV-HQ", "3BDE-1CAVDIV" },
        { "AVNBDE-1CAVDIV-HQ", "AVNBDE-1CAVDIV" },
        { "DIVARTY-1CAVDIV-HQ", "DIVARTY-1CAVDIV" },
        { "ENGBDE-1CAVDIV-HQ", "ENGBDE-1CAVDIV" },
        { "1-12-CAVRGT", "1BDE-1CAVDIV" },
        { "2-5-CAVRGT", "1BDE-1CAVDIV" },
        { "2-8-CAVRGT", "1BDE-1CAVDIV" },
        { "1-5-CAVRGT", "2BDE-1CAVDIV" },
        { "1-8-CAVRGT", "2BDE-1CAVDIV" },
        { "2-12-CAVRGT" , "2BDE-1CAVDIV" },
        { "1-9-CAVRGT", "3BDE-1CAVDIV" },
        { "2-7-CAVRGT", "3BDE-1CAVDIV" },
        { "3-8-CAVRGT", "3BDE-1CAVDIV" },
        { "1-227-AVNRGT", "AVNBDE-1CAVDIV" },
        { "1-SQDN-7-CAVRGT", "AVNBDE-1CAVDIV" },
        { "2-227-AVNRGT", "AVNBDE-1CAVDIV" },
        { "1-21-FABN", "DIVARTY-1CAVDIV" },
        { "1-82-FABN", "DIVARTY-1CAVDIV" },
        { "2-82-FABN", "DIVARTY-1CAVDIV" },
        { "3-82-FABN", "DIVARTY-1CAVDIV" },
        { "20-ENGBN", "ENGBDE-1CAVDIV" },
        { "8-ENGBN", "ENGBDE-1CAVDIV" },
        { "91-ENGBN", "ENGBDE-1CAVDIV" }};

  /** Create a default society based on all known battalions.
   */

  public Society() {
    units = new Hashtable();
    setSubordinates();
  }

  private void setSubordinates() {
    subordinates = new Vector();
    for (int i = 0; i < superiors.length; i++) {
      String s = superiors[i][0];
      boolean isSubordinate = true;
      for (int j = 0; j < superiors.length; j++)
        if (superiors[j][1].equals(s)) {
          isSubordinate = false;
          break;
        }
      if (isSubordinate)
        subordinates.addElement(s);
    }
  }

  public void createDefaultSociety() {
    setSubordinates();
    for (int i = 0; i < subordinates.size(); i++)
      addUnit((String)subordinates.elementAt(i), 0);
  }

  /** Create a society with the specified subordinate units, i.e. units
    that are the leaves of the society and have no subordinates.
   */

  public Society(Vector unitNames) {
    units = new Hashtable();
    for (int i = 0; i < unitNames.size(); i++)
      addUnit((String)unitNames.elementAt(i), 0);
  }

  public String getSuperiorName(String name) {
    for (int i = 0; i < superiors.length; i++)
      if (superiors[i][0].equals(name))
        return superiors[i][1];
    return null;
  }

  public void addUnit(String name, int unitLevel) {
    Unit unit = (Unit)units.get(name);
    if (unit == null) {
      unit = new Unit(name);
      unit.setLevel(unitLevel);
      String superiorName = getSuperiorName(name);
      if (superiorName == null)
        return;
      if (superiorName.length() == 0) // society
        unit.setSuperiorName(null);
      else
        unit.setSuperiorName(superiorName);
      Unit superiorUnit = (Unit)units.get(superiorName);
      if (superiorUnit == null) {
        unitLevel++;
        addUnit(superiorName, unitLevel); // recursive
        superiorUnit = (Unit)units.get(superiorName);
        if (superiorUnit != null)
          superiorUnit.addSubordinateName(name);
      } else
        superiorUnit.addSubordinateName(name);
    }
    units.put(name, unit);
  }

  public Unit getUnit(String unitName) {
    if (units.get(unitName) == null)
      System.out.println("WARNING: NO UNIT FOR: " + unitName);
    return (Unit)units.get(unitName);
  }

  public Enumeration getAllUnits() {
    return units.elements();
  }

  public Vector getUnitsAtLevel(int level) {
    Vector results = new Vector();
    Enumeration e = getAllUnits();
    while (e.hasMoreElements()) {
      Unit unit = (Unit)e.nextElement();
      if (unit.getLevel() == level)
        results.addElement(unit);
    }
    return results;
  }

  /** Returns true if the unit is in the default battalion list.
    */

  public boolean isDefaultBattalion(String unitName) {
    return subordinates.contains(unitName);
  }

  public void printSociety() {
    Enumeration e = units.elements();
    while (e.hasMoreElements()) {
      Unit unit = (Unit)e.nextElement();
      System.out.println(unit.toString());
    }
  }

  public static void main(String[] args) {
    Society society = new Society();
    society.createDefaultSociety();
    Enumeration e = society.units.elements();
    while (e.hasMoreElements()) {
      Unit unit = (Unit)e.nextElement();
      System.out.println(unit.toString());
    }
  }
}
