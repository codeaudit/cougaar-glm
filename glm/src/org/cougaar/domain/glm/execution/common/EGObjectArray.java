package org.cougaar.domain.glm.execution.common;

import java.io.IOException;
import java.io.EOFException;
import java.util.Collection;

public class EGObjectArray extends EGObjectBase implements EGObject {
  public EGObject[] egObjects;

  public EGObjectArray(Collection c) {
    egObjects = (EGObject[]) c.toArray(new EGObject[c.size()]);
  }
    
  public EGObjectArray() {
  }
    
  public void read(LineReader reader) throws IOException {
    int i = 0;
    try {
      egObjects = new EGObject[reader.readInt()];
      for (i = 0; i < egObjects.length; i++) {
        egObjects[i] = reader.readEGObject();
      }
    } catch (EOFException e) {
      throw new IOException("Premature eof after " + i + " elements");
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception e) {
      throw new IOException(e.getClass().getName() + ": " + e.getMessage());
    }
  }

  public void write(LineWriter writer) throws IOException {
    writer.writeInt(egObjects.length);
    for (int i = 0; i < egObjects.length; i++) {
      writer.writeEGObject(egObjects[i]);
      Class egClass = egObjects[i].getClass();
    }
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("EGObjectArray[");
    for (int i = 0; i < egObjects.length; i++) {
      if (i > 0) buf.append(",");
      buf.append(egObjects[i]);
    }
    buf.append("]");
    return buf.toString();
  }
}
