/*
 * <copyright>
 *  Copyright 2003 BBNT Solutions, LLC
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
package org.cougaar.glm.servlet;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.domain.*;
import org.cougaar.planning.ldm.*;
import org.cougaar.core.service.AlarmService;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.BlackboardQueryService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.SchedulerService;
import org.cougaar.core.servlet.SimpleServletSupportImpl;
import org.cougaar.util.ConfigFinder;
import org.cougaar.planning.servlet.BlackboardServletSupport;

/** 
 * <pre>
 * This support class offers additional services on top of the
 * SimpleServletSupport class, including access to the blackboard,
 * config finder, root factory, ldm serves plugin, and scheduler service.
 * </pre>
 */
public class CompletionWatcherSupport extends BlackboardServletSupport {
  public CompletionWatcherSupport(
      String path,
      MessageAddress agentId,
      BlackboardQueryService blackboardQuery,
      LoggingService logger,
      BlackboardService blackboard,
      ConfigFinder configFinder,
      PlanningFactory ldmf,
      LDMServesPlugin ldm,
      SchedulerService scheduler,
      AlarmService alarm) {
    super (path, agentId, blackboardQuery, logger, blackboard, configFinder, ldmf, ldm, scheduler);
    this.alarm = alarm;
  }

  protected AlarmService alarm;
  public AlarmService getAlarmService () { return alarm; }
}
