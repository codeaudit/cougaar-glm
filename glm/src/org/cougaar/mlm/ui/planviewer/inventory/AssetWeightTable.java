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
 
package org.cougaar.mlm.ui.planviewer.inventory;

import java.util.Hashtable;

public class AssetWeightTable {
  Hashtable weightTable;

  public AssetWeightTable() {
    weightTable = new Hashtable();
    weightTable.put("DODIC/A131", new Double(0.1));
    weightTable.put("DODIC/A576", new Double(0.37));
    weightTable.put("DODIC/A576", new Double(0.37));
    weightTable.put("DODIC/A975", new Double(2));
    weightTable.put("DODIC/A986", new Double(1.67));
    weightTable.put("DODIC/B129", new Double(0.81));
    weightTable.put("DODIC/C380", new Double(69));
    weightTable.put("DODIC/C787", new Double(88.4));
    weightTable.put("DODIC/D502", new Double(110.75));
    weightTable.put("DODIC/D505", new Double(99.62));
    weightTable.put("DODIC/D514", new Double(110.25));
    weightTable.put("DODIC/D528", new Double(109.37));
    weightTable.put("DODIC/D532", new Double(50));
    weightTable.put("DODIC/D533", new Double(42.5));
    weightTable.put("DODIC/D540", new Double(21));
    weightTable.put("DODIC/D541", new Double(30.5));
    weightTable.put("DODIC/D544", new Double(99.62));
    weightTable.put("DODIC/D563", new Double(109.25));
    weightTable.put("DODIC/D579", new Double(103.75));
    weightTable.put("DODIC/D864", new Double(108.75));
    weightTable.put("DODIC/G815", new Double(3.32));
    weightTable.put("DODIC/G815", new Double(3.32));
    weightTable.put("DODIC/G826", new Double(5.67));
    weightTable.put("DODIC/G826", new Double(5.67));
    weightTable.put("DODIC/H104", new Double(872.57));
    weightTable.put("DODIC/H116", new Double(40.5));
    weightTable.put("DODIC/H163", new Double(35.5));
    weightTable.put("DODIC/H164", new Double(35));
    weightTable.put("DODIC/H183", new Double(31.75));
    weightTable.put("DODIC/H183", new Double(31.75));
    weightTable.put("DODIC/N285", new Double(3.41));
    weightTable.put("DODIC/N286", new Double(2.86));
    weightTable.put("DODIC/N340", new Double(3.75));
    weightTable.put("DODIC/N464", new Double(3.75));
    weightTable.put("DODIC/N523", new Double(0.12));
    weightTable.put("DODIC/PV18", new Double(89));
    weightTable.put("DODIC/PV29", new Double(185));
  }

  /** The string is the asset nomenclature:type identification; we strip off the
    nomenclature and use that as an index in the hashtable.
    */

  public double get(String s) {
    int i = s.indexOf(':');
    if (i != -1) {
        //      s = s.substring(0, i); 
        s = s.substring(i+1); // MSB/JEB : Use the DODIC end of the concatenated asset name
    }
    Double d = (Double)weightTable.get(s);
    if (d == null)
      return 1.0;
    return d.doubleValue();
  }

}

