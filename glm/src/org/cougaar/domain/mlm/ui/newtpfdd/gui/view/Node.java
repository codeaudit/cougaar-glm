package org.cougaar.domain.mlm.ui.newtpfdd.gui.view;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;

import java.text.SimpleDateFormat;

import org.w3c.dom.Element;

import org.cougaar.domain.mlm.ui.newtpfdd.TPFDDConstants;

import org.cougaar.domain.mlm.ui.newtpfdd.xml.Location;
import org.cougaar.domain.mlm.ui.newtpfdd.xml.LogPlanObject;

public class Node extends LogPlanObject implements Serializable, Cloneable {

    // Tree Hierarchy Information
    private transient HashMap idToNode;
    private transient List children = new ArrayList(); // of DBID
    private String parentUUID; // parents name
    private String nodeDBID; // self-identifier

    // Real Data
    private String unitName;
    private String unitNameDBID;
    private String from;
    private String to;
    private String fromCode;
    private String toCode;
    private Date readyAt;     // preference
    private Date earlyEnd;    // preference
    private Date bestEnd;     // preference
    private Date lateEnd;     // preference
    private Date actualStart; // allocated time 
    private Date actualEnd;   // allocated time 

    // Display Information
    private int mode;
    private String displayName;
    private String longName;

    // Static finals for Mode Field
    public static final int MODE_SEA = 1;
    public static final int MODE_AIR = 2;
    public static final int MODE_GROUND = 3;
    public static final int MODE_ITINERARY = 7;
    public static final int MODE_AGGREGATE = 8;
    public static final int MODE_UNKNOWN = 9;

    // Constructors
    public Node(HashMap idToNode, String nodeDBID) {
	super(nodeDBID,null);
	this.nodeDBID = nodeDBID;
	this.idToNode = idToNode;
    }
    // Consturctor for XML
    public Node(HashMap idToNode, String nodeDBID, Element xml) {
	super(nodeDBID,xml);
	this.nodeDBID = nodeDBID;
	this.idToNode = idToNode;
    }

    // G/SETTERS FOR TREE STUFF
    public String getParentUUID() { return parentUUID; }
    public void setParentUUID(String parentUUID) { this.parentUUID = parentUUID; }

    public Node getParent() { 
	return (Node)idToNode.get(parentUUID); 
    }
    public void setParent(Node parent) { 
	parentUUID = parent.getUUID();
	idToNode.put(parentUUID,parent);
    }

    public void addChild(Node child) {
	children.add(child.getUUID());
	idToNode.put(child.getUUID(),child);
    }
    public void removeChild(Node child) {
	children.remove(child);
    }
    public int getChildCount() {
	return children.size();
    }
    public Node getChild(int index) {
	if ( index < 0 || index > children.size() ) {
	    System.err.println("Node.getChild using bad index: "+index);
	    return null;
	}
	return (Node)children.get(index);
    }
    public int indexOf(Node node) {
	return children.indexOf(node);
    }
    public boolean hasChildren() {
	return children != null && !children.isEmpty();
    }
    public List getChildren()
    {
	return children;
    }


    // G/SETTERS FOR REAL DATA
    public String getUnitName() { return unitName; }; 
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getUnitNameDBID() { return unitNameDBID; }; 
    public void setUnitNameDBID(String unitNameDBID) { this.unitNameDBID = unitNameDBID; }

    public String getFrom() { return from; }; 
    public String getFromName() { return from; }; 
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }; 
    public String getToName() { return to; }; 
    public void setTo(String to) { this.to = to; }

    public String getFromCode() { return fromCode; }; 
    public void setFromCode(String fromCode) { this.fromCode = fromCode; }

    public String getToCode() { return toCode; }; 
    public void setToCode(String toCode) { this.toCode = toCode; }

    public Date getReadyAt() { return readyAt; }; 
    public void setReadyAt(Date readyAt) { this.readyAt = readyAt; }
    
    public Date getEarlyEnd() { return earlyEnd; }; 
    public void setEarlyEnd(Date earlyEnd) { this.earlyEnd = earlyEnd; }
    
    public Date getBestEnd() { return bestEnd; }; 
    public void setBestEnd(Date bestEnd) { this.bestEnd = bestEnd; }
    
    public Date getLateEnd() { return lateEnd; }; 
    public void setLateEnd(Date lateEnd) { this.lateEnd = lateEnd; }
    
    public Date getActualStart() { return actualStart; }; 
    public void setActualStart(Date actualStart) { this.actualStart = actualStart; }
    
    public Date getActualEnd() { return actualEnd; }; 
    public void setActualEnd(Date actualEnd) { this.actualEnd = actualEnd; }

    // G/SETTERS FOR DISPLAY STUFF
    public int getMode() { return mode; }
    public void setMode(int mode) { this.mode = mode; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getLongName() { return longName; }
    public void setLongName(String longName) { this.longName = longName; }


    public String toString() {
	return nodeDBID; 
    }

    // DATE OUTPUT STUFF
    private static SimpleDateFormat shortFormat = new SimpleDateFormat("M/d");
    public static String shortDate(Date date) { return shortFormat.format(date); }  
    private static SimpleDateFormat longFormat = new SimpleDateFormat("HH:mm M/d");
    public static String longDate(Date date) { return longFormat.format(date); }

    // MISCELLANEOUS UTILITIES
    public boolean isTransport() {
	return (this instanceof ItineraryNode || this instanceof LegNode);
    }
    public boolean isStructural() {
	return !isTransport();
    }
    public boolean isRoot() {
	return nodeDBID.equals(TPFDDConstants.ROOTNAME);
    }

    // POTENTIAL ISSUES
    // 1) Tasks may need an update system so that when they change they can
    //    alert someone so they can be redrawn or whatever. This existed as
    //    proxyChangeNotify in a previous incarnation.

    // The following is some crappy stuff leftover from Node kept for compatibility
    // Try to not use this stuff and is possible get rid of it
    public static final int ROLLUP = 1;
    public static final int ROLLUP_FORCED_ROOT = 2;
    public static final int EQUIPMENT = 3;
    public static final int BY_CARRIER_TYPE = 4;
    public static final int BY_CARRIER_NAME = 5;
    public static final int BY_CARGO_TYPE = 6;
    public static final int BY_CARGO_NAME = 7;
    public static final int CARRIER_TYPE = 8;
    public static final int CARRIER_NAME = 9;
    public static final int CARGO_TYPE = 10;
    public static final int CARGO_NAME = 11;
    public static final int LAST_STRUCTURE_CODE = 11;

    public static final int ITINERARY = 20;
    public static final int ITINERARY_LEG = 21;
    public static final int TASK = 22;
}

