/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.glm.policy;

import org.cougaar.domain.planning.ldm.policy.*;

/**
 * StockLevelPolicy
 *
 * @author   ALPINE <alpine-software@bbn.com>
 * @version  $Id: StockageLevelPolicy.java,v 1.1 2000-12-15 20:18:03 mthome Exp $
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
