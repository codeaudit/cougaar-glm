/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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
