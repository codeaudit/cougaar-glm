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

import java.io.IOException;
import java.io.CharArrayReader;

public class CharArrayLineReader extends InputStreamLineReader implements LineReader {
  public CharArrayLineReader(char[] chars) {
    super(new CharArrayReader(chars));
  }
}
