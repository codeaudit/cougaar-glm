/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


/*
  Connects to debug PSP to get list of clusters and their URLs
  Then keeps table of producers created for each cluster, so views/consumers
  can avoid duplicating producers.
*/


package org.cougaar.domain.mlm.ui.newtpfdd.producer;

import java.io.IOException;
import java.io.StringReader;

import java.util.Iterator;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.swing.JOptionPane;

import org.cougaar.domain.mlm.ui.newtpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.newtpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.newtpfdd.util.ExceptionTools;

import org.cougaar.domain.mlm.ui.newtpfdd.xml.ClusterList;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.QueryData;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

// modern xerces references
//import org.apache.xerces.parsers.SAXParser;
//import org.xml.sax.helpers.DefaultHandler;
//import org.xml.sax.Attributes;

// old IBM XML jar references
import com.ibm.xml.parsers.SAXParser;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.Parser;
import org.xml.sax.helpers.ParserFactory;

public class ClusterCache
{
    public static final int CLIENT_PORT = 1111;

    // Vector of valid cluster IDs
    private Vector clusterNames = null;

    // Vector of clusters from which we'll allow Organization Itineraries
    // because corresponding carrier ones from corresponding leaf clusters are
    // broken or something
    private Vector allowOrgNames = null;
    
    // Hashtable mapping cluster IDs to URL
    private Hashtable clusterURLs;
    // Hashtables mapping cluster IDs to appropriate active Producer of that cluster
    private Hashtable logPlanCache;
    private Hashtable itineraryCache;
    private String host = "localhost";
    private boolean clientMode;

    // comma separated versions to build into above Vectors
    private String clusterList;
    private String allowOrgList;

  // defines default host when dialog comes up.
  private String defaultHostName = "localhost";
  // default root Cluster to ask for its subordinates
  private String rootCluster = "TRANSCOM";
  // are we asking the subordinates PSP for the subords of rootCluster?
  private boolean usingSubordinatesPSP;
  // turn off initial dialog and talk to default host
  private boolean noHostPrompt = false;

  private boolean debug = true;

    public ClusterCache(boolean clientMode)
    {
	this(clientMode, null);
    }

    public ClusterCache(boolean clientMode, String clusterList, String allowOrgList)
    {
	host = null;
	clusterNames = null;
	this.clusterList = clusterList;
	this.allowOrgList = allowOrgList;
	this.clientMode = clientMode;
	clusterURLs = new Hashtable();
	logPlanCache = new Hashtable();
	itineraryCache = new Hashtable();
	Debug.out("CC:CC leave");
    }

  /** dummy required to have a distinct signature */
  public ClusterCache(boolean clientMode, String allowOrgList) {
	usingSubordinatesPSP = true;
	clusterNames = null;
	//	this.clusterList = clusterList;
	this.allowOrgList = allowOrgList;
	this.clientMode = clientMode;
	clusterURLs = new Hashtable();
	logPlanCache = new Hashtable();
	itineraryCache = new Hashtable();
	Debug.out("CC:CC leave");

	String clusterNameProp = System.getProperty ("org.cougaar.domain.mlm.ui.newtpfdd.producer.rootCluster");
	if (clusterNameProp != null)
	  rootCluster = clusterNameProp;
	if (debug) 
	  System.out.println ("ClusterCache.ctor - root cluster now " + rootCluster);
	String defaultHostNameProp = System.getProperty ("org.cougaar.domain.mlm.ui.newtpfdd.producer.defaultHostName");
	if (defaultHostNameProp != null) {
	  defaultHostName = defaultHostNameProp;
	  host = defaultHostName;
	}
	noHostPrompt = 
	  ("true".equals(System.getProperty ("org.cougaar.domain.mlm.ui.newtpfdd.producer.noHostPrompt")));
	if (debug && noHostPrompt) 
	  System.out.println ("ClusterCache.ctor - skipping host dialog, connecting directly to " + defaultHostName);
	if (debug) {
	  System.out.println ("ClusterCache.ctor - root cluster property " + clusterNameProp + " - if null means use default");
	  System.out.println ("ClusterCache.ctor - default host " + defaultHostName);
	  System.out.println ("ClusterCache.ctor - noHostPrompt " + noHostPrompt);
	}
  }

    public String getHost()
    {
	return host;
    }

    public void setHost(String host)
    {
	this.host = host;
    }

