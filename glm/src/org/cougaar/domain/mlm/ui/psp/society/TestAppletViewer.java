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

import java.awt.*;
import java.io.*;
import java.net.*;

/**
Simple applet which can be evoked in 2 ways in order to test Applet file access/security issues w/ use of
LogPlanServer proxy service feature.

The applet has 2 panes.  The first line in each list box is the url that is fetched
for data to populate list.

In Case.1 below, the applet's left list will contain text.  Indicating the applet
successfully read data.txt file from the remote host site.

the right box should be empty (except first line URL indicator) regardless
whether logplanserver is running or not -- it violates the applet security
model to read the data.txt from local file system.


Case 2. The left should be empty, and the right contains text.
In this  case where the applet talks to localhost proxy to load the
applet.  Hower it reads a local file that has been "masked" by proxy.
In this case the applet cannot obtain data from remote source directly
since its host is the proxy, localhost.  Howerver, it can obtain data.txt
from the proxy local dir.


Case.1

>A.  In browser:

     http://wwww.bbb.com/test/testappletviewer.htm


Case 2.


>A.  launch Logplanserver on localhost, be sure data.txt file (any raw text will do)
     exists in logplanserver working directory.

>B.  In browser, first:
      http://localhost:5555/?PROXY?START?URL=http://www.bbb.com/test/

>C.  then:
      http://localhost:5555/testappletviewer.htm


**/

public class TestAppletViewer extends  java.applet.Applet {
   static final int W = 680;  // size in pixels
   static final int H = 640;
  List list1 = new List();
    String connString1 = "http://www.roaringshrimp.com/test/data.txt";
    String connString2 = "http://localhost:5555/?FILE?DOCUMENT=data.txt";
  List list2 = new List();

    public void start() {
     


           URL myURL;
           URLConnection myConnection;
           BufferedInputStream bis;

           list1.add(connString1);
           try{
              myURL = new URL(connString1);
              System.out.println("DATA URL: " + myURL.toExternalForm());
              myConnection = myURL.openConnection();
              System.out.println("opened query connection = "+ myConnection.toString());
              InputStream is = myConnection.getInputStream();
              bis = new BufferedInputStream(is);

              byte buf[] = new byte[512];
              int sz;

              while( (sz = bis.read(buf,0,30)) != -1)
              {
                 String line = new String(buf);
                 list1.add(line);
              }
            } catch (Exception e) {e.printStackTrace(); }


            list2.add(connString2);
           try{
              myURL = new URL(connString2);
              System.out.println("DATA URL: " + myURL.toExternalForm());
              myConnection = myURL.openConnection();
              System.out.println("opened query connection = "+ myConnection.toString());
              InputStream is = myConnection.getInputStream();
              bis = new BufferedInputStream(is);

              byte buf[] = new byte[512];
              int sz;

              while( (sz = bis.read(buf,0,30)) != -1)
              {
                 String line = new String(buf);
                 list2.add(line);
              }
            } catch (Exception e) {e.printStackTrace(); }


      show();
   }

   private void setDefaultColorsFonts(){
	  setFont(new Font("TimesRoman",Font.PLAIN,10));
      setBackground(java.awt.Color.white);
      setForeground(java.awt.Color.black);
      setLayout(new BorderLayout());
   }

   public void paint(Graphics g)
   {
      //g.drawString(connString,15,15);
   }


  public TestAppletViewer() {
    try  {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.add(list1, null);
    this.add(list2, null);
  }
   
   


   
}



