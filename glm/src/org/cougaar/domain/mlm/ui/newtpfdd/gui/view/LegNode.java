package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;

import java.util.HashMap;

public class LegNode extends ItineraryNode {
    private int tripTag;

    public static final int TRIP_TAG = 26;
    public static final int NON_TRIP_TAG = 27;

    public LegNode(HashMap idToNode, String nodeDBID) {
	super(idToNode,nodeDBID);
    }

    public int getTripTag() { return tripTag; }
    public void setTripTag(int tripTag) { this.tripTag = tripTag; }

}
