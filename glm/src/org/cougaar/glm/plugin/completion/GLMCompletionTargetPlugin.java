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

import org.cougaar.core.adaptivity.OperatingMode;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.planning.plugin.completion.CompletionCalculator;
import org.cougaar.planning.plugin.completion.CompletionTargetPlugin;
import org.cougaar.core.service.OperatingModeService;
import org.cougaar.util.UnaryPredicate;

public class GLMCompletionTargetPlugin 
extends CompletionTargetPlugin 
{

  private static final long MILLIS_PER_DAY = 86400000L;

  private static final String LEVEL2 = "level2";

  private OperatingMode level2Mode;
  private long cachedMaxTime = Long.MAX_VALUE;

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

  protected CompletionCalculator getCalculator() {
    long maxTime;
    if (haveLevel2Mode()) {
      int days = ((Number) level2Mode.getValue()).intValue();
      maxTime = scenarioNow + days * MILLIS_PER_DAY;
    } else {
      maxTime = Long.MAX_VALUE;
    }
    if (maxTime != cachedMaxTime || calc == null) {
      cachedMaxTime = maxTime;
      calc = new GLMCompletionCalculator(maxTime);
    }
    return calc;
  }

}
