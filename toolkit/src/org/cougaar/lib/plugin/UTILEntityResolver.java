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

/**
 * EntityResolver for use with all UTIL xml parsers.
 *
 * Wraps config file finder and uses it to resolve (find) xml entities.
 *
 * Need to provide an entity resolver to the parser.
 * The entity resolver is just a wrapper of the configFinder.
 * The resolver is expected to resolve a systemID, where:
 *
 * systemID is of the form : file:/ferris/bueller/day
 * user.dir is             :      /ferris/bueller
 * and we want just the last part (day)
 * NOTE: on NT -->
 * systemID is of the form : file:/C:/ferris/bueller/day
 * user.dir is             :       C:/ferris/bueller
 * No slash in front of the path on the user.dir.
 *
 */
public class UTILEntityResolver implements EntityResolver {
  public InputSource resolveEntity (String publicId, String systemId)	{
    // systemID is of the form : file:/ferris/bueller/day
    // user.dir is             :      /ferris/bueller
    // and we want just the last part
    // NOTE: on NT -->
    // systemID is of the form : file:/C:/ferris/bueller/day
    // user.dir is             :      C:/ferris/bueller
    // No slash in front of the path on the user.dir
    String separator = System.getProperty ("file.separator");
    String currDir   = System.getProperty ("user.dir");
    if (!currDir.startsWith (separator))
      currDir = separator + currDir;
    // 5 for the file: and one for the trailing slash
    String fileName = systemId.substring (currDir.length()+6);
    //	System.out.println ("curr dir = " + currDir);
    //	System.out.println ("Asking config file finder for systemID = " + systemId);
    //	System.out.println ("Asking config file finder for file = " + fileName);

    // return a special input source
    try {
      return new InputSource(ConfigFinder.getInstance().open(fileName));
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.err.println ("UTILLdmXMLPlugIn.getParsedDocument - Could not find on config path");
      e.printStackTrace();
      return null;
    }
  }
}






