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


package org.cougaar.domain.mlm.debug.ui;

import javax.swing.JComponent;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Font;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;

import org.cougaar.domain.mlm.debug.ui.draw.OMVector;
import org.cougaar.domain.mlm.debug.ui.draw.DrawUtil;

/** Creates a vertical bar graph display.  Takes a two dimensional array
  of values, and uses a different color bar to display each set of values.
  */

public class UIBarGraph extends JComponent {
  private boolean hasValidValues = false;
  private int numberOfXIntervals;
  private String xLegend;
  private String xLabels[];
  private int numberOfYIntervals;
  private String yLegend;
  private String yLabels[];
  private String legend[];
  private int values[][]; // for each bar, the y-value for the xth interval
  private boolean contiguous;
  private int maxAscent;
  private int xLength, yLength;
  private int xIntervalSize;
  private int nBars;
  private int yOrigin;
  private FontMetrics fm;
  private int width;
  private int height;
  private int barWidth;
  private static int X_ORIGIN = 120;
  private static int Y_TOP = 50; // compute y-origin from this
  private static int TICK_HALF_LENGTH = 5;
  private static int X_SPACE = 20; // between the bar sets
  private static int Y_INTERVAL_SIZE = 50;
  private double yIntervalSize;
  private static double Y_AXIS_SIZE = 160.0;
  private static int DEFAULT_BAR_WIDTH = 6;
  private static int MARGIN = 10;
  private double tickSpacing;
  private int tickLabel;
  private int nTicks;
  private static double MIN_TICK_SPACING = 14.0;
  private boolean haveData = false;

  /** Creates a vertical bar graph with the specified information.
    Takes a two dimensional array of values, and uses a different color
    bar to display each set of values.  The legend String array should
    have an entry for each set of values; these strings are displayed to the
    left of the bar graph next to a line of the appropriate color.
    @param numberOfXIntervals number of intervals on x-axis
    @param xLegend string to display underneath x-axis
    @param xLabels strings to display beneath the x-axis tick marks
    @param numberOfYIntervals number of intervals on y-axis
    @param yLegend string to display to the left of y-axis
    @param yLabels strings to display to the left of the y-axis tick marks
    @param legend strings to display with bar graph colors
    @param values values of sets of vertical bars
    @param contiguous true to make vertical bars contiguous
    */

  public UIBarGraph() {
  }

  private synchronized void setValidValues(boolean isCacheValid) {
    hasValidValues = isCacheValid;
  }

  private synchronized boolean getValidValues() {
    return hasValidValues;
  }

  /** Set new values for bar graph and repaint.
    @param numberOfXIntervals number of intervals on x-axis
    @param xLegend string to display underneath x-axis
    @param xLabels strings to display beneath the x-axis tick marks
    @param numberOfYIntervals number of intervals on y-axis
    @param yLegend string to display to the left of y-axis
    @param yLabels strings to display to the left of the y-axis tick marks
    @param legend strings to display with bar graph colors
    @param values values of sets of vertical bars
    */

  public synchronized void setParameters(int numberOfXIntervals, 
				  String xLegend, String xLabels[],
				  int numberOfYIntervals, 
				  String yLegend, String yLabels[],
				  String legend[], int values[][]) {
    this.numberOfXIntervals = numberOfXIntervals;
    this.xLegend = xLegend;
    this.xLabels = xLabels;
    this.numberOfYIntervals = numberOfYIntervals;
    this.yLegend = yLegend;
    this.yLabels = yLabels;
    this.legend = legend;
    this.values = values;
    setValidValues(false);
    haveData = true;
    repaint();
  }

  /** Figure out where to put tick marks and how to label them.
    Count by 10s, 100s, or 1000s and have at least MIN_TICK_SPACING intervals.
    */

  private void setTickSpacing(double interval) {
    if (interval > MIN_TICK_SPACING) {
      tickSpacing = interval;
      tickLabel = 1;
    }
    else if ((interval * 10.0) > MIN_TICK_SPACING) {
      tickSpacing = interval * 10.0;
      tickLabel = 10;
    } else if ((interval * 100.0) > MIN_TICK_SPACING) {
      tickSpacing = interval * 100.0;
      tickLabel = 100;
    } else if ((interval * 1000.0) > MIN_TICK_SPACING) {
      tickSpacing = interval * 1000.0;
      tickLabel = 1000;
    } else if ((interval * 10000.0) > MIN_TICK_SPACING) {
      tickSpacing = interval * 10000.0;
      tickLabel = 10000;
    } else {
      tickSpacing = 0.0;
      tickLabel = 0;
      nTicks = 0; // prevents them from being drawn
      return;
    }
    nTicks = (int)(Y_AXIS_SIZE / tickSpacing);
  }

