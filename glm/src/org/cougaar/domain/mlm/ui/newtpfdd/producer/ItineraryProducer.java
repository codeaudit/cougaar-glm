/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/producer/Attic/ItineraryProducer.java,v 1.1 2001-02-22 22:42:31 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

package org.cougaar.domain.mlm.ui.tpfdd.producer;


import java.lang.InterruptedException;

import java.util.Random;
import java.util.Vector;
import java.util.Iterator;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.ExceptionTools;
import org.cougaar.domain.mlm.ui.tpfdd.util.PathString;

import org.cougaar.domain.mlm.ui.psp.society.SocietyUI;

import org.cougaar.domain.mlm.ui.psp.transportation.PSP_Itinerary;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UITAssetInfo;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItinerary;




public class ItineraryProducer extends ThreadedProducer
{
    private boolean allowOrg;
    
    /**
       Since we want to avoid congestion by network-blasting, in imitation of
       similar techniques in Ethernet et al., we wait a small but significant
       random (+/-10%?) wait time in each thread in addition to the constant
       interval specified.
    */
    private class PollThread extends Thread
    {
	private Random noise;
	private int interval;
	
	public PollThread(int interval)
	{
	    super();
	    noise = new Random();
	    this.interval = interval;
	}

	public void run()
	{
	    try {
		pollThreadProduction();
	    }
	    catch ( Exception e ) {
		OutputHandler.out(ExceptionTools.toString("IP:PT:run", e));
	    }
	    catch ( Error e ) {
		OutputHandler.out(ExceptionTools.toString("IP:PT:run", e));
	    }	    
	}

	private void pollThreadProduction()
	{
	    int noisyInterval;
	    
	    while ( true ) {
		synchronized(clusterCache) {
		    produce(PSP_Itinerary.LIVE);
		}

		noisyInterval = ((int)(1000 * interval * (noise.nextDouble() * 0.2 + 0.9)));
		
		synchronized(ItineraryProducer.this) {
		    try {
			ItineraryProducer.this.wait(noisyInterval);
		    }
		    catch ( Exception e ) {
			OutputHandler.out("IP:pT:pTP Error: " + ItineraryProducer.this.getName()
					  + " Unexpected Exception: " + e);
		    }
		}
	    }
	}
    }

    private PollThread pollingThread = null;

    public ItineraryProducer(String clusterName, ClusterCache clusterCache, boolean allowOrg)
    {
	super(clusterName + " Producer", clusterName, clusterCache);
	this.allowOrg = allowOrg;
	// override; SocietyUI wants to compose full URL itself
	url = "http://" + clusterCache.getHost() + ":5555";
    }

