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
