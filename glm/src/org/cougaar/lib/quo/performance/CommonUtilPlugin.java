
/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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
 *  THIS SOFTWARE IS MODIFIED FOR TESTING QUO_ULTRALLOG INTEGRATION
 */

package org.cougaar.lib.quo.performance;

import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import java.util.*;
import java.io.*;

/**
 * This COUGAAR Plugin performs the common task as reading all the parameters 
 * and getting the values that may be common to all the child Plugins
 */

public  class CommonUtilPlugin extends SimplePlugin {
   
    /**
     * parsing the plugIn arguments and setting the values 
     */
    protected String getParameterValue(Vector p, String param){
	String strParam=null;
	for(int i = 0; i < p.size(); i++){
	    String s = (String)p.elementAt(i);
	    if (s.indexOf(param) != -1){
		 strParam = s.substring(s.indexOf("=")+1, s.length());
	    }
	}

	return strParam;
    }
   protected int getParameterIntValue(Vector p, String param) {
       int returnVal=-1;
       String str = getParameterValue(p, param);
       if (str != null)
	   returnVal = Integer.parseInt(str);

       return returnVal;
   }

    protected boolean getParameterBooleanValue(Vector p, String param) {
	boolean returnVal=false;
	String str = getParameterValue(p, param);
	if (str != null){
	    if (str.equals("true"))
		returnVal = true;
	}
	return returnVal;
   }

    
    /**
     * This Plugin has no subscriptions so this method does nothing
     */
    protected void execute (){}
    protected void setupSubscriptions(){
	Vector p = getParameters();
    }
    
    public void debug(boolean DEBUG, String message) {
	if (DEBUG) //default to stdout
	    //System.out.println(formatMessage(message));
	    System.out.println(message);
    }

    public void log(boolean LOG, String out, FileWriter fw, String message){
	try {
	    if (LOG) {//default to stdout
		if (out != null)
		    {
			fw = new FileWriter(out, true);
			fw.write(message+"\n");
			fw.close();
		    }
		else
		    System.out.println(message);
	    }
	} catch (IOException ie) {
	    ie.printStackTrace();
	}//catch
	
    }



    public String formatMessage(String msg[]){
	String delimiter="'";
	String returnMsg= msg[0]+delimiter;
	for(int i =1; i < msg.length; i++){
	    returnMsg = returnMsg+","+msg[i];
	}
	return returnMsg;
    }
    
    public void consumeCPU(int times) {
	int slurp = 0;
	for(int i= 0; i < times; i++){
	    slurp++;
	}
    }

    public byte[] alterMessageSize(int val) {
	byte[] bytes = new byte[val];
	for(int i = 0; i < val; i++) bytes[i] = 42;
	return bytes;
    }

   
	
    public void breakFromLoop(int count, int MAXCOUNT){
	if ( (count == MAXCOUNT+1) & (MAXCOUNT != -1)) 
	    // break;
	    System.exit(1); 
    }

    public void waitFor(int time){
	try {
	    Thread.sleep(time);
	} catch (InterruptedException e) {
	    System.out.println(e);
	}
    }
   }









