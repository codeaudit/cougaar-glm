package org.cougaar.domain.glm.execution.common;

import java.io.IOException;
import java.io.CharArrayReader;

public class CharArrayLineReader extends InputStreamLineReader implements LineReader {
  public CharArrayLineReader(char[] chars) {
    super(new CharArrayReader(chars));
  }
}
