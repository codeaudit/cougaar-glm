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

package org.cougaar.lib.callback;

import org.cougaar.core.blackboard.IncrementalSubscription;

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
   * @see org.cougaar.lib.filter.UTILAllocatorPluginAdapter#getAssets ()
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
        
        
                
                        
                
        
        
