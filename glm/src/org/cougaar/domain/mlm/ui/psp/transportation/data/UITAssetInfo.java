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

import java.util.*;

public class UITAssetInfo implements 
   org.cougaar.core.util.SelfPrinter, java.io.Serializable, java.lang.Cloneable {

  // The Asset Classes:

  public UITAssetInfo() {}

  public UITAssetInfo copy() {
    try {
      return (UITAssetInfo)super.clone();
    } catch (java.lang.CloneNotSupportedException noClone) {
      throw new RuntimeException("NEVER! "+noClone);
    }
  }

  /**
    * Below identifies the Asset which is being moved (eg. container)
    * that is specified by this Task.
   **/
  public String uid;

  /**
    * Below identifies the Asset which is being moved (eg. container)
    * that is specified by this Task.
    * For an individual asset the attributes would be (for example):
    *     TypeID = "NSN/2320011077155"
    *     TypeNomenclature = "M998 (HMMWV) Truck, Utility, Cargo/Troop Carrier"
    *     ItemID = "203-FSB-2320011077155-4"
    *     Quantity = 1
    * For an aggregate asset the attributes would be (for example):
    *     TypeID = "NSN/2350010684089"
    *     TypeNomenclature = "CARRIER,COMMAND POST"
    *     ItemID = ""
    *     Quantity = 30
    *
    * If( ItemID == "") Then its an AggregateAsset
    *   Else Asset
   **/
  public String TypeNomenclature; 
  public String TypeID;
  public String ItemID;
  public int Quantity; 
  public double Tons;

  /** The asset's class **/
  public int[] AssetClasses;

  public String getUID() { return uid; }
  public void setUID(String s) { uid = s; }

  public String getTypeNomenclature() { return TypeNomenclature; }
  public void setTypeNomenclature(String s) { TypeNomenclature = s; }

  public String getTypeID() { return TypeID; }
  public void setTypeID(String s) { TypeID = s; }

  public String getItemID() { return ItemID; }
  public void setItemID(String s) { ItemID = s; }

  public int getQuantity() { return Quantity; }
  public void setQuantity(int i) { Quantity = i; }

  public double getTons() { return Tons; }
  public void setTons(double i) { Tons = i; }

  public int[] getAssetClasses(){return AssetClasses;}
  public void setAssetClasses(int[] ac){AssetClasses = ac;}

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    pr.print(uid, "UID");
    pr.print(TypeNomenclature, "TypeNomenclature");
    pr.print(TypeID, "TypeID");
    pr.print(ItemID, "ItemID");
    pr.print(Quantity, "Quantity");
    pr.print(Tons, "Tons");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 7536699939182149705L;

}
