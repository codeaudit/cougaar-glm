package org.cougaar.domain.glm.execution.common;

import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.cougaar.core.society.UID;

/**
 * An implementation of the LineReader interface as an abstract base
 * class. the readLine method is abstract.
 **/
public abstract class LineReaderBase implements LineReader {
  public abstract String readLine() throws IOException;

  public EGObject readEGObject() throws IOException {
    try {
      int classIndex = readInt();
      EGObject o = (EGObject) EGObject.egObjectClasses[classIndex].newInstance();
      o.read(this);
      return o;
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException(e.toString());
    }
  }

  public String readUTF() throws IOException {
    return readLine();
  }

  public UID readUID() throws IOException {
    String uidOwner = readUTF();
    long uidId = readLong();
    return new UID(uidOwner, uidId);
  }

  public double readDouble() throws IOException {
    try {
      return Double.parseDouble(readLine());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(nfe.toString());
    }
  }

  public float readFloat() throws IOException {
    try {
      return Float.parseFloat(readLine());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(nfe.toString());
    }
  }

  public byte readByte() throws IOException {
    try {
      return Byte.parseByte(readLine());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(nfe.toString());
    }
  }

  public short readShort() throws IOException {
    try {
      return Short.parseShort(readLine());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(nfe.toString());
    }
  }

  public int readInt() throws IOException {
    try {
      return Integer.parseInt(readLine());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(nfe.toString());
    }
  }

  public long readLong() throws IOException {
    try {
      return Long.parseLong(readLine());
    } catch (NumberFormatException nfe) {
      throw new RuntimeException(nfe.toString());
    }
  }

  public boolean readBoolean() throws IOException {
    return readLine().equals("true");
  }

  public Object readObject() throws IOException {
    try {
      int nb = readInt();
      byte[] bytes = new byte[nb];
      int pb = 0;
      char[] line = new char[80];
      while (pb < nb) {
        String s = readLine();
        int nc = s.length();
        s.getChars(0, nc, line, 0);
        int pc = 0;
        while (pc < nc) {
          int threeBytes = ((base64Decoding[line[pc  ]] << 18) |
                            (base64Decoding[line[pc+1]] << 12) |
                            (base64Decoding[line[pc+2]] <<  6) |
                            (base64Decoding[line[pc+3]]      ));
          pc += 4;
          bytes[pb++] = (byte) (threeBytes >> 16);
          bytes[pb++] = (byte) (threeBytes >>  8);
          bytes[pb++] = (byte) (threeBytes      );
        }
      }
//        LineWriterBase.writeByteArray(bytes);
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      ObjectInputStream is = new ObjectInputStream(bais);
      return is.readObject();
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }
  }

  public static byte[] base64Decoding = new byte[128];

  static {
    for (byte i = 0; i < 64; i++) {
      char c = LineWriterBase.base64Encoding[i];
      base64Decoding[c] = i;
    }
  }
}
