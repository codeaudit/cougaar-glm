/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.AssetTransfer;
import org.cougaar.domain.planning.ldm.plan.PlanElement;

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

