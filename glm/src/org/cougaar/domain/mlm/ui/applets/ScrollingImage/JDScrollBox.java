/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.applets.ScrollingImage;

import java.awt.*;

/**
 * @JDScrollBox.java
 * @version 2.1 January 15th 1999
 * @author: John Donohue
 *
 * This Java example is provided "as is".  This code is
 * not supported, but I will try to answer questions as
 * time allows.  Email: wizjd@panix.com
 * Visit: <URL:http://www.serve.com/wizjd/> for updated,
 * and new examples.
 * <p>
 * Ver 2.1  01/16/98 Add ability to set default placement of zooming 
 * rectangle in index pic.
 * <p>
 * ALPINE -- modified to support pop-up menu
 * <p>
 * This canvas is used for the "peek window" which  displays a portion of
 * of the big picture. Which portion shown is controlled by  dragging the
 * zooming rectangle across the minature index version of the picture in the
 * lower right corner
 */

public class JDScrollBox extends Canvas
{
  PopupMenu popup = new PopupMenu();

 Image bigPicImg;         //Ptr to the big picture
 Image indexPicImg;       //Ptr to the small index picture
 int scaleFactor;          //How much of the big pic we show at once ( 4 = 1/4)
 Color zoomRectColor;     //Color of the zooming rectangle and border of the peek window
 Color zoomBorderColor;   //Color of the border around th index image


 Image imgBuffer = null;   //Work area to build our image in
 Image peekImgBuffer = null;   //Work area to build our image in
 Graphics gBuffer;         //Graphics context for our work area

 int bt = 5;
 public Point indexPos;         //The displacement of the small index picture in the canvas
 public Point rectPos;          //The current positon of the zooming rectangle in the index pic

 //The following variables are constants for each image, we calculate and save them
 //so that we dont have to recalculate them every time we need them.
 int bigPicWidth;       //Save the width of the big picture
 int bigPicHeight;      //Save the height of the big picture
 int peekWindowWidth;   //Save the width of the window we peek at the big picture through
 int peekWindowHeight;  //Save the height of the window we peek at the big pic through
 int indexPicWidth;     //Save the width of the small index pic that we zoom around on
 int indexPicHeight;    //Save the height of the small index pic that we zoom around on
 int imgBufferWidth;
 int imgBufferHeight;
 public int rectWidth;          //Save the width of the zooming rectangle
 public int rectHeight;         //Save the height of the zooming rectangle
 int dispRectCenterX;  //X displacement of index pic in the canvas + the 1/2 rect width
 int dispRectCenterY;  //Y displacement of index pic in the canvas + the 1/2 rect height
 int maxRectX;          //Maximum X of rect in index pic(keeps us from shooting past edge)
 int maxRectY;          //Maximum Y of rect in index pic(keeps us from shooting past edge)
 int indexToBigMultiple;   //Ratio of index pic to big pic * -1

 //Constructors:
JDScrollBox( Image bigPicInImg, Image indexPicImgIn, int scaleIn,
             Color zoomRectColorIn, Color zoomBorderColorIn )
{
  this( bigPicInImg, indexPicImgIn, scaleIn, zoomRectColorIn,  zoomBorderColorIn,
        0, 0, false);

}


JDScrollBox( Image bigPicInImg, Image indexPicImgIn, int scaleIn,
             Color zoomRectColorIn, Color zoomBorderColorIn, int xRectDispIn, int yRectDispIn, boolean centerZoomIn)

{
  bigPicImg    = bigPicInImg;
  indexPicImg  = indexPicImgIn;
  scaleFactor = scaleIn;
  zoomRectColor = zoomRectColorIn;
  zoomBorderColor = zoomBorderColorIn;
  bigPicWidth      = bigPicImg.getWidth(null);
  bigPicHeight     = bigPicImg.getHeight(null);
  indexPicWidth    = indexPicImg.getWidth(null);
  indexPicHeight   = indexPicImg.getHeight(null);

  //The scaling factor determines the fraction of the big pic we display at once,
  //by setting the size of the peek window and the zooming rectangle in the index pic.
  peekWindowWidth   = bigPicWidth/scaleFactor;
  peekWindowHeight  = bigPicHeight/scaleFactor;
  imgBufferWidth =  peekWindowWidth + (2 * bt);
  imgBufferHeight = peekWindowHeight + (2 * bt);
  rectWidth = indexPicWidth/scaleFactor;
  rectHeight =indexPicHeight/scaleFactor;

  //The ratio of the index picture to the big picture. We use this when translating
  //the X/Y of the zooming rectangle in the index image to the "position" of the
  //peek window in the big picture. This is a negative number so that we start
  //drawing above and to the left of our peek window's 0,0 origin.  The further
  //"up and to the left" we begin drawing the big pic the more "down and to the
  //right" will be the portion displayed in the peek window.
  indexToBigMultiple = (-1 * (bigPicWidth / indexPicWidth));

  //Put the index image at the lower right corner of the canvas
  indexPos  = new Point( peekWindowWidth - indexPicWidth, peekWindowHeight - indexPicHeight);

  //Calc the dispalcement of the center of the zoom rectangle in the canvas context
  dispRectCenterX = indexPos.x + (rectWidth/2);
  dispRectCenterY = indexPos.y + (rectHeight/2);

  //Calc the limits of the zoom rect in the index image
  maxRectX = (indexPicWidth - rectWidth) -2;
  maxRectY = (indexPicHeight - rectHeight) -2;

  //Set where the zooming rectangle starts in the index picture
  if ( centerZoomIn)
      rectPos = new Point( (indexPicWidth/2)-(rectWidth/2), (indexPicHeight/2) -(rectHeight/2) );
  else 
      rectPos = new Point(xRectDispIn, yRectDispIn);
  


  peekImgBuffer = createImage( peekWindowWidth, peekWindowHeight);
  imgBuffer = createImage( imgBufferWidth, imgBufferHeight);

  this.resize(imgBufferWidth, imgBufferHeight);

  MenuItem mi = new MenuItem("Activate Demo Sprites");
  mi.enable(true);
  popup.add(mi);
  this.add(popup);

 }

