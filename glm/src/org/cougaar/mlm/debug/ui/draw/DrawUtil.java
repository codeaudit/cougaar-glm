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
 */
// **********************************************************************
// 
// 
// 
// 
// 
// 
// 
// **********************************************************************

package org.cougaar.mlm.debug.ui.draw;

//import netscape.application.Point;


/** class DrawUtil
 *
 * Drawing utility functions
 *
 **/
public class DrawUtil {

    /** Bresenham's line algorithm.  Returns an array of points to draw. */
    public final static Point[] bresenham_line(Point pt1, Point pt2)
    {
	return bresenham_line(pt1.x, pt1.y, pt2.x, pt2.y);
    }

    /** Bresenham's line algorithm. */
    public final static Point[] bresenham_line(
	int x1, int y1, int x2, int y2)
    {
	// This is actually NOT bresenhams algorithm. It is faster!
	// -rmf

// 	System.out.println("DrawUtil.bresenham_line(" +
// 			   x1 + "," + y1 + ")->(" + x2 + "," + y2 + ")");
	int i;
	int d, x, y, ax, ay, sx, sy, dx, dy, t;

	dx = x2-x1;  ax = Math.abs(dx)<<1;  sx = MoreMath.sign(dx);
	dy = y2-y1;  ay = Math.abs(dy)<<1;  sy = MoreMath.sign(dy);

	t = Math.max(Math.abs(dx),Math.abs(dy))+1;
	Point[] ret_val = new Point[t];
    
	x = x1;
	y = y1;
	if (ax>ay) {		/* x dominant */
	    d = ay-(ax>>1);
	    for (i=0;;) {
		ret_val[i++] = new Point(x,y);
		//ret_val[i].x = x; ret_val[i++].y = y;
		if (x==x2)
		    return ret_val;
		if (d>=0) {
		    y += sy;
		    d -= ax;
		}
		x += sx;
		d += ay;
	    }
	}
	else {			/* y dominant */
	    d = ax-(ay>>1);
	    for (i=0;;) {
		ret_val[i++] = new Point(x,y);
		//ret_val[i].x = x; ret_val[i++].y = y;
		if (y==y2)
		    return ret_val;
		if (d>=0) {
		    x += sx;
		    d -= ay;
		}
		y += sy;
		d += ax;
	    }
	}
    }

//////////////////////////////////////////////////////////////////////////

    /** inside_polygon() - tests if a point is inside a polygon */
    public final static boolean inside_polygon(
	int[] xpts, int[] ypts, int ptx, int pty) {

	int j, inside_flag = 0;
	int numverts = xpts.length;
	if (numverts <= 2) return false;
	Point vtx0 = new Point(0,0), vtx1 = new Point(0,0);
	double dv0;                        // prevents OVERFLOW!!
	int crossings = 0;
	boolean xflag0 = false, yflag0 = false, yflag1 = false;

// 	vtx0 = (Point)pgon.elementAt(numverts-1);/*&pgon[numverts-1];*/
	vtx0.x = xpts[numverts-1];
	vtx0.y = ypts[numverts-1];
	// get test bit for above/below Y axis
	yflag0 = ((dv0 = vtx0.y - pty) >= 0);
    
	for (j=0; j<numverts; j++) {
	    if ((j & 0x1) != 0) {	//HACK - slightly changed
// 		vtx0 = (Point)pgon.elementAt(j);/*&pgon[j];*/
		vtx0.x = xpts[j];
		vtx0.y = ypts[j];
		yflag0 = ((dv0 = vtx0.y - pty) >= 0);
	    }
	    else {
// 		vtx1 = (Point)pgon.elementAt(j);/*&pgon[j];*/
		vtx1.x = xpts[j];
		vtx1.y = ypts[j];
		yflag1 = (vtx1.y >= pty);
	    }

	    /* check if points not both above/below X axis - can't hit ray */
	    if (yflag0 != yflag1) {
		/* check if points on same side of Y axis */
		if ((xflag0 = (vtx0.x >= ptx)) == (vtx1.x >= ptx)) {
		    if (xflag0) crossings++;
		}
		else {
		    crossings +=
			((vtx0.x - dv0*(vtx1.x-vtx0.x)/(vtx1.y-vtx0.y)) >= ptx)
			? 1 : 0;
		}
	    }
	    inside_flag = crossings & 0x01 ;
	}
	return (inside_flag != 0);
    }

//////////////////////////////////////////////////////////////////////////

