/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 1999-2004 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins.inventory;

import org.cougaar.planning.ldm.policy.BooleanRuleParameter;
import org.cougaar.planning.ldm.policy.DoubleRuleParameter;
import org.cougaar.planning.ldm.policy.IntegerRuleParameter;
import org.cougaar.planning.ldm.policy.Policy;
import org.cougaar.planning.ldm.policy.RuleParameterIllegalValueException;
import org.cougaar.planning.ldm.policy.StringRuleParameter;

/**
 * The PolicyPlugin
 *
 *
 */

public class InventoryPolicy extends Policy {
    public static final String ResourceType = "ResourceType";
    public static final String DaysOnHand = "DaysOnHand";
    public static final String DaysForward = "DaysForward";
    public static final String DaysBackward = "DaysBackward";
    public static final String GoalLevelMultiplier = "GoalLevelMultiplier";
    public static final String WithdrawSwitchoverDay = "WithdrawSwitchoverDay";
    public static final String RefillSwitchoverDay = "RefillSwitchoverDay";
    public static final String TurnOffProjections = "TurnOffProjections";
    public static final String FillToCapacity = "FillToCapacity";
    public static final String MaintainAtCapacity = "MaintainAtCapacity";

    public InventoryPolicy() {
	super("DaysOnHandPolicy");
	StringRuleParameter ic = new StringRuleParameter(ResourceType);
	try {
	    ic.setValue(new String());
	} catch (RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
	Add(ic);

	IntegerRuleParameter dbd = new IntegerRuleParameter(DaysOnHand, 0, 40);
	try {
	    dbd.setValue(new Integer(1));
	} catch (RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
	Add(dbd);

	IntegerRuleParameter frp = new IntegerRuleParameter(DaysForward, 0, 40);
	try {
	    frp.setValue(new Integer(15));
	} catch (RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
	Add(frp);

	IntegerRuleParameter brp = new IntegerRuleParameter(DaysBackward, 0, 40);
	try {
	    brp.setValue(new Integer(15));
	} catch (RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
	Add(brp);

	DoubleRuleParameter drp = new DoubleRuleParameter(GoalLevelMultiplier, 1.0, 30.0);
	try {
	    drp.setValue(new Double(2.0));
	} catch (RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
	Add(drp);
    }

    public String getResourceType() {
	StringRuleParameter param = (StringRuleParameter)
	    Lookup(ResourceType);
	return (String)param.getValue();
    }

    public void setResourceType(String name) {
	StringRuleParameter param = (StringRuleParameter)
	    Lookup(ResourceType);
	try {
	    param.setValue(name);
	} catch(RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
    }

    public int getDaysOnHand() {
	IntegerRuleParameter param = (IntegerRuleParameter)
	    Lookup(DaysOnHand);
	return ((Integer)(param.getValue())).intValue();
    }

    public void setDaysBetweenDemand(int days) {
	IntegerRuleParameter param = (IntegerRuleParameter)
	    Lookup(DaysOnHand);
	try {
	    param.setValue(new Integer(days));
	} catch(RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
    }

    public int getDaysForward() {
	IntegerRuleParameter param = (IntegerRuleParameter)
	    Lookup(DaysForward);
	return ((Integer)(param.getValue())).intValue();
    }

    public void setDaysForward(int days) {
	IntegerRuleParameter param = (IntegerRuleParameter)
	    Lookup(DaysForward);
	try {
	    param.setValue(new Integer(days));
	} catch(RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
    }
	
    public int getDaysBackward() {
	IntegerRuleParameter param = (IntegerRuleParameter)
	    Lookup(DaysBackward);
	return ((Integer)(param.getValue())).intValue();
    } 

    public void setDaysBackward(int days) {
	IntegerRuleParameter param = (IntegerRuleParameter)
	    Lookup(DaysBackward);
	try {
	    param.setValue(new Integer(days));
	} catch(RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
    }
   
    public int getWindowSize() {
	IntegerRuleParameter param = (IntegerRuleParameter)
	    Lookup(DaysForward);
	int forward = ((Integer)(param.getValue())).intValue();
	param = (IntegerRuleParameter)Lookup(DaysBackward);
	int backward = ((Integer)(param.getValue())).intValue();
	return forward + backward;
    }

    public double getGoalLevelMultiplier() {
	DoubleRuleParameter param = (DoubleRuleParameter)
	    Lookup(GoalLevelMultiplier);
	return ((Double)(param.getValue())).doubleValue();
    }

    public void setGoalLevelMultiplier(double multiplier) {
	DoubleRuleParameter param = (DoubleRuleParameter)
	    Lookup(GoalLevelMultiplier);
	try {
	    param.setValue(new Double(multiplier));
	} catch(RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
    }

    public boolean getFillToCapacity() {
	BooleanRuleParameter param = (BooleanRuleParameter)
	    Lookup(FillToCapacity);
	return ((Boolean)(param.getValue())).booleanValue();
    }

    public void setFillToCapacity(boolean fill_to_capacity) {
	BooleanRuleParameter param = (BooleanRuleParameter)
	    Lookup(FillToCapacity);
	try {
	    param.setValue(new Boolean(fill_to_capacity));
	}  catch(RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
    }

    public boolean hasFillToCapacityRule() {
	return Lookup(FillToCapacity) != null;
    }


    public boolean getMaintainAtCapacity() {
	BooleanRuleParameter param = (BooleanRuleParameter)
	    Lookup(MaintainAtCapacity);
	return ((Boolean)(param.getValue())).booleanValue();
    }

    public void setMaintainAtCapacity(boolean fill_to_capacity) {
	BooleanRuleParameter param = (BooleanRuleParameter)
	    Lookup(MaintainAtCapacity);
	try {
	    param.setValue(new Boolean(fill_to_capacity));
	}  catch(RuleParameterIllegalValueException ex) {
	    System.out.println(ex);
	}
    }

    public boolean hasMaintainAtCapacityRule() {
	return Lookup(MaintainAtCapacity) != null;
    }

    public boolean hasSwitchoverRule() {
        return Lookup(WithdrawSwitchoverDay) != null && Lookup(RefillSwitchoverDay) != null
            || Lookup(TurnOffProjections) != null;
    }

    public int getWithdrawSwitchoverDay() {
        if (getTurnOffProjections()) {
            return Integer.MAX_VALUE;
        } else {
            IntegerRuleParameter param =
                (IntegerRuleParameter) Lookup(WithdrawSwitchoverDay);
            return param.intValue();
        }
    }

    public int getRefillSwitchoverDay() {
        if (getTurnOffProjections()) {
            return Integer.MAX_VALUE;
        } else {
            IntegerRuleParameter param =
                (IntegerRuleParameter) Lookup(RefillSwitchoverDay);
            return param.intValue();
        }
    }

    public boolean getTurnOffProjections() {
        BooleanRuleParameter param =
            (BooleanRuleParameter) Lookup(TurnOffProjections);
        if (param == null) return false;
        return param.getValue().equals(Boolean.TRUE);
    }
}
	


