/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBN Technologies,
 *                               A Division of
 *                              BBN Corporation
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 1999 by
 *             BBN Technologies, A Division of
 *             BBN Corporation, all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.domain.glm.plugins.projection;

import org.cougaar.domain.planning.ldm.policy.IntegerRuleParameter;
import org.cougaar.domain.planning.ldm.policy.Policy;
import org.cougaar.domain.planning.ldm.policy.RuleParameter;
import org.cougaar.domain.planning.ldm.policy.RuleParameterIllegalValueException;
import org.cougaar.domain.planning.ldm.policy.StringRuleParameter;

/**
 * The PolicyPlugIn
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


