/*
 *
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
 
package org.cougaar.glm.servlet;

import org.cougaar.core.adaptivity.OperatingMode;
import org.cougaar.core.service.AlarmService;
import org.cougaar.core.service.OperatingModeService;
import org.cougaar.glm.plugin.completion.GLMCompletionCalculator;
import org.cougaar.planning.plugin.completion.CompletionCalculator;
import org.cougaar.planning.servlet.CompletionServlet;

/**
 * A <code>Servlet</code> that generates a GLM-specific
 * summary of task completion.
 * <p><pre>
 * Load with the default URL path "/glmcompletion" using:
 *   plugin = org.cougaar.glm.servlet.GLMCompletionServlet
 * or specify a path such as "/foo":
 *   plugin = org.cougaar.glm.servlet.GLMCompletionServlet(/foo)
 * </pre>
 */
public class GLMCompletionServlet
extends CompletionServlet
{

  private static final long MILLIS_PER_DAY = 86400000L;

  private static final String LEVEL2 = "level2";

  private AlarmService alarmService;

  private OperatingMode level2Mode;
  private long cachedMaxTime = Long.MAX_VALUE;

  public void setAlarmService(AlarmService alarmService) {
    this.alarmService = alarmService;
  }

  public void unload() {
    if (alarmService != null) {
      serviceBroker.releaseService(
          this, AlarmService.class, alarmService);
      alarmService = null;
    }
    super.unload();
  }

  protected boolean haveLevel2Mode() {
    if (level2Mode == null) {
      OperatingModeService oms = (OperatingModeService)
        serviceBroker.getService(this, OperatingModeService.class, null);
      if (oms == null) return false;
      level2Mode = oms.getOperatingModeByName(LEVEL2);
      serviceBroker.releaseService(this, OperatingModeService.class, oms);
    }
    return level2Mode != null;
  }

  protected long getExecutionTime() {
    return alarmService.currentTimeMillis();
  }

  protected CompletionCalculator getCalculator() {
    long maxTime;
    if (haveLevel2Mode()) {
      int days = ((Number) level2Mode.getValue()).intValue();
      maxTime = getExecutionTime() + days * 86400000L;
    } else {
      maxTime = Long.MAX_VALUE;
    }
    if (maxTime != cachedMaxTime || calc == null) {
      cachedMaxTime = maxTime;
      calc = new GLMCompletionCalculator(maxTime);
    }
    return calc;
  }

  protected String getTitlePrefix() {
    return "GLM";
  }
}
