/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm.plan;
import java.io.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

/** Abstract Representation of a personal skill or capability.
 * Examples of Skills:
 *  MOS/11B = INFANTRYMAN:        Army Enlisted: Military Occupational Speciality
 *  ASI/B4  = SNIPER:             Army Enlisted: Additional Skill Identifier
 *  SQI/P   = PARACHUTIST:        Army Enlisted: Special Qualification IDentifier
 *  AOC/15A = AVIATION, GENERAL:  Army Commissioned Officer: AreaOfConcentration
 *  OSC/B2  = UH­60 PILOT:        Army Commissioned Officer: Officer Skill Codes
 **/

public class Skill implements Serializable {
  protected String type;          // eg "MOS"
  protected String code;          // eg "11B" 
  protected String nomenclature;  // eg "INFANTRYMAN: Army Enlisted: Military Occupational"
  
  public String getType() { return type; }
  public String getCode() { return code; }
  public String getNomenclature() { return nomenclature; }

  /** empty constructor for use by subs, which will need to fill out 
   * type, code, nomenclature members themselves.
   **/
  protected Skill() {}

  /** 
   * @param type Skill class, e.g. MOS, ASI, AOC, SQI, etc.
   * @param code Skill code, e.g. 11B, B4, P, etc
   * @param nomenclature Human-readable description of skill, may be null.
   **/
  public Skill(String type, String code, String nomenclature) {
    this.type = type.intern();
    this.code = code.intern();
    this.nomenclature = nomenclature;
  }

  /** equivalent to Skill(type, code, null). **/
  public Skill(String type, String code) {
    this(type,code,null);
  }

  /** parser constructor: expects "TYPE/CODE[/NOMENCLATURE]" **/
  public Skill(String s) {
    int s1 = s.indexOf('/');
    if (s1 == -1) throw new IllegalArgumentException("Invalid Skill syntax: "+s);
    int s2 = s.indexOf('/',s1+1);
    String t = s.substring(0,s1);
    String c;
    String n = null;
    if (s2 == -1) {
      c = s.substring(s1+1);
    } else {
      c = s.substring(s1+1,s2);
      n = s.substring(s2+1);
    }
    this.type = t.intern();
    this.code = c.intern();
    this.nomenclature=n;
    
  }

  /** alternate parser constructor **/
  public static Skill newSkill(String s) {
    return new Skill(s);
  }

  /** @return "TYPE/CODE" **/
  public String toString() {
    return type+"/"+code;
  }

  /** @return true iff type and code are the same. **/
  public boolean equals(Object s) {
    if (s.getClass() == this.getClass()) { // must be exact class!
      Skill os = (Skill) s;
      // we can use == because type and code are always interned
      return ( type == os.getType() &&
               code == os.getCode() ); 
    } else {
      return false;
    }
  }

  public int hashCode() {
    return type.hashCode()+code.hashCode();
  }
      
  // make sure type and code remain interned.
  private void readObject(ObjectInputStream stream)
                throws ClassNotFoundException, IOException
  {
    stream.defaultReadObject();
    type = type.intern();
    code = code.intern();
  }

  // some utility predicates for Skill types:
  // Note that the use of == instead of .equals is both intentional 
  // and safe: the types are interned and string literals are always
  // interned.
  /** A Predicate which selects for MOS skill type **/
  public static final UnaryPredicate mosP = new UnaryPredicate() {
      public boolean execute(Object o) { 
        return (o instanceof Skill && ((Skill)o).getType() == "MOS");
      }};
  /** A Predicate which selects for Aircrew skill type **/
  public static final UnaryPredicate aircrewP = new UnaryPredicate() {
      public boolean execute(Object o) { 
        return (o instanceof Skill && ((Skill)o).getType() == "Aircrew");
      }};
  /** A Predicate which selects for ASI skill type **/
  public static final UnaryPredicate asiP = new UnaryPredicate() {
      public boolean execute(Object o) { 
        return (o instanceof Skill && ((Skill)o).getType() == "ASI");
      }};
  /** A Predicate which selects for SQI skill type **/
  public static final UnaryPredicate sqiP = new UnaryPredicate() {
      public boolean execute(Object o) { 
        return (o instanceof Skill && ((Skill)o).getType() == "SQI");
      }};
  /** A Predicate which selects for AOC skill type **/
  public static final UnaryPredicate aocP = new UnaryPredicate() {
      public boolean execute(Object o) { 
        return (o instanceof Skill && ((Skill)o).getType() == "AOC");
      }};
  /** A Predicate which selects for OSC skill type **/
  public static final UnaryPredicate oscP = new UnaryPredicate() {
      public boolean execute(Object o) { 
        return (o instanceof Skill && ((Skill)o).getType() == "OSC");
      }};
}
