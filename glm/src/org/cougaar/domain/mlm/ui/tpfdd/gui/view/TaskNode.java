/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/gui/view/Attic/TaskNode.java,v 1.2 2000-12-20 18:18:47 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.gui.view;


import java.io.Serializable;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import java.text.SimpleDateFormat;

import org.cougaar.util.TimeSpan;
import java.lang.reflect.Method;

import org.w3c.dom.Element;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;

import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.tpfdd.util.BeanInfoProvider;
import org.cougaar.domain.mlm.ui.tpfdd.util.MismatchException;
import org.cougaar.domain.mlm.ui.tpfdd.util.PathString;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.ExceptionTools;
import org.cougaar.domain.mlm.ui.tpfdd.util.Callback;

import org.cougaar.domain.mlm.ui.tpfdd.aggregation.ServerPlanElementProvider;

import org.cougaar.domain.mlm.ui.tpfdd.xml.Location;
import org.cougaar.domain.mlm.ui.tpfdd.xml.LogPlanObject;

import org.cougaar.domain.mlm.ui.tpfdd.producer.ClusterCache;
import org.cougaar.domain.mlm.ui.tpfdd.producer.PlanElementProvider;
import org.cougaar.domain.mlm.ui.tpfdd.producer.UnitHierarchy;
import org.cougaar.domain.mlm.ui.tpfdd.producer.TreeMap;

import org.cougaar.domain.mlm.ui.psp.transportation.data.UITAssetInfo;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItinerary;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItineraryElement;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItineraryElementCarrier;


public class TaskNode extends LogPlanObject implements ScheduleElement, Serializable, Cloneable
{ 
    // these have been set to match those in UITaskItineraryElement and must stay that way
    // only has meaning for "Transport" tasks
    public static final int MODE_SEA = 1;
    public static final int MODE_AIR = 2;
    public static final int MODE_GROUND = 3;
    public static final int MODE_ITINERARY = 7;
    public static final int MODE_AGGREGATE = 8;
    public static final int MODE_UNKNOWN = 9;

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


  // Various tages
  public static final int HIERARCHY_TAG = 23;
  public static final int DIRECT_TAG = 24;
  public static final int IMPLIED_TAG = 25;
  public static final int TRIP_TAG = 26;
  public static final int NON_TRIP_TAG = 27;
    
  // The cargo and carrier nodes and their children need to
  // know if they are cargo or carrier nodes -- everyone
  // else doesn't care, so we have this type
  public static final int CARGO_CARRIER_NONE = 28;
 
 
  // Let's make the comma a constant!
  public static final String COMMA_SEP = ".";

    /** Common to all tasknode bits **/
    private static SimpleDateFormat longFormat = 
      new SimpleDateFormat("HH:mm M/d");
    private static SimpleDateFormat shortFormat = new SimpleDateFormat("M/d");
    private static int nodeCounter = 0;

    private static final long serialVersionUID = 3141592653589793239L;

    // for callbacks
    private static Method parentMethod = 
      BeanInfoProvider.getWriter(TaskNode.class, "parent_");
    private static Method planElementMethod = 
      BeanInfoProvider.getWriter(TaskNode.class, "planElement_");
    private static Method directObjectMethod = 
      BeanInfoProvider.getWriter(TaskNode.class, "directObject_");
    private static Method workflowMethod = 
      BeanInfoProvider.getWriter(TaskNode.class, "workflow_");
    private static Method carrierMethod = 
      BeanInfoProvider.getWriter(TaskNode.class, "carrier_");

    /** Internal simple bits **/
    private int         sourceType; // one of above types
    private String      parentUUID;
    private String      clusterID;
    private String      source; // unit that originated task
    private String      destination;
    private String      verb;
    private String      forWhom;
    private int         mode;
  private int typeCargoCarrier = CARGO_CARRIER_NONE;
  

    /** Internal structural bits **/
    private transient TaskNode    parent_;	// parent node of this task (might be (Object)root)
    private transient TreeMap  children;
    private transient boolean     startedCalculation; // children calculation is asynchronous; need state
    private transient boolean     contiguityCheck = false;
    private transient boolean     contiguityResult;
    
