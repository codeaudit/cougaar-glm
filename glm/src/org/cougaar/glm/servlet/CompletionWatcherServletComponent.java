/* 
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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

import org.cougaar.core.service.AlarmService;
import org.cougaar.core.servlet.SimpleServletSupport;
import org.cougaar.planning.servlet.BlackboardServletComponent;

public class CompletionWatcherServletComponent extends BlackboardServletComponent {
  public final void setAlarmService(AlarmService s) {
    alarmService = s;
  }
  /**
   * Get the alarm service, for subclass use.
   */
  protected AlarmService getAlarmService() {
    return alarmService;
  }

  /**
   * so a subclass can create a different servlet support just by overriding this method
   * perhaps the core should work like this?
   */
  protected SimpleServletSupport makeServletSupport () {
    if (log.isInfoEnabled())
      log.info ("Creating CompletionWatcherServletSupport, log is " + log);

    // create a new "SimpleServletSupport" instance
    return 
      new CompletionWatcherSupport (
        path,
        agentId,
        blackboardQuery,
	log,
        getBlackboardService(),
	getConfigFinder(),
	getLDMService().getFactory(),
	getLDMService().getLDM(),
	getSchedulerService(),
	getAlarmService());
  }

  protected AlarmService alarmService;
}
