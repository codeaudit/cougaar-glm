/*--------------------------------------------------------------------------
 * <copyright>
 *  Copyright 1999-2003 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins.projection;

import org.cougaar.planning.ldm.policy.IntegerRuleParameter;
import org.cougaar.planning.ldm.policy.Policy;
import org.cougaar.planning.ldm.policy.RuleParameterIllegalValueException;
import org.cougaar.planning.ldm.policy.StringRuleParameter;

/**
 * The PolicyPlugin
 *
 * @author   ALPINE <alpine-software@bbn.com>
 *
 */

public class DemandProjectionPolicy extends Policy {
    public static final String ItemConsumed = "ItemConsumed";
    public static final String DaysBetweenDemand = "DaysBetweenDemand";

    public DemandProjectionPolicy() {
	super("DemandProjectionPolicy");

	StringRuleParameter ic = new StringRuleParameter(ItemConsumed);
	try {
	    ic.setValue(new String());
	} catch (RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
	Add(ic);

	IntegerRuleParameter dbd = new IntegerRuleParameter(DaysBetweenDemand, 0, 30);
	try {
	    dbd.setValue(new Integer(1));
	} catch (RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
	Add(dbd);

    }

    public String getItemConsumed() {
	StringRuleParameter param = (StringRuleParameter)
	    Lookup(ItemConsumed);
	return (String)param.getValue();
    }

    public void setItemConsumed(String name) {
	StringRuleParameter param = (StringRuleParameter)
	    Lookup(ItemConsumed);
	try {
	    param.setValue(name);
	} catch(RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
    }

    public int getDaysBetweenDemand() {
	IntegerRuleParameter param = (IntegerRuleParameter)
	    Lookup(DaysBetweenDemand);
	return ((Integer)(param.getValue())).intValue();
    }

    public void setDaysBetweenDemand(int days) {
	IntegerRuleParameter param = (IntegerRuleParameter)
	    Lookup(DaysBetweenDemand);
	try {
	    param.setValue(new Integer(days));
	} catch(RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
    }
}


