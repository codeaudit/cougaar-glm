/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plan;

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
