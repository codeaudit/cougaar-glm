/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/producer/Attic/UnitHierarchy.java,v 1.1 2000-12-15 20:17:48 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.producer;


import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;
import java.util.Enumeration;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;


import java.awt.event.ActionListener;
import javax.swing.event.MenuListener;

import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;


public class UnitHierarchy implements TreeModel
{
    private Hashtable immedHash;           // String -> Vector (immediate subordinates)
    private Hashtable depthHash;           // String -> Vector (downward commanded, recursively)
    private Hashtable reverseImmedHash;    // String -> String (immediate supervisor)
    private Hashtable reverseDepthHash;    // String -> Vector (upward command chain)
    private JTree myTree; // obnoxious intrusive callback that lets us force-expand when queried

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

    public UnitHierarchy()
    {
	immedHash = new Hashtable();
	reverseImmedHash = new Hashtable();
	depthHash = new Hashtable();
	reverseDepthHash = new Hashtable();
	// set up all the immediate relationships (a contains b if a directly commands b)
	for ( int i = 0; i < superiors.length; i++ ) {
	    Vector mySubs = (Vector)(immedHash.get(superiors[i][1]));
	    if ( mySubs == null )
		immedHash.put(superiors[i][1], (mySubs = new Vector()));
	    mySubs.add(superiors[i][0]);
	    reverseImmedHash.put(superiors[i][0], superiors[i][1]);
	}
	// expand to all ultimate relationships (a contains b if a above b in the command hierarchy)
	fillInChildren("HigherAuthority", new Vector());
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
	System.out.println(new UnitHierarchy());
    }

    // TreeModel Interface
    public Object getRoot()
    {
	return superiors[0][1];
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
