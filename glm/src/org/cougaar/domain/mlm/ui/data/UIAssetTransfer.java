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

public interface UIAssetTransfer extends UIPlanElement {
   
  /** Returns an Asset that has certain capabilities.
   * This Asset is being assigned to a cluster for use.
   *
   * @return UUID - the UUID of a physical entity or cluster that is assigned to a Cluster.
   **/
                
  UUID getUIAsset();
        
  /** Returns the receiving asset
   * @return UUID - the UUID of a physical entity or cluster that is receiving the asset
   */
        
  UUID getAssignee();
 
  /** Returns the Cluster that the asset is assigned from.
   * @return String - ClusterIdentifier representing the source of the asset
   */
        
  String getAssignor();
 
  /** Returns the LocationRangeScheduleElement for the "ownership" of the asset being transfered.
   *  @return UISchedule - schedule for ownership
   */
  UISchedule getUISchedule();

}

