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

import java.io.EOFException;
import java.io.IOException;
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
