/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/producer/Attic/TreeMap.java,v 1.1 2000-12-15 20:17:48 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.producer;


import java.util.Vector;
import java.util.Collection;

import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.VectorHashtable;
import org.cougaar.domain.mlm.ui.tpfdd.util.IndexedTreeMap;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;

import org.cougaar.domain.mlm.ui.tpfdd.gui.view.TaskNode;


public class TreeMap
{
    public static final int TREEMAP_SORT_STATE_FIRST = 24;
    public static final int TREEMAP_SORT_BY_START_DATE_THEN_NAME = 24;
    public static final int TREEMAP_SORT_BY_END_DATE_THEN_NAME = 25;
    public static final int TREEMAP_SORT_BY_NAME = 26;
    public static final int TREEMAP_SORT_BY_START_DATE = 27;
    public static final int TREEMAP_SORT_BY_END_DATE = 28;
    public static final int TREEMAP_SORT_STATE_LAST = 28;
    
    public static final String[] sortNames =
    { "by Name, Start Date",
      "by Name, End Date",
      "by Start Date, Name",
      "by End Date, Name",
      "by Name",
      "by Start Date",
      "by End Date" };

    private int sortState = TREEMAP_SORT_BY_START_DATE_THEN_NAME;
    private VectorHashtable missingParents;
    private IndexedTreeMap treeMap;
    private PlanElementProvider provider;

    public TreeMap(PlanElementProvider provider)
    {
	this.provider = provider;
	missingParents = new VectorHashtable();
	treeMap = new IndexedTreeMap();
    }

    public synchronized void setSortState(int state)
    {
	if ( state < TREEMAP_SORT_STATE_FIRST || state > TREEMAP_SORT_STATE_LAST ) {
	    OutputHandler.out("ALPTM:sSS Errning: invalid sort state: " + state);
	    return;
	}
	TaskNode objs[] = new TaskNode[treeMap.size()];
	for ( int i = 0; i < treeMap.size(); i++ ) {
	    TaskNode node = (TaskNode)(get(i));
	    objs[i] = node;
	    treeMap.remove(node);
	}
	sortState = state;
	for ( int i = 0; i < objs.length; i++ )
	    putAndRemoveOrphans(objs[i]);
    }

    private Comparable getKey(TaskNode node)
    {
	Comparable key = null;
	
	switch ( sortState ) {
	case TREEMAP_SORT_BY_START_DATE_THEN_NAME:
	    key = new DateIDKey(node.getActualStart(), node.getUUID());
	    break;
	case TREEMAP_SORT_BY_END_DATE_THEN_NAME:
	    key = new DateIDKey(node.getActualEnd(), node.getUUID());
	    break;
	case TREEMAP_SORT_BY_NAME:
	    key = new IDKey(node.getUUID());
	    break;
	case TREEMAP_SORT_BY_START_DATE:
	    key = new DateKey(node.getActualStart());
	    break;
	case TREEMAP_SORT_BY_END_DATE:
	    key = new DateKey(node.getActualEnd());
	    break;
	}
	return key;
    }
		
    public synchronized int indexOf(TaskNode node)
    {
	Comparable key = getKey(node);
	return treeMap.indexOf(key);
    }

    public synchronized Object get(int i)
    {
	return treeMap.get(i);
    }

    public synchronized Vector putAndRemoveOrphans(TaskNode node)
    {
	
	Comparable key = getKey(node);
	treeMap.put(key, node);
	//Debug.out("ALPTM:put " + node.getUUID() + " at row " + indexOf(node) + " with key '" + key + "'");

	// our parent might be missing and that's why were added to root
	if ( !node.getParentUUID().equals("!ROOT") && 
	     node.getParent_() == provider.getRoot() ) {
	  //Debug.out("ALPTM:put parent " + node.getParentUUID() 
	  //+ " missing from " + node.getUUID());
	  missingParents.vectorPut(node.getParentUUID(), node);
	}
	return removeOrphansOf(node);
    }

    public synchronized Vector removeOrphansOf(TaskNode node)
    {
	Vector orphans = missingParents.vectorGet(node.getUUID());
	if ( orphans != null ) {
	    missingParents.remove(node.getUUID());
	    // System.out.println("orphans size = " + orphans.size());
	}
	//System.out.println("orphans is null");
	return orphans;
    }

    public synchronized Object remove(TaskNode node)
    {
	Comparable key = getKey(node);
	int rowNum = treeMap.indexOf(key);
	// Debug.out("ALPTM:remove " + node.getUUID() + " from row " + rowNum + " with key '" + dateKey + "'");
	TaskNode removedNode = (TaskNode)(treeMap.remove(key));
	if ( removedNode == null || removedNode != node ) {
	    OutputHandler.out("ALPTM: Warning: "
			      + (node != null ? node.getUUID() : "[null node]")
			      + ": "
			      + (removedNode != null ? removedNode.getUUID() : "[null removedNode]"));
	    return removedNode;
	}
	return node;
    }

    public synchronized int size()
    {
	return treeMap.size();
    }

    public synchronized Collection values()
    {
	return treeMap.values();
    }

    public synchronized void dump()
    {
	for ( int i = 0; i < treeMap.size(); i++ )
	    Debug.out("ALPTM:dump " + i + " " + ((TaskNode)treeMap.get(i)).getUUID());
    }

}
