/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */


package org.cougaar.domain.glm.ldm.policy;

import org.cougaar.domain.planning.ldm.policy.*;

/**
 * The PolicyPlugIn
 *
 * @author   ALPINE <alpine-software@bbn.com>
 * @version  $Id: ShipPolicy.java,v 1.1 2000-12-20 18:18:21 mthome Exp $
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
