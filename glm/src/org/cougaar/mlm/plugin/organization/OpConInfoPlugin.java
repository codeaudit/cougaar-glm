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

package org.cougaar.mlm.plugin.organization;

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.logging.LoggingServiceWithPrefix;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collection;
import java.util.Iterator;

public class OpConInfoPlugin extends ComponentPlugin {
  
  String itemId;
  String typeId;
  ArrayList idInfo = new ArrayList();

  private static UnaryPredicate selfOrgAssetPred = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Organization) {
	  Organization org = (Organization) o;
	  return org.isSelf();
	}
	return false;
      }
    };

  private UnaryPredicate myOpConInfoRelayPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      return o instanceof OpConInfoRelay;
    }
  };

  /** rely upon load-time introspection to set these services - don't worry about revokation. */
  public final void setLoggingService (LoggingService logger) { this.logger = logger; }

  protected LoggingService logger;

  IncrementalSubscription mySelfOrgs;
  IncrementalSubscription myOpConInfoRelaySubscription;

  public void setupSubscriptions () {
    String me = getAgentIdentifier().toString();
    logger = LoggingServiceWithPrefix.add(logger, me + ": ");
    mySelfOrgs = (IncrementalSubscription)getBlackboardService().subscribe(selfOrgAssetPred);
    myOpConInfoRelaySubscription = (IncrementalSubscription)getBlackboardService().subscribe(myOpConInfoRelayPred);
  }

  protected void execute() {
    if (mySelfOrgs != null && mySelfOrgs.hasChanged()) {
      Enumeration selfOrgEnum = mySelfOrgs.getAddedList();
      if (selfOrgEnum.hasMoreElements()) {
        Organization selfOrgAsset = (Organization) selfOrgEnum.nextElement();
        itemId = selfOrgAsset.getItemIdentificationPG().getItemIdentification();
        typeId = selfOrgAsset.getTypeIdentificationPG().getTypeIdentification();
        if (logger.isDebugEnabled()) {
          logger.debug("My itemId is " + itemId +" and my typeId is " +typeId);
        }
        idInfo.add(itemId);
        idInfo.add(typeId);
      }
    }
    if (myOpConInfoRelaySubscription.hasChanged()) {
      if (logger.isDebugEnabled()) {
        logger.debug(": myOpConInfoRelaySubscription has changed!");
      }
      Collection addedOpConInfoRelays =
        myOpConInfoRelaySubscription.getAddedCollection();
      
      for (Iterator adds = addedOpConInfoRelays.iterator();
           adds.hasNext();) {
        OpConInfoRelay opiRelay = (OpConInfoRelay) adds.next();
        if (logger.isDebugEnabled()) {
          logger.debug("Updating the relay with: " +idInfo);
        }
        opiRelay.updateResponse(null, idInfo); 
        getBlackboardService().publishChange(opiRelay);
      }
    }
  }
}
