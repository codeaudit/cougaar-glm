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
