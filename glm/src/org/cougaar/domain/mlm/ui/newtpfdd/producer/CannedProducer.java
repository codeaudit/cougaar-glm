/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/producer/Attic/CannedProducer.java,v 1.1 2001-02-22 22:42:30 wseitz Exp $ */

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

import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.IOException;


import org.cougaar.domain.mlm.ui.tpfdd.util.ExceptionTools;
import org.cougaar.domain.mlm.ui.tpfdd.util.MismatchException;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.tpfdd.util.PathString;

import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItineraryElementOrg;


public class CannedProducer extends ThreadedProducer
{
    private FileInputStream file;
    private String fileName;
    private PlanElementProvider provider;
    private ObjectInputStream reader;
    private boolean bad = false;
    private Vector allowOrgItinsFrom;
    
    public CannedProducer(String fileName, PlanElementProvider provider, Vector allowOrgItinsFrom)
    {
	super("Canned objects from " + fileName, null, null);
	this.fileName = fileName;
	this.provider = provider;
	this.allowOrgItinsFrom = allowOrgItinsFrom;
	try {
	    file = new FileInputStream(fileName);
	    reader = new ObjectInputStream(file);
	}
	catch ( Exception e ) {
	    OutputHandler.out(ExceptionTools.toString("CP:CP", e));
	    bad = true;
	}
    }

    public boolean isBad()
    {
	return bad;
    }

    public Object[] produce(Object unused)
    {
	Vector victor = new Vector();
	OutputHandler.out("CP:produce " + fileName + " enter");

	try {
	    while ( true ) {
		Object o = reader.readObject();
		if ( o == null )
		    break;
		victor.add(o);
	    }
	}
	catch ( Exception e ) {
	    OutputHandler.out("CP:produce Error: " + e);
	    return null;
	}

	if ( victor.size() > 0 )
	    OutputHandler.out("CP:produce Reconstituted " + victor.size() + " canned objects.");
	else {
	    OutputHandler.out("CP:produce leave: From " + targetCluster + ": canned returned empty!");
	    return null;
	}
	Object[] response = { victor.toArray() };
	addNotify(response);
	return response;
    }
}
