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





