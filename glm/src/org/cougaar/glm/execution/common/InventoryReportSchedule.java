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
package org.cougaar.glm.execution.common;

import java.io.IOException;

import org.cougaar.glm.ldm.asset.ReportSchedulePG;

public class InventoryReportSchedule extends ReportSchedule implements EGObject {
  public static class Rescind extends InventoryReportSchedule {
    public Rescind() {}
    public Rescind(String anItemIdentification) {
      theItemIdentification = anItemIdentification;
    }
    public boolean isRescind() { return true; }
  }
  public boolean isRescind() { return false; }

  public String theItemIdentification;

  public InventoryReportSchedule(String anItemIdentification, ReportSchedulePG rspg) {
    super(rspg);
    theItemIdentification = anItemIdentification;
  }

  InventoryReportSchedule() {
  }

  private String nameForPattern(int pattern) {
    return "P" + pattern;
  }

  public void write(LineWriter writer) throws IOException {
    super.write(writer);
    writer.writeUTF(theItemIdentification);
  }

  public void read(LineReader reader) throws IOException {
    super.read(reader);
    theItemIdentification = reader.readUTF();
  }
}