  /** 
   * ask the user for the society host name 
   * defaults to localhost
   */
  public String guiSetHost()
  {
	if (noHostPrompt) {
	  host = defaultHostName;
	  if (debug)
		System.out.println ("ClusterCache.guiSetHost - no host prompt, host is " + host);
	  return host;
	}
	
	host = (String)JOptionPane.showInputDialog(null,
											   "Enter cluster proxy server",
											   "Location",
											   JOptionPane.INFORMATION_MESSAGE,
											   null, null, defaultHostName);
	if ( host != null )
	  host = host.trim();
	return host;
  }
    
  //
  // FUnction called by tpfdd client
  // to set the host, ie the aggregation server machine
  //
  public String clientGuiSetHost(String defHost)
  {
	if (noHostPrompt) {
	  host = defaultHostName;
	  if (debug)
		System.out.println ("ClusterCache.clientGuiSetHost - no host prompt, host is " + host);
	  return host;
	}
	
    host = (String)JOptionPane.showInputDialog(null,
             "Enter Aggregation Server Location","Location",
             JOptionPane.INFORMATION_MESSAGE,
             null, null, defHost);
    if ( host != null )
      host = host.trim();
    return host;
  }
    
  public Vector getallowOrgNames()
    {
	if ( allowOrgNames == null ) {
	    try {
		allowOrgNames = new Vector();
		if ( allowOrgList != null ) {
		    ClusterList allowParser = new ClusterList(allowOrgList);
		    String[] allowNameArray = allowParser.getNames();
		    for ( int i = 0; i < allowNameArray.length; i++ )
			allowOrgNames.add(allowNameArray[i]);
		}
		Debug.out("CC:gAON Allowing non-carrier itineraries from: " + allowOrgNames);
	    }
	    catch ( Exception e ) {
		OutputHandler.out(ExceptionTools.toString("CC:gAON", e));
	    }
	}
	return allowOrgNames;
    }



  public Vector getclusterNames() {
	if ( clusterNames == null ) {
	  if (usingSubordinatesPSP) {
		setClusterNames ();
		return clusterNames;
	  }
	  else {
	    try {
		  clusterNames = new Vector();
		  if ( clientMode )
		    clusterNames.add("Aggregation");
		  else { // aggregation server, talks to real clusters, but configured manually
		    ClusterList parser = new ClusterList(clusterList);
		    String[] nameArray = parser.getNames();
		    for ( int i = 0; i < nameArray.length; i++ )
			  clusterNames.add(nameArray[i]);
		  }
	    }
	    catch ( Exception e ) {
		  OutputHandler.out(ExceptionTools.toString("CC:init", e));
	    }
	  }
	}
	return clusterNames;
  }

  public Vector determineClusterFromSubordinatesPSP(){
    Vector names = new Vector();
    
    ConnectionHelper helper = 
      new ConnectionHelper (getClusterURL (rootCluster),
			    PSPClientConfig.PSP_package, 
			    PSPClientConfig.SubordinatesPSP_id + "?MODE=1");
    try {
      // talk to PSP
      String response = new String (helper.getResponse());
      if (debug)
	System.out.println ("got response " + response);
      // modern xerces jar
      //	  SAXParser parser = new SAXParser();
      // old IBM XML jar
      Parser parser = ParserFactory.makeParser("com.ibm.xml.parsers.SAXParser");
      // modern xerces jar
      //	  parser.setContentHandler (new ClusterNameHandler());
      // old IBM XML jar
      parser.setDocumentHandler (new ClusterNameHandler(names));
      parser.parse (new InputSource (new StringReader (response)));
    }
    catch ( IOException e ) {
      OutputHandler.out("ClusterCache.setSubordinates - IOError in connection, url was : " + helper + 
			"\nError was: " + e);
    } catch (Exception e) {
      System.err.println (e.getMessage());
      e.printStackTrace();
    }
    return names;
  }

  /**
   *  As a side effect, fills in the clusterNames Vector
   */
  protected void setClusterNames () {
    if (clientMode) {
      clusterNames = new Vector ();
      clusterNames.add("Aggregation");
      return;
    }
    clusterNames=determineClusterFromSubordinatesPSP();
  }

  // for modern Xerces jar
  //  private class ClusterNameHandler extends DefaultHandler {
  // for old (June 1999) IBM XML jar
  public static class ClusterNameHandler extends HandlerBase {
    Vector clusterNames;
    public ClusterNameHandler(Vector v){
      clusterNames=v;
    }
	// for modern Xerces jar
	//    public void startElement (String uri, String local, String name, Attributes atts) throws SAXException {
	// for old (June 1999) IBM XML jar
    public void startElement (String name, AttributeList atts) {
	  if (name.equals ("org")) {
		clusterNames.add (atts.getValue ("name"));
	  }
	}
  }

