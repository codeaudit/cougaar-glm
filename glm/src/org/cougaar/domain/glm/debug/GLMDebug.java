/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBN Technologies,
 *                               A Division of
 *                              BBN Corporation
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 1999 by
 *             BBN Technologies, A Division of
 *             BBN Corporation, all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.domain.glm.debug;

import org.cougaar.core.cluster.ClusterIdentifier;

import java.io.*;


/** 
 *  This class is used purely for debugging purposes.  It provides a single 
 *  point of output for clients.
 */
public class GLMDebug {

    private final static int debug_messages_ = setDebugMessages();
    private PrintStream outstream_ = System.err;
    private PrintWriter logstream_ = null;
    

    /** Property name 'debug_messages'. Values true/false */
    public final static String debug_messages_property_name  = "glm_debug_messages"; 
    /** Property name 'output_stream'. Values error/standard */
    public final static String output_stream                 = "glm_output_stream"; 
    /** Property name 'log_file'.  Value <file name>.*/
    public final static String log_file                      = "glm_log_file";  
    
    private static GLMDebug debug_ = null;


    private GLMDebug()
    {
	String val;

	try {
	    val = System.getProperty(output_stream);
	    if (val != null) {

		//System.err.println("System property '"+output_stream+"' = "+val);
		if (val.equals("standard"))
		    outstream_ = System.out;
	    }

	    val = System.getProperty(log_file);
	    if (val != null) {
		//System.err.println("System property '"+log_file+"' = "+val);
		File file = new File(val);
		try {
		    FileOutputStream fout = new FileOutputStream(file);
		    logstream_ = new PrintWriter(fout);
		} catch (FileNotFoundException fe) {
		    System.err.println(fe.toString());
		    fe.printStackTrace();
		} catch (IOException ie) {
		    System.err.println(ie.toString());
		    ie.printStackTrace();
		} catch (Exception ex) {
		    System.err.println(ex.toString());
		    ex.printStackTrace();
		}
	    }


	} catch (SecurityException se) {
	    System.err.println(se.toString());
	    se.printStackTrace();
	}

    }

    private final static int setDebugMessages () {
	String val;
	int result = 1000;
	try {
	    val = System.getProperty(debug_messages_property_name);
	    if (val != null) {
		//System.err.println("System property '"+debug_messages_property_name+"' = "+val);
	        if (val.equals("true")) {
		    result = 0;
		} else {
		    try {
			result =  Integer.parseInt(val);
		    } catch (Exception e) {
			//
		    }
		}
	    }
	} catch (SecurityException se) {
	    System.err.println(se.toString());
	    se.printStackTrace();
	}
	return result;
    }

    public final static  boolean printMessages()
    {
	return (debug_messages_ < 1);
    }

    public final static boolean printMessages(int p)
    {
	return (debug_messages_ <= p);
    }
    
    private PrintStream getOutputStream()
    {
	if (outstream_ != null)
	    return outstream_;
	
	return System.err;
    }

	
    private PrintWriter getLogStream()
    {
	return logstream_;
    }
    

    protected void finalize() throws Throwable
    {
	super.finalize();
	if (outstream_ != null)
	    outstream_.flush();
	if (logstream_ != null)
	    logstream_.flush();
    }



    /** 
     * Prints a debug message if debugging is on.
     * @param class_name that invoked this method
     * @param arg string to print 
     */
    public final static void DEBUG(String class_name, String arg)
    {
	DEBUG(class_name, null, arg);
    }
    
    /** 
     * Prints a debug message if debugging is on.
     * @param class_name  class that invoked this method
     * @param cid         cluster identifer
     * @param arg         string to print 
     */
    public final static void DEBUG(String class_name, ClusterIdentifier cid, String arg)
    {    
	if (debug_ == null)
	    debug_ = new GLMDebug();

	if (debug_.printMessages())
	    outputLine("##GLM-DEBUG##", class_name, cid, arg);
    }

    /** 
     * Prints a debug message if debugging is on.
     * @param class_name  class that invoked this method
     * @param cid         cluster identifer
     * @param arg         string to print 
     * @param priority 
     */
    public final static void DEBUG(String class_name, ClusterIdentifier cid, String arg, int priority)
    {    
	if (debug_ == null)
	    debug_ = new GLMDebug();

	if (debug_.printMessages(priority))
	    outputLine("##GLM-DEBUG##", class_name, cid, arg);
    }

    /** 
     * Prints a error message in the debug stream and logs message if logfile specified.
     * @param class_name  class that invoked this method
     * @param arg         string to print 
     */
    public final static void ERROR(String class_name, String arg)
    {
	ERROR(class_name, null, arg);
    }
    
    /** 
     * Prints a error message in the debug stream and logs message if logfile specified.
     * @param class_name  class that invoked this method
     * @param cid         cluster identifer
     * @param arg         string to print 
     */
    public final static void ERROR(String class_name, ClusterIdentifier cid, String arg)
    {
	if (debug_ == null)
	    debug_ = new GLMDebug();

	outputLine("##GLM-ERROR##", class_name, cid, arg);
	logToFile(class_name, cid, "GLM-ERROR "+arg);
    }
    
    private static void outputLine(String hdr, String class_name, ClusterIdentifier cid, String arg) 
    {
	debug_.getOutputStream().println(hdr+" "+class_name +": (" +(cid != null ? cid.toString():"<>") + ") "+arg);

    }
    
    private final static void logToFile(String class_name, ClusterIdentifier cid, String arg)
    {
	if (!isLogged()) return;
	PrintWriter pw = debug_.getLogStream();
	if (pw != null)
	    pw.println(class_name+": "+(cid != null ? ("(" + cid.toString() + ") "+arg): arg));
    }

    /** 
     * Prints a message and logs message if logfile specified.
     * @param class_name class that invoked this method
     * @param cid cluster identifer
     * @param arg string to print 
     */
    public final static void LOG(String class_name, ClusterIdentifier cid, String arg)
    {
	if (debug_ == null)
	    debug_ = new GLMDebug();
	
	if (debug_.printMessages(2))
	    outputLine("##GLM-LOG##", class_name, cid, arg);
	logToFile(class_name,cid,arg);
    }

    /** 
     * Prints a message and logs message if logfile specified.
     * @param class_name class that invoked this method
     * @param arg string to print 
     */
    public final static void LOG(String class_name, String arg)
    {
	LOG(class_name, null, arg);
    }


    public final static boolean isLogged()
    {
	if (debug_ == null)
	    debug_ = new GLMDebug();
	
	return (debug_.getLogStream() != null);
    }

}