    // will be UITaskItineraryElement for legs, UITaskItinerary for entire Itinerary
    private Object sourceObject = "[No source available]";
    
    /** Interface bits **/
    /* For non-transport and transport tasks */
    private String      displayName;
    private String      longName;
    private boolean     multi;  // indicates a multiple parent task XXX haven't dealt with this yet
    private boolean     transport; // fields below unused if not
    private int         childCount;
    
    /* Names */
    private String      unitName;
    private String      carrierName;
    private String      carrierType;
    private String      directObjectName;
    private String      directObjectType;

    /* for Itinerary legs only; keep track of the carrier name and type that
       this node's parent represents, for filtering purposes, so that we have
       integrity among all this node's siblings. */
    private String      parentCarrierName;
    private String      parentCarrierType;

    /* Locations */
    private Location    fromLocation;
    private Location    toLocation;
    private String      fromCode;
    private String      toCode;

    /* Dates */
    private long      minStart, maxStart;
    private long      bestStart;
    private long      actualStart, estimatedStart;
    private long      minEnd, maxEnd;
    private long      bestEnd;
    private long      actualEnd, estimatedEnd;

  //Default value
  // This records if this is a direct or implied leg
  // If it's not a leg, then use default value
  private int directTag = HIERARCHY_TAG;
  // This records if this is part of the actual trip
  // and thus should be drawn on the spiffy display and
  // used in the contiguous calculation
  // If it's not a leg, then use default value
  private int tripTag = HIERARCHY_TAG;


    public TaskNode copy()
    {
	TaskNode copy;
	try {
	    copy = (TaskNode)(clone());
	}
	catch ( Exception e ) {
	    OutputHandler.out(ExceptionTools.toString("TN:copy", e));
	    return null;
	}
	return copy;
    }

    private Location geoloc2Location(GeolocLocation geoloc)
    {
	Location location;
	String code = geoloc.getGeolocCode();
	if ( (location = provider.getLocation(code)) == null ) {
	    location = new Location(null);
	    location.setName(geoloc.getName());
	    location.setGeolocCode(code);
	    if ( location.getName() == null )
		location.setName(location.getGeolocCode());
	    location.setInstallationTypeCode(geoloc.getInstallationTypeCode());
	    location.setCountryStateCode(geoloc.getCountryStateCode());
	    location.setCountryStateName(geoloc.getCountryStateName());
	    location.setIcaoCode(geoloc.getIcaoCode());
	    provider.setLocation(code, location);
	}
	return location;
    }

    public void reconstituteSerialized(PlanElementProvider provider)
    {
	super.reconstituteSerialized();
	this.provider = provider;
	children = new TreeMap(provider);
	parent_ = (TaskNode)(provider.warmRead(getParentUUID()));
	if ( isAggregate() )
	    displayName += " (" + childCount + ")";
	childCount = 0; // we don't really have any children; above is valid as display hack for rollups only

	if ( parent_ == null )
	    parent_ = provider.getRoot();
	provider.setMinTaskStart(minStart);
	provider.setMinTaskStart(actualStart);
	provider.setMaxTaskEnd(actualEnd);
	provider.setMaxTaskEnd(maxEnd);

    	if ( parent_ != provider.getRoot() ) {
	    // Debug.out("TN:TN propagate " + UUID + " start " + getActualStart() + " end " + getActualEnd());
	    parent_.propagateStart(getActualStart());
	    parent_.propagateEnd(getActualEnd());
	}

    }

    // the invisible root node.
    public TaskNode()
    {
	super("!ROOT", null);
	setDisplayName("ROOT:SHOULD NOT APPEAR");
    }

    // rollup of all nodes from a particular request into a single TPFDD line.
    public TaskNode(PlanElementProvider provider, String UUID)
    {
	super(UUID, null);
	this.provider = provider;
	children = new TreeMap(provider);
	startedCalculation = true; // don't start standard child calculation
	setParentUUID("!ROOT");
	setParent_(provider.getRoot());
	setDisplayName(UUID);
    }

