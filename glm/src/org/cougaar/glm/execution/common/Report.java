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
