/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

package org.cougaar.glm.plugin.completion;

import java.util.HashSet;
import java.util.Set;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.plugin.completion.CompletionCalculator;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.util.UnaryPredicate;

/**
 */
public class GLMCompletionCalculator extends CompletionCalculator {

  private static final Set ignoredVerbs = new HashSet();
  private static final Set specialVerbs = new HashSet();

  static {
    ignoredVerbs.add(Constants.Verb.DetermineRequirements);
    ignoredVerbs.add(Constants.Verb.GetLogSupport);
    ignoredVerbs.add(Constants.Verb.MaintainInventory);

    specialVerbs.add(Constants.Verb.ProjectSupply);
    specialVerbs.add(Constants.Verb.ProjectWithdraw);
    specialVerbs.add(Constants.Verb.Supply);
    specialVerbs.add(Constants.Verb.Transport);
  }

  private final long maxTime;

  public GLMCompletionCalculator(long maxTime) {
    this.maxTime = maxTime;
  }

  protected UnaryPredicate createPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Task) {
          Task task = (Task) o;
          Verb verb = task.getVerb();
          if (!ignoredVerbs.contains(verb)) {
            if (maxTime < Long.MAX_VALUE &&
                specialVerbs.contains(verb)) {
              long endTime;
              try {
                endTime = PluginHelper.getEndTime(task);
              } catch (IllegalArgumentException iae) {
                // never?
                endTime = Long.MIN_VALUE;
              }
              if (endTime > maxTime) {
                return false;
              }
            }
            return true;
          }
        }
        return false;
      }
    };
  }
}
