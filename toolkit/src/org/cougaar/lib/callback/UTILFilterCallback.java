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

package org.cougaar.lib.callback;

import org.cougaar.core.blackboard.IncrementalSubscription;

/**
 * <pre>
 * Root interface for filter callbacks.
 *
 * Filter callbacks encapsulate two ideas : 1) that we filter for
 * some object in the space of cluster objects and 2) we want to be logger.informed
 * when new/changed/deleted things match the filter. 
 *
 * There are generally listener interfaces for every callback.
 * Very simple callbacks may not have an explicit listener class
 * paired with it.  And some callback classes share a listener class.
 *
 * The interface doesn't require a listener, but the adapter does.
 * </pre>
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

  //  void setExtraDebug      (boolean d);

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
  //  void setExtraExtraDebug (boolean d);

}
        
        
                
                        
                
        
        
