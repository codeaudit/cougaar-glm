/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.filter;

import org.cougaar.lib.callback.UTILAssetListener;
import org.cougaar.lib.callback.UTILAggregationListener;

/**
 * An aggregator is a plugin that is interested in aggregations (it's products)
 * and assets.  Subclasses must add what kind of workflows they're
 * interested in.
 * 
 */

public interface UTILAggregatorPlugIn 
  extends UTILAggregationListener, UTILAssetListener {
}
