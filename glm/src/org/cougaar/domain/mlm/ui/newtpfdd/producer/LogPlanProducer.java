/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/producer/Attic/LogPlanProducer.java,v 1.2 2001-02-23 01:02:19 wseitz Exp $ */

 /*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

package org.cougaar.domain.mlm.ui.newtpfdd.producer;


import java.net.Socket;
import java.net.UnknownHostException;
    
import java.util.Vector;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import org.cougaar.domain.mlm.ui.newtpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.newtpfdd.util.MismatchException;
import org.cougaar.domain.mlm.ui.newtpfdd.util.ExceptionTools;
import org.cougaar.domain.mlm.ui.newtpfdd.util.OutputHandler;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.QueryData;


public class LogPlanProducer extends ThreadedProducer
{
    private boolean usingCluster;
    private Socket aggSocket;
    private String aggHost;
    private int port;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;

    public LogPlanProducer(String clusterName, ClusterCache clusterCache)
    {
	super(clusterName + " LogPlan Producer", clusterName, clusterCache);
	usingCluster = true;
    }

    public LogPlanProducer(String aggHost, int port, ClusterCache clusterCache)
    {
	super("Aggregation Producer", "Aggregation", clusterCache);
	this.aggHost = aggHost;
	this.port = port;
	usingCluster = false;
    }

    public void start()
    {
	try {
	    Debug.out("LPP:start Connecting to " + aggHost + " on " + port + "...");
	    aggSocket = new Socket(aggHost, port);
	    writer = new ObjectOutputStream(aggSocket.getOutputStream());
	    reader = new ObjectInputStream(aggSocket.getInputStream());
	    if ( writer != null && reader != null )
		Debug.out("LPP:start ...connected this: " + this + " writer: " + writer + ".");
	}
	catch ( UnknownHostException e ) {
	    OutputHandler.out("LPP:start Error: connection to Aggregation server failed: " + e);
	    writer = null;
	}
	catch ( IOException e ) {
	    OutputHandler.out("LPP:start Error: connection to Aggregation server failed: " + e);
	    writer = null;
	}
	super.start();
    }

    public Object[] sendAndGetXMLResponse(String query)
    {
	ConnectionHelper XMLClientConnection =
	new ConnectionHelper(url, PSPClientConfig.PSP_package, PSPClientConfig.UIDataPSP_id);
	try {
	    XMLClientConnection.sendData(query);
	    Object[] single = { XMLClientConnection.getResponse() };
	    return single;
	}
	catch ( IOException e ) {
	    OutputHandler.out("LPP:sAGR Error in XML connection: " + e);
	    return null;
	}
    }
    
    public Object[] sendAndGetResponse(QueryData query)
    {
	Debug.out("LPP:sAGR sending query:");
	Debug.out(query.toString());
	if ( writer == null ) {
	    OutputHandler.out("LPP:sAGR Error: attempt to contact Aggregation server "
			      + "while connection closed.");
	    return null;
	}
	Vector responses = new Vector();
	try {
	    writer.writeObject(query);
	    writer.flush();
	    Debug.out("LPP:sAGR wrote query");
	    while ( true ) {
		Object response = reader.readObject();
		if ( response == null )
		    break;
		responses.add(response);
	    }
	}
	catch ( Exception e ) {
	    OutputHandler.out("LPP:sAGR Error in aggregation query : " + e);
	    writer = null;
	    return null;
	}
	Debug.out("LPP:sAGR response: " + responses.size());
	// PEP should receive a wrapped object since it has its own optimized way
	// of looping over actual array
	Object[] response = { responses.toArray() };
	return response;
    }

    public Object[] produce(Object stuff)
    {
	QueryData query = (QueryData)stuff;

	if ( query == null ) {
	    OutputHandler.out("LPP:produce Error: received null query!");
	    return null;
	}

	Object[] replies = null;
	try {
	    OutputHandler.out("LPP:produce working on: " + targetCluster);
	    // OutputHandler.out(query.toString());
	    replies = sendAndGetResponse(query);
	} catch ( Exception e ) {
	    OutputHandler.out("LPP:produce Error: Could not connect to " + targetCluster);
	    return null;
	}
	if ( replies == null ) {
	    OutputHandler.out("LPP:produce Error: Empty reply to " + query);
	    return null;
	}

	addNotify(replies);
	return replies;
    }
	
    public Object[] produce(String query)
    {
	if ( query == null ) {
	    OutputHandler.out("LPP:produce Error: received null query!");
	    return null;
	}

	Object[] replies = null;
	try {
	    replies = sendAndGetXMLResponse(query);
	} catch ( Exception e ) {
	    OutputHandler.out("LPP:produce Error: Could not connect to " + targetCluster);
	    return null;
	}
	if ( replies == null ) {
	    OutputHandler.out("LPP:produce Error: Empty reply to " + query);
	    return null;
	}

	addNotify(replies);
	return replies;
    }

    public void cleanup()
    {
	super.cleanup();
	    try {
		if ( reader != null )
		    reader.close();
		if ( writer != null )
		    writer.close();
		if ( aggSocket != null )
		    aggSocket.close();
	    }
	    catch ( IOException e ) {
		OutputHandler.out(ExceptionTools.toString("LPP:cleanup", e));
	    }
    }
}
