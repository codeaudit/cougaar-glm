package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;

public class UnitQuery implements Query {
    public static final int DEPTH_BY = 1;
    public static final int DEPTH_TYPE = 2;
    public static final int DEPTH_ITIN = 3;
    public static final int DEPTH_LEAF = 4;
    
    private String unitID = null;
    private int treeDepth = 1;

    public String getUnitID() { return unitID; };
    public void setUnitID(String unitID) { this.unitID = unitID; };

    public int getTreeDepth() { return treeDepth; };
    public void setTreeDepth(int treeDepth) { this.treeDepth = treeDepth; };
}
