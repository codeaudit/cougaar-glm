/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/producer/Attic/PlanElementProvider.java,v 1.1 2000-12-15 20:17:48 mthome Exp $ */

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
import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;
import java.util.Date;

import org.cougaar.util.TimeSpan;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.tpfdd.util.MismatchException;
import org.cougaar.domain.mlm.ui.tpfdd.util.PathString;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.Callback;
import org.cougaar.domain.mlm.ui.tpfdd.util.VectorHashtable;

import org.cougaar.domain.mlm.ui.tpfdd.xml.LogPlanObject;
import org.cougaar.domain.mlm.ui.tpfdd.xml.Location;

import org.cougaar.domain.mlm.ui.tpfdd.gui.view.TaskFilter;
import org.cougaar.domain.mlm.ui.tpfdd.gui.view.TaskNode;

import org.cougaar.domain.mlm.ui.tpfdd.gui.model.ItemPoolModelListener;

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
    protected TreeMap UIRowMap;
    private static TaskNode root = new TaskNode();
    private long minTaskStart, maxTaskEnd;

    private static int itinCounter = 0; // kludge to create unique IDs for itineraries
    private static int clusterCounter = 0; // kludge to create unique IDs for itineraries
    protected TaskFilter filter;
    private boolean cannedMode; // data source is from file; don't try to query clusters
    
    public PlanElementProvider(ClusterCache clusterCache, TaskFilter filter, boolean cannedMode)
    {
	this.clusterCache = clusterCache;
	this.filter = filter;
	this.cannedMode = cannedMode;
	UUIDPool = new Hashtable();
	callbacks = new VectorHashtable();
	UIRowMap = new TreeMap(this);
	minTaskStart = TimeSpan.MAX_VALUE;
	maxTaskEnd = TimeSpan.MIN_VALUE;
    }

    public TaskNode getRoot()
    {
	return root;
    }

    public TreeMap getRowMap()
    {
	return UIRowMap;
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
	// we receive items as Tasks, store them as TaskNodes
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
	TaskNode deletedNode = (TaskNode)UUIDPool.remove(UUID);
	if ( deletedNode == null ) {
	    OutputHandler.out("PEP:fID attempt to delete unknown object from hashtable keyed to " + UUID);
	    return;
	}

	if ( UIRowMap.remove(deletedNode) == null ) {
	    // not a root task, need to fix up children pointers
	    TaskNode parentNode = (TaskNode)(UUIDPool.get(deletedNode.getParent_().getUUID()));
	    parentNode.removeChild(deletedNode);
	}
	// ??? What are semantics of task deletion? All children thereof
	// deleted, or do children get orphaned to toplevel task? Assuming former.
	deleteFromHash(deletedNode);
    }

    public void firingComplete()
    {
	// unused
    }

    // helper for fireItemDeleted
    private void deleteFromHash(TaskNode node)
    {
	if ( node.hasChildren() )
	    for ( Iterator i = node.getChildren_().values().iterator(); i.hasNext(); )
		deleteFromHash((TaskNode)i.next());
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
	
	if ( lpObject instanceof TaskNode ) {
	    // important: work with a copy so every PEP uses a local copy, not a shared one
	    TaskNode node = ((TaskNode)lpObject).copy();
	    node.reconstituteSerialized(this);
	    // from parent view or agg. server
	    handleNewTaskNode((TaskNode)node);
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

    protected void handleNewTaskNode(TaskNode node)
    {
	TaskNode possibleParent;
	if ( !node.getParentUUID().equals("!ROOT") 
	     && node.getParent_() == root 
	     && (possibleParent = ((TaskNode)(warmRead(node.getParentUUID())))) != null ) {
          node.setParent_(possibleParent);
        }

	UUIDPool.put(node.getUUID(), node);

	int rootIndex;
	if ( node.getParent_() == root ) {
	  // orphan nodes will be childrened into 'node'
	    Vector orphans = UIRowMap.putAndRemoveOrphans(node); 
	    if ( orphans != null ) {
		TaskNode orphan;
		int orphanIndex;
		for ( int i = 0; i < orphans.size(); i++ ) {
		    orphan = (TaskNode)(orphans.get(i));
		    orphanIndex = UIRowMap.indexOf(orphan);
		    if ( UIRowMap.remove(orphan) == null ) {
			OutputHandler.out("PEP:hNTN Error: removing non-existent orphan: " + orphan);
			continue;
		    }
		    node.addChild(orphan);
		    orphan.setParent_(node);
		}
	    }
	}
	else {
	    TaskNode parentNode = node.getParent_();
	    parentNode.addChild(node);
	    Vector orphans = UIRowMap.removeOrphansOf(node);
	    if ( orphans != null ) {
		for ( int i = 0; i < orphans.size(); i++ ) {
		    TaskNode orphan = (TaskNode)(orphans.get(i));
		    int orphanIndex = UIRowMap.indexOf(orphan);
		    if ( UIRowMap.remove(orphan) == null ) {
			OutputHandler.out("PEP:hNTN Error: removing non-existent orphan: " + orphan);
			continue;
		    }
		    node.addChild(orphan);
		    orphan.setParent_(node);
		}
	    }
	}
    }

    public void proxyChangeNotify(TaskNode node)
    {
	// no default action; see clientpep for details
    }
    
    public void remap(TaskNode node, long newActualStart)
    {
	boolean isThere = (UIRowMap.indexOf(node) != -1);
	if ( isThere )
	    UIRowMap.remove(node);
	node.setActualStartValue_(newActualStart);
	if ( isThere )
	    UIRowMap.putAndRemoveOrphans(node);
    }

    public void summon(String objectType, String location)
    {
	LogPlanProducer producer = clusterCache.getLogPlanProducer(location, false);

	producer.request(objectType);
    }

    public LogPlanObject warmRead(String key)
    {
	// Debug.out("PEP:wR check for " + key + ", result " + UUIDPool.get(key));
	return (LogPlanObject)(UUIDPool.get(key));
    }

    public void summon(String objectType, String key, String location, String field, Callback callback)
    {
	// Debug.out("PEP:summon enter " + objectType + "." + field + "=" + key);

	if ( field == null )
	    field = "UID";
	String query = objectType + "." + field + "=" + key;
	Object cacheLookup;
	if ( callback != null && field.equals("UID")
	     && (cacheLookup = UUIDPool.get(key)) != null ) {
	    callback.execute(cacheLookup);
	    // Debug.out("PEP:summon warm read for " + query);
	    return;
	}
	if ( cannedMode )
	    return;

	if ( location == null )
	    location = PathString.dirname(key);
	callbacks.put(key, callback);
	LogPlanProducer producer = clusterCache.getLogPlanProducer(location, false);
	if ( producer != null )
	    producer.request(query);
	// Debug.out("PEP:summon leave " + query);
    }

    public int getChildCount(TaskNode parent)
    {
	if ( parent == root ) {
	    int size = UIRowMap.size();
	    // Debug.out("PEP:gCC root ret " + size);
	    return size;
	}
	if ( !(parent instanceof TaskNode) ) {
	    OutputHandler.out("PEP:gCC Error: parent of type " + parent.getClass());
	    return 0;
	}
	return ((TaskNode)parent).getChildCount_();
    }

    public Object getChild(Object parent, int index)
    {
	if ( index < 0 ) {
	    OutputHandler.out("PEP:gC Error: negative index: " + index + " "
			      + (parent == getRoot() ? "root" : parent));
	    return null;
	}
	if ( parent == root ) {
	    if ( index >= UIRowMap.size() ) {
		OutputHandler.out("PEP:gC Error: out of bounds index " + index
				   + " for toplevel task set of size " + UIRowMap.size());
		return null;
	    }
	    // Debug.out("PEP:gC of root: i " + index + " UID " + ((TaskNode)UIRowMap.get(index)).getUUID());
	    return UIRowMap.get(index);
	}
	if ( !(parent instanceof TaskNode) ) {
	    OutputHandler.out("PEP:gC Error: parent of type " + parent.getClass().getName());
	    return null;
	}
	TaskNode node = (TaskNode)parent;
	int childCount = node.getChildCount_();
	if ( index >= childCount ) {
	    OutputHandler.out("PEP:gC Error: out of bounds index " + index +
			      " for task children of count " + childCount);
	    return null;
	}
	return node.getChild_(index);
    }

    public int getIndexOfChild(Object parent, Object child)
    {
	if ( parent == root ) {
	    return UIRowMap.indexOf((TaskNode)child);
	}
	if ( !(parent instanceof TaskNode) ) {
	    OutputHandler.out("PEP:gIOC Error: parent of type " + parent.getClass().getName());
	    return -1;
	}
	if ( !(child instanceof TaskNode) ) {
	    OutputHandler.out("PEP:gIOC Error: child of type " + child.getClass().getName());
	    return -1;
	}
	return ((TaskNode)parent).getChildren_().indexOf((TaskNode)child);
    }

    public int getIndexOfTopTaskOfTask(TaskNode node)
    {
	// Debug.out("PEP:gIOTTOT " + this + " enter: " + node.getUUID());
	while ( node.getParent_() != root ) {
	    if ( node.getParent_() == null ) {
		OutputHandler.out("PEP:gIOTTOT " + this + " Error: parent of " + node.getUUID() + " is null!");
		return -1;
	    }
	    node = node.getParent_();
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

    public Iterator getTaskNodeIterator()
    {
	return UUIDPool.values().iterator();
    }

    public Hashtable getUnitManifests()
    {
	return unitManifests;
    }
}
