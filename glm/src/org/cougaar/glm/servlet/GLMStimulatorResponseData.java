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

package org.cougaar.glm.servlet;

import org.cougaar.planning.servlet.data.xml.XMLable;
import org.cougaar.planning.servlet.data.xml.XMLWriter;
import java.io.*;
import java.util.*;

/** 
 * <pre>
 * Represents the returned data from a stimulator request 
 *
 * Sample output is :
 *
 * <results totalTime="0:06:069">
 *  <task id="1" time="0:00:090"/>
 *  <task id="2" time="0:02:994"/>
 *  <task id="3" time="0:02:985"/>
 * </results>
 *
 * </pre>
 */
public class GLMStimulatorResponseData implements XMLable, Serializable {
  public String totalTime;
  public Map taskTimes = new HashMap();
  private static double log10 = Math.log(10.0);
  private static double log5 = Math.log(5.0);
  private static double log2 = Math.log(2.0);
  private static long[] pwr10 = new long[30];
  private static final int NBUCKETS = pwr10.length * 3;
  static {
    long m = 1L;
    for (int i = 0; i < pwr10.length; i++) {
      pwr10[i] = m;
      m *= 10L;
    }
  }
  /** Response-time histogram buckets **/
  public int[] buckets = new int[NBUCKETS];

  /** The maximum used bucket number **/
  public int maxBucket = 0;
  /** The minimum used bucket number **/
  public int minBucket = NBUCKETS - 1;

  /**
   * Gets the index of the histogram bucket for the given elapsed
   * time. The buckets cover the range 0..1, 1..2, 2..5, 5..10, etc.
   **/
  private static int getBucket(long elapsed) {
    if (elapsed < 1) return 0;
    int i = (int) Math.floor(Math.log(elapsed)/log10); // 1 or less should be 0
    if (i >= pwr10.length) return NBUCKETS - 1;
    int b = (int) (elapsed / pwr10[i]);
    switch (b) {
    case 0:
    case 1: return i * 3 + 0;
    case 2:             
    case 3:             
    case 4: return i * 3 + 1;
    case 5:             
    case 6:             
    case 7:             
    case 8:             
    case 9: return i * 3 + 2;
    }
    return i * 3;
  }

  /**
   * Record the time to handle a task.
   * @param uid the task UID
   * @param time the time string to be printed
   * @param elapsed the numeric elapsed time for histogram
   **/
  public void addTaskAndTime (String uid, String time, long elapsed) {
    taskTimes.put(uid, time);
    int bucket = getBucket(elapsed);
    maxBucket = Math.max(maxBucket, bucket);
    minBucket = Math.min(maxBucket, bucket);
    buckets[bucket]++;
  }

  /**
   * Write this class out to the Writer in XML format
   * @param w output Writer
   **/
  public void toXML(XMLWriter w) throws IOException{
    w.optagln("response");
    w.optagln("results", "totalTime", totalTime);
    for (Iterator iter = new TreeSet(taskTimes.keySet()).iterator(); iter.hasNext();) {
      Object key = iter.next ();
      w.sitagln("task", 
		"id",   (String) key, 
		"time", (String) taskTimes.get(key));
    }
    w.cltagln("results");
    w.optagln("histogram",
              "minBucket", String.valueOf(minBucket),
              "maxBucket", String.valueOf(maxBucket));
    long bucketMin = 0L;
    for (int bucket = 0; bucket <= maxBucket; bucket++) {
      long bucketMax = pwr10[bucket / 3];
      switch (bucket % 3) {
      case 0: bucketMax *=  2L; break;
      case 1: bucketMax *=  5L; break;
      case 2: bucketMax *= 10L; break;
      }
      if (bucket >= minBucket) {
        w.sitagln("bucket",
                  "elapsedTime", bucketMin + ".." + bucketMax,
                  "count", String.valueOf(buckets[bucket]));
      }
      bucketMin = bucketMax;
    }
    w.cltagln("histogram");
    w.cltagln("response");
  }

  public String toString () {
    StringBuffer buf = new StringBuffer ();
    buf.append ("\n************************* Testing Summary *************************\n");
    buf.append ("         Task UID             Time \n");
    for (Iterator iter = taskTimes.keySet().iterator(); iter.hasNext();) {
      Object key = iter.next ();
      buf.append("          " + key + "                       " + (String) taskTimes.get(key) + "\n");
    }
    buf.append("\n         Total Time: " + totalTime);
    buf.append("\n Histogram");
    long bucketMin = 0L;
    for (int bucket = 0; bucket < NBUCKETS; bucket++) {
      long bucketMax = pwr10[bucket / 3];
      switch (bucket % 3) {
      case 0: bucketMax *=  2L; break;
      case 1: bucketMax *=  5L; break;
      case 2: bucketMax *= 10L; break;
      }
      buf.append("\n")
        .append(bucketMin)
        .append("..")
        .append(bucketMax)
        .append(':')
        .append(buckets[bucket]);
      bucketMin = bucketMax;
    }
    buf.append("\n*******************************************************************\n");

    return buf.toString();
  }

  public static void main(String[] args) {
    for (int i = 0; i < args.length; i++) {
      System.out.println(args[i] + ": " + getBucket(new Long(args[i]).longValue()));
    }
  }
}
