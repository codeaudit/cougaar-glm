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
public class POLSpecialPlugIn implements FailureConsumptionPlugIn, TimeConstants {

  private String prefix = "POL";
  private static final long MIN_INTERVAL = ONE_DAY;

  private static final Random random = new Random();
  private EventGenerator theEventGenerator = null;

  private static Hashtable consumers = new Hashtable();
  private static Hashtable fuelTypes = new Hashtable();
  private static Hashtable hash = new Hashtable();

  private JComboBox cb1 = new JComboBox();
  private JComboBox cb2 = new JComboBox();
  private JComboBox cb3 = new JComboBox();

  private JTextField start = new JTextField(20);
  private JTextField end = new JTextField(20);
  private JTextField mult = new JTextField(20);

    private class ComboBoxActionListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
  	    if ((cb1.getSelectedItem() != null) &&
		(cb2.getSelectedItem() != null) &&
		(cb3.getSelectedItem() != null)) {
		TripletKey tk_temp = new TripletKey((String)fuelTypes.get(cb1.getSelectedItem()),
						    (String)consumers.get(cb2.getSelectedItem()),
						    (String)cb3.getSelectedItem());
		TripletValue tv = (TripletValue)hash.get(tk_temp);
		if (tv != null) {
		    start.setText(new EGDate(tv.getStartDate()).toString());
		    end.setText(new EGDate(tv.getEndDate()).toString());
		    mult.setText(new Double(tv.getMultiplier()).toString());
		} else {
		    start.setText("");
		    end.setText("");
		    mult.setText("");
		}
	    }
	}
    }


  private class TripletKey {
    private String fuelType;
    private String consumer;
    private String cluster;
    private int hashCode;

    public TripletKey(String ft, String co, String cl) {
      fuelType = ft;
      consumer = co;
      cluster = cl;
      hashCode = ft.hashCode() + co.hashCode() + cl.hashCode();
    }

    public String getFuelType() {
	return fuelType;
    }

    public String getConsumer() {
	return consumer;
    }

    public String getCluster() {
	return cluster;
    }

    public boolean equals(Object o) {
	TripletKey tk = (TripletKey) o;
	if (tk.getFuelType().equals(fuelType) &&
	    tk.getConsumer().equals(consumer) &&
	    tk.getCluster().equals(cluster)) {
 	   return true;
	} else {
	   return false;
        }
    }

    public int hashCode() {
	return hashCode;
    }
  }


  private class TripletValue {
    private long startDate;
    private long endDate;
    private double multiplier;

    public TripletValue(long start, long end, double mult) {
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
    public double getMultiplier() {
      return multiplier;
    }
  }


  private class POLItem extends FailureConsumptionPlugInItem {
    long previousTime = 0L;
    TripletKey tk;
    TripletKey tkAll;

    public POLItem(FailureConsumptionRate aRate, long theExecutionTime, FailureConsumptionSegment aSegment) {
      super(aRate);
      previousTime = Math.max(theExecutionTime, aRate.theStartTime);
      tk = new TripletKey(aRate.theItemIdentification, aRate.theConsumerId, aSegment.theSource);
      tkAll = new TripletKey(aRate.theItemIdentification, aRate.theConsumerId, "All Clusters");
    }


    private AnnotatedDouble getQPerMilli(long executionTime) {
      if (executionTime - previousTime < MIN_INTERVAL) {
        return new AnnotatedDouble(0.0);
      }
      double multiplier = 1.0;
      TripletValue tv = (TripletValue)hash.get(tk);
      if (tv == null) {
        tv = (TripletValue)hash.get(tkAll);
      }
      if (tv != null) {
        long startDate = tv.getStartDate();
        long endDate = tv.getEndDate();
        if (startDate <= executionTime &&
  	    endDate > executionTime) {
	   multiplier = tv.getMultiplier();
        }
      }
      double result = ((theFailureConsumptionRate.theRateValue
                        * multiplier
                        / theFailureConsumptionRate.theRateMultiplier)
                       / ONE_DAY);
      return new AnnotatedDouble(result);
    }

    public AnnotatedDouble getQuantity(long executionTime) {
      AnnotatedDouble v = getQPerMilli(executionTime);
      if (v.value <= 0.0) {
        v.value = 0.0;
        return v;
      }
      long elapsed = executionTime - previousTime;
      previousTime = executionTime;
      v.value = random.nextPoisson(elapsed * v.value);
      return v;
    }

    public long getTimeQuantum(long executionTime) {
      AnnotatedDouble v = getQPerMilli(executionTime);
      return previousTime + ((long) (1.0 / v.value)) - executionTime;
    }
  }

  /**
   * @return the name of this plugin
   **/
  public String getPlugInName() {
    return "POL Special";
  }

  public String getDescription() {
    return "Special plugin for POL items that uses a fixed rate";
  }

  public boolean isConfigurable() {
    return true;
  }

  public void setParameter(String parameter) {
    prefix = parameter;         // parameter is item prefix.
  }

  private static void addItem(JPanel message, int x, int y, JComponent c) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.anchor = gbc.WEST;
    message.add(c, gbc);
  }


  public void configure(java.awt.Component c) {
     String fuelType;
     String consumer;
     String cluster;

    JPanel message = new JPanel(new GridBagLayout());

    cb1.addActionListener(new ComboBoxActionListener());
    cb1.removeAllItems();
    if (!fuelTypes.isEmpty()) {
	Enumeration fuelTypesEnum = fuelTypes.keys();
	while (fuelTypesEnum.hasMoreElements()) {
	    cb1.addItem(fuelTypesEnum.nextElement());
      }
    }

    cb2.addActionListener(new ComboBoxActionListener());
    cb2.removeAllItems();
    if (!consumers.isEmpty()) {
	Enumeration consumersEnum = consumers.keys();
	while (consumersEnum.hasMoreElements()) {
	    cb2.addItem(consumersEnum.nextElement());
      }
    }

    cb3.addActionListener(new ComboBoxActionListener());
    cb3.removeAllItems();
    if (theEventGenerator != null) {
      // First, add an "All" option for clusters
      cb3.addItem("All Clusters");

      // Get the cluster names from the EventGenerator and add to the list
      String[] clusterNames = theEventGenerator.getClusterNames();
      for (int i = 0; i < clusterNames.length; i++) {
        cb3.addItem(clusterNames[i]);
      }
    }


    TripletKey tk_temp = new TripletKey((String)fuelTypes.get(cb1.getSelectedItem()),
					(String)consumers.get(cb2.getSelectedItem()),
					(String)cb3.getSelectedItem());
    TripletValue tv = (TripletValue)hash.get(tk_temp);
    if (tv != null) {
	start.setText(new EGDate(tv.getStartDate()).toString());
	end.setText(new EGDate(tv.getEndDate()).toString());
	mult.setText(new Double(tv.getMultiplier()).toString());
    } else {
	start.setText("");
	end.setText("");
	mult.setText("");
    }

    addItem(message, 0, 0, new JLabel("POL Item"));
    addItem(message, 1, 0, cb1);

    addItem(message, 0, 1, new JLabel("Consumer"));
    addItem(message, 1, 1, cb2);

    addItem(message, 0, 2, new JLabel("Cluster"));
    addItem(message, 1, 2, cb3);

    addItem(message, 0, 3, new JLabel("Start Time"));
    addItem(message, 1, 3, start);

    addItem(message, 0, 4, new JLabel("End Time"));
    addItem(message, 1, 4, end);

    addItem(message, 0, 5, new JLabel("Multiplier"));
    addItem(message, 1, 5, mult);
    int result = OptionPane.showOptionDialog(c,
                                             message,
                                             "Configure " + getPlugInName(),
                                             OptionPane.OK_CANCEL_OPTION,
                                             OptionPane.QUESTION_MESSAGE,
					     null, null, null);

    if (result == OptionPane.OK_OPTION) {
	try {
	    fuelType = (String) cb1.getSelectedItem();
	    consumer = (String) cb2.getSelectedItem();
	    cluster = (String) cb3.getSelectedItem();
	    long guiStart = new EGDate(start.getText()).getTime();
	    long guiEnd = new EGDate(end.getText()).getTime();
	    TripletKey tk = new TripletKey((String)fuelTypes.get(fuelType),
					   (String)consumers.get(consumer),
					   cluster);
	    TripletValue tripVal = new TripletValue(guiStart,
						    guiEnd,
						    Double.parseDouble(mult.getText()));
	    hash.put(tk, tripVal);

	} catch (Exception e) {
	    // Ignore errors.
	    System.out.println("POLSpecialPlugIn: All values were not entered!");
	    //	System.out.println("Exception: " + e.getMessage());
	}
    }
  }

  public void save(Properties props, String prefix) {
    props.setProperty(prefix + "fuelTypes", getSimpleHashAsString(fuelTypes));
    props.setProperty(prefix + "consumers", getSimpleHashAsString(consumers));
    props.setProperty(prefix + "hash", getHashAsString(hash));
  }

  public void restore(Properties props, String prefix) {
    try {
      String fuelArrayString = props.getProperty(prefix + "fuelTypes");
      fuelTypes = parseSimpleHashString(fuelArrayString);
      String hashString = props.getProperty(prefix + "hash");
      hash = parseHashString(hashString);
      String consumersArrayString = props.getProperty(prefix + "consumers");
      consumers = parseSimpleHashString(consumersArrayString);
    } catch (Exception e) {
      // State not present in props
    }
  }


 public Hashtable parseSimpleHashString(String hs) {
   StringTokenizer st = new StringTokenizer(hs.substring(1, hs.length()-1), "!");
   Hashtable h = new Hashtable();
   while (st.hasMoreTokens()) {
       String str1 = st.nextToken();
       StringTokenizer st2 = new StringTokenizer(str1, "=");
       h.put(st2.nextToken().trim(),
	      st2.nextToken().trim());
   }
   return h;
 }

 public String getSimpleHashAsString(Hashtable h) {
   String key = "";
   StringBuffer buff = new StringBuffer();
   boolean firstTime = true;
   buff.append("{");
   Enumeration e = h.keys();
   while(e.hasMoreElements()) {
     if (!firstTime) {
       buff.append("!");
     }
     key = (String) e.nextElement();
     //     buff.append("(" + key + ")=");
     //     buff.append("(" + h.get(key) + ")");
     buff.append(key + "=");
     buff.append(h.get(key));
     firstTime = false;
   }
   buff.append("}");
   return buff.toString();
 }



 /**
  * Returns the string representation of a Hashtable in the form:
  *
  *   [(1,2,3):(500,500,2.3);(4,5,6):(1000, 1000, 5.66)]
  *
  * where (1,2,3) and (4,5,6) are the keys
  * and (500,500,2.3) and (1000, 1000, 5.66) are the values
  **/
 public String getHashAsString(Hashtable h) {
   StringBuffer buff = new StringBuffer();
   boolean firstTime = true;
   buff.append("[");
   Enumeration e = h.keys();
   while(e.hasMoreElements()) {
     if (!firstTime) {
       buff.append(";");
     }
     TripletKey tkTemp = (TripletKey)e.nextElement();
     buff.append("(" + tkTemp.getFuelType() + "," + tkTemp.getConsumer() + "," + tkTemp.getCluster() + "):");
     TripletValue tkVal = (TripletValue)h.get(tkTemp);
     buff.append("(" + tkVal.getStartDate() + "," + tkVal.getEndDate() + "," + tkVal.getMultiplier() + ")");
     firstTime = false;
   }
   buff.append("]");
   return buff.toString();
 }


 /**
  * Parses a string representation of a Hashtable and
  * returns the resulting Hashtable.
  *  
  *  Ex: [(1,2,3):(500,500,2.3);(4,5,6):(1000,1000,5.66)]
  **/
 public Hashtable parseHashString(String hs) {
   Hashtable ht = new Hashtable();

   // Strip off square brackets at beginning and end of string
   // and split the string by the ";" which separate the slots
   // of the Hashtable
   StringTokenizer str = new StringTokenizer(hs.substring(1, hs.length()-1), ";");

   while (str.hasMoreTokens()) {
     String str1 = str.nextToken();

     // Split the string by ":" which separate the hash keys from the hash values
     StringTokenizer st2 = new StringTokenizer(str1, ":");
     String str2 = st2.nextToken();

     // Split the string by "," which separate the values which make up the
     // TripletKey and TripletValue objects
     StringTokenizer st3 = new StringTokenizer(str2.substring(1, str2.length()-1), ",");
     TripletKey key = new TripletKey(st3.nextToken(),
				     st3.nextToken(),
				     st3.nextToken());

     String str3 = st2.nextToken();
     StringTokenizer st4 = new StringTokenizer(str3.substring(1, str3.length()-1), ",");

     TripletValue value = new TripletValue(Long.parseLong(st4.nextToken()),
					   Long.parseLong(st4.nextToken()),
					   Double.parseDouble(st4.nextToken()));

     ht.put(key, value);
   }
   return ht;
 }


 public void setEventGenerator(EventGenerator eg) {
    theEventGenerator = eg;
 }

  /**
   * Create a FailureConsumptionItem for this plugin to handle a
   * particular FailureConsumptionRate.
   **/
  public FailureConsumptionPlugInItem createFailureConsumptionItem
    (FailureConsumptionRate aRate,
     FailureConsumptionSegment aSegment,
     long theExecutionTime,
     FailureConsumptionPlugInItem aFailureConsumptionPlugInItem)
  {

    // Check if it's fuel (by FSC)
    // NOTE: We may need to check by the NSN if the FSC doesn't work
    if ((aRate.theItemIdentification.startsWith("NSN/9130")) ||
	(aRate.theItemIdentification.startsWith("NSN/9140"))) {
       if (!fuelTypes.containsValue(aRate.theItemIdentification)) {
	 fuelTypes.put(aRate.theItemName, aRate.theItemIdentification);
       }
       if (!consumers.containsValue(aRate.theConsumerId)) {
         consumers.put(aRate.theConsumer, aRate.theConsumerId);
       }
    }

    if (aFailureConsumptionPlugInItem instanceof POLItem
        && aFailureConsumptionPlugInItem.theFailureConsumptionRate == aRate) {
      return aFailureConsumptionPlugInItem;
    }

//      TripletKey tk_temp = new TripletKey(aRate.theItemIdentification, aRate.theConsumerId, aSegment.theSource);
//      TripletValue tk_val = (TripletValue)(hash.get(tk_temp));
//      if (tk_val == null) {
//        tk_temp = new TripletKey(aRate.theItemIdentification, aRate.theConsumerId, "All Clusters");
//        tk_val = (TripletValue)(hash.get(tk_temp));
//      }
//      if (tk_val != null) {
      return new POLItem(aRate, theExecutionTime, aSegment);
//      }
//      return null;
  }
}
