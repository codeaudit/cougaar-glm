/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.oplan;


import org.cougaar.core.society.OwnedUniqueObject;
import org.cougaar.core.society.UID;
import org.cougaar.core.society.UniqueObject;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.util.XMLize;
import org.cougaar.util.XMLizable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;

/**
 * Redeem for one Oplan and accompanying OplanContributors
 */
public class OplanCoupon extends OwnedUniqueObject
  implements Transferable, UniqueObject, Serializable, Cloneable, XMLizable
{

  private UID _oplanUID;

  public OplanCoupon(UID oplanUID,
		      ClusterIdentifier homeClusterID) {
    _oplanUID = oplanUID;
    setOwner(homeClusterID);
  }

  public OplanCoupon(UID thisUID,
		      UID oplanUID,
		      ClusterIdentifier homeClusterID) {
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

  public void setHomeClusterID(ClusterIdentifier homeClusterID) {
    setOwner(homeClusterID);
  }

  public ClusterIdentifier getHomeClusterID() {
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

  // XMLizable method for UI, other clients
  public Element getXML(Document doc) {
    return XMLize.getPlanObjectXML(this,doc);
  }

}
