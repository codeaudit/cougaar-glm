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

package org.cougaar.lib.plugin;

import java.net.URL;

import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * <pre>
 * EntityResolver for use with all UTIL xml parsers.
 *
 * Wraps config file finder and uses it to resolve (find) xml entities.
 *
 * Need to provide an entity resolver to the parser.
 * The entity resolver is just a wrapper of the configFinder.
 * The resolver is expected to resolve a systemID, where:
 *
 * </pre>
 */
public class UTILEntityResolver implements EntityResolver {

  protected Logger logger;
  public UTILEntityResolver (Logger logger) { this.logger = logger; }

  /** 
   * Set org.cougaar.lib.plugin.UTILEntityResolver.debug to true to see debug info. <p>
   * Will say what file is getting read in...
   **/
  public InputSource resolveEntity (String publicId, String systemId)	{
    URL url = null;

    try {
      url = new URL(systemId);
    } catch(Exception e) {}

    String filename = url.getFile();

    filename = filename.replace('\\', '/');
    filename = filename.substring(filename.lastIndexOf("/")+1, filename.length());

    // return a special input source
    try {
      InputSource is = new InputSource(ConfigFinder.getInstance().open(filename));
      if (logger.isDebugEnabled())
	logger.debug ("UTILEntityResolver.resolveEntity - publicID " + 
		      publicId + " system ID " + 
		      systemId + " filename from systemID " + 
		      filename + " input source " + is);

      return is;
    } catch (Exception e) {
      logger.error ("UTILLdmXMLPlugin.getParsedDocument - Could not find on config path", e);
      return null;
    }
  }
}