    // nodes reconstituted from XML.
    public TaskNode(PlanElementProvider provider, String UUID, Element xml)
    {
	super(provider, UUID, xml);

	children = new TreeMap(provider);
	parent_ = (TaskNode)(provider.warmRead(getParentUUID()));
	if ( parent_ == null )
	    parent_ = provider.getRoot();

	if ( parent_ != provider.getRoot() ) {
	    // Debug.out("TN:TN propagate " + UUID + " start " + getActualStart() + " end " + getActualEnd());
	    parent_.propagateStart(getActualStart());
	    parent_.propagateEnd(getActualEnd());
	}
    }

  // the main structural nodes -- 
  // the fixed unit hierarchy from which drilldown begins.
    public TaskNode(ServerPlanElementProvider provider, 
		    String unitName, 
		    boolean forceRoot)
    {
	super("ROLLUP:" + unitName, null);
	this.provider = provider;
	sourceObject = UUID;
	if ( !forceRoot )
	    sourceType = ROLLUP;
	else
	    sourceType = ROLLUP_FORCED_ROOT;
	mode = MODE_AGGREGATE;

	source = unitName;
	children = new TreeMap(provider);
	startedCalculation = true; // don't start standard child calculation

	setUnitName(unitName);
	setVerb("N/A");
	setTransport(false);
	if ( forceRoot ) {
	    setParentUUID("!ROOT");
	    parent_ = provider.getRoot();
	}
	else {
	    String parentUnitName = provider.getUnitTree().getCommander(unitName);
	    if ( parentUnitName == null )
		parent_ = provider.getRoot(); // we must be a top-level node
	    else
		parent_ = provider.getRollupNode(parentUnitName);
	    setParentUUID(parent_.getUUID());
	}

	setFromCode("N/A");
	setToCode("N/A");
	// since we know of no actual children yet, time values will be zero, showing no schedule.
	setDirectObjectName("Roll-up");
	setDisplayName(unitName);
    }

    // equipment nodes -- the parent of all equipment movements of a particular unit only (not children)
    public TaskNode(ServerPlanElementProvider provider, String unitName)
    {
	super("EQUIP:" + unitName, null);
	this.provider = provider;
	sourceObject = UUID;
	sourceType = EQUIPMENT;
	mode = MODE_AGGREGATE;

	source = unitName;
	children = new TreeMap(provider);
	startedCalculation = true;

	setUnitName(unitName);
	setVerb("N/A");
	setTransport(false);

	parent_ = provider.getRollupNode(unitName);
	setParentUUID(parent_.getUUID());

	setFromCode("N/A");
	setToCode("N/A");
	setDirectObjectName("Own equipment");
	setDisplayName(unitName + " EQUIP");
    }

    // intermediate intermediate nodes. you are not expected to understand.
    public TaskNode(ServerPlanElementProvider provider, 
		    String unitName, 
		    int byWhat)
    {
	super("EQUIP/" + byWhat + ":" + unitName, null);
	this.provider = provider;
	sourceObject = UUID;
	sourceType = byWhat;
	mode = MODE_AGGREGATE;

	source = unitName;
	children = new TreeMap(provider);
	startedCalculation = true;
	setUnitName(unitName);
	setVerb("N/A");
	setTransport(false);

	parent_ = provider.getEquipmentNode(unitName);
	setParentUUID(parent_.getUUID());

	setFromCode("N/A");
	setToCode("N/A");
	if ( byWhat == BY_CARRIER_TYPE ) {
	    setDirectObjectName("Sorted by carrier type");
	    setDisplayName("BY CARRIER TYPE");
	}
	else if ( byWhat == BY_CARGO_TYPE ) {
	    setDirectObjectName("Sorted by cargo type");
	    setDisplayName("BY CARGO TYPE");
	}
	else
	    OutputHandler.out("TN:TN Error: alien invasion. " + byWhat);
    }
	    
