/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.glm.ldm.oplan;
import java.util.Vector;
import java.util.Enumeration;
import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.core.society.UID;

/**
 * Unify a bunch of getters that oplan sub-objects (hence "contributor"s)
 * support.
 * 
 * @author  ALPINE <alpine-software@bbn.com>
 * @version $Id: OplanContributor.java,v 1.2 2001-04-05 19:27:37 mthome Exp $
 */

public interface OplanContributor extends Transferable {
  /** Gets the time span in force. **/
  public TimeSpan getTimeSpan();

  /** Gets the time span in force. **/
  public void setTimeSpan(TimeSpan span);
  
  /** @deprecated Use getOplanUID*/
  public UID getOplanID();

  /** Which oplan are we connected to? **/
  public UID getOplanUID();

}

