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
import java.util.HashMap;
import java.util.Map;
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

    final static int TITLES_GRIDX    = 1;
    final static int REQUESTED_GRIDX = 2;
    final static int ACTUAL_GRIDX    = 3;
    final static int SHORTFALL_GRIDX = 4;
    final static int INVENTORY_GRIDX = 0;

    final static int SUPPLY_TITLE_GRIDY=1;
    final static int DEMAND_TITLE_GRIDY=2;
    final static int ACTUAL_DEMAND_TITLE_GRIDY=3;
    final static int PROJECTED_DEMAND_TITLE_GRIDY=4;

//      final static String SUPPLY_TITLE="Resupply";
//      final static String DEMAND_TITLE =           "Demand";
    final static String ACTUAL_DEMAND_TITLE    = "Actual Demand ";
    final static String PROJECTED_DEMAND_TITLE = "Projected Demand ";
    final static String ACTUAL_SUPPLY_TITLE    = "Actual Resupply ";
    final static String PROJECTED_SUPPLY_TITLE = "Projected Resupply ";

    final static String RECEIVED_LABEL  = "Received";
    final static String REQUESTED_LABEL = "Requested ";
    final static String SHIPPED_LABEL   = "Accepted";
    final static String CONSUMED_LABEL  = "Consumed";
    final static String PROMISED_LABEL  = "Accepted";
    final static String NONE_LABEL      = "-None";
    final static String EMPTY_LABEL     = "   ";

    private int extraGridX = INVENTORY_GRIDX;

    public InventoryLegend(InventoryChart inventoryChart, JCChart chart) {
	this.inventoryChart = inventoryChart;
	
	//     int iconWidth = getFontMetrics(getFont()).getHeight() / 2;
	
	// Circle icon width
	//iconWidth = getFontMetrics(getFont()).getHeight();
	
	// Square icon width
	iconWidth = getFontMetrics(getFont()).getHeight()-2;

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
	       (name.equals(InventoryChart.ACTUAL_INVENTORY_LEGEND)) ||
	       (name.equals(InventoryChart.REQUESTED_INACTIVE_INVENTORY_LEGEND)) ||
	       (name.equals(InventoryChart.ACTUAL_INACTIVE_INVENTORY_LEGEND))||
	       (name.equals(InventoryChart.REQUESTED_SUPPLY_LEGEND)) ||
	       (name.equals(InventoryChart.ACTUAL_SUPPLY_LEGEND))) {

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
	JLabel supplyValue=null;
	int j=0;

	ChartDataViewSeries series;

	int gridX=-1;
	int titleY=SUPPLY_TITLE_GRIDY;


	if((name.equals(InventoryChart.REQUESTED_INACTIVE_INVENTORY_LEGEND)) ||
	   (name.equals(InventoryChart.ACTUAL_INACTIVE_INVENTORY_LEGEND))) {
	    return;
	}

	if((name.equals(InventoryChart.REQUESTED_INVENTORY_LEGEND)) ||
	   (name.equals(InventoryChart.REQUESTED_SUPPLY_LEGEND)))
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

	    if ((name.equals(InventoryChart.REQUESTED_INVENTORY_LEGEND)) ||
	       (name.equals(InventoryChart.REQUESTED_SUPPLY_LEGEND))) {
		
		//Setup the left hand titles for dueins
		if (seriesName.equals(InventoryChartDataModel.REQUESTED_DUE_IN_LABEL) ||
		   seriesName.equals(InventoryChartDataModel.REQUESTED_PROJECTED_DUE_IN_LABEL) ||
		   seriesName.equals(InventoryChartDataModel.UNCONFIRMED_DUE_IN_LABEL)) { 
                    if (seriesName.equals(InventoryChartDataModel.REQUESTED_PROJECTED_DUE_IN_LABEL)) {	
                        component = createLabel(PROJECTED_SUPPLY_TITLE);
                    } else {
                        component = createLabel(ACTUAL_SUPPLY_TITLE);
                    }
                    demandSupplyPanel.add(component, 
				 new GridBagConstraints(TITLES_GRIDX, titleY++, 1, 1, 0.0, 0.0,
							GridBagConstraints.WEST, 
							GridBagConstraints.NONE, 
							insets, 0, 0));	

		    //Not just supply (consumer, no inventory) and haven't put up demand title
		    if (name.equals(InventoryChart.REQUESTED_SUPPLY_LEGEND)) {
			if (supplyValue == null) {
			    supplyValue = createLabel(EMPTY_LABEL);
			    demandSupplyPanel.add(supplyValue, 
						  new GridBagConstraints(TITLES_GRIDX + 1,
									 titleY-1,
									 1, 1, 0.0, 0.0,
									 GridBagConstraints.WEST, 
									 GridBagConstraints.NONE, 
									 insets, 0, 0));
			}
			if (seriesName.equals(InventoryChartDataModel.REQUESTED_DUE_IN_LABEL)){
			    component = createLabel(ACTUAL_SUPPLY_TITLE);
			}
			else {
			    component = createLabel(PROJECTED_SUPPLY_TITLE);
			}
			demandSupplyPanel.add(component, 
					      new GridBagConstraints(TITLES_GRIDX, titleY++, 
								     1, 1, 0.0, 0.0,
								     GridBagConstraints.WEST, 
								     GridBagConstraints.NONE, 
								     insets, 0, 0));
		    }
		} else {        // Setup for dueouts
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
	    } else {
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
		else if (seriesName.equals(InventoryChartDataModel.ACTUAL_PROJECTED_DUE_IN_LABEL)) {
		    labelName = PROMISED_LABEL;
		    
		}
		else {
		    labelName = RECEIVED_LABEL;
		}    
	    }   
	    
	    button = new SeriesToggleButton(labelName, iconWidth, j, series);

	    // Skip over Demand label
	    demandSupplyPanel.add(button, 
				  new GridBagConstraints(gridX, y++, 1, 1, 0.0, 0.0,
							 GridBagConstraints.WEST, 
							 GridBagConstraints.NONE, 
							 insets, 0, 0));
	}
    }


    protected void addOtherDataView(ChartDataView chartDataView) {

	String name = chartDataView.getName();
	JComponent component;
	Color color;
	JLabel label;	

	ChartDataViewSeries series;

//  	JPanel newPanel = new JPanel();
//  	newPanel.setLayout(new GridBagLayout());
//  	newPanel.setBorder(defaultBorder);
	
	initDemandSupplyPanel();

	// add checkbtn or label for the view
	int y = 0;
	if (inventoryChart.isChartDataViewSelectable(chartDataView)) {
	    component = createCheckBox(name,chartDataView.isVisible());
	} else {
	    component = createLabel(name);
	}
	demandSupplyPanel.add(component,
	    new GridBagConstraints(extraGridX, y++, 1, 1, 0.0, 0.0,
				   GridBagConstraints.WEST, 
				   GridBagConstraints.NONE, 
				   insets, 0, 0));
	// add label w/ color coded circle for each series
	int numSeries = chartDataView.getNumSeries();
	if (numSeries == 0) {
	    label = new JLabel("No "+name);
	    label.setForeground(Color.black);
	    demandSupplyPanel.add(label, 
			 new GridBagConstraints(extraGridX, y++, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, 
						GridBagConstraints.NONE, 
						insets, 0, 0));
	}
	for (int j = 0; j < numSeries; j++) {
	    series = chartDataView.getSeries(j);
	    color = series.getStyle().getSymbolColor();
	    label = new JLabel(series.getLabel(),
			       new SquareIcon(color, iconWidth),
			       SwingConstants.RIGHT);
	    label.setForeground(Color.black);
	    demandSupplyPanel.add(label, 
			 new GridBagConstraints(extraGridX, y++, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, 
						GridBagConstraints.NONE, 
						insets, 0, 0));
	}

        if (extraGridX == INVENTORY_GRIDX) {
            extraGridX = SHORTFALL_GRIDX;
        } else {
            extraGridX++;
        }

//  	panels.add(newPanel);
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
			       new SquareIcon(color, iconWidth),
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
		String viewName = cb1.getText();
		String assocName=null;
		inventoryChart.setChartDataViewVisible(viewName, cb1.isSelected());
		if(viewName.equals(InventoryChart.REQUESTED_INVENTORY_LEGEND))
		    assocName = InventoryChart.REQUESTED_INACTIVE_INVENTORY_LEGEND;
		else if(viewName.equals(InventoryChart.ACTUAL_INVENTORY_LEGEND))
		    assocName = InventoryChart.ACTUAL_INACTIVE_INVENTORY_LEGEND;
		if(assocName != null)
		    inventoryChart.setChartDataViewVisible(assocName, cb1.isSelected());
	    }

	});
	return cb;
    }

    protected JLabel createLabel(String name) {
	JLabel label = new JLabel(name);
	label.setForeground(Color.black);
	return label;
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
    

    static Map renderingHints = new HashMap();
    static {
        renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    static BasicStroke wideStroke = new BasicStroke(3f);

    static class CircleIcon implements Icon {
	public Color color;
	public int diameter;
	public boolean drawEmpty;
        public boolean isButton;

	public CircleIcon(Color color, int diameter, boolean doDrawEmpty) {
	    this(color, diameter, doDrawEmpty, true);
        }

	private CircleIcon(Color color, int diameter, boolean doDrawEmpty, boolean isButton) {
	    this.color = color;
	    this.diameter = diameter;
	    this.drawEmpty = doDrawEmpty;
            this.isButton = isButton;
	}

	public CircleIcon(Color color, int diameter) {
	    this(color, diameter, false, false);
	}

	public int getIconWidth() {
	    return diameter;
	}

	public int getIconHeight() {
	    return diameter;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setStroke(wideStroke);
            g2d.setRenderingHints(renderingHints);
	    if(drawEmpty) {
                g2d.setColor(color);
		g2d.drawOval(x+1, y+1, diameter-2, diameter-2);
	    }
	    else {
                if (isButton) {
                    g2d.setColor(color);
                    g2d.fillOval(x, y, diameter, diameter);
//                      g2d.setColor(color);
//                      g2d.fillOval(x+2, y+2, diameter-4, diameter-4);
                    g2d.setColor(Color.black);
                    g2d.drawOval(x+1, y+1, diameter-2, diameter-2);
                } else {
                    g2d.setColor(color);
                    g2d.fillOval(x, y, diameter, diameter);
                }
	    }
            g2d.dispose();
	}

	public void setDrawEmpty(boolean doDrawEmpty) { 
	    this.drawEmpty = doDrawEmpty;
	}
	
    }


    static class SquareIcon implements Icon {
	public Color color;
	public int length;
	public boolean drawEmpty;
        public boolean isButton;

	public SquareIcon(Color color, int length, boolean doDrawEmpty) {
	    this(color, length, doDrawEmpty, true);
        }

	private SquareIcon(Color color, int length, boolean doDrawEmpty, boolean isButton) {
	    this.color = color;
	    this.length = length;
	    this.drawEmpty = doDrawEmpty;
            this.isButton = isButton;
	}

	public SquareIcon(Color color, int length) {
	    this(color, length, false, false);
	}

	public int getIconWidth() {
	    return length;
	}

	public int getIconHeight() {
	    return length;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setStroke(wideStroke);
            g2d.setRenderingHints(renderingHints);
	    if(drawEmpty) {
                g2d.setColor(color);
		g2d.drawRect(x+1, y+1, length-2, length-2);
	    }
	    else {
                if (isButton) {
                    g2d.setColor(color);
                    g2d.fillRect(x, y, length, length);
//                      g2d.setColor(color);
//                      g2d.fillRect(x+2, y+2, length-4, length-4);
                    g2d.setColor(Color.black);
                    g2d.drawRect(x+1, y+1, length-2, length-2);
                } else {
                    g2d.setColor(color);
                    g2d.fillRect(x, y, length, length);
                }
	    }
            g2d.dispose();
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
				  int aLength,
				  int aSeriesIndex,
				  ChartDataViewSeries aSeries) {
	    super(aLabel,new SquareIcon(aSeries.getStyle().getSymbolColor(),
					aLength,true), true);
	    this.seriesIndex = aSeriesIndex;
	    this.dataView = aSeries.getParent();
	    this.dm = (InventoryChartDataModel) 
		inventoryChart.getChartDataModel(this.dataView);
 
	    this.series = aSeries;
	    this.setForeground(Color.black);
	    setSelectedIcon(new SquareIcon(aSeries.getStyle().getSymbolColor(),aLength,false));

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
		boolean ImSelected = (e.getStateChange()==e.SELECTED);

		dm.setSeriesVisible(seriesIndex,ImSelected);
		dm.getAssociatedInactiveModel().setSeriesVisible(seriesIndex,
								 ImSelected);
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