    // carrier or cargo type nodes -- intermediate drilldown 
  // whereby parents are equipment
  // nodes, nodes themselves represent individual 
  // carrier or carrier TYPES and children are
  // actual itineraries carried by their parent TYPE.
  public TaskNode(ServerPlanElementProvider provider, 
		  String unitName, 
		  String type, 
		  int byWhat)
    {
	super((byWhat == BY_CARRIER_TYPE ? "CARRIER_TYPE/" : "CARGO_TYPE/") + type + ":" + unitName, null);
	this.provider = provider;
	sourceObject = UUID;
	mode = MODE_AGGREGATE;

	source = unitName;
	children = new TreeMap(provider);
	startedCalculation = true;

	setUnitName(unitName);
	setVerb("N/A");
	setTransport(false);

	if ( byWhat == BY_CARRIER_TYPE ) {
	    sourceType = CARRIER_TYPE;
	    setDirectObjectName("Multiple Cargo names");
	    setDirectObjectType("Multiple Cargo types");
	    setCarrierType(type);
	    setCarrierName("Multiple Carrier names");
	    parent_ = provider.getByCarrierNode(unitName);
	}
	else if ( byWhat == BY_CARGO_TYPE ) {
	    sourceType = CARGO_TYPE;
	    setDirectObjectName("Multiple Cargo names");
	    setDirectObjectType(type);
	    setCarrierType("Multiple Carrier types");
	    setCarrierName("Multiple Carrier names");
	    parent_ = provider.getByCargoNode(unitName);
	}
	else
	    OutputHandler.out("TN:TN Error: underwear missing. " + byWhat);

	setParentUUID(parent_.getUUID());

	setFromCode("N/A");
	setToCode("N/A");
	setDisplayName(type);
    }

  // actual itinerary nodes. parents are carrier nodes as above 
  // and children are individual legs.
  public TaskNode(ServerPlanElementProvider provider, 
		  UITaskItinerary itinerary, 
		  String carrierName,
		  String carrierType, 
		  String cargoName, 
		  String cargoType, 
		  int byWhat)
    {
	super(deriveUUID(itinerary, 
	    (byWhat == BY_CARRIER_TYPE ? carrierType : cargoType)), null);
	this.provider = provider;
	// Debug.out("TN:TN{ITIN} derived UUID " + UUID + " this: " + this + " provider: " + provider);
	sourceObject = itinerary;
	sourceType = ITINERARY;
	mode = MODE_ITINERARY;

	source = itinerary.getClusterID();
	children = new TreeMap(provider);
	startedCalculation = true; // don't start standard child calculation
	fromLocation = geoloc2Location(itinerary.getFromRequiredLocation());
	toLocation = geoloc2Location(itinerary.getToRequiredLocation());

	setCarrierName(carrierName);
	setCarrierType(carrierType);
	if ( cargoName == null )
	    cargoName = cargoType;
	setDirectObjectName(cargoName);
	setDirectObjectType(cargoType);
	setUnitName(PathString.basename(itinerary.getTransportedUnitName()));
	setVerb("Mission");
	setTransport(true);
	
	if ( byWhat == BY_CARRIER_TYPE ) {
	    parent_ = provider.getCarrierNode(unitName, carrierType);
	    typeCargoCarrier = CARRIER_TYPE;
	}
	else if ( byWhat == BY_CARGO_TYPE ) {
	    parent_ = provider.getCargoNode(unitName, cargoType);
	    typeCargoCarrier = CARGO_TYPE;
	}
	else
	    OutputHandler.out("TN:TN Error: Help! They're eating me alive!");

	setParentUUID(parent_.getUUID());

	// our legs will propagate to us and tell us our bounds.
	setActualStart(TimeSpan.MAX_VALUE);
	setActualEnd(TimeSpan.MIN_VALUE);

	setDisplayName(UUID.substring(UUID.indexOf(": ") + 2, UUID.length()));
	UITAssetInfo assetInfo = 
	  (UITAssetInfo)(itinerary.getUITAssetInfoVector().get(0));
	setLongName(unitName + ": " + assetInfo.getTypeNomenclature() + "/" + assetInfo.getItemID());
    }

  boolean debugTimes = false;


  //

