/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.common;

import java.io.Serializable;
import org.cougaar.domain.glm.ldm.asset.ReportSchedulePG;
import java.io.IOException;

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
