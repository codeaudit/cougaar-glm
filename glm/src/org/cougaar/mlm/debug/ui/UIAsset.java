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


package org.cougaar.mlm.debug.ui;

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.PhysicalAsset;
import org.cougaar.planning.ldm.asset.AbstractAsset;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.planning.ldm.asset.TypeIdentificationPG;

/** Return a string description of a physical asset or cluster; used
 *  to display asset information in log plans, etc.
 */

public class UIAsset {

  /** Return a name for this asset:
    if typeIdentificationProperty is non-null
    then if nomenclature is defined, use it, else use typeIdentification
    else use toString
    For Requisition, use documentNumber
    For SupplySource, use vendorName
    */
    
  public static String getName(Asset asset) {
    TypeIdentificationPG tip = asset.getTypeIdentificationPG();
    String s = null;

    if (tip != null) {
      s = tip.getNomenclature();
      if (s == null || s.equals("") || s.equals("RuntimePrototype"))
        s = tip.getTypeIdentification();
    }
    
    // if it is null or looks like: org.cougaar.planning.ldm.asset.Asset@1394...
    // then use toString() for the object
    if ((s == null) || (s.indexOf("@") != -1)) 
      s = asset.toString();
    return s;
  }
  
  /*
  public static String getName(Requisition req) {
    return req.getDocumentNumber();   
  }
  */
  /** Return an asset description:
    For PhysicalAsset, 
    return name (from above) and ItemIdentificationPG.ItemIdentification
    For OrganizationAsset, return ItemIdentificationPG.AlternateItemId
    For AggregateAsset, return name (from above) and quantity
    For all other assets, return toString
    */
  public static String getDescription(Asset asset) {
    if (asset == null)
      return "";
    String s = "";
      String sn = "";
      if (asset instanceof PhysicalAsset ||
          asset instanceof AbstractAsset) {
        s = getName(asset);
        ItemIdentificationPG itemIdProp = 
          asset.getItemIdentificationPG();
        if (itemIdProp != null) {
          sn = itemIdProp.getItemIdentification();
	}
        if ((sn != null) && (sn != ""))
          s = s + " " +  sn;
      } else if (asset instanceof Organization) {
        ItemIdentificationPG itemIdProp = 
          ((Organization)asset).getItemIdentificationPG();
        if (itemIdProp != null)
          s = itemIdProp.getAlternateItemIdentification();
      } else if (asset instanceof AggregateAsset) {
        AggregateAsset aa = (AggregateAsset) asset;
        Asset a = aa.getAsset();
	String nomen = "";
	String tid = "";
	//        TypeIdentificationPG typeIdProp = 
	//	  asset.getTypeIdentificationPG();
       TypeIdentificationPG typeIdProp = 
	 a.getTypeIdentificationPG();
       if (typeIdProp != null) {
	 nomen = typeIdProp.getNomenclature();
	 tid = typeIdProp.getTypeIdentification();

	 if ((nomen != null) && (nomen != "") && (tid != null) && (tid != "")) {
	   if (tid.compareTo(nomen) != 0) {
	     s = nomen + " " + tid;
	   } else {
	     s = nomen;
	   }
	 } else if ((nomen != null) && (nomen != "")) {
	   s = nomen;
	 } else if ((tid != null) && (tid != "")) {
	   s = tid;
	 }
// 	 s = typeIdProp.getNomenclature() + " " +
// 	  typeIdProp.getTypeIdentification();
       }
	//        s = getName(a);
       /*
	Schedule sch = aa.getSchedule();
	Enumeration e = sch.getAllScheduleElements();
	int nElements = 0;
	while (e.hasMoreElements()) {
	  nElements++;
	  Object o = e.nextElement();
	  if (o instanceof QuantityRangeScheduleElement) {
	    double d = ((QuantityRangeScheduleElement)o).getStartQuantity();
	    s = s + " Start Quantity: " + d;
	  } else if (o instanceof QuantityScheduleElement) {
	    double d = ((QuantityScheduleElement)o).getQuantity();
	    s = s + " Quantity: " + d;
	  }
	}
	s = s + " Number of ScheduleElements: " + nElements;
       */
      } else {
        s = getName(asset);
      }
    return s;
  }

  /** Return a requisition (FGI) description which is:
    Document Number: documentNumber
    */

  /*
  public static String getDescription(Requisition req) {
    if (req == null)
      return "";
    String s = "";
    s = getName(req);
    return s;
  }
  */

  /**
    Return a SupplySource (FGI) description which is vendorName.
    */

  /*
  public static String getDescription(SupplySource suppSrc) {
    String s = "";
    if (suppSrc == null) {
      return s;
    } else if (suppSrc instanceof DepotSupplySource) {
      return "Depot SS";
    } else if (suppSrc instanceof DRMSSupplySource) {
      return "DRMS SS";
    } else if (suppSrc instanceof VendorSupplySource) {
      Vendor vend = ((VendorSupplySource)suppSrc).getVendor();
      if (vend != null) {
	s = vend.getVendorName();
	if (s != null && (!s.equals("")))
	  return s;
      }
    }
    return "";
  }
  */
}

