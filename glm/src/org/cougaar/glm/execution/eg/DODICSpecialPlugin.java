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
package org.cougaar.glm.execution.eg;

import org.cougaar.glm.execution.common.*;
import org.cougaar.planning.ldm.measure.Rate;
import org.cougaar.util.OptionPane;
import java.util.Properties;

/**
 * Example F/C plugin illustrating how one might write a plugin that
 * applies to a certain class of supply and how to use the GUI
 * configuration features. Consult the default plugin for more mundane
 * matters.
 *
 * Specifically, this plugin applies a fixed multiplier to the
 * consumption rates of ammunition. The multiplier can be configured
 * from the gui screen.
 **/
public class DODICSpecialPlugin implements FailureConsumptionPlugin, TimeConstants {
  /**
   * The fixed rate of consumption for DODICs. Can be configured from the configuration GUI
   **/
  private static final double INITIAL_FIXED_MULTIPLIER = 2.5; // Elevate rate 2.5 times

  private double fixedMultiplier = INITIAL_FIXED_MULTIPLIER;

  private String prefix = "DODIC";

  private class Item extends FailureConsumptionPluginItem {
    long previousTime = 0L;

    public Item(FailureConsumptionRate aRate, long theExecutionTime) {
      super(aRate);
      previousTime = Math.max(theExecutionTime, aRate.theStartTime);
    }

    private double getQPerMilli() {
      double result = theFailureConsumptionRate.theRateValue / ONE_DAY;
      result *= fixedMultiplier / theFailureConsumptionRate.theRateMultiplier;
      return result;
    }

    public AnnotatedDouble getQuantity(long executionTime) {
      double qPerMilli = getQPerMilli();
      long elapsed = executionTime - previousTime;
      double dr = elapsed * qPerMilli;
      double result = Math.floor(dr);
      previousTime += (long) (result / qPerMilli);
      return new AnnotatedDouble(result, "DODICSpecialPlugin");
    }

    public long getTimeQuantum(long executionTime) {
      double qPerMilli = getQPerMilli();
      return previousTime + ((long) (1.0 / qPerMilli)) - executionTime;
    }
  }

  /**
   * @return the name of this plugin
   **/
  public String getPluginName() {
    return prefix + " Special";
  }

  public String getDescription() {
    return "Special plugin for " + prefix + " items that uses a fixed rate";
  }

  public boolean isConfigurable() {
    return true;
  }

  public void setParameter(String parameter) {
    prefix = parameter;         // parameter is item prefix.
  }

  public void configure(java.awt.Component c) {
    String result =
      (String) OptionPane.showInputDialog(c,
                                          "Enter the consumption rate multiplier",
                                          "Configure " + getPluginName(),
                                          OptionPane.QUESTION_MESSAGE,
                                          null, null, new Double(fixedMultiplier));
    try {
      fixedMultiplier = Double.parseDouble(result);
    } catch (Exception e) {     // Ignore errors.
    }
  }

  public void save(Properties props, String prefix) {
    props.setProperty(prefix + "fixedMultiplier", Double.toString(fixedMultiplier));
  }

  public void restore(Properties props, String prefix) {
    try {
      fixedMultiplier = Double.parseDouble(props.getProperty(prefix + "fixedMultiplier"));
    } catch (Exception e) {
      // State not present in props
    }
  }


 public void setEventGenerator(EventGenerator eg) {
 }

  /**
   * Create a FailureConsumptionItem for this plugin to handle a
   * particular FailureConsumptionRate.
   **/
  public FailureConsumptionPluginItem createFailureConsumptionItem
    (FailureConsumptionRate aRate,
     FailureConsumptionSegment aSegment,
     long theExecutionTime,
     FailureConsumptionPluginItem aFailureConsumptionPluginItem)
  {
    if (aFailureConsumptionPluginItem instanceof Item
        && aFailureConsumptionPluginItem.theFailureConsumptionRate == aRate) {
      return aFailureConsumptionPluginItem;
    }
    if (aRate.theItemIdentification.startsWith(prefix)) {
      return new Item(aRate, theExecutionTime);
    }
    return null;
  }
}
