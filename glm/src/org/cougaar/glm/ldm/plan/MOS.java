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

/**
 *  MOS is a representation of a Skill of a MilitaryPerson
 */

package org.cougaar.glm.ldm.plan;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

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
