/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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





