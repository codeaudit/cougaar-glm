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

package org.cougaar.glm.ldm.oplan;


import java.io.Serializable;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.util.OwnedUniqueObject;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.planning.ldm.plan.Transferable;

/**
 * Redeem for one Oplan and accompanying OplanContributors
 */
public class OplanCoupon extends OwnedUniqueObject
  implements Transferable, UniqueObject, Serializable, Cloneable
{

  private UID _oplanUID;
  private String _oplanQueryFile;
  private String _oplanID;

  public OplanCoupon(MessageAddress homeClusterID) { 
    setOwner(homeClusterID);
  }

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

  public OplanCoupon(UID thisUID,
		      UID oplanUID,
		      MessageAddress homeClusterID,
                      String queryFile,
                      String oplanID) {
    setUID(thisUID);
    _oplanUID = oplanUID;
    setOwner(homeClusterID);
    setOplanQueryFile(queryFile);
    setOplanID(oplanID);
  }

  public void setOplanID (String oplanID) {
    _oplanID = oplanID;
  }

  public String getOplanID() {
    return _oplanID;
  }

  public void setOplanUID (UID oplanUID) {
    _oplanUID = oplanUID;
  }

  public UID getOplanUID() {
    return _oplanUID;
  }

  public void setOplanQueryFile (String oplanQueryFile) {
    _oplanQueryFile = oplanQueryFile;
  }

  public String getOplanQueryFile() {
    return _oplanQueryFile;
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
						 getOwner(),
                                                 getOplanQueryFile(),
                                                 getOplanID());
    return newOplanCoupon;
  }

  // Tranferable
  public boolean same(Transferable trans) {
    if (trans instanceof OplanCoupon) {
      OplanCoupon other = (OplanCoupon) trans;
      if (other.getUID().equals(getUID()) &&
	  other.getOplanUID().equals(_oplanUID) &&
	  other.getHomeClusterID().equals(getOwner()) &&
          other.getOplanQueryFile().equals(getOplanQueryFile()) &&
          other.getOplanID().equals(getOplanID())) {
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
    setOplanQueryFile(other.getOplanQueryFile());
    setOplanID(other.getOplanID());
  }

}
