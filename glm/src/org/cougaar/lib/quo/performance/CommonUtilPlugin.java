
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
 *  THIS SOFTWARE IS MODIFIED FOR TESTING QUO_ULTRALLOG INTEGRATION
 */

package org.cougaar.lib.quo.performance;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.cougaar.planning.plugin.legacy.SimplePlugin;

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









