/*
  Create legend for track plot data.
 */

package org.cougaar.domain.mlm.ui.planviewer.inventory;

import com.klg.jclass.chart.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import javax.swing.plaf.ButtonUI;

public class InventoryLegend extends JPanel {
    InventoryChart inventoryChart;
    int iconWidth;
    Insets insets;
    int gridx;
    Vector panels;
    JPanel demandSupplyPanel=null;


    
    final static Border defaultBorder=BorderFactory.createEmptyBorder(0,3,0,3);

    final static String checkBoxUIClassID=(new JCheckBox()).getUIClassID();

    final static int TITLES_GRIDX=0;
    final static int REQUESTED_GRIDX=1;
    final static int ACTUAL_GRIDX=2;

    final static int SUPPLY_TITLE_GRIDY=1;
    final static int DEMAND_TITLE_GRIDY=2;
    final static int ACTUAL_DEMAND_TITLE_GRIDY=3;
    final static int PROJECTED_DEMAND_TITLE_GRIDY=4;

    final static String SUPPLY_TITLE="Resupply";
    final static String DEMAND_TITLE="Demand";
    final static String ACTUAL_DEMAND_TITLE="   Actual ";
    final static String PROJECTED_DEMAND_TITLE="   Projected ";

    final static String RECEIVED_LABEL="Received";
    final static String REQUESTED_LABEL="Requested ";
    final static String SHIPPED_LABEL="Shipped";
    final static String CONSUMED_LABEL="Consumed";
    final static String NONE_LABEL="-None";
    final static String EMPTY_LABEL="   ";

    public InventoryLegend(InventoryChart inventoryChart, JCChart chart) {
	this.inventoryChart = inventoryChart;
	
	//     int iconWidth = getFontMetrics(getFont()).getHeight() / 2;
	iconWidth = getFontMetrics(getFont()).getHeight();
	setLayout(new GridBagLayout());
	insets = new Insets(0, 0, 0, 0);
	panels = new Vector();

	setBorder(defaultBorder);
	initializeDataView(chart);

	/****  MWD code for old display
	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	List chartDataViews = chart.getDataView();
	ListIterator i = chartDataViews.listIterator();
	gridx = 0; // grid cell 
	while (i.hasNext()) {
	    displayDataView((ChartDataView)i.next());
	}
	**/
    }

    protected void initializeDataView(JCChart chart) {
	List chartDataViews = chart.getDataView();
	ListIterator i = chartDataViews.listIterator();
	gridx = 0; // grid cell 
	while (i.hasNext()) {
	    ChartDataView chartDataView=(ChartDataView)i.next();
	    String name = chartDataView.getName();
	    if(name.equals(InventoryChart.REQUESTED_INVENTORY_LEGEND) ||
	       (name.equals(InventoryChart.ACTUAL_INVENTORY_LEGEND))) {
		addDemandSupplyDataView(chartDataView);
	    }
	    else {
		addOtherDataView(chartDataView);
	    }
	}

	for(int j=0; j<panels.size(); j++) {
	    JPanel newPanel = (JPanel)panels.elementAt(j);
	    add(newPanel, 
		new GridBagConstraints(j,0 , 1, 1, 0.0, 0.0,
				       GridBagConstraints.NORTH, 
				       GridBagConstraints.NONE, 
				       insets, 0, 0));
	}
    }

