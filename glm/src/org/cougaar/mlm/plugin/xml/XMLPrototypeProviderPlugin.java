/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.mlm.plugin.xml;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.cougaar.core.service.LoggingService;
import org.cougaar.lib.xml.parser.PrototypeParser;
import org.cougaar.planning.ldm.PrototypeProvider;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * <pre>
 * This plugin is a prototype provider that attempts to supply
 * prototypes by looking for XML files by name. It finds the prototype
 * file by using the ConfigFinder mechanism, and parses it using the
 * toolkit prototype XML Parser. If successful, it will cache and return the
 * generated prototype.
 * NOTE : The name of the prototype is modified to 
 * change '/' characters to '-' characters to allow for O/S compatibility:
 * Example : Prototype "NSN/1234567890123" looks for a 
 * file NSN-1234567890123.xml
 * 
 * </pre>
 * @see org.cougaar.lib.xml.parser.PrototypeParser#cachePrototype
 * @see org.cougaar.util.ConfigFinder#parseXMLConfigFile
 **/
public class XMLPrototypeProviderPlugin extends SimplePlugin
implements PrototypeProvider
{
  /** 
   * Rely upon load-time introspection to set these services - 
   * don't worry about revokation.
   */
  public final void setLoggingService(LoggingService bs) {  
    logger = bs; 
    prototypeParser = new PrototypeParser (logger);
  }

  /**
   * Get the logging service, for subclass use.
   */
  protected LoggingService getLoggingService() {  return logger; }

  /**
   * While this is a plugin, we only expect it to operate 
   * as a prototype provider.  No subscriptions are made here.
   */
  public void setupSubscriptions() 
  {
    // System.out.println("Loading XMLPrototypeProviderPlugin...");
  }

  /** no subscriptions -- nothing happens here */
  public void execute() 
  {
  }

  /**
   * <pre>
   * Interface required for PrototypeProvider
   * If possible, Create a prototype from XML file given by name
   * <aTypeName>.xml and cache and return prototype
   * Otherwise, return null
   * </pre>
   * @param aTypeName name of the Prototype to create
   * @param ignoredAnAssetClassHint class hint not used, since we read from a file
   * @return asset prototype if we can find and parse the file, null otherwise
   */
  public Asset getPrototype(String aTypeName, Class ignoredAnAssetClassHint)
  {
    Asset new_prototype = null;
    Node node = null;

    // Find the file named 'aTypeName'.xml and parse it
    // Using ConfigFileFinder
    String filename = cleanupPrototypeName(aTypeName) + ".xml";
    try {
      Document doc = getConfigFinder().parseXMLConfigFile(filename);
      if (doc != null) {
	node = doc.getDocumentElement();
      }
    } catch (FileNotFoundException fnfe) {
    } catch (IOException ie) {
      logger.error("Exception parsing prototype file : " + filename, ie);
    }

    // Create a prototype from the parsed node
    // Using toolkit XML Asset parser
    if (node != null) {
      new_prototype = prototypeParser.cachePrototype(getLDM(), node, true);
      if (new_prototype != null) {
	getLDM().cachePrototype(aTypeName, new_prototype);
	if (logger.isDebugEnabled())
	  logger.debug(".getPrototype : Caching prototype for " + aTypeName);
	getLDM().fillProperties(new_prototype);
      }
    }

    // Return newly constructed prototype (or null, if none found)
    return new_prototype;
  }

  /** Alter prototype string to change '/' to '-' */
  private String cleanupPrototypeName(String prototype)
  {
    return prototype.replace('/', '-');
  }

  protected LoggingService logger;
  protected PrototypeParser prototypeParser;
}





