/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.producers.policy;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.text.ParsePosition;
import java.util.*;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.policy.*;
import org.cougaar.lib.planserver.*;
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
      psc.getServerPlugInSupport().subscribe(this, allPolicyPred);
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
    Subscription subscription = 
      psc.getServerPlugInSupport().subscribe(this, xmlPredicate);
    Collection container = 
      ((CollectionSubscription)subscription).getCollection();
    Vector ldmPolicies = new Vector(container.size());
    for (Iterator j = container.iterator(); j.hasNext(); ) {
      Object changedObject = j.next();
      ldmPolicies.addElement(changedObject);
      psc.getServerPlugInSupport().publishChangeForSubscriber(changedObject);
    }
    // unsubscribe, don't need this subscription any more
    psc.getServerPlugInSupport().unsubscribeForSubscriber(subscription);

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
      System.out.println("Policy: " + ldmPolicy.getName());
      RuleParameter []ldmParameters = ldmPolicy.getRuleParameters();
      
      for (int i = 0; i < ldmParameters.length; i++) {
        int type = convertToUIType(ldmParameters[i].ParameterType());
        
        System.out.println("Parameter " + ldmParameters[i].getName() + " type " + 
                           ldmParameters[i].ParameterType());

        // Don't recognize the type so report error and move on to the 
        // next parameter
        if (type == -1) {
          System.err.println("PSP_PolicyEditor: unable to handle " +
                             " RuleParameter with a type of " + 
                             ldmParameters[i].ParameterType());
          continue;
        }

        switch (type) {
        case UIPolicyParameterInfo.DOUBLE_TYPE:
          DoubleRuleParameter 
            doubleParam = (DoubleRuleParameter)ldmParameters[i];
          Double doubleMin = new Double(doubleParam.getLowerBound());
          Double doubleMax = new Double(doubleParam.getUpperBound());
          policyInfo.add(new UIBoundedParameterInfo(doubleParam.getName(),
                                                    type,
                                                    doubleParam.getValue(),
                                                    doubleMin,
                                                    doubleMax));
          break;

        case UIPolicyParameterInfo.ENUMERATION_TYPE:
          EnumerationRuleParameter 
            enumParam = (EnumerationRuleParameter)ldmParameters[i];
          List ldmEnum = Arrays.asList(enumParam.getEnumeration());
          policyInfo.add(new UIEnumerationParameterInfo(enumParam.getName(),
                                                        type,
                                                        enumParam.getValue(),
                                                        new ArrayList(ldmEnum)));
          break;

        case UIPolicyParameterInfo.INTEGER_TYPE:
          IntegerRuleParameter 
            intParam = (IntegerRuleParameter)ldmParameters[i];
          Integer intMin = new Integer(intParam.getLowerBound());
          Integer intMax = new Integer(intParam.getUpperBound());
          policyInfo.add(new UIBoundedParameterInfo(intParam.getName(),
                                                    type,
                                                    intParam.getValue(),
                                                    intMin,
                                                    intMax));
          break;

        case UIPolicyParameterInfo.KEY_TYPE:
          KeyRuleParameter keyParam = (KeyRuleParameter)ldmParameters[i];
          List ldmKeys = Arrays.asList(keyParam.getKeys());
          ArrayList uiKeyEntries = new ArrayList();
          for (Iterator iterator = ldmKeys.iterator(); iterator.hasNext();) {
            KeyRuleParameterEntry entry = (KeyRuleParameterEntry)iterator.next();
            uiKeyEntries.add(new UIKeyEntryInfo(entry));
          }
          policyInfo.add(new UIKeyParameterInfo(keyParam.getName(),
                                                type,
                                                keyParam.getValue(),
                                                uiKeyEntries));
          break;

        case UIPolicyParameterInfo.RANGE_TYPE:
          RangeRuleParameter 
            rangeParam = (RangeRuleParameter)ldmParameters[i];
          List ldmRanges = Arrays.asList(rangeParam.getRanges());
          ArrayList uiRangeEntries = new ArrayList();
          for (Iterator iterator = ldmRanges.iterator();
               iterator.hasNext();) {
            RangeRuleParameterEntry entry = 
              (RangeRuleParameterEntry)iterator.next();
            uiRangeEntries.add(new UIRangeEntryInfo(entry));
          }
          policyInfo.add(new UIRangeParameterInfo(rangeParam.getName(),
                                                  type,
                                                  rangeParam.getValue(),
                                                  uiRangeEntries));
          break;
          
        default:
          policyInfo.add(new UIPolicyParameterInfo(ldmParameters[i].getName(),
                                                   type,
                                                   ldmParameters[i].getValue()));
        }
      }
      
      policies.add(policyInfo);
    }
    
    return policies;
  }

  protected static int convertToUIType(int ldmType) {
    int type;

    switch (ldmType) {
    case RuleParameter.INTEGER_PARAMETER:
      type = UIPolicyParameterInfo.INTEGER_TYPE;
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
    
    default:
      System.out.println("PSP_PolicyEditor.convertToUIType - " +
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

    case UIPolicyParameterInfo.KEY_TYPE:
      type = RuleParameter.KEY_PARAMETER;
      break;

    case UIPolicyParameterInfo.RANGE_TYPE:
      type = RuleParameter.RANGE_PARAMETER;
      break;

    case UIPolicyParameterInfo.STRING_TYPE:
      type = RuleParameter.STRING_PARAMETER;
      break;
    
    default:
      System.out.println("PSP_PolicyEditor.convertToUIType - " +
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








