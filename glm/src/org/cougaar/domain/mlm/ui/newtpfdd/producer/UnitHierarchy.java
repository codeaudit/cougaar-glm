/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/producer/Attic/UnitHierarchy.java,v 1.2 2001-02-23 01:02:19 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.newtpfdd.producer;


import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;


import java.awt.event.ActionListener;
import javax.swing.event.MenuListener;

import java.io.IOException;
import java.io.IOException;
import java.io.StringReader;

import org.cougaar.domain.mlm.ui.newtpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.newtpfdd.util.OutputHandler;

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

public class UnitHierarchy implements TreeModel
{
    private Hashtable immedHash;           // String -> Vector (immediate subordinates)
    private Hashtable depthHash;           // String -> Vector (downward commanded, recursively)
    private Hashtable reverseImmedHash;    // String -> String (immediate supervisor)
    private Hashtable reverseDepthHash;    // String -> Vector (upward command chain)
    private JTree myTree; // obnoxious intrusive callback that lets us force-expand when queried

  private static String higherAuthority = "HigherAuthority";
  
  protected Map orgToSuperior = new HashMap ();
  private Set demandRoots = new HashSet ();
  private String host = "localhost";

  /** who sets this? */
  public void setHost(String host) { this.host = host; }

  protected void getNames (Set nameSet, String names) {
	StringTokenizer st = new StringTokenizer(names, ",");
	while (st.hasMoreTokens()) {
	  String name = st.nextToken();
	  nameSet.add (name.trim());
	}
  }

  private static String demandRootClusters = 
    System.getProperty ("org.cougaar.domain.mlm.ui.newtpfdd.producer.UnitHierarchy.demandRootClusters");
  private static String testCluster = 
    System.getProperty ("org.cougaar.domain.mlm.ui.newtpfdd.producer.UnitHierarchy.testClusters");
  private static boolean useHierarchyPSP = 
    "true".equals (System.getProperty ("org.cougaar.domain.mlm.ui.newtpfdd.producer.UnitHierarchy.useHierarchyPSP"));
  private static boolean debug = 
    "true".equals (System.getProperty ("org.cougaar.domain.mlm.ui.newtpfdd.producer.UnitHierarchy.debug"));
  
