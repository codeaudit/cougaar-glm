/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
 */
 
package org.cougaar.mlm.ui.applets;

import java.applet.Applet;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class SetProxySession extends Applet implements Runnable
{

   private Thread myThread = null;
   private Image doubleBuffer = null;

   private boolean shouldStop=true;

   private int width = 0;
   private int height  = 0;
   private int basePort;

   private String proxyURL;
   private String proxyAlias;
   private String commandURL;
   private String redirect;


  public SetProxySession() {

  }

   public void init()
   {
      showStatus("Initializing ...");
      System.out.println("Initializing...");

      width=Integer.parseInt(getParameter("width"));
      height=Integer.parseInt(getParameter("height"));
      redirect=getParameter("redirect");

      //
      // One or the other provided. proxyAlias takes precedence.
      //
      String purl=getParameter("proxyurl");
      proxyAlias=getParameter("proxyalias");

      if( purl != null ){
        proxyURL = new String(purl);
        if (!(proxyURL.endsWith("/"))) {
          proxyURL += "/";
        }
      } else {
        proxyURL = "";
      }

      if( proxyAlias != null) {
          proxyURL = "http://" + this.getCodeBase().getHost()
                      + ":" + this.getCodeBase().getPort() + "/$" + proxyAlias;
      }

      if (redirect != null) {
        commandURL = "$" + redirect + "/?PROXY?START?URL="+ proxyURL;
      } else {
        commandURL = "?PROXY?START?URL="+ proxyURL;
      }

      System.out.println("CommandURL=" + commandURL);

      if (myThread == null) {
        myThread = new Thread(this);
	shouldStop=false;
        myThread.start();
        //myThread.setPriority(Thread.MIN_PRIORITY);
      }


      try  {
        this.resize( new Dimension( width, height));
        this.setBackground(Color.black);
      } catch (Exception e) {
        e.printStackTrace();
      }


      doubleBuffer = createImage(width, height);
   }

   public void destroy()
   {
        if (myThread != null) {
	 int ctr=0;
         //MWD stop replaced with below - myThread.stop();
	 this.shouldStop=true;
	 while(myThread.isAlive() && ctr<50) {
	     try {
		 Thread.sleep(100);
	     }
	     catch (InterruptedException ie) {
		 System.out.println("SetProxySession: Interupted during Destroy: this is bad" + ie.getMessage());
		 throw new RuntimeException(ie.getMessage());
	     }
	     ctr++;
	 }
	 if(myThread.isAlive()) {
	     System.out.println("ERROR: SetProxySession: Couldn't stop thread!!");
	 }
	 else {
	     myThread = null;
	 }
        }
   }

   /** called after init() **/
   public void start()
   {
   }

   public void stop()
   {
   }


   public void run()
   {

     URLConnection uc=null;
     InputStream is=null;

     try {
       //URL newdoc = new URL( this.getCodeBase().getProtocol(),
       //                      this.getCodeBase().getHost(),"HTMLALERTS.PSP?NUM=" + counter);

       URL newdoc = new URL("http://" + this.getCodeBase().getHost()
         + ":" + this.getCodeBase().getPort()
         + "/" + commandURL);

       if(!shouldStop) {
	   System.out.println(">URL=" + newdoc.toExternalForm() );
       }
       
       if(!shouldStop) {
	   uc = newdoc.openConnection();
	   uc.setDoInput(true);
       }

       if(!shouldStop) {	   
	   is = uc.getInputStream();
	   byte b[] = new byte[512];
	   int len;
	   while(( (len = is.read(b,0,512)) > -1 ) &&
		 (!shouldStop))
	       {
		   // System.out.write(b,0,len);
	       }
       }

       //this.getAppletContext().showDocument(newdoc,"_blank");

       is.close();
       is=null;

     } catch (Exception e) {
       System.out.println("SetProxySession:Exception:" + e.getMessage());
       if(is != null) {
	   try {
	       is.close();
	   }
	   catch(IOException ioe) {
	       System.out.println("SetProxySession:Got IOException when trying to close the InputStream:" + ioe.getMessage());
	   }
       }
     }
  }

}
