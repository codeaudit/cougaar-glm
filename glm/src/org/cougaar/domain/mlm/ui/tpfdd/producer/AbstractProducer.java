/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/producer/Attic/AbstractProducer.java,v 1.1 2000-12-15 20:17:47 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

package org.cougaar.domain.mlm.ui.tpfdd.producer;


import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;

import org.cougaar.domain.mlm.ui.tpfdd.gui.model.SimpleProducer;
import org.cougaar.domain.mlm.ui.tpfdd.gui.model.SimpleItemPoolModelProducer;

import org.cougaar.domain.mlm.ui.tpfdd.xml.LogPlanObject;

import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItinerary;



/**
  AbstractProducer is a lightweight abstract producer class.  Based on Nich Pioch's
  original code.  A producer is extremely simple, and only encapsulates the
  knowledge of where and how to talk to a cluster and transform the reply
  into objects useful to views and local datastores.

  An AbstractProducer is dedicated to a particular cluster; in the future, it will
  probably maintain long-lived connection state, and subscriptions to changes
  of desired objects (granularity of this is fuzzy to me right now.)

  The only query mode is synchronous, which blocks and returns objects in
  return value.  There are no start or stop methods, which are not meaningful
  in a non-threaded environment. See ThreadedAbstractProducer for addition of
  asynchronicity.

  The client is responsible for deciding what sort of query to send; the
  producer just passes it on and returns whatever the query generated.  A
  client is responsible for finding the corrent cluster to query.  Producers
  don't know where to go if the object is not found at their home cluster.

  Communication with views is through a listener registration similar
  to awt/swing.  Views and object managers spawn producers as needed,
  register as a listener of the producer by implementing the
  appropriate XXXListener interface and calling addConsumer, then
  call the producer's request() method to get the data.

  Data items are all handled by the rapidly complexifying PlanElementProvider.
*/

public abstract class AbstractProducer extends SimpleItemPoolModelProducer
{
    protected String targetCluster;
    protected String url; // url to access our cluster
    protected ClusterCache clusterCache;

    public AbstractProducer(String name, String clusterName, ClusterCache clusterCache)
    {
	super(name);
	targetCluster = clusterName;
	if ( clusterCache == null ) // producer might be local
	    return;
	this.clusterCache = clusterCache;
	if ( (url = clusterCache.getClusterURL(targetCluster)) == null )
	    OutputHandler.out("ALPP:ALPP Error: can't find URL for cluster " + clusterName + "!");
    }

    /**
     * Respond to a query synchronously; block until data received from cluster and
     * return in array to caller client.  Also fire onto bus for benefit of all consumers.
     */
    protected abstract Object[] produce(Object query);

    // This will get overridden by threaded producers which can handle it asynchronously.
    public void request(Object query)
    {
	produce(query);
    }

    public void start()
    {
	OutputHandler.out("ALPP:start Warning: called empty start() in AbstractProducer");
    }
    
    public String toString()
    {
        return name + " for " + targetCluster;
    }
}
