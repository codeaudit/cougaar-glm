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

import java.io.*;
import org.cougaar.core.plugin.*;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.util.*;
import org.cougaar.lib.xml.parser.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.*;
import org.w3c.dom.*;

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
 * @see org.cougaar.lib.xml.parser.PrototypeProvider#cachePrototype
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





