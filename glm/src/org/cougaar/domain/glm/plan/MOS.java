/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

/**
 *  MOS is a representation of a Skill of a MilitaryPerson
 */

package org.cougaar.domain.glm.plan;
import java.io.*;

public final class MOS implements Serializable {

  private String code;

  /** Answer with the String representation of the code. */
  public String getCode() { return code; }

    /** Implementation of the comparison */
  public boolean equals(Object obj) {
    if (obj instanceof MOS) {
      return getCode().equals(((MOS) obj).getCode());
    }
    return false;
  }

  public MOS( String string ) {
    if ( string == null ) {
      throw new IllegalArgumentException ("Null valued strings are not allowed");
    }
    code = string.intern();
  }

  public String toString() {
    return code;
  }
  public int hashCode() {
    return code.hashCode()+3;
  }

  private void readObject(ObjectInputStream stream)
                throws ClassNotFoundException, IOException
  {
    stream.defaultReadObject();
    if (code != null) code = code.intern();
  }

} 
