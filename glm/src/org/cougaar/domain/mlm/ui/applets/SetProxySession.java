/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.applets;

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
           myThread.stop();
           myThread = null;
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

     try {
       //URL newdoc = new URL( this.getCodeBase().getProtocol(),
       //                      this.getCodeBase().getHost(),"HTMLALERTS.PSP?NUM=" + counter);

       URL newdoc = new URL("http://" + this.getCodeBase().getHost()
         + ":" + this.getCodeBase().getPort()
         + "/" + commandURL);
       System.out.println(">URL=" + newdoc.toExternalForm() );

       URLConnection uc = newdoc.openConnection();
       uc.setDoInput(true);
       InputStream is = uc.getInputStream();
       byte b[] = new byte[512];
       int len;
       while( (len = is.read(b,0,512)) > -1 )
       {
         // System.out.write(b,0,len);
       }

       //this.getAppletContext().showDocument(newdoc,"_blank");
     } catch (Exception e) {
       System.out.println("Exception:" + e.getMessage());
     }
  }

}
