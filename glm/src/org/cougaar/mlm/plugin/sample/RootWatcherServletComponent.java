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

package org.cougaar.mlm.plugin.sample;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.ServletService;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.UnaryPredicate;


/**
 * The GLSInitServlet crams all of the functionality of the GLSGUIInitPlugin, 
 * GLSGUIRescindPlugin, and SQLOplanPlugin sans GUIs into one plugin
 * The buttons are now in a client application which talks to the
 * servlets in this plugin to publish the oplan and gls tasks
 *
 **/
public class RootWatcherServletComponent extends ComponentPlugin {

  private IncrementalSubscription selfOrgSubscription;
  private IncrementalSubscription glsSub;
  private Record[] records = new Record[20];
  private int record0 = 0;
  private int nrecords = 0;
  private long minTimestamp = System.currentTimeMillis();

  private static final String forRoot = "ForRoot".intern();
  
  protected static DateFormat logTimeFormat =
    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

  protected static String formatDate(long when) {
    return logTimeFormat.format(new Date(when));
  }

  private static DecimalFormat confidenceFormat = new DecimalFormat("##0%");

  private static UnaryPredicate selfOrgPredicate =
    new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Organization) {
          Organization org = (Organization) o;
          return org.isSelf();
        }
        return false;
      }
    };

  private static class Record {
    private long timestamp;
    private double confidence;
    public Record(long ts, double c) {
      timestamp = ts;
      confidence = c;
    }
    public String getTimestampString() {
      return formatDate(timestamp);
    }

    public String getConfidenceString() {
      return confidenceFormat.format(confidence);
    }
    public double getConfidence() {
      return confidence;
    }
    public long getTimestamp() {
      return timestamp;
    }

    public String toString() {
      return getTimestampString() + ": " + getConfidenceString();
    }
  }

  /**
   * This predicate selects for root tasks injected by the GLSGUIInitPlugin
   **/
  private static class GLSPredicate implements UnaryPredicate {
    MessageAddress agentId;
    Organization self;

    public GLSPredicate(Organization self, MessageAddress agentId) {
      this.self = self;
      this.agentId = agentId;
    }
    public boolean execute(Object o) {
	if (!(o instanceof PlanElement)) return false;
        PlanElement pe = (PlanElement) o;
	Task task = pe.getTask();
	if (!task.getVerb().equals(Constants.Verb.GETLOGSUPPORT)) return false;
	if (!task.getSource().equals(agentId)) return false;
	if (!task.getDestination().equals(agentId)) return false;
        PrepositionalPhrase pp = task.getPrepositionalPhrase(Constants.Preposition.FOR);
        if (pp == null || !self.equals(pp.getIndirectObject())) return false;
	if (task.getPrepositionalPhrase(forRoot) == null) return false;
        return true;
      }
    };

  private ServletService servletService;
  /** Sets the servlet service. Called by introspection on start
   **/
  public void setServletService(ServletService ss) {
    servletService = ss;
  }

  private LoggingService logger;
  public void setLoggingService(LoggingService ls) {
    logger = ls;
  }

  /*
   * Creates a subscription.
   */
  protected void setupSubscriptions() {
    blackboard.getSubscriber().setShouldBePersisted(false);
    selfOrgSubscription = (IncrementalSubscription) blackboard.subscribe(selfOrgPredicate);
    checkSelfOrg(selfOrgSubscription);

    // register with servlet service
    try {
      servletService.register("/rootWatcher", new RootWatcherServlet());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }	   		 


  /**
   * Executes Plugin functionality.
   */
  public void execute() {
    if (selfOrgSubscription != null && selfOrgSubscription.hasChanged()) {
      checkSelfOrg(selfOrgSubscription.getAddedCollection());
      // Don't worry about changes or deletes
    }
    if (glsSub != null && glsSub.hasChanged()) {
      recordGLS(glsSub.getAddedCollection());
      recordGLS(glsSub.getChangedCollection());
    }
  }

  private void checkSelfOrg(Collection selfOrgs) {
    for (Iterator i = selfOrgs.iterator(); i.hasNext(); ) {
      Organization self = (Organization) i.next();
      logger.info("self org found");
      UnaryPredicate pred = new GLSPredicate(self, getMessageAddress());
      glsSub = (IncrementalSubscription) blackboard.subscribe(pred);
      blackboard.unsubscribe(selfOrgSubscription);
      selfOrgSubscription = null;
      recordGLS(glsSub);
      break;
    }
  }

  private void recordGLS(Collection glsRoots) {
    for (Iterator i = glsRoots.iterator(); i.hasNext(); ) {
      PlanElement pe = (PlanElement) i.next();
      Collection changes = glsSub.getChangeReports(pe);
      boolean doit = changes == null || changes.isEmpty();
      if (!doit) {
        for (Iterator i2 = changes.iterator(); i2.hasNext(); ) {
          if (i2.next() instanceof PlanElement.EstimatedResultChangeReport) {
            doit = true;
            break;
          }
        }
      }
      if (doit) {
        AllocationResult ar = pe.getEstimatedResult();
        double confidence = ar.getConfidenceRating();
        Record record = new Record(System.currentTimeMillis(), confidence);
        logger.info("Adding record " + record);
        addRecord(record);
      }
    }
  }

  private synchronized void addRecord(Record newRecord) {
    records[(record0 + nrecords) % records.length] = newRecord;
    if (nrecords == records.length) {
      record0++;
    } else {
      nrecords++;
    }
  }

  private synchronized void setRecordsSize(int newSize) {
    if (newSize > 0 && newSize < 10000) {
      records = getRecords(newSize);
      record0 = 0;
      nrecords = Math.min(nrecords, newSize);
    }
  }

  private Record[] getRecords(int newSize) {
    int newOffset = 0;
    int oldOffset = record0;
    int oldSize = nrecords;
    if (newSize < oldSize) {
      int diff = oldSize - newSize;
      oldOffset += diff;
      oldSize -= diff;
    }
    Record[] newRecords = new Record[newSize];
    while (oldSize > 0) {
      int fragSize = records.length - oldOffset;
      int size = Math.min(fragSize, oldSize);
      System.arraycopy(records, oldOffset, newRecords, newOffset, size);
      newOffset += size;
      newSize -= size;
      oldOffset = (oldOffset + size) % records.length;
      oldSize -= size;
    }
    return newRecords;
  }

  private synchronized Record[] getRecords() {
    return getRecords(nrecords);
  }

  static String[] mimeTypes = {
    "image/gif",
    "image/png",
    "image/jpeg",
  };

  private void getGraph(HttpServletResponse response) throws IOException {
    ImageWriter iw = null;
    outer:
    for (int j = 0; j < mimeTypes.length; j++) {
      String mimeType = mimeTypes[j];
      for (Iterator i = ImageIO.getImageWritersByMIMEType(mimeType); i.hasNext(); ) {
        iw = (ImageWriter) i.next();
        iw.setOutput(new MemoryCacheImageOutputStream(response.getOutputStream()));
        response.setContentType(mimeType);
        break outer;
      }
    }
    if (iw == null) throw new IOException("No ImageWriter found");
    int width = 600;
    int height = 100;
    int M = 10;
    BufferedImage img = new BufferedImage(width + 2*M, height + 2*M, BufferedImage.TYPE_INT_RGB);
    Graphics g = img.createGraphics();
    long maxTimestamp = System.currentTimeMillis();
    Record[] records = getRecords();
    int xp = 0;
    int yp = 0;
    g.setColor(Color.lightGray);
    g.fillRect(0, 0, 10000, 10000);
    g.setColor(Color.black);
    g.drawRect(0, 0, width + 2*M - 1, height + 2*M - 1);
    g.setColor(Color.white);
    g.drawLine(M, M + height - 0, M + width, M + height - 0);
    for (long t = 0; t + minTimestamp <= maxTimestamp; t += 60000) {
      long ts = t + minTimestamp;
      int x = M + (int) ((ts - minTimestamp) * width / (maxTimestamp - minTimestamp));
      g.drawLine(x, M + height - 0, x, M);
    }
    for (int i = 0; i <= records.length; i++) {
      Record record;
      long ts;
      double c;
      int x;
      int y;
      if (i == records.length) {
        ts = maxTimestamp;
        c = records[i - 1].getConfidence();
      } else {
        record = records[i];
        ts = record.getTimestamp();
        c = record.getConfidence();
      }
      x = M + (int) ((ts - minTimestamp) * width / (maxTimestamp - minTimestamp));
      y = M + height - (int) (c * height);
      if (i > 0) {
        g.setColor(Color.black);
        g.drawLine(xp, yp, x, y);
      }
      if (i < records.length) {
        if (c < 0.9) {
          g.setColor(Color.red);
        } else if (c < 0.99) {
          g.setColor(Color.yellow);
        } else {
          g.setColor(Color.green);
        }
        g.fillOval(x - 3, y - 3, 6, 6);
        xp = x;
        yp = y;
      }
    }
    iw.write(img);
  }

  private class RootWatcherServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
      try {
        setRecordsSize(Integer.parseInt(request.getParameter("nrecords")));
      } catch (Exception e) {
      }
      String command = request.getParameter("command");
      if ("graph".equals(command)) {
        try {
          getGraph(response);
        } catch (IOException ioe) {
          throw new RuntimeException(ioe.toString());
        }
        return;
      }
      response.setContentType("text/html");
      PrintWriter out;
      try {
        out = response.getWriter();
      } catch (IOException ioe) {
        return;                 // Quietly ignore the exception (probably closed by client)
      }
      String title = "History of GLS Root Task Confidence";
      out.println("<html>");
      out.println(" <head>");
      out.println(" </head>");
      out.println("  <title>");
      out.println(title);
      out.println("  </title>");
      out.println(" <body>");
      out.println("<H1>");
      out.println(title);
      out.println("</H1>");
      out.println("<H2>");
      out.println(nrecords + " records available");
      out.println("</H2>");
      out.println("  <img src=\"?command=graph\">");
      out.println("  <table border=1>");
      Record[] records = getRecords();
      for (int i = 0; i < records.length; i++) {
        Record record = records[i];
        out.println("   <tr><td>"
                    + record.getTimestampString()
                    + "</td><td align=\"right\">"
                    + record.getConfidenceString()
                    + "</td></tr>");
      }
      out.println("  </table>");
      out.println(" </body>");
      out.println("</html>");
    }
  }
}
