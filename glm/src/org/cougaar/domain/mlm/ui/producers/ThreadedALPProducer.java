/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.producers;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.cougaar.domain.mlm.ui.views.ConsumerList;

/**
 *
 * ThreadedALPProducer is exactly the same as ALPProducer except that it
 * produces objects in its own thread.  start & refresh methods implemented by
 * running ALPProducer version of the method in a separate thread.
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

public abstract class ThreadedALPProducer extends ALPProducer {

  /**
   * Constructor for ALPProducer takes in a clusterID and adds
   * the producer with the producer cache for that cluster
   */
  public ThreadedALPProducer(String name, String clusterName) {
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
   * doStart - calls ALPProducer implemention of start. Called from within
   * StartThread.run().
   */
  protected void doStart() {
    super.start();
  }

  /**
   * doRefresh - calls ALPProducer implemention of refresh. Called from within
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
                                                   new ALPProducerError(text)));
  }

  private class UpdateConsumers implements Runnable {

    private boolean myProducerError = false;
    private ALPProducerError myError = null;
    private Object []myUpdateData = null;
    private ConsumerList myConsumers;
    private ALPProducer myProducer;
    
    public UpdateConsumers(ALPProducer producer, 
                           ConsumerList consumers,
                           Object updateData) {
      myProducer = producer;
      myConsumers = (ConsumerList)consumers.clone();

      if (updateData instanceof ALPProducerError) {
        myProducerError = true; 
        myError = (ALPProducerError)updateData;
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
  protected class ALPProducerError {
    private String myText;

    public ALPProducerError(String text) {
      myText = (text);
    }

    public String getText() {
      return myText;
    }
  }
}












