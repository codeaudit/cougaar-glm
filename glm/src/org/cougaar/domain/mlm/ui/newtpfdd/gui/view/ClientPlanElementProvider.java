/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/gui/view/Attic/ClientPlanElementProvider.java,v 1.1 2001-02-22 22:42:25 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.gui.view;


import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;

import org.cougaar.domain.mlm.ui.tpfdd.util.ExceptionTools;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;

import org.cougaar.domain.mlm.ui.tpfdd.gui.model.SimpleRowModelProducer;
import org.cougaar.domain.mlm.ui.tpfdd.gui.model.SimpleItemPoolModelProducer;
import org.cougaar.domain.mlm.ui.tpfdd.gui.model.ItemPoolModelListener;
import org.cougaar.domain.mlm.ui.tpfdd.gui.model.RowModelListener;

import org.cougaar.domain.mlm.ui.tpfdd.producer.ClusterCache;
import org.cougaar.domain.mlm.ui.tpfdd.producer.PlanElementProvider;
import org.cougaar.domain.mlm.ui.tpfdd.producer.ThreadedProducer;
import org.cougaar.domain.mlm.ui.tpfdd.producer.PSPClientConfig;

import org.cougaar.domain.mlm.ui.tpfdd.TPFDDConstants;


public class ClientPlanElementProvider extends PlanElementProvider
{
    private SimpleRowModelProducer rowProducer;
    private SimpleItemPoolModelProducer itemProducer;
    private Hashtable localUUIDPool;

    private boolean isMaster;
    private boolean didMaster = false; // handle master TPFDD special case


//     Removed Filter from constructor
    public ClientPlanElementProvider(ClusterCache clusterCache,
				     boolean cannedMode, boolean isMaster)
    {
	super(clusterCache, cannedMode);
	this.isMaster = isMaster;
	localUUIDPool = new Hashtable();
	rowProducer = new SimpleRowModelProducer();
	itemProducer = new SimpleItemPoolModelProducer();
    }

    public void addRowConsumer(RowModelListener listener)
    {
	rowProducer.addConsumer(listener);
    }

    public void addItemPoolConsumer(ItemPoolModelListener listener)
    {
	itemProducer.addConsumer(listener);
    }

    public void fireItemAdded(Object item)
    {
	if ( item instanceof Object[] ) {
	    if ( ((Object[])item).length == 0 )
		return;
	    Object first = ((Object[])item)[0];
	    if ( first instanceof TaskNode )
		handleBatch((Object[])item);
	    else if ( first instanceof Hashtable ) {
		synchronized(this) {
		    unitManifests = (Hashtable)first;
		    notify();
		}
	    }
	    else {
		OutputHandler.out("CPEP:fIA item of type " + first.getClass().getName() + ", ignoring.");
	    }
	    return;
	}
	super.fireItemAdded(item);
    }
    
    // A TaskNode finds out internally it's changed; needs us to propagate
    public void proxyChangeNotify(TaskNode node)
    {
	int rootIndex = getIndexOfTopTaskOfTask(node);
	// Debug.out("PEP:pCN row of " + node + " is " + rootIndex);
	// Debug.out("PEP:pCN 0: " + UIRowMap.get(0));
	rowProducer.changeRowNotify(rootIndex);
	itemProducer.changeItemNotify(node);
    }
    
    public void handleBatch(Object[] nodes)
    {
	Debug.out("CPEP:hB nodes " + nodes);
	if ( nodes.length == 0 ) {
	    OutputHandler.out("PEP:hB Error: got empty array.");
	    return;
	}

	rowProducer.setSuspended(true);
	itemProducer.setSuspended(true);

	Vector newNodes = new Vector();
	boolean doneAnything = false;
	for ( int i = 0; i < nodes.length; i++ ) {
	    TaskNode node = (TaskNode)(nodes[i]);
// 	    Next two lines commented because Query request should have already handled this
// 	    if ( !filter.admits(node) )
// 		continue;
	    String UUID = node.getUUID();
	    if ( !isMaster ) {
		String localUUID = UUID.substring(0, UUID.indexOf('/'))
		    + UUID.substring(UUID.indexOf(": ") + 2, UUID.length());
                /*
                  // MIK: this comment fixes the infamous 10linesofcode buggo,
                  // which prevented the nodes from getting hooked up properly.
                  // perhaps there are negative side effects, but it looks pretty 
                  // good to me.
		if ( localUUIDPool.get(localUUID) != null )
		    continue;
                */
		localUUIDPool.put(localUUID, node);
	    }
	    if ( UUIDPool.get(UUID) != null )
		continue;
	    // important: work with a copy so every PEP uses a local copy, not a shared one
	    TaskNode copyNode = node.copy();
	    copyNode.reconstituteSerialized(this);
	    
	    doneAnything = true;
	    handleNewTaskNode(copyNode);
	    newNodes.add(copyNode);
	    if ( i % 250 == 0 )
		OutputHandler.out(String.valueOf(i), false, false);
	    else if ( i % 50 == 0 )
		OutputHandler.out(".", false, false);
	}
	OutputHandler.out("", false, true);
	if ( !doneAnything ) // no producers to notify if we are a server
	    return;

	OutputHandler.out("CPEP:hB firing batch update...");
	rowProducer.setSuspended(false);
	itemProducer.setSuspended(false);
	if ( isMaster ) {
	    if ( !didMaster ) {
		didMaster = true;
		for ( int i = 0; i < UIRowMap.size(); i++ )
		    rowProducer.addRowNotify(i);
	    }
	    else
		for ( int i = 0; i < UIRowMap.size(); i++ )
		    rowProducer.changeRowNotify(i);
	}
	else
	    for ( int i = 0; i < UIRowMap.size(); i++ )
		rowProducer.addRowNotify(i);
	for ( Iterator i = newNodes.iterator(); i.hasNext(); )
	    itemProducer.addItemNotify(i.next());

	rowProducer.firingComplete();
	itemProducer.firingComplete();
	OutputHandler.out("PEP:hB ...done firing.");
    }

    public void request(String name, String psp, QueryData query)
    {
	ThreadedProducer producer = null;
	if ( psp.equals(PSPClientConfig.UIDataPSP_id) )
	    producer = clusterCache.getLogPlanProducer(name, false);
	else {
	    OutputHandler.out("CPEP:request Error: bad PSP name: " + psp);
	    return;
	}
	if ( producer == null ) {
	    OutputHandler.out("CPEP:request Error: can't find producer: " + name);
	    return;
	}
	producer.addConsumer(this);
	producer.request(query);
    }

    public synchronized Hashtable getUnitManifests()
    {
	if ( unitManifests == null ) {
	    QueryData queryManifest = new QueryData();
	    queryManifest.setOtherCommand(TPFDDConstants.SEND_UNIT_MANIFESTS);
	    request("Aggregation", PSPClientConfig.UIDataPSP_id, queryManifest);
	}
	try {
	    wait(7000); // actual retrieval should notify us
	}
	catch ( Exception e ) {
	    OutputHandler.out(ExceptionTools.toString("CPEP:gUM", e));
	}
	Debug.out("CPEP:gUM: Returning unit manifests.");
	return unitManifests;
    }
}
