/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/xml/Attic/ParseableImpl.java,v 1.3 2001-10-17 19:07:18 gvidaver Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.xml;


import java.lang.IllegalAccessException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.beans.PropertyDescriptor;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.cougaar.domain.mlm.ui.tpfdd.util.ExceptionTools;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.tpfdd.util.PathString;
import org.cougaar.domain.mlm.ui.tpfdd.util.Copiable;
import org.cougaar.domain.mlm.ui.tpfdd.util.BeanInfoProvider;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.CopiableImpl;

import org.cougaar.domain.mlm.ui.tpfdd.producer.UIVector;


public class ParseableImpl extends CopiableImpl implements Parseable
{
    private void init(Element xml)
    {
	System.err.println("Pars:Pars Error: initialization from XML not supported in this release.");
    }
    
    public ParseableImpl(Object target, Element xml)
    {
	super(target);
	if ( xml != null )
	    init(xml);
    }

    public String toXMLDocument()
    {
	return "<?xml version=\"1.0\"?><LogPlan>" + toXML() + "</LogPlan>";
    }

    public String toXML()
    {
	return toXML(false);
    }

    public String toXML(boolean newLines)
    {
	// Debug.out("PI:tXML target: " + target);
	String rep = "<object class=\"" + target.getClass().getName() + '"';
	if ( target instanceof LogPlanObject )
	    rep += " UID=\"" + ((LogPlanObject)target).getUUID() + '"';
	rep += '>';
	if ( newLines )
	    rep += "\n";
	PropertyDescriptor[] properties = BeanInfoProvider.getProperties(target.getClass());
	Object value = null;
	for ( int i = 0; i < properties.length; i++ ) {
	    try {
		Method readMethod = properties[i].getReadMethod();
		if ( readMethod == null ) {
		    OutputHandler.out("Warning: no read method for " + properties[i].getName());
		    continue;
		}
		value = readMethod.invoke(target, null);
	    }
	    catch ( IllegalAccessException e ) {
		OutputHandler.out(ExceptionTools.toString("Pars:toXML", e));
	    }
	    catch ( InvocationTargetException e ) {
		OutputHandler.out(ExceptionTools.toString("Pars:toXML", e));
	    }
	    if ( value == null || properties[i].getName().equals("class")
		 // minor kludge: all longs are dates, whose value of zero implies emptiness, so skip
		 || (properties[i].getPropertyType() == long.class && ((Long)value).longValue() == 0) )
		continue;
	    rep += "<field name=\"" + properties[i].getName() + "\" ";
	    if ( value.getClass().isArray() ) {
		rep += "collection type=\"";
		Object[] array = (Object [])value;
		Class type = array.getClass().getComponentType();
		if ( Parseable.class.isAssignableFrom(type) )
		    rep += "object";
		else
		    rep += type.getName();
		rep += "\">";
		if ( newLines )
		    rep += "\n";
		for ( int j = 0; j < array.length; j++ ) {
		    if ( Parseable.class.isAssignableFrom(type) )
			rep += ((Parseable)(array[j])).toXML();
		    else {
			rep += "<value>" + array[j].toString() + "</value>";
			if ( newLines )
			    rep += "\n";
		    }
		}
	    }
	    rep += "type=\"";
	    if ( value instanceof Parseable )
		rep += "object";
	    else
		rep += value.getClass().getName();
	    rep += "\">";
	    if ( value instanceof Parseable )
		rep += ((Parseable)value).toXML();
	    else
		rep += value.toString();
	    rep += "</field>";
	    if ( newLines )
		rep += "\n";
	}
	rep += "</object>";
	if ( newLines )
	    rep += "\n";
	return rep;
    }

    public String toURLQuery()
    {
	String rep = "?";
	PropertyDescriptor[] properties = BeanInfoProvider.getProperties(target.getClass());
	Object value = null;
	for ( int i = 0; i < properties.length; i++ ) {
	    try {
		Method readMethod = properties[i].getReadMethod();
		if ( readMethod == null ) {
		    OutputHandler.out("Warning: no read method for " + properties[i].getName());
		    continue;
		}
		value = readMethod.invoke(target, null);
	    }
	    catch ( IllegalAccessException e ) {
		OutputHandler.out(ExceptionTools.toString("Pars:tURLQ", e));
	    }
	    catch ( InvocationTargetException e ) {
		OutputHandler.out(ExceptionTools.toString("Pars:tURLQ", e));
	    }
	    if ( value == null || value.getClass().isArray() || properties[i].getName().equals("class") )
		continue;
	    if ( rep.length() > 1 ) // already has a parameter on it
		rep += "&";
	    rep += properties[i].getName() + "=";
	    if ( value instanceof Parseable )
		rep += ((Parseable)value).toURLQuery();
	    else
		rep += value.toString();
	}
	return rep;
    }
}
