/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
