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

package org.cougaar.glm.ldm.plan;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

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
