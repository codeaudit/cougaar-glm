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

package org.cougaar.glm.ldm.plan;

import java.io.*;
import java.util.*;

/**
 * An enumeration of known valid military (or otherwise) services.
 * The implementation attempts to keep the number of duplicate service
 * instances (e.g. that are .equals) to a minimum, but there are situations
 * where this is not practical.  Use .equals instead of ==.
 **/

public final class Service implements Serializable {
  	
  public final static Service CIVILIAN = new Service("Civilian");
  public final static Service ARMY = new Service("Army");
  public final static Service NAVY = new Service("Navy");
  public final static Service AIRFORCE = new Service("Airforce");
  public final static Service MARINE = new Service("Marine");
  public final static Service COASTGUARD = new Service("Coastguard");
  public final static Service JOINT = new Service("Joint");
  public final static Service OTHER = new Service("Other");

  private final static HashMap allTable = new HashMap(13);
  static {
    allTable.put("Civilian", CIVILIAN);
    allTable.put("Army", ARMY);
    allTable.put("Navy", NAVY);
    allTable.put("Airforce", AIRFORCE);
    allTable.put("Marine", MARINE);
    allTable.put("Coastguard", COASTGUARD);
    allTable.put("Joint", JOINT);
    allTable.put("Other", OTHER);
  }

  public static Collection getServices() { return allTable.values(); }
  public static Set getNames() { return allTable.keySet(); }

  public static Service getService(String name) {
    return (Service) allTable.get(name);
  }

  private String name;
	
  /** Constructor takes a String that represents the named Service.
   * It is preferred to use the constant Services or the getService
   * method rather than to use the constructor.
   **/
  public Service(String v) {
    if (v == null) throw new IllegalArgumentException();
    name = v.intern();
  }
	
  public String getName() {
    return name;
  }

  /** @return The name of the Service */
  public String toString() {
    return name;
  }
	
  public boolean equals(Object v) {
    // use == since name strings are interned
    return ( v instanceof Service && name == v.toString());
  }

  public int hashCode()
  {
    return name.hashCode()+3;
  }

  private void readObject(ObjectInputStream stream)
                throws ClassNotFoundException, IOException
  {
    stream.defaultReadObject();
    name = name.intern();
  }

}
