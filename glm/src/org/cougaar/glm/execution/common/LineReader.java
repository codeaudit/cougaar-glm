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
