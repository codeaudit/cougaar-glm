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

public interface TimeConstants {
  public static final long ONE_MILLISECOND = 1L;
  public static final long ONE_SECOND = 1000L;
  public static final long ONE_MINUTE = 60L * ONE_SECOND;
  public static final long ONE_HOUR = ONE_MINUTE * 60L;
  public static final long ONE_DAY = ONE_HOUR * 24L;
  public static final long ONE_WEEK = ONE_DAY * 7L;
  public static final long ONE_MONTH = ONE_DAY * 30L; // Approximate is ok
}
