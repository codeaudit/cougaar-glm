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
import java.io.Serializable;
import java.io.IOException;
import org.cougaar.core.util.UID;

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
