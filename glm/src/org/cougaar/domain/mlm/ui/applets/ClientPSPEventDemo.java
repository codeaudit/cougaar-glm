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
import java.awt.event.*;
import java.net.URL;
import java.util.*;

public class ClientPSPEventDemo extends Applet implements Runnable
{

   private Thread myThread = null;
   private Image doubleBuffer = null;

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

     while(true) {
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
            System.out.println("URL=" + newdoc.toExternalForm() );

            this.getAppletContext().showDocument(newdoc,updateWindowName); // "_blank");
         } catch (Exception e) {
             System.out.println("PopBrowser: Exception:" + e.getMessage());
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