  // Yee ha!  Actual data nodes
  // lowest level nodes. individual legs of a single itinerary.
    public TaskNode(PlanElementProvider provider, 
		    UITaskItineraryElement leg, 
		    TaskNode parent, 
		    String clusterID,
		    int cargoCarrierType)
    {
	super(deriveNameWithCountAndDot(parent), null);
	sourceType = ITINERARY_LEG;
	sourceObject = leg;
	this.clusterID = clusterID;
	this.provider = provider;
	parent_ = parent;
	unitName = parent_.getUnitName();
	typeCargoCarrier = cargoCarrierType;

	
	if ( leg instanceof UITaskItineraryElementCarrier ) {
	    UITaskItineraryElementCarrier carrierLeg = (UITaskItineraryElementCarrier)leg;
	    setCarrierName(carrierLeg.getCarrierItemNomenclature());
	    String carrierType = carrierLeg.getCarrierTypeNomenclature();
	    int dash = carrierType.indexOf(" - ");
	    if ( dash != -1 )
		carrierType = carrierType.substring(dash + 3, carrierType.length());
	    setCarrierType(carrierType);
	}
	else {
	    setCarrierName("UNK " + unitName + " carrier");
	    setCarrierType("UNK " + unitName + " carrier");
	}
	
	setDirectObjectName(parent_.getDirectObjectName());
	setDirectObjectType(parent_.getDirectObjectType());
	setParentUUID(parent_.getUUID());
	setParentCarrierName(parent_.getCarrierName());
	setParentCarrierType(parent_.getCarrierType());

	startedCalculation = true; // don't start standard calculation; we know there are no children
	
	if ( leg.getVerbRole() == null )
	    verb = "[no verb]";
	else
	    verb = leg.getVerbRole().toString();
	mode = leg.getTransportationMode();
	transport = true;
	fromLocation = geoloc2Location(leg.getStartLocation());
	fromCode = fromLocation.getGeolocCode();
	toLocation = geoloc2Location(leg.getEndLocation());
	toCode = toLocation.getGeolocCode();

	if ( leg.getStartDate() == null )
	    Debug.out("TN:TN Note: null start date for " + UUID + "!");
	else
	    setActualStart(leg.getStartDate().getTime());
	
	if ( leg.getEndDate() == null )
	    Debug.out("TN:TN Note: null end date for " + UUID + "!");
	else
	    setActualEnd(leg.getEndDate().getTime());

	if ( leg.getEndEarliestDate() == null )
	    Debug.out("TN:TN Note: null earliest end date for " + UUID);
	else
	    setMinEnd(leg.getEndEarliestDate().getTime());
	
	if ( leg.getEndLatestDate() == null )
	    Debug.out("TN:TN Note: null latest end date for " + UUID);
	else
	    setMaxEnd(leg.getEndLatestDate().getTime());

	if ( leg.getEndBestDate() == null )
	    Debug.out("TN:TN Note: null best end date for " + UUID);
	else
	    setBestEnd(leg.getEndBestDate().getTime());

	if ( leg.getStartEarliestDate() == null )
	    Debug.out("TN:TN Note: null earliest start date for " + UUID);
	else
	    setMinStart(leg.getStartEarliestDate().getTime());

	// Debug.out("TN:TN{UIIE} start: " + getActualStart() + " end: " + getActualEnd());

	long earliest = Math.min(getActualStart() != 0 ? 
				 getActualStart() : TimeSpan.MAX_VALUE,
				 getMinStart() != 0 ? 
				 getMinStart() : TimeSpan.MAX_VALUE);
	long latest = Math.max(getActualEnd(), getMinEnd());
	
	parent_.propagateStart(earliest);
	parent_.propagateEnd(latest);

	if (debugTimes) {
	  System.out.println ("TaskNode - start actual " + 
			      new Date(getActualStart ()) + 
			      " - min " + new Date(getMinStart ()) + 
			      " parent " + new Date(earliest));
	  System.out.println ("TaskNode - end   actual " 
			      + new Date(getActualEnd   ()) + 
			      " - min " + new Date(getMinEnd   ()) + 
			      " parent " + new Date(latest));
	}

	directObjectName = parent_.getDirectObjectName();
	displayName = parent_.getDisplayName() + COMMA_SEP;
    }


  // This is a little static function that uses a string buffer
  // and makes things faster (hopefully)
  public static String deriveNameWithCountAndDot(TaskNode parent) {
    StringBuffer buff = new StringBuffer();
    buff.append(parent.getUUID());
    buff.append(COMMA_SEP);
    buff.append(parent.getChildCount_());
    return buff.toString();
  }