  private synchronized void computeGraphParameters() {
    Graphics g = getGraphics();
    g.setFont(new Font("Helvetica", Font.PLAIN, 12));
    fm = g.getFontMetrics();
    maxAscent = fm.getMaxAscent();
    // compute some values used throughout
    if (values == null)
      nBars = 0;
    else
      nBars = values.length;
    yIntervalSize = Y_AXIS_SIZE / (numberOfYIntervals);
    setTickSpacing(yIntervalSize);
    yOrigin = Y_TOP + (int)Y_AXIS_SIZE;
    // determine the minimum width so that x-labels don't overlap
    int maxWidth = 0;
    for (int i = 0; i < numberOfXIntervals; i++)
      maxWidth = Math.max(fm.stringWidth(xLabels[i]), maxWidth);
    // adjust x interval to accomodate labels if necessary
    // if want contiguous bars, then make the width of the bars
    // be the width of the interval
    if (contiguous) {
      if (nBars == 0) { // special case to get an empty graph
	barWidth = DEFAULT_BAR_WIDTH;
	xIntervalSize = maxWidth + MARGIN;
      } else {
	xIntervalSize = nBars * 2; // minimum bar width
	xIntervalSize = Math.max(xIntervalSize, maxWidth + 2); // min margin
	barWidth = (xIntervalSize / nBars) + 1;
	xIntervalSize = barWidth * nBars;
      }
    }
    else {
      xIntervalSize = nBars * DEFAULT_BAR_WIDTH + X_SPACE;
      xIntervalSize = Math.max(xIntervalSize, maxWidth + MARGIN);
      barWidth = DEFAULT_BAR_WIDTH;
    }
    // make the x axis one interval longer than needed -- looks better
    xLength = (numberOfXIntervals + 1) * xIntervalSize;
    width = X_ORIGIN + xLength + MARGIN;
    // y axis + labels + legend
    height = yOrigin + maxAscent + MARGIN + maxAscent + MARGIN + MARGIN;
    setValidValues(true);
  }

  /** Overrides method in JComponent which automatically provides
    scrollbars when needed.
    @return the width and height of the bar graph display
    */

  public Dimension getPreferredSize() {
    if (!haveData) return new Dimension(100,100);
    if (!getValidValues())
      computeGraphParameters();
    return new Dimension(width, height);
  }

  /** Draws the bar graph.
    @param g graphics object associated with this object
    */

  public synchronized void paint(Graphics g) {
    if (!haveData) return;
    if (!getValidValues()) 
      computeGraphParameters();
    // draw the x and y axes
    g.setColor(Color.black);
    g.drawLine(X_ORIGIN, yOrigin, X_ORIGIN + xLength, yOrigin);
    g.drawLine(X_ORIGIN, yOrigin, X_ORIGIN, Y_TOP);
    // draw X-axis tick marks
    for (int i = 1; i <= numberOfXIntervals; i++)
      g.drawLine(X_ORIGIN + i * xIntervalSize, yOrigin - TICK_HALF_LENGTH, 
		 X_ORIGIN + i * xIntervalSize, yOrigin + TICK_HALF_LENGTH);
    // draw Y-axis tick marks
    for (int i = 1; i <= nTicks; i++)
      g.drawLine(X_ORIGIN - TICK_HALF_LENGTH, 
		 (int)(yOrigin - i * tickSpacing),
		 X_ORIGIN + TICK_HALF_LENGTH, 
		 (int)(yOrigin - i * tickSpacing));
    // label the X-axis tick marks
    int y = yOrigin + maxAscent + MARGIN;
    for (int i = 1; i <= numberOfXIntervals; i++) {
      String xLabel = xLabels[i-1];
      int w = fm.stringWidth(xLabel);
      g.drawString(xLabel, X_ORIGIN + i * xIntervalSize - w/2, y);
    }
    // label the Y-axis tick marks
    // ignore ylabels passed in and use tick labels instead
    // assumes that the y-axis is simply a quantity
    for (int i = 1; i <= nTicks; i++) {
      String tickS = String.valueOf(i * tickLabel);
      g.drawString(tickS,
		   X_ORIGIN - fm.stringWidth(tickS) - MARGIN,
		   (int)(yOrigin - i * tickSpacing + maxAscent/2));
    }
    // label the axes
    g.drawString(xLegend, 
		 X_ORIGIN + xLength/2 - fm.stringWidth(xLegend)/2,
		 y + fm.getMaxAscent() + MARGIN);
    g.drawString(yLegend, MARGIN, Y_TOP);
    // write the legend
    Color colors[] = { Color.blue, Color.red, Color.green };
    int yPosition = Y_TOP + 50;
    for (int i = 0; i < legend.length; i++) {
      g.setColor(colors[i % legend.length]);
      g.drawString(legend[i], MARGIN, yPosition);
      drawWideLine(g, 70, yPosition - barWidth / 2, 
		   90, yPosition - barWidth / 2, barWidth);
      yPosition = yPosition + maxAscent + MARGIN;
    }
    int xPosition = (int)(X_ORIGIN + xIntervalSize 
      - (nBars/2.0) * barWidth + barWidth/2);
    // draw the bars
    // use yOrigin-1 so the bars don't overwrite the axis
    for (int i = 0; i < nBars; i++) {
      g.setColor(colors[i % nBars]);
      for (int j = 0; j < numberOfXIntervals; j++)
	drawWideLine(g, xPosition + xIntervalSize * j,
		     yOrigin-1,
		     xPosition + xIntervalSize * j,
		     (int)(yOrigin-1 - values[i][j] * yIntervalSize),
		     barWidth);
      xPosition = xPosition + barWidth;
    }
  }

  /** Used to draw the bars in the bar graph.
   */

  private void drawWideLine(Graphics g, int x1, int y1, int x2, int y2, 
			    int lineWidth) {
    // if both width and length are zero, don't draw line
    // DrawUtil generates 1 pixel height lines in this case
    if ((x1 == x2) && (y1 == y2))
      return;
    OMVector vec = 
      DrawUtil.generateWideLine(lineWidth, x1, y1, x2, y2);
    vec.resetEnumerator();
    int[] x = (int[])vec.nextElement(true);
    int[] y = (int[])vec.nextElement(true);
    g.drawPolygon(x, y, 4);
    g.fillPolygon(x, y, 4);
  }

}
