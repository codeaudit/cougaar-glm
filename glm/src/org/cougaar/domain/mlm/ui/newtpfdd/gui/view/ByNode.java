package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;

import java.util.HashMap;

public class ByNode extends Node {
    private int typeCargoCarrier;
    public static final int CARGO_TYPE = 0;
    public static final int CARRIER_TYPE = 1;

    public ByNode(HashMap idToNode, String nodeDBID) {
	super(idToNode,nodeDBID);
    }

    public int getTypeCargoCarrier() { return typeCargoCarrier; }
    public void setTypeCargoCarrier(int typeCargoCarrier) { this.typeCargoCarrier = typeCargoCarrier; }
}
