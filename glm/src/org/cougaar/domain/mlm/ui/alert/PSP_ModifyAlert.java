/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.alert;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParsePosition;
import java.util.*;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.core.plugin.PlugInDelegate;
import org.cougaar.domain.planning.ldm.plan.Alert;
import org.cougaar.domain.planning.ldm.plan.AlertParameter;
import org.cougaar.domain.planning.ldm.plan.NewAlert;
import org.cougaar.core.society.UID;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

/**
 * PSP_ModifyAlert
 */

public class PSP_ModifyAlert extends PSP_BaseAdapter 
  implements PlanServiceProvider, UISubscriber {

  public static final String UID_LABEL = "UID=";
  public static final String ACK_LABEL = "ACK=";
  public static final String PARAM_CHOICE_LABEL = "PARAM_CHOICE=";
  public static final String URL_LABEL = "URL=";
  public static final String DELIM = "?";

  
  static private UID myModifyUID;
  private boolean myAck;
  private int myParamChoice;

  /** 
   * Constructor -  A zero-argument constructor is required for dynamically 
   * loaded PSPs by Class.newInstance()
   **/
  public PSP_ModifyAlert() {
    super();
  }
  
  /**
   * Constructor -
   *
   * @param pkg String specifying package id
   * @param id String specifying PSP name
   * @throw org.cougaar.lib.planserver.RuntimePSPException
   */
  public PSP_ModifyAlert(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }
  
  /**
   * infoMessagePred - subscribes for all alerts
   */
  private static UnaryPredicate alertPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      if ((o instanceof Alert) &&
          (((Alert)o).getUID().equals(myModifyUID))) {
        return true;
      }
      return false;
    }
  };            
  
  /**
   * test - Always returns false as currently implemented.
   * See doc in org.cougaar.lib.planserver.PlanServiceProvider
   *
   * @param queryParamaters HttpInput
   * @param sc PlanServiceContext
   */
  public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  //private int iterationCounter = 0;
  /**
   * execute - creates HTML with the relevant alerts
   * See doc in org.cougaar.lib.planserver.PlanServiceProvider
   *
   * @param out PrintStream to which output will be written
   * @param queryParameters HttpInput, modify parameters passed in post data
   * @param psc PlanServiceContext
   * @param psu PlanServiceUtilities
   **/
  public void execute(PrintStream out,
                      HttpInput queryParameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception {
    String postData = null;

    if (queryParameters.hasBody()) {
      postData = queryParameters.getBodyAsString();

      String uidStr = parseParameter(postData, UID_LABEL, DELIM);
      if (uidStr.equals("")) {
        throw new RuntimePSPException("No UID for alert.");
      }
      myModifyUID = new UID(uidStr);
      
      String ackStr = parseParameter(postData, ACK_LABEL, DELIM);
      if (ackStr.equals("")) {
        throw new RuntimePSPException("No Acknowledge value for alert.");
      }

      boolean ack;
      try {
        ack = Boolean.valueOf(ackStr).booleanValue();
      } catch (Exception e) {
        System.out.println(e);
        throw new RuntimePSPException("Unrecognized Acknowledge value : " + 
                                      ackStr);
      }

      int paramChoice = -1;
      String choiceStr = parseParameter(postData, PARAM_CHOICE_LABEL, DELIM);
      
      if (choiceStr != "") {
        try {
          paramChoice = Integer.parseInt(choiceStr);
        } catch (Exception e) {
          System.out.println(e);
          throw new RuntimePSPException("Unrecognized parameter choice : " + 
                                        choiceStr);
        }

        if (paramChoice < 0) {
            throw new RuntimePSPException("Invalid parameter choice : " + 
                                        choiceStr);
        }
      } 


      // Bracket subsequent activity within a transaction
      PlugInDelegate delegate = psc.getServerPlugInSupport().getDirectDelegate();
      delegate.openTransaction();

      Subscription subscription = delegate.subscribe(alertPred);
      Collection container = 
        ((CollectionSubscription)subscription).getCollection();

      if (container.size() == 0) {
        System.out.println("PSP_ModifyAlert.execute(): query by UID " + 
                           myModifyUID + 
                           " returned no alerts.");
        delegate.unsubscribe(subscription);
        delegate.closeTransaction();
        throw new RuntimePSPException("UID " + myModifyUID + 
                                      " does not exist.");
      } else if (container.size() > 1) {         
        System.out.println("PSP_ModifyAlert.execute(): query by UID " + 
                           myModifyUID + 
                           " returned multiple alerts.");
        delegate.unsubscribe(subscription);
        delegate.closeTransaction();
        throw new RuntimePSPException("UID " + myModifyUID + " not unique.");
      }          

      Enumeration alertsByUID  = new Enumerator(container);
      NewAlert newAlert  = (NewAlert)alertsByUID.nextElement();
      newAlert.setAcknowledged(ack);      
      
      Alert alert = (Alert)newAlert;
      AlertParameter chosenParam = null;
      if (paramChoice >= 0) {
        AlertParameter[] params = alert.getAlertParameters();
        
        if (paramChoice >= params.length) {
          throw new RuntimePSPException("Invalid parameter specification - " 
                                        + paramChoice + 
                                        ". Alert only has " + params.length + 
                                        " parameters.");
        }
        chosenParam = params[paramChoice];
        newAlert.setOperatorResponse(chosenParam);
      }

      delegate.publishChange(newAlert);
      delegate.unsubscribe(subscription);

      delegate.closeTransaction();

      if (chosenParam != null) {
        if (chosenParam.getParameter() instanceof String) {
          String paramStr = (String)chosenParam.getParameter();
          if (paramStr.endsWith(".html")) {
            out.println(URL_LABEL + paramStr + DELIM);
            out.flush();
          }
        }
      }
    }
  }
  
    /**
   * returnsXML - returns true if PSP can output XML.  Currently always false.
   * 
   * @return boolean 
   **/
  public boolean returnsXML() {
    return false;
  }
  
  /**
   * returnsHTML - returns true if PSP can output HTML.  Currently always true.
   * 
   * @return boolean 
   **/
  public boolean returnsHTML() {
    return true;
  }
  
  /** 
   * getDTD - returns null. PSP does not return XML.
   * Any PlanServiceProvider must be able to provide DTD of its
   * output IFF it is an XML PSP... ie.  returnsXML() == true;
   *
   * @return String
   **/
  public String getDTD() {
    return null;
  }

  /**
   * subscriptionChanged - adds new subscriptions to myIncomingAlerts.
   *
   * @param subscription Subscription
   */
  public void subscriptionChanged(Subscription subscription) {
  }

  public static String generateAckPostData(String uid, boolean ack) {
    return UID_LABEL + uid + DELIM + ACK_LABEL + ack + DELIM;  
  }

  public static String generateChoicePostData(String uid, boolean ack,
                                              int choice) {
    return UID_LABEL + uid + DELIM + ACK_LABEL + ack + DELIM + 
      PARAM_CHOICE_LABEL + choice + DELIM;  
  }

  private static String parseParameter(String data, String label, 
                                       String delim) {
    int start = data.indexOf(label);
    
    if (start == -1) {
      System.out.println("PSP_ModifyAlert.parseParameter() -  " + label + 
                         " label not found in " + data);
      return "";
    }

    start += label.length();
    int end = data.indexOf(delim, start);

    if (end < start) {
      System.out.println("PSP_ModifyAlert.parseParameter() -  " + delim + 
                         " delimiter not found after label " + label);
      return "";
    }

    return data.substring(start, end).trim();
  }
}





