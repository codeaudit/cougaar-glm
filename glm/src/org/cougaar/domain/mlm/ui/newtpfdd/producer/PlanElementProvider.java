/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/producer/Attic/PlanElementProvider.java,v 1.3 2001-02-23 17:28:45 wseitz Exp $ */

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
import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;
import java.util.Date;

import org.cougaar.util.TimeSpan;
import org.cougaar.domain.mlm.ui.newtpfdd.TPFDDConstants;
import org.cougaar.domain.mlm.ui.newtpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.newtpfdd.util.MismatchException;
import org.cougaar.domain.mlm.ui.newtpfdd.util.PathString;
import org.cougaar.domain.mlm.ui.newtpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.newtpfdd.util.Callback;
import org.cougaar.domain.mlm.ui.newtpfdd.util.VectorHashtable;

import org.cougaar.domain.mlm.ui.newtpfdd.xml.LogPlanObject;
import org.cougaar.domain.mlm.ui.newtpfdd.xml.Location;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.Node;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.model.ItemPoolModelListener;

import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItinerary;


public class PlanElementProvider implements ItemPoolModelListener
{
    protected ClusterCache clusterCache;

    protected Hashtable UUIDPool;

    protected Hashtable unitManifests;

    private VectorHashtable callbacks;
    // this is common to all PEPs now that we will have multiple ones each with a filtered viewpoint
    private static Hashtable locationPool = new Hashtable();
    // top level tasks, one per gantt chart row or per fully-collapsed treetable row
    private long minTaskStart, maxTaskEnd;

    private static int itinCounter = 0; // kludge to create unique IDs for itineraries
    private static int clusterCounter = 0; // kludge to create unique IDs for itineraries
    private boolean cannedMode; // data source is from file; don't try to query clusters

    protected HashMap allNodes = new HashMap();
    private Node root = new Node(allNodes,TPFDDConstants.ROOTNAME); // used to be static
    
    public PlanElementProvider(ClusterCache clusterCache, boolean cannedMode)
    {
	this.clusterCache = clusterCache;
	this.cannedMode = cannedMode;
	UUIDPool = new Hashtable();
	callbacks = new VectorHashtable();
	minTaskStart = TimeSpan.MAX_VALUE;
	maxTaskEnd = TimeSpan.MIN_VALUE;
    }

    public HashMap getNodeMap() {
	return allNodes;
    }

    public Node getRoot()
    {
	return root;
    }

    // AlpineConsumer portion of ItemPoolModelListener interface
    // (this is for benefit of AlpineProducer which is unspecialized)
    public void fireAddition(Object item)
    {
	fireItemAdded(item);
    }

    public void fireDeletion(Object item)
    {
	fireItemDeleted(item);
    }

    public void fireChange(Object item)
    {
	fireItemValueChanged(item);
    }


    public void fireItemAdded(Object item)
    {
	OutputHandler.out("PEP:fIA base class called, ignoring.");
    }

    public void fireItemValueChanged(Object item)
    {
	if ( !(item instanceof LogPlanObject) ) {
	    OutputHandler.out("PEP:fIVC item of type " + item.getClass().getName() + ", ignoring.");
	    return;
	}
	LogPlanObject lpObject = (LogPlanObject)item;
	String UUID = lpObject.getUUID();
	// we receive items as Tasks, store them as Nodes
	LogPlanObject storedObject = (LogPlanObject)UUIDPool.get(UUID);
	if ( storedObject == null ) {
	    OutputHandler.out("PEP:fIVC attempt to change unknown object in hashtable keyed to " + UUID);
	    return;
	}

	try {
	    // Debug.out("PEP:fIVC update " + lpObject.getClass().getName() + " " + lpObject.getUUID());
	    storedObject.copyFrom(item);
	}
	catch ( MismatchException e ) {
	    OutputHandler.out("PEP:fIVC copyFrom failed: " + e);
	}
    }

    public void fireItemWithIndexDeleted(Object item, int index)
    {
	OutputHandler.out("PEP:fIWID: Error: UNIMPLEMENTED Should not ever be called.");
    }

    public void fireItemDeleted(Object item)
    {
	if ( !(item instanceof LogPlanObject) ) {
	    OutputHandler.out("PEP:fID item of type " + item.getClass().getName() + ", ignoring.");
	    return;
	}
	LogPlanObject lpObject = (LogPlanObject)item;
	String UUID = lpObject.getUUID();
	Node deletedNode = (Node)UUIDPool.remove(UUID);
	if ( deletedNode == null ) {
	    OutputHandler.out("PEP:fID attempt to delete unknown object from hashtable keyed to " + UUID);
	    return;
	}

	// need to fix up children pointers
	Node parentNode = (Node)(UUIDPool.get(deletedNode.getParent().getUUID()));
	parentNode.removeChild(deletedNode);

	// ??? What are semantics of task deletion? All children thereof
	// deleted, or do children get orphaned to toplevel task? Assuming former.
	deleteFromHash(deletedNode);
    }

    public void firingComplete()
    {
	// unused
    }

