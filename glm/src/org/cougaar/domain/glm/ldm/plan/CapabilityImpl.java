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

// import org.cougaar.domain.planning.ldm.plan.*;

import org.cougaar.domain.glm.ldm.plan.Capability;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.Serializable;


/** Implementation of Capability.  
 * @author ALPINE <alpine-software@bbn.com>
 * @version $Id: CapabilityImpl.java,v 1.1 2000-12-20 18:18:16 mthome Exp $
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
