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

    
