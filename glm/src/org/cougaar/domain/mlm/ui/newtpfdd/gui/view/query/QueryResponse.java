package org.cougaar.domain.mlm.ui.newtpfdd.gui.view.query;

import java.util.Set;
import java.util.HashSet;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.Node;

public class  QueryResponse {
    
    public static final int QR_FAILURE = 0;
    public static final int QR_SUCCESS = 1;
    public static final int QR_WARNING = 2;

    public static final String NORMAL_OP = "Normal Operations";

    private Set nodes;
    private String message;
    private int condition;

    public QueryResponse() {
	condition = QR_SUCCESS;
	message = NORMAL_OP;
	nodes = new HashSet();
    }

    public void addNode(Node node) {
	nodes.add(node);
    }
    public Object[] getNodes() {
	return nodes.toArray();
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message=message; }

    public int getCondition() { return condition; }
    public void setCondition(int condition) { this.condition=condition; }
    
}
