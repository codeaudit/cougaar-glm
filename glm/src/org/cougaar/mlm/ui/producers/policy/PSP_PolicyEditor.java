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

package org.cougaar.mlm.ui.producers.policy;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.text.ParsePosition;
import java.util.*;

import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.plugin.PluginDelegate;
import org.cougaar.planning.ldm.policy.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.w3c.dom.Element;

public class PSP_PolicyEditor extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {
  private String myID;



  public PSP_PolicyEditor() throws RuntimePSPException {
    super();
  }

  public PSP_PolicyEditor(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
    setID(id);
  }

  public String getID() {
    return myID;
  }

  public void setID(String id) {
    myID = id;
  }

  /* This PSP is referenced directly (in the URL from the client)
     and hence this shouldn't be called.
     */

  public boolean test(HttpInput query_parameters, PlanServiceContext psc) {
    super.initializeTest();
    return false; 
  }

  /*
    Called when a request is received from a client.
    Assumed to be of the form:
    SET PolicyID name=value name=value, etc.
    where PolicyID is the Policy.ID field and the name/value pairs
    are the names and values of policy fields to set.
  */

  public void execute(PrintStream out,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception {

    String postData = null;

    if (query_parameters.hasBody()) {
      postData = query_parameters.getBodyAsString();
      postData = postData.trim();
      //System.out.println("POST DATA:" + postData);

      ParsePosition parsePosition = new ParsePosition(0);
      String command = 
        PolicyEditorMessage.parseParameter(postData, 
                                           PolicyEditorMessage.COMMAND_LABEL, 
                                           PolicyEditorMessage.DELIM,
                                           parsePosition);

      String format = 
        PolicyEditorMessage.parseParameter(postData, 
                                           PolicyEditorMessage.FORMAT_LABEL, 
                                           PolicyEditorMessage.DELIM,
                                           parsePosition);

      if (!format.equals(PolicyEditorMessage.XML_FORMAT)) {
        throw new RuntimePSPException("Unsupported format " + format + 
                                      " - must be " + 
                                      PolicyEditorMessage.XML_FORMAT);
      }

      AbstractPrinter pr = AbstractPrinter.createPrinter("xml", out);
      
      if (command.equals(PolicyEditorMessage.QUERY_COMMAND)) {
        listPolicies(pr, psc, psu);
      } else if (command.equals(PolicyEditorMessage.MODIFY_COMMAND)) {
        modifyPolicy(postData, parsePosition, pr, psc, psu);
      } else {
        throw new RuntimePSPException("Unrecognized command - " + postData);
      }
    }     
  }

  private void listPolicies(AbstractPrinter pr,
                            PlanServiceContext psc,
                            PlanServiceUtilities psu) throws Exception {
    Subscription subscription = 
      psc.getServerPluginSupport().subscribe(this, allPolicyPred);
    Enumeration en = ((CollectionSubscription)subscription).elements();

    Vector policies = convertToUIPolicyInfos(en);

    pr.printObject(policies);
  }

  protected void modifyPolicy(String modifyArgs, 
                              ParsePosition parsePosition,
                              AbstractPrinter pr, 
                              PlanServiceContext psc,
                              PlanServiceUtilities psu) 
    throws RuntimePSPException {

    String uidStr = 
      PolicyEditorMessage.parseParameter(modifyArgs, 
                                         PolicyEditorMessage.UID_LABEL, 
                                         PolicyEditorMessage.DELIM, 
                                         parsePosition);
    if (uidStr.equals("")) {
      throw new RuntimePSPException("No UID for policy.");
    }

    String parameterXML = 
      PolicyEditorMessage.parseParameter(modifyArgs, 
                                         PolicyEditorMessage.PARAMETERS_LABEL, 
                                         PolicyEditorMessage.DELIM, 
                                         parsePosition);
    //parse parameterXML
    Object obj;
    Element root = 
      XMLObjectFactory.readXMLRoot(new ByteArrayInputStream(parameterXML.getBytes()));

    if (root == null) {
      throw new RuntimePSPException("XML parse returned null!");
    }
    
    obj = XMLObjectFactory.parseObject(root);
    ArrayList policyParameters = (ArrayList)obj;

    PolicyEditor xmlPredicate = new PolicyEditor(uidStr, policyParameters);
    
    // enter subscription which causes policy editor
    // to be invoked and make its changes
    PluginDelegate delegate = psc.getServerPluginSupport().getDirectDelegate();
    delegate.openTransaction();

    Subscription subscription = delegate.subscribe(xmlPredicate);
    Collection container = 
      ((CollectionSubscription)subscription).getCollection();
    Vector ldmPolicies = new Vector(container.size());
    for (Iterator j = container.iterator(); j.hasNext(); ) {
      Object changedObject = j.next();
      ldmPolicies.addElement(changedObject);
      delegate.publishChange(changedObject);
    }
    // unsubscribe, don't need this subscription any more
    delegate.unsubscribe(subscription);
    delegate.closeTransaction();

    Vector policies = convertToUIPolicyInfos(ldmPolicies.elements());

    pr.printObject(policies);
  }  

  public boolean returnsXML() {
    return true;
  }

  public boolean returnsHTML() {
    return false;
  }



  /* This should eventually return a valid DTD; should we define the return type
     as being a DTD to enforce this?
  */

  public String getDTD() {
    return "";
  }

  /* The UISubscriber interface.
     This PSP doesn't care if subscriptions change
     because it treats each request as a new request.
  */
  public void subscriptionChanged(Subscription subscription) {
  }

  protected Vector convertToUIPolicyInfos(Enumeration ldmPolicies) {
    Vector policies = new Vector();

    // Convert to UI representation
    while (ldmPolicies.hasMoreElements()) {
      Policy ldmPolicy = (Policy)ldmPolicies.nextElement();
      
      if (ldmPolicy.getUID() == null) {
        System.err.println("PSP_PolicyEditor: policy " + ldmPolicy.getName() + 
                           " has a null UID. Can not be modified.");
        continue;
      }

      UIPolicyInfo policyInfo = 
        new UIPolicyInfo(ldmPolicy.getName(), 
                         ldmPolicy.getUID().toString());
      //System.out.println("Policy: " + ldmPolicy.getName());
      RuleParameter []ldmParameters = ldmPolicy.getRuleParameters();
      
      for (int i = 0; i < ldmParameters.length; i++) {
        policyInfo.add(convertToUIPolicyParameterInfo(ldmParameters[i]));
      }
      
      policies.add(policyInfo);
    }
    
    return policies;
  }

  protected UIPolicyParameterInfo convertToUIPolicyParameterInfo(RuleParameter ldmParameter) {

    UIPolicyParameterInfo uiPolicyParameterInfo;

    int type = convertToUIType(ldmParameter.ParameterType());
    
    //System.out.println("Parameter " + ldmParameter.getName() + " type " + 
    //                   ldmParameter.ParameterType());
    
    // Don't recognize the type so report error and move on to the 
    // next parameter
    if (type == -1) {
      System.err.println("PSP_PolicyEditor: unable to handle " +
                         " RuleParameter with a type of " + 
                         ldmParameter.ParameterType());
      return null;
    }
    
    switch (type) {
    case UIPolicyParameterInfo.DOUBLE_TYPE:
      DoubleRuleParameter 
        doubleParam = (DoubleRuleParameter)ldmParameter;
      Double doubleMin = new Double(doubleParam.getLowerBound());
      Double doubleMax = new Double(doubleParam.getUpperBound());
      uiPolicyParameterInfo = new UIBoundedParameterInfo(doubleParam.getName(),
                                                type,
                                                doubleParam.getValue(),
                                                doubleMin,
                                                doubleMax);
      break;
      
    case UIPolicyParameterInfo.ENUMERATION_TYPE:
      EnumerationRuleParameter 
        enumParam = (EnumerationRuleParameter)ldmParameter;
      List ldmEnum = Arrays.asList(enumParam.getEnumeration());
      uiPolicyParameterInfo = new UIEnumerationParameterInfo(enumParam.getName(),
                                                    type,
                                                    enumParam.getValue(),
                                                    new ArrayList(ldmEnum));
      break;
      
    case UIPolicyParameterInfo.INTEGER_TYPE:
      IntegerRuleParameter 
        intParam = (IntegerRuleParameter)ldmParameter;
      Integer intMin = new Integer(intParam.getLowerBound());
      Integer intMax = new Integer(intParam.getUpperBound());
      uiPolicyParameterInfo = new UIBoundedParameterInfo(intParam.getName(),
                                                   type,
                                                   intParam.getValue(),
                                                   intMin,
                                                   intMax);
      break;
      
    case UIPolicyParameterInfo.LONG_TYPE:
      LongRuleParameter 
        longParam = (LongRuleParameter)ldmParameter;
      Long longMin = new Long(longParam.getLowerBound());
      Long longMax = new Long(longParam.getUpperBound());
      uiPolicyParameterInfo = new UIBoundedParameterInfo(longParam.getName(),
                                                   type,
                                                   longParam.getValue(),
                                                   longMin,
                                                   longMax);
      break;
      
    case UIPolicyParameterInfo.CLASS_TYPE:
      ClassRuleParameter 
        classParam = (ClassRuleParameter)ldmParameter;
      uiPolicyParameterInfo = new UIPolicyParameterInfo(classParam.getName(),
                                                  type,
                                                  ((Class) classParam.getValue()).getName());
      break;
      
    case UIPolicyParameterInfo.PREDICATE_TYPE:
      PredicateRuleParameter 
        predicateParam = (PredicateRuleParameter)ldmParameter;
      uiPolicyParameterInfo = new UIPolicyParameterInfo(predicateParam.getName(),
                                                  type,
                                                  predicateParam.getValue().getClass());
      break;
      
    case UIPolicyParameterInfo.KEY_TYPE:
      KeyRuleParameter keyParam = (KeyRuleParameter)ldmParameter;
      List ldmKeys = Arrays.asList(keyParam.getKeys());
      ArrayList uiKeyEntries = new ArrayList();
      for (Iterator iterator = ldmKeys.iterator(); iterator.hasNext();) {
        KeyRuleParameterEntry entry = (KeyRuleParameterEntry)iterator.next();
        uiKeyEntries.add(new UIKeyEntryInfo(entry));
      }
      uiPolicyParameterInfo = new UIKeyParameterInfo(keyParam.getName(),
                                               type,
                                               keyParam.getValue(),
                                               uiKeyEntries);
      break;
      
    case UIPolicyParameterInfo.RANGE_TYPE:
      RangeRuleParameter 
        rangeParam = (RangeRuleParameter)ldmParameter;
      List ldmRanges = Arrays.asList(rangeParam.getRanges());
      ArrayList uiRangeEntries = new ArrayList();
      for (Iterator iterator = ldmRanges.iterator();
           iterator.hasNext();) {
        RangeRuleParameterEntry entry = 
          (RangeRuleParameterEntry)iterator.next();
        Object entryValue = entry.getValue();
        if (entryValue instanceof String) {
          // value was/is a String
          uiRangeEntries.add(new UIStringRangeEntryInfo(entry));
        } else if (entryValue instanceof RuleParameter) {
          uiRangeEntries.add(new UIRangeEntryInfo(convertToUIPolicyParameterInfo((RuleParameter) entryValue), 
                                                  entry.getMax(),
                                                  entry.getMin()));
        } else {
          // Convert to string because we don't know how to xmit otherwise
          // Enclose in quotation marks so XML parser doesn't try to parse.
          uiRangeEntries.add(new UIRangeEntryInfo("\"" + entryValue.toString() + "\"",
                                                  entry.getMin(),
                                                  entry.getMax()));
        }

      }
      uiPolicyParameterInfo = new UIRangeParameterInfo(rangeParam.getName(),
                                                       type,
                                                       rangeParam.getValue(),
                                                       uiRangeEntries);
      break;
        
      default:
        uiPolicyParameterInfo = new UIPolicyParameterInfo(ldmParameter.getName(),
                                                 type,
                                                 ldmParameter.getValue());
    }
    return uiPolicyParameterInfo;
  }

  protected static int convertToUIType(int ldmType) {
    int type;

    switch (ldmType) {
    case RuleParameter.INTEGER_PARAMETER:
      type = UIPolicyParameterInfo.INTEGER_TYPE;
      break;

    case RuleParameter.LONG_PARAMETER:
      type = UIPolicyParameterInfo.LONG_TYPE;
      break;

    case RuleParameter.DOUBLE_PARAMETER:
      type = UIPolicyParameterInfo.DOUBLE_TYPE;
      break;

    case RuleParameter.STRING_PARAMETER:
      type = UIPolicyParameterInfo.STRING_TYPE;
      break;

    case RuleParameter.ENUMERATION_PARAMETER:
      type = UIPolicyParameterInfo.ENUMERATION_TYPE;
      break;
 
    case RuleParameter.BOOLEAN_PARAMETER:
      type = UIPolicyParameterInfo.BOOLEAN_TYPE;
      break;

 
    case RuleParameter.CLASS_PARAMETER:
      type = UIPolicyParameterInfo.CLASS_TYPE;
      break;

    case RuleParameter.RANGE_PARAMETER:
      type = UIPolicyParameterInfo.RANGE_TYPE;
      break;

    case RuleParameter.KEY_PARAMETER:
      type = UIPolicyParameterInfo.KEY_TYPE;
      break;

    case RuleParameter.PREDICATE_PARAMETER:
      type = UIPolicyParameterInfo.PREDICATE_TYPE;
      break;
    
    default:
      System.err.println("PSP_PolicyEditor.convertToUIType - " +
                         "unrecognized type " + ldmType);
      type = -1;
    }
     
    return type;
  }

  protected static int convertToLDMType(int ldmType) {
    int type;

    switch (ldmType) {
    case UIPolicyParameterInfo.BOOLEAN_TYPE:
      type = RuleParameter.BOOLEAN_PARAMETER;
      break;
 
    case UIPolicyParameterInfo.CLASS_TYPE:
      type = RuleParameter.CLASS_PARAMETER;
      break;

    case UIPolicyParameterInfo.DOUBLE_TYPE:
      type = RuleParameter.DOUBLE_PARAMETER;
      break;

    case UIPolicyParameterInfo.ENUMERATION_TYPE:
      type = RuleParameter.ENUMERATION_PARAMETER;
      break;

    case UIPolicyParameterInfo.INTEGER_TYPE:
      type = RuleParameter.INTEGER_PARAMETER;
      break;

    case UIPolicyParameterInfo.LONG_TYPE:
      type = RuleParameter.LONG_PARAMETER;
      break;

    case UIPolicyParameterInfo.KEY_TYPE:
      type = RuleParameter.KEY_PARAMETER;
      break;

    case UIPolicyParameterInfo.PREDICATE_TYPE:
      type = RuleParameter.PREDICATE_PARAMETER;
      break;

    case UIPolicyParameterInfo.RANGE_TYPE:
      type = RuleParameter.RANGE_PARAMETER;
      break;

    case UIPolicyParameterInfo.STRING_TYPE:
      type = RuleParameter.STRING_PARAMETER;
      break;
    
    default:
      System.err.println("PSP_PolicyEditor.convertToUIType - " +
                         "unrecognized type " + ldmType);
      type = -1;
    }
     
    return type;
  }

  /**
   * allPolicyPred - subscribes to all Policies
   */
  private static UnaryPredicate allPolicyPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Policy);
    }
  };  		

}








