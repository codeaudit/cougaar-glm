package org.cougaar.domain.glm.execution.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class StringArrayLineReader extends LineReaderBase implements LineReader {
  private ArrayList lines = new ArrayList();
  private int currentLine = 0;

  public StringArrayLineReader(String[] lines) {
    this.lines.addAll(Arrays.asList(lines));
  }

  public String readLine() throws IOException {
    if (lines == null) throw new IOException("LineReader had been closed");
    if (currentLine >= lines.size()) return null;
    return (String) lines.get(currentLine++);
  }

  public void close() {
    lines = null;
  }
}
