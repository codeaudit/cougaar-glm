/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.views;

import org.cougaar.domain.mlm.ui.producers.MLMProducer;

public interface MLMConsumer {

  // Simplified to only support bulk update.
  public void fireDataUpdate(Object []updateData, Object source);

  public void fireErrorReport(String errorText, Object source);
}
