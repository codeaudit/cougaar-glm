package org.cougaar.mlm.plugin.xml;

/***************************************************************
 *                                                              
 *  Copyright 1997-1999 Defense Advanced Research Projects 
 *  Agency and ALPINE (A BBN Technologies (BBN) and Raytheon 
 *  Systems Company (RSC) Consortium). This software to be used 
 *  in accordance with the COUGAAR license agreement.                       
 *                                                              
 ****************************************************************
 **/


import org.cougaar.core.plugin.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;
import org.w3c.dom.*;
import org.cougaar.lib.xml.parser.*;
import java.io.*;

/**
 * This plugin is a prototype provider that attempts to supply
 * prototypes by looking for XML files by name. It finds the prototype
 * file by using the ConfigFileFinder mechanism, and parses it using the
 * 'contrib' TOPS XML Parser. If successful, it will cache and return the
 * generated prototype.
 * NOTE : The name of the prototype is modified to 
 * change '/' characters to '-' characters to allow for O/S compatibility:
 * Example : Prototype "NSN/1234567890123" looks for a 
 * file NSN-1234567890123.xml
 * 
 **/
public class XMLPrototypeProviderPlugIn extends SimplePlugIn
implements PrototypeProvider
{

  // While this is a plugin, we only expect it to operate 
  // as a prototype provider
  public void setupSubscriptions() 
  {
    // System.out.println("Loading XMLPrototypeProviderPlugIn...");
  }

  public void execute() 
  {
  }

  // Interface required for PrototypeProvider
  // If possible, Create a prototype from XML file given by name
  // <aTypeName>.xml and cache and return prototype
  // Otherwise, return null
  public Asset getPrototype(String aTypeName, Class anAssetClassHint)
  {
    //    System.out.println("XMLPrototypeProviderPlugIn.getPrototype : " + 
    //		       aTypeName + " " + anAssetClassHint);

    Asset new_prototype = null;
    Node node = null;

    // Find the file named 'aTypeName'.xml and parse it
    // Using ConfigFileFinder
    String filename = cleanupPrototypeName(aTypeName) + ".xml";
    try {
      Document doc = ConfigFileFinder.parseXMLConfigFile(filename);
      if (doc != null) {
	node = doc.getDocumentElement();
      }
    } catch (FileNotFoundException fnfe) {
    } catch (IOException ie) {
      System.out.println("Exception parsing prototype file : " + filename);
      ie.printStackTrace();
    }

    // Create a prototype from the parsed node
    // Using TOPS contrib XML Asset parser
    if (node != null) {
      new_prototype = PrototypeParser.cachePrototype(getLDM(), node, true);
      if (new_prototype != null) {
	getLDM().cachePrototype(aTypeName, new_prototype);
	//System.out.println("XMLPrototypeProviderPlugIn : " +
  //	   "Caching prototype for " + aTypeName);
	getLDM().fillProperties(new_prototype);
      }
    }

    // Return newly constructed prototype (or null, if none found)
    return new_prototype;
  }

  // Alter prototype string to change '/' to '-'
  private String cleanupPrototypeName(String prototype)
  {
    return prototype.replace('/', '-');
  }


}
