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
package org.cougaar.domain.glm.execution.common;

import java.io.IOException;
import java.io.EOFException;
import java.util.Collection;

public class EGObjectArray extends EGObjectBase implements EGObject {
  public EGObject[] egObjects;

  public EGObjectArray(Collection c) {
    egObjects = (EGObject[]) c.toArray(new EGObject[c.size()]);
  }
    
  public EGObjectArray() {
  }
    
  public void read(LineReader reader) throws IOException {
    int i = 0;
    try {
      egObjects = new EGObject[reader.readInt()];
      for (i = 0; i < egObjects.length; i++) {
        egObjects[i] = reader.readEGObject();
      }
    } catch (EOFException e) {
      throw new IOException("Premature eof after " + i + " elements");
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception e) {
      throw new IOException(e.getClass().getName() + ": " + e.getMessage());
    }
  }

  public void write(LineWriter writer) throws IOException {
    writer.writeInt(egObjects.length);
    for (int i = 0; i < egObjects.length; i++) {
      writer.writeEGObject(egObjects[i]);
      Class egClass = egObjects[i].getClass();
    }
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("EGObjectArray[");
    for (int i = 0; i < egObjects.length; i++) {
      if (i > 0) buf.append(",");
      buf.append(egObjects[i]);
    }
    buf.append("]");
    return buf.toString();
  }
}
