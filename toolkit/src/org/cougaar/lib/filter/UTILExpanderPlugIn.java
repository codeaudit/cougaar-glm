/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.filter;

import org.cougaar.domain.planning.ldm.plan.Task;

import java.util.Vector;

import org.cougaar.lib.callback.UTILExpansionListener;
import org.cougaar.lib.callback.UTILGenericListener;

/**
 * An expander just listens for "generic" events and then
 * requires expandable tasks to be expanded with getSubtasks.
 *
 * Note that it is the filter callback which will feed it expandable 
 * tasks. (E.g. UTILExpandableTaskCallback).
 */

public interface UTILExpanderPlugIn extends 
  UTILGenericListener, UTILExpansionListener {

  /** 
   * Where actual expansion should occur.
   *
   * @return Vector of subtasks 
   */
  Vector getSubtasks(Task t);
}                
        
        
