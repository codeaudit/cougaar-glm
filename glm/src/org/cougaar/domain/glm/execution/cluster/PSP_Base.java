package org.cougaar.domain.glm.execution.cluster;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.core.plugin.PlugInDelegate;
import org.cougaar.lib.planserver.HttpInput;
import org.cougaar.lib.planserver.PSP_BaseAdapter;
import org.cougaar.lib.planserver.PlanServiceContext;
import org.cougaar.lib.planserver.PlanServiceUtilities;
import org.cougaar.lib.planserver.RuntimePSPException;
import org.cougaar.lib.planserver.ServerPlugInSupport;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.cougaar.domain.glm.execution.common.*;

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
    protected ServerPlugInSupport sps;
    protected PlanServiceContext psc;
    protected PlanServiceUtilities psu;
    protected PlugInDelegate delegate;
    protected ClusterServesPlugIn cluster;
    protected RootFactory factory;
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
      context.sps = psc.getServerPlugInSupport();
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
