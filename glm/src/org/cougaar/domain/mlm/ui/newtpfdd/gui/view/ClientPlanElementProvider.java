/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/gui/view/Attic/ClientPlanElementProvider.java,v 1.4 2001-02-24 21:47:40 gvidaver Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;


import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;

import org.cougaar.domain.mlm.ui.newtpfdd.util.ExceptionTools;
import org.cougaar.domain.mlm.ui.newtpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.newtpfdd.util.Debug;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.model.SimpleRowModelProducer;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.model.SimpleItemPoolModelProducer;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.model.ItemPoolModelListener;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.model.RowModelListener;

import org.cougaar.domain.mlm.ui.newtpfdd.producer.ClusterCache;
import org.cougaar.domain.mlm.ui.newtpfdd.producer.PlanElementProvider;
import org.cougaar.domain.mlm.ui.newtpfdd.producer.ThreadedProducer;
import org.cougaar.domain.mlm.ui.newtpfdd.producer.PSPClientConfig;

import org.cougaar.domain.mlm.ui.newtpfdd.TPFDDConstants;


public class ClientPlanElementProvider extends PlanElementProvider
{
    private SimpleRowModelProducer rowProducer;
    private SimpleItemPoolModelProducer itemProducer;

//     Removed Filter from constructor
    public ClientPlanElementProvider(ClusterCache clusterCache, boolean cannedMode)
    {
	super(clusterCache, cannedMode);
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
	    if ( first instanceof Node )
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
    
    // A Node finds out internally it's changed; needs us to propagate
    public void proxyChangeNotify(Node node)
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
	    Node node = (Node)(nodes[i]);
	    String UUID = node.getUUID();
	    if ( UUIDPool.get(UUID) != null )
		continue;
	    doneAnything = true;
	    handleNewNode(node);
	    newNodes.add(node);
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

	for ( Iterator i = newNodes.iterator(); i.hasNext(); )
	    itemProducer.addItemNotify(i.next());

	rowProducer.firingComplete();
	itemProducer.firingComplete();
	OutputHandler.out("PEP:hB ...done firing.");
    }

    public void request(String name, String psp, QueryData query)
    {
	ThreadedProducer producer = null;
	if ( psp.equals(PSPClientConfig.UIDataPSP_id) ) {
  	  //	    producer = clusterCache.getLogPlanProducer(name, false);
	  producer = (ThreadedProducer) clusterCache.getProducer(name, false);
	} else {
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
