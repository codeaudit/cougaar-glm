/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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
package org.cougaar.glm.execution.cluster;

import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.plugin.legacy.PluginDelegate;
import org.cougaar.lib.planserver.HttpInput;
import org.cougaar.lib.planserver.PSP_BaseAdapter;
import org.cougaar.lib.planserver.PlanServiceContext;
import org.cougaar.lib.planserver.PlanServiceUtilities;
import org.cougaar.lib.planserver.RuntimePSPException;
import org.cougaar.lib.planserver.ServerPluginSupport;
import org.cougaar.planning.ldm.ClusterServesPlugin;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.cougaar.glm.execution.common.*;

/**
 * Receives clock control messages from the EventGenerator and sets
 * the execution clock accordingly. Acknowledgement is performed
 * implicitly by the ConnectionAcknowledgement.
 **/
public abstract class PSP_Base extends PSP_BaseAdapter
{
  protected static abstract class Context {
    protected PrintStream out;
    protected LineReader reader;
    protected LineWriter writer;
    protected ServerPluginSupport sps;
    protected PlanServiceContext psc;
    protected PlanServiceUtilities psu;
    protected PluginDelegate delegate;
    protected ClusterServesPlugin cluster;
    protected PlanningFactory factory;
    protected abstract void execute() throws InterruptedException, IOException;
  }

  protected PSP_Base() throws RuntimePSPException {
    super();
  }

  protected PSP_Base(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  /** The simple execute() implemented in subclasses **/
  protected abstract Context createContext();

  public void execute(PrintStream out,
                      HttpInput params,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu)
  {
    try {
      Context context = createContext();
      context.out = out;
      context.reader = new CharArrayLineReader(params.getBodyAsCharArray());
      context.writer = new OutputStreamLineWriter(out);
      context.psc = psc;
      context.psu = psu;
      context.sps = psc.getServerPluginSupport();
      context.delegate = context.sps.getDirectDelegate();
      context.cluster = context.delegate.getCluster();
      context.factory = context.sps.getFactoryForPSP();
      context.execute();
      context.writer.flush();
      context.out.flush();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return;                   // Connection closed
    } catch (RuntimeException re) {
      re.printStackTrace();
      return;
    } catch (InterruptedException ie) {
      return;                   // Terminated
    }
  }

  /**
   * This PSP is referenced directly (in the URL from the client) and
   * hence this shouldn't be called.
  **/
  public boolean test(HttpInput query_parameters, PlanServiceContext psc) {
    super.initializeTest();
    return false;
  }

  public String getDTD() {
    return null;
  }

  public boolean returnsHTML() {
    return false;
  }

  public boolean returnsXML() {
    return false;
  }
}
