/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/xml/Attic/UIParser.java,v 1.2 2001-02-23 01:02:24 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.newtpfdd.xml;

import java.io.ByteArrayInputStream;

import com.ibm.xml.parser.Parser;
import com.ibm.xml.parser.TXDocument;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.cougaar.domain.mlm.ui.newtpfdd.util.MismatchException;
import org.cougaar.domain.mlm.ui.newtpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.newtpfdd.util.Debug;

import org.cougaar.domain.mlm.ui.newtpfdd.producer.UIVector;
import org.cougaar.domain.mlm.ui.newtpfdd.producer.PlanElementProvider;


public class UIParser
{
    protected String tagName;
    protected Node[] nodes;
    protected Element element; // used between parse_one() and getOne/Allxxx()
    private PlanElementProvider provider;

    public UIParser(byte[] buffer, String tagName, PlanElementProvider provider) throws MismatchException
    {
	Parser parser = new Parser("file:///");
	TXDocument document = parser.readStream(new ByteArrayInputStream(buffer));
	String version = document.getVersion();
	if ( version == null || !version.equals("1.0") ) {
	    String error = "UIParser:UIParser bad XML (version " + version + ")";
	    throw new MismatchException(error);
	}
	element = document.getDocumentElement();
	nodes = getAllNodesByTag(tagName);
	this.tagName = tagName;
	this.provider = provider;
    }
		
    public UIParser(Node[] nodes)
    {
	this.nodes = nodes;
    }    
		
    protected Node[] getAllNodesByTag(String tagName)
    {
	NodeList potentials = element.getChildNodes();
	UIVector values = new UIVector();
				
	if ( potentials == null ) {
	    /* OutputHandler.out("error: empty parent: " + element.getTagName()
	       + " while looking for: " + tagName + "!"); */
	    return null;
	}
	for ( int i = 0; i < potentials.getLength(); i++ )
	if ( (potentials.item(i)) instanceof Element ) {
	    String name = ((Element)(potentials.item(i))).getTagName();
	    if ( tagName.equals("all") || name.equals(tagName) )
	        values.add(potentials.item(i)); }
	return values.toNode();
    }
    
    protected String[] getAllValuesByTag(String tagName)
    {
	NodeList potentials = element.getChildNodes();
	UIVector values = new UIVector();
				
	if ( potentials == null ) {
	    /* OutputHandler.out("UIP:gOVBT Error: empty parent: " + element.getTagName()
		    + " while looking for: " + tagName + "!"); */
	    return null;
	}
	for ( int i = 0; i < potentials.getLength(); i++ )
	if ( (potentials.item(i)) instanceof Element 
	     && ((Element)(potentials.item(i))).getTagName().equals(tagName) )
	    values.add(potentials.item(i).getFirstChild().getNodeValue());
	
	return values.toStrings();
    }
    
    protected String getOneValueByTag(String tagName)
    {
	NodeList potentials = element.getChildNodes();
	Node theElement = null;
	if ( potentials == null ) {
	    /* OutputHandler.out("UIP:goVBT Error: empty parent: " + element.getTagName()
		    + " while looking for: " + tagName + "!"); */
	    return null;
	}
	for ( int i = 0; i < potentials.getLength(); i++ )
	    if ( (potentials.item(i)) instanceof Element
		 && ((Element)(potentials.item(i))).getTagName().equals(tagName) )
		if ( theElement != null )
		    OutputHandler.out("UIP:gOVBT Warning: duplicate tag in: " + element.getTagName()
				       + " while looking for: " + tagName + ".");
		else
		    theElement = potentials.item(i).getFirstChild();
	
	if ( theElement == null ) {
	    /* OutputHandler.out("UIP:gOVBT Error: no such tag in: " + element.getTagName()
	       + " while looking for: " + tagName); */
	    return null; }
	return theElement.getNodeValue();
    }


    public UIVector parse()
    {
	UIVector UIObjects = new UIVector();

	try {
	    for ( int i = 0; i < nodes.length; i++ ) {
		element = (Element)nodes[i];
		UIObjects.add(parse_one());
	    }
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	return UIObjects;
    }

    public Object parse_one()
    {
	Object object = UIObjectFactory.create(element, provider);
	return object;
    }
}
