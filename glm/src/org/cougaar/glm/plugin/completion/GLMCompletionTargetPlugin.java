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


/*
 * File: DMPIClusterStartup.java
 * Heavily modified from original to be an LDMPlugin
 */

package org.cougaar.glm.plugin.completion;

import java.util.HashSet;
import java.util.Set;
import org.cougaar.core.adaptivity.OperatingMode;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.plugin.completion.CompletionTargetPlugin;
import org.cougaar.core.plugin.util.PluginHelper;
import org.cougaar.core.service.OperatingModeService;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.util.UnaryPredicate;

public class GLMCompletionTargetPlugin extends CompletionTargetPlugin {
  private static final String LEVEL2 = "level2";

  private static Set specialVerbs = new HashSet();

  static {
    specialVerbs.add(Constants.Verb.ProjectSupply);
    specialVerbs.add(Constants.Verb.ProjectWithdraw);
    specialVerbs.add(Constants.Verb.Supply);
    specialVerbs.add(Constants.Verb.Transport);
  }

  private OperatingMode level2Mode;

  public GLMCompletionTargetPlugin() {
    super();
    addIgnoredVerb(Constants.Verb.DetermineRequirements);
    addIgnoredVerb(Constants.Verb.GetLogSupport);
    addIgnoredVerb(Constants.Verb.MaintainInventory);
  }

  private boolean haveLevel2Mode() {
    if (level2Mode == null) {
      ServiceBroker sb = getServiceBroker();
      OperatingModeService oms = (OperatingModeService)
        sb.getService(this, OperatingModeService.class, null);
      if (oms == null) return false;
      level2Mode = oms.getOperatingModeByName(LEVEL2);
      sb.releaseService(this, OperatingModeService.class, oms);
    }
    return level2Mode != null;
  }

  protected UnaryPredicate createTasksPredicate() {
    if (haveLevel2Mode()) {
      int days = ((Number) level2Mode.getValue()).intValue();
      final long maxTime = scenarioNow + days * 86400000L;
      return new UnaryPredicate() {
          public boolean execute(Object o) {
            if (o instanceof Task) {
              Task task = (Task) o;
              Verb verb = task.getVerb();
              if (ignoredVerbs.contains(verb)) return false;
              if (specialVerbs.contains(verb)) {
                try {
                  if (PluginHelper.getEndTime(task) > maxTime) return false;
                } catch (IllegalArgumentException iae) {
                }
              }
              return true;
            }
            return false;
          }
        };
    } else {
      return super.createTasksPredicate();
    }
  }
}
