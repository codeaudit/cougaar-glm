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

import org.cougaar.lib.callback.UTILAllocationListener;
import org.cougaar.lib.callback.UTILAssetListener;

/**
 * An allocator is a plugin that is interested in allocations
 * and assets.  Subclasses must add what kind of workflow they're
 * interested in (single task or generic workflow filter callbacks).
 * 
 * UTILSimpleAllocatorPlugIn    - single task workflow
 * UTILBufferingAllocatorPlugIn - generic workflow
 *
 * @see org.cougaar.lib.plugin.plugins.UTILSimpleAllocatorPlugIn
 * @see UTILBufferingAllocatorPlugIn
 */

public interface UTILAllocatorPlugIn 
  extends UTILAllocationListener, UTILAssetListener {
}
