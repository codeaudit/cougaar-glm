package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;

import java.util.HashMap;

public class TypeNode extends ByNode {
    private String carrierName;
    private String carrierType;
    private String cargoName;
    private String cargoType;
    private String carrierNameDBID;
    private String carrierTypeDBID;
    private String cargoNameDBID;
    private String cargoTypeDBID;
    
    public TypeNode(HashMap idToNode, String nodeDBID) {
	super(idToNode,nodeDBID);
    }

    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }

    public String getCarrierType() { return carrierType; }
    public void setCarrierType(String carrierType) { this.carrierType = carrierType; }

    public String getCargoName() { return cargoName; }
    public void setCargoName(String cargoName) { this.cargoName = cargoName; }

    public String getCargoType() { return cargoType; }
    public void setCargoType(String cargoType) { this.cargoType = cargoType; }

    public String getCarrierNameDBID() { return carrierNameDBID; }
    public void setCarrierNameDBID(String carrierNameDBID) { this.carrierNameDBID = carrierNameDBID; }

    public String getCarrierTypeDBID() { return carrierTypeDBID; }
    public void setCarrierTypeDBID(String carrierTypeDBID) { this.carrierTypeDBID = carrierTypeDBID; }

    public String getCargoNameDBID() { return cargoNameDBID; }
    public void setCargoNameDBID(String cargoNameDBID) { this.cargoNameDBID = cargoNameDBID; }

    public String getCargoTypeDBID() { return cargoTypeDBID; }
    public void setCargoTypeDBID(String cargoTypeDBID) { this.cargoTypeDBID = cargoTypeDBID; }
}