    public static String deriveUUID(UITaskItinerary itinerary, String type)
    {
	UITAssetInfo assetInfo = 
	  (UITAssetInfo)(itinerary.getUITAssetInfoVector().get(0));
	String unitName = 
	  PathString.basename(itinerary.getTransportedUnitName());

	String itemID = 
	  assetInfo.getItemID() == null ? 
	  "?Unknown-ID" : 
	  assetInfo.getItemID();

	String itemType = 
	  assetInfo.getTypeNomenclature() == null ? 
	  "?Unknown Type" : 
	  assetInfo.getTypeNomenclature();

	String firstPart = null, lastPart = null;

	if ( itemType.indexOf("Infantry") != -1 )
	    return unitName + "/" + type + ": " + 
	      assetInfo.getQuantity() + " PAX";

	if ( itemType.indexOf("AirForcePerson") != -1 ) {
	    Debug.out("TN:dUUID got AirForce: " + assetInfo);
	    return unitName + "/" + type + ": " + 
	      assetInfo.getQuantity() + " AirForce Personnel ("
		+ assetInfo.getUID() + ")";
	}
	int space = itemType.indexOf(' ');
	if ( space == -1 ) {
	    // Debug.out("TN:dUUID Note: could not find ' ' in: '" 
	  // + itemType +  "'");
	    firstPart = itemType;
	}
	else
	    firstPart = itemType.substring(0, space);

	int dash = itemID.lastIndexOf('-');
	if ( dash == -1 )
	    dash = itemID.indexOf('_');
	if ( dash == -1 ) {
	  //Debug.out("TN:dUUID Note: could not find '-' or '_' in: '" 
	  // + itemID + "'");
	    lastPart = itemID;
	}
	else 
	    lastPart = itemID.substring(dash + 1, itemID.length());


	return unitName + "/" + type + ": " + firstPart + '-' + lastPart;
    }
	
    public void propagateStart(long start)
    {
	// Debug.out("TN:prS " + UUID + " start: " + start + " actual " + getActualStart());
	if ( start > 0 && (getActualStart() == 0 || getActualStart() > start) ) {
	    // Debug.out("TN:prS " + getActualStart() + " > " + start + " in " + getUUID());
	    setActualStart(start);
	    provider.proxyChangeNotify(this);
	    if ( !isRoot_() )
		parent_.propagateStart(start);
	}
    }
    
    public void propagateEnd(long end)
    {
	// Debug.out("TN:prE " + UUID + " end: " + end);
	if ( end > 0 && (getActualEnd() == 0 || getActualEnd() < end) ) {
	    // Debug.out("TN:prE " + getActualEnd() + " < " + end + " in " + getUUID());
	    setActualEnd(end);
	    provider.proxyChangeNotify(this);
	    if ( !isRoot_() )
		parent_.propagateEnd(end);
	}
    }

    public boolean isStructural()
    {
	return sourceType <= LAST_STRUCTURE_CODE;
    }

    public boolean isAggregate()
    {
	return sourceType >= CARRIER_TYPE && sourceType <= CARGO_NAME;
    }

    public String getVerb()
    {
	return verb;
    }

    public void setVerb(String verb)
    {
	this.verb = verb;
    }

    public String getForWhom()
    {
	return forWhom;
    }

    public void setForWhom(String forWhom)
    {
	this.forWhom = forWhom;
    }

    public int getMode()
    {
	return mode;
    }

    public void setMode(int mode)
    {	
	this.mode = mode;
    }

    public boolean isRoot_()
    {
	return parent_ == provider.getRoot();
    }

    public int getSourceType()
    {
	return sourceType;
    }

    public void setSourceType(int sourceType)
    {
	this.sourceType = sourceType;
    }

    public String getParentUUID()
    {
	return parentUUID;
    }

    public void setParentUUID(String parentUUID)
    {
	this.parentUUID = parentUUID;
    }

    public TaskNode getParent_()
    {
	return parent_;
    }

    public void setParent_(TaskNode parent)
    {
	if ( parent == null )
	    parent = provider.getRoot();
	// Debug.out("TN:sP " + this + " to " + parent);
	this.parent_ = parent;
	provider.proxyChangeNotify(this);
    }
    