    public Object[] produce(Object request)
    {
	String mode = (String)request;
	//Debug.out("IP:produce " + targetCluster + " url: " + url + " enter");
	Vector itins;
	try {
	    itins = SocietyUI.getTaskItineraries(url, // String host_URL
		   targetCluster, // String cluster_name
		   null, // String transported_unit_name_filter
		   null, // input_task_uid_filter
		   mode, // String mode
		   false, // boolean interpolate
		   false, // boolean sort
		   true, // boolean includeUnload,
		   !allowOrg, // boolean ignoreOrgItineraryElements,
		   false, // ignoreCarrierItineraryElements,
		   false); // boolean useXML
	}
	catch ( RuntimeException e ) {
	    Debug.out("IP:produce Caught this exception: " + e);
	    OutputHandler.out(ExceptionTools.toString("IP:produce", e));
	    return null;
	}
	if ( itins == null ) {
	    OutputHandler.out("IP:produce Warning: " + targetCluster + " returned null.");
	    return null;
	}
	Vector expandedItins = new Vector(itins.size());
	for ( Iterator itinsIter = itins.iterator(); itinsIter.hasNext(); ) {
	    UITaskItinerary itin = (UITaskItinerary)(itinsIter.next());
	    Vector assetVector = itin.getUITAssetInfoVector();
	    // No FOR preposition information, so just pass on
	    if (itin.getTransportedUnitName() == null) {
	      expandedItins.add(itin);
	      continue;
	    }
	    if ( assetVector.size() == 1 ) {
		expandedItins.add(itin);
		String unitAndQuant = itin.getTransportedUnitName();
		if ( unitAndQuant.indexOf(':') != -1 )
		    itin.setTransportedUnitName(
		     unitAndQuant.substring(0, unitAndQuant.indexOf(':')));
		continue;
	    }

	    if ( itin.getTransportedUnitName().equals("UNKNOWN") ) {
		OutputHandler.out("IP:produce Warning: itin for " 
				  + itin.getClusterID() + "/" +
				  itin.getAllocTaskUID()
		     + "has transported unit name of UNKNOWN, tossing.");
		continue;
	    }

            itin.setUITAssetInfoVector(null);

	    Vector unitsAndQuants = 
	      PathString.split(itin.getTransportedUnitName(), ' ');
	    String unitNames[] = new String[unitsAndQuants.size()];
	    int quantities[] = new int[unitsAndQuants.size()];
	    for ( int i = 0; i < unitsAndQuants.size(); i++ ) {
		String uAQ = (String)(unitsAndQuants.get(i));
		if ( uAQ.indexOf(':') == -1 ) {
		    OutputHandler.out("IP:produce Errning: missing ':' " + 
			   "format in: " + itin.getTransportedUnitName());
		    unitNames[i] = uAQ;
		    quantities[i] = 1;
		}
		else {
		    unitNames[i] = PathString.dirname(uAQ, ':');
		    quantities[i] = Integer.parseInt(PathString.basename(uAQ, ':'));
		}
	    }
	    
	    // ignore the itinerary completely if sanity check fails.
	    if ( unitsAndQuants.size() != assetVector.size() ) {
		OutputHandler.out("IP:produce Error: got " + 
                  unitsAndQuants.size() + " unit names but "
		 + assetVector.size() + " asset elements; tossing itinerary.");
		continue;
	    }
	    
	    for ( int i = 0; i < unitsAndQuants.size(); i++ ) {
		UITAssetInfo assetInfo = (UITAssetInfo)assetVector.get(i);
		UITaskItinerary clonedItin = itin.copy();
		Vector singleAssetInfoVector = new Vector(1);
                singleAssetInfoVector.addElement(assetInfo);
		clonedItin.setUITAssetInfoVector(singleAssetInfoVector);
		clonedItin.setTransportedUnitName(unitNames[i]);
		if ( assetInfo.getQuantity() != quantities[i] )
		    OutputHandler.out("IP:produce Warning: actual: " + 
				      assetInfo.getQuantity()
				      + " and parsed: " + quantities[i] + 
				      " do not match for " + 
				      assetInfo + "/" + unitNames[i]);

                expandedItins.addElement(clonedItin);
	    }
	}
	UITaskItinerary[] itineraries = 
	  UIVector.UITaskItineraryFrom(expandedItins);

	if ( itineraries.length > 0 ) {
	    OutputHandler.out("IP:produce leave: From " + 
			      targetCluster + ": got "
			      + itineraries.length + " itineraries.");
	    addNotify(itineraries);
	}
	else
	    OutputHandler.out("IP:produce leave: From " + 
		      targetCluster + ": itineraries returned empty!");
	return itineraries;
    }

    public synchronized void beginPollingItineraries(int interval)
    {
	pollingThread = new PollThread(interval);
	try {
	    pollingThread.start();
	}
	catch ( Exception e ) {
	    OutputHandler.out(ExceptionTools.toString("IP:bPI", e));
	}
	catch ( Error e ) {
	    OutputHandler.out(ExceptionTools.toString("IP:bPI", e));
	}
    }

    public synchronized void forcePoll()
    {
	synchronized(pollingThread) {
	    if ( pollingThread == null ) {
		OutputHandler.out("IP:fP Error: tried to force poll when no thread active.");
		return;
	    }
	    try {
		notify();
	    }
	    catch ( Exception e ) {
		OutputHandler.out(ExceptionTools.toString("IP:fP", e));
	    }
	}
    }
}
