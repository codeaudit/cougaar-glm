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
 
package org.cougaar.mlm.ui.applets.ScrollingImage;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;

/**
 * @version 3.1 January 16 1999
 * @author John Donohue
 *
 *  This Java example is provided "as is".  This code is
 * not supported, but I will try to answer questions as
 * time allows.  Email: wizjd@panix.com
 * Visit: <URL:http://www.serve.com/wizjd/> for updated,
 * and new examples.
 *
 * Ver 02   01/21/98 Allow colors to be set via applet parameters
 * Ver 03   11/14/98 Add support for Frames
 *          Also add error checking to the parameters this Java
 *          Applet expects to be passed through the APPLET tag
 * Ver 3.1  01/16/98 Add ability to set default placement of zooming rectangle in index pic.
 *
 * The Applet.
 */

public class JDScrollingImagemap extends Applet
{
 private Vector imagemapRectVector;
 // Graph extends JDScrollBox
 private Graph windowCanvas;
 private boolean error = false;
 private Vector errorMessageVector = new Vector();


 
 /**
  * init
  */
 public void init()
  {
   Color scrollBoxColor;
   Color borderColor;
   String scrollBoxColorString;
   String borderColorString;
   Image smallMapImg = null, bigMapImg = null;
   int scale = 0;
   int defaultXIndexPos =0;
   int defaultYIndexPos =0;
   boolean centerZoom = false;


   super.init();
   imagemapRectVector = new Vector (100);

   String imagemapFileName = getParameter("imagemap_file_name");
   if ( imagemapFileName == null)
      sendErrorMessage("Missing parameter \"imagemap_file_name=\" in APPLET tag");

   String indexFileName = getParameter("index_file_name");
   if ( indexFileName == null)
      sendErrorMessage("Missing parameter \"index_file_name=\" in APPLET tag");

   String scaleParm = getParameter("scale");
   if ( scaleParm == null)
      sendErrorMessage("Missing parameter \"scale=\" in APPLET tag");
   else
    {
     try { scale = (Integer.valueOf(scaleParm)).intValue(); }
     catch (NumberFormatException e)
       { sendErrorMessage("Scale must be Number"); }
    }

   String defaultIndexPos = getParameter("defaultIndexPos");
   if ( defaultIndexPos != null) {
     if ( defaultIndexPos.equalsIgnoreCase("center")) {
         centerZoom = true;
     }
     else {
         defaultYIndexPos  = (Integer.valueOf( defaultIndexPos.substring(0, defaultIndexPos.indexOf(",") ) )).intValue();
         defaultXIndexPos  = (Integer.valueOf( defaultIndexPos.substring(defaultIndexPos.indexOf(",")+1 ) )).intValue();
     }
   }


   scrollBoxColorString = getParameter("scrollBoxColor");
   if ( scrollBoxColorString == null)
      sendErrorMessage("Missing parameter \"scrollBoxColor=\" in APPLET tag");

   borderColorString = getParameter("borderColor");
   if ( borderColorString == null)
      sendErrorMessage("Missing parameter \"borderColor=\" in APPLET tag");

   String imagemapData = getParameter("imagemap_data");
   if ( imagemapData == null)
     { sendErrorMessage("Missing parameter \"imagemap_data=\" in APPLET tag"); return; }

   StringTokenizer tokens = new StringTokenizer ( imagemapData, ";");
   while (tokens.hasMoreTokens() )
    {
     StringTokenizer fields = new StringTokenizer ( tokens.nextToken(), ",");
     if ( (fields.countTokens() != 5) && (fields.countTokens() != 6) )
       {
        sendErrorMessage("Bad number of parameters in \"imagemapData=\" APPLET tag");
        return;
       }
     if ( fields.countTokens() == 5)
       {
        imagemapRectVector.addElement( new JDImagemapRect(
        (Integer.valueOf( fields.nextToken().trim() )).intValue(),
        (Integer.valueOf( fields.nextToken().trim() )).intValue(),
        (Integer.valueOf( fields.nextToken().trim() )).intValue(),
        (Integer.valueOf( fields.nextToken().trim() )).intValue(),
        fields.nextToken().trim() ) );
       }
     if ( fields.countTokens() == 6)
       {
        imagemapRectVector.addElement( new JDImagemapRect(
        (Integer.valueOf( fields.nextToken().trim() )).intValue(),
        (Integer.valueOf( fields.nextToken().trim() )).intValue(),
        (Integer.valueOf( fields.nextToken().trim() )).intValue(),
        (Integer.valueOf( fields.nextToken().trim() )).intValue(),
        fields.nextToken().trim(),
        fields.nextToken().trim() ) );
       }
    }

   // If our input parameters look good- go ahead and load our images.
   if ( !error )
    {
     bigMapImg   = getPicImage(imagemapFileName);
     smallMapImg = getPicImage(indexFileName);
    }

   // If we loaded our images ok- then go ahead and start.
   if ( !error )
    {
     //Change our foreground, and background color to the ones specified
     int backRed   = (Integer.valueOf(scrollBoxColorString.substring(0,2),16) ).intValue();
     int backGreen = (Integer.valueOf(scrollBoxColorString.substring(2,4),16) ).intValue();
     int backBlue  = (Integer.valueOf(scrollBoxColorString.substring(4,6),16) ).intValue();
     int foreRed   = (Integer.valueOf(borderColorString.substring(0,2),16) ).intValue();
     int foreGreen = (Integer.valueOf(borderColorString.substring(2,4),16) ).intValue();
     int foreBlue  = (Integer.valueOf(borderColorString.substring(4,6),16) ).intValue();
     borderColor = new Color(foreRed, foreGreen, foreBlue);
     scrollBoxColor = new Color(backRed, backGreen, backBlue);

//#######################################################
    String edges = getParameter("GEdges");
    String center = getParameter("GCenter");

     //Set up a scroll box canvas, and add it to the applet frame
     // Graph extends JDScrollBox
     windowCanvas = new Graph(bigMapImg, smallMapImg, scale, scrollBoxColor, borderColor, defaultXIndexPos,
                                    defaultYIndexPos, centerZoom);

     windowCanvas.addMouseListener(new MouseAdapter() { 
	 public void mousePressed(MouseEvent evt) {
	     int mX, mY;
	     // translate these back to canvas context
	     mX = (evt.getX() - windowCanvas.getBounds().x);
	     mY = (evt.getY() - windowCanvas.getBounds().y);
	     processImagemapClick(mX,mY);
	 }});
	 

     if( edges != null ){
         windowCanvas.load( edges, center );
         windowCanvas.start();
     }

//########################################################

     setLayout (new BorderLayout() );
     add(windowCanvas);

    }
  }