    protected void addDemandSupplyDataView(ChartDataView chartDataView) {

	String name = chartDataView.getName();
	JComponent component;
	Color color;
	JLabel label;
	SeriesToggleButton button;
	JLabel demandValue=null;
	int j=0;
	boolean madeDemandLabel=false;
	boolean skippedDemandLabel=false;

	ChartDataViewSeries series;

	int gridX=-1;
	int titleY=SUPPLY_TITLE_GRIDY;

	if(name.equals(InventoryChart.REQUESTED_INVENTORY_LEGEND))
	    gridX=REQUESTED_GRIDX;
	else
	    gridX=ACTUAL_GRIDX;
	    
	    


	initDemandSupplyPanel();

	// add checkbtn or label for the view
	int y = 0;
	if (inventoryChart.isChartDataViewSelectable(chartDataView)) {
	    component = createCheckBox(name,chartDataView.isVisible());
	} else {
	    component = createLabel(name);
	}
	demandSupplyPanel.add(component,
	    new GridBagConstraints(gridX, y++, 1, 1, 0.0, 0.0,
				   GridBagConstraints.WEST, 
				   GridBagConstraints.NONE, 
				   insets, 0, 0));
	// add label w/ color coded circle for each series
	int numSeries = chartDataView.getNumSeries();
	if (numSeries == 0) {
	    label = new JLabel("No "+name);
	    label.setForeground(Color.black);
	    demandSupplyPanel.add(label, 
				  new GridBagConstraints(gridX, y++, 1, 1, 0.0, 0.0,
							 GridBagConstraints.WEST, 
							 GridBagConstraints.NONE, 
							 insets, 0, 0));
	}
	for (j = 0; j < numSeries; j++) {
	    series = chartDataView.getSeries(j);
	    color = series.getStyle().getSymbolColor();
	    String seriesName = series.getLabel();
	    String labelName="";

	    if(name.equals(InventoryChart.REQUESTED_INVENTORY_LEGEND)) {
		
		//Setup the left hand titles
		if(seriesName.equals(InventoryChartDataModel.REQUESTED_DUE_IN_LABEL) ||
		   seriesName.equals(InventoryChartDataModel.UNCONFIRMED_DUE_IN_LABEL)) { 
		    component = createLabel(SUPPLY_TITLE);
		    demandSupplyPanel.add(component, 
				 new GridBagConstraints(TITLES_GRIDX, titleY++, 1, 1, 0.0, 0.0,
							GridBagConstraints.WEST, 
							GridBagConstraints.NONE, 
							insets, 0, 0));	

		    if(!madeDemandLabel) {
			component = createLabel(DEMAND_TITLE);
			demandSupplyPanel.add(component, 
					      new GridBagConstraints(TITLES_GRIDX, 
								     titleY, 
								     1, 1, 0.0, 0.0,
								     GridBagConstraints.WEST, 
								     GridBagConstraints.NONE, 
								     insets, 0, 0));
			demandValue = createLabel(NONE_LABEL);
			demandSupplyPanel.add(demandValue, 
					      new GridBagConstraints(TITLES_GRIDX + 1,
								     titleY,
								     1, 1, 0.0, 0.0,
								     GridBagConstraints.WEST, 
								     GridBagConstraints.NONE, 
								     insets, 0, 0));
			madeDemandLabel=true;
			titleY++;
		    }
		}
		else {
		    //if there is demand erase the NONE_LABEL
		    if(demandValue != null) {
			demandValue.setText(EMPTY_LABEL);
		    }

		    if(seriesName.equals(InventoryChartDataModel.REQUESTED_DUE_OUT_LABEL)) {
			component = createLabel(ACTUAL_DEMAND_TITLE);
		    }
		    else {
			component = createLabel(PROJECTED_DEMAND_TITLE);
		    }
		    demandSupplyPanel.add(component, 
					  new GridBagConstraints(TITLES_GRIDX, titleY++, 
								 1, 1, 0.0, 0.0,
								 GridBagConstraints.WEST, 
								 GridBagConstraints.NONE, 
								 insets, 0, 0));

		}
		//Setup Requested label
		labelName = REQUESTED_LABEL;			
	    }
	    else {
		//Setup actual label
		if(seriesName.equals(InventoryChartDataModel.ACTUAL_DUE_OUT_SHIPPED_LABEL) ||
		   (seriesName.equals(InventoryChartDataModel.ACTUAL_PROJECTED_DUE_OUT_SHIPPED_LABEL))) {
		    labelName=SHIPPED_LABEL;
		}
		else if(seriesName.equals(InventoryChartDataModel.ACTUAL_DUE_OUT_CONSUMED_LABEL)
			||
			(seriesName.equals(InventoryChartDataModel.ACTUAL_PROJECTED_DUE_OUT_CONSUMED_LABEL))){
		    labelName = CONSUMED_LABEL;
		}
		else {
		    labelName = RECEIVED_LABEL;
		}    
	    }   
	    
	    /***
	    label = new JLabel(labelName,
			       new CircleIcon(color, iconWidth),
			       SwingConstants.RIGHT);
	    label.setForeground(Color.black);
	    ***/
	    
	    button = new SeriesToggleButton(labelName,iconWidth,
					    j,series);
	    
	    demandSupplyPanel.add(button, 
				  new GridBagConstraints(gridX, y++, 1, 1, 0.0, 0.0,
							 GridBagConstraints.WEST, 
							 GridBagConstraints.NONE, 
							 insets, 0, 0));
	    // Skip over Demand label
	    if(!skippedDemandLabel) {
		y++;
		skippedDemandLabel=true;
	    }
	}
    }


protected void addOtherDataView(ChartDataView chartDataView) {

	String name = chartDataView.getName();
	JComponent component;
	Color color;
	JLabel label;	

	ChartDataViewSeries series;
	int gridX = 0;

	JPanel newPanel = new JPanel();
	newPanel.setLayout(new GridBagLayout());
	newPanel.setBorder(defaultBorder);
	

	// add checkbtn or label for the view
	int y = 0;
	if (inventoryChart.isChartDataViewSelectable(chartDataView)) {
	    component = createCheckBox(name,chartDataView.isVisible());
	} else {
	    component = createLabel(name);
	}
	newPanel.add(component,
	    new GridBagConstraints(gridX, y++, 1, 1, 0.0, 0.0,
				   GridBagConstraints.WEST, 
				   GridBagConstraints.NONE, 
				   insets, 0, 0));
	// add label w/ color coded circle for each series
	int numSeries = chartDataView.getNumSeries();
	if (numSeries == 0) {
	    label = new JLabel("No "+name);
	    label.setForeground(Color.black);
	    newPanel.add(label, 
			 new GridBagConstraints(gridX, y++, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, 
						GridBagConstraints.NONE, 
						insets, 0, 0));
	}
	for (int j = 0; j < numSeries; j++) {
	    series = chartDataView.getSeries(j);
	    color = series.getStyle().getSymbolColor();
	    label = new JLabel(series.getLabel(),
			       new CircleIcon(color, iconWidth),
			       SwingConstants.RIGHT);
	    label.setForeground(Color.black);
	    newPanel.add(label, 
			 new GridBagConstraints(gridX, y++, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, 
						GridBagConstraints.NONE, 
						insets, 0, 0));
	}

	panels.add(newPanel);
    }


