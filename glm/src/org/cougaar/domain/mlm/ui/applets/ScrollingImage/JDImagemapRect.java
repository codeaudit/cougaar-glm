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
 * @JDImagemapRect.java
 * @version 2.0 August 1st 1998
 * @author: John Donohue
 *
 * This Java example is provided "as is".  This code is
 * not supported, but I will try to answer questions as
 * time allows.  Email: wizjd@panix.com
 * Visit: <URL:http://www.serve.com/wizjd/> for updated,
 * and new examples.
 * <p>
 * A Class for holding the corners of a image-map rectangle and the URL 
 * that clicks inside that rectangle point to.
 */

class JDImagemapRect
{
 Point upperLeftCorner, lowerRightCorner;
 String theUrl; String frameTarget;

  JDImagemapRect(int x1In, int y1In, int x2In, int y2In, String theUrlIn, String frameTargetIn)
   {
    upperLeftCorner  = new Point(x1In, y1In);
    lowerRightCorner = new Point(x2In, y2In);
    theUrl = theUrlIn;
    frameTarget = frameTargetIn;
   }

  JDImagemapRect(int x1In, int y1In, int x2In, int y2In, String theUrlIn)
   {
    upperLeftCorner  = new Point(x1In, y1In);
    lowerRightCorner = new Point(x2In, y2In);
    theUrl = theUrlIn;
    frameTarget = "";
   }

  /**
   *   Test if supplied X,Y parameters are inside of this imagemape rectangle
   */
   public boolean testForHit(int x, int y)
   {
    if ( ( x >= upperLeftCorner.x) && (x <= lowerRightCorner.x) &&
         ( y >= upperLeftCorner.y) && (y <= lowerRightCorner.y)    )
      return true;
    else
      return false;
   }

}

