/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/util/Attic/SwingQueue.java,v 1.1 2000-12-15 20:17:47 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

/**
 * extension/hack to ensure that runnables get called in the order they are invoked.
 * Warning: may be totally unnecessary.
 */

package org.cougaar.domain.mlm.ui.tpfdd.util;


import javax.swing.SwingUtilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.InterruptedException;


public class SwingQueue
{
    private static Fifo runQ = new Fifo();
    private static Runnable doNext = new Runnable() { public void run() { runNext(); } };
    
    public SwingQueue()
    {
	throw new Error("SQ:SQ SwingQueue is just a container for static methods");
    }

    public static void invokeLater(Runnable runnable)
    {
	runQ.enqueue(runnable);
	SwingUtilities.invokeLater(doNext);

    }

    public static void invokeAndWait(Runnable runnable)
    {
	if ( Thread.currentThread().getName().equals("AWT-EventQueue-0") ) {
	    OutputHandler.out("SQ:iAW Warning: called from own thread, illegal. Running directly.");
	    OutputHandler.out(ExceptionTools.stackToString("SQ:iAW"));
	    runnable.run();
	    return;
	}
	try {
	    SwingUtilities.invokeAndWait(runnable);
	}
	catch ( InvocationTargetException e ) {
	    OutputHandler.out("SQ:iAW Error: Unexpected exception: " + e);
	}
	catch ( InterruptedException e ) {
	    OutputHandler.out("SQ:iAW Error: Unexpected exception: " + e);
	}
	catch ( Throwable e ) {
	    OutputHandler.out("SQ:iAW Error: Really unexpected exception: " + e);
	}
    }

    private static void runNext()
    {
	try {
	    Runnable next = (Runnable)(runQ.dequeue());
	    if ( next != null )
		next.run();
	}
	catch ( Exception e ) {
	    OutputHandler.out(ExceptionTools.toString("SQ:rN", e));
	}
	catch ( Error e ) {
	    OutputHandler.out(ExceptionTools.toString("SQ:rN", e));
	}	    
    }
}