  //Overide the default update method to avoid it blanking the screen and causing flickering
  public void update(Graphics g)
   {
    paint(g);
   }

 public void paint (Graphics g)
  {
   if ( peekImgBuffer == null)  peekImgBuffer = createImage( peekWindowWidth, peekWindowHeight);
   if ( imgBuffer == null)  imgBuffer = createImage( imgBufferWidth, imgBufferHeight);

   gBuffer = peekImgBuffer.getGraphics();
   gBuffer.drawImage(bigPicImg, rectPos.x * indexToBigMultiple, rectPos.y * indexToBigMultiple, this);
   gBuffer.drawImage(indexPicImg, indexPos.x, indexPos.y, this);
   gBuffer.setColor(zoomBorderColor);
   gBuffer.drawRect(indexPos.x -1, indexPos.y -1, indexPicWidth, indexPicHeight);
   gBuffer.setColor(zoomRectColor);
   gBuffer.drawRect(rectPos.x + indexPos.x, rectPos.y + indexPos.y, rectWidth, rectHeight);
   
   gBuffer = imgBuffer.getGraphics();
   gBuffer.setColor(zoomRectColor);
   gBuffer.fillRect(0,0,imgBufferWidth, imgBufferHeight);
   gBuffer.drawImage(peekImgBuffer, bt, bt, null);
   g.drawImage(imgBuffer, 0, 0, this);
 }


  // If the user clicks or drags the mouse on the Canvas we will look at if they
  // did it inside the index picture.  If they did we will process this event here.
  // If it was outside the index pic we will allow the event to continue up the
  // hierarchy to the applet ( or whatever instantiated this canvas)- after
  // changing the x/y to show where the click was in terms of the big picture
  // not this canvas.
  // This is so that the mouse clicks can be used in the applet to do HTML image-map
  // type functions.
  public boolean handleEvent(Event evt)
   {
    int clickX, clickY;


    int mode = 0;

    //Was it a click on our canvas ?
    if ( (evt.target == this) &&
         ( (evt.id == Event.MOUSE_DOWN) || (evt.id == Event.MOUSE_DRAG) ) )
     {
         if( evt.controlDown() ) {
            mode = 2;
         }else mode =1;
     }
   // CONTROL DOWN MOUSE CLICK
   if( mode == 2 ) {
        System.out.println("POPUPMENU");
        popup.show(this,10,10);
        return true;
   }
   // REGULAR MOUSE CLICK
   if( mode == 1 ) {
      clickX = evt.x - bt;
      clickY = evt.y - bt;
      // It was in the canvas but was it on our index picture ?
      if ( ( clickX > indexPos.x) && ( clickY > indexPos.y) )
       {
        processScrollboxClick(clickX, clickY);
        return true;
       }
      else
       {
         // Change this to be the x,y on our big pic so that caller can do imagemap stuff
         evt.x += ( rectPos.x * (-1 * indexToBigMultiple));
         evt.y += ( rectPos.y * (-1 * indexToBigMultiple));
        return super.handleEvent(evt);    //This passess the mouse click back up to Applet
       }
     }
    else
     {
      return super.handleEvent(evt);    //This passess the mouse click back up to Applet
     }
   }



  public void processScrollboxClick( int x, int y)
   {
    //Subtract half the zoom rectangles dimensions to place the rectangle at
    //the center of the users click
    rectPos.x = x - dispRectCenterX;
    rectPos.y = y - dispRectCenterY;

    if (rectPos.x > maxRectX) rectPos.x = maxRectX;
    if (rectPos.y > maxRectY) rectPos.y = maxRectY;;
    if (rectPos.x < 0)  rectPos.x = 0;
    if (rectPos.y < 0)  rectPos.y = 0;
    repaint();
   }



}
