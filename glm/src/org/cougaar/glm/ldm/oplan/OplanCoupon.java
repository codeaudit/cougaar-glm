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

package org.cougaar.glm.ldm.oplan;


import org.cougaar.core.util.OwnedUniqueObject;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.plan.Transferable;

import java.io.Serializable;

/**
 * Redeem for one Oplan and accompanying OplanContributors
 */
public class OplanCoupon extends OwnedUniqueObject
  implements Transferable, UniqueObject, Serializable, Cloneable
{

  private UID _oplanUID;

  public OplanCoupon(UID oplanUID,
		      MessageAddress homeClusterID) {
    _oplanUID = oplanUID;
    setOwner(homeClusterID);
  }

  public OplanCoupon(UID thisUID,
		      UID oplanUID,
		      MessageAddress homeClusterID) {
    setUID(thisUID);
    _oplanUID = oplanUID;
    setOwner(homeClusterID);
  }

  public void setOplanUID (UID oplanUID) {
    _oplanUID = oplanUID;
  }

  public UID getOplanUID() {
    return _oplanUID;
  }

  public void setHomeClusterID(MessageAddress homeClusterID) {
    setOwner(homeClusterID);
  }

  public MessageAddress getHomeClusterID() {
    return getOwner();
  }


  // Tranferable
  public Object clone() {
    OplanCoupon newOplanCoupon = new OplanCoupon(getUID(),
						 _oplanUID,
						 getOwner());
    return newOplanCoupon;
  }

  // Tranferable
  public boolean same(Transferable trans) {
    if (trans instanceof OplanCoupon) {
      OplanCoupon other = (OplanCoupon) trans;
      if (other.getUID().equals(getUID()) &&
	  other.getOplanUID().equals(_oplanUID) &&
	  other.getHomeClusterID().equals(getOwner())) {
	return true;
      }
    }
    return false;
  }

  // Tranferable
  public void setAll(Transferable otherTransferable) {

    if (!(otherTransferable instanceof OplanCoupon)) {
      throw new IllegalArgumentException("Parameter is not OplanCoupon.");
    }

    OplanCoupon other = (OplanCoupon) otherTransferable;

    setUID(other.getUID());
    _oplanUID = other.getOplanUID();
    setOwner(other.getHomeClusterID());
  }

}
