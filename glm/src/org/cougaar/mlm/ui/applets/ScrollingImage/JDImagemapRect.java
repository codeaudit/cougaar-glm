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

