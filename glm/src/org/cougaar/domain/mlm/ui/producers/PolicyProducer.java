/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.producers;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.cougaar.core.society.UID;
import org.cougaar.core.util.XMLObjectFactory;

import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.domain.mlm.ui.planviewer.XMLClientConfiguration;
import org.cougaar.domain.mlm.ui.producers.policy.PolicyEditorMessage;
import org.cougaar.domain.mlm.ui.producers.policy.UIPolicyInfo;
import org.cougaar.domain.mlm.ui.producers.policy.UIPolicyParameterInfo;

import org.w3c.dom.Element;

/**
 * The <code>PolicyProducer</code> class retrieves XML for all policy objects
 * from a target ALP cluster, parses the XML into Policies, and fires the
 * policies to any registered consumers.
 *
 * @author  Nick Pioch, BBN Technologies
 * @version 1.0
 */

public class PolicyProducer extends ThreadedALPProducer {

  /**
   * A constructor for the PolicyProducer, with the target cluster as arg
   *
   * @param clusterName String specifying the cluster
   */
  public PolicyProducer(String clusterName) {
    super("Policy Producer", clusterName);
    myQuery = PolicyEditorMessage.generateQueryRequest();
  }
  
  
  /**
   * publish - Publish specified policy. Starts a separate thread
   * to perform the work. Refreshes data after publish is complete.
   * 
   * @param p UIPolicyInfo to be published
   */
  public void publish(UIPolicyInfo p) {
    class PublishThread extends Thread {
      private UIPolicyInfo myPolicyInfo;
      public PublishThread(String name, UIPolicyInfo policyInfo) {
        super(name);
        myPolicyInfo = policyInfo;
      }

      public void run() {
        doPublish(myPolicyInfo);

        // Do a complete refresh - don't want to keep working
        // with stale data.
        doRefresh();
      }
    };

    PublishThread publishThread = new PublishThread(toString() + " - Publish",
                                                    p);
    publishThread.start();
  }

  

  /**
   * produceObjects - called from start() and refresh() 
   * to perform the initial retrieval of policies from the cluster.
   *
   * @return Object[] - array of policy objects from the target cluster.
   */
  protected Object []produceObjects() {
    // call code to get Policy objects from target cluster
    return producePolicies(myQuery);
  }
  
  
  /**
   * producePolicies - produce UIPolicyInfo objects, either for initial 
   * population or refresh
   *
   * @param queryStr String with the query command for the PSP. (Could be
   * modified to support single policy query)
   *
   * @return UIPolicyInfo[] - array of policies returned by the PSP
   */
  private UIPolicyInfo[] producePolicies(String querystr) {
    UIPolicyInfo[] policies = null;
    
    // Connect to target cluster's URL
    ConnectionHelper xmlConnection = null;
    byte[] reply = null;
    String url = ClusterCache.getClusterURL(myTargetCluster);
    if (url != null) {
      try {
        xmlConnection = 
          new ConnectionHelper(url,
                               XMLClientConfiguration.PSP_package,
                               "POLICY.PSP");
        xmlConnection.sendData(querystr);
        reply = xmlConnection.getResponse();
        
        String xmlString = new String(reply);

        Element root = 
          XMLObjectFactory.readXMLRoot(new ByteArrayInputStream(reply));

        if (root == null) {
          throw new Exception("XML parse returned null!");
        }

        Object obj = XMLObjectFactory.parseObject(null, root);
        
        if (obj != null) {
          Vector objVector = (Vector)obj;
          policies = new UIPolicyInfo[objVector.size()];
          objVector.copyInto(policies);
        } else {
          policies = new UIPolicyInfo[0];
        }
      } catch (Exception e) {
        System.err.println(e);
        e.printStackTrace();
        
        policies = null;
      }
    } else {
      System.err.println("Could not connect to " + myTargetCluster);
    }
    return policies;
  }
  
  /**
   * doPublish - publish policy object to ALP. Private method, see
   * publish() for public entry.
   *
   *
   * @param p UIPolicyInfo to be published
   */
  private void doPublish(UIPolicyInfo p) {
    String modifyRequest = PolicyEditorMessage.generateModifyRequest(p);

    // Connect to target cluster's URL, using special Policy PSP
    ConnectionHelper xmlConnection = null;
    byte[] reply = null;
    String url = ClusterCache.getClusterURL(myTargetCluster);
    if (url != null) {
      try {
        xmlConnection = 
          new ConnectionHelper(url,
                               XMLClientConfiguration.PSP_package,
                               "POLICY.PSP");
        xmlConnection.sendData(modifyRequest);
        reply = xmlConnection.getResponse();
      } catch (Exception e) {
        System.err.println(e);
        e.printStackTrace();
      }
    }   
  }
}  








