/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.IOException;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * An implementation of the LineReader interface that reads from an
 * InputStream. The InputStream is wrapped in a BufferedReader and the
 * lines are read as needed.
 **/
public class InputStreamLineReader extends LineReaderBase implements LineReader {
  private static final String ack = "<ACK>";
  BufferedReader reader;

  public InputStreamLineReader(InputStream is) {
    reader = new BufferedReader(new InputStreamReader(is));
  }

  public InputStreamLineReader(Reader reader) {
    this.reader = new BufferedReader(reader);
  }

  public String readLine() throws IOException {
    if (reader == null) throw new IOException("LineReader had been closed");
    while (true) {
      String result = reader.readLine();
      if (result == null) throw new EOFException();
      while (result.trim().startsWith(ack)) {
        result = result.substring(ack.length());
      }
      return result;
    }
  }

  public void close() throws IOException {
    reader.close();
    reader = null;
  }
}
