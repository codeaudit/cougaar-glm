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

// import org.cougaar.planning.ldm.plan.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;


/** Implementation of Capability.  
 * @author ALPINE <alpine-software@bbn.com>
 *
 */
	
public final class CapabilityImpl 
  implements Capability, Serializable, Cloneable
{
  	
  private String capability;
	
  //no-arg constructor
  public CapabilityImpl() {
    super();
  }
	
  /** Constructor takes a String that represents the capability */
  public CapabilityImpl(String c) {
    if (c != null) c = c.intern();
    capability = c;
  }
	
  /** @return String toString returns the String that represents the capability */
  public String toString() {
    return capability;
  }
	
  /** Capabilities are equal IFF they encapsulate the same string
   */
  public boolean equals(Object c) {
    return (capability != null && c instanceof CapabilityImpl && 
            capability.equals(c.toString()));
  }
		
  /** Since these instances' identifies are Value based, we ensure the hashCode of
   * any instance of this class is derived from the Value.
   * @return int hashCode value for a Capability instance.
   */
  public int hashCode()
  {
    return capability.hashCode();
  }

  private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
    stream.defaultReadObject();
    if (capability != null) capability = capability.intern();
  }

}
