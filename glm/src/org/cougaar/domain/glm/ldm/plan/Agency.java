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

package org.cougaar.domain.glm.ldm.plan;

import java.io.*;
import java.util.*;

/**
 * An enumeration of known valid military and civilian/idustrial agencies.
 * An Agency is the highest-level meaningful organization description 
 * associated with a lower-level organization.  
 * Military Agencies are generally the CINC-level descriptions of
 * affiliation. E.g. Usually "USTC" rather than "TCJ6".
 *
 * The implementation attempts to keep the number of duplicate Agency
 * instances (e.g. that are .equals) to a minimum, but there are situations
 * where this is not practical.  Use .equals instead of ==.
 **/

public final class Agency implements Serializable {

  private final static HashMap allTable = new HashMap(13);
  	
  /*
  FORSCOM
  CENTCOM
  DLA
  TRANSCOM
  EUCOM
  PACOM
  COALITION
  HOSTNATION
  ACOM
  */

  public final static Agency FORSCOM = new Agency("FORSCOM");
  public final static Agency CENTCOM = new Agency("CENTCOM");
  public final static Agency DLA = new Agency("DLA");
  public final static Agency TRANSCOM = new Agency("TRANSCOM");
  public final static Agency EUCOM = new Agency("EUCOM");
  public final static Agency PACOM = new Agency("PACOM");
  public final static Agency COALITION = new Agency("COALITION");
  public final static Agency HOSTNATION = new Agency("HOSTNATION");
  public final static Agency ACOM = new Agency("ACOM");
  static {
    allTable.put("FORSCOM", FORSCOM);
    allTable.put("CENTCOM", CENTCOM);
    allTable.put("DLA", DLA);
    allTable.put("TRANSCOM", TRANSCOM);
    allTable.put("EUCOM", EUCOM);
    allTable.put("PACOM", PACOM);
    allTable.put("COALITION", COALITION);
    allTable.put("HOSTNATION", HOSTNATION);
    allTable.put("ACOM", ACOM);
  }

  public static Collection getAgencies() { return allTable.values(); }
  public static Set getNames() { return allTable.keySet(); }

  public static Agency getAgency(String name) {
    return (Agency) allTable.get(name);
  }

  private String name;
	
  /** Constructor takes a String that represents the named Agency.
   * It is preferred to use the constant Agencies or the getAgency
   * method rather than to use the constructor.
   **/
  public Agency(String v) {
    if (v == null) throw new IllegalArgumentException();
    name = v.intern();
  }
	
  public String getName() {
    return name;
  }

  /** @return The name of the Agency */
  public String toString() {
    return name;
  }
	
  public boolean equals(Object v) {
    // use == since name strings are interned
    return ( v instanceof Agency && name == v.toString());
  }

  public int hashCode()
  {
    return name.hashCode();
  }

  private void readObject(ObjectInputStream stream)
                throws ClassNotFoundException, IOException
  {
    stream.defaultReadObject();
    name = name.intern();
  }

}
