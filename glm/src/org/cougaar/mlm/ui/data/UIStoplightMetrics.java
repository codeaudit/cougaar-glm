/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.mlm.ui.data;

import java.io.Serializable;
import java.util.ArrayList;

public class UIStoplightMetrics implements Serializable {
  public static final int CAPACITY = 50;

  ArrayList al;
  double totalQuantity;

  public UIStoplightMetrics() {
    al = new ArrayList(CAPACITY);
    for (int i = 0; i < CAPACITY; i++)
      al.add(new Double(0));
  }

  public ArrayList get() {
    return al;
  }

  public void add(double quantity, int daysLate) {
    if (daysLate < 0) {
      System.out.println("WARNING: Treating: " + daysLate +
                         " as arriving on time");
      daysLate = 0;
    } else if (daysLate >= CAPACITY) {
      int tmp = CAPACITY-1;
      System.out.println("WARNING: Treating: " + daysLate + " as: " +
                         tmp + " days late");
      daysLate = CAPACITY-1;
    }
    double d = ((Double)al.get(daysLate)).doubleValue();
    al.set(daysLate, new Double(d + quantity));
    totalQuantity = totalQuantity + quantity;
  }

  public boolean applyTest(String perCentOperation, int perCentNumber, 
                           String operation, int daysLate) {
    double quantityLate = 0;
    double perCent = perCentNumber * .01;

    if (operation.equals("=")) {
      quantityLate = ((Double)al.get(daysLate)).doubleValue();
    } else if (operation.equals(">=")) {
      for (int i = daysLate; i < al.size(); i++)
        quantityLate = quantityLate + ((Double)al.get(i)).doubleValue();
    } else if (operation.equals(">")) {
      for (int i = daysLate+1; i < al.size(); i++)
        quantityLate = quantityLate + ((Double)al.get(i)).doubleValue();
    } else if (operation.equals("<=")) {
      for (int i = daysLate; i >= 0; i--)
        quantityLate = quantityLate + ((Double)al.get(i)).doubleValue();
    } else if (operation.equals("<")) {
      if (daysLate == 0)
        return false; 
      for (int i = daysLate-1; i >= 0; i--)
        quantityLate = quantityLate + ((Double)al.get(i)).doubleValue();
    }
    double perCentLate = quantityLate / totalQuantity;
    if (perCentOperation.equals("="))
      return perCentLate == perCent;
    else if (perCentOperation.equals(">="))
      return perCentLate >= perCent;
    else if (perCentOperation.equals(">"))
      return perCentLate > perCent;
    else if (perCentOperation.equals("<="))
      return perCentLate <= perCent;
    else if (perCentOperation.equals("<"))
      return perCentLate < perCent;
    return false;
  }

  public static void main(String[] args) {
    UIStoplightMetrics sm = new UIStoplightMetrics();
    sm.add(2, 3); 
    sm.add(8, 3); 
    sm.add(6, 0);
    sm.add(4, 10);
    sm.add(3, -2);
    sm.add(4, 50);
    if (sm.applyTest(">=", 50, "<=", 30))
      System.out.println("more than or equal to 50% are less than or equal to 3 days late");
    if (sm.applyTest("<=", 40, ">=", 10))
      System.out.println("less than or equal to 40% are greater than or equal to 10 days late");
  }
}




