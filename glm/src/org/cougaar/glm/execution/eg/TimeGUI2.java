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

import javax.swing.*;

public class TimeGUI2 extends JLabel implements Runnable {
    String rateString;          // The current rate as a string
    double theRate;             // The current rate
    long lastNow;               // The start of this timing segment
    long theOffset;             // The offset
    Thread thread;

    public synchronized void setTime(long newTime, double newRate) {
      lastNow = System.currentTimeMillis();
      theRate = newRate;
      theOffset = newTime;
      if (newRate == 0.0) {
        rateString = " (Paused)";
      } else if (newRate != 1.0) {
        rateString = " (Rate " + newRate + ")";
      } else {
        rateString = " (Running)";
      }
      if (thread == null) {
        thread = new Thread(this, "TimeGUI");
        thread.start();
      }
      notify();
    }

    public synchronized void run() {
      while (true) {
        long now = System.currentTimeMillis();
        long executionTime = (long) ((now - lastNow) * theRate + theOffset);
        setText(new EGDate(executionTime).toString() + rateString);
        long nextExecutionTime = executionTime + 60000; // One minute later
        long nextNow = (long) (lastNow + (nextExecutionTime - theOffset) / theRate);
        long delay = nextNow - now;
        if (delay < 200) delay = 200;
        if (delay > 10000) delay = 10000;
        try {
          wait(delay);
        } catch (InterruptedException ie) {
        }
      }
    }
  }