    /** closestPolyDistance() - returns the distance from Point (x,y)
        to the closest line segment in the Poly (int[] xpts, ypts) */
    public final static float closestPolyDistance(
	int[] xpts, int[] ypts, int ptx, int pty, boolean connected)
    {
	int npts = (connected) ? xpts.length : xpts.length-1;
	if (npts == 1) return distance(xpts[0], ypts[0], ptx, pty);
	if (npts == 0) return Float.POSITIVE_INFINITY;

	Point from = new Point(0,0), to = new Point(0,0);
	float temp, distance = Float.POSITIVE_INFINITY;
	int i, j;

 	from.x = xpts[0];
	from.y = ypts[0];
	for (i=0, j=1; i<npts; i++, j = (i+1)%xpts.length)
	{
	    to.x = xpts[j];
	    to.y = ypts[j];
	    temp = distance_to_line(from.x, from.y, to.x, to.y, ptx, pty);
// 	    System.out.println(
// 		"\tdistance from line (" + from.x + "," + from.y + "<->" +
// 		to.x + "," + to.y + ") to point (" + ptx + "," + pty + ")=" +
// 		temp);
	    if (temp < distance) distance = temp;
	    from.x = to.x;
	    from.y = to.y;
	}
	return distance;
    }

    /** distance() - 2D distance formula */
    public final static float distance(int x1, int y1, int x2, int y2)
    {
	int xdiff = x2 - x1;
	int ydiff = y2 - y1;
	return (float)Math.sqrt((float)(xdiff * xdiff + ydiff * ydiff));
    }

    /** distance_to_endpoint() - distance to closest endpoint */
    public final static float distance_to_endpoint(
	int x1, int y1, int x2, int y2, int x, int y)
    {
	return (float)Math.min(distance(x1,y1,x,y), distance(x2,y2,x,y));
    }

    /****************************************************************
     *
     * distance_to_line(): Compute the distance from point (x,y) to a
     *	line by computing the perpendicular line from (x,y) to the
     *	line and finding the intersection of this perpendicular and
     *	the line.  If the intersection is on the line segment, then
     *	the distance is the distance from the mouse to the
     *	intersection, otherwise it is the distance from (x,y) to the
     *	nearest endpoint.
     *
     *	Equations used to compute distance:
     *	m = (y2-y1)/(x2-x1) slope of the line
     *	y = mx + b    equation of the line
     *	c = -1/m      slope of line perpendicular to it
     *	y = cx + d    equation of perpendicular line
     *	xi = (d-b)/(m-c) x-intersection, from equating the two line equations
     *	y1 = c* xi + d   y-intersection
     *	distance = sqrt(sqr(x-xi) + sqr(y-yi)) distance between two points
     *
     ****************************************************************/
    public final static float distance_to_line(
	int x1, int y1, int x2, int y2, int x, int y)
    {
	float m;	/* slope of the line */
	float c;	/* slope of a line perpendicular to the line */
	float b;	/* y intercept of line */
	float d;	/* y intercept of a line perpendicular to the line */
	int xi, yi;	/* intersection of line and perpendicular */

	if (x2 == x1)	/* vertical line */
	{
	    if (y1 <= y && y <= y2 || y2 <= y && y <= y1)
		return (float)Math.abs(x - x1);   // mouse is alongside line 
	    return distance_to_endpoint(x1, y1, x2, y2, x, y);
	}

	if (y2 == y1)	/* horizontal line */
	{
	    if (x1 <= x && x <= x2 || x2 <= x && x <= x1)
		return (float)Math.abs(y - y1);   // mouse is alongside line
	    return distance_to_endpoint(x1, y1, x2, y2, x, y);
	}

	m = ((float)(y2 - y1)) / ((float)(x2 - x1));  /* slope of the line */
	c = -1.0f / m;                       /* slope of perpendicular line */
	d = (float) y - c * (float) x;/* perpendicular line through mouse */
	b = (float) y1 - m * (float) x1;  /* the line in the drawing */

	// NOTE: round error
	xi = (int) MoreMath.qint((d - b)/(m - c));// x intersection
	yi = (int) MoreMath.qint(c * (float) xi + d);// y intersection

	/*
	 *  If intersection is on the line segment
	 *  distance is distance from mouse to it.
	 */
	if ((x1 <= xi && xi <= x2 || x2 <= xi && xi <= x1) &&
	    (y1 <= yi && yi <= y2 || y2 <= yi && yi <= y1))
	    return distance(xi, yi, x, y);

	/* distance is distance from mouse to nearest endpt */
	return distance_to_endpoint(x1, y1, x2, y2, x, y);
    }

//////////////////////////////////////////////////////////////////////////

