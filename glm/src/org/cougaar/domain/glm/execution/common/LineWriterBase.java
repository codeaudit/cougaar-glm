/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.common;

import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.Serializable;
import java.io.IOException;
import org.cougaar.core.society.UID;

public abstract class LineWriterBase implements LineWriter {
  public abstract void writeLine(String s) throws IOException;

  public abstract void flush() throws IOException;

  public abstract void close() throws IOException;

  public void writeUTF(String s) throws IOException {
    writeLine(s);
  }

  public void writeBoolean(boolean b) throws IOException {
    writeLine(b ? "true" : "false");
  }

  public void writeByte(byte b) throws IOException {
    writeLine(Byte.toString(b));
  }

  public void writeChar(char c) throws IOException {
    writeLine("" + c);
  }

  public void writeShort(short c) throws IOException {
    writeLine(Short.toString(c));
  }

  public void writeInt(int i) throws IOException {
    writeLine(Integer.toString(i));
  }

  public void writeLong(long l) throws IOException {
    writeLine(Long.toString(l));
  }

  public void writeDouble(double d) throws IOException {
    writeLine(Double.toString(d));
  }

  public void writeFloat(float f) throws IOException {
    writeLine(Float.toString(f));
  }

  public void writeEGObject(EGObject o) throws IOException {
    writeInt(o.getClassIndex());
    o.write(this);
  }

  public void writeUID(UID uid) throws IOException {
    writeUTF(uid.getOwner());
    writeLong(uid.getId());
  }

  /**
   * Base64 encode the serialized object. This may not be the official
   * encoding by that name, but it suffices.
   **/
  public void writeObject(Serializable o) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream(baos);
    os.writeObject(o);
    int nb = baos.size();
    while (nb % 3 != 0) {
      baos.write(0);            // pad with zeros to a multiple of three
      nb++;
    }
    writeInt(nb);
    byte[] bytes = baos.toByteArray(); // The bytes to encode
//      writeByteArray(bytes);
    char[] line = new char[80]; // Encode into this array
    int pb = 0;
    while (pb < nb) {
      int eb = Math.min(nb, pb + 60);
      int nc = 0;
      while (pb < eb) {
        int threeBytes = 0;
        threeBytes = (((bytes[pb  ] & 0xff) << 16) |
                      ((bytes[pb+1] & 0xff) <<  8) |
                      ((bytes[pb+2] & 0xff)      ));
        pb += 3;
        line[nc++] = base64Encoding[(threeBytes >> 18) & 0x3f];
        line[nc++] = base64Encoding[(threeBytes >> 12) & 0x3f];
        line[nc++] = base64Encoding[(threeBytes >>  6) & 0x3f];
        line[nc++] = base64Encoding[(threeBytes      ) & 0x3f];
      }
      writeLine(new String(line, 0, nc));
    }
  }

  public static void writeByteArray(byte[] bytes) {
    for (int i = 0; i < bytes.length; i++) {
      if (i > 0) {
        if (i % 20 == 0) {
          System.out.println();
        } else {
          System.out.print(" ");
        }
      }
      String hex = Integer.toHexString(bytes[i] & 0xff);
      if (hex.length() == 1) hex = "0" + hex;
      System.out.print(hex);
    }
  }

  public static final char[] base64Encoding = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
    'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
    'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
    'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
  };                  
}
