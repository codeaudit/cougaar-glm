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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;

import org.cougaar.util.StringUtility;

/** Refinement of Skill to represent Aircrew Skills
 * Example of AircrewSkill:
 *  aircraftType = C-17
 *  position = pilot
 *  qualification = air refueling (may be null)
 *  trainingLevel = A1 (1st char is flight level (A-E), 2nd char is ground level (1-4))
 *  missionCapable = true (satifies currency requirements)
 * The Skill slots are filled in as:
 *  Type = "Aircrew"
 *  Code = position
 *  Nomenclature = aircraftType+" "+position
 **/

public class AircrewSkill extends Skill {
  private String aircraftType;
  private String position;
  private String qualification;
  private String trainingLevel;
  private boolean missionCapable;

  public String getAircraftType() { return aircraftType; }
  public String getPosition() { return position; }
  public String getQualification() { return qualification; }
  public String getTrainingLevel() { return trainingLevel; }
  public boolean getMissionCapable() { return missionCapable; }

  public AircrewSkill(String aircraftType,
                      String position,
                      String qualification,
                      String trainingLevel,
                      boolean missionCapable) {
    super("Aircrew", position, aircraftType+" "+position);
    this.aircraftType = aircraftType.intern();
    this.position = position.intern();
    if (qualification != null) qualification = qualification.intern();
    this.qualification = qualification;
    this.trainingLevel = trainingLevel.intern();
    this.missionCapable = missionCapable;
  }

  /** parser-based constructor.  Requires that the argument be
   * of the form "ACType/Position/Qualification/Training/capable"
   **/
  public AircrewSkill(String s) {
    Vector v = StringUtility.parseCSV(s,'/');
    if (v.size() != 5) 
      throw new IllegalArgumentException("Invalid AircrewSkill syntax: "+s);
    aircraftType = ((String) v.elementAt(0)).intern();
    position = ((String) v.elementAt(1)).intern();
    qualification = (String) v.elementAt(2);
    if (qualification != null) qualification = qualification.intern();
    trainingLevel = ((String) v.elementAt(3)).intern();
    missionCapable = (new Boolean((String) v.elementAt(4))).booleanValue();

    this.type = "Aircrew";      // string literals are interned already
    this.code = position;
    this.nomenclature = aircraftType+" "+position;
  }

  public String toString() {
    return super.toString()+"("+aircraftType+" "+position+")";
  }

  public boolean equals(Object s) {
    if (s instanceof AircrewSkill) {
      AircrewSkill os = (AircrewSkill) s;
      return ( aircraftType == os.getAircraftType() &&
               position == os.getPosition() &&
               qualification == os.getQualification() &&
               trainingLevel == os.getTrainingLevel() &&
               missionCapable == os.getMissionCapable());
    } else {
      return false;
    }
  }

  public int hashCode() {
    return (aircraftType.hashCode()+
            position.hashCode()+
            trainingLevel.hashCode() );
    // we dont use qualification, or the Skill-level params
  }

  // make sure strings remain interned
  private void readObject(ObjectInputStream stream)
                throws ClassNotFoundException, IOException
  {
    stream.defaultReadObject();
    aircraftType = aircraftType.intern();
    position = position.intern();
    if (qualification != null)
      qualification = qualification.intern();
    trainingLevel = trainingLevel.intern();
  }
}
