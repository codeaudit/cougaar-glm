/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.plugin;

import org.cougaar.util.ConfigFinder;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import java.net.URL;

/**
 * EntityResolver for use with all UTIL xml parsers.
 *
 * Wraps config file finder and uses it to resolve (find) xml entities.
 *
 * Need to provide an entity resolver to the parser.
 * The entity resolver is just a wrapper of the configFinder.
 * The resolver is expected to resolve a systemID, where:
 *
 *
 */
public class UTILEntityResolver implements EntityResolver {
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
      return new InputSource(ConfigFinder.getInstance().open(filename));
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.err.println ("UTILLdmXMLPlugIn.getParsedDocument - Could not find on config path");
      e.printStackTrace();
      return null;
    }
  }
}






