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

import org.cougaar.core.util.UID;

public class TaskEventId implements Comparable {
  public UID theTaskUID;
  public int theAspectType;
  private int hc;

  public TaskEventId(UID aTaskUID, int anAspectType) {
    theTaskUID = aTaskUID;
    theAspectType = anAspectType;
    hc = theTaskUID.hashCode() + theAspectType;
  }

  public int hashCode() {
    return hc;
  }

  public boolean equals(Object o) {
    if (o instanceof TaskEventId) {
      TaskEventId other = (TaskEventId) o;
      return (theTaskUID.equals(other.theTaskUID) && theAspectType == other.theAspectType);
    }
    return false;
  }

  public int compareTo(Object o) {
    TaskEventId key = (TaskEventId) o;
    int diff = theTaskUID.compareTo(key.theTaskUID);
    if (diff == 0) {
      diff = theAspectType - key.theAspectType;
    }
    return diff;
  }

  public String toString() {
    return theTaskUID.toString() + ":" + theAspectType;
  }
}