 /**
  * Overide the default update method to avoid it blanking the screen and causing
  *  flickering
  */
  public void update(Graphics g)   //Overide the default update method to avoid it
   {                               // blanking the screen and causing flickering
    paint(g);
   }

 /**
  * Output any error messages if we have them
  */
  public void paint(Graphics g)   //Overide the default update method to avoid it
   {                               // blanking the screen and causing flickering
    if (error)
     {
      int y =0;
      Font f = new Font("SansSerif", Font.BOLD, 12);
      FontMetrics fm = g.getFontMetrics(f);
      g.setFont(f);
      Enumeration enum = errorMessageVector.elements();
      while (enum.hasMoreElements() )
       {
        y += fm.getAscent();
        g.drawString( ((String)enum.nextElement()), 1, y);
       }
     }
   }



  /**
   * If the user clicked on the scrolling Canavs we will catch it here and
   * do our html style image-map stuff
   *
   * handleEvent taken out (deprecated style) use the MouseAdapter above
   *   to do the same thing - MWD.
   *
  public boolean handleEvent(Event evt)
   {
    if ( evt.target == windowCanvas && evt.id == Event.MOUSE_DOWN  )
     {
      // translate these back to canvas context
      evt.x = (evt.x - windowCanvas.getBounds().x);
      evt.y = (evt.y - windowCanvas.getBounds().y);
      processImagemapClick(evt.x, evt.y);
     }
    return true;    //Dont allow event to continue up
   }

  */

 /**
  * processImagemapClick
  */
 private void processImagemapClick( int x, int y)
   {
    JDImagemapRect currentEnum;
    Enumeration enum = imagemapRectVector.elements();

    while (enum.hasMoreElements())
     {
      currentEnum = ((JDImagemapRect)enum.nextElement() );
      if (currentEnum.testForHit(x,y))
        {
         jumpToUrl(currentEnum);
         break;
        }
     }
   }




  /**
   * jumpToUrl
   */
  private void jumpToUrl(JDImagemapRect r)
   {
    try
     {
      URL urlToGoto = new URL(getCodeBase(), r.theUrl);
      if ( r.frameTarget.equals("") )
        getAppletContext().showDocument(urlToGoto);
      else
        getAppletContext().showDocument(urlToGoto,r.frameTarget);
     }
    catch (MalformedURLException e)
     {
      e.printStackTrace();
     }
   }



 /**
  * Use MediaTracker to insure the images are fully loaded before
  * we try to use them
  */
 private Image getPicImage(String imageFileName)
 {
  Image imgWork = null;
  MediaTracker mt = new MediaTracker(this);
  try
   {
    imgWork = getImage(getDocumentBase(),imageFileName);
   }
  catch(Exception e1)
   {
    sendErrorMessage("Could not find file \"" + imageFileName + "\"");
    System.out.println(e1);
   }
  mt.addImage(imgWork, 0);
  try
  {
   showStatus("Loading image " + imageFileName );
   mt.checkID(0, true);
   mt.waitForID(0);
  }
  catch(Exception e2)
  {
   sendErrorMessage("Could not find file \"" + imageFileName + "\"");
   System.out.println(e2);
  }
  return imgWork;
 }


 /**
  * print error message.
  */
 private void sendErrorMessage(String errorMessageIn)
 {
  error = true;
  errorMessageVector.addElement("Error: " + errorMessageIn);
  System.out.println(errorMessageIn);
 }
}