    static final String[][] superiors = 
    { // { "Society", "HigherAuthority" },
      { "FUTURE", "HigherAuthority" },
      { "IOC", "HigherAuthority" },
      { "AEF1", "HigherAuthority" },
      { "AEF2", "HigherAuthority" },
      { "AEF9", "HigherAuthority" },
      { "COMMARFORPAC", "HigherAuthority" },
      { "CMDELEMENT-IMEFFWD", "COMMARFORPAC" },
      { "HQ-3MAWFWD", "CMDELEMENT-IMEFFWD" },
      { "MAG-11FWD", "HQ-3MAWFWD" },
      { "VMFA-225", "MAG-11FWD" },
      { "III Corps", "Society" },
      { "XVIII Corps", "HigherAuthority" },
      { "1CAVDIV", "III Corps" },
      { "4ID", "III Corps" },
      { "3ID", "XVIII Corps", },
      { "1BDE-3ID", "3ID" },
      { "2BDE-3ID", "3ID" },
      { "3BDE-3ID", "3ID" },
      { "AVNBDE-3ID", "3ID" },
      { "DIVARTY-3ID", "3ID" },
      { "ENGBDE-3ID", "3ID" },
      { "DISCOM-3ID", "3ID" },
      { "703-MSB", "DISCOM-3ID"},
      { "3-FSB", "DISCOM-3ID"},
      { "26-FSB", "DISCOM-3ID"},
      { "203-FSB", "DISCOM-3ID"},
      { "603-DASB", "DISCOM-3ID"},
      { "3-7-INFBN", "1BDE-3ID" },
      { "3-69-ARBN", "1BDE-3ID" },
      { "2-7-INFBN", "1BDE-3ID" },
      { "3-15-INFBN", "2BDE-3ID" },
      { "4-64-ARBN", "2BDE-3ID" },
      { "1-64-ARBN" , "2BDE-3ID" },
      { "2-69-ARBN", "3BDE-3ID" },
      { "1-30-INFBN", "3BDE-3ID" },
      { "1-15-INFBN", "3BDE-3ID" },
      { "3-7-CAVSQDN", "AVNBDE-3ID" },
      { "1-3-AVNBN", "AVNBDE-3ID" },
      { "2-3-AVNBN", "AVNBDE-3ID" },
      { "1-9-FABN", "DIVARTY-3ID" },
      { "1-41-FABN", "DIVARTY-3ID" },
      { "1-10-FABN", "DIVARTY-3ID" },
      { "10-ENGBN", "ENGBDE-3ID" },
      { "317-ENGBN", "ENGBDE-3ID" },
      { "11-ENGBN", "ENGBDE-3ID" },
      // everything above goes in applet menu, everything below does not

      { "1BDE-4ID", "4ID" },
      { "2BDE-4ID", "4ID" },
      { "3BDE-4ID", "4ID" },
      { "AVNBDE-4ID", "4ID" },
      { "DIVARTY-4ID", "4ID" },
      { "ENGBDE-4ID", "4ID" },
      { "1-22-INFBN", "1BDE-4ID" },
      { "1-66-ARBN", "1BDE-4ID" },
      { "3-66-ARBN", "1BDE-4ID" },
      { "1-67-ARBN", "2BDE-4ID" },
      { "2-8-INFBN", "2BDE-4ID" },
      { "3-67-ARBN" , "2BDE-4ID" },
      { "1-12-INFBN", "3BDE-4ID" },
      { "1-68-ARBN", "3BDE-4ID" },
      { "1-8-INFBN", "3BDE-4ID" },
      { "1-10-CAVSQDN", "AVNBDE-4ID" },
      { "1-4-AVNBN", "AVNBDE-4ID" },
      { "2-4-AVNBN", "AVNBDE-4ID" },
      { "2-20-FABN", "DIVARTY-4ID" },
      { "3-16-FABN", "DIVARTY-4ID" },
      { "3-29-FABN", "DIVARTY-4ID" },
      { "4-42-FABN", "DIVARTY-4ID" },
      { "299-ENGBN", "ENGBDE-4ID" },
      { "4-ENGBN", "ENGBDE-4ID" },
      { "588-ENGBN", "ENGBDE-4ID" },
      { "1BDE-1CAVDIV", "1CAVDIV" },
      { "2BDE-1CAVDIV", "1CAVDIV" },
      { "3BDE-1CAVDIV", "1CAVDIV" },
      { "AVNBDE-1CAVDIV", "1CAVDIV" },
      { "DIVARTY-1CAVDIV", "1CAVDIV" },
      { "ENGBDE-1CAVDIV", "1CAVDIV" },
      { "1-12-CAVRGT", "1BDE-1CAVDIV" },
      { "2-5-CAVRGT", "1BDE-1CAVDIV" },
      { "2-8-CAVRGT", "1BDE-1CAVDIV" },
      { "1-5-CAVRGT", "2BDE-1CAVDIV" },
      { "1-8-CAVRGT", "2BDE-1CAVDIV" },
      { "2-12-CAVRGT" , "2BDE-1CAVDIV" },
      { "1-9-CAVRGT", "3BDE-1CAVDIV" },
      { "2-7-CAVRGT", "3BDE-1CAVDIV" },
      { "3-8-CAVRGT", "3BDE-1CAVDIV" },
      { "1-227-AVNRGT", "AVNBDE-1CAVDIV" },
      { "1-SQDN-7-CAVRGT", "AVNBDE-1CAVDIV" },
      { "2-227-AVNRGT", "AVNBDE-1CAVDIV" },
      { "1-21-FABN", "DIVARTY-1CAVDIV" },
      { "1-82-FABN", "DIVARTY-1CAVDIV" },
      { "2-82-FABN", "DIVARTY-1CAVDIV" },
      { "3-82-FABN", "DIVARTY-1CAVDIV" },
      { "20-ENGBN", "ENGBDE-1CAVDIV" },
      { "8-ENGBN", "ENGBDE-1CAVDIV" },
      { "91-ENGBN", "ENGBDE-1CAVDIV" }
      };

