package org.cougaar.domain.glm.execution.common;

import java.io.IOException;
import org.cougaar.core.society.UID;

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
