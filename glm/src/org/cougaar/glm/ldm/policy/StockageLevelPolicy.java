/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import org.cougaar.planning.ldm.policy.BooleanRuleParameter;
import org.cougaar.planning.ldm.policy.IntegerRuleParameter;
import org.cougaar.planning.ldm.policy.Policy;
import org.cougaar.planning.ldm.policy.RuleParameterIllegalValueException;

/**
 * StockLevelPolicy
 *
 * @author   ALPINE <alpine-software@bbn.com>
 *
 */

public class StockageLevelPolicy extends Policy {

  public static final String DaysOfSupply = "DaysOfSupply";
  public static final String HostNation = "HostNation";
  public static final String WarReserves = "WarReserves";
  public static final String PrepositionStock = "PrepositionStock";

  public StockageLevelPolicy() {
    super("StockageLevelPolicy");
    
    IntegerRuleParameter irp = new IntegerRuleParameter(DaysOfSupply,0,365);
    try {
      irp.setValue(new Integer(2));
    } catch (RuleParameterIllegalValueException ex) {
      System.out.println(ex);
    }
    Add(irp);

    BooleanRuleParameter brp = new BooleanRuleParameter(HostNation);
    Boolean bv = new Boolean(false);
    try {
      brp.setValue(bv);
    } catch (RuleParameterIllegalValueException ex) {
      System.out.println(ex);
    }
    Add(brp);

    brp = new BooleanRuleParameter(WarReserves);
    bv = new Boolean(false);
    try {
      brp.setValue(bv);
    } catch (RuleParameterIllegalValueException ex) {
      System.out.println(ex);
    }
    Add(brp);

    brp = new BooleanRuleParameter(PrepositionStock);
    bv = new Boolean(false);
    try {
      brp.setValue(bv);
    } catch (RuleParameterIllegalValueException ex) {
      System.out.println(ex);
    }
    Add(brp);
  }

  public int getDaysOfSupply() {
    IntegerRuleParameter param = (IntegerRuleParameter)Lookup(DaysOfSupply);
    return ((Integer)(param.getValue())).intValue();
  }

  public void setDaysOfSupply(int days) {
    IntegerRuleParameter param = (IntegerRuleParameter)Lookup(DaysOfSupply);
    try {
      param.setValue(new Integer(days));
    } catch(RuleParameterIllegalValueException ex) {
      System.out.println(ex);
    }
  }

  public boolean getHostNation() {
    BooleanRuleParameter param = (BooleanRuleParameter)Lookup(HostNation);
    return ((Boolean)(param.getValue())).booleanValue();
  }
    
  public void setHostNation(boolean value) {
    BooleanRuleParameter param = (BooleanRuleParameter)Lookup(HostNation);
    try {

      param.setValue(new Boolean(value));
    } catch(RuleParameterIllegalValueException ex) {
      System.out.println(ex);
    }
  }


  public boolean getWarReserves() {
    BooleanRuleParameter param = (BooleanRuleParameter)Lookup(WarReserves);
    return ((Boolean)(param.getValue())).booleanValue();
  }
    
  public void setWarReserves(boolean value) {
    BooleanRuleParameter param = (BooleanRuleParameter)Lookup(WarReserves);
    try {
      param.setValue(new Boolean(value));
    } catch(RuleParameterIllegalValueException ex) {
      System.out.println(ex);
    }
  }

  public boolean getPrepositionStock() {
    BooleanRuleParameter param = (BooleanRuleParameter)Lookup(PrepositionStock);
    return ((Boolean)(param.getValue())).booleanValue();
  }
    
  public void setPrepositionStock(boolean value) {
    BooleanRuleParameter param = (BooleanRuleParameter)Lookup(PrepositionStock);
    try {
      param.setValue(new Boolean(value));
    } catch(RuleParameterIllegalValueException ex) {
      System.out.println(ex);
    }
  }

}
