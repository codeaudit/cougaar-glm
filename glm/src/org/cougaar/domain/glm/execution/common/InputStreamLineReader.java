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
