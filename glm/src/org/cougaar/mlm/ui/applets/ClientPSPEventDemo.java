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
 */
 
package org.cougaar.mlm.ui.applets;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;

public class ClientPSPEventDemo extends Applet implements Runnable
{

   private Thread myThread = null;
   private Image doubleBuffer = null;

   private boolean shouldStop=true;

   private int width = 0;
   private int height  = 0;


   private final String urlFetchAll = "http://localhost:5555/";
   private String updateWindowName;
   private int basePort;

  private String msg = "None";
  private String msgPrefix = "AlpAlertMonitor On";

  public ClientPSPEventDemo() {
    try  {
      initAWT();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }

   public void init()
   {
      showStatus("Initializing ...");

      width=Integer.parseInt(getParameter("width"));
      height=Integer.parseInt(getParameter("height"));
      basePort=Integer.parseInt(getParameter("port"));
      updateWindowName = new String(getParameter("window"));

      if (myThread == null) {
        myThread = new Thread(this);
	shouldStop = false;
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
	 this.shouldStop=true;
         myThread.interrupt();
	 while(myThread.isAlive() && ctr<50) {
	     try {
		 Thread.sleep(100);
	     }
	     catch (InterruptedException ie) {
		 System.out.println("PopBrowser: Interupted during Destroy: this is bad" + ie.getMessage());
		 throw new RuntimeException(ie.getMessage());
	     }
	     ctr++;
	 }
	 if(myThread.isAlive()) {
	     System.out.println("ERROR: PopBrowser: Couldn't stop thread!!");
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

   // redirected by update() to paint double buffer
   public void paint(Graphics g)
   {

       if (msg != null )
       {
          g.clearRect(0,0,width,height);

          Color old = g.getColor();
          g.setColor(Color.red);
          g.drawString(msg, 10,10);

          g.setColor(old);
       }
   }

   public void update(Graphics g)
   {
      paint(doubleBuffer.getGraphics());
      g.drawImage(doubleBuffer,0,0,this);
   }


   public void run()
   {
     int sleepInterval;
     int counter=0;
     msg = msgPrefix;

     while(!shouldStop) {
         sleepInterval = (int)8000;
         counter++;

         repaint();

         try {
            myThread.sleep(sleepInterval);
            //URL newdoc = new URL( this.getCodeBase().getProtocol(),
            //                      this.getCodeBase().getHost(),"HTMLALERTS.PSP?NUM=" + counter);
	    
	    URL newdoc = new URL("http://" + this.getCodeBase().getHost()
				     + ":" + basePort
				 + "/alpine/demo/HTMLALERT.PSP?NUM=" + counter);
	    if(!shouldStop) {
		System.out.println("URL=" + newdoc.toExternalForm() );
	    }

	    if(!shouldStop) {
		this.getAppletContext().showDocument(newdoc,updateWindowName); // "_blank");
	    }
         }
	 catch (InterruptedException e) {
	     if(!shouldStop) {
	      System.out.println("PopBrowser: Unexepected Interuppt Exeception: " + e.getMessage());
	     }
	 }
	 catch (Exception e) {
             System.out.println("PopBrowser: Exception:" + e.getMessage());
         }
	 
	 if(shouldStop) {
	     System.out.println("PopBrowser: Has been stopped cleanly.");
	 }
     }
  }


  private void initAWT() throws Exception {
    this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        this_mouseMoved(e);
      }
      public void mouseDragged(MouseEvent e) {
        this_mouseDragged(e);
      }
    });
    this.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        this_mouseClicked(e);
      }
      public void mouseReleased(MouseEvent e) {
        this_mouseReleased(e);
      }
      public void mousePressed(MouseEvent e) {
        this_mousePressed(e);
      }
    });
  }




  void this_mouseClicked(MouseEvent e) {
     int x = e.getX();
     int y = e.getY();

     msg  = new String("("+x+","+y+")");
     System.out.println("MOUSE (clicked): x= " + x + " y= " + y);
  }

  void this_mouseMoved(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();

     System.out.println("MOUSE (moved): x= " + x + " y= " + y + " msg=" + msg);
  }


  void this_mouseDragged(MouseEvent e) {
      System.out.println("MOUSE dragged...");
  }

  void this_mouseReleased(MouseEvent e) {
     System.out.println("MOUSE released");
  }

  void this_mousePressed(MouseEvent e) {
     int x = e.getX();
     int y = e.getY();

     msg  = new String("("+x+","+y+")");
     System.out.println("MOUSE (pressed): x= " + x + " y= " + y);
  }

}