    /* generateWideLine() - generates a line with width lw, returns an
       OMVector of 4 x-y coords. */
    public static OMVector generateWideLine(
	int lw, int x1, int y1, int x2, int y2)
    {
	OMVector ret_val = new OMVector(2);
	int[] x = new int[4];
	int[] y = new int[4];

	// calculate the offsets
// 	lw = lw -1;
	int off1 = (int)lw/2;
	int off2 = (lw%2 == 1) ? (int)lw/2 + 1 : (int)lw/2;

	// slope <= 1
	if (Math.abs((float)(y2 - y1) / (float)(x2 - x1)) <= 1f) {
	    x[0] = x[3] = x1;
	    x[1] = x[2] = x2;

	    y[0] = y1 + off1;
	    y[1] = y2 + off1;
	    y[2] = y2 - off2;
	    y[3] = y1 - off2;

	    ret_val.add(x);
	    ret_val.add(y);
	}

	// slope > 1
	else {
	    x[0] = x1 + off1;
	    x[1] = x2 + off1;
	    x[2] = x2 - off2;
	    x[3] = x1 - off2;

	    y[0] = y[3] = y1;
	    y[1] = y[2] = y2;

	    ret_val.add(x);
	    ret_val.add(y);
	}

	return ret_val;
    }

    /** generateWidePoly() - generates a polygon or polyline with
        positive width lw, returns OMVector of x-y array pairs of
        coordinates of polygon segments. the parameter altx must
        either be null, or a mirror image of xpts. */
    public static OMVector generateWidePoly(
	int lw, int[] xpts, int[] ypts, int[] altx, boolean connect)
    {
	return generateWidePoly(lw, xpts.length, xpts, ypts, altx, connect);
    }

    public static OMVector generateWidePoly(
	int lw, int len, int[] xpts, int[] ypts, int[] altx, boolean connect)
    {
	OMVector ret_val = new OMVector(len*4);
	int off1 = 0, off2 = 0;
	int[] x = null, y = null, a_x = null;

	int end = (connect) ? len : len-1;
// 	lw = lw -1;

	for (int i=0, j=1; i<end; i++, j=(i+1)%len)
	{
	    x = new int[4];
	    y = new int[4];

	    // calculate the offsets  - HACK not consistent?
	    off1 = (int)lw/2;
	    off2 = (lw%2 == 1) ? (int)lw/2 + 1 : (int)lw/2;

	    // slope <= 1
	    if (Math.abs((float)(ypts[j] - ypts[i]) /
			 (float)(xpts[j] - xpts[i])) <= 1f)
	    {
		x[0] = x[3] = xpts[i];
		x[1] = x[2] = xpts[j];

		y[0] = ypts[i] + off1;
		y[1] = ypts[j] + off1;
		y[2] = ypts[j] - off2;
		y[3] = ypts[i] - off2;

		ret_val.add(x);
		ret_val.add(y);

		if (altx != null) {
		    a_x = new int[4];
		    a_x[0] = a_x[3] = altx[i];
		    a_x[1] = a_x[2] = altx[j];
		    ret_val.add(a_x);
		    ret_val.add(y);
		}
	    }

	    // slope > 1
	    else {
		x[0] = xpts[i] + off1;
		x[1] = xpts[j] + off1;
		x[2] = xpts[j] - off2;
		x[3] = xpts[i] - off2;

		y[0] = y[3] = ypts[i];
		y[1] = y[2] = ypts[j];

		ret_val.add(x);
		ret_val.add(y);

		if (altx != null) {
		    a_x = new int[4];
		    a_x[0] = altx[i] + off1;
		    a_x[1] = altx[j] + off1;
		    a_x[2] = altx[j] - off2;
		    a_x[3] = altx[i] - off2;
		    ret_val.add(a_x);
		    ret_val.add(y);
		}
	    }
	}
	return ret_val;
    }

//////////////////////////////////////////////////////////////////////////

