/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/util/Attic/Fifo.java,v 1.1 2000-12-15 20:17:47 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

package org.cougaar.domain.mlm.ui.tpfdd.util;

import java.util.NoSuchElementException;
import java.util.LinkedList;


public class Fifo
{
    private LinkedList internal;

    public Fifo()
    {  
	internal = new LinkedList();
    }

    public void enqueue(Object o)
    {
	boolean noGood = false;

	synchronized(internal) {
	    if ( internal.indexOf(o) == -1 )
		internal.addFirst(o);
	    else
		noGood = true;
	}
    }

    public Object dequeue()
    {
	Object o = null;
	try {
	    synchronized(internal) {
		o = internal.removeLast();
	    }
	}
	catch ( NoSuchElementException e ) {
	}
	return o;
    }
}
