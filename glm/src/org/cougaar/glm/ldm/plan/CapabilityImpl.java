/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

// import org.cougaar.planning.ldm.plan.*;

import org.cougaar.glm.ldm.plan.Capability;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
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
