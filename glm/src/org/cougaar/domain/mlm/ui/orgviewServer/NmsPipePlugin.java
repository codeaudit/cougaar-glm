/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.orgviewServer;

import java.io.*;
import java.util.*;

public interface NmsPipePlugin {
  public void execute(InputStream is, OutputStream os, Object arg);
  public void control(String command);
}

