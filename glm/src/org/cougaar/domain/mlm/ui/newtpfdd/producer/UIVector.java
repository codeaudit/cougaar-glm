/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/producer/Attic/UIVector.java,v 1.2 2001-02-23 01:02:19 wseitz Exp $ */

/*
  portions Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.newtpfdd.producer;


import java.util.Vector;

import org.w3c.dom.Node;

import org.cougaar.domain.mlm.ui.newtpfdd.xml.*;

import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItinerary;


public class UIVector extends Vector
{
    public String[] toStrings()
    {
        String[] array = new String[size()];
	for ( int i = 0; i < size(); i++ )
	    array[i] = (String)(get(i));
	return array;
    }

    public static String[] toStrings(Object[] oArray)
    {
	String[] sArray = new String[oArray.length];
	for ( int i = 0; i < oArray.length; i++ )
	    sArray[i] = (String)(oArray[i]);
	return sArray;
    }

    public static String[] toStrings(Vector v)
    {
	String[] array = new String[v.size()];
	for ( int i = 0; i < v.size(); i++ )
	    array[i] = (String)(v.get(i));
	return array;
    }

    public static UITaskItinerary[] UITaskItineraryFrom(Vector v)
    {
	UITaskItinerary[] array = new UITaskItinerary[v.size()];
	for ( int i = 0; i < v.size(); i++ )
	    array[i] = (UITaskItinerary)v.get(i);
	return array;
    }

    public UITaskItinerary[] toUITaskItinerary()
    {
	UITaskItinerary[] array = new UITaskItinerary[size()];
	for ( int i = 0; i < size(); i++ )
	    array[i] = (UITaskItinerary)get(i);
	return array;
    }

    public LogPlanObject[] toLogPlanObject()
    {
	LogPlanObject[] array = new LogPlanObject[size()];
	for ( int i = 0; i < size(); i++ )
	    array[i] = (LogPlanObject)get(i);
	return array;
    }
	
public Node[] toNode()
    {
	Node[] array = new Node[size()];
	for ( int i = 0; i < size(); i++ )
	    array[i] = (Node)get(i);
	return array;
    }

}
