package org.cougaar.domain.glm.execution.common;

import java.io.IOException;

/**
 * Base class for all reports. Supplies the date when the report was
 * initiated and the date when the report was received by the cluster.
 **/
public abstract class Report extends EGObjectBase implements EGObject {
  public long theReportDate = -1;
  public long theReceivedDate = -1;

  public Report(long aReportDate, long aReceivedDate) {
    theReportDate = aReportDate;
    theReceivedDate = aReceivedDate;
  }

  public Report() {
  }

  public void write(LineWriter writer) throws IOException {
    writer.writeLong(theReportDate);
    writer.writeLong(theReceivedDate);
  }

  public void read(LineReader reader) throws IOException {
    theReportDate = reader.readLong();
    theReceivedDate = reader.readLong();
  }
}
