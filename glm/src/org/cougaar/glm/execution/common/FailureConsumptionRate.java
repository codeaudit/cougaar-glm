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

public class FailureConsumptionRate extends EGObjectBase implements EGObject {
  public static class Rescind extends FailureConsumptionRate {
    public Rescind() {}
    public Rescind(UID aTaskUID) {
      theTaskUID = aTaskUID;
    }
    public boolean isRescind() { return true; }
  }
  public boolean isRescind() { return false; }

  public UID theTaskUID;
  public long theStartTime;
  public long theEndTime;
  public double theRateValue;
  public String theItemIdentification = ""; // Insure non-null for Rescind
  public String theItemName = "";
  public String theRateUnits = "";          // Insure non-null for Rescind
  public double theRateMultiplier;
  public String theConsumer = "";           // What kind of asset is consuming this item
  public String theConsumerId = "";         // The id of the asset consuming this item

  public FailureConsumptionRate(UID aTaskUID,
                                String anItemIdentification,
				String anItemName,
                                long aStartTime,
                                long anEndTime,
                                double aRateValue,
                                String aRateUnits,
                                double aRateMultiplier,
                                String aConsumerId,
                                String aConsumer)
  {
    theTaskUID = aTaskUID;
    theItemIdentification = anItemIdentification.intern();
    theItemName = anItemName;
    theStartTime = aStartTime;
    theEndTime = anEndTime;
    theRateValue = aRateValue;
    theRateUnits = aRateUnits;
    theRateMultiplier = aRateMultiplier;
    theConsumerId = aConsumerId;
    theConsumer = aConsumer;
  }

  FailureConsumptionRate() {}
  public FailureConsumptionRate(FailureConsumptionRate original) {
    this(original.theTaskUID,
         original.theItemIdentification,
         original.theItemName,
         original.theStartTime,
         original.theEndTime,
         original.theRateValue,
         original.theRateUnits,
         original.theRateMultiplier,
         original.theConsumerId,
         original.theConsumer);
  }

  public void write(LineWriter writer) throws IOException {
    writer.writeUID(theTaskUID);
    writer.writeUTF(theItemIdentification);
    writer.writeUTF(theItemName);
    writer.writeLong(theStartTime);
    writer.writeLong(theEndTime);
    writer.writeDouble(theRateValue);
    writer.writeUTF(theRateUnits);
    writer.writeDouble(theRateMultiplier);
    writer.writeUTF(theConsumerId);
    writer.writeUTF(theConsumer);
  }

  public void read(LineReader reader) throws IOException {
    theTaskUID = reader.readUID();
    theItemIdentification = reader.readUTF().intern();
    theItemName = reader.readUTF();
    theStartTime = reader.readLong();
    theEndTime = reader.readLong();
    theRateValue = reader.readDouble();
    theRateUnits = reader.readUTF();
    theRateMultiplier = reader.readDouble();
    theConsumerId = reader.readUTF();
    theConsumer = reader.readUTF();
  }
}
