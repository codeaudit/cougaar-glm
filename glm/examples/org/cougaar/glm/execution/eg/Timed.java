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
package org.cougaar.glm.execution.eg;

public abstract class Timed implements Comparable {
  private static int nextTieBreaker = 0;
  private int tieBreaker = nextTieBreaker++;
  private boolean enabled = false;
  private Object annotation = null; // Annotation supplied by plugin

  protected Timed() {
  }

  public abstract Object getKey();
  /**
   * Return true if this item has expired and should be removed from
   * the screen.
   **/
  public abstract boolean expired(long time);

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean newEnabled) {
    enabled = newEnabled;
  }

  public abstract long getTime();

  public abstract String getCluster();

  public void setAnnotation(Object o) {
    annotation = o;
  }

  public Object getAnnotation() {
    return annotation;
  }

  public int compareTo(Object o) {
    Timed other = (Timed) o;
    long diff = this.getTime() - other.getTime();
    if (diff < 0L) return -1;
    if (diff > 0L) return 1;
    return compareToTieBreaker(other);
  }

  //Override this to use a different tie breaker
  protected int compareToTieBreaker(Timed other) {
    return (other.tieBreaker - this.tieBreaker);
  }

  private String getShortClassName() {
    String result = getClass().getName();
    return result.substring(result.lastIndexOf('.') + 1);
  }

  public String toString() {
    return getShortClassName() + "@" + new EGDate(getTime());
  }

  public static class Dummy extends Timed {
    private long theTime = -1;

    public Dummy(long aTime) {
      theTime = aTime;
    }

    public Object getKey() {
      return null;
    }

    public long getTime() {
      return theTime;
    }

    public String getCluster() {
      return "";
    }

    public boolean expired(long aTime) {
      theTime = aTime;
      return true;
    }
  }
}
