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

package org.cougaar.mlm.ui.data;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.AssetTransfer;
import org.cougaar.planning.ldm.plan.PlanElement;

public class UIAssetTransferImpl extends UIPlanElementImpl implements UIAssetTransfer {

  public UIAssetTransferImpl(AssetTransfer assetTransfer) {
    super((PlanElement)assetTransfer);
  }
   
  /** Returns an Asset that has certain capabilities.
   * This Asset is being assigned to another asset cluster for use.
   *
   * @return UUID - the UUID of a physical entity or cluster that is assigned to another asset
   **/
                
  public UUID getUIAsset() {
    Asset asset = ((AssetTransfer)planElement).getAsset();
    if (asset != null)
      return new UUID(asset.getUID().toString());
    else
      return null;
  }
        
  /** Returns the receiving asset 
   * @return UUID - the UUID of a physical entity or cluster that is receiving the asset
   */
        
  public UUID getAssignee() {
    return new UUID(((AssetTransfer)planElement).getAssignee().getUID().toString());
  }
 
  /** Returns the Cluster that the asset is assigned from.
   * @return String - ClusterIdentifier representing the source of the asset
   */
        
  public String getAssignor() {
    return ((AssetTransfer)planElement).getAssignor().getAddress();
  }
 
  /** Returns the LocationRangeScheduleElement for the "ownership" of the asset being transfered.
   *  @return UIScheduleElement - schedule for ownership
   */
  public UISchedule getUISchedule() {
    return new UIScheduleImpl(((AssetTransfer)planElement).getSchedule());
  }

}

