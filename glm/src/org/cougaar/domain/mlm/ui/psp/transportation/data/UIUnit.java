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
 
package org.cougaar.domain.mlm.ui.psp.transportation.data;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;

public class UIUnit implements  org.cougaar.core.util.SelfPrinter, java.io.Serializable
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

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    pr.print(location, "Location");
    pr.print(ms2525IconName, "Ms2525IconName");
    pr.print(selfAssetUID, "SelfAssetUID");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 7125315612284475001L;

}
