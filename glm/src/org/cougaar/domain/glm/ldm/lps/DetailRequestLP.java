/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm.lps;

import org.cougaar.core.cluster.*;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.society.UniqueObject;
import org.cougaar.core.society.UID;
import org.cougaar.domain.planning.ldm.plan.Directive;
import org.cougaar.domain.glm.ldm.ALPFactory;
import org.cougaar.domain.glm.ldm.plan.DetailRequest;
import org.cougaar.domain.glm.ldm.plan.DetailRequestAssignment;
import org.cougaar.domain.glm.ldm.plan.DetailReplyAssignment;
import org.cougaar.domain.glm.ldm.plan.QueryRequest;
import org.cougaar.domain.glm.ldm.plan.QueryRequestAssignment;
import org.cougaar.domain.glm.ldm.plan.QueryReplyAssignment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

/**
 *  Logic Provider for handling Detail and Query Requests and Replys.
 *  It is both an EnvelopeLogicProvider and a MessageLogicProvider,
 *  so it both reads from the logplan and accepts directive messages.
 *  
 */

public class DetailRequestLP
  extends LogPlanLogicProvider
  implements EnvelopeLogicProvider, RestartLogicProvider, MessageLogicProvider
{
  private transient ALPFactory _alpFactory=null;
  
  private transient HashMap outstandingRequests = new HashMap(7);

  public DetailRequestLP(LogPlanServesLogicProvider logplan,
			 ClusterServesLogicProvider cluster) {
    super(logplan,cluster);
  }

  private ALPFactory getALPFactory() {
    if (_alpFactory==null) {
      _alpFactory = (ALPFactory)cluster.getFactory("alp");
    }
    return _alpFactory;
  }

  /**  
   * implements execute from EnvelopePlanLogicProvider
   * processes DetailRequests and QueryRequests published in the logplan
   */
  public void execute(EnvelopeTuple o, Collection changes) {
    Object obj = o.getObject();
    if (obj instanceof DetailRequest) {
      //System.out.println("DetailRequestLP: received DetailRequest");
      DetailRequest ir = (DetailRequest) obj;
      if (o.isAdd()) {
        processDetailRequestAdded(ir);
      } else if (o.isRemove()) {
	// no-nop
      }
    } else if (obj instanceof QueryRequest) {
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
   * Processes DetailRequest/ReplyAssignment directives from other clusters. 
   * Processes QueryRequest/ReplyAssignment directives from other clusters. 
   **/
  public void execute(Directive dir, Collection changes) {
    if (dir instanceof DetailReplyAssignment) {
      //System.out.println("DetailRequestLP: received DetailReplyAssignment");
      processDetailReplyAssignment((DetailReplyAssignment) dir, changes);
    } else if (dir instanceof DetailRequestAssignment) {
      //System.out.println("DetailRequestLP: received DetailRequestAssignment");
      processDetailRequestAssignment((DetailRequestAssignment) dir, changes);
    } else if (dir instanceof QueryRequestAssignment) {
      processQueryRequestAssignment((QueryRequestAssignment) dir, changes);
    } else if (dir instanceof QueryReplyAssignment) {
      processQueryReplyAssignment((QueryReplyAssignment) dir, changes);
    }
  }

  // RestartLogicProvider implementation

  /**
   * Cluster restart handler. Resend all our DetailRequest
   * and QueryRequests to the restarted cluster. 
   **/
  public void restart(final ClusterIdentifier cid) {
    System.out.println("Resending DetailRequest to " + cid);
    UnaryPredicate pred = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof DetailRequest) {
          DetailRequest ir = (DetailRequest) o;
          ClusterIdentifier dest = ir.getSourceCluster();
          return cid.equals(dest);
        }
        return false;
      }
    };
    Enumeration enum = logplan.searchALPPlan(pred);
    while (enum.hasMoreElements()) {
      DetailRequest ir = (DetailRequest) enum.nextElement();
      System.out.println("Resending " + ir);
      processDetailRequestAdded(ir);
    }

    System.out.println("Resending QueryRequest to " + cid);
    UnaryPredicate queryPred = new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof QueryRequest) {
          QueryRequest ir = (QueryRequest) o;
          ClusterIdentifier dest = ir.getSourceCluster();
          return cid.equals(dest);
        }
        return false;
      }
    };
    Enumeration queryEnum = logplan.searchALPPlan(queryPred);
    while (queryEnum.hasMoreElements()) {
      QueryRequest ir = (QueryRequest) queryEnum.nextElement();
      System.out.println("Resending " + ir);
      processQueryRequestAdded(ir);
    }
    System.out.println("Resending finished");
  }
    

  /**
   * Turn request into assignment.
   * First step in the process. A request read from the logplan is
   * turned into an assignment, and sent to the cluster where
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
    DetailRequestAssignment dra = getALPFactory().newDetailRequestAssignment(dr);
    //System.out.println("DetailRequestLP: sending DetailRequestAssignment to " + dest);
    // Give the directive to the logplan for tranmission
    logplan.sendDirective(dra);
  }
  

  /**
   * Turn request assignment into reply assignment.
   * The second and third steps in the process.
   * A request for an object is received on the cluster where the object resides.
   * The object is found, packaged, and sent back to  cluster where the request originated.
   */
  private void processDetailRequestAssignment(DetailRequestAssignment ta, Collection changes) {
    DetailRequest request = (DetailRequest) ta.getDetailRequest();
    UID uid = request.getDetailUID();
    //System.out.println("UID: " + uid);
    try {
      UniqueObject uo = (UniqueObject)logplan.findUniqueObject(uid) ;
      DetailReplyAssignment dra = getALPFactory().newDetailReplyAssignment(uo,
								      uid,
						   cluster.getClusterIdentifier(),
						   request.getRequestingCluster());

      //System.out.println("DetailRequestLP:  DetailReplyAssignment " + dra);
      //System.out.println("DetailReplyAssignment UID: " + dra.getRequestUID());
      logplan.sendDirective(dra);
    } catch (RuntimeException excep) {
      excep.printStackTrace();
    }
  }


  /**
   * Publish the result of the request to the logplan.
   * The last step in the process. An answer has returned to the
   * originating cluster, and is published here.
   */
  private void processDetailReplyAssignment(DetailReplyAssignment reply, Collection changes) {
    UniqueObject obj = reply.getDetailObject();
    final UID replyUID = reply.getRequestUID();
    if (obj == null) {
      System.out.println("DetailRequestLP: object not found on remote cluster " +
			 replyUID);
      cleanup(replyUID);
      return;
    }

    final UID objUID = obj.getUID();
    UniqueObject existingObj = logplan.findUniqueObject(obj.getUID());
    if (existingObj != null) {
      existingObj = obj;
      try {
	//System.out.println("DetailRequestLP: publish changing existing obj " + obj + obj.getUID());
	// changes probably not filled in
	logplan.change(existingObj, changes);
      } catch (RuntimeException re) {
	re.printStackTrace();	
      }
    } else {
      try {
	//System.out.println("DetailRequestLP: publishing DetailReply " + reply + obj.getUID());
	logplan.add(obj);
      } catch (RuntimeException excep) {
	excep.printStackTrace();
      }
    }
    cleanup(replyUID);
  }
   

  /**
   * Removes DetailRequests from the logplan. Also removes uids from
   * the outstanding requests hash.
   */
  private void cleanup (final UID cleanupUID) {
    // clear out Requests for this object
    outstandingRequests.remove(cleanupUID);

    Enumeration requests = logplan.searchLogPlan( new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof DetailRequest) {
	  DetailRequest dr = (DetailRequest) o;
	  UID uid = dr.getDetailUID();
	  if (uid == null) {
	    //System.out.println("Why the hell is this null? " + dr);
	    return false;
	  }
	  if (cleanupUID == null) {
	    //System.out.println("Why the hell is replyUID null?");
	    return false;
	  }
	  if (uid.equals(cleanupUID))
	    return true;
	}
	return false;
      }
    });

    while ( requests.hasMoreElements()) {
      DetailRequest dr = (DetailRequest) requests.nextElement();
      //System.out.println("Removing DetailRequest from logplan: " + dr);
      logplan.remove(dr);
    }
  }


  /**
   * Turn request into assignment.
   * First step in the process. A request read from the logplan is
   * turned into an assignment, and sent to the cluster where
   * the object lives.
   */
  private void processQueryRequestAdded(QueryRequest qr)
  {
    // First, check to see if we are already waiting for this object
    UnaryPredicate pred = qr.getQueryPredicate();
    if (outstandingRequests.containsValue(pred)) {
      return;
    }

    outstandingRequests.put(pred, pred);

    // create an QueryRequestAssignment directive
    QueryRequestAssignment qra = getALPFactory().newQueryRequestAssignment(qr);
    //System.out.println("DetailRequestLP: sending QueryRequestAssignment to " + qra.getDestination());
    // Give the directive to the logplan for tranmission
    logplan.sendDirective(qra);
  }


  /**
   * Turn request assignment into reply assignment.
   * The second and third steps in the process.
   * A request for a query is received on the cluster.
   * The query is executed and the results are packaged, 
   * and sent back to  cluster where the request originated.
   */
  private void processQueryRequestAssignment(QueryRequestAssignment ta, Collection changes) {
    QueryRequest request = (QueryRequest) ta.getQueryRequest();
    UnaryPredicate pred = request.getQueryPredicate();
    ArrayList collection;
    //System.out.println("QueryPredicate: " + pred);
    try {
      Enumeration e = logplan.searchLogPlan(pred);
      collection = new ArrayList(7);
      while(e.hasMoreElements()) {
	collection.add(e.nextElement());
      }
      
      QueryReplyAssignment dra = getALPFactory().newQueryReplyAssignment(collection,
								    pred,
						   cluster.getClusterIdentifier(),
						   request.getRequestingCluster());

      //System.out.println("DetailRequestLP:  QueryReplyAssignment " + dra);
      //System.out.println("QueryReplyAssignment Pred: " + dra.getRequestPredicate());
      logplan.sendDirective(dra);
    } catch (RuntimeException excep) {
      excep.printStackTrace();
    }
  }

  /**
   * Publish the result of the query request to the logplan.
   * The last step in the process. An answer has returned to the
   * originating cluster, and is published here.
   */
  private void processQueryReplyAssignment(QueryReplyAssignment reply, Collection changes) {
    Collection objs = reply.getQueryResponse();
    final UnaryPredicate replyPredicate = reply.getRequestPredicate();
    if ((objs == null) || objs.isEmpty()) {
      //System.out.println("DetailRequestLP: query on remote cluster returned no values " +
      //			 replyPredicate);
      cleanup(replyPredicate);
      return;
    }

    for (Iterator it=objs.iterator(); it.hasNext();) {
      Object obj = it.next();
      try {
	if (obj instanceof UniqueObject) {
	  UniqueObject existingObj = logplan.findUniqueObject(((UniqueObject) obj).getUID());
	  if (existingObj != null) {
	    existingObj = (UniqueObject) obj;
	    //System.out.println("DetailRequestLP: publish changing existing obj " + obj);
	    // changes probably not filled in
	    logplan.change(existingObj, changes);
	  } else {
	    //System.out.println("DetailRequestLP: publishing QueryReply " + reply + obj);
	    logplan.add(obj);
	  }
	} else {
	  //System.out.println("DetailRequestLP: publishing QueryReply " + reply + obj);
	  logplan.add(obj);
	}
      } catch (RuntimeException excep) {
	excep.printStackTrace();
      }
    }
    cleanup(replyPredicate);
  }

  /**
   * Removes QueryRequests from the logplan. Also removes uids from
   * the outstanding requests hash.
   */
  private void cleanup (final UnaryPredicate cleanupPred) {
    // clear out Requests for this object
    outstandingRequests.remove(cleanupPred);

    Enumeration requests = logplan.searchLogPlan( new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof QueryRequest) {
	  QueryRequest dr = (QueryRequest) o;
	  UnaryPredicate pred = dr.getQueryPredicate();
	  if (pred == null) {
	    //System.out.println("Why the hell is this null? " + pred);
	    return false;
	  }
	  if (cleanupPred == null) {
	    //System.out.println("Why the hell is replyPred null?");
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
      //System.out.println("Removing QueryRequest from logplan: " + qr);
      logplan.remove(qr);
    }
  }
}
