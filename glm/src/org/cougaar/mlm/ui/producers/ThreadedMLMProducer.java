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
 
package org.cougaar.mlm.ui.producers;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.cougaar.mlm.ui.views.ConsumerList;

/**
 *
 * ThreadedMLMProducer is exactly the same as MLMProducer except that it
 * produces objects in its own thread.  start & refresh methods implemented by
 * running MLMProducer version of the method in a separate thread.
 *
 * subclasses responsible for implementing produceObjects
 *
 * 01/03/2000 - removed stop method.  Current implementation didn't work and
 * reimplementing seemed like over kill given the current use.
 *
 * @author  Nick Pioch, BBN Technologies
 * @version 1.0
 *
 *
 */

public abstract class ThreadedMLMProducer extends MLMProducer {

  /**
   * Constructor for MLMProducer takes in a clusterID and adds
   * the producer with the producer cache for that cluster
   */
  public ThreadedMLMProducer(String name, String clusterName) {
    super(name, clusterName);
  }

  /**
   * Starts the producer in a new thread. 
   */
  public void start() {
    Thread startThread = new Thread(toString() + " - Start") {
      public void run() {
        doStart();
      }
    };
    startThread.start();
  }

  /**
   * Refreshes the data in a new thread. 
   */
  public void refresh() {
    Thread refreshThread = new Thread(toString() + " - Refresh") {
      public void run() {
        doRefresh();
      }
    };
    refreshThread.start();
  }

  /**
   * doStart - calls MLMProducer implemention of start. Called from within
   * StartThread.run().
   */
  protected void doStart() {
    super.start();
  }

  /**
   * doRefresh - calls MLMProducer implemention of refresh. Called from within
   * RefreshThread.run().
   */
  protected void doRefresh() {
    super.refresh();
  }

  /**
   * Sends data update to all consumers - assumes asynch update so uses
   * SwingUtilities.invokeLater to insert update in the swing event queue.
   */
  protected void updateConsumers() {
    SwingUtilities.invokeLater(new UpdateConsumers(this,
                                                   myConsumers,
                                                   myDataItems));
  }

  /**
   * sends error report to all consumers - assumes asynch report so uses
   * SwingUtilities.invokeLater to insert error report in the swing event 
   * queue.
   *
   * @param text String with error message
   */
  protected void reportErrorToConsumers(String text) {
    SwingUtilities.invokeLater(new UpdateConsumers(this,
                                                   myConsumers,
                                                   new MLMProducerError(text)));
  }

  private class UpdateConsumers implements Runnable {

    private boolean myProducerError = false;
    private MLMProducerError myError = null;
    private Object []myUpdateData = null;
    private ConsumerList myConsumers;
    private MLMProducer myProducer;
    
    public UpdateConsumers(MLMProducer producer, 
                           ConsumerList consumers,
                           Object updateData) {
      myProducer = producer;
      myConsumers = (ConsumerList)consumers.clone();

      if (updateData instanceof MLMProducerError) {
        myProducerError = true; 
        myError = (MLMProducerError)updateData;
      } else {
        myUpdateData = ((ArrayList)updateData).toArray();
      }
    }
    
    public void run() {
      if (myProducerError) {
        myConsumers.fireErrorReport(myError.getText(), myProducer);
      } else {
        myConsumers.fireDataUpdate(myUpdateData, myProducer);
      }
    }
  }

  //Convenience class so UpdateConsumer thread knows what kind of info
  // it's sending.
  protected class MLMProducerError {
    private String myText;

    public MLMProducerError(String text) {
      myText = (text);
    }

    public String getText() {
      return myText;
    }
  }
}












