/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */


package org.cougaar.domain.mlm.debug.ui;

import java.awt.event.ActionListener;

/** Interface which must be implemented by any object that wants
  to be displayed as a bar graph.
  */

public interface UIBarGraphSource {

  /** Return the number of intervals to display on the x-axis.
    @return number of x-axis intervals
    */

  public int getNumberOfXIntervals();

  /** Return the string to display beneath the x-axis.
    @return string to display beneath the x-axis
    */

  public String getXLegend();

  /** Return the strings to display beneath the x-axis tick marks.
    @return strings to display beneath x-axis tick marks
    */

  public String[] getXLabels();
  
  /** Return the number of intervals to display on the y-axis.
    @return number of y-axis intervals
    */

  public int getNumberOfYIntervals();

  /** Return the string to display to the left of the y-axis
    @return string to display for y-axis
    */

  public String getYLegend();

  /** Return the strings to display to the left of the y-axis tick marks
    @return strings to display on y-axis tick marks
    */

  public String[] getYLabels();

  /** Return the strings to use in a legend explaining the vertical bar sets.
    @return strings (one for each vertical bar) explaining the vertical bars
    */

  public String[] getLegend();

  /** Return the values to display.
    @return sets of values for vertical bars
    */

  public int[][] getValues();

  /** Return whether or not to display the bars contiguously.
    @return true for contiguous bars
    */

  public boolean getContiguous();

  /** Register for changes in the information displayed in the bar graph.
    @param listener object to notify when data changes
   */
  public void registerListener(ActionListener listener);

  /** Update the data fetched with the above methods.
   */

  public void update();

  /** Start subscription which will get data from CCV2 collection.
   */

  public void startSubscription();

}
