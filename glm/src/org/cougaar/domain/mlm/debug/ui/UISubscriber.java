/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */


package org.cougaar.domain.mlm.debug.ui;

import org.cougaar.core.cluster.IncrementalSubscription;

public interface UISubscriber {

  /** Tell the subscriber that their subscription has changed.
   */

    public void subscriptionChanged(IncrementalSubscription container);

}
