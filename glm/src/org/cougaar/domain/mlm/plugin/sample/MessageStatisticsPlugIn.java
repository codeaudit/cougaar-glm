/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.sample;

import org.cougaar.core.society.MessageStatistics;
import java.util.Vector;

/**
 * Scalability plugin at leaf : allocate tasks to assets based on value 
 * in task and value associated with plugin
 **/
public class MessageStatisticsPlugIn extends org.cougaar.core.plugin.SimplePlugIn
{
  private long interval;
  public void setupSubscriptions()
  {
    Vector params = getParameters();
    if (params.size() > 0) {
      interval = Long.parseLong((String) params.elementAt(0));
    } else {
      interval = 60000L;
    }
    wakeAfterRealTime(interval);
  }

  // Grab tasks and allocate to asset
  public void execute()
  {
    printHistogram();
    wakeAfterRealTime(interval);
  }
  private void printHistogram() {
    Object cluster = getCluster();
    MessageStatistics.Statistics messageStatistics = null;
    if (cluster instanceof MessageStatistics) {
      messageStatistics = ((MessageStatistics) cluster).getMessageStatistics(true);
      StringBuffer buf = new StringBuffer();
      buf.append("\nMessage Histogram:\n");
      for (int bin = 0; bin < MessageStatistics.NBINS; bin++) {
        buf.append(formatLong(MessageStatistics.BIN_SIZES[bin], 13));
        buf.append(":");
        buf.append(formatLong(messageStatistics.histogram[bin], 10));
        buf.append("\n");
      }
      System.out.println(buf.toString());
    } else {
      System.out.println("No Message Histogram:");
    }
  }

  private static String formatLong(long n, int w) {
    String digits = "" + n;
    return "                ".substring(digits.length(), w) + digits;
  }
}
