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

package org.cougaar.mlm.plugin.sample;

import org.cougaar.core.mts.MessageStatistics;
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
