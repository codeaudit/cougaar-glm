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
package org.cougaar.domain.glm.execution.common;

import java.io.IOException;
import java.io.Serializable;

public class InventoryReport extends Report implements Serializable, EGObject {
  public String theItemIdentification;
  public double theQuantity;

  public InventoryReport(String itemIdentification,
                         long aReportDate,
                         long aReceivedDate,
                         double quantity) {
    super(aReportDate, aReceivedDate);
    theItemIdentification = itemIdentification;
    theQuantity = quantity;
  }

  InventoryReport() {}

  public InventoryReport(InventoryReport original) {
    this(original.theItemIdentification,
         original.theReportDate,
         original.theReceivedDate,
         original.theQuantity);
  }

  public void write(LineWriter writer) throws IOException {
    super.write(writer);
    writer.writeUTF(theItemIdentification);
    writer.writeDouble(theQuantity);
  }

  public void read(LineReader reader) throws IOException {
    super.read(reader);
    theItemIdentification = reader.readUTF();
    theQuantity = reader.readDouble();
  }
}

    
