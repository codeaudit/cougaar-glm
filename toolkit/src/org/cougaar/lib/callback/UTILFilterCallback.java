/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.callback;

import org.cougaar.core.cluster.IncrementalSubscription;

/**
 * Root interface for filter callbacks.
 *
 * Filter callbacks encapsulate two ideas : 1) that we filter for
 * some object in the space of cluster objects and 2) we want to be informed
 * when new/changed/deleted things match the filter. 
 *
 * There are generally listener interfaces for every callback.
 * Very simple callbacks may not have an explicit listener class
 * paired with it.  And some callback classes share a listener class.
 *
 * The interface doesn't require a listener, but the adapter does.
 */

public interface UTILFilterCallback {
  /** 
   * used by plugins to ask the subscription direct questions,
   * like "give me all your elements." 
   * @see org.cougaar.lib.filter.UTILAllocatorPlugInAdapter#getAssets ()
   */
  IncrementalSubscription getSubscription ();

  /** 
   * The filter has matched something, this defines what to do
   * next.
   */
  void reactToChangedFilter ();

  void setExtraDebug      (boolean d);

  /**
   * Set to true if you want to see when the container changes in
   * ways that *don't* notify the listener.
   *
   * (This can make you feel better, since interestingXXX calls are
   * made even on objects that are about to disappear from your
   * container.  This can let you know that it's OK that even though
   * the listener finds the object interesting, it's not being acted
   * upon.)
   */
  void setExtraExtraDebug (boolean d);

}
        
        
                
                        
                
        
        
