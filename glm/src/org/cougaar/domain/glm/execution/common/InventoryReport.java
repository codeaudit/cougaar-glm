package org.cougaar.domain.glm.execution.common;

import java.io.IOException;

public class InventoryReport extends Report implements EGObject {
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

    
