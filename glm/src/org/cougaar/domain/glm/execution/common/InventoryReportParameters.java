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

import java.io.IOException;

public class InventoryReportParameters extends EGObjectBase implements EGObject {
  public String theItemIdentification;
  public long theReportDate;

  public InventoryReportParameters(String anItemIdentification, long aReportDate) {
    theItemIdentification = anItemIdentification;
    theReportDate = aReportDate;
  }

  InventoryReportParameters() {
  }

  public void read(LineReader reader) throws IOException {
    theItemIdentification = reader.readUTF();
    theReportDate = reader.readLong();
  }

  public void write(LineWriter writer) throws IOException {
    writer.writeUTF(theItemIdentification);
    writer.writeLong(theReportDate);
  }
}
