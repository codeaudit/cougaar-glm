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
 
package org.cougaar.domain.mlm.ui.planviewer.stoplight;

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

