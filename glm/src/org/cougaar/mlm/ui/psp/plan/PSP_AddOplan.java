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
 
package org.cougaar.mlm.ui.psp.plan;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.cougaar.lib.planserver.*;
import org.cougaar.core.plugin.PluginDelegate;

import org.cougaar.mlm.plugin.ldm.OplanFileReader;
import org.cougaar.glm.ldm.oplan.*;

/**
 * PSP to add an <code>Oplan</code> to the LogPlan.
 *
 * @see #PROP_NAME
 */
public class PSP_AddOplan 
    extends PSP_BaseAdapter 
    implements PlanServiceProvider, UISubscriber
{

  /**
   * Set the System Property with this name to provide a drop-down list
   * of Oplan files in the HTML form.
   * <pre>
   * Format is:
   *   filename ('+' filename)*
   * For example:
   *   not-set  ---  allow the user to enter a filename
   *   ""  ---  allow the user to enter a filename
   *   "a.xml"  ---  user can only select "a.xml"
   *   "a.xml+b.xml"   ---  user can select "a.xml" or "b.xml"
   *   "a.xml+b.xml+c.xml"   ---  user can select "a.xml" or "b.xml" or "c.xml"
   *   etc.
   * </pre>
   */
  public static final String PROP_NAME = 
    "org.cougaar.mlm.ui.psp.plan.addOplan.files";

  /**
   * @see #PROP_NAME
   */
  private static List selectFilenames = null;
  static {
    // parse property, using format defined in "PROP_NAME" docs above
    String prop = System.getProperty(PROP_NAME);
    if (prop != null) {
      prop = prop.trim();
      int n = prop.length();
      if (n > 0) {
        selectFilenames = new ArrayList();
        int i = 0;
        while (true) {
          String s;
          int j = prop.indexOf('+', i);
          if (j < 0) {
            j = n;
          }
          s = prop.substring(i, j);
          s = s.trim();
          if ((s.length() > 0) &&
              (!(selectFilenames.contains(s)))) {
            selectFilenames.add(s);
          }
          i = j+1;
          if (i >= n) {
            break;
          }
        }
      }
    }
  }

  public void execute(PrintStream out,
      HttpInput query_parameters,
      PlanServiceContext psc,
      PlanServiceUtilities psu) throws Exception
  {
    MyPSPState myState = new MyPSPState(this, query_parameters, psc);
    myState.configure(query_parameters);

    if (myState.filename == null) {
      displayForm(myState, out);
    } else {
      publishOplan(myState, out, myState.filename);
    }
  }

  private static final void displayForm(
      final MyPSPState myState, 
      final PrintStream out) {
    out.print(
        "<html>\n"+
        "<head>\n"+
        "<title>"+
        "Add Oplan to ");
    out.print(myState.clusterID);
    out.print(
        "</title>"+
        "</head>\n"+
        "<body  bgcolor=\"#F0F0F0\">\n"+
        "<h3>Add Oplan to <b><font size=+1>");
    out.print(myState.clusterID);
    out.print(
        "</font></b></h3>\n"+
        "<form name=\"addOplanForm\" method=\"post\"\n"+
        " action=\"/$");
    out.print(myState.clusterID);
    out.print(myState.psp_path);
    out.print(
        "?POST\">\n"+
        "Oplan filename: ");
    int nFilenames = 
      ((selectFilenames != null) ? selectFilenames.size() : 0);
    if (nFilenames > 0) {
      // select from a pre-configured list
      out.print(
          "<select name=\""+
          MyPSPState.FILENAME+
          "\">\n");
      for (int i = 0; i < nFilenames; i++) {
        String si = (String)selectFilenames.get(i);
        out.print("<option value=\"");
        out.print(si);
        out.print("\">");
        out.print(si);
        out.print("</option>\n");
      }
      out.print("</select><br>\n");
    } else {
      //  provide text input
      out.print(
          "<input type=\"text\" name=\""+
          MyPSPState.FILENAME+
          "\" size=15><br>\n");
    }
    out.print(
        "<input type=\"submit\" name=\"formSubmit\""+
        " value=\"Add Oplan\"><br>\n"+
        "</form>\n"+
        "</body>\n"+
        "</html>\n");
    out.flush();
  }

  private static final void publishOplan(
      final MyPSPState myState, 
      final PrintStream out,
      final String filename) {
    // get direct delegate to PlanServer's Plugin
    PluginDelegate pi = myState.sps.getDirectDelegate();

    // parse the oplan file
    Oplan oplan;
    List oplanComponents;
    try {
      OplanFileReader ofr =
        new OplanFileReader(
            filename,
            pi.getFactory(),
            pi.getCluster());

      oplan = ofr.readOplan();
      if (oplan == null) {
        throw new IllegalArgumentException(
            "No such file \""+filename+"\" or incorrect XML format");
      }

      oplanComponents = new ArrayList();
      oplanComponents.addAll(ofr.getForcePackages(oplan));
      oplanComponents.addAll(ofr.getOrgActivities(oplan));
      oplanComponents.addAll(ofr.getOrgRelations(oplan));
      oplanComponents.addAll(ofr.getPolicies(oplan));

      if (oplan.getEndDay() == null) {
        oplan.inferEndDay(oplanComponents);
      }
    } catch (Exception e) {
      out.print(
          "<html><head><title>Unable to add Oplan</title></head>\n"+
          "<body bgcolor=\"#F0F0F0\">\n"+
          "<h3><font color=red>Unable to parse Oplan \"");
      out.print(filename);
      out.print("\"</font>"+
          "</h3>\n<p>"+
          "Either the file is unreachable or is in the incorrect XML format"+
          "<p><b>Exception:</b><pre>\n");
      e.printStackTrace(out);
      out.print("</pre></body></html>");
      out.flush();
      return;
    }

    try {
      pi.openTransaction();

      // publish the Oplan
      pi.publishAdd(oplan);

      // publish the components
      int nComp = oplanComponents.size();
      for (int i = 0; i < nComp; i++) {
        pi.publishAdd(oplanComponents.get(i));
      }

      // publish the Oplan coupon
      OplanCoupon ow = 
        new OplanCoupon(
            oplan.getUID(), 
            pi.getClusterIdentifier());
      pi.getCluster().getUIDServer().registerUniqueObject(ow);
      pi.publishAdd(ow);
    } catch (Exception e) {
      out.print(
          "<html><head><title>Unable to add Oplan</title></head>\n"+
          "<body  bgcolor=\"#F0F0F0\">\n"+
          "<h1><font color=red>Unable to publish Oplan \"");
      out.print(filename);
      out.print("\"</font>"+
          "</h1>\n<p><pre>");
      e.printStackTrace(out);
      out.print("</pre></body></html>");
      out.flush();
      return;
    } finally {
      pi.closeTransaction(false);
    }

    // success
    out.print("<html><head><title>Oplan Added to \"");
    out.print(myState.clusterID);
    out.print(
        "\"</title></head>\n"+
        "<body  bgcolor=\"#F0F0F0\">\n"+
        "<h3>Oplan \"");
    out.print(filename);
    out.print("\" <font color=darkgreen>Successfully</font> Published to \"");
    out.print(myState.clusterID);
    out.print("\"</h3>\n</body></html>");
    out.flush();
  }

  protected static class MyPSPState extends PSPState {

    /** my additional fields **/

    public static final String FILENAME = "file";
    public String filename;

    /** constructor **/
    public MyPSPState(
        UISubscriber xsubscriber,
        HttpInput query_parameters,
        PlanServiceContext xpsc) {
      super(xsubscriber, query_parameters, xpsc);
      // should do this in super!
      super.clusterID = super.clusterID.intern();
      // set fields
      filename = null;
    }

    /** use a query parameter to set a field **/
    public void setParam(String name, String value) {
      //super.setParam(name, value);
      if (name.equalsIgnoreCase(FILENAME)) {
        filename = value;
      }
    }
  }

  //
  // ancient/uninteresting methods
  //

  public PSP_AddOplan() {
    super();
  }
  public PSP_AddOplan(String pkg, String id) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }
  public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
    super.initializeTest();
    return false;
  }
  public boolean returnsXML() {
    return false;
  }
  public boolean returnsHTML() {
    return true;
  }
  public String getDTD() {
    return null;
  }
  public void subscriptionChanged(org.cougaar.core.blackboard.Subscription sub) {
  }

}
