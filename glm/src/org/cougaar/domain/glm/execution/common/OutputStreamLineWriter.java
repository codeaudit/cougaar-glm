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

public class OutputStreamLineWriter extends LineWriterBase implements LineWriter {
  private BufferedWriter writer;

  public OutputStreamLineWriter(OutputStream os) {
    writer = new BufferedWriter(new OutputStreamWriter(os));
  }

  public void writeLine(String s) throws IOException {
    writer.write(s);          // with the line contents
    writer.newLine();
  }

  public void flush() throws IOException {
    writer.flush();
  }

  public void close() throws IOException {
    writer.close();
  }
}
