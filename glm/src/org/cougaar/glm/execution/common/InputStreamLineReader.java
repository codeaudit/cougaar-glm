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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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
