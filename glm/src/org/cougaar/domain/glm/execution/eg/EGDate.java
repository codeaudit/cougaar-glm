package org.cougaar.domain.glm.execution.eg;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Extend java.util.Date to modify the toString method. Used as table
 * cell values.
 **/
public class EGDate extends Date {
  private static final SimpleDateFormat format =
    new SimpleDateFormat("MM/dd/yyyy HH:mm (EEE)");

  public EGDate(long millis) {
    super(millis);
  }

  public EGDate(String s) {
    super(s);
  }

  public static String format(Date date) {
    return format.format(date);
  }

  public String toString() {
    return format(this);
  }
}

			   
