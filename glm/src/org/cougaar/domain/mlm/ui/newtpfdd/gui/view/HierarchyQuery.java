package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;

public class HierarchyQuery implements Query {
    private boolean leavesOnly = false;
    public boolean getLeavesOnly() { return leavesOnly; };
    public void setLeavesOnly(boolean leavesOnly) { this.leavesOnly = leavesOnly; };
}
