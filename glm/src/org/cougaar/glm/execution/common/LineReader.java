/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
package org.cougaar.glm.execution.common;

import java.util.GregorianCalendar;
import java.io.IOException;
import org.cougaar.core.util.UID;

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
