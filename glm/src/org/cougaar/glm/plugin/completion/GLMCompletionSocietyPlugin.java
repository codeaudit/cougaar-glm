/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.planning.plugin.completion.CompletionSocietyPlugin;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.mlm.plugin.organization.GLSInitServlet;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.UnaryPredicate;

public class GLMCompletionSocietyPlugin extends CompletionSocietyPlugin {
  private static final long DEFAULT_SEND_OPLAN_DELAY = 60000L;
  private static final long DEFAULT_PUBLISH_GLS_DELAY = 10000L;
  private static final long DEFAULT_WAIT_GLS_DELAY = 1000L;
  private static final String forRoot = "ForRoot";
  private long SEND_OPLAN_DELAY = DEFAULT_SEND_OPLAN_DELAY;
  private long PUBLISH_GLS_DELAY = DEFAULT_PUBLISH_GLS_DELAY;
  private long WAIT_GLS_DELAY = DEFAULT_WAIT_GLS_DELAY;
  private long timeout;
  private CollectionSubscription oplanSubscription;
  private CollectionSubscription glsSubscription;
  private static UnaryPredicate oplanPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Oplan);
    }
  };
  
  /**
   * This predicate selects for root tasks injected by the GLSGUIInitPlugin
   **/
  private UnaryPredicate glsPredicate =
    new UnaryPredicate() {
      public boolean execute(Object o) {
	if (!(o instanceof Task)) return false;
	Task task = (Task) o;
	if (!task.getVerb().equals(Constants.Verb.GETLOGSUPPORT)) return false;
	if (!task.getSource().equals(getMessageAddress())) return false;
	if (!task.getDestination().equals(getMessageAddress())) return false;
	return (task.getPrepositionalPhrase(forRoot) != null);
      }
    };
  private CompletionAction sendOplanAction =
    new CompletionAction() {
      public boolean checkCompletion(boolean haveLaggard) {
        if (haveLaggard) {
          timeout = now + SEND_OPLAN_DELAY;
        } else if (now > timeout) {
          sendOplan();
          timeout = now + PUBLISH_GLS_DELAY;
          return true;
        }
        return false;
      }
      public String toString() {
        return "CompletionAction(sendOplan)";
      }
    };
  private CompletionAction publishGLSAction =
    new CompletionAction() {
      public boolean checkCompletion(boolean haveLaggard) {
        if (haveLaggard) {
          timeout = now + WAIT_GLS_DELAY;
        } else if (now > timeout) {
          publishGLS();
          return  true;
        }
        return false;
      }
      public String toString() {
        return "CompletionAction(publishGLS)";
      }
    };
  private CompletionAction waitGLSAction =
    new CompletionAction() {
      public boolean checkCompletion(boolean haveLaggard) {
        if (haveLaggard) {
          timeout = now + PUBLISH_GLS_DELAY;
        } else if (now > timeout) {
          return !blackboard.query(glsPredicate).isEmpty();
        }
        return false;
      }
      public String toString() {
        return "CompletionAction(waitGLS)";
      }
    };
  private CompletionAction[] myCompletionActions = {
//     sendOplanAction,
//     publishGLSAction
    waitGLSAction
  };

  public void setupSubscriptions() {
    oplanSubscription = (CollectionSubscription)
      blackboard.subscribe(oplanPredicate, false);
    glsSubscription = (CollectionSubscription)
      blackboard.subscribe(glsPredicate, false);
    super.setupSubscriptions();
  }

  protected CompletionAction[] getCompletionActions() {
    return myCompletionActions;
  }

  private void sendOplan() {
    GLSInitServlet.Request req =
      new GLSInitServlet.Request(GLSInitServlet.SENDOPLAN);
    blackboard.publishAdd(req);
  }

  private void publishGLS() {
    GLSInitServlet.Request req =
      new GLSInitServlet.Request(GLSInitServlet.PUBLISHGLS);
    blackboard.publishAdd(req);
  }
}
