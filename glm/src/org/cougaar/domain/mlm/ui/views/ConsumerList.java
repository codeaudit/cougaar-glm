/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.views;

import java.util.ArrayList;
import java.util.List;

/**
 * A ConsumerList is simply a container for ALPConsumers,
 * which itself implements ALPConsumer in that it
 * has methods for firing updates/errors to all consumers in the list.
 *
 * @author  Nick Pioch, BBN Technologies
 * @version 1.0
 */

public class ConsumerList implements ALPConsumer, Cloneable {
  
  private ArrayList consumers;

  public ConsumerList() {
    consumers = new ArrayList();
  }

  public void add(ALPConsumer consumer) {
    consumers.add(consumer);
  }

  public synchronized Object clone() {
    try {
      ConsumerList clone = (ConsumerList)super.clone();
      clone.consumers = (ArrayList)consumers.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      //This shouldn't happen since we implement Cloneable
      throw new InternalError();
    }
  }                  

  public void fireDataUpdate(Object []updateData, Object parent) {
    for (int i=0; i<consumers.size(); i++) {
      ((ALPConsumer)consumers.get(i)).fireDataUpdate(updateData, parent);
    }
  }

  public void fireErrorReport(String errorText, Object parent) {
    for (int i=0; i<consumers.size(); i++) {
      ((ALPConsumer)consumers.get(i)).fireErrorReport(errorText, parent);
    }
  }

}




