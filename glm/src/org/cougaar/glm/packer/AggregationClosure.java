// Copyright (12/99) Honeywell Inc.
// Unpublished - All rights reserved. This software was developed with funding 
// under U.S. government contract MDA972-97-C-0800

package org.cougaar.glm.packer;

import org.cougaar.planning.ldm.plan.NewMPTask;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.core.domain.RootFactory;

/**
  * This class is used to wrap up an aggregation's state into an
  * object that is able to generate new MPTasks on demand.  These
  * MPTasks will be used as the "right-hand-side" of Aggregations
  * constructed by the GenericPlugin.<br>
  * A standard strategy for writing AggregationClosures is to provide
  * a constructor that sets a number of instance variables. Then the newTask method
  * will use these instance variables to appropriately construct the MPTask it 
  * returns.<br>
  * The name of this class is a trifle unfortunate --- it would be
  * better if it and the GenericTemplate were renamed to MPTaskTemplate
  * and WorkflowTemplate, respectively, but this has not been done in the
  * interests of backward-compatibility.
  * @see GenericPlugin
  */
public abstract class AggregationClosure {
  protected GenericPlugin _gp = null;
  protected RootFactory _factory = null;

  /**
    * This method will be called by aggregation and packing scripts of
    * the scripting Plugin.  It is guaranteed to be called before the
    * newTask method is called, so that writers of newTask methods may
    * feel free to use the variables _gp and _factory, that point to the
    * GenericPlugin and its RootFactory, respectively.
    * @see #newTask
    */
  public void setGenericPlugin (GenericPlugin gp) {
    _gp = gp;
    _factory = _gp.getGPFactory();
  }
  
  /**
    * Developers of aggregation and packing rules should supply an
    * AggregationClosure subclass that provides an instantiation of this
    * method.  This method should return a NewMPTask, but need not publish
    * it (this will be done by the GenericPlugin) or set its Preferences
    * (this is the job of the PreferenceAggregator.
    * @see PreferenceAggregator
    */
  public abstract NewMPTask newTask();

  /**
   * getQuantity - return the amount this container can hold
   */
  public abstract double getQuantity();

  /**
   * return true if task is valid for this AggregationClosure.
   */
  public abstract boolean validTask(Task task);
}