    // helper for fireItemDeleted
    private void deleteFromHash(Node node)
    {
	if ( node.hasChildren() )
	    for ( Iterator i = node.getChildren().iterator(); i.hasNext(); )
		deleteFromHash((Node)i.next());
	UUIDPool.remove(node);
    }

    private void handleNewItinerary(UITaskItinerary itinerary)
    {
	OutputHandler.out("PEP:hNI Warning: base class invocation ignored.");
    }

    private void handleLogPlan(LogPlanObject lpObject)
    {
	// Debug.out("PEP:hLP enter " + lpObject.getUUID());
	String UUID = lpObject.getUUID();
	if ( UUIDPool.get(UUID) != null )
	    return;
	
	if ( lpObject instanceof Node ) {
	    // important: work with a copy so every PEP uses a local copy, not a shared one
	    // from parent view or agg. server
	    handleNewNode((Node)lpObject);
	}
	else {
	    UUIDPool.put(UUID, lpObject);
	    Vector itemCallbacks;
	    if ( (itemCallbacks = callbacks.vectorGet(UUID)) != null ) {
		for ( int i = 0; i < itemCallbacks.size(); i++ ) {
		    // Debug.out("PEP:fIA callback " + i + " " + itemCallbacks.get(i) + " UUID " + UUID);
		    ((Callback)(itemCallbacks.get(i))).execute(lpObject);
		}
		callbacks.remove(UUID);
	    }
	}
    }

    protected void handleNewNode(Node node) {
	// Nodes come in with DBIDs for their parents and children
	// There is no need to connect them

	UUIDPool.put(node.getUUID(), node);
	allNodes.put(node.getUUID(), node);
    }


    public void proxyChangeNotify(Node node)
    {
	// no default action; see clientpep for details
    }
    
    public LogPlanObject warmRead(String key)
    {
	// Debug.out("PEP:wR check for " + key + ", result " + UUIDPool.get(key));
	return (LogPlanObject)(UUIDPool.get(key));
    }

    public int getChildCount(Node parent)
    {
	if ( !(parent instanceof Node) ) {
	    OutputHandler.out("PEP:gCC Error: parent of type " + parent.getClass());
	    return 0;
	}
	return ((Node)parent).getChildCount();
    }

    public Object getChild(Object parent, int index)
    {
	if ( index < 0 ) {
	    OutputHandler.out("PEP:gC Error: negative index: " + index + " "
			      + (parent == getRoot() ? "root" : parent));
	    return null;
	}
	if ( !(parent instanceof Node) ) {
	    OutputHandler.out("PEP:gC Error: parent of type " + parent.getClass().getName());
	    return null;
	}
	Node node = (Node)parent;
	int childCount = node.getChildCount();
	if ( index >= childCount ) {
	    OutputHandler.out("PEP:gC Error: out of bounds index " + index +
			      " for task children of count " + childCount);
	    return null;
	}
	return node.getChild(index);
    }

    public int getIndexOfChild(Object parent, Object child)
    {
	if ( !(parent instanceof Node) ) {
	    OutputHandler.out("PEP:gIOC Error: parent of type " + parent.getClass().getName());
	    return -1;
	}
	if ( !(child instanceof Node) ) {
	    OutputHandler.out("PEP:gIOC Error: child of type " + child.getClass().getName());
	    return -1;
	}
	return ((Node)parent).getChildren().indexOf((Node)child);
    }

    public int getIndexOfTopTaskOfTask(Node node)
    {
	// Debug.out("PEP:gIOTTOT " + this + " enter: " + node.getUUID());
	while ( node.getParent() != root ) {
	    if ( node.getParent() == null ) {
		OutputHandler.out("PEP:gIOTTOT " + this + " Error: parent of " + node.getUUID() + " is null!");
		return -1;
	    }
	    node = node.getParent();
	}
	return getIndexOfChild(root, node);
    }

    public long getMinTaskStart()
    {
	return minTaskStart;
    }

    public void setMinTaskStart(long anotherValue)
    {
	// Debug.out("PEP:sMTS aV " + anotherValue + " mTS " + minTaskStart);
	if ( anotherValue < minTaskStart && anotherValue > 0)
	    minTaskStart = anotherValue;
    }

    public long getMaxTaskEnd()
    {
	return maxTaskEnd;
    }

    public void setMaxTaskEnd(long anotherValue)
    {
	// Debug.out("PEP:sMTE aV " + anotherValue + " mTS " + maxTaskEnd);
	if ( anotherValue > maxTaskEnd )
	    maxTaskEnd = anotherValue;
    }

    public static Location getLocation(String code)
    {
	return (Location)(locationPool.get(code));
    }

    public static void setLocation(String code, Location location)
    {
	if ( locationPool.get(code) != null ) {
	    OutputHandler.out("PEP:sL Error: tried to put duplicate location: " + code);
	    return;
	}
	locationPool.put(code, location);
    }

    public Iterator getNodeIterator()
    {
	return UUIDPool.values().iterator();
    }

    public Hashtable getUnitManifests()
    {
	return unitManifests;
    }
}
