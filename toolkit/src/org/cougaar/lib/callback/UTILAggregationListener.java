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

import org.cougaar.planning.ldm.plan.Aggregation;
import org.cougaar.planning.ldm.plan.Task;

/**
 * <pre>
 * Listener intended to be used by all aggregators.
 *
 * Being an aggregation listener now means participating in a 2 step process.
 * When an aggregation changes, it can change in one of two ways, each of which the 
 * the listener can play a role in.
 *
 * Note that below, when default behavior is mentioned, it is in ???PluginAdapter.
 *  
 * 1) The aggregation can fail.  (handleFailedAggregation)
 * Some downstream allocation can fail, and if this happens, the listener will
 * be called with the aggregation.  
 *
 * The listener can handle this in one of two ways :
 *  a) rescind the aggregation and replan and reallocate OR
 *  b) give up and report the failed aggregation to a superior.  An example of this
 *     approach is in UTILXXXPlugin
 *
 * Since this is important, THERE IS NO DEFAULT BEHAVIOR for this option.
 *
 * 2) The aggregation is fine.  (reportChangedAggregation)
 * Everything is fine with the aggregation, and the listener is asked to report the results
 * of the aggregation to its superior.
 *  
 * The listener can handle this by simply reporting the changed aggregation by calling
 * updateAllocationResult.
 *  
 * ???Default behavior is to do this.  
 *
 * </pre>
 * @see org.cougaar.lib.callback.UTILAggregationCallback
 * @see org.cougaar.lib.callback.UTILExpansionListener
 * @see org.cougaar.lib.callback.UTILAllocationListener
 */

public interface UTILAggregationListener extends UTILFilterCallbackListener {
  /** 
   * Defines tasks you find interesting. 
   * @param t Task to check for interest
   * @return boolean true if task is interesting
   */
  boolean interestingParentTask(Task t);
  
  /**
   * Defines conditions for rescinding tasks.
   *
   * When returns TRUE, handleRescindedAggregation is called.
   *
   * See comment on UTILAggregatorPluginAdapter.needToRescind.
   *
   * @param agg Aggregation to check for
   * @return boolean true if task needs to be rescinded
   * @see #handleRescindedAggregation
   * @see org.cougaar.lib.filter.UTILAggregatorPluginAdapter#needToRescind
   */
  boolean needToRescind (Aggregation agg);

  /**
   * An aggregation has failed.  It's up to the plugin how to deal with the
   * failure.
   *
   * See comment on UTILAggregatorPluginAdapter.needToRescind.
   *
   * @param agg aggregation that failed/has been rescinded by listener
   * @return true if handled
   * @see #needToRescind
   * @see org.cougaar.lib.filter.UTILAggregatorPluginAdapter#needToRescind
   */
  boolean handleRescindedAggregation(Aggregation agg);

  /**
   * Updates and publishes allocation result of aggregation.
   *
   * @param agg aggregation to report
   */
  void reportChangedAggregation(Aggregation agg);

  /**
   * What to do with a successful aggregation. 
   * For implementers who DON'T extend UTILPluginAdapter,
   * this should be implemented with an empty body.
   * 
   * Called after reportChangedAggregation when needToRescind returns FALSE.
   *
   * @param agg the returned successful aggregation
   * @see #needToRescind
   * @see org.cougaar.lib.filter.UTILAggregatorPluginAdapter#needToRescind
   */
  void handleSuccessfulAggregation(Aggregation agg);

  /** 
   * Called when an aggregation is removed from the cluster.
   * I.e. an upstream cluster removed an allocation, and this 
   * has resulted in this aggregation being removed.
   *
   * If the plugin maintains some local state of the availability
   * of assets, it should update them here.
   */
  void handleRemovedAggregation (Aggregation agg);

  /**
   * Public version of publishRemove
   *
   * Called when needToRescind returns TRUE.
   *
   * @param agg aggregation to remove
   */
  void publishRemovalOfAggregation(Aggregation agg);
}
