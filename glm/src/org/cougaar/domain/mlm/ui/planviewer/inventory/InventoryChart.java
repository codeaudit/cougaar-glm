/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.inventory;

import org.cougaar.domain.glm.ldm.plan.PlanScheduleType;
import org.cougaar.util.ThemeFactory;

import com.klg.jclass.chart.*;
//451K uncomment MWD 
import com.klg.jclass.util.legend.*;
import com.klg.jclass.chart.data.JCDefaultDataSource;
import com.klg.jclass.util.swing.JCExitFrame;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;

import org.cougaar.domain.mlm.ui.data.UIInventoryImpl;
import org.cougaar.domain.mlm.ui.data.UISimpleInventory;
import org.cougaar.domain.mlm.ui.data.UISimpleNamedSchedule;
import org.cougaar.domain.mlm.ui.data.UIQuantityScheduleElement;

public class InventoryChart extends JPanel implements JCPickListener,
ActionListener {
    JCChart chart=null;
    InventoryLegend legend=null;
    GridBagConstraints legendLocale=null;
    JToggleButton legendButton=null;

    JLabel pointLabel; // initialized when creating chart, set by pick method
    Hashtable dataViews = new Hashtable(); // temporary, until data view-data model correspondence is working
    InventoryColorTable colorTable;
    static final String INVENTORY_LEGEND = "Inventory";
    static final String REQUESTED_INVENTORY_LEGEND = "Requested";
    static final String ACTUAL_INVENTORY_LEGEND = "Actual";
    static final String REQUESTED_SUPPLY_LEGEND = REQUESTED_INVENTORY_LEGEND + " ";
    static final String ACTUAL_SUPPLY_LEGEND = ACTUAL_INVENTORY_LEGEND + " ";
    static final String SHORTFALL_LEGEND = "Shortfall";
    static final String TOTAL_CAPACITY_LEGEND = "Total Capacity";
    static final String ALLOCATED_LEGEND = "Allocated";
    static final String UNCONFIRMED_LEGEND = "UnConfirmed";
    
    static final int CDAY_MIN=-5;
    Vector removeableViews = new Vector();
    int viewIndex = 0;
    int gridx = 0;
    int gridy = 0;
    Insets blankInsets = new Insets(0, 0, 0, 0);

    UISimpleInventory myInventory;
    String myTitle;
    String myYAxisTitle;

    InventoryExecutionTimeStatusHandler timeStatusHandler;
    JCChartLabel nowLabel=null;
    Date alpNow=new Date();

    long baseCDayTime;
    boolean displayCDay=false;

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
	this("Mock 3-FSB", getMockInventory() , null);
    }


    public static UISimpleInventory getMockInventory() {

	UISimpleInventory inventory = new UISimpleInventory();

	Date now = new Date();
	long nowTime = now.getTime();
	final long aDayTime = 24*60*60*1000;
	final int numDays = 32;
	
	final double DUE_OUT_QTY=150;
	final double DUE_IN_QTY=750;
	final double PROJ_DUE_OUT_QTY=75;    

	final double PROJ_SHORTFALL_QTY=60;
	final double DUE_IN_SHORTFALL_QTY=500;
	final double DUE_OUT_SHORTFALL_QTY=50;

	double dueOutQty=DUE_OUT_QTY;
	double dueInQty=DUE_IN_QTY;
	double projDueOutQty=PROJ_DUE_OUT_QTY;    

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
	Vector projDueOuts= new Vector(numDays);
	
	Vector reqDueIns=new Vector(numDays);
	Vector reqDueOuts=new Vector(numDays);
	Vector reqProjDueOuts=new Vector(numDays);

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
	    reqProjDueOuts.add(new UIQuantityScheduleElement(lastNow,
							     currNow,
							     projDueOutQty));


	    if(shortfallDay) {
		projDueOutQty = PROJ_SHORTFALL_QTY;
	    }
	    projDueOuts.add(new UIQuantityScheduleElement(lastNow,
							  currNow,
							  projDueOutQty));

	    //currInventory=currInventory-projDueOutQty;
	    
	    if((currNow >= c0Time) && 
	       ((i%2)==0)){
		dueOutQty = DUE_OUT_QTY;

		reqDueOuts.add(new UIQuantityScheduleElement(lastNow,
							     currNow,
							     dueOutQty));
		if(shortfallDay) {
		    dueOutQty = DUE_OUT_SHORTFALL_QTY;
		}
		dueOuts.add(new UIQuantityScheduleElement(lastNow,
							  currNow,
							  dueOutQty));
		currInventory=currInventory-dueOutQty;
	    } else {
		reqDueOuts.add(new UIQuantityScheduleElement(lastNow,
							     currNow,
							     0));
		dueOuts.add(new UIQuantityScheduleElement(lastNow,
							  currNow,
							  0));
	    }
	    
	    if((currNow >= c0Time) && 
	       ((i%10)==0)){
		dueInQty = DUE_IN_QTY;
		reqDueIns.add(new UIQuantityScheduleElement(lastNow,
							    currNow,
							    dueInQty));
		if(shortfallDay) {
		    dueInQty = DUE_IN_SHORTFALL_QTY;
		}

		dueIns.add(new UIQuantityScheduleElement(lastNow,
							 currNow,
							 dueInQty));
		currInventory+=dueInQty;

		dueInCtr++;
	    } else {
		reqDueIns.add(new UIQuantityScheduleElement(lastNow,
							    currNow,
							    0));
		dueIns.add(new UIQuantityScheduleElement(lastNow,
							 currNow,
							 0));
	    }
	    
	    lastNow = currNow;
	    currNow+=aDayTime;
	    
	    onHand.add(new UIQuantityScheduleElement(lastNow,
						     currNow,
						     currInventory));
	    

	    
	    
	}
	

	inventory.addNamedSchedule(UISimpleNamedSchedule.ON_HAND,onHand);
	inventory.addNamedSchedule(UISimpleNamedSchedule.DUE_IN,dueIns);
	inventory.addNamedSchedule(UISimpleNamedSchedule.REQUESTED_DUE_IN,reqDueIns);
	
	inventory.addNamedSchedule(UISimpleNamedSchedule.DUE_OUT,dueOuts);
	inventory.addNamedSchedule(UISimpleNamedSchedule.REQUESTED_DUE_OUT,reqDueOuts);

	inventory.addNamedSchedule(UISimpleNamedSchedule.PROJECTED_DUE_OUT,
				   projDueOuts);
	inventory.addNamedSchedule(UISimpleNamedSchedule.PROJECTED_REQUESTED_DUE_OUT,
				   reqProjDueOuts);

	// MWD Uncomment to get Unconfirmed into the mix.
	//	inventory.addNamedSchedule(UISimpleNamedSchedule.UNCONFIRMED_DUE_IN,unconfirmedDueIns);

	
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
    public InventoryChart(String title, UISimpleInventory inventory, 
			  InventoryExecutionTimeStatusHandler aTimeHandler) {
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
	} else if (scheduleType.equals(PlanScheduleType.TOTAL_INVENTORY)) {
	    initializeTotalInventoryChart(title, inventory);
	} else if (scheduleType.equals(UIInventoryImpl.NO_INVENTORY_SCHEDULE_JUST_CONSUME)) {
	    initializeConsumeOnlyChart(title, inventory);
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
	add(pointLabel, new GridBagConstraints(gridx, gridy++, 1, 1, 1.0, 0.0,
					       GridBagConstraints.CENTER, 
					       GridBagConstraints.NONE, 
					       blankInsets, 0, 0));

	legendButton = new JToggleButton(new HorzLegendIcon(), true);
	legendButton.setSelectedIcon(new VertLegendIcon());
	legendButton.setMargin (new Insets (0, 0, 0, 0));
	legendButton.setBorderPainted (false);
	legendButton.setToolTipText ("Show/Hide Legend");

	//          ((JScrollPane) rainbowChart).setCorner
        //    (JScrollPane.UPPER_RIGHT_CORNER, legendButton);


	legendButton.addActionListener(this);

	add(legendButton, new GridBagConstraints(gridx, gridy++, 1, 1, 0.2, 0.0,
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

	timeStatusHandler = aTimeHandler;
	if(timeStatusHandler != null) {
	    setAlpNow(timeStatusHandler.getLastDayRollover());
	}

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
	} else if (scheduleType.equals(PlanScheduleType.TOTAL_INVENTORY)) {
	    initializeTotalInventoryChart(myTitle, myInventory);
	} else if (scheduleType.equals(UIInventoryImpl.NO_INVENTORY_SCHEDULE_JUST_CONSUME)) {
	    initializeConsumeOnlyChart(myTitle, myInventory);
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
	
	if(timeStatusHandler != null) {
	    setAlpNow(timeStatusHandler.getLastDayRollover());
	}

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
	//scheduleTypes.addElement(UISimpleNamedSchedule.TOTAL_LABOR);
	//scheduleTypes.addElement(UISimpleNamedSchedule.ALLOCATED);
	addView(chart, JCChart.AREA,
		createInventoryDataModel(inventory, TOTAL_CAPACITY_LEGEND));
    }

    private void initializeActualCapacityChart(String title, UISimpleInventory inventory) {
	title = "Capacity of " + inventory.getAssetName() + " at " + title;
	setChartTitle(title);
	//MWD Remove
	//Vector scheduleTypes = new Vector(1);
	//scheduleTypes.addElement(UISimpleNamedSchedule.ON_HAND);
	addView(chart, JCChart.AREA,
		createInventoryDataModel(inventory, INVENTORY_LEGEND));
	//MWD Remove
	//scheduleTypes = new Vector(1);
	//scheduleTypes.addElement(UISimpleNamedSchedule.ALLOCATED);
	addView(chart, JCChart.BAR, 
		createInventoryDataModel(inventory, ALLOCATED_LEGEND));
    }

    // Fix up inventory by making sure if there is a requested schedule,
    // and no actual series then add one.
    private void equalizeInventorySchedule(UISimpleInventory inventory) {
	String[][] reqActTable = {{UISimpleNamedSchedule.REQUESTED_DUE_IN,
				   UISimpleNamedSchedule.DUE_IN},
				  {UISimpleNamedSchedule.REQUESTED_DUE_OUT,
				   UISimpleNamedSchedule.DUE_OUT},
				  {UISimpleNamedSchedule.PROJECTED_REQUESTED_DUE_OUT,
				   UISimpleNamedSchedule.PROJECTED_DUE_OUT}};

	for(int i=0; i<reqActTable.length; i++) {
	    UISimpleNamedSchedule req,act;
	    req = inventory.getNamedSchedule(reqActTable[i][0]);
	    act = inventory.getNamedSchedule(reqActTable[i][1]);
	    if((req!=null) && (act == null)) {
		Vector reqSch = req.getSchedule();
		Vector actSch = new Vector(reqSch.size());
		for(int j=0; j < reqSch.size() ; j++) {
		    UIQuantityScheduleElement element = 
			(UIQuantityScheduleElement) reqSch.elementAt(j);
		    UIQuantityScheduleElement newElement = 
			(UIQuantityScheduleElement) element.clone();
		    newElement.setQuantity(0);
		    actSch.add(newElement);
		}
		inventory.addNamedSchedule(reqActTable[i][1],actSch);
	    }
	    else if((act!=null) && (req == null)) {
		throw new RuntimeException("Did not expect to receive a schedule with an Actual " + reqActTable[i][0] + " but not corresponding requested");
	    }

	}    
    }

    private void initializeTotalInventoryChart(String title, UISimpleInventory inventory) {
	Vector scheduleTypes;


	// if no on hand schedule, then don't plot an inventory graph
	if (inventory.getNamedSchedule(UISimpleNamedSchedule.ON_HAND) == null) {
	    JOptionPane.showMessageDialog(null, "No data received",
					  "No data received",
					  JOptionPane.ERROR_MESSAGE);
	    return;
	}

	// Title and chart
	title = "Inventory of " + inventory.getAssetName() + " at " + title;
	setChartTitle(title);

	equalizeInventorySchedule(inventory);

     
	// On Hand
	// MWD Remove
	//scheduleTypes = new Vector(1);
	//scheduleTypes.addElement(UISimpleNamedSchedule.ON_HAND);
	InventoryChartDataModel chartDataModel = createInventoryDataModel(inventory, 
									  INVENTORY_LEGEND);
	ChartDataView icv = addView(chart, JCChart.BAR, chartDataModel);
	((JCBarChartFormat)icv.getChartFormat()).setClusterWidth(100);
	icv.setOutlineColor(colorTable.get(UISimpleNamedSchedule.ON_HAND));

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

	computeAndInitializeShortfall(inventory,actual,requested);
    }

    private void initializeConsumeOnlyChart(String title, UISimpleInventory inventory) {
    	// Title and chart
	title = "Consumption of " + inventory.getAssetName() + " at " + title;
	setChartTitle(title);

	equalizeInventorySchedule(inventory);

	// Requested
	InventoryChartDataModel requested = 
	    createInventoryDataModel(inventory, 
				     REQUESTED_SUPPLY_LEGEND);
	addView(chart, JCChart.BAR, requested);

	// Actual
	InventoryChartDataModel actual = 
	    createInventoryDataModel(inventory, 
				     ACTUAL_SUPPLY_LEGEND);
	addView(chart, JCChart.BAR, actual);

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
	 if (lbl.equals(InventoryShortfallChartDataModel.DUE_OUT_SHORTFALL_LABEL)) {
	     //setSeriesColor(series, Color.magenta);
	     setSeriesColor(series, new Color(0,204,0));

	     series.getStyle().setSymbolSize(0);
	     series.getStyle().setLineWidth(2);
	     //    JCLineStyle line_style = new JCLineStyle(2,  Color.magenta, JCLineStyle.LONG_DASH);
	     //    series.getStyle().setLineStyle(line_style);
	     //    series.getStyle().setSymbolShape(JCSymbolStyle.CROSS);
	 } else if (lbl.equals(InventoryShortfallChartDataModel.UNCONFIRMED_DUE_IN_SHORTFALL_LABEL)) {
	     //setSeriesColor(series, new Color(255, 95, 0));
	     setSeriesColor(series, new Color(204,51,255));
// 	     JCLineStyle line_style = new JCLineStyle(4,  new Color(255, 95, 0), JCLineStyle.SHORT_DASH);
// 	     series.getStyle().setLineStyle(line_style);
	     series.getStyle().setLineWidth(4);
	     series.getStyle().setSymbolSize(0);
	     // series.getStyle().setSymbolShape(JCSymbolStyle.DOT);
	 } else if (lbl.equals(InventoryShortfallChartDataModel.CONFIRMED_DUE_IN_SHORTFALL_LABEL)) {
	     setSeriesColor(series, Color.cyan);
	     series.getStyle().setSymbolSize(0);
	     series.getStyle().setLineWidth(2);
	 } else if (lbl.equals(InventoryShortfallChartDataModel.PROJECTED_DUE_OUT_SHORTFALL_LABEL)) {
	     setSeriesColor(series, new Color(255,51,0));
	     series.getStyle().setSymbolSize(0);
	     series.getStyle().setLineWidth(2);
	 } else if (lbl.equals(InventoryShortfallChartDataModel.PROJECTED_DUE_IN_SHORTFALL_LABEL)) {
	     setSeriesColor(series, new Color(40,245,245));
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
	for (int i = 0; i < chartDataView.getNumSeries(); i++) 
	    setSeriesColor(chartDataView.getSeries(i), 
			   colorTable.get(names[i]));
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

    public static Vector getScheduleTypesForLegend(String legendTitle) {
	//MWD
	Vector scheduleTypes = new Vector(3);
	if(legendTitle.equals(INVENTORY_LEGEND)) {
	    scheduleTypes.addElement(UISimpleNamedSchedule.ON_HAND);
	}
	else if(legendTitle.equals(REQUESTED_INVENTORY_LEGEND)) {
	    scheduleTypes.addElement(UISimpleNamedSchedule.REQUESTED_DUE_IN);
	    scheduleTypes.addElement(UISimpleNamedSchedule.REQUESTED_DUE_OUT);
	    scheduleTypes.addElement(UISimpleNamedSchedule.PROJECTED_REQUESTED_DUE_OUT);
	}
	else if(legendTitle.equals(ACTUAL_INVENTORY_LEGEND)) {
	    scheduleTypes.add(UISimpleNamedSchedule.DUE_IN);
	    scheduleTypes.add(UISimpleNamedSchedule.DUE_OUT);
	    scheduleTypes.add(UISimpleNamedSchedule.PROJECTED_DUE_OUT);
	}
	else if(legendTitle.equals(REQUESTED_SUPPLY_LEGEND)) {
	    scheduleTypes.addElement(UISimpleNamedSchedule.REQUESTED_DUE_IN);
	    scheduleTypes.addElement(UISimpleNamedSchedule.PROJECTED_REQUESTED_DUE_IN);
	}
	else if(legendTitle.equals(ACTUAL_SUPPLY_LEGEND)) {
	    scheduleTypes.add(UISimpleNamedSchedule.DUE_IN);
	    scheduleTypes.add(UISimpleNamedSchedule.PROJECTED_DUE_IN);
	}
	else if(legendTitle.equals(SHORTFALL_LEGEND)) {
	}
	else if(legendTitle.equals(TOTAL_CAPACITY_LEGEND)) {
	    scheduleTypes.addElement(UISimpleNamedSchedule.TOTAL_LABOR);
	    scheduleTypes.addElement(UISimpleNamedSchedule.ALLOCATED);
	}
	else if(legendTitle.equals(ALLOCATED_LEGEND)) {
	    scheduleTypes.addElement(UISimpleNamedSchedule.ALLOCATED);
	}
	else if(legendTitle.equals(UNCONFIRMED_LEGEND)) {
	    scheduleTypes.add(UISimpleNamedSchedule.UNCONFIRMED_DUE_IN);
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

    public static Vector extractOnHandSchedule(UISimpleInventory inventory) {

      Vector onHandSchedule=null;

      boolean inventoryFlag = 
	  inventory.getScheduleType().equals(PlanScheduleType.TOTAL_INVENTORY);

      if (inventoryFlag) {
	  UISimpleNamedSchedule tmp =
		inventory.getNamedSchedule(UISimpleNamedSchedule.ON_HAND);
	    if (tmp != null)
		onHandSchedule = tmp.getSchedule();
      }

      return onHandSchedule;
    }
	

    private InventoryChartDataModel createInventoryDataModel(UISimpleInventory inventory,
							     String legendTitle) {
	Vector scheduleNames = getScheduleTypesForLegend(legendTitle);
	return createInventoryDataModel(scheduleNames,inventory,legendTitle);
    }

    private InventoryChartDataModel 
	createInventoryDataModel(Vector scheduleNames,
				 UISimpleInventory inventory,
				 String legendTitle) {

	// create a vector of schedules that are part of this model
	Vector schedulesToPlot = 
	    extractSchedulesFromInventory(scheduleNames,inventory);

	boolean inventoryFlag = 
	    (inventory.getScheduleType().equals(PlanScheduleType.TOTAL_INVENTORY) ||
	     inventory.getScheduleType().equals(UIInventoryImpl.NO_INVENTORY_SCHEDULE_JUST_CONSUME));
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

    private void setSeriesColor(ChartDataViewSeries series, Color color) {
	series.getStyle().setLineColor(color);
	series.getStyle().setFillColor(color);
	series.getStyle().setSymbolColor(color);
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
		xaxis.setTickSpacing(7);
		xaxis.setGridSpacing(7);
		xaxis.setTickSpacingIsDefault(true);
		xaxis.setGridSpacingIsDefault(true);
		//xaxis.setMin(CDAY_MIN);
		xaxis.setGridVisible(true);
		xaxis.setAnnotationRotation(JCAxis.ROTATE_NONE);
	    }
	    else {
		xaxis.setAnnotationMethod(JCAxis.TIME_LABELS);
		xaxis.setTimeUnit(JCAxis.DAYS);
		xaxis.setTimeBase(new Date(InventoryChartBaseCalendar.getBaseTime()));
		//System.out.println("Base Date is: " + new Date(InventoryChartBaseCalendar.getBaseTime()));
		xaxis.setTickSpacing(7);
		xaxis.setGridSpacing(7);
		xaxis.setGridVisible(true);
		xaxis.setTimeFormat("M/d");
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


    /**
       Called from legend when user selects/deselects checkbox
       for a view.
    */
    public void setChartDataViewVisible(String name, boolean visible) {
	Enumeration enum = removeableViews.elements();
	while (enum.hasMoreElements()) {
	    ChartDataView cdv = (ChartDataView)enum.nextElement();
	    if (cdv.getName().equals(name)) {
		cdv.setVisible(visible);
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
	    // create the label
	    dayOfYear = ((int)x[pt]);
	    cDay = dayOfYear - daysFromBaseToCDay;
	}
	InventoryChartBaseCalendar tmpC = new InventoryChartBaseCalendar();
	tmpC.set(Calendar.YEAR, InventoryChartBaseCalendar.getBaseYear());
	tmpC.set(Calendar.DAY_OF_YEAR, dayOfYear);
	// add 1 to month as it numbers them from 0
	int month = tmpC.get(Calendar.MONTH) + 1;
	String label = "Date: " + month + "/" +
	    tmpC.get(Calendar.DAY_OF_MONTH) + "/" +
	    tmpC.get(Calendar.YEAR) + " " +
	    "(C" + cDay + ")" +
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
	if(timeStatusHandler != null) {
	    setAlpNow(alpNow);
	}
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

	    /*** MWD Took out and now again have reinitializeAndUpdate() -
		 below kinda flukey with new JChart software
	    **/ 

	    java.util.List viewList = chart.getDataView();
	    for (int i = 0; i < viewList.size(); i++) {
		ChartDataView chartDataView = (ChartDataView)viewList.get(i);
		//		ChartDataModel dm = (ChartDataModel) dataViews.get(chartDataView);
		ChartDataModel dm = chartDataView.getDataSource();
		if(dm != null) {
		    if (dm instanceof InventoryBaseChartDataModel) {
			InventoryBaseChartDataModel baseDM = 
			    (InventoryBaseChartDataModel) dm;
			//System.out.println("Setting view " + baseDM.getDataSourceName() + " to display c days:" + useCDays);
			baseDM.setDisplayCDays(useCDays);
			//chartDataView.setChanged(true,chartDataView.BASIC_CHANGE_MASK,true);
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


  class HorzLegendIcon implements Icon {

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
