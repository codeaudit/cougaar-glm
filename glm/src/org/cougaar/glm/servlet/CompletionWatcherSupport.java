package org.cougaar.glm.servlet;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.domain.*;
import org.cougaar.core.service.AlarmService;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.BlackboardQueryService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.NamingService;
import org.cougaar.core.service.SchedulerService;
import org.cougaar.core.servlet.SimpleServletSupportImpl;
import org.cougaar.util.ConfigFinder;
import org.cougaar.core.servlet.BlackboardServletSupport;

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
      NamingService ns,
      LoggingService logger,
      BlackboardService blackboard,
      ConfigFinder configFinder,
      RootFactory ldmf,
      LDMServesPlugin ldm,
      SchedulerService scheduler,
      AlarmService alarm) {
    super (path, agentId, blackboardQuery, ns, logger, blackboard, configFinder, ldmf, ldm, scheduler);
    this.alarm = alarm;
  }

  protected AlarmService alarm;
  public AlarmService getAlarmService () { return alarm; }
}
