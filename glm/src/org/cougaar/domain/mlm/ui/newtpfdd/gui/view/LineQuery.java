package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;

import java.util.List;

public class LineQuery implements Query {
    
    private List unitIDList = null;
    private List carrierPrototypeIDList = null;
    private List cargoPrototypeIDList = null;
    private List carrierInstanceIDList = null;
    private List cargoInstanceIDList = null;
    private boolean fullLines = true;

    public List getUnitIDList() { return unitIDList; }
    public void setUnitIDList(List unitIDList) { this.unitIDList = unitIDList; }

    public List getCarrierPrototypeIDList() { return carrierPrototypeIDList; };
    public void setCarrierPrototypeIDList(List carrierPrototypeIDList) { this.carrierPrototypeIDList = carrierPrototypeIDList; };

    public List getCargoPrototypeIDList() { return cargoPrototypeIDList; };
    public void setCargoPrototypeIDList(List cargoPrototypeIDList) { this.cargoPrototypeIDList = cargoPrototypeIDList; };

    public List getCarrierInstanceIDList() { return carrierInstanceIDList; };
    public void setCarrierInstanceIDList(List carrierInstanceIDList) { this.carrierInstanceIDList = carrierInstanceIDList; };

    public List getCargoInstanceIDList() { return cargoInstanceIDList; };
    public void setCargoInstanceIDList(List cargoInstanceIDList) { this.cargoInstanceIDList = cargoInstanceIDList; };

    public boolean getFullLines() { return fullLines; }
    public void setFullLines(boolean fullLines) { this.fullLines = fullLines; }
}
