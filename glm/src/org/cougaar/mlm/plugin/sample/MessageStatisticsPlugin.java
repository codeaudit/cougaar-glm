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

package org.cougaar.mlm.plugin.sample;

import java.util.Vector;

import org.cougaar.core.mts.MessageStatistics;

/**
 * Scalability plugin at leaf : allocate tasks to assets based on value 
 * in task and value associated with plugin
 **/
public class MessageStatisticsPlugin extends org.cougaar.planning.plugin.legacy.SimplePlugin
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
