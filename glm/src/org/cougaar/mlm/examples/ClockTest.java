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

package org.cougaar.mlm.examples;

import java.util.Date;
import java.util.Vector;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.ClusterServesPlugin;
import org.cougaar.planning.plugin.legacy.SimplePlugin;


/** 
 * this plugin is a point-test for (demo-time) clock advance functionality.
 * It can do two things:
 *  1. when it is passed an argument, it is a clock setter, advancing the
 *  COUGAAR time by 10 minutes every 10 (realtime) seconds.
 *  2. otherwise it sets wake timers 30 minutes in advance of the current
 *  time each time it is waked, reporting on the current time each time 
 *  around.
 *
 * So, it shows examples of:
 *  1. how to set the society time (be careful!)
 *  2. how to look at the society time.
 *  3. how to request that your plugin be activated approximately at a
 *    specific scenario time (or after scenario time has elapsed).
 *  4. how to code a timer based on real (system) time.
 *  
 * to try it out:
 * One cluster ini file should have the following line (for the advancer)
 *   plugin = org.cougaar.mlm.examples.ClockTest(advance)
 * another cluster should have (for the watcher):
 *   plugin = org.cougaar.mlm.examples.ClockTest
 **/

public class ClockTest extends SimplePlugin
{
  ClusterServesPlugin cluster = null;
  MessageAddress cid = null;
  
  public ClockTest() {
    setThreadingChoice(SINGLE_THREAD);
  }

  // "initialization" method
  public void setupSubscriptions() {
    cluster = getCluster();
    cid = getMessageAddress();
    
    System.err.println("ClockTest at "+cid+" initializing.");
    // see if we are a supposed to be a setter
    Vector argv = getParameters();
    if (argv != null && argv.size() > 0) {
      startAdvancer();
    } else {
      // set up a waker
      nextWake();
    }

    // don't actually set up any subscriptions
  }

  public void execute() {
    System.err.println("ClockTest at "+cid+" executing at "+
                       new Date(currentTimeMillis()));
    nextWake();
  }

  private void nextWake() {
    long ms = currentTimeMillis()+(30*60*1000); // wake in 30 minutes

    System.err.println("ClockTest at "+cid+" will wake at "+
                       new Date(ms));
    wakeAt(ms);
  }

  private void startAdvancer() {
    System.err.println("ClockTest at "+cid+" starting advancer");
    
    Thread t = new Thread(new Advancer());
    t.start();
  }

  private class Advancer implements Runnable {
    public void run() {
      while (true) {

        // sleep for 10 seconds
        try {
          synchronized (this) {
            this.wait(10 * 1000);
          }
        } catch (InterruptedException ie) {}
        
        // advance the clock by 10 minutes

        // Note - we can do this directly (without a transaction)
        // because time is not LDM controlled.
        System.err.println("ClockTest at "+cid+" Advancing to "+
                           new Date(currentTimeMillis()+10*60*1000)
                           );
        cluster.advanceTime(10 * 60 * 1000);

        // if we were using this loop to *activate* the plugin
        // every 10 (realtime) seconds, we'd do something like:
        // plugin.wake();
      }
    }
  }
}

