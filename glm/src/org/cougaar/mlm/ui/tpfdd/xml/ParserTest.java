/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/ParserTest.java,v 1.1 2001-12-27 22:44:36 bdepass Exp $ */

/*
  Copyright (C) 1998-1999 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.mlm.ui.tpfdd.xml;


import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Enumeration;

import org.cougaar.mlm.ui.tpfdd.util.MismatchException;

import org.cougaar.mlm.ui.tpfdd.producer.UIVector;


public class ParserTest
{
    public static void main(String argv[])
    {
	if ( argv.length != 1 ) {
	    System.err.println("Usage: parser <filename>\n");
	    return;
	}
	String filename = argv[0];
	File file = new File(filename);
	int length;

	length = (int)file.length();
	char[] file_buffer = new char[length];
	
	try {
	    FileReader reader = new FileReader(filename);
	    BufferedReader breader = new BufferedReader(reader);
	    breader.read(file_buffer, 0, length);
	} catch (Exception e) {
	    System.err.println(e);
	    return;
	}
	String xmlString = new String(file_buffer);
	UIParser child = null;
	child = new UIParser(xmlString.getBytes(), "object", null);
	UIVector results = null;
	try {
	    results = child.parse();
	    for ( Enumeration e = results.elements(); e.hasMoreElements(); ) {
		Object o = e.nextElement();
		System.out.println(o.toString());
		System.out.println(((Parseable)o).toXML());
	    }
	}
	catch (Exception e) {
	    System.err.println(e);
	}
    }
}
