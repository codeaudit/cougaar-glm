/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.domain.planning.ldm.policy.Policy;
import org.cougaar.domain.planning.ldm.policy.RuleParameter;

public class UIPolicyImpl implements UIPolicy, XMLUIPlanObject {
  Policy policy;
  UUID policyUUID;

  public UIPolicyImpl(Policy policy) {
    System.out.println("UIPolicyImpl");
    this.policy = policy;
    policyUUID = new UUID(policy.getUID().toString());
  }
   
  /**
   * @return String - the name of the policy
   */
  public String getPolicyName() {
    return policy.getName();
  }

  public UIPolicyParameter[] getPolicyParameters() {
    RuleParameter[] rp = policy.getRuleParameters();
    UIPolicyParameter[] params = new UIPolicyParameterImpl[rp.length];
    for (int i = 0; i < rp.length; i++) {
      System.out.println("Rule Parameter: " + rp[i]);
      RuleParameter param;
      params[i] = new UIPolicyParameterImpl(rp[i].getName(),
                                            rp[i].ParameterType(),
                                            rp[i].getValue());
    }
    return params;
  }

  public UIPolicyParameter getPolicyParameter(int i) {
    UIPolicyParameter[] params = getPolicyParameters();
    if (i < params.length)
      return params[i];
    else
      return null;
  }

  public UUID getUUID() {
    return policyUUID;
  }

  //  XMLPlanObject method for UI
  
  public Element getXML(Document doc, Vector requestedFields) {
    return XMLUIPlanObjectConverter.getPlanObjectXML(this, doc, requestedFields);
  }
}





