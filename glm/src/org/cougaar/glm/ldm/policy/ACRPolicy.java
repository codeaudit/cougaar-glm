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

import org.cougaar.planning.ldm.policy.DoubleRuleParameter;
import org.cougaar.planning.ldm.policy.Policy;
import org.cougaar.planning.ldm.policy.RuleParameter;
import org.cougaar.planning.ldm.policy.RuleParameterIllegalValueException;
import org.cougaar.planning.ldm.policy.StringRuleParameter;

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
