/* 
 * <copyright>
 * Copyright 2002 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).

 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.glm.servlet;

import javax.servlet.Servlet;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.NamingService;
import org.cougaar.core.plugin.LDMService;
import org.cougaar.core.plugin.PluginBindingSite;
import org.cougaar.util.ConfigFinder;
import org.cougaar.core.service.AlarmService;
import org.cougaar.core.service.SchedulerService;
import org.cougaar.core.servlet.BlackboardServletComponent;
import org.cougaar.core.servlet.SimpleServletSupport;

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
      log.info ("Creating CompletionWatcherServletSupport");
    System.out.println ("log is " + log);
    // create a new "SimpleServletSupport" instance
    return 
      new CompletionWatcherSupport (
        path,
        agentId,
        blackboard,
        ns,
	log,
	getConfigFinder(),
	getLDMService().getFactory(),
	getLDMService().getLDM(),
	getSchedulerService(),
	getAlarmService());
  }

  protected AlarmService alarmService;
}
