package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;

import java.util.HashMap;

public class LegNode extends ItineraryNode {
    private int tripTag;

    public LegNode(HashMap idToNode, String nodeDBID) {
	super(idToNode,nodeDBID);
    }

    public int getTripTag() { return tripTag; }
    public void setTripTag(int tripTag) { this.tripTag = tripTag; }

}
