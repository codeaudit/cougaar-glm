package org.cougaar.domain.glm.ldm.policy;

import org.cougaar.domain.planning.ldm.policy.*;

/**
 * Answers the question, "What should our policy be for interpreting acr factors?" In the absence of an explicit policy, the answer is to multiply the factor by 1.0.
 **/
public class ACRPolicy extends Policy {
  private static final String CONSUMER = "consumer";
  private static final String CONSUMED = "consumed";
  private static final String ADJUSTMENT_FACTOR = "adjustmentFactor";
  public ACRPolicy() {
    super("ACRPolicy");
  }

  public void setConsumerTypeIdentification(String newConsumerTypeIdentification)
    throws RuleParameterIllegalValueException
  {
    RuleParameter p = new StringRuleParameter(CONSUMER);
    p.setValue(newConsumerTypeIdentification);
    Add(p);
  }

  public String getConsumerTypeIdentification() {
    return (String) Lookup(CONSUMER).getValue();
  }

  public void setConsumedTypeIdentification(String newConsumedTypeIdentification)
    throws RuleParameterIllegalValueException
  {
    RuleParameter p = new StringRuleParameter(CONSUMED);
    p.setValue(newConsumedTypeIdentification);
    Add(p);
  }

  public String getConsumedTypeIdentification() {
    return (String) Lookup(CONSUMED).getValue();
  }

  public void setAdjustmentFactor(double newAdjustmentFactor)
    throws RuleParameterIllegalValueException
  {
    RuleParameter p = new DoubleRuleParameter(ADJUSTMENT_FACTOR, 0.0, Double.MAX_VALUE);
    p.setValue(new Double(newAdjustmentFactor));
    Add(p);
  }

  public double getAdjustmentFactor() {
    return ((Double) Lookup(ADJUSTMENT_FACTOR).getValue()).doubleValue();
  }

  public String toString() {
    return "ACRPolicy[consumer="
      + getConsumerTypeIdentification()
      + ",consumed="
      + getConsumedTypeIdentification()
      + ",factor="
      + getAdjustmentFactor()
      + "]";
  }
}
