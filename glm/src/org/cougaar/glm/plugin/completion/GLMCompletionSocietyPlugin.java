/*
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


/*
 * File: DMPIClusterStartup.java
 * Heavily modified from original to be an LDMPlugin
 */

package org.cougaar.glm.plugin.completion;

import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.mlm.plugin.organization.GLSInitServlet;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.plugin.completion.CompletionSocietyPlugin;
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
	if (!task.getSource().equals(getAgentIdentifier())) return false;
	if (!task.getDestination().equals(getAgentIdentifier())) return false;
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
      new GLSInitServlet.Request(GLSInitServlet.GETOPINFO);
    blackboard.publishAdd(req);
  }

  private void publishGLS() {
    GLSInitServlet.Request req =
      new GLSInitServlet.Request(GLSInitServlet.PUBLISHGLS);
    blackboard.publishAdd(req);
  }
}
