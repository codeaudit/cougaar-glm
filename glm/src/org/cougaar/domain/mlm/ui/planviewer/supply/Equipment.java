/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.supply;

import java.util.Vector;

class Equipment {
  String name;
  String nomenclature;
  String superiorName;
  Vector subordinateNames;
  int level;

  public Equipment(String name, String nomenclature) {
    this.name = name;
    this.nomenclature = nomenclature;
    this.superiorName = null;
    this.subordinateNames = null;
    this.level = -1;
  }

  public Equipment(String name, String nomenclature, Vector subordinateNames) {
    this.name = name;
    this.nomenclature = nomenclature;
    this.superiorName = null;
    this.subordinateNames = subordinateNames;
    this.level = -1;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setSubordinateNames(Vector subordinateNames) {
    this.subordinateNames = subordinateNames;
  }

  public Vector getSubordinateNames() {
    return subordinateNames;
  }

  public void addSubordinateName(String name) {
    if (subordinateNames == null)
      subordinateNames = new Vector();
    subordinateNames.addElement(name);
  }

  public void setSuperiorName(String name) {
    this.superiorName = name;
  }

  public String getSuperiorName() {
    return superiorName;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public int getLevel() {
    return level;
  }

  public String getNomenclature() {
    return nomenclature;
  }

  public String toString() {
    String s = "Name: " + name;
    if (superiorName != null)
      s = s + " superior: " + superiorName;
    s = s + " level: " + level + " ";
    if (subordinateNames != null)
      for (int i = 0; i < subordinateNames.size(); i++)
        s = s + subordinateNames.elementAt(i) + " ";
    s = s.trim();
    return s;
  }

}

