/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */


package org.cougaar.glm.ldm.policy;

import org.cougaar.planning.ldm.policy.EnumerationRuleParameter;
import org.cougaar.planning.ldm.policy.IntegerRuleParameter;
import org.cougaar.planning.ldm.policy.Policy;
import org.cougaar.planning.ldm.policy.RuleParameterIllegalValueException;

/**
 * The PolicyPlugin
 *
 * @author   ALPINE <alpine-software@bbn.com>
 *
 */

public class ShipPolicy extends Policy {

  public static final String ShipDays = "ShipDays";
  public static final String ShipMode = "ShipMode";
  public static final String Ground = "Ground";
  public static final String Air = "Air";
  public static final String Sea = "Sea";

  public ShipPolicy() {
    super("ShipPolicy");
    
    IntegerRuleParameter irp = new IntegerRuleParameter(ShipDays, 1, 10);
    try {
      irp.setValue(new Integer(2));
    } catch (RuleParameterIllegalValueException ex) {
      System.out.println(ex);
    }
    Add(irp);

    String modes[] = new String [3];
    modes[0] = Ground; modes[1] = Sea; modes[2] = Air;
    EnumerationRuleParameter erp  = new EnumerationRuleParameter(ShipMode, modes);
    try {
      erp.setValue(Ground);
    } catch (RuleParameterIllegalValueException ex) {
      System.out.println(ex);
    }
    Add(erp);
  }

  public int getShipDays() {
    IntegerRuleParameter param = (IntegerRuleParameter)Lookup(ShipDays);
    return ((Integer)(param.getValue())).intValue();
  }

  public void setShipDays(int days) {
    IntegerRuleParameter param = (IntegerRuleParameter)Lookup(ShipDays);
    try {
      param.setValue(new Integer(days));
    } catch(RuleParameterIllegalValueException ex) {
      System.out.println(ex);
    }
  }

  public String getShipMode() {
    EnumerationRuleParameter param = (EnumerationRuleParameter)Lookup(ShipMode);
    return ((String)param.getValue());
  }
    
  public void setShipMode(String mode) {
    EnumerationRuleParameter param = (EnumerationRuleParameter)Lookup(ShipMode);
    try {
      param.setValue(mode);
    } catch(RuleParameterIllegalValueException ex) {
      System.out.println(ex);
    }
  }
}
