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

package org.cougaar.mlm.plugin.organization;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.plan.AssetTransfer;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.plugin.asset.AssetReportPlugin;
import org.cougaar.util.UnaryPredicate;

/**
 * OrgReportPlugin manages REPORTFORDUTY and REPORTFORSERVICE relationships
 * Handles both expansion and allocation of these tasks.
 * @see org.cougaar.planning.plugin.legacy.SimplifiedPlugin
 * @see org.cougaar.planning.plugin.legacy.SimplifiedPluginTest
 */
public class OrgReportPlugin extends AssetReportPlugin
{

  /**
   * getTaskPredicate - returns task predicate for task subscription
   * Default implementation subscribes to all non-internal tasks. Derived classes
   * should probably implement a more specific version.
   * 
   * @return UnaryPredicate - task predicate to be used.
   */
  protected UnaryPredicate getTaskPredicate() {
    return allReportTaskPred();
  }

  protected UnaryPredicate getAssetTransferPred() {
    return allReportAssetTransferPred();
  }

  // #######################################################################
  // BEGIN predicates
  // #######################################################################
  
  // predicate for getting allocatable tasks of report for duty
  private static UnaryPredicate allReportTaskPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof Task) {
          Task task = (Task) o;
	  if ((task.getVerb().equals(Constants.Verb.REPORTFORDUTY)) ||
              (task.getVerb().equals(Constants.Verb.REPORTFORSERVICE))) {
	    return true;
          }
	}
	return false;
      }
    };
  }

  private static UnaryPredicate allReportAssetTransferPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof AssetTransfer) {
          Task t = ((AssetTransfer)o).getTask();
          return ((t.getVerb().equals(Constants.Verb.REPORTFORDUTY)) ||
                  (t.getVerb().equals(Constants.Verb.REPORTFORSERVICE)));
        }
        return false;
      }
    };
  }
}