    protected void displayDataView(ChartDataView chartDataView) {
	String name = chartDataView.getName();
	JComponent component;
	Color color;
	JLabel label;
	ChartDataViewSeries series;

	// add checkbtn or label for the view
	int y = 0;
	if (inventoryChart.isChartDataViewSelectable(chartDataView)) {
	    component = createCheckBox(name,chartDataView.isVisible());
	} else {
	    component = createLabel(name);
	}
	add(component,
	    new GridBagConstraints(gridx, y++, 1, 1, 0.0, 0.0,
				   GridBagConstraints.WEST, 
				   GridBagConstraints.NONE, 
				   insets, 0, 0));
	// add label w/ color coded circle for each series
	int numSeries = chartDataView.getNumSeries();
	if (numSeries == 0) {
	    label = new JLabel("No "+name);
	    label.setForeground(Color.black);
	    add(label, 
		new GridBagConstraints(gridx, y++, 1, 1, 0.0, 0.0,
				       GridBagConstraints.WEST, 
				       GridBagConstraints.NONE, 
				       insets, 0, 0));
	}
	for (int j = 0; j < numSeries; j++) {
	    series = chartDataView.getSeries(j);
	    color = series.getStyle().getSymbolColor();
	    label = new JLabel(series.getLabel(),
			       new CircleIcon(color, iconWidth),
			       SwingConstants.RIGHT);
	    label.setForeground(Color.black);
	    add(label, 
		new GridBagConstraints(gridx, y++, 1, 1, 0.0, 0.0,
				       GridBagConstraints.WEST, 
				       GridBagConstraints.NONE, 
				       insets, 0, 0));
	}
	gridx++;
    }


