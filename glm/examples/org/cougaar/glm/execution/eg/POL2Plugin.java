/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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
package org.cougaar.glm.execution.eg;

import org.cougaar.util.OptionPane;
import org.cougaar.util.Random;
import org.cougaar.glm.execution.common.FailureConsumptionRate;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Example F/C plugin illustrating how one might write a plugin that
 * applies to a certain class of supply and how to use the GUI
 * configuration features. Consult the default plugin for more mundane
 * matters.
 **/
public class POL2Plugin extends TripletFCPlugin {
  private String prefix = "POL";
  private static String[] valueLabels = {
    "Start Time",
    "End Time",
    "Multiplier"
  };
  private static Class[] valueClasses = {
    EGDate.class,
    EGDate.class,
    Double.class
  };

  protected class TripletValueImpl implements TripletValue {
    private long startDate;
    private long endDate;
    private double multiplier;

    public TripletValueImpl(long start, long end, double mult) {
      startDate = start;
      endDate = end;
      multiplier = mult;
    }

    public long getStartDate() {
      return startDate;
    }
    public long getEndDate() {
      return endDate;
    }
    public AnnotatedDouble getMultiplier() {
      return new AnnotatedDouble(multiplier);
    }
    public int getFieldCount() {
      return 3;
    }
    public Object getFieldValue(int ix) {
      switch (ix) {
      case 0: return new EGDate(startDate);
      case 1: return new EGDate(endDate);
      case 2: return new Double(multiplier);
      default: return "";
      }
    }
    public Object getDefaultFieldValue(int ix) {
      switch (ix) {
      case 0: return new EGDate(theEventGenerator.getExecutionTime());
      case 1: return new EGDate(theEventGenerator.getExecutionTime() + 1800L * ONE_DAY);
      case 2: return new Double(1.0);
      default: return "";
      }
    }
    public String toString() {
      return new StringBuffer()
        .append(getFieldValue(0))
        .append(",")
        .append(getFieldValue(1))
        .append(",")
        .append(getFieldValue(2))
        .toString();
    }
  }

  protected TripletValue createTripletValue(String[] args) {
    long guiStart = new EGDate(args[0]).getTime();
    long guiEnd = new EGDate(args[1]).getTime();
    double multiplier = Double.parseDouble(args[2]);
    return new TripletValueImpl(guiStart, guiEnd, multiplier);
  }

  protected String[] getTripletValueNames() {
    return valueLabels;
  }

  protected Class[] getTripletValueClasses() {
    return valueClasses;
  }

  protected Object[] getTripletDefaultValues() {
    Object[] result = {
      new EGDate(theEventGenerator.getExecutionTime()),
      new EGDate(theEventGenerator.getExecutionTime() + 1800L * ONE_DAY),
      new Double(1.0),
    };
    return result;
  }

  /**
   * @return the name of this plugin
   **/
  public String getPluginName() {
    return "POL Special";
  }

  public String getDescription() {
    return "Special plugin for POL items that uses a fixed rate";
  }

  public void setParameter(String parameter) {
    prefix = parameter;         // parameter is item prefix.
  }

  /**
   * Create a FailureConsumptionItem for this plugin to handle a
   * particular FailureConsumptionRate. If the consumable is a
   * suitable item (POL NSN), use the base class method, else return
   * null.
   **/
  public FailureConsumptionPluginItem createFailureConsumptionItem
    (FailureConsumptionRate aRate,
     FailureConsumptionSegment aSegment,
     long theExecutionTime,
     FailureConsumptionPluginItem aFailureConsumptionPluginItem)
  {

    // Check if it's fuel (by FSC)
    // NOTE: We may need to check by the NSN if the FSC doesn't work
    if ((aRate.theItemIdentification.startsWith("NSN/9130")) ||
	(aRate.theItemIdentification.startsWith("NSN/9140"))) {
         System.out.println("Adding " + aRate.theItemIdentification);
         return super.createFailureConsumptionItem(aRate, aSegment,
                                                   theExecutionTime,
                                                   aFailureConsumptionPluginItem);
    } else {
         System.out.println("Ignoring " + aRate.theItemIdentification);
    }
    return null;
  }
}
