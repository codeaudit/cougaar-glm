/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/producer/Attic/ThreadedProducer.java,v 1.2 2001-02-23 01:02:19 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

package org.cougaar.domain.mlm.ui.newtpfdd.producer;


import java.lang.InterruptedException;

import org.cougaar.domain.mlm.ui.newtpfdd.util.Fifo;
import org.cougaar.domain.mlm.ui.newtpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.newtpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.newtpfdd.util.ExceptionTools;


/**
   ThreadedProducer an Producer that can additionally produce
   objects asynchronously.
   
   New event-queueing model as of 10/25 which should be much more enduring
   as the system grows in complexity.
*/
public abstract class ThreadedProducer extends AbstractProducer
{
    private Fifo requests;
    private ServiceThread service;

    public ThreadedProducer(String name, String clusterName, ClusterCache clusterCache)
    {
      super(name, clusterName, clusterCache);
      requests = new Fifo();
    }
    
    public void start()
    {
	OutputHandler.out("TP:start " + name);
	synchronized(this) {
	    if ( service == null ) {
		service = new ServiceThread();
		try {
		    service.start();
		}
		catch ( Exception e ) {
		    OutputHandler.out(ExceptionTools.toString("TP:start", e));
		}
		catch ( Error e ) {
		    OutputHandler.out(ExceptionTools.toString("TP:start", e));
		}
	    }
	    else
		OutputHandler.out(this + ": service thread already started!");
	}
    }
    
    public void request(Object query)
    {
	requests.enqueue(query);
	// Debug.out("TP:request " + name + " enqueued:");
	// Debug.out(query.toString().substring(0, Math.min(query.toString().length(), 100)));
	synchronized(requests) {
	    requests.notify();
	}
	Debug.out("TP:request " + name + " notified requests");
    }

    private class ServiceThread extends Thread
    {
	public void run()
	{
	    try {
		runloop();
	    }
	    catch ( Exception e ) {
		OutputHandler.out(ExceptionTools.toString("TA:ST:run", e));
	    }
	    catch ( Error e ) {
		if ( e instanceof ThreadDeath )
		    OutputHandler.out("TA:ST:run " + Thread.currentThread().getName() + " says: Buh-bye!");
		else
		    OutputHandler.out(ExceptionTools.toString("TA:ST:run", e));
	    }	    
	}

	private void runloop()
	{
	    Object request;

	    Debug.out("TP:ST:run enter");
	    while ( true ) {
		// atomic test-and-set
		synchronized(requests) {
		    request = requests.dequeue();
		    if ( request == null ) {
			try {
			    Debug.out("TP:ST:run " + ThreadedProducer.this.getName()
				      + " entering wait");
			    requests.wait(); // releases synch automatically
			}
			catch ( InterruptedException e ) {
			    OutputHandler.out("TP:ST:run " + ThreadedProducer.this.getName()
					      + " wait interrupted? " + e);
			    continue;
			}
			Debug.out("TP:ST:run " + ThreadedProducer.this.getName()
				  + " requests awakened");
			request = requests.dequeue(); 
		    }
		}
		if ( request.toString().equals("terminate") ) {
		    Debug.out("TALLP:ST:run honoring request to self-terminate.");
		    return;
		}
		try {
		    produce(request);
		}
		catch ( Exception e ) {
		    OutputHandler.out(ExceptionTools.toString("TP:ST:run", e));
		}
	    }
	}
    }

    public void cleanup()
    {
	request("terminate");
	try {
	    if ( service != null )
		service.join();
	    else
		OutputHandler.out("TP:ST:Huh? Service thread already null.");
	}
	catch ( Exception e ) {
	    OutputHandler.out(ExceptionTools.toString("TP:ST:cleanup", e));
	}
    }
}

