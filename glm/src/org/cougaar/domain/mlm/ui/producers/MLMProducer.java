/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.producers;

import java.util.ArrayList;
import java.util.List;

import org.cougaar.core.society.UID;

import org.cougaar.domain.mlm.ui.producers.ClusterCache;
import org.cougaar.domain.mlm.ui.views.MLMConsumer;
import org.cougaar.domain.mlm.ui.views.ConsumerList;

/**
 * MLMProducer is a lightweight abstract producer class.
 * Each instance of a producer typically gets data from a single
 * target cluster.  Each producer has its own query run against
 * the UIData PSP in the produceObjects method.  It may also have
 * a produceSingleObject method used to retrieve an object matching
 * a particular uuid.
 *
 * Communication with views is through a listener registration similar
 * to awt/swing.  Views spawn producers as needed, register as a listener
 * of the producer by implementing the MLMConsumer interface and calling
 * addConsumer, then call the producers's start method to get the data.
 *
 * 01/03/2000 - removed stop method.  Current implementation didn't work and
 * reimplementing seemed like over kill given the current use.
 *
 * @author  Nick Pioch, BBN Technologies
 * @version 1.0
 */

public abstract class MLMProducer implements java.io.Serializable {
            
  /** 
   * Name of the producer
   */
  protected String myName;
  
  /** 
   * Default query string to send to PSP
   */
  protected String myQuery;
  
  /**
   * The local store of data items
   */
  protected ArrayList myDataItems;
  
  /**
   * The list of MLMConsumers to fire new/changed/removed data to
   */
  protected ConsumerList myConsumers;
  
  // COUGAAR-specific fields
  protected String myTargetCluster = null;
  
  /**
   * Constructor for MLMProducer takes in a clusterID and adds
   * the producer with the producer cache for that cluster
   *
   * @param name String specifying the name of the producer
   * @param clusterName String specifying the name of the target cluster.
   */
  public MLMProducer(String name, String clusterName) {
    myName = name;
    myTargetCluster = clusterName;
    myDataItems = new ArrayList();
    myConsumers = new ConsumerList();
    // Register this producer with the cluster cache
    ClusterCache.addProducerForCluster(myTargetCluster,this);
  }
  
  /** 
   * Register a new consumer with this producer.
   * Consumer immediately updated with all current data.
   *
   * @param consumer MLMConsumer to add.
   */
  public void addConsumer(MLMConsumer consumer) {
    myConsumers.add(consumer);
    synchronized(myDataItems) {
      if (myDataItems != null) {
        ConsumerList consumerList = new ConsumerList();
        consumerList.add(consumer);
        updateConsumers();
      }
    }
  }

    
  /**
   * Starts the producer in current thread. 
   * See ThreadedMLMProducer for producer that runs in its own thread.
   */
  public void start() {
    updateDataItems();
  }
  
  /** 
   * Go out to clusters again to refresh data. Runs in same thread 
   */
  public void refresh() {
    updateDataItems();
  }
  
  /**
   * Should describe this producer and its target cluster
   */
  public String toString() {
    return myName + " for " + myTargetCluster;
  }

  /**
   * Sends data update to all consumers
   */
  protected void updateConsumers() {
    myConsumers.fireDataUpdate(myDataItems.toArray(), this);
  }

  /**
   * sends error report to all consumers
   *
   * @param text String with error message
   */
  protected void reportErrorToConsumers(String text) {
    myConsumers.fireErrorReport(text, this);
  }


  /**
   * Updates dataItems and fires data update
   */
  protected void updateDataItems() {
    Object []newData = produceObjects();

    synchronized(myDataItems) {
      myDataItems.clear();

      for (int i = 0; i < newData.length; i++) {
        myDataItems.add(newData[i]);
      }

      updateConsumers();
    }
  }

  /**
   * This is where the producer gets the COUGAAR objects 
   *
   * @return Object [] - array of objects returned by the cluster
   */
  protected abstract Object []produceObjects();
}







