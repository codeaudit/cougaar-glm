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
import java.io.Serializable;

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