    protected JCheckBox createCheckBox(String name, boolean selected) {
	JCheckBox cb = new JCheckBox(name);
	cb.setSelected(selected);
	cb.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		JCheckBox cb1 = (JCheckBox)e.getSource();
		inventoryChart.setChartDataViewVisible(cb1.getText(), cb1.isSelected());
// 		checkBox_actionPerformed(e);
	    }
	});
	return cb;
    }

    protected JLabel createLabel(String name) {
	JLabel label = new JLabel(name);
	label.setForeground(Color.black);
	return label;
    }
    
    void checkBox_actionPerformed(ActionEvent e) {
	JCheckBox cb = (JCheckBox)e.getSource();
	inventoryChart.setChartDataViewVisible(cb.getText(), cb.isSelected());
    }

    protected void initDemandSupplyPanel() {
	if(demandSupplyPanel == null) {
	    demandSupplyPanel = new JPanel();
	    demandSupplyPanel.setLayout(new GridBagLayout());
	    demandSupplyPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
	    JLabel component = createLabel(EMPTY_LABEL);
	    demandSupplyPanel.add(component,
	    new GridBagConstraints(0,0, 1, 1, 0.0, 0.0,
				   GridBagConstraints.WEST, 
				   GridBagConstraints.NONE, 
				   insets, 0, 0));
	    panels.add(demandSupplyPanel);
	}
    }
    

    static class CircleIcon implements Icon {
	public Color color;
	public int diameter;
	public boolean drawEmpty;

	public CircleIcon(Color color, int diameter, boolean doDrawEmpty) {
	    this.color = color;
	    this.diameter = diameter;
	    this.drawEmpty = doDrawEmpty;
	}

	public CircleIcon(Color color, int diameter) {
	    this(color,diameter,false);
	}

	public int getIconWidth() {
	    return diameter;
	}

	public int getIconHeight() {
	    return diameter;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
	    g.setColor(color);
	    if(drawEmpty) {
		g.drawOval(x, y, diameter, diameter);
	    }
	    else {
		g.drawOval(x, y, diameter, diameter);
		g.fillOval(x, y, diameter, diameter);
	    }
	}

	public void setDrawEmpty(boolean doDrawEmpty) { 
	    this.drawEmpty = doDrawEmpty;
	}
	
    }

    public class SeriesToggleButton extends JToggleButton {
	private ChartDataViewSeries series=null;

	private int seriesIndex;
	private ChartDataView dataView;
	private InventoryChartDataModel dm;


	public SeriesToggleButton(String aLabel,
				  int aDiameter,
				  int aSeriesIndex,
				  ChartDataViewSeries aSeries) {
	    super(aLabel,new CircleIcon(aSeries.getStyle().getSymbolColor(),
					aDiameter,true), true);
	    this.seriesIndex = aSeriesIndex;
	    this.dataView = aSeries.getParent();
	    this.dm = (InventoryChartDataModel) 
		inventoryChart.getChartDataModel(this.dataView);
 
	    this.series = aSeries;
	    this.setForeground(Color.black);
	    setSelectedIcon(new CircleIcon(aSeries.getStyle().getSymbolColor(),aDiameter,false));

	    //setOpaque(true);
	    setBorderPainted(false);
	    setHorizontalAlignment(LEFT);
	    
	    //MWD remove
	    //Color color = series.getStyle().getSymbolColor();
	}
	
	protected void fireItemStateChanged(ItemEvent e) {
	    super.fireItemStateChanged(e);
	    if(series != null) {
		//		System.out.println("InventoryLegend::SeriesToggleButton - " + series.getLabel() + "item selected: " + (e.getStateChange()==e.SELECTED));
		//series.setVisible(e.getStateChange()==e.SELECTED);
		dm.setSeriesVisible(seriesIndex,(e.getStateChange()==e.SELECTED));
	    }
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


}
