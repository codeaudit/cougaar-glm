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

package org.cougaar.mlm.ui.util;

import java.io.OutputStream;
import org.cougaar.core.util.*;

/**
 * @see AbstractPrinter
 */
public abstract class AsciiPrettyPrinter extends AsciiPrinter {

  public AsciiPrettyPrinter(java.io.OutputStream out) {
    super(out);
  }

  protected int indent = 0;

  protected static String[] presetIndents;
  static {
    growPresetIndents();
  }

  protected static void growPresetIndents() {
    int newLen = ((presetIndents != null) ? 
	          (2*presetIndents.length) :
		  10);
    presetIndents = new String[newLen];
    StringBuffer sb = new StringBuffer(2*newLen);
    for (int i = 0; i < newLen; i++) {
      presetIndents[i] = sb.toString();
      sb.append("  ");
    }
  }

  protected void printIndent() {
    while (true) {
      try {
        print(presetIndents[indent]);
	return;
      } catch (ArrayIndexOutOfBoundsException e) {
	// rare!
	if (indent < 0)
	  throw new RuntimeException("NEGATIVE INDENT???");
	growPresetIndents();
      }
    }
  }

}
