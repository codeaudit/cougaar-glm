/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.execution.eg;

public abstract class Timed implements Comparable {
  private static int nextTieBreaker = 0;
  private int tieBreaker = nextTieBreaker++;
  private boolean enabled = false;

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
