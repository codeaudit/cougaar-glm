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

package org.cougaar.mlm.plugin;

import java.awt.Component;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.planning.plugin.legacy.PluginDelegate;
import org.cougaar.util.MinMaxPanel;
import org.cougaar.util.UnaryPredicate;

public class RandomButtonPusher implements java.io.Serializable {

  private static final Random random = new Random();

  private boolean enabled;

  private int nPushes = 0;

  private int remainingTime = 0;

  private int totalTime = 1;

  private transient int accumulatedDelta = 0;

  private int minSleepTime;

  private int maxSleepTime;

  private transient String buttonLabel;

  private transient JButton theButtonToPush;

  private transient MinMaxPanel theMinMaxPanel;

  private transient Thread sleepThread;

  private transient Runnable clicker;

  private transient PluginDelegate plugInDelegate;

  protected RandomButtonPusher(int minSleepTime, int maxSleepTime, boolean initialState) {
    this.minSleepTime = minSleepTime;
    this.maxSleepTime = maxSleepTime;
    enabled = initialState;
  }

  public static IncrementalSubscription subscribe(PluginDelegate pid, Class randomButtonPusherClass) {
    final String randomButtonPusherClassName = randomButtonPusherClass.getName();
    UnaryPredicate predicate = new UnaryPredicate() {
      public boolean execute(Object o) {
        return randomButtonPusherClassName.equals(o.getClass().getName());
      }
    };
    return (IncrementalSubscription) pid.subscribe(predicate);
  }

  public Component init(String label, PluginDelegate delegate, JButton aButtonToPush) {
    buttonLabel = label;
    plugInDelegate = delegate;
    theButtonToPush = aButtonToPush;
    theMinMaxPanel = new MinMaxPanel() {
      protected void newMin(int min) {
        minSleepTime = min * 1000;
      }
      protected void newMax(int max) {
        maxSleepTime = max * 1000;
      }
      protected void newEnable(boolean newEnabled) {
        enabled = newEnabled;
	RandomButtonPusher.this.setEnabled(enabled);
        stopTimer();
	if (enabled) {
	  startTimer();
	}
      }
    };
    theMinMaxPanel.setText(buttonLabel);
    theMinMaxPanel.setColumns(3);
    theMinMaxPanel.setEnabled(enabled);
    theMinMaxPanel.setMin(minSleepTime / 1000);
    theMinMaxPanel.setMax(maxSleepTime / 1000);
    if (enabled) {
      startTimer();
    }
    return theMinMaxPanel;
  }

  private void publishChange() {
    plugInDelegate.openTransaction();
    plugInDelegate.publishChange(this);
    plugInDelegate.closeTransactionDontReset();
  }

  private void setEnabled(boolean newEnabled) {
    if (newEnabled != enabled) {
      enabled = newEnabled;
      publishChange();
    }
  }

  private synchronized void setTotalTime(int howLong) {
    totalTime = howLong;
    theMinMaxPanel.setProgressMax(totalTime/1000);
  }

  private synchronized void setRemainingTime(int howLong) {
    remainingTime = howLong;
    accumulatedDelta = 0;
    publishChange();
    setCheckBoxText();
  }

  private synchronized int getRemainingTime() {
    return remainingTime - accumulatedDelta;
  }

  private synchronized boolean decreaseRemainingTime(int delta) {
    accumulatedDelta += delta;
    if (getRemainingTime() <= 0) {
      setRemainingTime(0);
      return true;
    }
    if (accumulatedDelta > 10000) {
      setRemainingTime(getRemainingTime());
    } else {
      setCheckBoxText();
    }
    return false;
  }

  private void incrementNPushes() {
    nPushes++;
    publishChange();
    setCheckBoxText();
  }

  private void setCheckBoxText() {
    theMinMaxPanel.setProgressValue((totalTime - getRemainingTime()) / (1.0f * totalTime));
    theMinMaxPanel.setText(buttonLabel + " " + (nPushes + 1));
  }

  private Runnable getClicker() {
    if (clicker == null) {
      clicker = new Runnable() {
        public void run() {
          theButtonToPush.doClick();
          incrementNPushes();
        }
      };
    }
    return clicker;
  }

  private void startTimer() {
    sleepThread = new Thread(buttonLabel) {
      public void run() {
	try {
	  while (true) {
            if (getRemainingTime() == 0) {
              int time = minSleepTime + random.nextInt(maxSleepTime - minSleepTime);
              setTotalTime(time);
              setRemainingTime(time);
            }
            int sleepTime = getRemainingTime() % 1000;
            if (sleepTime == 0) {
              sleepTime = 1000;
            }
            sleep(sleepTime);
            if (decreaseRemainingTime(sleepTime)) {
              SwingUtilities.invokeLater(getClicker());
            }
          }
        }
	catch (InterruptedException e) {
	}
      }
    };
    sleepThread.start();
  }

  private synchronized void stopTimer() {
    if (sleepThread != null) {
      sleepThread.interrupt();
      sleepThread = null;
    }
    setRemainingTime(0);
  }
}