    public String getClusterURL(String clusterName)
    {
	return "http://" + host + ":5555/$" + clusterName + "/";
    }
    
    public boolean isValidClusterName(String clusterName)
    {
	return getclusterNames().contains(clusterName);
    }

    private void putProducerForCluster(String clusterName, AbstractProducer producer)
    {
	if ( !(getclusterNames().contains(clusterName)) ) {
	    Debug.out("CC:pIPFC Note: adding new cluster name: " + clusterName);
	    clusterNames.add(clusterName);
	}
	if ( producer instanceof ItineraryProducer )
	    putItineraryProducerForCluster(clusterName, (ItineraryProducer)producer);
	else if ( producer instanceof LogPlanProducer )
	    putLogPlanProducerForCluster(clusterName, (LogPlanProducer)producer);
	else
	    OutputHandler.out("CC:pPFC Error: unexpected producer type: " + producer.getClass().getName());
    }

    private void putItineraryProducerForCluster(String clusterName, ItineraryProducer producer)
    {
	ItineraryProducer oldProducer = (ItineraryProducer)(itineraryCache.get(clusterName));
	if ( oldProducer != null ) {
	    OutputHandler.out("CC:pIPFC Purging old itinerary producer for " + clusterName);
	    oldProducer.cleanup(); // kills service thread
	}

	itineraryCache.put(clusterName, producer);
    }

    private void putLogPlanProducerForCluster(String clusterName, LogPlanProducer producer)
    {
	LogPlanProducer oldProducer = (LogPlanProducer)(logPlanCache.get(clusterName));
	if ( oldProducer != null ) {
	    OutputHandler.out("CC:pLPPFC Purging old logplan/aggregation producer for " + clusterName);
	    oldProducer.cleanup(); // kills service thread
	}
	    
	logPlanCache.put(clusterName, producer);
    }

    public ItineraryProducer getItineraryProducer(String clusterName, boolean cold)
    {
	return (ItineraryProducer)(getProducer(clusterName, itineraryCache, cold, false));
    }

    public ThreadedProducer getLogPlanProducer(String clusterName, boolean cold)
    {
	// The LogPlanProducer has been replaced by Folger's crystals. Let's see if anyone notices!
	//return (LogPlanProducer)(getProducer(clusterName, logPlanCache, cold, true));
	return (ThreadedProducer)(getProducer(clusterName,logPlanCache,cold,true));
    }

    private AbstractProducer getProducer(String clusterName, Hashtable cache, boolean cold, boolean autoStart)
    {
      if (clusterName == null) {
        OutputHandler.out("CC:gP Warning: clusterName is null");
        return null;
      }
	// Debug.out("CC:gP enter " + clusterName);
	if ( clusterName.equals("MCCGlobalMode") ) {
	    OutputHandler.out("CC:gP Warning: forcing old 'MCCGlobalMode' to updated 'TRANSCOM'");
	    clusterName = "TRANSCOM";
	}
	// Debug.out("CC:gP checking cluster name validity " + clusterName);
	if ( !isValidClusterName(clusterName) ) {
	    OutputHandler.out("CC:gP Error: " + clusterName + " is an invalid cluster name!");
	    return null;
	}

	AbstractProducer producer = null;
	if ( !cold ) {
	    producer = (AbstractProducer)cache.get(clusterName);
	    
	    // return first producer that matches classname
	    if ( producer != null ) {
		Debug.out("CC:gP returning warm " + producer + " to request " + clusterName);
		return producer;
	    }
	}

	// Debug.out("CC:gP making new producer");
	if ( clusterName.equals("Aggregation") )
	    producer = new LogPlanProducer(clusterName, this);
// 	    producer = new LogPlanProducer(host, CLIENT_PORT, this);
	else if ( cache == logPlanCache )
	    producer = new LogPlanProducer(clusterName, this);
// 	    producer = new LogPlanProducer(clusterName, this);
	else if ( cache == itineraryCache )
	    producer = new ItineraryProducer(clusterName, this, getallowOrgNames().contains(clusterName));
	putProducerForCluster(clusterName, producer); // this kills off any old one in the same slot
	if ( autoStart )
	    producer.start();
	//Debug.out("CC:gP returning cold " + producer + " to request " + clusterName);
	return producer;
    }

  /** Test as standalone class  -- gets the names of the subordinates of rootCluster */
  public static void main (String args[]) {
	ClusterCache cache = new ClusterCache (false, "");
	System.out.println ("Talked to the psp, and the subords of " + cache.host + " are " + cache.getclusterNames ());
  }
}