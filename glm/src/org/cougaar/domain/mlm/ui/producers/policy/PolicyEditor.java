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
 
package org.cougaar.domain.mlm.ui.producers.policy;

import java.util.ArrayList;
import java.util.Iterator;

import org.cougaar.domain.planning.ldm.policy.Policy;
import org.cougaar.domain.planning.ldm.policy.*;
import org.cougaar.core.society.UID;
import org.cougaar.util.UnaryPredicate;

public class PolicyEditor implements UnaryPredicate {
  UID myPolicyID;
  ArrayList myPolicyParameters;

  public PolicyEditor(String policyID, ArrayList policyParameters) {
    myPolicyID = new UID(policyID);
    myPolicyParameters = policyParameters;
  }

  /* Find the policy object to change based on the Policy ID
     Edit the policy object and return true, which places
     the object in the subscription.
     The PSP calls publishChange (via the PlanServerPlugIn)
     for all the objects in the subscription.
     */
  public boolean execute(Object object) {
    if (!(object instanceof Policy)) {
      return false;
    }
    
    Policy ldmPolicy = (Policy)object;

    if (!ldmPolicy.getUID().equals(myPolicyID)) {
      return false;
    }

    Object []UIPolicyParameterInfos = myPolicyParameters.toArray();

    for (int i = 0; i < UIPolicyParameterInfos.length; i++) {
      UIPolicyParameterInfo uiParameterInfo = 
        (UIPolicyParameterInfo)UIPolicyParameterInfos[i];

      // No point in comparing values if we don't support editting this type
      // of parameter
      if (!uiParameterInfo.getEditable()) {
        System.out.println("PolicyEditor: parameter " + uiParameterInfo.getName() + 
                           " has not changed.");
        continue;
      }

      String name = uiParameterInfo.getName();

      RuleParameter ruleParameter = ldmPolicy.Lookup(name);

      // BOZO - do we support adding/deleting parameters through the editor?
      
      if (ruleParameter == null) {
        System.err.println("PolicyEditor.execute: " + name + 
                           " not a valid parameter for " + ldmPolicy);
        continue;
      } 
       
      Object value = uiParameterInfo.getValue();     

      if ((uiParameterInfo.getType() != UIPolicyParameterInfo.RANGE_TYPE) &&
          (uiParameterInfo.getType() != UIPolicyParameterInfo.KEY_TYPE) && 
          (value.equals(ruleParameter.getValue()))) {
        // Parameter hasn't changed
        System.out.println("PolicyEditor: parameter " + uiParameterInfo.getName() + 
                           " has not changed.");
        continue;
      }

      int ldmType = 
        PSP_PolicyEditor.convertToLDMType(uiParameterInfo.getType());
      if (ldmType != ruleParameter.ParameterType()) {
        System.err.println("PolicyEditor.execute: parameter types not the" + 
                           "same for " + name + " parameter");
        continue;
      }

      try {
        //BOGUS - must set ranges before value because 
        //RangeRuleParameter.setRanges clears the default value.
        if ((ruleParameter instanceof RangeRuleParameter) ||
            (ruleParameter instanceof KeyRuleParameter)) {
          if (!convertEntries(uiParameterInfo, ruleParameter)) {
            continue;
          }
        }
        ruleParameter.setValue(value);
      } catch (RuleParameterIllegalValueException e) {
        System.err.println(e);
      }
    }
    return true;
  }
    
  protected boolean convertEntries(UIPolicyParameterInfo uiParameterInfo,
                                   RuleParameter ruleParameter) {

    if (ruleParameter instanceof RangeRuleParameter) {
      ArrayList uiRangeEntries =
        ((UIRangeParameterInfo)uiParameterInfo).getRangeEntries();
      RangeRuleParameterEntry []ldmRangeEntries = 
        new RangeRuleParameterEntry[uiRangeEntries.size()];
      
      int row = 0;
      for (Iterator iterator = uiRangeEntries.iterator();
           iterator.hasNext();
           row++) {
        UIRangeEntryInfo uiEntry = (UIRangeEntryInfo)iterator.next();
        if (uiEntry instanceof UIStringRangeEntryInfo) {
          ldmRangeEntries[row] = new RangeRuleParameterEntry(uiEntry.getValue(),
                                                             uiEntry.getMin(),
                                                             uiEntry.getMax());
        } else {
          // !!! No changes to non-string range entries
          System.err.println("Unable to convert range entries for " + uiParameterInfo.getName() +
                             ". Only support UIStringRangeEntryInfo at this time.");
          return false;
        }
      }
      ((RangeRuleParameter)ruleParameter).setRanges(ldmRangeEntries);
    } else if (ruleParameter instanceof KeyRuleParameter) {
      ArrayList uiKeyEntries =
        ((UIKeyParameterInfo)uiParameterInfo).getKeyEntries();
      KeyRuleParameterEntry []ldmKeyEntries = 
        new KeyRuleParameterEntry[uiKeyEntries.size()];
          
      int row = 0;
      for (Iterator iterator = uiKeyEntries.iterator();
           iterator.hasNext();
           row++) {
        UIKeyEntryInfo uiEntry = (UIKeyEntryInfo)iterator.next();
        ldmKeyEntries[row] = new KeyRuleParameterEntry(uiEntry.getKey(),
                                                       uiEntry.getValue());
                                                       
      }
      ((KeyRuleParameter)ruleParameter).setKeys(ldmKeyEntries);
    }

    return true;
  }
}




