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

import java.util.GregorianCalendar;
import java.io.IOException;
import org.cougaar.core.society.UID;

/**
 * Due to assumptions made in the PSP support classes, direct access
 * to the stream into a PSP is inaccessible. The only accessible
 * information is the array of lines read from the stream. So this
 * becomes the lowest common denominator for the form of information
 * passed back and forth between clusters and the EventGenerator.
 **/
public interface LineReader {
  /**
   * Recover resources, close underlying streams, etc.
   **/
  void close() throws IOException;

  /**
   * Read a String. The name reflects that name of a similar function
   * in DataInputStream, but there is not necessarily any actual UTF
   * encoding involved. The String must not contain anything that
   * could be confused with and end-of-line.
   **/
  String readUTF() throws IOException;

  UID readUID() throws IOException;

  /**
   * Read a boolean. true is spelled "true".
   **/
   boolean readBoolean() throws IOException;

  /**
   * Read an integer as a byte
   **/
  byte readByte() throws IOException;

  /**
   * Read an integer as a short
   **/
  short readShort() throws IOException;

  /**
   * Read an integer
   **/
  int readInt() throws IOException;

  /**
   * Read a long
   **/
  long readLong() throws IOException;
  /**
   * Read a double.
   **/
  double readDouble() throws IOException;

  /**
   * Read a float.
   **/
  float readFloat() throws IOException;
  /**
   * Read an object.
   **/
  Object readObject() throws IOException;
  /**
   * Read an EGObject.
   **/
  EGObject readEGObject() throws IOException;
}
