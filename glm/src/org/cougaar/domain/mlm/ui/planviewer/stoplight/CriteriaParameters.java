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
 
package org.cougaar.domain.mlm.ui.planviewer.stoplight;

import org.cougaar.core.util.AsciiPrinter;
import org.cougaar.core.util.SelfPrinter;

public class CriteriaParameters implements SelfPrinter {
  String perCentOperation;
  int perCent;
  String daysLateOperation;
  int daysLate;
  String color;

  public CriteriaParameters(String perCentOperation,
                            int perCent,
                            String daysLateOperation,
                            int daysLate,
                            String color) {
    this.perCentOperation = perCentOperation;
    this.perCent = perCent;
    this.daysLateOperation = daysLateOperation;
    this.daysLate = daysLate;
    this.color = color;
  }

  public CriteriaParameters() {
  }

  public String getPerCentOperation() {
    return perCentOperation;
  }

  public int getPerCent() {
    return perCent;
  }

  public String getDaysLateOperation() {
    return daysLateOperation;
  }

  public int getDaysLate() {
    return daysLate;
  }

  public String getColor() {
    return color;
  }

  public void setPerCentOperation(String perCentOperation) {
    this.perCentOperation = perCentOperation;
  }

  public void setPerCent(int perCent) {
    this.perCent = perCent;
  }

  public void setDaysLateOperation(String daysLateOperation) {
    this.daysLateOperation = daysLateOperation;
  }

  public void setDaysLate(int daysLate) {
    this.daysLate = daysLate;
  }

  public void setColor(String color) {
    this.color = color;
  }

  /** Used to save criteria parameters to a file. 
   */

  public void printContent(AsciiPrinter pr) {
    pr.print(perCentOperation, "PerCentOperation");
    pr.print(perCent, "PerCent");
    pr.print(daysLateOperation, "DaysLateOperation");
    pr.print(daysLate, "DaysLate");
    pr.print(color, "Color");
  }

}
