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


package org.cougaar.mlm.debug.ui;

import java.awt.event.ActionListener;

/** Interface which must be implemented by any object that wants
  to be displayed as a bar graph.
  */

public interface UIBarGraphSource {

  /** Return the number of intervals to display on the x-axis.
    @return number of x-axis intervals
    */

  int getNumberOfXIntervals();

  /** Return the string to display beneath the x-axis.
    @return string to display beneath the x-axis
    */

  String getXLegend();

  /** Return the strings to display beneath the x-axis tick marks.
    @return strings to display beneath x-axis tick marks
    */

  String[] getXLabels();
  
  /** Return the number of intervals to display on the y-axis.
    @return number of y-axis intervals
    */

  int getNumberOfYIntervals();

  /** Return the string to display to the left of the y-axis
    @return string to display for y-axis
    */

  String getYLegend();

  /** Return the strings to display to the left of the y-axis tick marks
    @return strings to display on y-axis tick marks
    */

  String[] getYLabels();

  /** Return the strings to use in a legend explaining the vertical bar sets.
    @return strings (one for each vertical bar) explaining the vertical bars
    */

  String[] getLegend();

  /** Return the values to display.
    @return sets of values for vertical bars
    */

  int[][] getValues();

  /** Return whether or not to display the bars contiguously.
    @return true for contiguous bars
    */

  boolean getContiguous();

  /** Register for changes in the information displayed in the bar graph.
    @param listener object to notify when data changes
   */
  void registerListener(ActionListener listener);

  /** Update the data fetched with the above methods.
   */

  void update();

  /** Start subscription which will get data from CCV2 collection.
   */

  void startSubscription();

}
