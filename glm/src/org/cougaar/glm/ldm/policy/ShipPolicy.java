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


package org.cougaar.glm.ldm.policy;

import org.cougaar.planning.ldm.policy.*;

/**
 * The PolicyPlugIn
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
