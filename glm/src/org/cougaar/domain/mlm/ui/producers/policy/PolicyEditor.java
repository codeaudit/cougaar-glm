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
      String name = uiParameterInfo.getName();

      RuleParameter ruleParameter = ldmPolicy.Lookup(name);

      // BOZO - do we support adding/deleting parameters through the editor?
      
      if (ruleParameter == null) {
        System.out.println("PolicyEditor.execute: " + name + 
                           " not a valid parameter for " + ldmPolicy);
        continue;
      } 
       
      Object value = uiParameterInfo.getValue();     

      if ((uiParameterInfo.getType() != UIPolicyParameterInfo.RANGE_TYPE) &&
          (uiParameterInfo.getType() != UIPolicyParameterInfo.KEY_TYPE) &&
          (value.equals(ruleParameter.getValue()))) {
        // Parameter hasn't changed
        System.out.println("PolicyEditor: policy " + uiParameterInfo.getName() + 
                           " has not changed.");
        continue;
      }

      int ldmType = 
        PSP_PolicyEditor.convertToLDMType(uiParameterInfo.getType());
      if (ldmType != ruleParameter.ParameterType()) {
        System.out.println("PolicyEditor.execute: parameter types not the" + 
                           "same for " + name + " parameter");
        continue;
      }

      try {

        //BOGUS - must set ranges before value because 
        //RangeRuleParameter.setRanges clears the default value.
        if ((ruleParameter instanceof RangeRuleParameter) ||
            (ruleParameter instanceof KeyRuleParameter)) {
          convertEntries(uiParameterInfo, ruleParameter);
        }

        ruleParameter.setValue(value);
      } catch (RuleParameterIllegalValueException e) {
        System.out.println(e);
      }
    }
    return true;
  }
    
  protected void convertEntries(UIPolicyParameterInfo uiParameterInfo,
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
        ldmRangeEntries[row] = new RangeRuleParameterEntry(uiEntry.getValue(),
                                                           uiEntry.getMin(),
                                                           uiEntry.getMax());
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
  }
}




