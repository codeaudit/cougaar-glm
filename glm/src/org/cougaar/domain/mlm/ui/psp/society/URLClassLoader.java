/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.society;
 /**
   *  THIS IS OBSOLETE W/ JAVA 1.2 .  This implementation was used
   *  w/ JDK 1.1 VERSION OF LPS.
   *
   *  LOG PLAN SERVER now uses  java.net.URLClassLoader for URL based
   *  class loading.
   *
   *  java.net.URLClassLoader has proven to be more
   *  robust than this implementation.
   *
   *  EXAMPLE CODE ONLY.   
   **/

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;

/**
 * Loads class bytes from a URL
 */
public class URLClassLoader extends MultiClassLoader {

//---------- Private Fields ------------------------------
private String    urlString;

//---------- Initialization ------------------------------
public URLClassLoader(String urlString) {
    this.urlString = urlString;
}
//---------- Abstract Implementation ---------------------
protected byte[] loadClassBytes(String className) {

    className = formatClassName(className);
    try {
        URL url = new URL(urlString + className);
        URLConnection connection = url.openConnection();
        if (sourceMonitorOn) {
            print("Loading from URL: " + connection.getURL() );
        }
        monitor("Content type is: " + connection.getContentType());

        InputStream inputStream = connection.getInputStream();
        int length = connection.getContentLength();
        monitor("InputStream length = " + length); // Failure if -1

        byte[] data = new byte[length];
        inputStream.read(data); // Actual byte transfer
        inputStream.close();
        return data;

    } catch(Exception ex) {
        print("### URLClassLoader.loadClassBytes() - Exception:");
        ex.printStackTrace();
        return null;
    }
}

} // End class