    public void addChild(TaskNode child)
    {
	getChildren_().putAndRemoveOrphans(child);
	childCount++;
	provider.proxyChangeNotify(this);
    }

    public void removeChild(TaskNode child)
    {
	getChildren_().remove(child);
	childCount--;
	provider.proxyChangeNotify(this);
    }

    public int getChildCount_()
    {
	return childCount;
    }

    public TaskNode getChild_(int index)
    {
	// Debug.out("TN:gChild_ " + index);
	if ( index < 0 || index > getChildren_().size() ) {
	    OutputHandler.out("TN:gC Error: " + getChildren_().size() +
			      " total; invalid index: " + index);
	    return null;
	}
	return (TaskNode)getChildren_().get(index);
    }

    public int indexOf(TaskNode node)
    {
      if ( !hasChildren() ) {
	return -1;
      }
      return getChildren_().indexOf(node);
    }

    public boolean hasChildren()
    {
	// Debug.out("TN:hC " + getUUID() + ": " + (getChildren_().size() > 0));
	return children != null && getChildren_().size() > 0;
    }

    public TreeMap getChildren_()
    {
	return children;
    }

    public boolean isContiguous()
    {
	if ( contiguityCheck )
	    return contiguityResult;

	contiguityCheck = true;
	contiguityResult = true;


	// Dont' check!  We no longer think that it's relevant
	/*
	for ( int i = 0; i < getChildCount_() - 1; i++ )
	    if ( !getChild_(i).getToName().equals(getChild_(i + 1).getFromName()) )
		contiguityResult = false;

	*/
	
	return contiguityResult;
    }

    public Object getSourceObject()
    {
	return sourceObject;
    }

    public void setSourceObject()
    {
	this.sourceObject = sourceObject;
    }

    public String getDisplayName()
    {
	if ( sourceType == ITINERARY_LEG )
	    return displayName + parent_.indexOf(this);
	return displayName;
    }

    public void setDisplayName(String displayName)
    {
	this.displayName = displayName;
    }

    public String getLongName()
    {
	return longName;
    }

    public void setLongName(String longName)
    {
	this.longName = longName;
    }

    public boolean isMulti()
    {
	return multi;
    }

    public void setMulti(boolean multi)
    {
	this.multi = multi;
    }

    public boolean isTransport()
    {
	return transport;
    }

    public void setTransport(boolean transport)
    {
	this.transport = transport;
    }

    public String getUnitName()
    {
	if ( unitName != null )
	    return unitName;
	else
	    return "?Unknown Unit Name";
    }

    public void setUnitName(String unitName)
    {
	this.unitName = unitName;
    }

    public String getCarrierName()
    {
	if ( carrierName != null )
	    return carrierName;
	else
	    return "?Unknown Carrier Name";
    }

    public void setCarrierName(String carrierName)
    {
	this.carrierName = carrierName;
    }

    public String getParentCarrierName()
    {
	return parentCarrierName;
    }

    public void setParentCarrierName(String parentCarrierName)
    {
	this.parentCarrierName = parentCarrierName;
    }

    public String getCarrierType()
    {
	if ( carrierType != null )
	    return carrierType;
	else
	    return "?Unknown Carrier Type";
    }

    public void setCarrierType(String carrierType)
    {
	this.carrierType = carrierType;
    }

    public String getParentCarrierType()
    {
	return parentCarrierType;
    }

    public void setParentCarrierType(String parentCarrierType)
    {
	this.parentCarrierType = parentCarrierType;
    }

    public String getDirectObjectName()
    {
	if ( directObjectName != null )
	    return directObjectName;
	else
	    return "?Unknown Cargo Name?";
    }

    public void setDirectObjectName(String directObjectName)
    {
	this.directObjectName = directObjectName;
    }

    public String getDirectObjectType()
    {
	if ( directObjectType != null )
	    return directObjectType;
	else
	    return "?Unknown Cargo Type";
    }

    public void setDirectObjectType(String directObjectType)
    {
	this.directObjectType = directObjectType;
    }

    public String getFromName()
    {
	if ( fromLocation != null )
	    return fromLocation.getName();
	else
	    return "N/A";
    }

