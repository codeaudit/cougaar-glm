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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.util.OptionPane;
import org.cougaar.util.UnaryPredicate;

/** Displays information in a vertical bar graph.
 */

public class UIBarGraphDisplay implements Runnable, ActionListener, UISubscriber {
  private UIPlugin uiPlugin;
  private String planName;
  private MessageAddress clusterId;
  private String command;
  private UIBarGraph uiBarGraph;
  private UIBarGraphSource dataSource;
  private Vector myAssets = new Vector(10);
  private boolean graphDisplayed = false;
  private String title = "";
  private UIFrame uiFrame;

  /** Contains the correspondence between display and data source.
   */

  /** Display the specified data (either assets or scheduled assets)
    in a bar graph.
    All the work is done in the run method so
    that the main user interface thread which creates this,
    isn't waiting to fetch the information needed for the bar graph.
    @param uiPlugin this user interface plugin
    @param planName the name of the plan for which to display information
    @param clusterId the cluster for which to display information
    @param command UIDisplay.ASSET_SCHEDULE_COMMAND or UIDisplay.ASSETS_COMMAND
   */

  public UIBarGraphDisplay(UIPlugin uiPlugin, String planName, 
			   MessageAddress clusterId, String command) {
    this.uiPlugin = uiPlugin;
    this.planName = planName;
    this.clusterId = clusterId;
    this.command = command;
    //    uiPlugin.subscribe(this, assetPredicate());
  }

  private static UnaryPredicate assetPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	//System.out.println("Predicate called with: " + o.toString());
	return ( o instanceof Asset );
      }
    };
  }

  public synchronized void subscriptionChanged(IncrementalSubscription container) {
    Enumeration added = container.getAddedList();
    while (added.hasMoreElements())
      myAssets.addElement(added.nextElement());
    Enumeration removed = container.getRemovedList();
    while (removed.hasMoreElements())
      myAssets.removeElement(removed.nextElement());
    Enumeration changed = container.getChangedList();
    while (changed.hasMoreElements()) {
      Object o = changed.nextElement();
      myAssets.removeElement(o);
      myAssets.addElement(o);
    }
  }

  /** Create the data source for the bar graph, the bar graph, and its window.
    Fill in the graph here so that delays in accessing information
    from clusters don't affect the main user interface thread.
    Register as a listener for changes in the information in the bar graph.
    */

  public void run() {
    title = "";

    try {
      if (command.equals(UIDisplay.ASSET_SCHEDULE_COMMAND)) {
	// asset name is null for graph all assets
	dataSource = new UISchedule(uiPlugin, planName, clusterId, null, this);
	title = "Asset Schedule in " + 
	  clusterId.getAddress();
      }
      else if (command.equals(UIDisplay.SINGLE_ASSET_SCHEDULE_COMMAND)) {
	Vector tmp = uiPlugin.getPhysicalAssetNames();
	String assetNames[] = new String[tmp.size()];
	tmp.copyInto(assetNames);
	String assetChosen = 
	  (String) OptionPane.showInputDialog(new JFrame(),
                                              "Choose an asset:", "Asset",
                                              OptionPane.QUESTION_MESSAGE,
                                              null, assetNames, null);
	if (assetChosen == null)
	  return;
	// create information for bar graph schedule for chosen asset
	dataSource = new UISchedule(uiPlugin, planName, 
				    clusterId, assetChosen, this);
	title = "Single Asset Schedule in " +
	  clusterId.getAddress();
      }
      else if (command.equals(UIDisplay.ASSETS_COMMAND)) {
	dataSource = new UIAssets(uiPlugin, planName, clusterId, this);
	title = "Assets in " +
	  clusterId.getAddress();
      } else {
	new UIDisplayError("This request is not supported.");
	return;
      }
    } catch (UINoPlanException e) {
      new UIDisplayError(e.getMessage());
      return;
    }
    uiBarGraph = new UIBarGraph();
    // create frame with update function (if non-local) and no save function
    uiFrame = new UIFrame(title, uiBarGraph, this, false, false, false);
    //    dataSource.registerListener(this);
    dataSource.startSubscription();
  }


  /** Redisplay bar graph.
    This gets called:
    if the dataSource (a collection) is notified that it was changed or
    the user requests an update,
    i.e. this is used for changes stimulated either internally 
    (the data changed) or externally (the user wants an update).
    The reason for the duality is that we can listen for changes in the
    LOCAL cluster, but not in REMOTE clusters.
    If the change was stimulated internally, then the data source got
    the change, and updated its values, and this simply redisplays;
    if the change was stimulated externally (by the user), then this
    first calls the data source to update the values, and then redisplays.
    @param e the action event (object added, deleted or changed)
   */

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source instanceof UIBarGraphSource) {
      uiBarGraph.setParameters(dataSource.getNumberOfXIntervals(),
			       dataSource.getXLegend(),
			       dataSource.getXLabels(),
			       dataSource.getNumberOfYIntervals(),
			       dataSource.getYLegend(),
			       dataSource.getYLabels(),
			       dataSource.getLegend(),
			       dataSource.getValues());
    } else if (e.getActionCommand().equals(UIFrame.UPDATE_COMMAND)) {
	dataSource.update();
	uiBarGraph.setParameters(dataSource.getNumberOfXIntervals(),
			       dataSource.getXLegend(),
			       dataSource.getXLabels(),
			       dataSource.getNumberOfYIntervals(),
			       dataSource.getYLegend(),
			       dataSource.getYLabels(),
			       dataSource.getLegend(),
			       dataSource.getValues());
    } else
	System.out.println("UIBarGraphDisplay: unknown notification.");
  }


}