    public UnitHierarchy(String host)
    {
	  if (demandRootClusters == null || demandRootClusters.length () < 1)
		demandRootClusters = "FUTURE, IOC, XVIIICorps"; // AEF1, AEF2, AEF9, COMMARFORPAC, 
	  if (debug)
		System.out.println("UnitHierarchy.UnitHierarchy - host is " + host);
	  
	immedHash = new Hashtable();
	reverseImmedHash = new Hashtable();
	depthHash = new Hashtable();
	reverseDepthHash = new Hashtable();
	// set up all the immediate relationships (a contains b if a directly commands b)
	if (useHierarchyPSP) {
	  if (debug)
		System.out.println("UnitHierarchy.UnitHierarchy - getting hierarchy from psp.");

	  getNames (demandRoots, demandRootClusters);
	  setHost (host);
	  determineHierarchyFromPSP ();
	  for ( Iterator iter = orgToSuperior.keySet().iterator (); iter.hasNext (); ) {
		String org = (String) iter.next ();
		String superior = (String) orgToSuperior.get (org);
		
	    Vector mySubs = (Vector)(immedHash.get(superior));
	    if ( mySubs == null )
		  immedHash.put(superior, (mySubs = new Vector()));
	    mySubs.add(org);
	    reverseImmedHash.put(org, superior);
	  }
	}
	else {
	  for ( int i = 0; i < superiors.length; i++ ) {
	    Vector mySubs = (Vector)(immedHash.get(superiors[i][1]));
	    if ( mySubs == null )
		  immedHash.put(superiors[i][1], (mySubs = new Vector()));
	    mySubs.add(superiors[i][0]);
	    reverseImmedHash.put(superiors[i][0], superiors[i][1]);
	  }
	}
	
	// expand to all ultimate relationships (a contains b if a above b in the command hierarchy)
	fillInChildren("HigherAuthority", new Vector());
    }

  /** there may be more than one root demand cluster */
  public void determineHierarchyFromPSP(){
	for (Iterator iter = demandRoots.iterator (); iter.hasNext ();) {
	  String org = (String) iter.next ();
	  determineHierarchyFor (orgToSuperior, org);
	  orgToSuperior.put (org, higherAuthority);
	}
	// so we can run with test input data from the GLMStimulatorPlugIn
	if (testCluster != null) {
	  orgToSuperior.put (testCluster, higherAuthority);
	}
  }

