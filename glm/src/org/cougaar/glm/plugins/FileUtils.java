/*--------------------------------------------------------------------------
 * <copyright>
 *  Copyright 1999-2003 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins;

import org.cougaar.util.ConfigFinder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.lang.Runtime;
import java.lang.System;
import java.util.Enumeration;
import java.util.Vector;
import org.cougaar.glm.debug.*;

public final class FileUtils {

    // Constants
    private static final char COMMA = ',';
    private static final int NOTFOUND = -1;

    public static Vector findFields (String line, char delim) {
	
	Vector fields = new Vector();
	int start = 0;
	int end = 0;
	boolean DONE = false;
	
	while (!DONE) {
	    end = line.indexOf(delim, start);
	    if (end == NOTFOUND) {
		DONE = true;
		end = line.length();
	    }
	    
	    String f = line.substring(start, end);
	    
	    f = f.trim();
	    if ((f.length() > 0)) {
		fields.addElement(new String(f));
	    }
	    start = end+1;
	    
	}
	return fields;
	// end of findFields
    }

    public static Enumeration readFile(String fullPath) {
	Vector readLines = new Vector();
	try {
	    FileInputStream fis = new FileInputStream(new File(fullPath));
	    InputStreamReader isr = new InputStreamReader(fis);
	    LineNumberReader reader = new LineNumberReader(isr);
	    String line;
	    while ((line = reader.readLine()) != null) {
		readLines.add(line);
	    }
	    reader.close();
	} catch (IOException ioe) {
	    GLMDebug.ERROR("readFile()", " IOEXception for "+fullPath+": " + ioe);
	}	
	return readLines.elements();
    }

    /** @deprecated  Use readConfigFile(filename,configFinder) with plugins or clusters config finder passed **/
    public static Enumeration readConfigFile(String filename) {
	return readConfigFile(filename,ConfigFinder.getInstance());
    }

    public static Enumeration readConfigFile(String filename, ConfigFinder finder) {
	Vector readLines = new Vector();
	try {
	    InputStreamReader isr = new InputStreamReader(finder.open(filename));
    	    LineNumberReader lnr = new LineNumberReader(isr);
	    String line;
	    while((line = lnr.readLine()) != null) {
		readLines.add(line);
// 		GLMDebug.DEBUG("readConfigFile()", line);		
	    }
	    isr.close();
	} catch (IOException ioe) {
	    // The file may not exist in normal operation (Inventory files).
	    // Print a debug message and return null so calling module can handle it
	    GLMDebug.DEBUG("readConfigFile()", " IOEXception for "+filename+": " + ioe);
	    return  null;
	}	
	return readLines.elements();
    }

    public static void writeFile(String fullPath, boolean append, Enumeration data) {
	try {
	    File outputFile = new File(fullPath);
//  	    GLMDebug.DEBUG("writeFile()", " Size of file "+fullPath+" before is: "+outputFile.length()+", append? "+append);
	    FileOutputStream fos = new FileOutputStream(fullPath, append);
	    PrintWriter pw = new PrintWriter(fos, true);
	    while (data.hasMoreElements()) {
		pw.println(data.nextElement());
	    }
	    pw.close();
//  	    GLMDebug.DEBUG("writeFile()", "Size of file "+fullPath+" after is: "+outputFile.length()+", append? "+append);
	} catch (IOException ioe) {
	    GLMDebug.ERROR("writeFile()", "IOExceptionfor "+fullPath+": " + ioe);
	}
    }

    public static boolean renameFile(String from, String to) {
	// This method is actually rename file	
	File orig_file = new File(from);
	try {
	    if (!orig_file.exists()) {
		GLMDebug.ERROR("moveFile()", "Source file does not exist. "+from);
		return false;
	    }
	    if (!orig_file.canWrite()) {
		GLMDebug.ERROR("moveFile()", "Permissions problem: Cannot move file "+from);
		return false;
	    }
	    File new_file = new File(to);
	    orig_file.renameTo(new_file);
	    
	} catch (SecurityException se) {
	    GLMDebug.ERROR("moveFile()", "Security Exception for "+from+": "+se);
	    return false;
	}
	return true;
    }

    public static boolean deleteFile(String filename) {
	File discardFile = new File(filename);
	if (discardFile.exists()) {
	    discardFile.delete();
	}
	return true;
    }

    public static boolean copyFile(String from, String to) {
	Enumeration fileContents = readFile(from);
	writeFile(to, false, fileContents);
	return true;
    }

    public static boolean asciiToBinary(String bin_path, String type, String file_name) {
	// Warning: uses a linux executable.
	String fail_msg = type+" failure to convert input file: "+file_name+" because: ";
	if (!System.getProperty("os.name").equals("Linux")) {
	    GLMDebug.ERROR("asciiToBinary()", fail_msg+"icisatob exec only expected to run on Linux.");
	    return false;
	}
	Runtime rt = Runtime.getRuntime();
	try {
	    Process atob = 
		rt.exec(bin_path+"icisatob "+type+" "+file_name);
	    atob.waitFor();
	} catch (InterruptedException ie) {
	    GLMDebug.ERROR("asciiToBinary()", fail_msg + ie);
	    return false;
	} catch (IOException ioe) {
	    GLMDebug.ERROR("asciiToBinary()", fail_msg + ioe);
	    return false;
	}
	return true;
    }

    public static boolean  binaryToAscii(String bin_path, String type, String file_name) {
	// Warning: uses a linux executable.
	String fail_msg = type+" failure to convert input file: "+file_name+" because: ";
	if (!System.getProperty("os.name").equals("Linux")) {
	    GLMDebug.ERROR("binaryToAscii()", fail_msg+"icisbtoa exec only expected to run on Linux.");
	    return false;
	}
	Runtime rt = Runtime.getRuntime();
	try {
	    Process atob = 
		rt.exec(bin_path+"icisbtoa "+type+" "+file_name);
	    atob.waitFor();
	} catch (InterruptedException ie) {
	    GLMDebug.ERROR("binaryToAscii()", fail_msg + ie);
	    return false;
	} catch (IOException ioe) {
	    GLMDebug.ERROR("binaryToAscii()", fail_msg + ioe);
	    return false;
	}
	return true;
    }

    public static boolean sortFile(String input_file, String output_file, String keys) {
	// Warning: uses a unix sort
	String os = System.getProperty("os.name");
	if (!(os.equals("Linux") || (os.equals("SunOS")))) {
	    GLMDebug.ERROR("fileSort()", "Uses unix sort, Not a unix system.");
	    return false;
	}
	String command = "sort "+keys+" "+input_file+" -o "+output_file+" -T /tmp";
// 	GLMDebug.DEBUG("fileSort()", "sort command:"+command);
	try {
	    Runtime rt = Runtime.getRuntime();
	    Process sort = rt.exec(command);
	    sort.waitFor();
	} catch (Exception ie) {
	    GLMDebug.ERROR("fileSort()", "Sorting InterruptedException: " + ie);
	    return false;
	}
	return true;

    }
}
