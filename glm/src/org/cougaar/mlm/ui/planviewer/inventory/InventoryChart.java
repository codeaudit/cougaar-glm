/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
 
package org.cougaar.mlm.ui.planviewer.inventory;

import org.cougaar.glm.ldm.plan.PlanScheduleType;
import org.cougaar.util.ThemeFactory;

import com.klg.jclass.chart.*;
//451K uncomment MWD 
import com.klg.jclass.util.legend.*;
import com.klg.jclass.chart.data.JCDefaultDataSource;
import com.klg.jclass.util.swing.JCExitFrame;
import com.klg.jclass.chart.JCFillStyle;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.ButtonUI;

import org.cougaar.mlm.ui.data.UIInventoryImpl;
import org.cougaar.mlm.ui.data.UISimpleInventory;
import org.cougaar.mlm.ui.data.UISimpleNamedSchedule;
import org.cougaar.mlm.ui.data.UISimpleNamedScheduleNames;
import org.cougaar.mlm.ui.data.UIQuantityScheduleElement;

public class InventoryChart extends JPanel
    implements JCPickListener, 
	       ActionListener, 
	       ItemListener,
	       UISimpleNamedScheduleNames
{
    JCChart chart=null;
    InventoryLegend legend=null;
    JPanel chartCheckBoxPanel=null;
    GridBagConstraints legendLocale=null;

    final static String checkBoxUIClassID=(new JCheckBox()).getUIClassID();
    JToggleButton legendButton=null;
    boolean       doShowInactive=true;

    JLabel pointLabel; // initialized when creating chart, set by pick method
    Hashtable dataViews = new Hashtable(); // temporary, until data view-data model correspondence is working
    InventoryColorTable colorTable;
    static final String INVENTORY_LEGEND = "Inventory";
    static final String INVENTORY_DETAILS_LEGEND = "Inventory (Detailed)";
    static final String REQUESTED_INVENTORY_LEGEND = "Requested";
    static final String ACTUAL_INVENTORY_LEGEND = "Actual";
    static final String REQUESTED_INVENTORY_DETAILS_LEGEND = REQUESTED_INVENTORY_LEGEND;
    static final String ACTUAL_INVENTORY_DETAILS_LEGEND = ACTUAL_INVENTORY_LEGEND;
    static final String REQUESTED_INACTIVE_INVENTORY_LEGEND = "Requested Inactive";
    static final String ACTUAL_INACTIVE_INVENTORY_LEGEND = "Actual Inactive";
    static final String REQUESTED_SUPPLY_LEGEND = REQUESTED_INVENTORY_LEGEND + " ";
    static final String ACTUAL_SUPPLY_LEGEND = ACTUAL_INVENTORY_LEGEND + " ";
    static final String SHORTFALL_LEGEND = "Shortfall";
    static final String TOTAL_CAPACITY_LEGEND = "Total Capacity";
    static final String ALLOCATED_LEGEND = "Allocated";
    static final String UNCONFIRMED_LEGEND = "UnConfirmed";
    
    static final int CDAY_MIN=-5;

    public static final String CDAY_MODE = "Cdays";
    public static final String SHOW_INACTIVE = "Show Inactive";
    public static final String DISPLAY_DETAILS = "Inv Details";


    //PER_25,50,75,HORIZ_STRIPE,VERT_STRIPE,DIAG_HATCHED,CROSS_HATCHED,etc
    static final int INACTIVE_SERIES_PATTERN=JCFillStyle.STRIPE_45;

    Vector removeableViews = new Vector();
    int viewIndex = 0;
    int gridx = 0;
    int gridy = 0;
    Insets blankInsets = new Insets(0, 0, 0, 0);

    UISimpleInventory myInventory;
    String myTitle;
    String myYAxisTitle;

    JCChartLabel nowLabel=null;
    Date alpNow=new Date();

    JCheckBox cdaysModeCheck; 
    JCheckBox showInactiveCheck; 

    long baseCDayTime;
    boolean displayCDay=false;
    boolean canDisplayDetails=false;
    boolean displayDetails=false;

    /** For testing, creates a chart with dummy data.
	Assume random quantities are requisitioned from us.
	If the current day's requisition plus any previous shortfall
	will cause us to fall below our minimum stockage level, 
	put in a restock order for what we need.
	Assume a random quantity (less than the restock order) is received.
	Ship the minimum of, the current day's requisition + any previous shortfall
	and,
	the quantity on hand yesterday + the quantity received today - the minimum
	stockage level.
	Compute what we have on hand today as what we had on hand yesterday +
	what was received today - what was shipped today.
	Update the shortfalls; received shortfall is the total of what
	we asked for - what we received; 
	shipped shortfall is the total of what we were asked for - the total of
	what we shipped.
    */

    public InventoryChart() {
	this("Mock 3-FSB", getMockInventory());
    }


    public static UISimpleInventory getMockInventory() {

	UISimpleInventory inventory = new UISimpleInventory();

	Date now = new Date();
	long nowTime = now.getTime();
	final long aDayTime = 24*60*60*1000;
	final int numDays = 32;
	final int switchDay = 20;
	
	final double DUE_OUT_QTY=150;
	final double DUE_IN_QTY=750;
	final double PROJ_DUE_OUT_QTY=75;    
	final double PROJ_DUE_IN_QTY=PROJ_DUE_OUT_QTY; 

	final double PROJ_SHORTFALL_QTY=60;
	final double DUE_IN_SHORTFALL_QTY=500;
	final double DUE_OUT_SHORTFALL_QTY=50;

	double dueOutQty=DUE_OUT_QTY;
	double dueInQty=DUE_IN_QTY;
	double projDueOutQty=PROJ_DUE_OUT_QTY;    
	double projDueInQty=PROJ_DUE_IN_QTY;;    

	double projShortfallQty=PROJ_SHORTFALL_QTY;
	double dueInShortfallQty=DUE_IN_SHORTFALL_QTY;
	double dueOutShortfallQty=DUE_OUT_SHORTFALL_QTY;
	
	double currInventory = 800.0;
	long currNow = nowTime+aDayTime;
	long lastNow = nowTime;
	long c0Time = nowTime + (5*aDayTime);

	Vector onHand = new Vector(numDays);
	Vector dueIns=new Vector(numDays);
	Vector dueOuts=new Vector(numDays);
	Vector inactDueOuts=new Vector(numDays);
	Vector inactDueIns=new Vector(numDays);
	Vector projDueOuts= new Vector(numDays);
	Vector projDueIns= new Vector(numDays);
	Vector projInactDueIns= new Vector(numDays);
	Vector projInactDueOuts= new Vector(numDays);
	
	Vector reqDueIns=new Vector(numDays);
	Vector reqInactDueIns=new Vector(numDays);
	Vector reqDueOuts=new Vector(numDays);
	Vector reqInactDueOuts=new Vector(numDays);
	Vector reqProjDueOuts=new Vector(numDays);
	Vector reqProjDueIns=new Vector(numDays);
	Vector reqProjInactDueOuts=new Vector(numDays);
	Vector reqProjInactDueIns= new Vector(numDays);


	Vector unconfirmedDueIns=dueOuts;

	inventory.setAssetName("JP8 Fuel");
	inventory.setScheduleType(PlanScheduleType.TOTAL_INVENTORY);
	inventory.setProvider(true);
	inventory.setBaseCDay(c0Time);

	int dueInCtr=0;
	
	for(int i=0; i <= numDays ; i++) {

	    boolean shortfallDay=((currNow >= (c0Time + (12 * aDayTime))) && 
				  (currNow <= (c0Time + (22 * aDayTime))));

	    projDueOutQty = PROJ_DUE_OUT_QTY;

	    if(i<=switchDay) {
		reqProjInactDueOuts.add(new UIQuantityScheduleElement(lastNow,
								 currNow,
								 projDueOutQty));
	    }
	    else {
		reqProjDueOuts.add(new UIQuantityScheduleElement(lastNow,
								 currNow,
								 projDueOutQty));
	    }
	    if(shortfallDay) {
		projDueOutQty = PROJ_SHORTFALL_QTY;
	    }

	    if(i<=switchDay) {
		projInactDueOuts.add(new UIQuantityScheduleElement(lastNow,
							      currNow,
							      projDueOutQty));

	    }
	    else {
		projDueOuts.add(new UIQuantityScheduleElement(lastNow,
							      currNow,
							      projDueOutQty));
		currInventory=currInventory-projDueOutQty;
	    }

	    //currInventory=currInventory-projDueOutQty;
	    
	    if((currNow >= c0Time) && 
	       ((i%2)==0)){
		dueOutQty = DUE_OUT_QTY;

		if(i<=switchDay) {
		    reqDueOuts.add(new UIQuantityScheduleElement(lastNow,
								 currNow,
								 dueOutQty));
		}
		else {
		    reqInactDueOuts.add(new UIQuantityScheduleElement(lastNow,
								     currNow,
								     dueOutQty));
		}
				       
		if(shortfallDay) {
		    dueOutQty = DUE_OUT_SHORTFALL_QTY;
		}
		if(i<=switchDay) {
		    dueOuts.add(new UIQuantityScheduleElement(lastNow,
							      currNow,
							      dueOutQty));
		    currInventory=currInventory-dueOutQty;
		}
		else {
		    inactDueOuts.add(new UIQuantityScheduleElement(lastNow,
								   currNow,
								   dueOutQty));
		}


	    } else {
		if(i<=switchDay) {
		    reqDueOuts.add(new UIQuantityScheduleElement(lastNow,
								 currNow,
								 0));
		    dueOuts.add(new UIQuantityScheduleElement(lastNow,
							      currNow,
							      0));
		}
		else {
		    reqInactDueOuts.add(new UIQuantityScheduleElement(lastNow,
								 currNow,
								 0));
		    inactDueOuts.add(new UIQuantityScheduleElement(lastNow,
							      currNow,
							      0));
		}

	    }

	    if(i<=switchDay) {
		reqProjInactDueIns.add(new UIQuantityScheduleElement(lastNow,
								     currNow,
								     projDueInQty));

		projInactDueIns.add(new UIQuantityScheduleElement(lastNow,
								  currNow,
								  projDueInQty));

	    }
	    else {
		reqProjDueIns.add(new UIQuantityScheduleElement(lastNow,
								 currNow,
								 projDueInQty));
		projDueIns.add(new UIQuantityScheduleElement(lastNow,
							     currNow,
							     projDueInQty));
		currInventory+=projDueInQty;
	    }
	    
	    if((currNow >= c0Time) && 
	       ((i%10)==0)){
		dueInQty = DUE_IN_QTY;

		if(i<=switchDay) {
		    reqDueIns.add(new UIQuantityScheduleElement(lastNow,
								currNow,
								dueInQty));	    
		}
		else {
		    reqInactDueIns.add(new UIQuantityScheduleElement(lastNow,
								currNow,
								dueInQty));	

	
		}


		if(i<=switchDay) {
		    if(shortfallDay) {
			dueInQty = DUE_IN_SHORTFALL_QTY;
		    }
		    dueIns.add(new UIQuantityScheduleElement(lastNow,
							     currNow,

							     dueInQty));
		    currInventory+=dueInQty;
		}
		else {
		    inactDueIns.add(new UIQuantityScheduleElement(lastNow,
								currNow,
								dueInQty));		    

		}

	    } else {
		if(i<=switchDay) {
		    reqInactDueIns.add(new UIQuantityScheduleElement(lastNow,
								     currNow,
								     0));
		    inactDueIns.add(new UIQuantityScheduleElement(lastNow,
								  currNow,
								  0));
		} else {
		    reqDueIns.add(new UIQuantityScheduleElement(lastNow,
								currNow,
								0));
		    dueIns.add(new UIQuantityScheduleElement(lastNow,
							     currNow,
							     0));
		}
	    }
	    
	    lastNow = currNow;
	    currNow+=aDayTime;
	    
	    onHand.add(new UIQuantityScheduleElement(lastNow,
						     currNow,
						     currInventory));
	}
	

	inventory.addNamedSchedule(ON_HAND, onHand);
	inventory.addNamedSchedule(DUE_IN, dueIns);
	inventory.addNamedSchedule(REQUESTED_DUE_IN, reqDueIns);

	inventory.addNamedSchedule(DUE_IN + INACTIVE, inactDueIns);
	inventory.addNamedSchedule(REQUESTED_DUE_IN + INACTIVE, reqInactDueIns);
	                           
	inventory.addNamedSchedule(DUE_OUT, dueOuts);
	inventory.addNamedSchedule(REQUESTED_DUE_OUT, reqDueOuts);

	inventory.addNamedSchedule(DUE_OUT + INACTIVE, inactDueOuts);
	inventory.addNamedSchedule(REQUESTED_DUE_OUT + INACTIVE, reqInactDueOuts);
                                   
	inventory.addNamedSchedule(PROJECTED_DUE_OUT, projDueOuts);
	inventory.addNamedSchedule(PROJECTED_REQUESTED_DUE_OUT, reqProjDueOuts);

	inventory.addNamedSchedule(PROJECTED_DUE_OUT+INACTIVE, projInactDueOuts);
	inventory.addNamedSchedule(PROJECTED_REQUESTED_DUE_OUT+INACTIVE, reqProjInactDueOuts);

	inventory.addNamedSchedule(PROJECTED_DUE_IN, projDueIns);
	inventory.addNamedSchedule(PROJECTED_REQUESTED_DUE_IN, reqProjDueIns);

	inventory.addNamedSchedule(PROJECTED_DUE_IN+INACTIVE, projInactDueIns);
	inventory.addNamedSchedule(PROJECTED_REQUESTED_DUE_IN+INACTIVE, reqProjInactDueIns);



	// MWD Uncomment to get Unconfirmed into the mix.
//	inventory.addNamedSchedule(UNCONFIRMED_DUE_IN,unconfirmedDueIns);

	return inventory;

    }

    /** Create charts:
	For capacity: one AREA chart view with two series
	For inventory: one BAR chart view with On Hand series and
	one BAR chart view with Due In and Due Out series
	one BAR chart view with Requested Due In and Out series
	Note that title is just the cluster name; the real title
	is built from this to be:
	Inventory (or Capacity) of asset name at cluster name
    */

    // This is the constructor used for real alp generated data
    public InventoryChart(String title, UISimpleInventory inventory) {
	super();

	myInventory = inventory;
	myTitle = title;
	colorTable = new InventoryColorTable();
	String scheduleType = inventory.getScheduleType();
	chart = new JCChart();
	baseCDayTime = inventory.getBaseCDayTime();
	if (scheduleType.equals(PlanScheduleType.TOTAL_CAPACITY)) {
	    initializeTotalCapacityChart(title, inventory);
	} else if (scheduleType.equals(PlanScheduleType.ACTUAL_CAPACITY)) {
	    initializeActualCapacityChart(title, inventory);
	} else if ((scheduleType.equals(PlanScheduleType.TOTAL_INVENTORY)) ||
		   (scheduleType.equals(UIInventoryImpl.NO_INVENTORY_SCHEDULE_JUST_CONSUME))) {
	    initializeTotalInventoryChart(title, inventory);
	} else
	    System.out.println("Unconfirmed schedule type: " + scheduleType);

	// unit type is gallons, stons, etc.
	// customizes x and y axis info
	//     if (inventory.getUnitType().equals("STons"))
	//       customizeAxes(chart, "Rounds");
	//     else
	customizeAxes(chart, inventory.getUnitType());

	// allow user to zoom in on chart
	chart.setTrigger(0, new EventTrigger(0, EventTrigger.ZOOM));

	// allow interactive customization using left shift click
	chart.setAllowUserChanges(true);
	chart.setTrigger(1, new EventTrigger(Event.SHIFT_MASK,
					     EventTrigger.CUSTOMIZE));

	// allow user to display labels using right mouse click
	chart.addPickListener(this);
	chart.setTrigger(1, new EventTrigger(Event.META_MASK, EventTrigger.PICK));

	// set header and legend to black
	// set beveled borders around plot area and chart
	((JLabel)chart.getHeader()).setForeground(Color.black);
	chart.setBorder(BorderFactory.createLoweredBevelBorder());
	chart.getChartArea().setBorder(BorderFactory.createLoweredBevelBorder());

	// add chart to panel
	setLayout(new GridBagLayout());
	chart.getHeader().setVisible(true);
	// set legend invisible, because we create our own
	chart.getLegend().setVisible(false);
	add(chart, new GridBagConstraints(gridx, gridy++, 1, 1, 1.0, 1.0,
					  GridBagConstraints.CENTER, 
					  GridBagConstraints.BOTH, 
					  blankInsets, 0, 0));


	// provide for point labels displayed beneath chart
	pointLabel = new JLabel("Right click to get quantity at a point."); // to leave space in layout
	pointLabel.setBackground(Color.magenta);
	add(pointLabel, new GridBagConstraints(gridx, gridy, 1, 1, 1.0, 0.0,
					       GridBagConstraints.CENTER, 
					       GridBagConstraints.NONE, 
					       blankInsets, 0, 0));

	//checkAndLegendButtonPanel = createChecksAndLegendButtonPanel();
	JPanel cbPanel = createCheckBoxPanel();
	

	add(cbPanel, new GridBagConstraints(gridx, gridy, 1, 1, 0.4, 0.0,
					       GridBagConstraints.EAST, 
					       GridBagConstraints.NONE, 
					       blankInsets, 0, 0));

	legendButton = createLegendButton();

	add(legendButton, 
	    new GridBagConstraints(gridx, gridy++, 1, 1, 0.1, 0.0,
				   GridBagConstraints.WEST, 
				   GridBagConstraints.VERTICAL, 
				   blankInsets, 0, 0));

	legend = new InventoryLegend(this,chart);
	legendLocale =
	    new GridBagConstraints(gridx, gridy++, 1, 1, 1.0, 0.0,
				   GridBagConstraints.WEST, 
				   GridBagConstraints.BOTH, 
				   blankInsets, 0, 0);
	add(legend, legendLocale);

	initializeNowLabel();
	setAlpNow(new Date(inventory.getAlpNowTime()));
    }

    private void initializeNowLabel() {
	nowLabel = new JCChartLabel("|");
	//	nowLabel = new JCChartLabel("Now",false);
	//	nowLabel.setDataView( view );
	nowLabel.setCoord(new Point(50,300));
	nowLabel.setAttachMethod(JCChartLabel.ATTACH_COORD);
	nowLabel.setAnchor( JCChartLabel.SOUTH );

	//MAGIC number offset - apparently to do with offset from
	//XAxis top, and coordinate system.
	nowLabel.setOffset( new Point(0,32) );

	JLabel label = (JLabel) nowLabel.getComponent();
	label.setForeground(Color.red);
	label.setVisible(false);
	label.setHorizontalAlignment(JLabel.RIGHT);
	//label.setBorder(BorderFactory.createLineBorder(Color.black));
	
	chart.getChartLabelManager().addChartLabel(nowLabel);

	chart.addComponentListener( new ComponentAdapter() {
	    public void componentResized(ComponentEvent e) {
		resetAlpNow();
	    }});

	chart.addChartListener( new JCChartAdapter() {
	    public void changeChart(JCChartEvent chart) {
		resetAlpNow();
	    }});
    }

    public void reinitializeAndUpdate(String title, 
				      UISimpleInventory inventory) {

	myInventory = inventory;
	myTitle = title;
	equalizeInventorySchedule(myInventory);

	reinitializeAndUpdate();
    }

    protected void reinitializeAndUpdate() {


	String scheduleType = myInventory.getScheduleType();
	Object[] viewsToRemove = removeableViews.toArray();

	//Batch up chart updates while rebuilding otherwise there
	//will be trouble when removing views.
	chart.setBatched(true);

	removeAllViews();

	baseCDayTime = myInventory.getBaseCDayTime();

	if (scheduleType.equals(PlanScheduleType.TOTAL_CAPACITY)) {
	    initializeTotalCapacityChart(myTitle, myInventory);
	} else if (scheduleType.equals(PlanScheduleType.ACTUAL_CAPACITY)) {
	    initializeActualCapacityChart(myTitle, myInventory);
	} else if ((scheduleType.equals(PlanScheduleType.TOTAL_INVENTORY)) ||
		   (scheduleType.equals(UIInventoryImpl.NO_INVENTORY_SCHEDULE_JUST_CONSUME))) {
		if(displayDetails) { 
		    initializeDetailedInventoryChart(myTitle, myInventory);
		}
		else {
		    initializeTotalInventoryChart(myTitle, myInventory);
		    cdaysModeCheck.setEnabled(true); 
		    cdaysModeCheck.setVisible(true); 
		    showInactiveCheck.setEnabled(true);
		    showInactiveCheck.setVisible(true);
		}

	} else
	    System.out.println("Unconfirmed schedule type: " + scheduleType);

	for(int i=0; i<viewsToRemove.length; i++) {
	    ChartDataView cdView = (ChartDataView) viewsToRemove[i];
	    setChartDataViewVisible(cdView.getName(),cdView.isVisible());
	}


	/**
	 **  MWD Will this work?

	java.util.List viewList = chart.getDataView();
	for (int i = 0; i < viewList.size(); i++) {
	    ChartDataView chartDataView = (ChartDataView)viewList.get(i);
	    ChartDataModel dm = chartDataView.getDataSource();
	    if(dm != null) {
		if (dm instanceof InventoryBaseChartDataModel) {
		    InventoryBaseChartDataModel baseDM = 
			(InventoryBaseChartDataModel) dm;
		    //System.out.println("Setting view " + baseDM.getDataSourceName() + " to display c days:" + useCDays);
		    baseDM.resetInventory(myInventory);
		}
		else
		    System.out.println("View with data model of type: " + dm.getClass().getName());
	    }	
	}

	***
	**/
	
	remove(legend);
	legend = new InventoryLegend(this,chart);
	add(legend, legendLocale);
	
	setAlpNow(new Date(myInventory.getAlpNowTime()));

	chart.update();
	legend.revalidate();

	chart.setBatched(false);
	
	//      chart.repaint();

	//this.repaint();

    }

    private void initializeTotalCapacityChart(String title, UISimpleInventory inventory) {
	title = "Capacity of " + inventory.getAssetName() + " at " + title;
	setChartTitle(title);
	//MWD Remove
	//Vector scheduleTypes = new Vector(2);
	//scheduleTypes.addElement(TOTAL_LABOR);
	//scheduleTypes.addElement(ALLOCATED);
	addView(chart, JCChart.AREA,
		createInventoryDataModel(inventory, TOTAL_CAPACITY_LEGEND));
    }

    private void initializeActualCapacityChart(String title, UISimpleInventory inventory) {
	title = "Capacity of " + inventory.getAssetName() + " at " + title;
	setChartTitle(title);
	//MWD Remove
	//Vector scheduleTypes = new Vector(1);
	//scheduleTypes.addElement(ON_HAND);
	addView(chart, JCChart.AREA,
		createInventoryDataModel(inventory, INVENTORY_LEGEND));
	//MWD Remove
	//scheduleTypes = new Vector(1);
	//scheduleTypes.addElement(ALLOCATED);
	addView(chart, JCChart.BAR, 
		createInventoryDataModel(inventory, ALLOCATED_LEGEND));
    }

    // Fix up inventory by making sure if there is a requested schedule,
    // and no actual series then add one.
    private void equalizeInventorySchedule(UISimpleInventory inventory) {
	String[][] reqActTable = {
            {REQUESTED_DUE_IN,
             DUE_IN,
	     REQUESTED_DUE_IN + INACTIVE,
             DUE_IN + INACTIVE},
            {PROJECTED_REQUESTED_DUE_IN,
             PROJECTED_DUE_IN,
	     PROJECTED_REQUESTED_DUE_IN + INACTIVE,
             PROJECTED_DUE_IN + INACTIVE},
            {REQUESTED_DUE_OUT,
             DUE_OUT, 
	     REQUESTED_DUE_OUT + INACTIVE,
             DUE_OUT + INACTIVE},
            {PROJECTED_REQUESTED_DUE_OUT,
             PROJECTED_DUE_OUT,
	     PROJECTED_REQUESTED_DUE_OUT + INACTIVE, 
	     PROJECTED_DUE_OUT + INACTIVE}
        };


	for (int i = 0; i < reqActTable.length; i++) {
	    UISimpleNamedSchedule curr=null, ex=null;
	    int j=0;


	    //If model is not null after this, then there
	    //is a schdule entry for this "row" in the table.
	    //ex is the exemplar for the rest of the table
	    for(j=0;j < reqActTable[i].length; j++) {
		curr = inventory.getNamedSchedule(reqActTable[i][j]);
		if((curr != null) && (ex==null)) {
		    ex = curr;
		}
	    }
	    
	    if(ex!=null) {
		
		for(j=0;j < reqActTable[i].length; j++) {
		    curr = inventory.getNamedSchedule(reqActTable[i][j]);	
		    if(curr == null) {
			Vector modelSch = ex.getSchedule();
			Vector newSch = new Vector(modelSch.size());
			for(int k=0; k < modelSch.size() ; k++) {
			    UIQuantityScheduleElement element = 
				(UIQuantityScheduleElement) modelSch.elementAt(k);
			    UIQuantityScheduleElement newElement = 
				(UIQuantityScheduleElement) element.clone();
			    newElement.setQuantity(0);
			    newSch.add(newElement);
			}
			inventory.addNamedSchedule(reqActTable[i][j],newSch);
		    }
		}
	    }
	}    
    }

    private void initializeDetailedInventoryChart(String title, UISimpleInventory inventory) {
	    if (inventory.getNamedSchedule(ON_HAND_DETAILED) == null) {
		JOptionPane.showMessageDialog(null, "No data received",
					      "No data received",
					      JOptionPane.ERROR_MESSAGE);
		return;
	    }

	    System.out.println("InventoryChart::InitializeDetailedInventoryChart");

	    // Title and chart
	    title = "Inventory of " + inventory.getAssetName() + " at " + title;
	    
	    //equalizeInventorySchedule(inventory);

     
	    // On Hand
	    // MWD Remove
	    //scheduleTypes = new Vector(1);
	    //scheduleTypes.addElement(ON_HAND);
	    InventoryChartDataModel chartDataModel = createDetailedInventoryDataModel(inventory, INVENTORY_DETAILS_LEGEND);

	    ChartDataView icv = addView(chart, JCChart.AREA, chartDataModel);
	    //((JCBarChartFormat)icv.getChartFormat()).setClusterWidth(100);
	    icv.setOutlineColor(colorTable.get(ON_HAND));

	    setChartTitle(title);

	    //Requested
	    InventoryChartDataModel requested =
		createDetailedInventoryDataModel(inventory, REQUESTED_INVENTORY_DETAILS_LEGEND);
	    icv = addView(chart, JCChart.BAR, requested);
	    ((JCBarChartFormat)icv.getChartFormat()).setClusterWidth(20);
	    
	    //Actual
	    InventoryChartDataModel actual =
		createDetailedInventoryDataModel(inventory, ACTUAL_INVENTORY_DETAILS_LEGEND);
	    icv = addView(chart, JCChart.BAR, actual);
	    ((JCBarChartFormat)icv.getChartFormat()).setClusterWidth(20);

	    cdaysModeCheck.setVisible(false); 
	    cdaysModeCheck.setEnabled(false); 
	    showInactiveCheck.setEnabled(false);
	    showInactiveCheck.setVisible(false);

    }


    private void initializeTotalInventoryChart(String title, UISimpleInventory inventory) {
	Vector scheduleTypes;

	if(!(inventory.getScheduleType().equals(UIInventoryImpl.NO_INVENTORY_SCHEDULE_JUST_CONSUME))){
	    // if no on hand schedule, then don't plot an inventory graph
	    if (inventory.getNamedSchedule(ON_HAND) == null) {
		JOptionPane.showMessageDialog(null, "No data received",
					      "No data received",
					      JOptionPane.ERROR_MESSAGE);
		return;
	    }

	    if (inventory.getNamedSchedule(ON_HAND_DETAILED) != null) {
		canDisplayDetails = true;
	    }

	    // Title and chart
	    title = "Inventory of " + inventory.getAssetName() + " at " + title;
	    
	    equalizeInventorySchedule(inventory);

     
	    // On Hand
	    // MWD Remove
	    //scheduleTypes = new Vector(1);
	    //scheduleTypes.addElement(ON_HAND);
	    InventoryChartDataModel chartDataModel = createInventoryDataModel(inventory, 
									      INVENTORY_LEGEND);
	    ChartDataView icv = addView(chart, JCChart.BAR, chartDataModel);
	    ((JCBarChartFormat)icv.getChartFormat()).setClusterWidth(100);
	    icv.setOutlineColor(colorTable.get(ON_HAND));
	}
	else {
	    if (inventory.getSchedules().size() == 0) {
		JOptionPane.showMessageDialog(null, "No data received",
					      "No data received",
					      JOptionPane.ERROR_MESSAGE);
		return;
	    }
	    title = "Consumption of " + inventory.getAssetName() + " at " + title;
	    equalizeInventorySchedule(inventory);
	}

	setChartTitle(title);
	    
	    // Requested
	InventoryChartDataModel requested = 
	    createInventoryDataModel(inventory, 
				     REQUESTED_INVENTORY_LEGEND);
	addView(chart, JCChart.BAR, requested);

	// Actual
	InventoryChartDataModel actual = 
	    createInventoryDataModel(inventory, 
				     ACTUAL_INVENTORY_LEGEND);
	addView(chart, JCChart.BAR, actual);

	
	// Requested Inactive
	InventoryChartDataModel requestedInactive = 
	    createInventoryDataModel(inventory, 
				     REQUESTED_INACTIVE_INVENTORY_LEGEND);
	requested.setAssociatedInactiveModel(requestedInactive);
	addView(chart, JCChart.BAR, requestedInactive).setVisible(doShowInactive);

	// Actual Inactive
	InventoryChartDataModel actualInactive = 
	    createInventoryDataModel(inventory, 
				     ACTUAL_INACTIVE_INVENTORY_LEGEND);
	actual.setAssociatedInactiveModel(actualInactive);
	addView(chart, JCChart.BAR, actualInactive).setVisible(doShowInactive );

	computeAndInitializeShortfall(inventory,actual,requested);
    }

    private void computeAndInitializeShortfall(UISimpleInventory inventory,
					       InventoryChartDataModel actual,
					       InventoryChartDataModel requested) {

	// unconfirmed
	// do not add to chart separatly - only used to calc shortfall
	InventoryChartDataModel unconfirmed = createInventoryDataModel(inventory,								  UNCONFIRMED_LEGEND);

	// Shortfalls
	ChartDataModel dm = 
	    new InventoryShortfallChartDataModel(actual, requested, unconfirmed);
	ChartDataView chartDataView = new ChartDataView();
	chartDataView.setDataSource(dm);
	chart.setDataView(viewIndex, chartDataView);
	viewIndex++;
	dataViews.put(chartDataView, dm);
	int tmp = chartDataView.getNumSeries();
	if (tmp == 0) {
	    // print no shortfalls
	     System.out.println("InventoryChart no shortfalls");
	}
	for (int ii = 0; ii < tmp; ii++) {
	 ChartDataViewSeries series = chartDataView.getSeries(ii);
	 String lbl = series.getLabel();
	 // MWD any orig colors settings are the first commented out ones.
	 // Changed to be more in line with the colors of requested and actuals.
	 Color seriesColor = colorTable.get(lbl);
	 if (lbl.equals(InventoryShortfallChartDataModel.DUE_OUT_SHORTFALL_LABEL)) {
	     setSeriesColor(series, seriesColor);

	     series.getStyle().setSymbolSize(0);
	     series.getStyle().setLineWidth(2);
	     //    JCLineStyle line_style = new JCLineStyle(2,  Color.magenta, JCLineStyle.LONG_DASH);
	     //    series.getStyle().setLineStyle(line_style);
	     //    series.getStyle().setSymbolShape(JCSymbolStyle.CROSS);
	 } else if (lbl.equals(InventoryShortfallChartDataModel.UNCONFIRMED_DUE_IN_SHORTFALL_LABEL)) {
	     setSeriesColor(series, seriesColor);
// 	     JCLineStyle line_style = new JCLineStyle(4,  new Color(255, 95, 0), JCLineStyle.SHORT_DASH);
// 	     series.getStyle().setLineStyle(line_style);
	     series.getStyle().setLineWidth(4);
	     series.getStyle().setSymbolSize(0);
	     // series.getStyle().setSymbolShape(JCSymbolStyle.DOT);
	 } else if ((lbl.equals(InventoryShortfallChartDataModel.CONFIRMED_DUE_IN_SHORTFALL_LABEL)) || 
		    (lbl.equals(InventoryShortfallChartDataModel.PROJECTED_DUE_OUT_SHORTFALL_LABEL)) ||
		    (lbl.equals(InventoryShortfallChartDataModel.PROJECTED_DUE_IN_SHORTFALL_LABEL))) {
	     setSeriesColor(series, seriesColor);
	     series.getStyle().setSymbolSize(0);
	     series.getStyle().setLineWidth(2);
	 } else {
	     System.err.println("InventoryChart shortfall series with unrecongized label <"+lbl+">");
	 }
	 
	}
	removeableViews.add(chartDataView);
    }

    private JCChart createChart(String title) {
	JCChart chart = new JCChart();
	setChartTitle(title);
	return chart;
    }

    private void setChartTitle(String title) {
	((JLabel)chart.getHeader()).setText(title);
    }

    /** Add a view to a chart and set the data model.
	The on hand inventory schedule is passed to the data model as it
	is needed for ALL inventory schedules (regardless of whether or not
	it is graphed; see InventoryChartDataModel comments).
    */

    private ChartDataView addView(JCChart chart, int chartType, 
				  InventoryChartDataModel dm) {

	ChartDataView chartDataView = new ChartDataView();
	chartDataView.setChartType(chartType);
	chartDataView.setDataSource(dm);
	//     // DEBUG
	//     if (chartDataView.getDataSource() == null) {
	// 	System.err.println("InventoryChart.addView() fail to get data source for "+
	// 			   legendTitle);
	//     }
	chart.setDataView(viewIndex, chartDataView);
	viewIndex++;
	dataViews.put(chartDataView, dm);
	String[] names = dm.getScheduleNames();
	// set series color
	for (int i = 0; i < chartDataView.getNumSeries(); i++) {
	    setSeriesColor(chartDataView.getSeries(i), 
			   colorTable.get(names[i]));

	    if(names[i].endsWith(INACTIVE)) {
		setInactiveSeriesPattern(chartDataView.getSeries(i));
	    }
	}


	// set line width
	if (chartType == JCChart.PLOT)
	    for (int j = 0; j < chartDataView.getNumSeries(); j++)

		chartDataView.getSeries(j).getStyle().setLineWidth(2);

	// set removeable - has checkbox in legend
	removeableViews.add(chartDataView);
	return chartDataView;
    }

    private void removeAllViews() {

	for(int i=viewIndex-1; i>=0; i--) {
	    ChartDataView iView = chart.getDataView(i);
	    InventoryBaseChartDataModel baseDM = 
			    (InventoryBaseChartDataModel) iView.getDataSource();
	    //	    System.out.println("Removing: " + baseDM.getDataSourceName() + " at " + i);
	    chart.removeDataView(i);
	    dataViews.remove(iView);
	    removeableViews.remove(iView);
	}

	viewIndex=0;
    }

    public ChartDataModel getChartDataModel(ChartDataView view) {
	return (ChartDataModel) dataViews.get(view);
    }

    public static Vector getScheduleTypesForLegend(String legendTitle, boolean detailed) {
	//MWD
	Vector scheduleTypes = new Vector(3);
	if(legendTitle.equals(INVENTORY_LEGEND)) {
	    scheduleTypes.addElement(ON_HAND);
	}
	else if(detailed && (legendTitle.equals(INVENTORY_DETAILS_LEGEND))) {
	    scheduleTypes.addElement(ON_HAND_DETAILED);
	}
	else if(detailed && (legendTitle.equals(REQUESTED_INVENTORY_DETAILS_LEGEND))) {
	    scheduleTypes.addElement(REQUESTED_DUE_IN);
	    scheduleTypes.addElement(REQUESTED_DUE_OUT);
	}
	else if(detailed && (legendTitle.equals(ACTUAL_INVENTORY_DETAILS_LEGEND))) {
	    scheduleTypes.add(DUE_IN);
	    scheduleTypes.add(DUE_OUT);
	}
	else if(legendTitle.equals(REQUESTED_INVENTORY_LEGEND)) {
	    scheduleTypes.addElement(REQUESTED_DUE_IN);
	    scheduleTypes.addElement(PROJECTED_REQUESTED_DUE_IN);
	    scheduleTypes.addElement(REQUESTED_DUE_OUT);
	    scheduleTypes.addElement(PROJECTED_REQUESTED_DUE_OUT);
	}
	else if(legendTitle.equals(ACTUAL_INVENTORY_LEGEND)) {
	    scheduleTypes.add(DUE_IN);
	    scheduleTypes.add(PROJECTED_DUE_IN);
	    scheduleTypes.add(DUE_OUT);
	    scheduleTypes.add(PROJECTED_DUE_OUT);

	}
	else if(legendTitle.equals(REQUESTED_INACTIVE_INVENTORY_LEGEND)) {
	    scheduleTypes.addElement(REQUESTED_DUE_IN + INACTIVE);
	    scheduleTypes.addElement(PROJECTED_REQUESTED_DUE_IN + INACTIVE);
	    scheduleTypes.addElement(REQUESTED_DUE_OUT + INACTIVE);
	    scheduleTypes.addElement(PROJECTED_REQUESTED_DUE_OUT + INACTIVE);

	}
	else if(legendTitle.equals(ACTUAL_INACTIVE_INVENTORY_LEGEND)) {
	    scheduleTypes.add(DUE_IN + INACTIVE);
	    scheduleTypes.add(PROJECTED_DUE_IN + INACTIVE);
	    scheduleTypes.add(DUE_OUT + INACTIVE);
	    scheduleTypes.add(PROJECTED_DUE_OUT + INACTIVE);
	}
	else if(legendTitle.equals(REQUESTED_SUPPLY_LEGEND)) {
	    scheduleTypes.addElement(REQUESTED_DUE_IN);
	    scheduleTypes.addElement(PROJECTED_REQUESTED_DUE_IN);
	}
	else if(legendTitle.equals(ACTUAL_SUPPLY_LEGEND)) {
	    scheduleTypes.add(DUE_IN);
	    scheduleTypes.add(PROJECTED_DUE_IN);
	}
	else if(legendTitle.equals(SHORTFALL_LEGEND)) {
	}
	else if(legendTitle.equals(TOTAL_CAPACITY_LEGEND)) {
	    scheduleTypes.addElement(TOTAL_LABOR);
	    scheduleTypes.addElement(ALLOCATED);
	}
	else if(legendTitle.equals(ALLOCATED_LEGEND)) {
	    scheduleTypes.addElement(ALLOCATED);
	}
	else if(legendTitle.equals(UNCONFIRMED_LEGEND)) {
	    scheduleTypes.add(UNCONFIRMED_DUE_IN);
	}
	else {
	    throw new RuntimeException("Unknown legend title");
	}

	return scheduleTypes;
    }

    public static Vector extractSchedulesFromInventory(Vector scheduleNames,
						       UISimpleInventory inventory) {
    
      Vector schedulesToPlot = new Vector(scheduleNames.size());

      for (int i = 0; i < scheduleNames.size(); i++) {
	    String scheduleName = (String)scheduleNames.elementAt(i);
	    UISimpleNamedSchedule s =
		inventory.getNamedSchedule(scheduleName);

	    if (s != null) 
		schedulesToPlot.addElement(s);
	}
      
      return schedulesToPlot;
    }

    public static Vector extractVectorFromInventory(String scheduleName,
						      UISimpleInventory inventory) {
      Vector theSchedule=null;

      UISimpleNamedSchedule tmp =
	  inventory.getNamedSchedule(scheduleName);
      if (tmp != null)
	  theSchedule = tmp.getSchedule();

      return theSchedule;
    }

    public static Vector extractOnHandSchedule(UISimpleInventory inventory) {

      Vector onHandSchedule=null;

      boolean inventoryFlag = 
	  inventory.getScheduleType().equals(PlanScheduleType.TOTAL_INVENTORY);

      if (inventoryFlag) {
	  onHandSchedule = extractVectorFromInventory(ON_HAND,inventory);
      }

      return onHandSchedule;
    }
	

    private InventoryChartDataModel createInventoryDataModel(UISimpleInventory inventory,
							     String legendTitle) {
	Vector scheduleNames = getScheduleTypesForLegend(legendTitle,false);
	return createInventoryDataModel(scheduleNames,inventory,legendTitle, false);
    }

    private InventoryChartDataModel createDetailedInventoryDataModel(UISimpleInventory inventory,
								     String legendTitle) {
	Vector scheduleNames = getScheduleTypesForLegend(legendTitle,true);
	return createInventoryDataModel(scheduleNames,inventory,legendTitle,true);
    }

    private InventoryChartDataModel 
	createInventoryDataModel(Vector scheduleNames,
				 UISimpleInventory inventory,
				 String legendTitle,
				 boolean detailed) {

	// create a vector of schedules that are part of this model
	Vector schedulesToPlot = 
	    extractSchedulesFromInventory(scheduleNames,inventory);

	boolean inventoryFlag = 
	    (inventory.getScheduleType().equals(PlanScheduleType.TOTAL_INVENTORY) ||
	     inventory.getScheduleType().equals(UIInventoryImpl.NO_INVENTORY_SCHEDULE_JUST_CONSUME));


	if(detailed) {
	    Vector onHandSchedule=extractVectorFromInventory(ON_HAND_DETAILED,inventory);
	    return new InventoryDetailsChartDataModel(inventoryFlag,
						     inventory.getAssetName(),
						     inventory.getUnitType(),
						     schedulesToPlot,
						     onHandSchedule,
						     legendTitle,
						     inventory.isProvider(),
						     baseCDayTime,
						     displayCDay);
	}
	else {   
	    Vector onHandSchedule = extractOnHandSchedule(inventory);
	    return new InventoryChartDataModel(inventoryFlag,
					       inventory.getAssetName(),
					       inventory.getUnitType(),
					       schedulesToPlot,
					       onHandSchedule,
					       legendTitle,
					       inventory.isProvider(),
					       baseCDayTime,
					       displayCDay);
	}
    }

    private void setSeriesColor(ChartDataViewSeries series, Color color) {
	series.getStyle().setLineColor(color);
	series.getStyle().setFillColor(color);
	series.getStyle().setSymbolColor(color);
    }

    private void setInactiveSeriesPattern(ChartDataViewSeries series) {
	series.getStyle().setFillPattern(INACTIVE_SERIES_PATTERN);
    }

    private void customizeAxes(JCChart chart, String yAxisTitleText) {
	myYAxisTitle = yAxisTitleText;
	resetAxes();
    }  

    private void resetAxes() {
	//     if (yAxisTitleText.equals("STons"))
	//       yAxisTitleText = "Rounds";

	java.util.List viewList = chart.getDataView();
	for (int i = 0; i < viewList.size(); i++) {
	    ChartDataView chartDataView = (ChartDataView)viewList.get(i);

	    // use time axis for x axis
	    JCAxis xaxis = chartDataView.getXAxis();
	    
	    if(displayCDay) {
		xaxis.setAnnotationMethod(JCAxis.VALUE);
		xaxis.setPrecision(0);

		//xaxis.setAnnotationMethod(JCAxis.TIME_LABELS);
		//xaxis.setTimeUnit(JCAxis.DAYS);
		//xaxis.setTimeBase(new Date(InventoryChartBaseCalendar.getBaseTime() - baseCDayTime ));
		//xaxis.setTimeFormat("D");

		xaxis.setTickSpacing(7);
		xaxis.setGridSpacing(7);
		//		xaxis.setTickSpacingIsDefault(true);
		//              xaxis.setGridSpacingIsDefault(true);
		//xaxis.setMin(CDAY_MIN);
		xaxis.setGridVisible(true);
		xaxis.setAnnotationRotation(JCAxis.ROTATE_NONE);
	    }
	    else {
		xaxis.setAnnotationMethod(JCAxis.TIME_LABELS);
		xaxis.setTimeBase(new Date(InventoryChartBaseCalendar.getBaseTime()));
		if(displayDetails) {
		    /**
		    xaxis.setTimeUnit(JCAxis.SECONDS);
		    xaxis.setTickSpacing(7*24*60*60);
		    xaxis.setGridSpacing(7*24*60*60);
		    */
		    xaxis.setTimeUnit(JCAxis.MINUTES);
		    xaxis.setTickSpacing(7*24*60);
		    xaxis.setGridSpacing(7*24*60);
		    xaxis.setTimeFormat("M/d H:m");
		}
		else {
		    xaxis.setTimeUnit(JCAxis.DAYS);
		    xaxis.setTickSpacing(7);
		    xaxis.setGridSpacing(7);
		    xaxis.setTimeFormat("M/d");
		}

		//System.out.println("Base Date is: " + new Date(InventoryChartBaseCalendar.getBaseTime()));
		xaxis.setGridVisible(true);
		xaxis.setAnnotationRotation(JCAxis.ROTATE_270);
	    }


      
	    // y axis titles
	    JCAxis yaxis = chartDataView.getYAxis();
	    yaxis.setGridVisible(true);
	    JCAxisTitle yAxisTitle = new JCAxisTitle(myYAxisTitle);
	    yAxisTitle.setRotation(ChartText.DEG_270);
	    yAxisTitle.setPlacement(JCLegend.WEST);
	    yaxis.setTitle(yAxisTitle);
	    //       yaxis.setEditable(false); // don't allow zoomin on this axis
	    yaxis.setMin(0); // set zero as the minimum value on the axis
	}
    }  

    public JCChart getChart() {
	return chart;
    }

    public boolean getShowInactive() {
	return doShowInactive;
    }

    /** Called from legend to determine if the view label
	should be a checkbox (allowing the view to be 
	displayed or hidden).
    */
    public boolean isChartDataViewSelectable(ChartDataView view) {
	Enumeration enum = removeableViews.elements();
	while (enum.hasMoreElements()) {
	    ChartDataView cdv = (ChartDataView)enum.nextElement();
	    if (cdv.equals(view))
		return true;
	}
	return false;
    }

    public void setInactiveViewsVisible(boolean visible) {

	Enumeration enum = removeableViews.elements();
	int currIndex=0;
	ChartDataView cdv=null;
	ChartDataView actual,requested,actualInactive,requestedInactive;
	
	actual=null;
	requested=null;
	actualInactive=null;
	requestedInactive=null;

	while (enum.hasMoreElements()) {
	    cdv = (ChartDataView)enum.nextElement();
	    if(cdv.getName().equals(REQUESTED_INACTIVE_INVENTORY_LEGEND)){
		requestedInactive=cdv;
	    } 
	    else if(cdv.getName().equals(ACTUAL_INACTIVE_INVENTORY_LEGEND)) {
		actualInactive=cdv;
	    }
	    else if(cdv.getName().equals(REQUESTED_INVENTORY_LEGEND)) {
		requested=cdv;
	    }
	    else if(cdv.getName().equals(ACTUAL_INVENTORY_LEGEND)) {
		actual=cdv;     
	    }
	}
	
	//only set visible f there active counterparts are visible
	if(!visible || actual.isVisible()) actualInactive.setVisible(visible);
	if(!visible || requested.isVisible()) requestedInactive.setVisible(visible);


	/****
	 **
	 ** below is more efficient, but hard to reconcile with 
	 ** updates.
	 ** MWD Remove.

	chart.setBatched(true);

	if(visible) {	
	    while (enum.hasMoreElements()) {
		cdv = (ChartDataView)enum.nextElement();
		chart.setDataView(currIndex,cdv);
		currIndex++;
	    }
	}
	else {
	    for(int i=viewIndex-1; i>=0; i--) {
		cdv=chart.getDataView(i);
		if((cdv.getName().equals(REQUESTED_INACTIVE_INVENTORY_LEGEND)) ||
		    (cdv.getName().equals(ACTUAL_INACTIVE_INVENTORY_LEGEND))) {
		    chart.removeDataView(i);
		}
	    }
	}


	// MWD remove
	//resetAxes();
	//chart.setChanged(true,chart.LAYOUT);
	//chart.setChanged(true,chart.RECALC);
	//chart.setChanged(true,chart.REDRAW);


	chart.setBatched(false);
	chart.update();


	**/

    }



    /**
       Called from legend when user selects/deselects checkbox
       for a view.
    */
    public void setChartDataViewVisible(String name, boolean visible) {
	Enumeration enum = removeableViews.elements();
	while (enum.hasMoreElements()) {
	    ChartDataView cdv = (ChartDataView)enum.nextElement();
	    if (cdv.getName().equals(name)) {
		//don't make the INACTIVE ones visible if they've been
		//turned off.
		if(!(((cdv.getName().equals(REQUESTED_INACTIVE_INVENTORY_LEGEND)) ||
		      (cdv.getName().equals(ACTUAL_INACTIVE_INVENTORY_LEGEND)))
		     &&(!doShowInactive))){
		    cdv.setVisible(visible);
		}
		return;
	    }
	}
    }

    public void setViewsVisible(boolean visible) {
	Enumeration enum = removeableViews.elements();
	while (enum.hasMoreElements()) {
	    ChartDataView view = (ChartDataView)enum.nextElement();
	    view.setVisible(visible);
	}
    }

    /** Implements point labels displayed beneath chart.
	Ignore events for which the dataIndex is null, or the dataView is null,
	or the seriesIndex or pointInSeries are -1.
    */
    public void pick(JCPickEvent e) {
	JCDataIndex dataIndex = e.getPickResult();
	// check for all the possible failures
	if (dataIndex == null) {
	    System.out.println("WARNING: dataIndex is null");
	    return;
	}
	ChartDataView chartDataView = dataIndex.getDataView();
	if (chartDataView == null) {
	    System.out.println("WARNING: chartDataView is null");
	    return;
	}
	int seriesIndex = dataIndex.getSeriesIndex();
	int pt = dataIndex.getPoint();
	if (pt < 0 || seriesIndex < 0) {

	    System.out.println("WARNING: series or point index is null");
	    return;
	}
	// ChartDataModel dataModel = chartDataView.getDataSource();
	// temporary until chart data view to data model correspondence is working
	ChartDataModel dataModel = (ChartDataModel)dataViews.get(chartDataView);
	if (dataModel == null) {
	    System.out.println("WARNING: data model is null");
	    return;
	}

	// user has picked a valid point
	double[] x = dataModel.getXSeries(seriesIndex);
	double[] y = dataModel.getYSeries(seriesIndex);
	

	int cDay;
	int dayOfYear;
	int daysFromBaseToCDay = (int) ((baseCDayTime - InventoryChartBaseCalendar.getBaseTime())
					/ InventoryChartDataModel.MILLIS_IN_DAY);

	if(displayCDay) {
	    cDay = ((int)x[pt]);
	    dayOfYear = cDay + daysFromBaseToCDay;
	} else {
	    if(displayDetails) {
		dayOfYear = (int)(((int)x[pt])/(24*60));
	    }
	    else {
		dayOfYear = ((int)x[pt]);
	    }
	    cDay = dayOfYear - daysFromBaseToCDay;
	}
	InventoryChartBaseCalendar tmpC = new InventoryChartBaseCalendar();
	tmpC.set(Calendar.YEAR, InventoryChartBaseCalendar.getBaseYear());
	tmpC.set(Calendar.DAY_OF_YEAR, dayOfYear);
	// add 1 to month as it numbers them from 0
	int month = tmpC.get(Calendar.MONTH) + 1;
	
	String time="";
	if(displayDetails) {
	    time = " " + tmpC.get(Calendar.HOUR_OF_DAY) + ":" +
		tmpC.get(Calendar.MINUTE) + " ";
	}

	String label = "Date: " + month + "/" +
	    tmpC.get(Calendar.DAY_OF_MONTH) + "/" +
	    tmpC.get(Calendar.YEAR) + " " +
	    "(C" + cDay + ")" + time +
	    " Quantity: " + JCChartUtil.format(y[pt], 1);
	
	    
	pointLabel.setText(label);
    }

    public static void main(String args[]) {
	ThemeFactory.establishMetalTheme();
	JCExitFrame f = new JCExitFrame(INVENTORY_LEGEND);
	f.getContentPane().add(new InventoryChart());
	f.setSize(600, 500);
	f.setVisible(true);
    }

    public boolean setAlpNow(Date now) {
	JCAxis xAxis = chart.getChartArea().getXAxis(0);

	
	double nowValue;

	if(displayCDay) 
	    nowValue = ((now.getTime() - baseCDayTime)
			/ InventoryChartDataModel.MILLIS_IN_DAY);
	else 
	    nowValue = xAxis.dateToValue(now);

	    
	System.out.println("InventoryChart::setAlpNow: NowDate: " + now + " NowValue:" + nowValue);
	//System.out.println("InventoryChart::setAlpNow: minValue:" + xAxis.getMin() + " maxValue:" + xAxis.getMax() + " diff:" + (xAxis.getMax() - xAxis.getMin()) + " diff/4:" + ((xAxis.getMax() - xAxis.getMin())/4));
	
	alpNow=now;

	if((nowValue > xAxis.getMin()) &&
	   (nowValue < xAxis.getMax())) {
	    
	    JCValueLabel[] xLabels = xAxis.getGeneratedValueLabels();
	    
	    int nowX=xAxis.toPixel(nowValue,true); 
	    
	    //adjust for scaling / there seems to be a loss of accuracy
	    //the more number of days are displayed on the screen
	    //adjusting by that factor.
	    nowX=nowX+((int)((xAxis.getMax()-xAxis.getMin()) / 8));
	    

	    int nowY=xAxis.getTop();

	    //System.out.println("InventoryChart::XAxis Origin: " + xAxis.getOrigin() +  "\nOrigin Placement:" + xAxis.getOriginPlacement() +  "\nPlacment:" + xAxis.getPlacement() + "\nPlacement Location:" + xAxis.getPlacementLocation());	    
	    
	    // MWD remove
	    // double res = ((xAxis.getTickSpacing() - xAxis.getGap())/2);
	    // System.out.println("InventoryChart::xAxis gap: " + xAxis.getGap() +" xAxis: tick spacing: " + xAxis.getTickSpacing() + " xAxis: number spacing: " + xAxis.getNumSpacing());

	    
	    nowLabel.setCoord(new Point(nowX,nowY));
	    nowLabel.getComponent().setVisible(true);
	    return true;
	}
	else {
	    nowLabel.getComponent().setVisible(false);
	    return false;
	}

    }

    public void resetAlpNow() { 
	setAlpNow(alpNow);
    }

    public void resetChart() {
	chart.reset();
	resetAlpNow();

	java.util.List viewList = chart.getDataView();
	for (int i = 0; i < viewList.size(); i++) {
	    ChartDataView chartDataView = (ChartDataView)viewList.get(i);
	    
	    /*** MWD SetMin has bad ramifications for toggling
		 // use time axis for x axis
		 JCAxis xaxis = chartDataView.getXAxis();
		 if(displayCDay) 
		 xaxis.setMin(CDAY_MIN);
	    ***/
	    
	    JCAxis yaxis = chartDataView.getYAxis();
	    yaxis.setMin(0);
	}
    }


    public void setDisplayCDays(boolean useCDays) {
	if(useCDays != displayCDay) {
	    displayCDay=useCDays;

	    resetAxes();
	    
	    //reinitializeAndUpdate();

	    /*** MWD Took out and now again have reinitializeAndUpdate()
		 reinitialize does same basic thing as this.**/
	    

	    java.util.List viewList = chart.getDataView();
	    for (int i = 0; i < viewList.size(); i++) {
		ChartDataView chartDataView = (ChartDataView)viewList.get(i);
		ChartDataModel dm = chartDataView.getDataSource();
		if(dm != null) {
		    if (dm instanceof InventoryBaseChartDataModel) {
			InventoryBaseChartDataModel baseDM = 
			    (InventoryBaseChartDataModel) dm;
			baseDM.setDisplayCDays(useCDays);
		    }
		    else
			System.out.println("View with data model of type: " + dm.getClass().getName());
		}
		else
		    System.out.println("View with Null Data Source");
	    }
	    

	    resetChart();
	}
    }

    protected JPanel createChecksAndLegendButtonPanel() {
	JPanel cnlPanel = new JPanel(new BorderLayout());
	legendButton = createLegendButton();
	JPanel cbPanel = createCheckBoxPanel();

	cnlPanel.add(legendButton,BorderLayout.WEST);
	cnlPanel.add(cbPanel,BorderLayout.EAST);

	return cnlPanel;
    }
	

    protected JPanel createCheckBoxPanel() {
	JPanel cbPanel = new JPanel(new GridBagLayout());

	cdaysModeCheck = new JCheckBox(CDAY_MODE,false);
	
	Font newFont = cdaysModeCheck.getFont();
	newFont = newFont.deriveFont(newFont.getStyle(), (float) 15);
	
	cdaysModeCheck.setFont(newFont);
	cdaysModeCheck.setActionCommand(CDAY_MODE);
	cdaysModeCheck.setToolTipText("Display xaxis dates as C-Days");
	cdaysModeCheck.addItemListener(this);
	cbPanel.add(cdaysModeCheck,
		    new GridBagConstraints(0,0, 1, 1, 1.0, 1.0,
					   GridBagConstraints.WEST, 
					   GridBagConstraints.NONE, 
					   blankInsets, 0, 0));

	showInactiveCheck = new JCheckBox(SHOW_INACTIVE,doShowInactive);
	showInactiveCheck.setFont(newFont);
	showInactiveCheck.setActionCommand(SHOW_INACTIVE);
	showInactiveCheck.setToolTipText("Display the inactive allocations");
	showInactiveCheck.addItemListener(this);
	cbPanel.add(showInactiveCheck,
		    new GridBagConstraints(1,0, 1, 1, 1.0, 1.0,
					   GridBagConstraints.WEST, 
					   GridBagConstraints.NONE, 
					   blankInsets, 0, 0));

	if(canDisplayDetails) {
	    JCheckBox displayDetailsCheck = new JCheckBox(DISPLAY_DETAILS,displayDetails);
	    displayDetailsCheck.setFont(newFont);
	    displayDetailsCheck.setActionCommand(DISPLAY_DETAILS);
	    displayDetailsCheck.setToolTipText("Display the inventory graph details");
	    displayDetailsCheck.addItemListener(this);
	    cbPanel.add(displayDetailsCheck,
			new GridBagConstraints(2,0, 1, 1, 1.0, 1.0,
					       GridBagConstraints.WEST, 
					       GridBagConstraints.NONE, 
					       blankInsets, 0, 0));
	}

	return cbPanel;

    }

    protected JToggleButton createLegendButton() {
	JToggleButton lButton = new LegendToggleButton(true);
	lButton.setToolTipText ("Show/Hide Legend");

	//          ((JScrollPane) rainbowChart).setCorner
        //    (JScrollPane.UPPER_RIGHT_CORNER, legendButton);

	lButton.addActionListener(this);
	
	return lButton;
    }
    

    public void actionPerformed(ActionEvent e) {
	System.out.println("InventoryChart::actionPerformed: Event: " + e);
	if(e.getSource() == legendButton) {
	    System.out.println("Is Legend Button !");    
	    if(legendButton.isSelected()) {
		System.out.println("Is Selected!");
		add(legend,legendLocale);
	    }
	    else {
		remove(legend);
	    }

	    revalidate();
	    repaint();
	}
    }

    public void itemStateChanged(ItemEvent e) {
	if(e.getSource() instanceof JCheckBox) {
	    JCheckBox source = (JCheckBox) e.getSource();
	    if(source.getActionCommand().equals(CDAY_MODE)) {
		setDisplayCDays(e.getStateChange() == e.SELECTED);
	    }
	    else if(source.getActionCommand().equals(SHOW_INACTIVE)) {
		doShowInactive = (e.getStateChange() == e.SELECTED);
		setInactiveViewsVisible(doShowInactive);
	    }
	    else if(source.getActionCommand().equals(DISPLAY_DETAILS)) {
		displayDetails = (e.getStateChange() == e.SELECTED);
		System.out.println("InventoryChart::itemStateChanged:Toggled Details - value is :" + displayDetails);
		if(displayCDay) { 
		    cdaysModeCheck.setSelected(false);
		    displayCDay=false;
		}
		chart.reset();
		if(displayDetails) 
		    resetAxes();
		reinitializeAndUpdate();
		if(!displayDetails) 
		    resetAxes();
	    }
	}
    }

    public class LegendToggleButton extends JToggleButton {

	public LegendToggleButton(boolean isSelected) {
	    super(new HorzLegendIcon(), isSelected);
	    setSelectedIcon(new VertLegendIcon());
	    setMargin (blankInsets);
	    setBorderPainted (false);
	    this.setToolTipText("Show/Hide Legend");
	    this.setOpaque(true);
	}

	/**
	 * Notification from the UIFactory that the L&F
	 * has changed. 
	 *
	 * @see JComponent#updateUI
	 */
	public void updateUI() {
	    this.setUI((ButtonUI)UIManager.getUI(this));
	}


	/**
	 * Returns a string that specifies the name of the L&F class
	 * that renders this component.
	 *
	 * @return "CheckBoxUI"
	 * @see JComponent#getUIClassID
	 * @see UIDefaults#getUI
	 * @beaninfo
	 *        expert: true
	 *   description: A string that specifies the name of the L&F class
	 */
	public String getUIClassID() {
	    return (checkBoxUIClassID);
	}
    }
    class VertLegendIcon implements Icon {
	
	public int getIconHeight() {
	    return 16;
	}
	
	public int getIconWidth() {
	    return 8;
	}
	
	public void paintIcon (Component c, Graphics g, int x, int y) {
	    g.setColor (Color.red);
	    g.fillRect (x, y, 8, 4);
	    g.setColor (Color.blue);
	    g.fillRect (x, y + 4, 8, 4);
	    g.setColor (Color.green);
	    g.fillRect (x, y + 8, 8, 4);
	    g.setColor (Color.yellow);
	    g.fillRect (x, y + 12, 8, 4);
	}
	
    }
    
    
    static class HorzLegendIcon implements Icon {
	    
	public int getIconHeight() {
	    return 8;
	}
	
	public int getIconWidth() {
		return 16;
	}
	    
	public void paintIcon (Component c, Graphics g, int x, int y) {
	    g.setColor (Color.red);
	    g.fillRect (x, y, 4, 8);
	    g.setColor (Color.blue);
	    g.fillRect (x + 4, y, 4, 8);
	    g.setColor (Color.green);
	    g.fillRect (x + 8, y, 4, 8);
	    g.setColor (Color.yellow);
	    g.fillRect (x + 12, y, 4, 8);
	}
    }   

}















