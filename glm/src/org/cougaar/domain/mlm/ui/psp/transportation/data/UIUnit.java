/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation.data;

import org.cougaar.domain.glm.plan.GeolocLocation;

public class UIUnit implements  org.cougaar.util.SelfPrinter, java.io.Serializable
{
     /**
       * Location of Unit
       *    a.) first locate "SELF" Organizational Asset
       *    b.) location = org.getMilitaryOrgPG().getHomeLocation();
       **/
     private GeolocLocation location;

     /**
       * The 2525B symbol name, reachable from something like:
       * OrgAsset.getMilitaryOrgPG().getHierarchy2525()
       **/
     private String ms2525IconName;

     /** UID Of "self" organization asset at cluster **/
     private String selfAssetUID;

     

     public String getSelfAssetUID() { return selfAssetUID; }
     public void setSelfAssetUID(String uid) { this.selfAssetUID = uid; }

     public GeolocLocation getLocation() {return location;}
     public void setLocation(GeolocLocation loc) {
         this.location = loc;
     }

     public String getMs2525IconName() {return ms2525IconName;}
     public void setMs2525IconName(String name) {
         this.ms2525IconName = name;
     }

  public void printContent(org.cougaar.util.AsciiPrinter pr) {
    pr.print(location, "Location");
    pr.print(ms2525IconName, "Ms2525IconName");
    pr.print(selfAssetUID, "SelfAssetUID");
  }

  public String toString() {
    return org.cougaar.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 7125315612284475001L;

}