  /**
   * @see org.cougaar.domain.mlm.ui.psp.transportation.PSP_Hierarchy
   **/
  public void determineHierarchyFor(Map orgToSuperior, String rootDemandCluster){
    ConnectionHelper helper = 
      new ConnectionHelper (getClusterURL (rootDemandCluster),
			    PSPClientConfig.PSP_package, 
			    PSPClientConfig.HierarchyPSP_id + "?MODE=1");
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
      parser.setDocumentHandler (new ClusterNameHandler(orgToSuperior));
      parser.parse (new InputSource (new StringReader (response)));
    }
    catch ( IOException e ) {
      OutputHandler.out("ClusterCache.setSubordinates - IOError in connection, url was : " + helper + 
			"\nError was: " + e);
    } catch (Exception e) {
      System.err.println (e.getMessage());
      e.printStackTrace();
    }
  }

  public String getClusterURL(String clusterName) { return "http://" + host + ":5555/$" + clusterName + "/";  }

  /** 
   * Expected format is:
   * 
   * <pre>
   * Format is :
   * <org name=xxx>
   *  <subord name=xxx/>
   *  <subord name=xxx/>
   * </org>
   * <org name=xxx>
   *  <subord name=xxx/>
   *  <subord name=xxx/>
   * </org>
   * ....
   * </pre>
   * @see org.cougaar.domain.mlm.ui.psp.transportation.PSP_Hierarchy
   **/
  // for modern Xerces jar
  //  private class ClusterNameHandler extends DefaultHandler {
  // for old (June 1999) IBM XML jar
  public static class ClusterNameHandler extends HandlerBase {
    Map orgToSuperior;
	String superior;
    public ClusterNameHandler(Map m){
      orgToSuperior=m;
    }
	// for modern Xerces jar
	//    public void startElement (String uri, String local, String name, Attributes atts) throws SAXException {
	// for old (June 1999) IBM XML jar
    public void startElement (String name, AttributeList atts) {
	  if (name.equals ("org"))
		superior = atts.getValue ("name");
	  else if (name.equals ("subord"))
		orgToSuperior.put (atts.getValue ("name"), superior);
	}
  }

    public void setTree(JTree tree)
    {
	myTree = tree;
    }

    private void fillInChildren(String unitName, Vector commandingUnits)
    {
	Iterator commandIter = commandingUnits.iterator();
	while ( commandIter.hasNext() ) {
	    // add ourselves as subordinate to everyone in our command chain
	    String commandName = (String)commandIter.next();
	    Vector subs = (Vector)(depthHash.get(commandName));
	    if ( subs == null )
		depthHash.put(commandName, (subs = new Vector()));
	    subs.add(unitName);
	}

	// record the command chain in the reverse look up
	reverseDepthHash.put(unitName, commandingUnits.clone());
	
	// add ourselves to the command chain for reference by all children
	Vector unit = (Vector)immedHash.get(unitName);
	if ( unit == null )
	    return;
	commandingUnits.add(0, unitName);
	for ( int i = 0; i < unit.size(); i++ )
	    fillInChildren((String)(unit.get(i)), commandingUnits);
	commandingUnits.remove(0);
    }

    public Vector getCommandees(String name)
    {
	return (Vector)(immedHash.get(name));
    }

    public Vector getDeepCommandees(String name)
    {
	return (Vector)(depthHash.get(name));
    }

    public String getCommander(String name)
    {
	return (String)(reverseImmedHash.get(name));
    }

    public Vector getCommanders(String name)
    {
	return (Vector)(reverseDepthHash.get(name));
    }

    public Iterator unitNameIterator()
    {
	return reverseImmedHash.keySet().iterator();
    }

    public JMenu toMenu(ActionListener listener)
    {
	JMenu menu = new JMenu();
	menu.setText("Society subordinates");
	menu.setName("societySubordinatesMenu");
	setupMenu(menu, "Society", listener);
	return menu;
    }
    
    public void setupMenu(JMenu parentMenu, String unitName, ActionListener listener)
    {
	Vector subs = (Vector)(immedHash.get(unitName));
	
	for ( Iterator subsIter = subs.iterator(); subsIter.hasNext(); ) {
	    String subName = (String)(subsIter.next());
	    JMenuItem menuItem = new JMenuItem();
	    menuItem.setName(subName + "MenuItem");
	    menuItem.setText(subName);
	    menuItem.addActionListener(listener);
	    parentMenu.add(menuItem);

	    if ( immedHash.get(subName) == null )
		continue;
	    
	    JMenu submenu = new JMenu();
	    submenu.setName(subName + "SubordinatesMenuItem");
	    submenu.setText("   " + subName + " subordinates");
	    setupMenu(submenu, subName, listener);
	    parentMenu.add(submenu);
	}
    }

    public String toString()
    {
	String out = "";
	for ( Enumeration enum = depthHash.keys(); enum.hasMoreElements(); ) {
	    String name = (String)enum.nextElement();
	    Vector subs = (Vector)(depthHash.get(name));
	    out += name + ":\n";
	    for ( Iterator subsIter = subs.iterator(); subsIter.hasNext(); )
		out += "\t" + subsIter.next() + "\n";
	}
	return out;
    }

    public static void main(String[] args)
    {
	System.out.println(new UnitHierarchy("localhost"));
    }

    // TreeModel Interface
    public Object getRoot()
    {
	  //	return superiors[0][1];
	  return higherAuthority;
    }

    private Object getParent(Object child)
    {
	return (String)(reverseImmedHash.get(child));
    }

    public TreePath getPathToNode(Object child)
    {
	Vector chain = new Vector();
	Object chainWalk = child;
	while ( chainWalk != null ) {
	    chain.add(0, chainWalk);
	    chainWalk = getParent(chainWalk);
	}
	return new TreePath(chain.toArray());
    }
    
    public void expandAll()
    {
	if ( myTree == null )
	    return;
	for ( Enumeration i = immedHash.keys(); i.hasMoreElements(); )
	    myTree.expandPath(getPathToNode(i.nextElement()));
    }

    public Object getChild(Object parent, int index)
    {
	Vector children = (Vector)(immedHash.get(parent));

	return children.get(index);
    }

    public int getChildCount(Object parent)
    {
	Vector children = (Vector)(immedHash.get(parent));
	return children.size();
    }

    public boolean isLeaf(Object node)
    {
	return (immedHash.get(node) == null);
    }

    public void valueForPathChanged(TreePath path, Object newValue)
    {
    }

    public int getIndexOfChild(Object child, Object parent)
    {
	Vector children = (Vector)(immedHash.get(parent));
	return children.indexOf(child);
    }

    public void addTreeModelListener(TreeModelListener l)
    {
    }

    public void removeTreeModelListener(TreeModelListener l)
    {
    }
}
