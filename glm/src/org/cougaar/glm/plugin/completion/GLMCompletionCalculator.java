/*
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

package org.cougaar.glm.plugin.completion;

import java.util.HashSet;
import java.util.Set;
import org.cougaar.planning.plugin.completion.CompletionCalculator;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
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
