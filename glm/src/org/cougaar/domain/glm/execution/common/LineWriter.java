package org.cougaar.domain.glm.execution.common;

import java.util.GregorianCalendar;
import java.io.Serializable;
import java.io.IOException;
import org.cougaar.core.society.UID;

/**
 * Due to assumptions made in the PSP support classes, direct access
 * to the stream into a PSP is inaccessible. The only accessible
 * information is the array of lines read from the stream. So this
 * becomes the lowest common denominator for the form of information
 * passed back and forth between clusters and the EventGenerator.
 *
 * Note that the methods declared here can be satisfied by a PrintWriter
 **/
public interface LineWriter {
  void close() throws IOException;
  void flush() throws IOException;
  void writeUTF(String s) throws IOException;
  void writeObject(Serializable o) throws IOException;
  void writeEGObject(EGObject o) throws IOException;
  void writeUID(UID uid) throws IOException;
  void writeBoolean(boolean b) throws IOException;
  void writeByte(byte b) throws IOException;
  void writeShort(short b) throws IOException;
  void writeInt(int i) throws IOException;
  void writeLong(long l) throws IOException;
  void writeDouble(double d) throws IOException;
  void writeFloat(float f) throws IOException;
}
