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

package org.cougaar.glm.ldm.lps;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logging;

import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.core.util.UID;
import org.cougaar.core.blackboard.Directive;
import org.cougaar.planning.ldm.plan.Transferable;
import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.plan.DetailRequest;
import org.cougaar.glm.ldm.plan.DetailRequestAssignment;
import org.cougaar.glm.ldm.plan.DetailReplyAssignment;
import org.cougaar.glm.ldm.plan.QueryRequest;
import org.cougaar.glm.ldm.plan.QueryRequestAssignment;
import org.cougaar.glm.ldm.plan.QueryReplyAssignment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;


/**
 *  Logic Provider for handling Detail and Query Requests and Replys.
 *  It is both an EnvelopeLogicProvider and a MessageLogicProvider,
 *  so it both reads from the blackboard and accepts directive messages.
 *  
 */

public class DetailRequestLP
implements LogicProvider, EnvelopeLogicProvider, RestartLogicProvider, MessageLogicProvider
{
  private final RootPlan rootplan;
  private final MessageAddress self;
  private final GLMFactory glmFactory;
  private final HashMap outstandingRequests = new HashMap(7);

  public DetailRequestLP(
      RootPlan rootplan,
      MessageAddress self,
      GLMFactory glmFactory) {
    this.rootplan = rootplan;
    this.self = self;
    this.glmFactory = glmFactory;
  }

  public void init() {
  }

  /**  
   * implements execute from EnvelopePlanLogicProvider
   * processes DetailRequests and QueryRequests published in
   * the blackboard
   */
  public void execute(EnvelopeTuple o, Collection changes) {
    Object obj = o.getObject();
    if (obj instanceof DetailRequest) {
      Logging.defaultLogger().debug("Received DetailRequest");
      DetailRequest ir = (DetailRequest) obj;
      if (o.isAdd()) {
        processDetailRequestAdded(ir);
      } else if (o.isRemove()) {
	// no-nop
      }
    } else if (obj instanceof QueryRequest) {
      Logging.defaultLogger().debug("Received QueryRequest");
      QueryRequest qr = (QueryRequest) obj;
      if (o.isAdd()) {
	processQueryRequestAdded(qr);
      } else if (o.isRemove()) {
	// no-op
      }
    }
  }

  /**
   * implements execute() from MessageLogicProvider 
   * Processes DetailRequest/ReplyAssignment directives from other agents. 
   * Processes QueryRequest/ReplyAssignment directives from other agents. 
   **/
  public void execute(Directive dir, Collection changes) {
    if (dir instanceof DetailReplyAssignment) {
      Logging.defaultLogger().debug("Received DetailReplyAssignment");
      processDetailReplyAssignment((DetailReplyAssignment) dir, changes);
    } else if (dir instanceof DetailRequestAssignment) {
      Logging.defaultLogger().debug("Received DetailRequestAssignment");
      processDetailRequestAssignment((DetailRequestAssignment) dir, changes);
    } else if (dir instanceof QueryRequestAssignment) {
      Logging.defaultLogger().debug("Received QueryRequestAssignment");
      processQueryRequestAssignment((QueryRequestAssignment) dir, changes);
    } else if (dir instanceof QueryReplyAssignment) {
      Logging.defaultLogger().debug("Received QueryReplyAssignment");
      processQueryReplyAssignment((QueryReplyAssignment) dir, changes);
    }
  }

  // RestartLogicProvider implementation

  /**
   * Cluster restart handler. Resend all our DetailRequest
   * and QueryRequests to the restarted agent. 
   **/
  public void restart(final MessageAddress cid) {
    UnaryPredicate pred = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof DetailRequest) {
          DetailRequest ir = (DetailRequest) o;
          MessageAddress dest = ir.getSourceCluster();
          return 
            RestartLogicProviderHelper.matchesRestart(
                self, cid, dest);
        }
        return false;
      }
    };
    Enumeration enum = rootplan.searchBlackboard(pred);
    while (enum.hasMoreElements()) {
      DetailRequest ir = (DetailRequest) enum.nextElement();
      processDetailRequestAdded(ir);
    }

    UnaryPredicate queryPred = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof QueryRequest) {
          QueryRequest ir = (QueryRequest) o;
          MessageAddress dest = ir.getSourceCluster();
          return 
            RestartLogicProviderHelper.matchesRestart(
                self, cid, dest);
        }
        return false;
      }
    };
    Enumeration queryEnum = 
      rootplan.searchBlackboard(queryPred);
    while (queryEnum.hasMoreElements()) {
      QueryRequest ir = (QueryRequest) queryEnum.nextElement();
      processQueryRequestAdded(ir);
    }
  }

  /**
   * Turn request into assignment.
   * First step in the process. A request read from the blackboard
   * is turned into an assignment, and sent to the agent where
   * the object lives.
   */
  private void processDetailRequestAdded(DetailRequest dr)
  {
    // First, check to see if we are already waiting for this object
    UID uid = dr.getDetailUID();
    if (outstandingRequests.containsValue(uid)) {
      return;
    }

    outstandingRequests.put(uid,uid);

    // create an DetailRequestAssignment directive
    DetailRequestAssignment dra = glmFactory.newDetailRequestAssignment(dr);
    Logging.defaultLogger().debug("Sending DetailRequestAssignment to " + dra.getDestination());
    // Give the directive to the blackboard for tranmission
    rootplan.sendDirective(dra);
  }
  

  /**
   * Turn request assignment into reply assignment.
   * The second and third steps in the process.
   * A request for an object is received on the agent where the object resides.
   * The object is found, packaged, and sent back to  agent where the request originated.
   */
  private void processDetailRequestAssignment(DetailRequestAssignment ta, Collection changes) {
    DetailRequest request = (DetailRequest) ta.getDetailRequest();
    UID uid = request.getDetailUID();
    Logging.defaultLogger().debug("UID: " + uid);
    try {
      UniqueObject uo = (UniqueObject)
        rootplan.findUniqueObject(uid) ;
      if (uo instanceof Transferable) {
        // Clone so we don't have cross agent references to the same object
        uo = (UniqueObject) ((Transferable)uo).clone();
      } else {
          Logging.defaultLogger().warn(uo + " does not implement Transferable." +
                                     " Fullfillment of the DetailRequest may result \n" +
                                     " in cross agent references to the same object.");
      }
      DetailReplyAssignment dra = glmFactory.newDetailReplyAssignment(
          uo,
          uid,
          self,
          request.getRequestingCluster());

      Logging.defaultLogger().debug("DetailReplyAssignment " + dra);
      Logging.defaultLogger().debug("DetailReplyAssignment UID: " + dra.getRequestUID());
      rootplan.sendDirective(dra);
    } catch (RuntimeException excep) {
      excep.printStackTrace();
    }
  }


  /**
   * Publish the result of the request to the blackboard.
   * The last step in the process. An answer has returned to the
   * originating agent, and is published here.
   */
  private void processDetailReplyAssignment(DetailReplyAssignment reply, Collection changes) {
    UniqueObject obj = reply.getDetailObject();
    final UID replyUID = reply.getRequestUID();
    if (obj == null) {
      Logging.defaultLogger().warn("Object not found on remote agent " +
                                   replyUID);
      cleanup(replyUID);
      return;
    }

    final UID objUID = obj.getUID();
    UniqueObject existingObj = 
      rootplan.findUniqueObject(obj.getUID());
    if (existingObj != null) {

      // Copy fields so the changes actually appear
      if (obj instanceof Transferable) { 
        ((Transferable) existingObj).setAll((Transferable) obj);
      } else {
        Logging.defaultLogger().warn(existingObj + " does not implement Transferable." +     
                                   " Changes to source will not be visible.");
      }

      try {
	rootplan.change(obj, changes);
      } catch (RuntimeException re) {
	re.printStackTrace();	
      }
    } else {
      try {
	Logging.defaultLogger().debug("Publishing DetailReply " + reply + obj.getUID());
	rootplan.add(obj);
      } catch (RuntimeException excep) {
	excep.printStackTrace();
      }
    }
    cleanup(replyUID);
  }
   

  /**
   * Removes DetailRequests from the blackboard. Also removes uids from
   * the outstanding requests hash.
   */
  private void cleanup (final UID cleanupUID) {
    // clear out Requests for this object
    outstandingRequests.remove(cleanupUID);

    UnaryPredicate pred = 
      new UnaryPredicate() {
        public boolean execute(Object o) {
          if (o instanceof DetailRequest) {
            DetailRequest dr = (DetailRequest) o;
            UID uid = dr.getDetailUID();
            if (uid == null) {
              Logging.defaultLogger().error("Null UID for " + dr);
              return false;
            }
            if (cleanupUID == null) {
              Logging.defaultLogger().error("Null cleanup UID");
              return false;
            }
            if (uid.equals(cleanupUID))
              return true;
          }
          return false;
        }
      };
    Enumeration requests = rootplan.searchBlackboard(pred);

    while ( requests.hasMoreElements()) {
      DetailRequest dr = (DetailRequest) requests.nextElement();
      Logging.defaultLogger().debug(
          "Removing DetailRequest from blackboard: " + dr);
      rootplan.remove(dr);
    }
  }


  /**
   * Turn request into assignment.
   * First step in the process. A request read from the blackboard
   * is turned into an assignment, and sent to the agent where
   * the object lives.
   */
  private void processQueryRequestAdded(QueryRequest qr)
  {
    // First, check to see if we are already waiting for this object
    UnaryPredicate pred = qr.getQueryPredicate();

    if (outstandingRequests.containsValue(pred)) {
      Logging.defaultLogger().debug("Outstanding QueryRequestAssignment");
      return;
    }

    outstandingRequests.put(pred, pred);

    // create an QueryRequestAssignment directive
    QueryRequestAssignment qra = glmFactory.newQueryRequestAssignment(qr);
    Logging.defaultLogger().debug("Sending QueryRequestAssignment to " + qra.getDestination());
    // Give the directive to the blackboard for tranmission
    rootplan.sendDirective(qra);
  }


  /**
   * Turn request assignment into reply assignment.
   * The second and third steps in the process.
   * A request for a query is received on the agent.
   * The query is executed and the results are packaged, 
   * and sent back to  agent where the request originated.
   */
  private void processQueryRequestAssignment(QueryRequestAssignment ta, Collection changes) {
    QueryRequest request = (QueryRequest) ta.getQueryRequest();
    UnaryPredicate pred = request.getQueryPredicate();
    ArrayList collection;
    Logging.defaultLogger().debug("QueryPredicate: " + pred);
    try {
      Enumeration e = rootplan.searchBlackboard(pred);
      collection = new ArrayList(7);
      while(e.hasMoreElements()) {
        Object next = e.nextElement();

        if (next instanceof Transferable) {
          // Clone so we don't end up with cross agent refs to the same object
          next = ((Transferable) next).clone();
        } else {
          Logging.defaultLogger().warn(next + " does not implement Transferable." +
                                     " Fullfillment of the QueryRequest may result \n" +
                                     " in cross agent references to the same object.");

        }
	collection.add(next);
      }
      
      QueryReplyAssignment dra = glmFactory.newQueryReplyAssignment(
          collection,
          pred,
          request.getLocalQueryPredicate(),
          self,
          request.getRequestingCluster());

      Logging.defaultLogger().debug("QueryReplyAssignment " + dra);
      Logging.defaultLogger().debug("QueryReplyAssignment Pred: " + dra.getRequestPredicate());
      Logging.defaultLogger().debug("QueryReplyAssignment requestor: " + request.getRequestingCluster());
      rootplan.sendDirective(dra);
    } catch (RuntimeException excep) {
      excep.printStackTrace();
    }
  }

  /**
   * Publish the result of the query request to the blackboard.
   * The last step in the process. An answer has returned to the
   * originating agent, and is published here.
   */
  private void processQueryReplyAssignment(QueryReplyAssignment reply, Collection changes) {
    Collection replyCollection = reply.getQueryResponse();
    final UnaryPredicate replyPredicate = reply.getRequestPredicate();
    if ((replyCollection == null) || replyCollection.isEmpty()) {
      Logging.defaultLogger().debug("Query on remote agent returned no values " +
      			 replyPredicate);
      cleanup(replyPredicate);
      return;
    }
    
    //Compare reply collection with local collection.
    ArrayList localCollection = new ArrayList();
    if (reply.getLocalPredicate() != null) {
      Enumeration localEnum = 
        rootplan.searchBlackboard(reply.getLocalPredicate());
      while (localEnum.hasMoreElements()) {
        localCollection.add(localEnum.nextElement());
      }
    }
    
    for (Iterator it = replyCollection.iterator(); it.hasNext();) {
      Object obj = it.next();
      if (obj instanceof UniqueObject) {
        Object localObj = 
          rootplan.findUniqueObject(((UniqueObject) obj).getUID());
        
        if (localObj != null) {
          // Only publish change if object is really different
          if (!localObj.equals(obj)) {
            if (obj instanceof Transferable) { 
              ((Transferable) localObj).setAll((Transferable) obj);
            } else {
              Logging.defaultLogger().warn(localObj + " does not implement Transferable." +     
                                         " Changes will not be visible.");
            }
            rootplan.change(obj, changes);
          } else {
            Logging.defaultLogger().debug(" not publish changing existing obj " + obj);
          }
        } else {
          Logging.defaultLogger().debug("Publishing QueryReply " + reply + obj);
          rootplan.add(obj);
        }
      } else {
        // Not unique
        // Look for object match in local collection
        if (findMatch(obj, localCollection) == null) {        
          rootplan.add(obj);
        }
      }
    }

    //Remove local objects which are not in the reply collection
    for (Iterator iterator = localCollection.iterator();
         iterator.hasNext();) {
      Object localObj = iterator.next();
      if (findMatch(localObj, replyCollection) == null) {
        rootplan.remove(localObj);
      }
    }
        

    cleanup(replyPredicate);
  }

  /**
   * Removes QueryRequests from the blackboard. Also removes uids from
   * the outstanding requests hash.
   */
  private void cleanup (final UnaryPredicate cleanupPred) {
    // clear out Requests for this object
    outstandingRequests.remove(cleanupPred);

    Enumeration requests = rootplan.searchBlackboard( new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof QueryRequest) {
	  QueryRequest dr = (QueryRequest) o;
	  UnaryPredicate pred = dr.getQueryPredicate();
	  if (pred == null) {
            Logging.defaultLogger().error("Predicate is null for " + dr);
	    return false;
	  }
	  if (cleanupPred == null) {
            Logging.defaultLogger().error("cleanup() called with a null predicate.");
	    return false;
	  }
	  if (pred.equals(cleanupPred))
	    return true;
	}
	return false;
      }
    });

    while ( requests.hasMoreElements()) {
      QueryRequest qr = (QueryRequest) requests.nextElement();
      Logging.defaultLogger().debug("Removing QueryRequest from blackboard: " + qr);
      rootplan.remove(qr);
    }
  }

  private Object findMatch(Object object, Collection collection) {

    if (object instanceof UniqueObject) {
      UID uid = ((UniqueObject) object).getUID();
      
      for (Iterator iterator = collection.iterator();
           iterator.hasNext();) {
        Object match = iterator.next();
        if (match instanceof UniqueObject) {
          if (((UniqueObject) match).getUID().equals(uid)) {
            return match;
          }
        }
      }
    } else {
      for (Iterator iterator = collection.iterator();
           iterator.hasNext();) {
        Object match = iterator.next();
        if (match.equals(object)) {
          return match;
        }
      }
    }

    return null;
  }
}
