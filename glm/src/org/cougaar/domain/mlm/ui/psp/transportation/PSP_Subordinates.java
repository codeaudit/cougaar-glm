/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation;

import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Relationship;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.lib.planserver.PSPState;
import org.cougaar.lib.planserver.PSP_BaseAdapter;
import org.cougaar.lib.planserver.PlanServiceContext;
import org.cougaar.lib.planserver.PlanServiceProvider;
import org.cougaar.lib.planserver.PlanServiceUtilities;
import org.cougaar.lib.planserver.UISubscriber;
import org.cougaar.lib.planserver.RuntimePSPException;
import org.cougaar.lib.planserver.HttpInput;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.MutableTimeSpan;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Organization;

public class PSP_Subordinates extends PSP_BaseAdapter
                       implements PlanServiceProvider, UISubscriber
{
  public static final boolean DEBUG = false;

  /** 
   * A zero-argument constructor is required for dynamically loaded PSPs,
   * required by Class.newInstance()
   **/
  public PSP_Subordinates() 
  {
    super();
  }

  public PSP_Subordinates(
    String pkg, String id) throws RuntimePSPException
  {
    setResourceLocation(pkg, id);
  }

  public boolean test(
    HttpInput query_parameters, PlanServiceContext sc)
  {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  private static UnaryPredicate getSelfPred(){
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Organization) {
          //System.out.println("ORG PREDICATE CALLED true!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
          return ((Organization)o).isSelf();
        }
        return false;
      }
    };
  }

  /**
   * <pre>
   *  Operates in 2 modes.
   *        Mode 1.   "?MODE=1"  Return relationships from all other Clusters as
   *                             well as yourself.   As HTML or XML.  A PSP
   *                             evoked in this mode doesn't actually generate
   *                             response, it will "recurse" upon all white page
   *                             clusters (redirect query) (including itself)
   *                             and evoke each PSP in MODE=2..
   *
   *        Mode 2.   "?MODE=2" Return your relationships only. As HTML or XML.
   *
   *       ?SUPPRESS  = Suppress header and footer (HTML)
   *
   *       ?HTML = return results as Expanded HTML (see ?UNEXPANDED_HTML)
   *       ?UNEXPANDED_HTML  =  Don't provide expanded table representation, just links.
   *                       Viewer can click on link.
   *                       Identical to ?HTML if Mode == 2.
   * </pre>
   * @param out the output stream that the XML text will go to
   * @param query_parameters - parameters as above
   * @param psc - context of the psp
   * @param psu - unused psp utilities
   **/
  public void execute(PrintStream out,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception
  {
    MyPSPState myState = new MyPSPState(this, query_parameters, psc);
    myState.configure(query_parameters);

	if (DEBUG) {
	  System.out.println ("PSP_Subordinates.execute - URL params were : " + query_parameters.getURLParameters());
	}

    if (myState.mode == 1) {
      // ASK CHILDREN (case MODE == 1 )
	  out.println ("<?xml version='1.0'?><subordinates>");
      getMyRelationships(out, myState);
	  out.println ("</subordinates>");
    } else if (myState.mode == 2) {
       //
       // ASK MYSELF (ONLY ANSWER DIRECT QUERIES -- CASE OF WHEN ALL CLUSTERS
       // ASKED BY SOMEONE OF THIS PSP WILL BE HANDLED BY QUERY-REDIRECT...
       // IE. a recursive call in MODE 2.  This is done out of simplicity.
       //
       getMyRelationships(out, myState);
    } else {
      getUsage(out, myState);
    }
  }

  /** USAGE **/
  private void getUsage(PrintStream out, MyPSPState myState)
  {
    out.println(
     "<HTML><HEAD><TITLE>Subordinates Usage</TITLE></HEAD><BODY>");
    out.println(
     "<H2><CENTER>Subordinates Usage</CENTER></H2><P>");
    out.print("<FORM METHOD=\"POST\" ACTION=\"");
    out.print(myState.cluster_psp_url);
    out.print("?POST");
    out.println("\">");
    out.println(
      "Show cluster relations for:<p>");
    out.print(
      "&nbsp;&nbsp;<INPUT TYPE=\"radio\" NAME=\"mode\" VALUE=\"1\" CHECKED>");
    out.println("&nbsp;All related clusters<p>");
    out.print(
      "&nbsp;&nbsp;<INPUT TYPE=\"radio\" NAME=\"mode\" VALUE=\"2\">");
    out.print("&nbsp;Just ");
    out.print(myState.clusterID);
    out.println("<P>");
    out.println("</INPUT>");
    out.println(
      "<INPUT TYPE=\"hidden\" NAME=\"html\" VALUE=\"true\">");
    out.println(
      "<INPUT TYPE=\"hidden\" NAME=\"unexpand_html\" VALUE=\"false\">");
    out.println(
      "<INPUT TYPE=\"hidden\" NAME=\"suppress\" VALUE=\"false\">");
    out.println(
      "<INPUT TYPE=\"submit\" NAME=\"Display PSP\">");
    out.println("</FORM></BODY></HTML>");
  }

  private void getMyRelationships(PrintStream out, MyPSPState myState) {
    // ASK MYSELF
    Collection col =
      myState.psc.getServerPlugInSupport().queryForSubscriber(
          getSelfPred());
    boolean empty = (col.size() == 0);
    if (!empty) {
      Iterator iter = col.iterator();
      do {
        Asset asst = (Asset)iter.next();
        Organization org = (Organization)asst;
		if (!org.isSelf ())
		  continue;
		
        if (myState.htmlFlag || true) {
		  generateXML (out, myState, org);
        }
      } while (iter.hasNext());
    }
  }

  /** actually spits out the XML 
   * Overridden in PSP_Hierarchy
   * @see org.cougaar.domain.mlm.ui.psp.transportation.PSP_Hierarchy
   * @param org the org to generate xml for
   **/
  protected void generateXML (PrintStream out, MyPSPState myState, Organization org) {
	Collection subordinates = org.getSubordinates(new MutableTimeSpan());
	String orgName = org.getItemIdentificationPG().getNomenclature();
	RelationshipSchedule schedule = org.getRelationshipSchedule ();
	
	out.println("<org name=\""+ orgName+ "\" />");

	// Safe to iterate over subordinates because getSubordinates() returns
	// a new Collection.
	for (Iterator schedIter = subordinates.iterator(); schedIter.hasNext();) {
	  Relationship relationship = (Relationship)schedIter.next();
	  if (DEBUG)
		System.out.println ("PSP_Subordinates.getMyRelationships - self " + 
							orgName + 
							"'s relationship : " + relationship);
	  String subord =
		((Asset)schedule.getOther(relationship)).getItemIdentificationPG().getNomenclature();

	  if (DEBUG)
		System.out.println ("\tsubord is " + subord + ", subord's role is : " + 
							relationship.getRoleA());
	  if (!orgName.equals (subord))
		recurseOnSubords (out, myState, subord);
	}
  }

  /**
   * yuck -- this PSP calling other PSPs!
   *
   * would rather have PSPs come in two types:
   *   1) logplan access with no outside connections
   *   2) no logplan access with outside connections (i.e. PSPs)
   * i.e. this PSP split into two PSPs, a (1) and a (2).
   *
   * @param out stream to print results to
   * @param myState has all the psp's state info - need it for the psp_path
   * @param subordinate the name of the subordinate to recurse on
   */
  protected void recurseOnSubords (PrintStream out,
								   MyPSPState myState, 
								   String subordinate) {
	try {
	  //
	  // this.getPath() => resource path of this PSP...
	  //
	  URL myURL = new URL(myState.base_url+"$"+subordinate+myState.psp_path+"?MODE=2?SUPPRESS?HTML");

	  if (DEBUG)
		System.out.println("[PSP_Subordinates.recurseOnSubords] child URL: " + myURL.toString());

	  URLConnection myConnection = myURL.openConnection();
	  if (DEBUG)
		System.out.println("[PSP_Subordinates.recurseOnSubords] opened query connection = "+ myConnection.toString());

	  InputStream is = myConnection.getInputStream();
	  BufferedInputStream bis = new BufferedInputStream(is);
	  byte buf[] = new byte[512];
	  int sz2;
	  while ((sz2 = bis.read(buf,0,512)) != -1) {
		out.write(buf,0,sz2);
	  }
	} catch (Exception e) {
	  e.printStackTrace();
	}
  }
  
  /** required */
  public void subscriptionChanged(Subscription subscription) {}

  /**
   * A PSP can output either HTML or XML (for now).  The server
   * should be able to ask and find out what type it is.
   **/
  public boolean returnsXML() {
    return true;
  }

  public boolean returnsHTML() {
    return false;
  }

  /**  Any PlanServiceProvider must be able to provide DTD of its
   *  output IFF it is an XML PSP... ie.  returnsXML() == true;
   *  or return null
   **/
  public String getDTD()  {
    return null;
  }

  /** holds PSP state (i.e. url flags for MODE, etc) **/
  protected static class MyPSPState extends PSPState {

    /** my additional fields **/
    public int mode;
    public boolean htmlFlag;
    public boolean xmlFlag;
    public boolean suppressFlag;
    public boolean unexpandedHtmlFlag;

    /** constructor **/
    public MyPSPState(
        UISubscriber xsubscriber,
        HttpInput query_parameters,
        PlanServiceContext xpsc) {
      super(xsubscriber, query_parameters, xpsc);
    }

    /** use a query parameter to set a field **/
    public void setParam(String name, String value) {
      //super.setParam(name, value);

      if (name.equalsIgnoreCase("html")) {
        htmlFlag =
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("xml")) {
        xmlFlag =
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("unexpanded_html")) {
        unexpandedHtmlFlag = 
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("suppress")) {
        suppressFlag = 
          ((value != null) ?  value.equalsIgnoreCase("true") : true);
      } else if (name.equalsIgnoreCase("mode")) {
        try {
          mode = Integer.parseInt(value);
        } catch (Exception e) {
          mode = 0;
        }
      }
    }
  }

}