    /** main() - for testing */
    public static void main(String[] args) {

	// 3-4-5 triangle
	System.out.println(distance(0,0,3,4));
	System.out.println(distance(0,0,-3,4));
	System.out.println(distance(0,0,-3,-4));
	System.out.println(distance(0,0,3,-4));
	System.out.println();

	System.out.println(distance_to_line(0,0,2,2, 0,2));	// root 2
	System.out.println(distance_to_line(0,0,2,0, 0,2));	// 2
	System.out.println(distance_to_line(0,0,2,0, -1,-1));	// root 2
	System.out.println(distance_to_line(0,0,2,0, 1,0));	// 0
	System.out.println(distance_to_line(0,0,2,2, 1,0));	// rounded!
	System.out.println();

	int[] xpts = new int[3];
	int[] ypts = new int[3];
	xpts[0] = 0; ypts[0] = 0;
	xpts[1] = 3; ypts[1] = 0;
	xpts[2] = 3; ypts[2] = 4;

	System.out.println(closestPolyDistance(xpts, ypts, 0,4, true));
	System.out.println(closestPolyDistance(xpts, ypts, 0,4, false));//3

	xpts[0] = 0; ypts[0] = 0;
	xpts[1] = 2; ypts[1] = 0;
	xpts[2] = 2; ypts[2] = 2;
	System.out.println(closestPolyDistance(xpts, ypts, 0,1, true));//round
	System.out.println(closestPolyDistance(xpts, ypts, 0,1, false));//1

	// linewidth testing

	System.out.println("");
	OMVector vec = generateWideLine(3, 0, 0, 5, 5);
	vec.resetEnumerator();
	int[] x = (int[])vec.nextElement(true);
	int[] y = (int[])vec.nextElement(true);
	System.out.print("wide line: ");
	for (int i = 0; i<x.length; i++) {
	    System.out.print(x[i] + "," + y[i] + " ");
	}
	System.out.println("");

	System.out.println("");
	vec = generateWideLine(4, 0, 0, -5, -3);
	vec.resetEnumerator();
	x = (int[])vec.nextElement(true);
	y = (int[])vec.nextElement(true);
	System.out.print("wide line: ");
	for (int i = 0; i<x.length; i++) {
	    System.out.print(x[i] + "," + y[i] + " ");
	}
	System.out.println("");
	System.out.println("");

	xpts = new int[4];
	ypts = new int[4];
	xpts[0] = 0; ypts[0] = 0;
	xpts[1] = 5; ypts[1] = 2;
	xpts[2] = 4; ypts[2] = 8;
	xpts[3] = -2; ypts[3] = 6;
	vec = generateWidePoly(3, xpts, ypts, null, false);
	vec.resetEnumerator();
	while (vec.hasMoreElements()) {
	    x = (int[])vec.nextElement(true);
	    y = (int[])vec.nextElement(true);
	    System.out.print("wide line: ");
	    for (int i = 0; i<x.length; i++) {
		System.out.print(x[i] + "," + y[i] + " ");
	    }
	    System.out.println("");
	}
    }
}
