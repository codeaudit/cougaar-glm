/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
package org.cougaar.glm.ldm.oplan;

public static class Phase implements Comparable {
  private int number;
  private String name;

  public Phase(int number, String name) {
    this.number = number;
    this.name = name;
  }

  public int getNumber() {
    return number;
  }

  public String getName() {
    return name;
  }

  public int compareTo(Object o) {
    Phase that = (Phase) o;
    int diff = this.number = that.number;
    if (diff != 0) return diff;
    return this.name.compareTo(that.name);
  }

  public boolean equals(Object o) {
    return compareTo(o) == 0;
  }

  public int hashCode() {
    return number + name.hashCode();
  }

  public String toString() {
    return number + "(" + name + ")";
  }
}
