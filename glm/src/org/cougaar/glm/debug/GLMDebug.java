/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 1999-2004 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.cougaar.core.mts.MessageAddress;


/** 
 *  This class is used purely for debugging purposes.  It provides a single 
 *  point of output for clients.
 */
public class GLMDebug {

    private final static int debug_messages_ = setDebugMessages();
    private PrintStream outstream_ = System.err;
    private PrintWriter logstream_ = null;
    
    public final static int   ERROR_LEVEL = 8;
    public final static int CONCISE_LEVEL = 4;
    public final static int   DEBUG_LEVEL = 2;
    public final static int     LOG_LEVEL = 1;
    private final static int   DFLT_LEVEL = 8;
    private final static int    ALL_LEVEL = 0;
    private final static int   NONE_LEVEL = 9;
    
    /** Property name 'debug_messages'. Values true/false */
    public final static String DEBUG_MESSAGES_PROP  = "glm_debug_messages"; 
    /** Property name 'output_stream'. Values error/standard */
    public final static String output_stream                 = "glm_output_stream"; 
    /** Property name 'log_file'.  Value <file name>.*/
    public final static String log_file                      = "glm_log_file";  
    
    private static GLMDebug debug_ = null;
    private static String delayedSeparatorMessage = null;

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
        String val = null;
	try {
            val = System.getProperty(DEBUG_MESSAGES_PROP);
	    if (val == null) return DFLT_LEVEL;
            if (val.equals("true"))    return     ALL_LEVEL;
            if (val.equals("all"))     return     ALL_LEVEL;
            if (val.equals("error"))   return   ERROR_LEVEL;
            if (val.equals("debug"))   return   DEBUG_LEVEL;
            if (val.equals("concise")) return CONCISE_LEVEL;
            if (val.equals("log"))     return     LOG_LEVEL;
            if (val.equals("none"))    return    NONE_LEVEL;
            if (val.equals("false"))   return    DFLT_LEVEL;
            return Integer.parseInt(val);

	} catch (NumberFormatException nfe) {
            System.err.println("Bad system property '"
                               + DEBUG_MESSAGES_PROP
                               + "' = "
                               + val);
 	} catch (SecurityException se) {
	    se.printStackTrace();
	}
	return DFLT_LEVEL;
    }

    public final static boolean printMessages()
    {
	return (debug_messages_ <= DEBUG_LEVEL);
    }

    public final static boolean printDebug()
    {
	return (debug_messages_ <= DEBUG_LEVEL);
    }

    public final static boolean printError()
    {
	return (debug_messages_ <= ERROR_LEVEL);
    }

    public final static boolean printLogging()
    {
	return (debug_messages_ <= LOG_LEVEL);
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
    public final static void DEBUG(String class_name, MessageAddress cid, String arg)
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
    public final static void DEBUG(int priority, String class_name, MessageAddress cid, String arg) {
        DEBUG(class_name, cid, arg, priority);
    }

    public final static void DEBUG(String class_name, MessageAddress cid, String arg, int priority)
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
    public final static void ERROR(String class_name, MessageAddress cid, String arg)
    {
	if (debug_ == null)
	    debug_ = new GLMDebug();

	outputLine("##GLM-ERROR##", class_name, cid, arg);
	logToFile(class_name, cid, "GLM-ERROR "+arg);
    }
    
    private static void outputLine(String hdr, String class_name, MessageAddress cid, String arg) 
    {
        if (delayedSeparatorMessage != null) {
            debug_.getOutputStream().println();
            debug_.getOutputStream().println(delayedSeparatorMessage);
            debug_.getOutputStream().println();
            delayedSeparatorMessage = null;
        }
	debug_.getOutputStream().println(hdr+" "+class_name +": (" +(cid != null ? cid.toString():"<>") + ") "+arg);
    }

    public static void setDelayedSeparator(String msg) {
        delayedSeparatorMessage = msg;
    }
    
    public static void clearDelayedSeparator(String msg) {
        if (delayedSeparatorMessage == null) {
            debug_.getOutputStream().println();
            debug_.getOutputStream().println(msg);
            debug_.getOutputStream().println();
        }
        delayedSeparatorMessage = null;
    }
    
    private final static void logToFile(String class_name, MessageAddress cid, String arg)
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
    public final static void LOG(String class_name, MessageAddress cid, String arg)
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
