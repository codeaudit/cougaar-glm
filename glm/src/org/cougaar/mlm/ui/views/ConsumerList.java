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
 
package org.cougaar.mlm.ui.views;

import java.util.ArrayList;
import java.util.List;

/**
 * A ConsumerList is simply a container for MLMConsumers,
 * which itself implements MLMConsumer in that it
 * has methods for firing updates/errors to all consumers in the list.
 *
 * @author  Nick Pioch, BBN Technologies
 * @version 1.0
 */

public class ConsumerList implements MLMConsumer, Cloneable {
  
  private ArrayList consumers;

  public ConsumerList() {
    consumers = new ArrayList();
  }

  public void add(MLMConsumer consumer) {
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
      ((MLMConsumer)consumers.get(i)).fireDataUpdate(updateData, parent);
    }
  }

  public void fireErrorReport(String errorText, Object parent) {
    for (int i=0; i<consumers.size(); i++) {
      ((MLMConsumer)consumers.get(i)).fireErrorReport(errorText, parent);
    }
  }

}




