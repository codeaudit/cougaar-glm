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
