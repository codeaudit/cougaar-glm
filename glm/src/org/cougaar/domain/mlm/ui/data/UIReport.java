/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import java.util.Date;

public interface UIReport {
   
  /**
   * getInfoText - Informational text
   *
   * @return String informational message
   **/
  String getText();

  /**
   * getDate - Date object was injected into the system
   *
   * @return Date creation date for message
   **/
  Date getDate();
}
  



