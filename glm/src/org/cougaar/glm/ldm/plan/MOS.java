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
