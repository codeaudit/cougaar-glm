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
package org.cougaar.glm.execution.common;

import java.io.IOException;
import org.cougaar.core.util.UID;

/**
 * Report the failure or consumption of an item in an inventory. This
 * is identical to an InventoryReport, but is processed differently
 * (creates a supply task rather than updating the inventory
 * baseline).
 **/
public class FailureConsumptionReport extends Report {
  public UID theTaskUID;        // The UID of the projection task
  public String theItemIdentification; // The asset being consumed or failed
  public double theQuantity;    // How much was consumed/failed
  public String theConsumer;    // The consumer (display only)

  public FailureConsumptionReport(UID aTaskUID,
                                  String itemIdentification,
                                  long aReportDate,
                                  long aReceivedDate,
                                  double quantity,
                                  String aConsumer) {
    super(aReportDate, aReceivedDate);
    theTaskUID = aTaskUID;
    theItemIdentification = itemIdentification.intern();
    theQuantity = quantity;
    theConsumer = aConsumer;
  }

  FailureConsumptionReport() {}

  public FailureConsumptionReport(FailureConsumptionReport original) {
    this(original.theTaskUID,
         original.theItemIdentification,
         original.theReportDate,
         original.theReceivedDate,
         original.theQuantity,
         original.theConsumer);
  }

  public void write(LineWriter writer) throws IOException {
    super.write(writer);
    writer.writeUID(theTaskUID);
    writer.writeUTF(theItemIdentification);
    writer.writeDouble(theQuantity);
    writer.writeUTF(theConsumer);
  }

  public void read(LineReader reader) throws IOException {
    super.read(reader);
    theTaskUID = reader.readUID();
    theItemIdentification = reader.readUTF().intern();
    theQuantity = reader.readDouble();
    theConsumer = reader.readUTF();
  }

  public String toString() {
    return "FCR: " + theQuantity + " of " + theItemIdentification + " for " + theConsumer;
  }
}