    public void setFromName(String fromName)
    {
	if ( fromLocation == null )
	    fromLocation = new Location(null);
	fromLocation.setName(fromName);
    }

    public String getFromCode()
    {
	// should have a way of switching between codes and names
	if ( fromCode != null )
	    return fromCode;
	else
	    return "N/A";
    }

    public void setFromCode(String fromCode)
    {
	this.fromCode = fromCode;
    }

    public String getToName()
    {
	if ( toLocation != null )
	    return toLocation.getName();
	else
	    return "N/A";
    }

    public void setToName(String toName)
    {
	if ( toLocation == null )
	    toLocation = new Location(null);
	toLocation.setName(toName);
    }

    public String getToCode()
    {
	if ( toCode != null )
	    return toCode;
	else
	    return "N/A";
    }

    public void setToCode(String toCode)
    {
	this.toCode = toCode;
    }



  public void setDirectTag(int tag) {
    directTag = tag;
  }

  public int getDirectTag() {
    return directTag;
  }

  public void setTripTag(int tag) {
    tripTag = tag;
  }

  public int getTripTag() {
    return tripTag;
  }
  


  
  /**
     * Get the value of typeCargoCarrier.
     * @return Value of typeCargoCarrier.
     */
  public int getTypeCargoCarrier() {return typeCargoCarrier;}
  
  /**
     * Set the value of typeCargoCarrier.
     * @param v  Value to assign to typeCargoCarrier.
     */
  public void setTypeCargoCarrier(int  v) {this.typeCargoCarrier = v;}
  

    // schedule element interface 
    public long getMinStart()
    {
	return minStart;
    }
    
    public void setMinStart(long minStart)
    {
	this.minStart = minStart;
	provider.setMinTaskStart(minStart);
    }
	    
    public long getMaxStart()
    {
	return maxStart;
    }

    public void setMaxStart(long maxStart)
    {
	this.maxStart = maxStart;
	provider.setMinTaskStart(maxStart);
    }

    public long getBestStart()
    {
	return bestStart;
    }

    public void setBestStart(long bestStart)
    {
	this.bestStart = bestStart;
	provider.setMinTaskStart(bestStart);
    }

    public long getActualStart()
    {
	return actualStart;
    }

    public void setActualStart(long actualStart)
    {
	// Debug.out("TN:sAS provider: " + provider + " this: " + this + " aS: " + actualStart);
	provider.remap(this, actualStart);
	provider.setMinTaskStart(actualStart);
    }

    public void setActualStartValue_(long actualStart)
    {
	this.actualStart = actualStart;
    }

    public long getEstimatedStart()
    {
	return estimatedStart;
    }
    
    public void setEstimatedStart(long estimatedStart)
    {
	this.estimatedStart = estimatedStart;
	provider.setMinTaskStart(estimatedStart);
    }

    public long getMinEnd()
    {
	return minEnd;
    }
    
    public void setMinEnd(long minEnd)
    {
	this.minEnd = minEnd;
	provider.setMaxTaskEnd(minEnd);
    }

    public long getMaxEnd()
    {
	return maxEnd;
    }

    public void setMaxEnd(long maxEnd)
    {
	this.maxEnd = maxEnd;
	provider.setMaxTaskEnd(maxEnd);
    }

    public long getBestEnd()
    {
	return bestEnd;
    }

    public void setBestEnd(long bestEnd)
    {
	this.bestEnd = bestEnd;
	provider.setMaxTaskEnd(bestEnd);
    }

    public long getActualEnd()
    {
	return actualEnd;
    }

    public void setActualEnd(long actualEnd)
    {
	this.actualEnd = actualEnd;
	provider.setMaxTaskEnd(actualEnd);
    }

    public long getEstimatedEnd()
    {
	return estimatedEnd;
    }
    
    public void setEstimatedEnd(long estimatedEnd)
    {
	this.estimatedEnd = estimatedEnd;
	provider.setMaxTaskEnd(estimatedEnd);
    }

    public static String shortDate(long timeValue)
    {
	return shortFormat.format(new Date(timeValue));
    }  

    public static String longDate(long timeValue)
    {
	return longFormat.format(new Date(timeValue));
    }
    
    public String toString()
    {
	return UUID;
    }
}
